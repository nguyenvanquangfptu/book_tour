package myproject.booking_tour.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import myproject.booking_tour.dto.request.ChangePasswordRequest;
import myproject.booking_tour.dto.request.RegisterRequest;
import myproject.booking_tour.dto.request.ResetPasswordRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class StrongPasswordValidatorTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private Set<ConstraintViolation<ResetPasswordRequest>> validateReset(String password) {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("123456");
        request.setNewPassword(password);
        return validator.validate(request);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Passw0rd",        // vua du 8 ky tu, co chu va so
            "matkhau123",
            "a1bcdefgh",
            "Mat-Khau-2026"
    })
    void shouldAccept_ValidPasswords(String password) {
        assertThat(validateReset(password)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Pass0rd",         // 7 ky tu - thieu 1
            "abc12",
            "1"
    })
    void shouldReject_TooShort(String password) {
        assertThat(validateReset(password))
                .extracting(ConstraintViolation::getMessage)
                .anyMatch(m -> m.contains("ít nhất 8 ký tự"));
    }

    @Test
    void shouldReject_LettersOnly() {
        assertThat(validateReset("matkhaukhongso"))
                .extracting(ConstraintViolation::getMessage)
                .anyMatch(m -> m.contains("cả chữ và số"));
    }

    @Test
    void shouldReject_DigitsOnly() {
        assertThat(validateReset("12345678"))
                .extracting(ConstraintViolation::getMessage)
                .anyMatch(m -> m.contains("cả chữ và số"));
    }

    /**
     * BCrypt cat cut sau 72 BYTE va khong bao loi. Neu khong chan o day, hai mat
     * khau chi khac nhau tu byte 73 tro di se dang nhap lan nhau duoc.
     */
    @Test
    void shouldReject_LongerThan72Bytes() {
        String password = "a1" + "x".repeat(71);   // 73 ky tu ASCII = 73 byte
        assertThat(password.getBytes(StandardCharsets.UTF_8).length).isGreaterThan(72);

        assertThat(validateReset(password))
                .extracting(ConstraintViolation::getMessage)
                .anyMatch(m -> m.contains("quá dài"));
    }

    /**
     * Gioi han tinh theo BYTE chu khong theo ky tu: chu tieng Viet co dau chiem
     * 3 byte UTF-8, nen 30 ky tu da vuot 72 byte.
     */
    @Test
    void shouldCountBytes_NotCharacters_ForVietnameseText() {
        String password = "mậtkhẩu1" + "ố".repeat(25);   // 33 ky tu, nhung > 72 byte
        assertThat(password.length()).isLessThan(72);
        assertThat(password.getBytes(StandardCharsets.UTF_8).length).isGreaterThan(72);

        assertThat(validateReset(password))
                .extracting(ConstraintViolation::getMessage)
                .anyMatch(m -> m.contains("quá dài"));
    }

    /** Rong thi de @NotBlank lo - moi annotation mot viec. */
    @Test
    void shouldDeferEmptyToNotBlank() {
        assertThat(validateReset(""))
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("New password is required");
    }

    /**
     * Ba duong dat mat khau phai cung mot luat. Truoc day dang ky doi 6 ky tu,
     * doi mat khau doi 6, con "quen mat khau" thi khong doi gi - cua sau la
     * duong yeu nhat.
     */
    @Test
    void allThreeEntryPoints_ShouldShareTheSameRule() {
        String weak = "abc123";   // 6 ky tu - truoc day hop le o 2/3 noi

        RegisterRequest register = new RegisterRequest();
        register.setUsername("someone");
        register.setEmail("someone@example.com");
        register.setFullName("Some One");
        register.setPassword(weak);

        ChangePasswordRequest change = new ChangePasswordRequest();
        change.setOldPassword("cu");
        change.setNewPassword(weak);
        change.setConfirmPassword(weak);

        assertThat(validator.validate(register)).isNotEmpty();
        assertThat(validator.validate(change)).isNotEmpty();
        assertThat(validateReset(weak)).isNotEmpty();
    }
}
