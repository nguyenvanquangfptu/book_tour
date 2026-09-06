package myproject.booking_tour.scheduler;

import myproject.booking_tour.entity.Payment;
import myproject.booking_tour.repository.PaymentRepository;
import myproject.booking_tour.service.PayOSReconciliationService;
import myproject.booking_tour.service.PayOSStatusReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tinh chat quan trong nhat cua lop nay: MOT PAYMENT HONG CHI LAM HONG CHINH NO.
 *
 * Phien ban truoc goi toan bo vong lap trong mot @Transactional. Mot payment gap
 * su co la ca luot quet bi huy - ke ca nhung payment da xac nhan thanh cong
 * truoc do. Cac test duoi day khoa chat viec do khong duoc tai dien.
 */
@ExtendWith(MockitoExtension.class)
class PayOSReconciliationSchedulerTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PayOSStatusReader payOSStatusReader;

    @Mock
    private PayOSReconciliationService reconciliationService;

    @InjectMocks
    private PayOSReconciliationScheduler scheduler;

    private Payment pending(long id, String orderCode) {
        Payment p = new Payment();
        p.setId(id);
        p.setOrderCode(orderCode);
        p.setPaymentStatus("PENDING");
        return p;
    }

    private void givenPending(Payment... payments) {
        when(paymentRepository.findByPaymentStatusAndOrderCodeNotNull("PENDING"))
                .thenReturn(List.of(payments));
    }

    @Test
    void shouldKeepProcessing_WhenOnePaymentFailsToApply() {
        givenPending(pending(1L, "1001"), pending(2L, "1002"), pending(3L, "1003"));
        when(payOSStatusReader.statusOf(anyString())).thenReturn("PAID");

        // Payment 2 gap su co - vi du chu tai khoan da xoa tai khoan nen
        // cancelBooking khong tim thay user.
        when(reconciliationService.applyStatus(eq(2L), anyString()))
                .thenThrow(new RuntimeException("User not found"));
        when(reconciliationService.applyStatus(eq(1L), anyString())).thenReturn(true);
        when(reconciliationService.applyStatus(eq(3L), anyString())).thenReturn(true);

        assertDoesNotThrow(() -> scheduler.reconcilePendingPayments());

        // Ca ba deu duoc thu, khong dung lai o cai hong.
        verify(reconciliationService).applyStatus(1L, "PAID");
        verify(reconciliationService).applyStatus(2L, "PAID");
        verify(reconciliationService).applyStatus(3L, "PAID");
    }

    @Test
    void shouldKeepProcessing_WhenPayOSLookupFails() {
        givenPending(pending(1L, "1001"), pending(2L, "1002"));
        when(payOSStatusReader.statusOf("1001")).thenThrow(new RuntimeException("Connection timed out"));
        when(payOSStatusReader.statusOf("1002")).thenReturn("PAID");

        assertDoesNotThrow(() -> scheduler.reconcilePendingPayments());

        // Payment 1 hoi PayOS that bai -> khong duoc ghi gi ca...
        verify(reconciliationService, never()).applyStatus(eq(1L), anyString());
        // ...nhung payment 2 van duoc xu ly binh thuong.
        verify(reconciliationService).applyStatus(2L, "PAID");
    }

    /**
     * Hoi PayOS phai xay ra TRUOC khi mo transaction ghi. Neu goi mang nam ben
     * trong transaction, mot lan timeout se giu transaction mo hang chuc giay.
     */
    @Test
    void shouldAskPayOS_BeforeOpeningWriteTransaction() {
        givenPending(pending(1L, "1001"));
        when(payOSStatusReader.statusOf("1001")).thenReturn("CANCELLED");

        scheduler.reconcilePendingPayments();

        InOrder inOrder = inOrder(payOSStatusReader, reconciliationService);
        inOrder.verify(payOSStatusReader).statusOf("1001");
        inOrder.verify(reconciliationService).applyStatus(1L, "CANCELLED");
    }

    @Test
    void shouldDoNothing_WhenNoPendingPayments() {
        when(paymentRepository.findByPaymentStatusAndOrderCodeNotNull("PENDING"))
                .thenReturn(List.of());

        scheduler.reconcilePendingPayments();

        verifyNoInteractions(payOSStatusReader, reconciliationService);
    }
}
