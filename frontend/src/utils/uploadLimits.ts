// Giới hạn kích thước ảnh tải lên.
//
// Con số này phải khớp với spring.servlet.multipart.max-file-size trong
// application.properties của backend. Trình duyệt không đọc được cấu hình đó
// nên phải viết lại ở đây; chốt chặn thật vẫn nằm ở server, chỗ này chỉ để
// người dùng biết ngay thay vì chờ tải hết file lên rồi mới nhận lỗi.
export const MAX_UPLOAD_SIZE_MB = 5;
export const MAX_UPLOAD_SIZE_BYTES = MAX_UPLOAD_SIZE_MB * 1024 * 1024;

/** Tên những file vượt quá giới hạn, rỗng nghĩa là tất cả đều hợp lệ. */
export const findOversizedFiles = (files: FileList | File[]): string[] =>
  Array.from(files)
    .filter(file => file.size > MAX_UPLOAD_SIZE_BYTES)
    .map(file => file.name);

/** Thông báo tiếng Việt cho danh sách file quá khổ. */
export const oversizedFilesMessage = (names: string[]): string =>
  names.length === 1
    ? `Ảnh "${names[0]}" vượt quá ${MAX_UPLOAD_SIZE_MB}MB. Vui lòng chọn ảnh nhỏ hơn.`
    : `${names.length} ảnh vượt quá ${MAX_UPLOAD_SIZE_MB}MB: ${names.join(', ')}. Vui lòng chọn ảnh nhỏ hơn.`;
