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

    @Transactional
    @Override
    public void deleteVoucher(Long id) {
        if (!voucherRepository.existsById(id)) {
            throw new ResourceNotFoundException("Voucher not found with id: " + id);
        }
        voucherRepository.deleteById(id);
    }
}
