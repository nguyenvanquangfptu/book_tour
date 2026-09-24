package myproject.booking_tour.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContactMessageRequest {
    // Ba cot duoi day la varchar(255). Khong gioi han o day thi mot chu de dai
    // hon di thang xuong database, bi tu choi voi "value too long" va thanh 500
    // tren mot form cong khai.
    @NotBlank(message = "Họ tên không được để trống")
    @jakarta.validation.constraints.Size(max = 255, message = "Họ tên tối đa 255 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @jakarta.validation.constraints.Size(max = 255, message = "Email tối đa 255 ký tự")
    private String email;

    @NotBlank(message = "Chủ đề không được để trống")
    @jakarta.validation.constraints.Size(max = 255, message = "Chủ đề tối đa 255 ký tự")
    private String subject;

    @NotBlank(message = "Nội dung không được để trống")
    @jakarta.validation.constraints.Size(max = 5000, message = "Nội dung tối đa 5000 ký tự")
    private String message;
}
