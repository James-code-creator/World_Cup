package worldcupbraket.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import worldcupbraket.domain.Match;
import worldcupbraket.domain.Matches;
import worldcupbraket.domain.Score;
import worldcupbraket.model.MatchModel;
import worldcupbraket.model.MatchRepository;
import worldcupbraket.model.MatchResultModel;
import worldcupbraket.model.MatchResultRepository;

import java.util.List;

@Service
public class MatchService {
    MatchRepository matchRepository;
    MatchResultRepository matchResultRepository;

    public MatchService(
            MatchRepository matchRepository,
            MatchResultRepository matchResultRepository
    ) {
        this.matchResultRepository = matchResultRepository;
        this.matchRepository = matchRepository;
        this.downloadLatestMatchResults();
        this.downloadLatestMatches();
    }

    //@Scheduled(cron = "0 0 9 * * *")
    public void downloadLatestMatches(){
        List<Match> matches = Matches.loadOverNet().matches();
        this.matchRepository.deleteAll();
        Long id = 1L;
        for (Match match : matches) {
            MatchModel model = new MatchModel(
                id,
                match.round(),
                match.date(),
                match.time(),
                match.team1(),
                match.team2(),
                match.group(),
                match.ground()
            );
            this.matchRepository.save(model);
            id++;
        }
    }

    //@Scheduled(cron = "0 0 * * * *")
    public void downloadLatestMatchResults() {
        Matches matches = Matches.loadOverNet();
        matches.matches().forEach(match -> {
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

                List<Integer> score = match.score().et() != null
                        ? match.score().et()
                        : match.score().ft();

                if (resultModel == null) {
                    resultModel = new MatchResultModel(
                        matchModel,
                        score.getFirst(),
                        score.getLast()
                    );
                } else {
                    resultModel.score1 = score.getFirst();
                    resultModel.score2 = score.getLast();
                }
                matchResultRepository.save(resultModel);
            }
        });
    }
}
