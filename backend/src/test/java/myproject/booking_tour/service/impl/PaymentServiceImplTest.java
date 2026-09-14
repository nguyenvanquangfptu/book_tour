package myproject.booking_tour.service.impl;

import myproject.booking_tour.dto.request.PaymentRequest;
import myproject.booking_tour.dto.response.PaymentResponse;
import myproject.booking_tour.entity.Booking;
import myproject.booking_tour.entity.Payment;
import myproject.booking_tour.mapper.PaymentMapper;
import myproject.booking_tour.repository.BookingRepository;
import myproject.booking_tour.repository.PaymentRepository;
import myproject.booking_tour.service.PayOSReconciliationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.payos.PayOS;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private PayOSReconciliationService reconciliationService;
    @Mock(answer = org.mockito.Answers.RETURNS_DEEP_STUBS)
    private PayOS payOS;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Booking mockBooking;

    @BeforeEach
    void setUp() {
        mockBooking = new Booking();
        mockBooking.setId(1L);
        mockBooking.setTotalPrice(BigDecimal.valueOf(1000));
    }

    @Test
    void createPayment_ShouldSavePayment() {
        PaymentRequest request = new PaymentRequest();
        request.setBookingId(1L);
        request.setPaymentMethod("CASH");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(mockBooking));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(10L);
            return p;
        });
        when(paymentMapper.toResponse(any(Payment.class))).thenReturn(new PaymentResponse());

        PaymentResponse response = paymentService.createPayment(request);

        assertNotNull(response);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void getAllPayments_ShouldQueryOnlyTheCustomersOwnRows() {
        // Trước đây là findAll() rồi lọc trong Java: một khách xem hóa đơn của
        // mình kéo cả bảng payments vào memory để trả về vài dòng.
        when(paymentRepository.findByBookingUserId(7L)).thenReturn(java.util.List.of(new Payment()));
        when(paymentMapper.toResponse(any(Payment.class))).thenReturn(new PaymentResponse());

        assertEquals(1, paymentService.getAllPayments(7L, false).size());

        verify(paymentRepository, never()).findAll();
    }

    @Test
    void getAllPayments_ShouldReturnEverythingForAdmin() {
        when(paymentRepository.findAll()).thenReturn(java.util.List.of(new Payment(), new Payment()));
        when(paymentMapper.toResponse(any(Payment.class))).thenReturn(new PaymentResponse());

        assertEquals(2, paymentService.getAllPayments(7L, true).size());

        verify(paymentRepository, never()).findByBookingUserId(any());
    }

    private Booking bookingOwnedBy(long ownerId, String status) {
        myproject.booking_tour.entity.User owner = new myproject.booking_tour.entity.User();
        owner.setId(ownerId);
        mockBooking.setUser(owner);
        mockBooking.setStatus(status);
        return mockBooking;
    }

    @Test
    void createPaymentUrl_ShouldRefuse_WhenBookingBelongsToSomeoneElse() {
        // Endpoint /api/payments/** chỉ đòi một tài khoản bất kỳ. Không có chốt
        // này thì ai cũng tạo được link thanh toán cho đơn của người khác, đọc ra
        // số tiền phải trả của họ và rải bản ghi payment rác vào đơn của họ.
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(bookingOwnedBy(7L, "CONFIRMED")));

        myproject.booking_tour.exception.BadRequestException ex = assertThrows(
                myproject.booking_tour.exception.BadRequestException.class,
                () -> paymentService.createPaymentUrl(1L, 99L, false));

        assertTrue(ex.getMessage().contains("không có quyền"), ex.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
        verifyNoInteractions(payOS);
    }

    @Test
    void createPaymentUrl_ShouldRefuse_WhenBookingAlreadyCancelled() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(bookingOwnedBy(7L, "CANCELLED")));

        myproject.booking_tour.exception.BadRequestException ex = assertThrows(
                myproject.booking_tour.exception.BadRequestException.class,
                () -> paymentService.createPaymentUrl(1L, 7L, false));

        assertTrue(ex.getMessage().contains("đã hủy"), ex.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void createPaymentUrl_ShouldRefuse_WhenBookingAlreadyPaid() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(bookingOwnedBy(7L, "PAID")));

        myproject.booking_tour.exception.BadRequestException ex = assertThrows(
                myproject.booking_tour.exception.BadRequestException.class,
                () -> paymentService.createPaymentUrl(1L, 7L, false));

        assertTrue(ex.getMessage().contains("đã thanh toán"), ex.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void createPaymentUrl_ShouldLetAdminPastTheOwnershipCheck() {
        // Admin tạo link hộ khách là hợp lệ; đơn chưa duyệt mới là thứ chặn nó
        // lại. Lỗi trả về phải nói về trạng thái, không phải về quyền.
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(bookingOwnedBy(7L, "PENDING")));

        myproject.booking_tour.exception.BadRequestException ex = assertThrows(
                myproject.booking_tour.exception.BadRequestException.class,
                () -> paymentService.createPaymentUrl(1L, 99L, true));

        assertTrue(ex.getMessage().contains("chưa được duyệt"), ex.getMessage());
        assertFalse(ex.getMessage().contains("không có quyền"), ex.getMessage());
    }

    private Payment pendingPayment(String orderCode) {
        Payment payment = new Payment();
        payment.setId(10L);
        payment.setOrderCode(orderCode);
        payment.setPaymentStatus("PENDING");
        payment.setBooking(bookingOwnedBy(7L, "PAID"));
        return payment;
    }

    // Builder cua SDK doi du moi truong @NonNull, nen dung mock thay vi dung
    // doi tuong that chi de mang mot trang thai.
    private static vn.payos.model.v2.paymentRequests.PaymentLink linkIn(
            vn.payos.model.v2.paymentRequests.PaymentLinkStatus status) {
        vn.payos.model.v2.paymentRequests.PaymentLink link = mock(vn.payos.model.v2.paymentRequests.PaymentLink.class);
        when(link.getStatus()).thenReturn(status);
        return link;
    }

    private static vn.payos.model.webhooks.WebhookData webhookFor(long orderCode) {
        vn.payos.model.webhooks.WebhookData data = mock(vn.payos.model.webhooks.WebhookData.class);
        when(data.getOrderCode()).thenReturn(orderCode);
        lenient().when(data.getCode()).thenReturn("00");
        return data;
    }

    /**
     * Khach tao hai link cho mot don, tra qua link thu nhat (don thanh PAID),
     * roi mo trang ket qua cua link thu hai. Link thu hai chua nhan dong nao -
     * truoc day no van bi ghi SUCCESS chi vi don da PAID.
     */
    @Test
    void processPayOSCallback_ShouldFollowTheLinksOwnStatus_NotTheBookings() {
        when(paymentRepository.findByOrderCode("1001")).thenReturn(Optional.of(pendingPayment("1001")));
        vn.payos.model.v2.paymentRequests.PaymentLink link =
                linkIn(vn.payos.model.v2.paymentRequests.PaymentLinkStatus.PENDING);
        when(payOS.paymentRequests().get(1001L)).thenReturn(link);

        paymentService.processPayOSCallback(java.util.Map.of("orderCode", "1001"));

        verify(reconciliationService).applyStatus(10L, "PENDING");
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    /**
     * Webhook mang code "00" cho MOI lan chuyen khoan vao, ke ca chuyen thieu.
     * Trang thai cua link (UNDERPAID) moi la thu quyet dinh.
     */
    @Test
    void processPayOSWebhook_ShouldNotTreatCode00AsPaid_WhenLinkIsUnderpaid() {
        vn.payos.model.webhooks.Webhook webhook = new vn.payos.model.webhooks.Webhook();
        vn.payos.model.webhooks.WebhookData data = webhookFor(1001L);
        when(payOS.webhooks().verify(webhook)).thenReturn(data);
        when(paymentRepository.findByOrderCode("1001")).thenReturn(Optional.of(pendingPayment("1001")));
        vn.payos.model.v2.paymentRequests.PaymentLink link =
                linkIn(vn.payos.model.v2.paymentRequests.PaymentLinkStatus.UNDERPAID);
        when(payOS.paymentRequests().get(1001L)).thenReturn(link);

        paymentService.processPayOSWebhook(webhook);

        verify(reconciliationService).applyStatus(10L, "UNDERPAID");
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void processPayOSWebhook_ShouldIgnoreUnknownOrderCode() {
        vn.payos.model.webhooks.Webhook webhook = new vn.payos.model.webhooks.Webhook();
        vn.payos.model.webhooks.WebhookData data = webhookFor(123L);
        when(payOS.webhooks().verify(webhook)).thenReturn(data);
        when(paymentRepository.findByOrderCode("123")).thenReturn(Optional.empty());

        paymentService.processPayOSWebhook(webhook);

        verifyNoInteractions(reconciliationService);
    }
}
