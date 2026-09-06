package myproject.booking_tour.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.charset.StandardCharsets;

/**
 * Bon luat, moi luat mot ly do cu the.
 */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private static final int MIN_LENGTH = 8;

    /**
     * BCrypt CAT CUT moi thu qua 72 BYTE va khong bao gio bao loi. Hai mat khau
     * chi khac nhau tu byte thu 73 tro di se bam ra cung mot chuoi va dang nhap
     * lan nhau duoc. Gioi han theo BYTE chu khong theo ky tu: mot chu tieng Viet
     * co dau chiem 2-3 byte UTF-8, nen 72 ky tu co the vuot 72 byte tu lau.
     */
    private static final int MAX_BYTES = 72;

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        // null / rong de @NotBlank lo - moi annotation mot viec.
        if (password == null || password.isEmpty()) {
            return true;
        }

        context.disableDefaultConstraintViolation();

        if (password.length() < MIN_LENGTH) {
            return reject(context, "Mật khẩu phải có ít nhất " + MIN_LENGTH + " ký tự.");
        }

        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            return reject(context, "Mật khẩu quá dài (tối đa " + MAX_BYTES + " byte).");
        }

        boolean hasLetter = false;
        boolean hasDigit = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
            if (hasLetter && hasDigit) {
                break;
            }
        }

        // Chi doi chu VA so. Khong bat buoc ky tu dac biet hay chu hoa: cac luat
        // do day nguoi dung toi "Matkhau1!" va toi viec ghi mat khau ra giay,
        // trong khi do dai moi la thu thuc su lam tang chi phi do tim.
        if (!hasLetter || !hasDigit) {
            return reject(context, "Mật khẩu phải chứa cả chữ và số.");
        }

        return true;
    }

    private boolean reject(ConstraintValidatorContext context, String message) {
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        return false;
    }
}
