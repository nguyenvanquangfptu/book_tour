package myproject.booking_tour.mapper;

import myproject.booking_tour.dto.response.BookingResponse;
import myproject.booking_tour.entity.Booking;
import myproject.booking_tour.entity.User;
import myproject.booking_tour.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class BookingMapperTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private BookingMapper bookingMapper;

    private static Booking bookingBy(String accountName, String contactName) {
        User account = new User();
        account.setId(7L);
        account.setFullName(accountName);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(account);
        booking.setCustomerName(contactName);
        booking.setCustomerPhone("0901234567");
        return booking;
    }

    /**
     * Dat ho nguoi khac: ten, email, so dien thoai lien he deu la cua nguoi di.
     * Truoc day ten bi thay bang ten tai khoan, trong khi so dien thoai van la
     * cua nguoi di - admin goi dien hoi mot nguoi khong ton tai o dau day.
     */
    @Test
    void customerName_ShouldBeTheContactNameEnteredAtCheckout() {
        BookingResponse response = bookingMapper.toResponse(bookingBy("Nguyen Van A", "Tran Thi B"));

        assertEquals("Tran Thi B", response.getCustomerName());
        assertEquals("0901234567", response.getCustomerPhone());
    }

    @Test
    void customerName_ShouldFallBackToTheAccountName_WhenBookingHasNone() {
        assertEquals("Nguyen Van A", bookingMapper.toResponse(bookingBy("Nguyen Van A", null)).getCustomerName());
        assertEquals("Nguyen Van A", bookingMapper.toResponse(bookingBy("Nguyen Van A", "  ")).getCustomerName());
    }
}
