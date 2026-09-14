package myproject.booking_tour.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import myproject.booking_tour.dto.request.VoucherRequest;
import myproject.booking_tour.dto.response.VoucherResponse;
import myproject.booking_tour.entity.Voucher;
import myproject.booking_tour.exception.BadRequestException;
import myproject.booking_tour.exception.ResourceNotFoundException;
import myproject.booking_tour.mapper.VoucherMapper;
import myproject.booking_tour.repository.VoucherRepository;
import myproject.booking_tour.repository.AuditLogRepository;
import myproject.booking_tour.service.VoucherService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherMapper voucherMapper;
    private final AuditLogRepository auditLogRepository;

    @Override
    public List<VoucherResponse> getAllVouchers() {
        boolean isAdmin = myproject.booking_tour.security.SecurityUtil.isAdmin();
        return voucherRepository.findAll().stream()
                .filter(v -> isAdmin || Boolean.TRUE.equals(v.getIsActive()))
                .map(voucherMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public VoucherResponse getVoucherById(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with id: " + id));
        return voucherMapper.toResponse(voucher);
    }

    @Override
    public VoucherResponse getVoucherByCode(String code) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with code: " + code));
        return voucherMapper.toResponse(voucher);
    }

    /**
     * @Transactional o day khong phai trang tri: voucher va nhat ky cua no phai
     * cung song hoac cung chet. Khong co no, moi lenh save chay trong transaction
     * rieng - nen khi ghi audit log that bai, VOUCHER VAN DUOC TAO trong khi
     * admin nhan thong bao loi. Do dung la chuyen da xay ra: hai voucher nam san
     * trong bang sau hai lan bam ma man hinh bao that bai, va bam lai lan nua thi
     * doi thanh "Voucher code already exists".
     */
    @Transactional
    @Override
    public VoucherResponse createVoucher(VoucherRequest request) {
        if (voucherRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Voucher code already exists");
        }
        Voucher voucher = voucherMapper.toEntity(request);
        assertVoucherValid(voucher);
        Voucher savedVoucher = voucherRepository.save(voucher);

        myproject.booking_tour.entity.AuditLog log = new myproject.booking_tour.entity.AuditLog();
        log.setEntityName("Voucher");
        log.setEntityId(savedVoucher.getId());
        log.setAction("CREATE_VOUCHER");
        log.setNewValue("Code: " + savedVoucher.getCode() + ", Discount: " + savedVoucher.getDiscountAmount());
        log.setUserId(myproject.booking_tour.security.SecurityUtil.getCurrentUserId());
        auditLogRepository.save(log);

        return voucherMapper.toResponse(savedVoucher);
    }

    @Transactional
    @Override
    public VoucherResponse updateVoucher(Long id, VoucherRequest request) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with id: " + id));
        
        if (request.getCode() != null && !request.getCode().equals(voucher.getCode()) 
            && voucherRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Voucher code already exists");
        }

        voucherMapper.updateEntityFromRequest(voucher, request);
        assertVoucherValid(voucher);
        Voucher updatedVoucher = voucherRepository.save(voucher);

        myproject.booking_tour.entity.AuditLog log = new myproject.booking_tour.entity.AuditLog();
        log.setEntityName("Voucher");
        log.setEntityId(updatedVoucher.getId());
        log.setAction("UPDATE_VOUCHER");
        log.setNewValue("Code: " + updatedVoucher.getCode() + ", Discount: " + updatedVoucher.getDiscountAmount());
        log.setUserId(myproject.booking_tour.security.SecurityUtil.getCurrentUserId());
        auditLogRepository.save(log);

        return voucherMapper.toResponse(updatedVoucher);
    }

    /**
     * Doi chieu voucher voi cac rang buoc CHECK cua bang vouchers TRUOC khi ghi.
     *
     * Truoc day khong co gi kiem tra ca, nen moi voucher nhap sai deu di het
     * duong xuong database roi bi chan o do: ck_vouchers_period (ngay ket thuc
     * khong sau ngay bat dau), ck_vouchers_percentage (phan tram ngoai 0-100),
     * ck_vouchers_discount_type (khong chon muc giam nao, hoac chon ca hai). Admin
     * nhan 500 "Da co loi xay ra, vui long thu lai sau!" - thu lai bao nhieu lan
     * cung vay, va khong biet phai sua o nhap nao. O "So tien giam" trong form
     * khong co min, nen go 0 la du de gap loi nay.
     *
     * Kiem tra tren entity SAU khi gop request vao, vi luc cap nhat nhung truong
     * bo trong se giu gia tri cu.
     */
    private void assertVoucherValid(Voucher voucher) {
        if (voucher.getValidFrom() != null && voucher.getValidUntil() != null
                && !voucher.getValidUntil().isAfter(voucher.getValidFrom())) {
            throw new BadRequestException("Ngày hết hạn voucher phải sau ngày bắt đầu.");
        }

        java.math.BigDecimal amount = voucher.getDiscountAmount() != null
                ? voucher.getDiscountAmount() : java.math.BigDecimal.ZERO;
        double percentage = voucher.getDiscountPercentage() != null ? voucher.getDiscountPercentage() : 0;

        if (amount.signum() < 0 || percentage < 0) {
            throw new BadRequestException("Mức giảm của voucher không được âm.");
        }
        if (percentage > 100) {
            throw new BadRequestException("Voucher giảm theo phần trăm chỉ được giảm tối đa 100%.");
        }
        if ((amount.signum() > 0) == (percentage > 0)) {
            throw new BadRequestException(
                    "Voucher phải giảm theo số tiền hoặc theo phần trăm - chọn đúng một loại, với mức giảm lớn hơn 0.");
        }
        if ((voucher.getMaxDiscount() != null && voucher.getMaxDiscount().signum() < 0)
                || (voucher.getMinOrderValue() != null && voucher.getMinOrderValue().signum() < 0)
                || (voucher.getUsageLimit() != null && voucher.getUsageLimit() < 0)) {
            throw new BadRequestException("Mức giảm tối đa, giá trị đơn tối thiểu và số lượt dùng không được âm.");
        }
    }

    @Transactional
    @Override
    public void deleteVoucher(Long id) {
        if (!voucherRepository.existsById(id)) {
            throw new ResourceNotFoundException("Voucher not found with id: " + id);
        }
        voucherRepository.deleteById(id);
    }
}
