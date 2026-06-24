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
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
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
        validateAndDeductTourSchedule(tour, request);

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

    private int parseDurationDays(String duration) {
        if (duration == null || duration.trim().isEmpty()) return 1;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("(\\d+)\\s*(ngày|day)", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher m = p.matcher(duration);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        p = java.util.regex.Pattern.compile("(\\d+)");
        m = p.matcher(duration);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return 1;
    }

    private void validateAndDeductTourSchedule(Tour tour, BookingRequest request) {
        if (request.getTravelDate() == null) {
            throw new BadRequestException("Travel date is required");
        }
        if (request.getTravelDate().isBefore(java.time.LocalDate.now())) {
            throw new BadRequestException("Ngày khởi hành không được nằm trong quá khứ");
        }
        
        int days = parseDurationDays(tour.getDuration());
        int defaultSlots = tour.getAvailableSlots() != null ? tour.getAvailableSlots() : (tour.getMaxPeople() != null ? tour.getMaxPeople() : 0);
        
        java.time.LocalDate startDate = request.getTravelDate();
        java.time.LocalDate endDate = startDate.plusDays(days - 1);

        List<TourSchedule> existingSchedules = tourScheduleRepository.findByTourIdAndDepartureDateBetween(tour.getId(), startDate, endDate);
        Map<java.time.LocalDate, TourSchedule> scheduleMap = existingSchedules.stream()
                .collect(Collectors.toMap(TourSchedule::getDepartureDate, s -> s));

        List<TourSchedule> schedulesToSave = new java.util.ArrayList<>();

        // Validate and prepare for deduction
        for (int i = 0; i < days; i++) {
            java.time.LocalDate checkDate = startDate.plusDays(i);
            TourSchedule schedule = scheduleMap.getOrDefault(checkDate, null);
            
            if (schedule == null) {
                schedule = new TourSchedule();
                schedule.setTour(tour);
                schedule.setDepartureDate(checkDate);
                schedule.setAvailableSlots(defaultSlots);
            }

            if (schedule.getAvailableSlots() < request.getNumberOfPeople()) {
                throw new BadRequestException("Not enough available slots for date " + checkDate + "! Only " 
                        + schedule.getAvailableSlots() + " slots left.");
            }
            
            schedule.setAvailableSlots(schedule.getAvailableSlots() - request.getNumberOfPeople());
            schedulesToSave.add(schedule);
        }

        // Batch save
        tourScheduleRepository.saveAll(schedulesToSave);
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
        org.springframework.data.domain.Page<Booking> bookingPage = bookingRepository.findAll(org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "bookingDate")));
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
        // 1. tìm booking
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!booking.getUser().getId().equals(userId) && !user.getRole().getName().equals("ADMIN")) {
            throw new BadRequestException("You do not have permission to cancel this booking!");
        }

        if ("CANCELLED".equals(booking.getStatus())) {
            throw new BadRequestException("Booking is already cancelled!");
        }

        // 3. Hoàn lại số chỗ trống vào TourSchedule cho tất cả các ngày diễn ra tour
        Tour tour = booking.getTour();
        if (tour != null && booking.getTravelDate() != null) {
            int days = parseDurationDays(tour.getDuration());
            java.time.LocalDate startDate = booking.getTravelDate();
            java.time.LocalDate endDate = startDate.plusDays(days - 1);
            
            List<TourSchedule> existingSchedules = tourScheduleRepository.findByTourIdAndDepartureDateBetween(tour.getId(), startDate, endDate);
            for (TourSchedule schedule : existingSchedules) {
                schedule.setAvailableSlots(schedule.getAvailableSlots() + booking.getNumberOfPeople());
            }
            if (!existingSchedules.isEmpty()) {
                tourScheduleRepository.saveAll(existingSchedules);
            }
        }
        
        // Hoàn lại Voucher (nếu có)
        if (booking.getVoucher() != null) {
            myproject.booking_tour.entity.Voucher voucher = booking.getVoucher();
            if (voucher.getUsedCount() > 0) {
                voucher.setUsedCount(voucher.getUsedCount() - 1);
                voucherRepository.save(voucher);
            }
        }

        // 2. status = CANCELLED
        booking.setStatus("CANCELLED");

        // 4. save
        Booking updated = bookingRepository.save(booking);
        return bookingMapper.toResponse(updated);
    }
}
