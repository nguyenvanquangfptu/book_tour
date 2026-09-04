package myproject.booking_tour.repository;

import myproject.booking_tour.entity.Booking;
import myproject.booking_tour.entity.Payment;
import myproject.booking_tour.entity.Role;
import myproject.booking_tour.entity.User;
import myproject.booking_tour.entity.Tour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private TourRepository tourRepository;

    private Booking testBooking;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setName("ROLE_USER");
        roleRepository.save(role);

        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setFullName("Test User");
        testUser.setEmail("test@test.com");
        testUser.setPassword("pass");
        testUser.setRole(role);
        userRepository.save(testUser);

        Tour testTour = new Tour();
        testTour.setTitle("Test Tour");
        testTour.setPrice(BigDecimal.valueOf(100));
        tourRepository.save(testTour);

        testBooking = new Booking();
        testBooking.setUser(testUser);
        testBooking.setTour(testTour);
        testBooking.setNumberOfPeople(2);
        testBooking.setTotalPrice(BigDecimal.valueOf(200));
        bookingRepository.save(testBooking);

        Payment payment = new Payment();
        payment.setBooking(testBooking);
        payment.setAmount(BigDecimal.valueOf(100));
        payment.setPaymentStatus("PAID");
        payment.setPaymentMethod("PAYOS");
        payment.setOrderCode("17824664510005");
        paymentRepository.save(payment);
    }

    @Test
    void findByBooking_ShouldReturnPayment() {
        Optional<Payment> result = paymentRepository.findByBooking(testBooking);
        assertThat(result).isPresent();
        assertThat(result.get().getAmount()).isEqualByComparingTo("100");
    }

    @Test
    void findByPaymentStatus_ShouldReturnPayments() {
        List<Payment> result = paymentRepository.findByPaymentStatus("PAID");
        assertThat(result).hasSize(1);
    }

    @Test
    void findByOrderCode_ShouldReturnPayment() {
        Optional<Payment> result = paymentRepository.findByOrderCode("17824664510005");
        assertThat(result).isPresent();
    }

    @Test
    void findByPaymentStatusAndOrderCodeNotNull_ShouldSkipPaymentsWithoutOrderCode() {
        Payment khongCoMaDonHang = new Payment();
        khongCoMaDonHang.setBooking(testBooking);
        khongCoMaDonHang.setAmount(new java.math.BigDecimal("100.00"));
        khongCoMaDonHang.setPaymentStatus("PAID");
        khongCoMaDonHang.setPaymentMethod("CASH");
        paymentRepository.save(khongCoMaDonHang);

        List<Payment> result = paymentRepository.findByPaymentStatusAndOrderCodeNotNull("PAID");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderCode()).isEqualTo("17824664510005");
    }
}
