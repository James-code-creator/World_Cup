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
        List<Match> matches = Matches.loadLocal().matches();
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
}
