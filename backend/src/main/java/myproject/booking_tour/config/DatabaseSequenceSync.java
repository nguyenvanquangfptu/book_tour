package myproject.booking_tour.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DatabaseSequenceSync {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void syncSequences() {
        try {
            String[] tables = {"tour_schedules", "bookings", "tours", "users", "vouchers", "reviews"};
            for (String table : tables) {
                try {
                    String sql = String.format("SELECT setval(pg_get_serial_sequence('%s', 'id'), coalesce(max(id), 1), max(id) IS NOT null) FROM %s", table, table);
                    jdbcTemplate.execute(sql);
                    log.info("Synced sequence for table: " + table);
                } catch (Exception e) {
                    log.warn("Could not sync sequence for table " + table + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error syncing sequences", e);
        }
    }
}
