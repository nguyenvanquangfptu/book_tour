package myproject.booking_tour.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import myproject.booking_tour.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif");

    private final Cloudinary cloudinary;

    /**
     * Cung mot nguon voi gioi han ma Spring dung de chan multipart. Truoc day
     * cho nay la hang so 5MB viet cung: doi gioi han trong application.properties
     * ma quen sua hang so nay thi hai con so lech nhau, va thong bao loi se noi
     * mot muc trong khi he thong chan o muc khac.
     */
    @org.springframework.beans.factory.annotation.Value("${spring.servlet.multipart.max-file-size:1MB}")
    private org.springframework.util.unit.DataSize maxFileSize;

    public String uploadFile(MultipartFile multipartFile) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new BadRequestException("File không được để trống!");
        }
        if (multipartFile.getSize() > maxFileSize.toBytes()) {
            throw new BadRequestException(
                    "File vượt quá kích thước tối đa cho phép (" + maxFileSize.toMegabytes() + "MB)!");
        }
        String contentType = multipartFile.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Chỉ chấp nhận file ảnh (JPEG, PNG, WEBP, GIF)!");
        }

        // secure_url chu khong phai url: Cloudinary tra ve ca hai, va "url" la
        // ban http. Mot trang chay https ma nhung anh http vao thi trinh duyet
        // chan het vi noi dung hon hop - anh bia tour, anh dai dien, thu vien
        // anh deu khong hien. Nhung URL da luu trong database van la http, chi
        // nhung lan tai len tu day tro di moi doi.
        return cloudinary.uploader()
                .upload(multipartFile.getBytes(),
                        ObjectUtils.asMap("public_id", UUID.randomUUID().toString(),
                                "folder", "booking_tour"))
                .get("secure_url")
                .toString();
    }
}
