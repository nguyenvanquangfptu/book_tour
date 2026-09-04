package myproject.booking_tour.service.impl;

import myproject.booking_tour.dto.request.VoucherRequest;
import myproject.booking_tour.dto.response.VoucherResponse;
import myproject.booking_tour.entity.AuditLog;
import myproject.booking_tour.entity.Voucher;
import myproject.booking_tour.exception.BadRequestException;
import myproject.booking_tour.exception.ResourceNotFoundException;
import myproject.booking_tour.mapper.VoucherMapper;
import myproject.booking_tour.repository.AuditLogRepository;
import myproject.booking_tour.repository.VoucherRepository;
import myproject.booking_tour.security.SecurityUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoucherServiceImplTest {

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private VoucherMapper voucherMapper;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private VoucherServiceImpl voucherService;

    private Voucher mockVoucher;
    private MockedStatic<SecurityUtil> mockedSecurityUtil;

    @BeforeEach
    void setUp() {
        mockVoucher = new Voucher();
        mockVoucher.setId(1L);
        mockVoucher.setCode("DISCOUNT20");
        mockVoucher.setIsActive(true);
        
        mockedSecurityUtil = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityUtil.close();
    }

    @Test
    void getAllVouchers_ShouldReturnAll_WhenAdmin() {
        mockedSecurityUtil.when(SecurityUtil::isAdmin).thenReturn(true);
        when(voucherRepository.findAll()).thenReturn(List.of(mockVoucher));
        when(voucherMapper.toResponse(any())).thenReturn(new VoucherResponse());

        List<VoucherResponse> result = voucherService.getAllVouchers();
        assertEquals(1, result.size());
    }

    @Test
    void getVoucherById_ShouldReturnVoucher() {
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(mockVoucher));
        when(voucherMapper.toResponse(mockVoucher)).thenReturn(new VoucherResponse());

        VoucherResponse result = voucherService.getVoucherById(1L);
        assertNotNull(result);
    }

    @Test
    void createVoucher_ShouldSave_WhenCodeIsUnique() {
        VoucherRequest request = new VoucherRequest();
        request.setCode("DISCOUNT20");

        when(voucherRepository.existsByCode("DISCOUNT20")).thenReturn(false);
        when(voucherMapper.toEntity(request)).thenReturn(mockVoucher);
        when(voucherRepository.save(mockVoucher)).thenReturn(mockVoucher);
        when(voucherMapper.toResponse(mockVoucher)).thenReturn(new VoucherResponse());

        VoucherResponse result = voucherService.createVoucher(request);

        assertNotNull(result);
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void createVoucher_ShouldThrowException_WhenCodeExists() {
        VoucherRequest request = new VoucherRequest();
        request.setCode("DISCOUNT20");

        when(voucherRepository.existsByCode("DISCOUNT20")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> voucherService.createVoucher(request));
    }

    @Test
    void updateVoucher_ShouldUpdate_WhenCodeIsUnique() {
        VoucherRequest request = new VoucherRequest();
        request.setCode("NEWCODE");

        when(voucherRepository.findById(1L)).thenReturn(Optional.of(mockVoucher));
        when(voucherRepository.existsByCode("NEWCODE")).thenReturn(false);
        when(voucherRepository.save(any(Voucher.class))).thenReturn(mockVoucher);
        when(voucherMapper.toResponse(any())).thenReturn(new VoucherResponse());

        VoucherResponse result = voucherService.updateVoucher(1L, request);

        assertNotNull(result);
        verify(voucherMapper, times(1)).updateEntityFromRequest(mockVoucher, request);
    }
    
    @Test
    void deleteVoucher_ShouldDelete_WhenExists() {
        when(voucherRepository.existsById(1L)).thenReturn(true);
        voucherService.deleteVoucher(1L);
        verify(voucherRepository, times(1)).deleteById(1L);
    }
}
