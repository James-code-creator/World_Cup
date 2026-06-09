package worldcupbraket;

import java.util.*;

public class WorldCupBraket {
    Tournament tournament = Tournament.load();
    Map<Match, Result> results = new HashMap<>();
    Map<String, Player> players = new HashMap<>();

    public WorldCupBraket() {

    }

    public List<Match> getMatches() {
        return tournament.matches();
    }

    public void addPlayer(Player player) {
        players.put(player.name, player);
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players.values());
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
}
