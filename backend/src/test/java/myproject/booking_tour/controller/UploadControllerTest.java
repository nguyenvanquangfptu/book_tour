package myproject.booking_tour.controller;

import myproject.booking_tour.security.JwtService;
import myproject.booking_tour.security.CustomUserDetailsService;

import myproject.booking_tour.service.FileUploadService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UploadController.class)
@AutoConfigureMockMvc(addFilters = false)
class UploadControllerTest {

    // JwtAuthenticationFilter nay inject repository nay; @WebMvcTest khong nap
    // tang repository nen phai mock.
    @org.springframework.boot.test.mock.mockito.MockBean
    private myproject.booking_tour.repository.InvalidatedTokenRepository invalidatedTokenRepository;

    @org.springframework.boot.test.mock.mockito.MockBean
    private JwtService jwtService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileUploadService fileUploadService;

    @Test
    void uploadFile_ShouldReturn200() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());

        Mockito.when(fileUploadService.uploadFile(any())).thenReturn("http://url");

        mockMvc.perform(multipart("/api/upload")
                .file(mockFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}

