package worldcupbraket.domain.livematch;

public record Competitor(
    int id,
    String countryName,
    Results results
){}
