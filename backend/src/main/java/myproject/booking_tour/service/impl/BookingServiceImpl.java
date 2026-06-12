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
import myproject.booking_tour.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import myproject.booking_tour.service.EmailService;
import java.util.HashMap;
import java.util.Map;

@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private EmailService emailService;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request, Long userId) {
        // 1. tìm user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // 2. tìm tour & 3. kiểm tra tour tồn tại
        Tour tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with id: " + request.getTourId()));

        // 4. kiểm tra availableSlots
        if (tour.getAvailableSlots() == null || tour.getAvailableSlots() < request.getNumberOfPeople()) {
            throw new BadRequestException("Not enough available slots! Only " 
                    + (tour.getAvailableSlots() != null ? tour.getAvailableSlots() : 0) + " slots left.");
        }

        // 5. totalPrice = price * numberOfPeople
        BigDecimal price = tour.getPrice();
        BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(request.getNumberOfPeople()));

        // 6. status = PENDING
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setTour(tour);
        booking.setNumberOfPeople(request.getNumberOfPeople());
        booking.setTotalPrice(totalPrice);
        booking.setStatus("PENDING");
        booking.setBookingDate(LocalDateTime.now());

        // 7. save
        Booking savedBooking = bookingRepository.save(booking);

        // 8. Send Email
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("customerName", user.getFullName());
            templateModel.put("bookingId", "#" + savedBooking.getId());
            templateModel.put("tourName", tour.getTitle());
            templateModel.put("numberOfPeople", savedBooking.getNumberOfPeople());
            templateModel.put("totalPrice", savedBooking.getTotalPrice().toString());
            emailService.sendMessageUsingThymeleafTemplate(user.getEmail(), "Xác nhận đặt tour thành công - #" + savedBooking.getId(), "booking-confirmation", templateModel);
        } catch (Exception e) {
            // Log exception, don't fail booking if email fails
            System.err.println("Failed to send email: " + e.getMessage());
        }

        return bookingMapper.toResponse(savedBooking);
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
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
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

        // 3. availableSlots -= numberOfPeople
        Tour tour = booking.getTour();
        if (tour != null) {
            if (tour.getAvailableSlots() < booking.getNumberOfPeople()) {
                throw new BadRequestException("Cannot confirm! Not enough available slots left on the tour.");
            }
            tour.setAvailableSlots(tour.getAvailableSlots() - booking.getNumberOfPeople());
            tourRepository.save(tour);
        }

        // 4. save
        Booking updated = bookingRepository.save(booking);
        return bookingMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long id) {
        // 1. tìm booking
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        if ("CANCELLED".equals(booking.getStatus())) {
            throw new BadRequestException("Booking is already cancelled!");
        }

        // 3. availableSlots += numberOfPeople (only if booking was CONFIRMED!)
        if ("CONFIRMED".equals(booking.getStatus())) {
            Tour tour = booking.getTour();
            if (tour != null) {
                tour.setAvailableSlots(tour.getAvailableSlots() + booking.getNumberOfPeople());
                tourRepository.save(tour);
            }
        }

        // 2. status = CANCELLED
        booking.setStatus("CANCELLED");

        // 4. save
        Booking updated = bookingRepository.save(booking);
        return bookingMapper.toResponse(updated);
    }
}
