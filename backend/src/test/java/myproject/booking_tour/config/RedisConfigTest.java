package myproject.booking_tour.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Moi truong test khong co Redis - dung la tinh huong Redis sap o production.
 * Cache chi la toi uu, nen cac endpoint co cache van phai tra loi binh thuong.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RedisConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void cachedEndpoints_ShouldStillAnswer_WhenRedisIsUnreachable() throws Exception {
        mockMvc.perform(get("/api/tours/options")).andExpect(status().isOk());
        mockMvc.perform(get("/api/tours/popular-destinations")).andExpect(status().isOk());
    }
}
