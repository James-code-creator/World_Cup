package worldcupbraket;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.util.List;

@SpringBootTest
class WorldCupBraketApplicationTests {

    @Test
    void loadTournament() {
        Tournament tournament = Tournament.load();
        Assert.isTrue(
                tournament.name().equals("World Cup 2026"),
                "Tournament name is not correct"
        );
        Assert.isTrue(
                tournament.matches().getFirst().team1().equals("Mexico"),
                "First match team 1 is not correct"
        );
        Assert.isTrue(
                !tournament.matches().get(2).team1().equals("Czech Republic"),
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

        List<Game> games = List.of(
            new Game(new Prediction(1, 0), new Result(1, 0), 10),
            new Game(new Prediction(2, 1), new Result(3, 2), 8),
            new Game(new Prediction(2, 1), new Result(2, 0), 6),
            new Game(new Prediction(3, 0), new Result(2, 1), 5),
            new Game(new Prediction(2, 1), new Result(0, 1), 1),
            new Game(new Prediction(2, 1), new Result(0, 2), 0)
        );

        Player player;
        for (Game game : games) {
            player = new Player("John Doe");
            player.predict(0, game.prediction);
            player.gradeMatch(0, game.result);
            Assert.isTrue(
                    player.points == game.points,
                    "Player points are not correct"
            );
        }
    }
}
