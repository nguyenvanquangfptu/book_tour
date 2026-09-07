package myproject.booking_tour.service.impl;

import myproject.booking_tour.dto.request.BookingRequest;
import myproject.booking_tour.dto.response.BookingResponse;
import myproject.booking_tour.entity.Booking;
import myproject.booking_tour.entity.Tour;
import myproject.booking_tour.entity.TourSchedule;
import myproject.booking_tour.entity.User;
import myproject.booking_tour.exception.BadRequestException;
import myproject.booking_tour.mapper.BookingMapper;
import myproject.booking_tour.repository.BookingRepository;
import myproject.booking_tour.repository.TourRepository;
import myproject.booking_tour.repository.TourScheduleRepository;
import myproject.booking_tour.repository.UserRepository;
import myproject.booking_tour.repository.VoucherRepository;
import myproject.booking_tour.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TourRepository tourRepository;
    @Mock
    private VoucherRepository voucherRepository;
    @Mock
    private TourScheduleRepository tourScheduleRepository;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User mockUser;
    private Tour mockTour;

    @BeforeEach
    void setUp() {
        myproject.booking_tour.entity.Role customerRole = new myproject.booking_tour.entity.Role();
        customerRole.setName("CUSTOMER");

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setRole(customerRole);

        mockTour = new Tour();
        mockTour.setId(10L);
        mockTour.setStatus("ACTIVE");
        mockTour.setPrice(BigDecimal.valueOf(100));
        mockTour.setAvailableSlots(10);
    }

    @Test
    void createBooking_ShouldThrowException_WhenTourIsSoldOut() {
        BookingRequest request = new BookingRequest();
        request.setTourId(10L);
        
        mockTour.setStatus("SOLD_OUT");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(tourRepository.findById(10L)).thenReturn(Optional.of(mockTour));

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(request, 1L));
    }
    
    @Test
    void createBooking_ShouldThrowException_WhenDateInPast() {
        BookingRequest request = new BookingRequest();
        request.setTourId(10L);
        request.setTravelDate(LocalDate.now().minusDays(1));
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(tourRepository.findById(10L)).thenReturn(Optional.of(mockTour));

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(request, 1L));
    }

    @Test
    void createBooking_ShouldDeductEveryDayOfTour_ThroughAtomicUpdate() {
        LocalDate travelDate = LocalDate.now().plusDays(7);
        mockTour.setDuration("3 ngày 2 đêm");

        BookingRequest request = new BookingRequest();
        request.setTourId(10L);
        request.setTravelDate(travelDate);
        request.setNumberOfPeople(2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(tourRepository.findById(10L)).thenReturn(Optional.of(mockTour));
        when(tourScheduleRepository.insertIfAbsent(anyLong(), any(LocalDate.class), anyInt())).thenReturn(1);
        when(tourScheduleRepository.deductSlots(anyLong(), any(LocalDate.class), anyInt())).thenReturn(1);
        when(tourScheduleRepository.findFirstByTourIdAndDepartureDate(10L, travelDate))
                .thenReturn(Optional.of(new TourSchedule()));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(new BookingResponse());

        bookingService.createBooking(request, 1L);

        // Tour 3 ngày chiếm chỗ của cả 3 ngày, không chỉ ngày khởi hành.
        for (int i = 0; i < 3; i++) {
            verify(tourScheduleRepository).insertIfAbsent(eq(10L), eq(travelDate.plusDays(i)), eq(10));
            verify(tourScheduleRepository).deductSlots(eq(10L), eq(travelDate.plusDays(i)), eq(2));
        }
        // Không còn đường đọc-sửa-ghi nào chạm vào available_slots.
        verify(tourScheduleRepository, never()).saveAll(any());
        verify(tourScheduleRepository, never()).save(any(TourSchedule.class));
    }

    @Test
    void createBooking_ShouldReportRemainingSlots_WhenDayIsFull() {
        LocalDate travelDate = LocalDate.now().plusDays(7);
        mockTour.setDuration("1 ngày");

        BookingRequest request = new BookingRequest();
        request.setTourId(10L);
        request.setTravelDate(travelDate);
        request.setNumberOfPeople(5);

        TourSchedule almostFull = new TourSchedule();
        almostFull.setAvailableSlots(2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(tourRepository.findById(10L)).thenReturn(Optional.of(mockTour));
        when(tourScheduleRepository.insertIfAbsent(anyLong(), any(LocalDate.class), anyInt())).thenReturn(0);
        when(tourScheduleRepository.deductSlots(10L, travelDate, 5)).thenReturn(0);
        when(tourScheduleRepository.findFirstByTourIdAndDepartureDate(10L, travelDate))
                .thenReturn(Optional.of(almostFull));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> bookingService.createBooking(request, 1L));

        // Thông báo phải bằng tiếng Việt và nói rõ còn bao nhiêu chỗ, để khách
        // biết đường giảm số người thay vì bấm lại vô ích.
        assertTrue(ex.getMessage().contains("không đủ chỗ"), ex.getMessage());
        assertTrue(ex.getMessage().contains("chỉ còn 2 chỗ trống"), ex.getMessage());
        assertTrue(ex.getMessage()
                .contains(travelDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))),
                ex.getMessage());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_ShouldStopAtFirstFullDay_OfMultiDayTour() {
        LocalDate travelDate = LocalDate.now().plusDays(7);
        mockTour.setDuration("3 ngày 2 đêm");

        BookingRequest request = new BookingRequest();
        request.setTourId(10L);
        request.setTravelDate(travelDate);
        request.setNumberOfPeople(4);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(tourRepository.findById(10L)).thenReturn(Optional.of(mockTour));
        when(tourScheduleRepository.insertIfAbsent(anyLong(), any(LocalDate.class), anyInt())).thenReturn(0);
        when(tourScheduleRepository.deductSlots(10L, travelDate, 4)).thenReturn(1);
        when(tourScheduleRepository.deductSlots(10L, travelDate.plusDays(1), 4)).thenReturn(0);
        when(tourScheduleRepository.findFirstByTourIdAndDepartureDate(10L, travelDate.plusDays(1)))
                .thenReturn(Optional.of(new TourSchedule()));

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(request, 1L));

        // Ngày thứ 3 không được đụng tới; phần đã trừ của ngày 1 do transaction
        // rollback trả lại.
        verify(tourScheduleRepository, never()).deductSlots(10L, travelDate.plusDays(2), 4);
    }

    @Test
    void cancelBooking_ShouldRestoreSlotsForWholeDuration_ThroughAtomicUpdate() {
        LocalDate travelDate = LocalDate.now().plusDays(7);
        mockTour.setDuration("3 ngày 2 đêm");

        Booking booking = new Booking();
        booking.setId(99L);
        booking.setUser(mockUser);
        booking.setTour(mockTour);
        booking.setTravelDate(travelDate);
        booking.setNumberOfPeople(3);
        booking.setStatus("PENDING");

        when(bookingRepository.findById(99L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(new BookingResponse());

        bookingService.cancelBooking(99L, 1L);

        assertEquals("CANCELLED", booking.getStatus());
        verify(tourScheduleRepository).restoreSlots(10L, travelDate, travelDate.plusDays(2), 3);
        // Đường hoàn chỗ phải cùng cơ chế với đường trừ chỗ, nếu không một lần
        // hủy chạy song song với một lần đặt sẽ ghi đè kết quả của lần đặt.
        verify(tourScheduleRepository, never()).saveAll(any());
    }

    private Booking bookingOfMockUser(String status, LocalDate travelDate) {
        Booking booking = new Booking();
        booking.setId(99L);
        booking.setUser(mockUser);
        booking.setTour(mockTour);
        booking.setTravelDate(travelDate);
        booking.setNumberOfPeople(3);
        booking.setStatus(status);
        return booking;
    }

    @Test
    void cancelBooking_ShouldRefuseCustomerCancellingAPaidBooking() {
        // Hủy suông sẽ trả lại chỗ và lượt voucher trong khi tiền vẫn nằm ở
        // PayOS, payment vẫn SUCCESS, và không có dấu vết hoàn tiền nào.
        Booking booking = bookingOfMockUser("PAID", LocalDate.now().plusDays(7));
        when(bookingRepository.findById(99L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> bookingService.cancelBooking(99L, 1L));

        assertTrue(ex.getMessage().contains("liên hệ bộ phận hỗ trợ"), ex.getMessage());
        assertEquals("PAID", booking.getStatus());
        verify(tourScheduleRepository, never()).restoreSlots(any(), any(), any(), anyInt());
    }

    @Test
    void cancelBooking_ShouldLetAdminCancelAPaidBooking() {
        myproject.booking_tour.entity.Role adminRole = new myproject.booking_tour.entity.Role();
        adminRole.setName("ADMIN");
        User admin = new User();
        admin.setId(2L);
        admin.setRole(adminRole);

        Booking booking = bookingOfMockUser("PAID", LocalDate.now().plusDays(7));
        when(bookingRepository.findById(99L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(new BookingResponse());

        bookingService.cancelBooking(99L, 2L);

        assertEquals("CANCELLED", booking.getStatus());
    }

    @Test
    void cancelBooking_ShouldRefuseCancellingATourThatAlreadyDeparted() {
        // Chỗ hoàn lại là chỗ của ngày đã qua nên vô nghĩa, nhưng lượt VOUCHER
        // thì hoàn thật - khách đi tour xong vẫn lấy lại được voucher đã dùng.
        mockTour.setDuration("1 ngày");
        Booking booking = bookingOfMockUser("CONFIRMED", LocalDate.now().minusDays(1));
        when(bookingRepository.findById(99L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> bookingService.cancelBooking(99L, 1L));

        assertTrue(ex.getMessage().contains("đã khởi hành"), ex.getMessage());
        verify(tourScheduleRepository, never()).restoreSlots(any(), any(), any(), anyInt());
    }

    @Test
    void cancelBookingBySystem_ShouldNeverTouchAPaidBooking() {
        Booking booking = bookingOfMockUser("PAID", LocalDate.now().plusDays(7));
        when(bookingRepository.findById(99L)).thenReturn(Optional.of(booking));

        assertFalse(bookingService.cancelBookingBySystem(99L, "PayOS báo giao dịch EXPIRED"));

        assertEquals("PAID", booking.getStatus());
        verify(tourScheduleRepository, never()).restoreSlots(any(), any(), any(), anyInt());
    }

    @Test
    void cancelBookingBySystem_ShouldBeIdempotent_NotRefundSlotsTwice() {
        Booking booking = bookingOfMockUser("CANCELLED", LocalDate.now().plusDays(7));
        when(bookingRepository.findById(99L)).thenReturn(Optional.of(booking));

        assertFalse(bookingService.cancelBookingBySystem(99L, "PayOS báo giao dịch CANCELLED"));

        verify(tourScheduleRepository, never()).restoreSlots(any(), any(), any(), anyInt());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void cancelBookingBySystem_ShouldNotNeedTheBookingOwnerToStillExist() {
        // Đường hệ thống không đọc bảng users nữa. Trước đây nó truyền chính chủ
        // đơn vào chốt kiểm tra quyền để tự thỏa mãn nó - chốt đó không bảo vệ
        // được gì, chỉ làm việc hủy thất bại khi tài khoản chủ đơn đã bị xóa.
        mockTour.setDuration("2 ngày 1 đêm");
        LocalDate travelDate = LocalDate.now().plusDays(7);
        Booking booking = bookingOfMockUser("CONFIRMED", travelDate);
        when(bookingRepository.findById(99L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        assertTrue(bookingService.cancelBookingBySystem(99L, "quá hạn thanh toán"));

        assertEquals("CANCELLED", booking.getStatus());
        verify(tourScheduleRepository).restoreSlots(10L, travelDate, travelDate.plusDays(1), 3);
        verifyNoInteractions(userRepository);
    }

    @Test
    void createBooking_ShouldReadWeeksAsSevenDays() {
        // "1 tuần" từng rơi vào nhánh "lấy con số đầu tiên" và trả về 1: tour cả
        // tuần mà chỉ giữ chỗ đúng ngày khởi hành.
        LocalDate travelDate = LocalDate.now().plusDays(7);
        mockTour.setDuration("1 tuần");

        BookingRequest request = new BookingRequest();
        request.setTourId(10L);
        request.setTravelDate(travelDate);
        request.setNumberOfPeople(2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(tourRepository.findById(10L)).thenReturn(Optional.of(mockTour));
        when(tourScheduleRepository.insertIfAbsent(anyLong(), any(LocalDate.class), anyInt())).thenReturn(1);
        when(tourScheduleRepository.deductSlots(anyLong(), any(LocalDate.class), anyInt())).thenReturn(1);
        when(tourScheduleRepository.findFirstByTourIdAndDepartureDate(10L, travelDate))
                .thenReturn(Optional.of(new TourSchedule()));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(new BookingResponse());

        bookingService.createBooking(request, 1L);

        for (int i = 0; i < 7; i++) {
            verify(tourScheduleRepository).deductSlots(eq(10L), eq(travelDate.plusDays(i)), eq(2));
        }
        verify(tourScheduleRepository, never()).deductSlots(eq(10L), eq(travelDate.plusDays(7)), eq(2));
    }
}
