package worldcupbraket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.Assert;
import worldcupbraket.domain.*;
import worldcupbraket.domain.livematch.*;
import worldcupbraket.model.MatchRepository;
import worldcupbraket.model.MatchResultRepository;
import worldcupbraket.model.PredictionRepository;
import worldcupbraket.model.UserRepository;
import worldcupbraket.service.GraphService;
import worldcupbraket.service.LiveMatchService;
import worldcupbraket.service.WorldCupBraketService;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ServiceTests {
    @Test
    void testGraphService() throws IOException {
        List<Match> matches = Matches.load().matches();
        List<Player> players = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Player player = new Player("Player "+i);
            players.add(player);
        }

        players.forEach(player -> {
            for (int i = 0; i < 10; i++) {
                Prediction prediction = new Prediction(
                    matches.get(i),
                    (int) (Math.random() * 4),
                    (int) (Math.random() * 4)
                );
                player.predict(prediction);
                Result result = new Result(
                    matches.get(i),
                    (int) (Math.random() * 4),
                    (int) (Math.random() * 4)
                );
                player.gradeMatch(result);
            }
        });


        BufferedImage image = GraphService.createPlayersScoreBoardGraph(players);

        ImageIO.write(image, "png", new File("graph.png"));
    }

    @Mock
    MatchRepository matchRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    PredictionRepository predictionRepository;

    @Mock
    MatchResultRepository matchResultRepository;

    @Mock
    LiveMatchService liveMatchService;

    @Test
    void updatesLiveMatchResult_WhenLiveMatchExists() throws Exception {
        LiveMatch liveMatch = new LiveMatch(
                new Sport(0, "football"),
                "Live",
                "USA - Mexico",
                new ContestInfo(new ContestSeason("5193")),
                new DateTimeInfo("2026-06-19T00:00:00"),
                new Competitor(1, "USA", new Results(99)),
                new Competitor(2, "Mexico", new Results(98))
        );

        when(userRepository.findAll()).thenReturn(List.of());
        when(matchRepository.findFirstByDateAndTimeAndTeam1AndTeam2(
                anyString(),
                anyString(),
                anyString(),
                anyString()
        )).thenReturn(null);
        when(matchResultRepository.findAll()).thenReturn(List.of());
        when(liveMatchService.getCurrent()).thenReturn(liveMatch);


        WorldCupBraketService service = new WorldCupBraketService(
                matchRepository,
                userRepository,
                predictionRepository,
                matchResultRepository,
                liveMatchService
        );

        WorldCupBraket braket = service.getInstance();

        assertNotNull(braket);
        Match latest = braket.getMatches().stream().filter(Match::hasStarted).toList().getLast();
        Result result = braket.getResults().get(latest);
        Assert.isTrue(result.Score1() == 99, "Match was not Live updated");
        Assert.isTrue(result.Score2() == 98, "Match was not Live updated");
    }
}
