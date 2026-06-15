package worldcupbraket;

import org.junit.jupiter.api.Test;
import worldcupbraket.domain.*;
import worldcupbraket.service.GraphService;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
}
