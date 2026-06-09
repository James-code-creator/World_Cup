package worldcupbraket;

import tools.jackson.databind.ObjectMapper;
import java.util.Comparator;
import java.util.List;

public record Tournament(
        String name,
        List<Match> matches
) {
    public static Tournament load() {
        ObjectMapper mapper = new ObjectMapper();
        Tournament tournament = mapper.readValue(
                Tournament.class
                        .getClassLoader()
                        .getResourceAsStream("worldcup.json"),
                Tournament.class
        );
        tournament.sortMatches();
        return tournament;
    }

    private void sortMatches() {
        matches.sort(Comparator.comparing(Match::date));
    }
}
