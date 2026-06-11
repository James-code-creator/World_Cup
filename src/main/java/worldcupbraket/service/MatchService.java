package worldcupbraket.service;

import org.springframework.stereotype.Service;
import worldcupbraket.domain.Match;
import worldcupbraket.domain.Matches;
import worldcupbraket.model.MatchModel;
import worldcupbraket.model.MatchRepository;

import java.util.List;

@Service
public class MatchService {
    public MatchService(
            MatchRepository matchRepository
    ) {
        List<Match> matches = Matches.load().matches();
        matchRepository.deleteAll();
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
            matchRepository.save(model);
            id++;
        }
    }
}
