package myproject.booking_tour.controller;

import lombok.RequiredArgsConstructor;
import myproject.booking_tour.dto.response.ApiResponse;
import myproject.booking_tour.service.FileUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final FileUploadService fileUploadService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String url = fileUploadService.uploadFile(file);
            return ResponseEntity.ok(new ApiResponse<>(true, "File uploaded successfully!", url));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "File upload failed: " + e.getMessage(), null));
        }
    }

    @PostMapping("/multiple")
    public ResponseEntity<ApiResponse<List<String>>> uploadMultipleFiles(@RequestParam("files") List<MultipartFile> files) {
        try {
            List<String> urls = new ArrayList<>();
            for (MultipartFile file : files) {
                urls.add(fileUploadService.uploadFile(file));
            }
            return ResponseEntity.ok(new ApiResponse<>(true, "Files uploaded successfully!", urls));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Files upload failed: " + e.getMessage(), null));
        }
    }
}
