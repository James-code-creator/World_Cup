package worldcupbraket.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import worldcupbraket.domain.*;
import worldcupbraket.domain.livematch.LiveMatch;
import worldcupbraket.model.*;

import java.io.IOException;

@Service
public class WorldCupBraketService {
    MatchRepository matchRepository;
    UserRepository userRepository;
    PredictionRepository predictionRepository;
    MatchResultRepository matchResultRepository;
    LiveMatchService liveMatchService;

    WorldCupBraket worldCupBraket;

    public WorldCupBraketService(
            MatchRepository matchRepository,
            UserRepository userRepository,
            PredictionRepository predictionRepository,
            MatchResultRepository matchResultRepository,
            LiveMatchService liveMatchService
    ){
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
        this.predictionRepository = predictionRepository;
        this.matchResultRepository = matchResultRepository;
        this.liveMatchService = liveMatchService;

        worldCupBraket = new WorldCupBraket();
        this.addAllPlayers();
        this.addAllPredictions();
        this.downloadLatestMatchResults();
        this.addAllMatchResults();
        this.updateLiveMatch();
    }

    public WorldCupBraket getInstance() {
        worldCupBraket = new WorldCupBraket();
        this.addAllPlayers();
        this.addAllPredictions();
        this.downloadLatestMatchResults();
        this.addAllMatchResults();
        this.updateLiveMatch();
        return worldCupBraket;
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

        Match matchDomain = getMatchFrom(matchModel);
        if (matchDomain != null && matchDomain.hasStarted()) {
            return worldCupBraket;
        }

        MatchResultModel resultModel = matchResultRepository.findFirstByMatch(matchModel);

        if (resultModel != null) {
            return worldCupBraket;
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

    @Transactional
    public WorldCupBraket addResult(
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

    private void downloadLatestMatchResults() {
        worldCupBraket.getMatches().forEach(match -> {
            MatchModel matchModel =
                    matchRepository.findFirstByDateAndTimeAndTeam1AndTeam2(
                            match.date(),
                            match.time(),
                            match.team1(),
                            match.team2()
                    );
            if (matchModel == null) {
                return;
            }
            if (match.score() != null && match.score().ft() != null) {
                MatchResultModel resultModel =
                        matchResultRepository.findFirstByMatch(matchModel);

                if (resultModel == null) {
                    resultModel = new MatchResultModel(
                            matchModel,
                            match.score().ft().getFirst(),
                            match.score().ft().getLast()
                    );
                } else {
                    resultModel.score1 = match.score().ft().getFirst();
                    resultModel.score2 = match.score().ft().getLast();
                }
                matchResultRepository.save(resultModel);
            }
        });
    }

    private void updateLiveMatch() {
        try {
            LiveMatch liveMatch = liveMatchService.getCurrentOrCached();
            if (liveMatch != null) {
                Match latest = worldCupBraket.getMatches().stream().filter(
                        Match::hasStarted
                ).toList().getLast();
                Result liveResult  = new Result(
                    latest,
                    liveMatch.competitor1().results().main(),
                    liveMatch.competitor2().results().main()
                );
                worldCupBraket.recordMatchResult(liveResult);
            }
        } catch (InterruptedException | IOException _) {}
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
