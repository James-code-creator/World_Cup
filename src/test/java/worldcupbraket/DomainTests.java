package worldcupbraket;

import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;
import worldcupbraket.domain.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;


class DomainTests {
    @Test
    void testWordCupBraket() {
        WorldCupBraket braket = new WorldCupBraket(Matches.loadOverNet().matches());
        List<Match> matches = braket.getMatches();
        Assert.isTrue(
                !matches.isEmpty(),
                "No matches were loaded"
        );
        Score score = matches.getFirst().score();
        assert score != null;
        Assert.isTrue(
                score.ft().getFirst() == 2,
                "No matches were loaded"
        );
        Player newPlayer = new Player("David Johns");
        braket.addPlayer(newPlayer);
        List<Player> players = braket.getPlayers();
        Assert.isTrue(
                !players.isEmpty(),
                "No players were loaded"
        );
        Prediction prediction = new Prediction(matches.getFirst(), 0, 0);
        braket.predictMatch(prediction, newPlayer);
        Result result = new Result(matches.getFirst(), 0, 0);
        braket.recordMatchResult(result);
        List<Player> scoreboard = braket.getScoreboard();
        Assert.isTrue(
                scoreboard.getFirst().getPoints() == 10,
                "The best player does not have 10 points"
        );
        // Try to predict a game that is in the past should fail
        assertThrows(
                IllegalStateException.class,
                () -> braket.predictMatch(prediction, newPlayer)
        );
    }

    @Test
    void loadTournament() {
        Matches matches = Matches.loadLocal();
        Assert.isTrue(
                matches.name().equals("World Cup 2026"),
                "Tournament name is not correct"
        );
        Assert.isTrue(
                matches.matches().getFirst().team1().equals("Mexico"),
                "First match team 1 is not correct"
        );
        Assert.isTrue(
                !matches.matches().get(2).team1().equals("Czech Republic"),
                "Third match team 1 is not correct"
        );
    }

    @Test
    void makeAPrediction() {
        record Game(
                Prediction prediction,
                Result result,
                int points
        ) {}

        List<Match> matches = Matches.loadLocal().matches();
        Match groupMatch = matches.getFirst();
        Match koMatch = matches.get(matches.size() - 2);
        Match finalMatch = matches.getLast();

        List<Game> games = List.of(
            new Game(new Prediction(groupMatch, 1, 0), new Result(groupMatch,1, 0), 10),
            new Game(new Prediction(groupMatch,2, 1), new Result(groupMatch,3, 2), 8),
            new Game(new Prediction(groupMatch,2, 1), new Result(groupMatch,2, 0), 6),
            new Game(new Prediction(groupMatch,3, 0), new Result(groupMatch,2, 1), 5),
            new Game(new Prediction(groupMatch,2, 1), new Result(groupMatch,0, 1), 1),
            new Game(new Prediction(groupMatch,2, 1), new Result(groupMatch,0, 2), 0),
            new Game(new Prediction(koMatch, 1, 0), new Result(koMatch,1, 0), 20),
            new Game(new Prediction(koMatch,2, 1), new Result(koMatch,3, 2), 16),
            new Game(new Prediction(koMatch,2, 1), new Result(koMatch,2, 0), 12),
            new Game(new Prediction(koMatch,3, 0), new Result(koMatch,2, 1), 10),
            new Game(new Prediction(koMatch,2, 1), new Result(koMatch,0, 1), 2),
            new Game(new Prediction(koMatch,2, 1), new Result(koMatch,0, 2), 0),
            new Game(new Prediction(finalMatch, 1, 0), new Result(finalMatch,1, 0), 50),
            new Game(new Prediction(finalMatch,2, 1), new Result(finalMatch,3, 2), 40),
            new Game(new Prediction(finalMatch,2, 1), new Result(finalMatch,2, 0), 30),
            new Game(new Prediction(finalMatch,3, 0), new Result(finalMatch,2, 1), 25),
            new Game(new Prediction(finalMatch,2, 1), new Result(finalMatch,0, 1), 5),
            new Game(new Prediction(finalMatch,2, 1), new Result(finalMatch,0, 2), 0)
        );

        Player player;
        for (Game game : games) {
            player = new Player("John Doe");
            player.predict(game.prediction);
            player.gradeMatch(game.result);
            Assert.isTrue(
                    player.getPoints() == game.points,
                    "Player points are not correct"
            );
            Assert.isTrue(
                    player.getDiffPointsScoredPerMatch().getLast() == game.points,
                    "Player diff points are not correct"
            );
        }
    }
}
