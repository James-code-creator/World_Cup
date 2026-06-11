package worldcupbraket.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<MatchModel, Long> {
    MatchModel findFirstByDateAndTimeAndTeam1AndTeam2(
            String date,
            String time,
            String team1,
            String team2
    );
}