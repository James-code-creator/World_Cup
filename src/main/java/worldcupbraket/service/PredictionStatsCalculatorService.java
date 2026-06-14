package worldcupbraket.service;

import worldcupbraket.domain.Prediction;
import worldcupbraket.domain.PredictionStats;

import java.util.List;

public class PredictionStatsCalculatorService {
    public static PredictionStats calculateOutcome(List<Prediction> predictions) {
        if (predictions == null || predictions.isEmpty()) {
            return new PredictionStats(0, 0, 0);
        }

        long team1Wins = predictions.stream()
                .filter(p -> p.Score1() > p.Score2())
                .count();

        long draws = predictions.stream()
                .filter(p -> p.Score1() == p.Score2())
                .count();

        long team2Wins = predictions.stream()
                .filter(p -> p.Score1() < p.Score2())
                .count();

        int total = predictions.size();

        return new PredictionStats(
                team1Wins * 100.0 / total,
                draws * 100.0 / total,
                team2Wins * 100.0 / total
        );
    }
}
