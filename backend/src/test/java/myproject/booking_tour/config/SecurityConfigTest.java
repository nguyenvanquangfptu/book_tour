package myproject.booking_tour.config;

import myproject.booking_tour.dto.response.VoucherResponse;
import myproject.booking_tour.entity.Role;
import myproject.booking_tour.entity.User;
import myproject.booking_tour.repository.RoleRepository;
import myproject.booking_tour.repository.UserRepository;
import myproject.booking_tour.security.JwtService;
import myproject.booking_tour.service.VoucherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Luat phan quyen trong SecurityConfig, chay qua bo loc that.
 *
 * Cac test controller deu tat filter (addFilters = false), nen truoc day khong
 * co gi kiem tra cac dong requestMatchers - moi lo hong o day chi lo ra khi co
 * nguoi goi thu.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    private static final String CUSTOMER_USERNAME = "security-test-customer";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    @MockBean
    private VoucherService voucherService;

    @org.springframework.beans.factory.annotation.Value("${app.admin.username}")
    private String adminUsername;

    @BeforeEach
    void ensureCustomerExists() {
        if (userRepository.existsByUsername(CUSTOMER_USERNAME)) {
            return;
        }
        Role customerRole = roleRepository.findByName("CUSTOMER").orElseThrow();
        User customer = new User();
        customer.setUsername(CUSTOMER_USERNAME);
        customer.setPassword("not-used");
        customer.setEmail("security-test-customer@test.com");
        customer.setFullName("Security Test");
        customer.setRole(customerRole);
        userRepository.save(customer);
    }

    private String bearer(String username) {
        return "Bearer " + jwtService.generateToken(username);
    }

    @Test
    void customer_ShouldNotBeAbleToListEveryVoucher() throws Exception {
        mockMvc.perform(get("/api/vouchers").header("Authorization", bearer(CUSTOMER_USERNAME)))
                .andExpect(status().isForbidden());
        Mockito.verifyNoInteractions(voucherService);
    }

    @Test
    void customer_ShouldNotBeAbleToReadVouchersById() throws Exception {
        mockMvc.perform(get("/api/vouchers/1").header("Authorization", bearer(CUSTOMER_USERNAME)))
                .andExpect(status().isForbidden());
    }

    @Test
    void customer_ShouldStillLookUpTheCodeTheyWereGiven() throws Exception {
        Mockito.when(voucherService.getVoucherByCode("SUMMER")).thenReturn(new VoucherResponse());

        mockMvc.perform(get("/api/vouchers/code/SUMMER").header("Authorization", bearer(CUSTOMER_USERNAME)))
                .andExpect(status().isOk());
    }

    @Test
    void admin_ShouldStillListVouchers() throws Exception {
        Mockito.when(voucherService.getAllVouchers()).thenReturn(List.of());

        mockMvc.perform(get("/api/vouchers").header("Authorization", bearer(adminUsername)))
                .andExpect(status().isOk());
    }
}
