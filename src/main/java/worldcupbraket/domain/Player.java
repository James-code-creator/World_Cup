package worldcupbraket.domain;


import java.util.HashMap;
import java.util.Map;

public class Player {
    public final String name;
    private final Map<Match, Prediction> predictions;
    int points;

    public Player(String name) {
        this.name = name;
        this.points = 0;
        this.predictions = new HashMap<>();
    }

    public int getPoints() {
        return points;
    }

    public void predict(Prediction prediction) {
        predictions.put(prediction.Match(), prediction);
    }

    public void gradeMatch(Result result) {
        if (predictions.containsKey(result.Match())) {
            Prediction prediction = predictions.get(result.Match());
            points += calculatePoints(prediction, result);        }
    }

    public Map<Match, Prediction> getPredictions() {
        return predictions;
    }

    private int calculatePoints(Prediction prediction, Result result) {
        int correctWinner = calculatePointsForWinner(prediction, result);
        int correctScore1 = prediction.Score1() == result.Score1() ? 1 : 0;
        int correctScore2 = prediction.Score2() == result.Score2() ? 1 : 0;
        int correctDiff =
                Math.abs(prediction.Score1() - prediction.Score2())
                == Math.abs(result.Score1() - result.Score2())
                && correctWinner != 0
                ? 3 : 0;
        return correctWinner + correctScore1 + correctScore2 + correctDiff;
    }

    private int calculatePointsForWinner(Prediction prediction, Result result) {
        boolean team1Won = prediction.Score1() > prediction.Score2()
                && result.Score1() > result.Score2();
        boolean team2Won = prediction.Score1() < prediction.Score2()
                && result.Score1() < result.Score2();
        boolean draw = prediction.Score1() == prediction.Score2()
                && result.Score1() == result.Score2();
        return team1Won ? 5 : team2Won ? 5 : draw ? 5 : 0;
    }
}
