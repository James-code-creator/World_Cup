package worldcupbraket.domain;

import java.util.*;

public class WorldCupBraket {
    Map<String, Player> players = new HashMap<>();
    Map<Match, Result> results = new HashMap<>();
    List<Match> matches = Matches.loadLocal().matches();

    public WorldCupBraket() {}

    public WorldCupBraket(List<Match> matches) {
        this.matches = matches;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public void addPlayer(Player player) {
        players.put(player.name, player);
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players.values());
    }

    public Player getPlayer(String name) {
        return players.get(name);
    }

    public void predictMatch(Prediction prediction, Player player) throws IllegalStateException {
        if (results.containsKey(prediction.Match())) {
            throw new IllegalStateException(
                "You can not predict a match that is already in the past"
            );
        }
        players.get(player.name).predict(prediction);
    }

    public void recordMatchResult(Result result) {
        results.put(result.Match(), result);
        for (Player player : players.values()) {
            player.gradeMatch(result);
        }
    }

    public List<Player> getScoreboard() {
        List<Player> list = new ArrayList<>(players.values());
        list.sort(Comparator.comparing(Player::getPoints));
        return list;
    }

    public List<Player> getScoreboard(Phase phase) {
        List<Player> list = new ArrayList<>(players.values());
        list.sort(
            Comparator.comparingInt(
                    (Player player) -> player.getPoints(phase)
            )
        );
        return list;
    }

    public Map<Match, List<Prediction>> getAllPredictions() {
        Map<Match, List<Prediction>> allPredictions = new HashMap<>();
        players.forEach((_, player) -> {
            player.getPredictions().forEach((match, prediction) -> {
                if (allPredictions.containsKey(match)) {
                    allPredictions.get(match).add(prediction);
                } else {
                    List<Prediction> predictions = new ArrayList<>();
                    predictions.add(prediction);
                    allPredictions.put(match, predictions);
                }
            });
        });
        return allPredictions;
    }

    public Map<Match, Result> getResults() {
        return results;
    }
}
