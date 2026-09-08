package myproject.booking_tour.service.impl;

import myproject.booking_tour.dto.request.BookingRequest;
import myproject.booking_tour.dto.response.BookingResponse;
import myproject.booking_tour.entity.Booking;
import myproject.booking_tour.entity.Tour;
import myproject.booking_tour.entity.User;
import myproject.booking_tour.exception.BadRequestException;
import myproject.booking_tour.exception.ResourceNotFoundException;
import myproject.booking_tour.mapper.BookingMapper;
import myproject.booking_tour.repository.BookingRepository;
import myproject.booking_tour.repository.TourRepository;
import myproject.booking_tour.repository.UserRepository;
import myproject.booking_tour.repository.VoucherRepository;
import myproject.booking_tour.entity.TourSchedule;
import myproject.booking_tour.repository.TourScheduleRepository;
import myproject.booking_tour.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import myproject.booking_tour.service.EmailService;
import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TourRepository tourRepository;
    private final VoucherRepository voucherRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final BookingMapper bookingMapper;
    private final EmailService emailService;

    @Value("${app.admin.email}")
    private String adminEmail;

    /** Ngày trong thông báo lỗi hiện theo định dạng người Việt đọc quen, không phải ISO. */
    private static final java.time.format.DateTimeFormatter DATE_FORMAT =
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request, Long userId) {
        // 1. tìm user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // 2. tìm tour
        Tour tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with id: " + request.getTourId()));

        if ("SOLD_OUT".equals(tour.getStatus())) {
            throw new myproject.booking_tour.exception.BadRequestException("Rất tiếc, tour này đã hết chỗ!");
        }
        if ("INACTIVE".equals(tour.getStatus())) {
            throw new myproject.booking_tour.exception.BadRequestException("Tour này hiện đang tạm ngưng nhận khách.");
        }
        if ("DELETED".equals(tour.getStatus())) {
            throw new myproject.booking_tour.exception.BadRequestException("Tour này không còn tồn tại.");
        }

        // 3. Xử lý logic kiểm tra và trừ quỹ chỗ theo ngày khởi hành
        TourSchedule departureSchedule = validateAndDeductTourSchedule(tour, request);

        // 4. Tạo Booking
        Booking booking = new Booking();
        booking.setUser(user);
        
        BigDecimal totalPrice = tour.getPrice().multiply(BigDecimal.valueOf(request.getNumberOfPeople()));
        
        // 4. Áp dụng Voucher (nếu có) và tính giá cuối cùng
        BigDecimal finalPrice = applyVoucherAndCalculatePrice(request, booking, totalPrice);

        // 5. Gán thông tin Booking
        booking.setTour(tour);
        booking.setNumberOfPeople(request.getNumberOfPeople());
        booking.setTotalPrice(finalPrice);
        booking.setStatus("PENDING");
        booking.setBookingDate(LocalDateTime.now());
        booking.setTravelDate(request.getTravelDate());
        // Khoa ngoai that toi lich khoi hanh (truoc day chi lien ket ngam qua
        // cap tour_id + travel_date, database khong the bao ve)
        booking.setSchedule(departureSchedule);
        
        booking.setCustomerName(request.getCustomerName() != null ? request.getCustomerName() : user.getFullName());
        booking.setCustomerEmail(request.getCustomerEmail() != null ? request.getCustomerEmail() : user.getEmail());
        booking.setCustomerPhone(request.getCustomerPhone());
        booking.setNote(request.getNote());

        // 6. Lưu Booking
        Booking savedBooking = bookingRepository.save(booking);

        // 7. Gửi email xác nhận
        sendBookingEmails(savedBooking, tour);

        return bookingMapper.toResponse(savedBooking);
    }

    /**
     * Doc so ngay tour keo dai tu chuoi mo ta tu do ("3 ngay 2 dem", "1 tuan").
     *
     * Con so nay quyet dinh tru cho cua bao nhieu ngay, nen doc thieu la ban
     * vuot cho o nhung ngay khong duoc tinh den. "1 tuan" tung roi vao nhanh
     * "lay con so dau tien" va tra ve 1 thay vi 7 - tour ca tuan ma chi giu cho
     * dung ngay khoi hanh.
     */
    private int parseDurationDays(String duration) {
        if (duration == null || duration.trim().isEmpty()) return 1;

        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(\\d+)\\s*(ngày|ngay|day)", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(duration);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }

        m = java.util.regex.Pattern
                .compile("(\\d+)\\s*(tuần|tuan|week)", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(duration);
        if (m.find()) {
            return Integer.parseInt(m.group(1)) * 7;
        }

        m = java.util.regex.Pattern.compile("(\\d+)").matcher(duration);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return 1;
    }

    /**
     * Kiem tra du cho va tru cho cho TAT CA cac ngay ma tour dien ra.
     *
     * Moi ngay di qua dung hai cau lenh, ca hai deu nguyen tu:
     *
     *   1. insertIfAbsent - tao dong lich neu ngay do chua co
     *   2. deductSlots    - tru cho, kem dieu kien "con du cho" ngay trong
     *                       cau UPDATE
     *
     * Khong con doc-sua-ghi tren entity, nen khong con cua so nao de hai
     * request chen vao giua buoc kiem tra va buoc tru. Doi lai, so cho con lai
     * de bao loi phai doc them mot lan - nhung chi doc tren duong THAT BAI,
     * la duong hiem.
     *
     * Vong lap luon chay theo thu tu ngay tang dan, giong nhau o moi request,
     * nen hai booking chong lan nhau khong the khoa cheo (deadlock).
     *
     * @return dong tour_schedules cua NGAY KHOI HANH, de booking giu duoc khoa
     *         ngoai that toi lich khoi hanh.
     */
    private TourSchedule validateAndDeductTourSchedule(Tour tour, BookingRequest request) {
        if (request.getTravelDate() == null) {
            throw new BadRequestException("Vui lòng chọn ngày khởi hành.");
        }
        if (request.getTravelDate().isBefore(java.time.LocalDate.now())) {
            throw new BadRequestException("Ngày khởi hành không được nằm trong quá khứ");
        }

        int days = parseDurationDays(tour.getDuration());
        int defaultSlots = tour.getAvailableSlots() != null ? tour.getAvailableSlots() : (tour.getMaxPeople() != null ? tour.getMaxPeople() : 0);
        int people = request.getNumberOfPeople();

        java.time.LocalDate startDate = request.getTravelDate();

        for (int i = 0; i < days; i++) {
            java.time.LocalDate checkDate = startDate.plusDays(i);

            tourScheduleRepository.insertIfAbsent(tour.getId(), checkDate, defaultSlots);

            if (tourScheduleRepository.deductSlots(tour.getId(), checkDate, people) == 0) {
                // Khong tru duoc nghia la THAT SU khong du cho, khong phai tranh
                // chap ky thuat - bao 400 kem so cho con lai de khach biet duong
                // ma giam so nguoi.
                int remaining = tourScheduleRepository
                        .findFirstByTourIdAndDepartureDate(tour.getId(), checkDate)
                        .map(TourSchedule::getAvailableSlots)
                        .orElse(0);
                throw new BadRequestException("Ngày " + checkDate.format(DATE_FORMAT) + " không đủ chỗ cho "
                        + people + " khách, chỉ còn " + remaining + " chỗ trống.");
            }
        }

        // Doc sau khi da tru: dong lich chac chan ton tai, va gia tri doc ra
        // phan anh dung ket qua vua ghi trong cung transaction.
        return tourScheduleRepository.findFirstByTourIdAndDepartureDate(tour.getId(), startDate)
                .orElse(null);
    }

    private BigDecimal applyVoucherAndCalculatePrice(BookingRequest request, Booking booking, BigDecimal totalPrice) {
        if (request.getVoucherId() == null) {
            return totalPrice;
        }

        myproject.booking_tour.entity.Voucher voucher = voucherRepository.findById(request.getVoucherId())
                .orElseThrow(() -> new BadRequestException("Voucher không tồn tại"));
        
        if (voucher.getIsActive() == null || !voucher.getIsActive()) {
            throw new BadRequestException("Voucher đã bị vô hiệu hóa");
        }
        if (voucher.getValidUntil() != null && LocalDateTime.now().isAfter(voucher.getValidUntil())) {
            throw new BadRequestException("Voucher đã hết hạn sử dụng");
        }
        if (voucher.getValidFrom() != null && LocalDateTime.now().isBefore(voucher.getValidFrom())) {
            throw new BadRequestException("Voucher chưa đến thời gian sử dụng");
        }
        if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new BadRequestException("Voucher đã hết lượt sử dụng");
        }
        if (voucher.getMinOrderValue() != null && totalPrice.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new BadRequestException("Đơn hàng chưa đạt giá trị tối thiểu để áp dụng voucher");
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (voucher.getDiscountAmount() != null && voucher.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            discountAmount = voucher.getDiscountAmount();
        } else if (voucher.getDiscountPercentage() != null && voucher.getDiscountPercentage() > 0) {
            discountAmount = totalPrice.multiply(BigDecimal.valueOf(voucher.getDiscountPercentage() / 100.0));
            if (voucher.getMaxDiscount() != null && discountAmount.compareTo(voucher.getMaxDiscount()) > 0) {
                discountAmount = voucher.getMaxDiscount();
            }
        }

        BigDecimal finalPrice = totalPrice.subtract(discountAmount);
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO;
        }

        voucher.setUsedCount(voucher.getUsedCount() + 1);
        voucherRepository.save(voucher);
        booking.setVoucher(voucher);

        return finalPrice;
    }

    private void sendBookingEmails(Booking savedBooking, Tour tour) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("customerName", savedBooking.getCustomerName());
            templateModel.put("customerEmail", savedBooking.getCustomerEmail());
            templateModel.put("bookingId", "#" + savedBooking.getId());
            templateModel.put("tourName", tour.getTitle());
            templateModel.put("numberOfPeople", savedBooking.getNumberOfPeople());
            templateModel.put("totalPrice", savedBooking.getTotalPrice().toString());
            
            // Send to customer
            emailService.sendMessageUsingThymeleafTemplate(savedBooking.getCustomerEmail(), "Xác nhận đặt tour thành công - #" + savedBooking.getId(), "booking-confirmation", templateModel);
            
            // Notify Admin
            emailService.sendMessageUsingThymeleafTemplate(adminEmail, "Có đơn đặt tour mới - #" + savedBooking.getId(), "admin-booking-notification", templateModel);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public myproject.booking_tour.dto.response.PageResponse<BookingResponse> getAllBookings(int page, int size) {
        // page/size den thang tu query string: khong kep thi ?size=1000000 co nap
        // ca bang bookings, con so am lam PageRequest.of nem IllegalArgumentException
        // roi thanh 500.
        org.springframework.data.domain.Page<Booking> bookingPage = bookingRepository.findAll(
                org.springframework.data.domain.PageRequest.of(
                        myproject.booking_tour.utils.PageableUtils.safePage(page),
                        myproject.booking_tour.utils.PageableUtils.safeSize(size),
                        org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "bookingDate")));
        List<BookingResponse> responses = bookingPage.getContent().stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());

        myproject.booking_tour.dto.response.PageResponse<BookingResponse> pageResponse = new myproject.booking_tour.dto.response.PageResponse<>();
        pageResponse.setContent(responses);
        pageResponse.setPageNumber(bookingPage.getNumber());
        pageResponse.setPageSize(bookingPage.getSize());
        pageResponse.setTotalElements(bookingPage.getTotalElements());
        pageResponse.setTotalPages(bookingPage.getTotalPages());
        pageResponse.setLast(bookingPage.isLast());
        return pageResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse confirmBooking(Long id) {
        // 1. tìm booking
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        if ("CANCELLED".equals(booking.getStatus())) {
            throw new BadRequestException("Cannot confirm a cancelled booking!");
        }

        if ("CONFIRMED".equals(booking.getStatus())) {
            return bookingMapper.toResponse(booking); // already confirmed
        }

        // 2. status = CONFIRMED
        booking.setStatus("CONFIRMED");
        booking.setApprovedAt(LocalDateTime.now());

        // (Bỏ trừ availableSlots của tour vì đã trừ theo ngày khởi hành lúc đặt)

        // 4. save
        Booking updated = bookingRepository.save(booking);

        // 5. Send Payment Reminder Email
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("customerName", updated.getCustomerName() != null ? updated.getCustomerName() : updated.getUser().getFullName());
            templateModel.put("bookingId", "#" + updated.getId());
            templateModel.put("tourName", updated.getTour() != null ? updated.getTour().getTitle() : "Tour");
            templateModel.put("totalPrice", updated.getTotalPrice().toString());
            
            String emailTo = updated.getCustomerEmail() != null ? updated.getCustomerEmail() : updated.getUser().getEmail();
            emailService.sendMessageUsingThymeleafTemplate(emailTo, "Thông báo: Đơn đặt tour đã được duyệt - Vui lòng thanh toán", "payment-reminder", templateModel);
        } catch (Exception e) {
            System.err.println("Failed to send payment reminder email: " + e.getMessage());
        }

        return bookingMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long id, Long userId) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isAdmin = "ADMIN".equals(user.getRole().getName());

        if (!booking.getUser().getId().equals(userId) && !isAdmin) {
            throw new BadRequestException("Bạn không có quyền hủy đơn đặt tour này!");
        }

        if ("CANCELLED".equals(booking.getStatus())) {
            throw new BadRequestException("Đơn đặt tour này đã được hủy trước đó!");
        }

        // Don da thanh toan: huy suong se hoan lai cho va luot voucher trong khi
        // tien van nam o PayOS, ban ghi payment van la SUCCESS, va khong co dau
        // vet hoan tien nao. Viec hoan tien phai do nguoi that quyet dinh, nen
        // chi admin moi huy duoc.
        if (!isAdmin && "PAID".equals(booking.getStatus())) {
            throw new BadRequestException(
                    "Đơn đã thanh toán không thể tự hủy. Vui lòng liên hệ bộ phận hỗ trợ để được hoàn tiền.");
        }

        // Huy sau khi tour da khoi hanh: so cho hoan lai la cho cua mot ngay da
        // qua nen vo nghia, nhung luot VOUCHER thi hoan that - khach di tour
        // xong van lay lai duoc voucher da dung.
        if (!isAdmin && booking.getTravelDate() != null
                && booking.getTravelDate().isBefore(java.time.LocalDate.now())) {
            throw new BadRequestException("Không thể hủy đơn của tour đã khởi hành.");
        }

        releaseBookingResources(booking);
        booking.setStatus("CANCELLED");

        Booking updated = bookingRepository.save(booking);
        return bookingMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public boolean cancelBookingBySystem(Long id, String reason) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null || "CANCELLED".equals(booking.getStatus())) {
            return false;
        }

        if ("PAID".equals(booking.getStatus())) {
            log.warn("[HuyHeThong] Bỏ qua đơn #{} ({}): đơn đã thanh toán, việc hoàn tiền phải do người thật quyết định.",
                    id, reason);
            return false;
        }

        releaseBookingResources(booking);
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);

        log.info("[HuyHeThong] Đã hủy đơn #{}: {}", id, reason);
        return true;
    }

    /**
     * Tra lai nhung gi booking dang giu: so cho cua moi ngay tour dien ra, va
     * luot su dung voucher.
     *
     * Phan hoan cho phai dung UPDATE nguyen tu giong het duong tru cho ben
     * createBooking. Neu de lai doc-sua-ghi o day, mot lan huy chay song song
     * voi mot lan dat se ghi de len ket qua cua lan dat: cac cau UPDATE truc
     * tiep khong lam nhich cot version, nen khoa lac quan khong con phat hien
     * duoc.
     */
    private void releaseBookingResources(Booking booking) {
        Tour tour = booking.getTour();
        if (tour != null && booking.getTravelDate() != null) {
            int days = parseDurationDays(tour.getDuration());
            java.time.LocalDate startDate = booking.getTravelDate();
            java.time.LocalDate endDate = startDate.plusDays(days - 1);

            tourScheduleRepository.restoreSlots(tour.getId(), startDate, endDate, booking.getNumberOfPeople());
        }

        if (booking.getVoucher() != null) {
            myproject.booking_tour.entity.Voucher voucher = booking.getVoucher();
            if (voucher.getUsedCount() > 0) {
                voucher.setUsedCount(voucher.getUsedCount() - 1);
                voucherRepository.save(voucher);
            }
        }
    }

    // Viec tu dong huy don qua han da chuyen sang BookingScheduler +
    // BookingAutoCancelService. O day no vua la @Scheduled nam trong mot
    // @Service (kho tim), vua goi cancelBooking cua CHINH MINH nen ca luot quet
    // dung chung mot transaction: mot don hong keo sap ca luot.
}
