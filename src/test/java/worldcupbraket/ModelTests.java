package worldcupbraket;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;
import worldcupbraket.model.*;
import worldcupbraket.service.UserService;

@SpringBootTest
@Transactional
public class ModelTests {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PredictionRepository predictionRepository;
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testUserDatabase() {
        UserService userService = new UserService(userRepository, passwordEncoder);
        userService.createUser("ranaldo", "password123");

        User user2 = userRepository.findFirstByName("ranaldo");
        Assert.isTrue(
                user2 != null,
                "User was not found in the database"
        );
        MatchModel match = matchRepository.save(new MatchModel(
                1L,
                "Matchday 1",
                "2026-06-11",
                "18:00",
                "Mexico",
                "South Africa",
                "A",
                "Stadium"
        ));
        PredictionModel prediction = new PredictionModel(
            match,
            0,
            0
        );

        user2.addPrediction(prediction);
        user2 = userRepository.save(user2);
        Assert.isTrue(
                user2.getPredictionFromMatch(match).score1 == 0,
                "The prediction was not saved correctly"
        );
        user2.removePrediction(prediction);
        userRepository.saveAndFlush(user2);
        User reloadedUser = userRepository.findFirstByName("ranaldo");
        Assert.isTrue(
                reloadedUser.getAllPredictions().isEmpty(),
                "The prediction was not removed from the user"
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
        predictionRepository.deleteAll();
        userRepository.deleteAll();
        matchRepository.deleteAll();
    }
}
