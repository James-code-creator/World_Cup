package worldcupbraket.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import worldcupbraket.domain.*;
import worldcupbraket.model.*;

import java.util.List;
import java.util.Optional;

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

        worldCupBraket = new WorldCupBraket(getMatches());
        this.addAllPlayers();
        this.addAllPredictions();
        this.addAllMatchResults();
    }

    public WorldCupBraket getInstance() {
        worldCupBraket = new WorldCupBraket(getMatches());
        this.addAllPlayers();
        this.addAllPredictions();
        this.addAllMatchResults();
        return worldCupBraket;
    }

    @Transactional
    public void addPrediction(
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

        Match matchDomain = getMatchFrom(matchModel);
        if (matchDomain != null && matchDomain.hasStarted()) {
            return;
        }

        MatchResultModel resultModel = matchResultRepository.findFirstByMatch(matchModel);

        if (resultModel != null) {
            return;
        }

        Optional<PredictionModel> existing =
                predictionRepository.findFirstByUser_IdAndMatch_IdOrderByIdDesc(
                        user.getId(),
                        matchModel.getId()
                ).stream().findFirst();

        PredictionModel prediction;

        if (existing.isPresent()) {
            prediction = existing.get();
            prediction.score1 = score1;
            prediction.score2 = score2;
        } else {
            prediction = new PredictionModel(
                    matchModel,
                    score1,
                    score2
            );
            prediction.setUser(user);
            predictionRepository.save(prediction);
        }
    }

    @Transactional
    public void addResult(
        String date,
        String time,
        String team1,
        String team2,
        int score1,
        int score2

    ) {
        MatchModel matchModel =
            matchRepository.findFirstByDateAndTimeAndTeam1AndTeam2(
                date,
                time,
                team1,
                team2
            );

        if (matchModel == null) {
            throw new IllegalStateException("Match not found");
        }

        MatchResultModel resultModel =
            matchResultRepository.findFirstByMatch(matchModel);

        if (resultModel == null) {
            resultModel = new MatchResultModel(
                matchModel,
                score1,
                score2
            );
        } else {
            resultModel.score1 = score1;
            resultModel.score2 = score2;
        }
        matchResultRepository.saveAndFlush(resultModel);
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

    private List<Match> getMatches() {
        return matchRepository.findAll().stream()
                .map(matchModel -> new Match(
                        matchModel.getRound(),
                        matchModel.getDate(),
                        matchModel.getTime(),
                        matchModel.getTeam1(),
                        matchModel.getTeam2(),
                        null,
                        matchModel.getGroup(),
                        matchModel.getGround()
                ))
                .toList();
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
