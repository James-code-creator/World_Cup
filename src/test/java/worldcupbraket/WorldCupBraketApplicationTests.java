package worldcupbraket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class WorldCupBraketApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testWordCupBraket() {
        WorldCupBraket braket = new WorldCupBraket();
        List<Match> matches = braket.getMatches();
        Assert.isTrue(
                !matches.isEmpty(),
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
                scoreboard.getFirst().points == 10,
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

        Match match = Tournament.load().matches().getFirst();

        List<Game> games = List.of(
            new Game(new Prediction(match, 1, 0), new Result(match,1, 0), 10),
            new Game(new Prediction(match,2, 1), new Result(match,3, 2), 8),
            new Game(new Prediction(match,2, 1), new Result(match,2, 0), 6),
            new Game(new Prediction(match,3, 0), new Result(match,2, 1), 5),
            new Game(new Prediction(match,2, 1), new Result(match,0, 1), 1),
            new Game(new Prediction(match,2, 1), new Result(match,0, 2), 0)
        );

        Player player;
        for (Game game : games) {
            player = new Player("John Doe");
            player.predict(game.prediction);
            player.gradeMatch(game.result);
            Assert.isTrue(
                    player.points == game.points,
                    "Player points are not correct"
            );
        }
    }

    @Test
    void testDatabase() {
        UserService userService = new UserService(userRepository, passwordEncoder);
        User user = userService.createUser("ranaldo", "password123");

        User user2 = userRepository.findFirstByName("ranaldo");
        Assert.isTrue(
                user2 != null,
                "User was not found in the database"
        );
    }

    @Test
    void testUserService() {
        UserService userService = new UserService(userRepository, passwordEncoder);
        User user = userService.createUser("messi", "password123");
        Assert.isTrue(
                user != null,
                "User was not created"
        );
        boolean success = userService.authenticate("messi", "password123");
        Assert.isTrue(
                success,
                "User was not able to login"
        );
        success = userService.authenticate("messi", "wrong password");
        Assert.isTrue(
                !success,
                "User was able to login with wrong password"
        );
    }

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }
}
