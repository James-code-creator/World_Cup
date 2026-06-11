package worldcupbraket.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import worldcupbraket.domain.*;
import worldcupbraket.model.*;

@Service
public class WorldCupBraketService {
    MatchRepository matchRepository;
    UserRepository userRepository;
    PredictionRepository predictionRepository;
    MatchResultRepository matchResultRepository;

    WorldCupBraket worldCupBraket;

    public WorldCupBraketService(
            MatchRepository matchRepository,
            UserRepository userRepository,
            PredictionRepository predictionRepository,
            MatchResultRepository matchResultRepository
    ){
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
        this.predictionRepository = predictionRepository;
        this.matchResultRepository = matchResultRepository;

        worldCupBraket = new WorldCupBraket();
        this.addAllPlayers();
        this.addAllPredictions();
        this.addAllMatchResults();
    }

    public WorldCupBraket getInstance() {
        worldCupBraket = new WorldCupBraket();
        this.addAllPlayers();
        this.addAllPredictions();
        this.addAllMatchResults();
        return  worldCupBraket;
    }

    @Transactional
    public WorldCupBraket addPrediction(
            String username,
            String date,
            String time,
            String team1,
            String team2,
            int score1,
            int score2

    ) {
        User user = userRepository.findFirstByName(username);
        MatchModel matchModel =
                matchRepository.findFirstByDateAndTimeAndTeam1AndTeam2(
                        date,
                        time,
                        team1,
                        team2
                );
        if (user == null) {
            throw new IllegalStateException("User not found: " + username);
        }

        if (matchModel == null) {
            throw new IllegalStateException("Match not found");
        }
        PredictionModel predictionModel = new PredictionModel(
                matchModel,
                score1,
                score2
        );
        user.addPrediction(predictionModel);
        userRepository.saveAndFlush(user);
        predictionRepository.saveAndFlush(predictionModel);
        return worldCupBraket;
    }

    private void addAllPlayers(){
        userRepository.findAll().forEach(user -> {
            worldCupBraket.addPlayer(
                new Player(
                    user.getName()
                )
            );
        });
    }

    private void addAllPredictions() {
        userRepository.findAll().forEach(user -> {
            Player player = worldCupBraket.getPlayer(user.getName());
            user.getAllPredictions().forEach(predictionModel -> {
                MatchModel matchModel = predictionModel.getMatch();

                Prediction prediction = new Prediction(
                        getMatchFrom(matchModel),
                        predictionModel.score1,
                        predictionModel.score2
                );

                worldCupBraket.predictMatch(prediction, player);
            });
        });
    }

    private void addAllMatchResults() {
        matchResultRepository.findAll().forEach(matchResultModel -> {
            Result result = new Result(
                    getMatchFrom(matchResultModel.getMatch()),
                    matchResultModel.score1,
                    matchResultModel.score2
            );
            worldCupBraket.recordMatchResult(result);
        });
    }

    private Match getMatchFrom(MatchModel fromModel) {
        return worldCupBraket.getMatches()
            .stream()
            .filter(m ->
                    m.date().equals(fromModel.getDate()) &&
                            m.time().equals(fromModel.getTime()) &&
                            m.team1().equals(fromModel.getTeam1()) &&
                            m.team2().equals(fromModel.getTeam2())
            )
            .findFirst()
            .orElse(null);
    }
}
