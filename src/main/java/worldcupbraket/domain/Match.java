package worldcupbraket.domain;

public record Match(
    String round,
    String date,
    String time,
    String team1,
    String team2,
    String group,
    String ground
) {
}