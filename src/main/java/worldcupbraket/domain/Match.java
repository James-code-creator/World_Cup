package worldcupbraket.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public record Match(
    String round,
    String date,
    String time,
    String team1,
    String team2,
    String group,
    String ground
) {
    public boolean hasStarted() {
        String[] parts = time.split(" ");
        ZoneId zone = ZoneId.of(parts[1]);
        ZonedDateTime matchStart = ZonedDateTime.of(
                LocalDate.parse(date),
                LocalTime.parse(parts[0]),
                zone
        );
        return !ZonedDateTime.now(zone).isBefore(matchStart);
    }
}