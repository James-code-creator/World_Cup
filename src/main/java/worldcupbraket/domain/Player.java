package worldcupbraket.domain;


import java.util.*;

public class Player {
    public final String name;
    private final Map<Match, Prediction> predictions;
    int points;
    int lastPointChange;
    List<Integer> pointProgression = new ArrayList<>();

    public Player(String name) {
        this.name = name;
        this.points = 0;
        this.predictions = new HashMap<>();
        pointProgression.add(points);
    }

    public void predict(Prediction prediction) {
        predictions.put(prediction.Match(), prediction);
    }

    public void gradeMatch(Result result) {
        if (predictions.containsKey(result.Match())) {
            Prediction prediction = predictions.get(result.Match());
            points += calculatePoints(prediction, result);
            pointProgression.add(points);
            lastPointChange = pointProgression.size() < 2
                    ? points
                    : pointProgression.getLast() - pointProgression.get(pointProgression.size() - 2);
        } else {
            pointProgression.add(points);
            lastPointChange = 0;
        }
    }

    public int getPoints() {
        return points;
    }

    public int getLastPointChange() {
        return lastPointChange;
    }

    public List<Integer> getPointProgression() {
        return pointProgression;
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

        int points = correctWinner + correctScore1 + correctScore2 + correctDiff;

        boolean isFinal = Objects.equals(prediction.Match().round(), "Final");
        boolean isKoGame = prediction.Match().group() == null && !isFinal;
        boolean isGroupGame = prediction.Match().group() != null;

        if (isGroupGame) {
            return points;
        } else if (isKoGame) {
            return points * 2;
        } else if (isFinal) {
            return points * 5;
        }
        return points;
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
