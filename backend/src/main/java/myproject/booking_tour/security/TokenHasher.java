package myproject.booking_tour.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Bam SHA-256 mot chieu, tra ve hex 64 ky tu.
 *
 * Dung cho refresh token: database chi luu ban bam, con ban goc chi ton tai
 * trong cookie cua trinh duyet. Lo ban sao database thi ke tan cong van khong
 * co token nao dung duoc.
 *
 * Khong can salt hay bcrypt: dau vao la 256 bit ngau nhien tu SecureRandom nen
 * khong ton tai khong gian de do doan hay bang tra nguoc - khac han mat khau
 * do nguoi dung tu dat.
 */
public final class TokenHasher {

    private TokenHasher() {
    }

    public static String sha256Hex(String raw) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256")
                    .digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 la thuat toan bat buoc co trong moi JVM
            throw new IllegalStateException("Khong tim thay thuat toan SHA-256", e);
        }
    }
}
