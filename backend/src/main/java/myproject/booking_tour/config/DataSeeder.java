package myproject.booking_tour.config;

import lombok.RequiredArgsConstructor;
import myproject.booking_tour.entity.Accommodation;
import myproject.booking_tour.entity.Utility;
import myproject.booking_tour.repository.AccommodationRepository;
import myproject.booking_tour.repository.UtilityRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final AccommodationRepository accommodationRepository;
    private final UtilityRepository utilityRepository;

    private void seedUtility(String name, String description) {
        if (!utilityRepository.existsByName(name)) {
            Utility u = new Utility();
            u.setName(name);
            u.setDescription(description);
            u.setIsActive(true);
            utilityRepository.save(u);
        }
    }

    private void seedAccommodation(String name, String address, String description) {
        // Just checking count for accommodation as before to keep it simple, or we can just leave it as count == 0
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed Utilities
        seedUtility("Khách sạn 5 sao", "Lưu trú tại khách sạn cao cấp 5 sao");
        seedUtility("Xe đưa đón tận nơi", "Di chuyển bằng xe du lịch đời mới");
        seedUtility("Hướng dẫn viên", "HDV nhiệt tình, giàu kinh nghiệm");
        seedUtility("Bữa ăn tiêu chuẩn", "Bao gồm các bữa ăn đặc sản địa phương");
        seedUtility("Bảo hiểm du lịch", "Bảo hiểm an toàn suốt tuyến cho du khách");
        seedUtility("Vé tham quan", "Đã bao gồm vé vào cổng các điểm tham quan");
        seedUtility("Nước uống & Khăn lạnh", "Phục vụ nước suối và khăn lạnh mỗi ngày trên xe");
        seedUtility("WiFi miễn phí", "Cung cấp WiFi tốc độ cao miễn phí trên xe di chuyển");
        seedUtility("Quà tặng du lịch", "Tặng nón du lịch cao cấp hoặc quà lưu niệm");
        seedUtility("Hỗ trợ Visa", "Tư vấn và hỗ trợ thủ tục xin Visa nhanh chóng (với tour quốc tế)");
        
        System.out.println("Seeded Utilities");

        // Seed Accommodations
        if (accommodationRepository.count() == 0) {
            Accommodation a1 = new Accommodation();
            a1.setName("Mường Thanh Luxury");
            a1.setAddress("Đà Nẵng");
            a1.setDescription("Khách sạn 5 sao chuẩn quốc tế tại Đà Nẵng");
            a1.setIsActive(true);

            Accommodation a2 = new Accommodation();
            a2.setName("Vinpearl Resort");
            a2.setAddress("Phú Quốc");
            a2.setDescription("Khu nghỉ dưỡng phức hợp đẳng cấp 5 sao");
            a2.setIsActive(true);

            Accommodation a3 = new Accommodation();
            a3.setName("InterContinental");
            a3.setAddress("Nha Trang");
            a3.setDescription("Khách sạn 5 sao mặt biển Nha Trang");
            a3.setIsActive(true);

            accommodationRepository.saveAll(Arrays.asList(a1, a2, a3));
            System.out.println("Seeded Accommodations");
        }
    }
}
