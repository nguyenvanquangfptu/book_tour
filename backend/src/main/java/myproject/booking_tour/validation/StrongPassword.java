package myproject.booking_tour.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Mot luat mat khau duy nhat cho ca he thong.
 *
 * Truoc khi co annotation nay, ba noi dat mat khau moi co ba luat khac nhau:
 *
 *     RegisterRequest.password        @Size(min = 6, max = 100)
 *     ChangePasswordRequest.newPassword  @Size(min = 6)      - khong co max
 *     ResetPasswordRequest.newPassword   @NotBlank           - khong co gi ca
 *
 * Nghia la mot nguoi dung dang ky voi mat khau 8 ky tu van co the ha xuong mot
 * ky tu bang cach di duong "quen mat khau". Cua sau la duong yeu nhat, khong
 * phai duong chinh. Gom ve mot cho de khong con troi dat.
 *
 * Luat kiem tra xem trong {@link StrongPasswordValidator}.
 */
@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface StrongPassword {

    String message() default "Mật khẩu không hợp lệ.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
