package myproject.booking_tour.controller;

import myproject.booking_tour.security.JwtService;
import myproject.booking_tour.security.CustomUserDetailsService;

import com.fasterxml.jackson.databind.ObjectMapper;
import myproject.booking_tour.dto.request.ContactMessageRequest;
import myproject.booking_tour.dto.response.ContactMessageResponse;
import myproject.booking_tour.service.ContactMessageService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContactMessageController.class)
@AutoConfigureMockMvc(addFilters = false)
class ContactMessageControllerTest {

    @org.springframework.boot.test.mock.mockito.MockBean
    private JwtService jwtService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactMessageService contactMessageService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createMessage_ShouldReturn200() throws Exception {
        ContactMessageRequest request = new ContactMessageRequest();
        request.setFullName("John Doe");
        request.setEmail("john@example.com");
        request.setSubject("Test");
        request.setMessage("Test Message");

        Mockito.when(contactMessageService.createMessage(any(ContactMessageRequest.class)))
                .thenReturn(Mockito.mock(ContactMessageResponse.class));

        mockMvc.perform(post("/api/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getAllMessages_ShouldReturn200() throws Exception {
        Mockito.when(contactMessageService.getAllMessages()).thenReturn(List.of(Mockito.mock(ContactMessageResponse.class)));

        mockMvc.perform(get("/api/contacts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateStatus_ShouldReturn200() throws Exception {
        Mockito.when(contactMessageService.updateMessageStatus(eq(1L), eq("RESOLVED")))
                .thenReturn(Mockito.mock(ContactMessageResponse.class));

        mockMvc.perform(put("/api/contacts/1/status")
                .param("status", "RESOLVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}

