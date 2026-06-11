package worldcupbraket.domain;

public record Prediction(
        Match Match,
        int Score1,
        int Score2
) {}
