package worldcupbraket.domain;

import tools.jackson.databind.ObjectMapper;

import java.util.Comparator;
import java.util.List;

public record Matches(
        String name,
        List<Match> matches
) {
    public static Matches load() {
        ObjectMapper mapper = new ObjectMapper();
        Matches matches = mapper.readValue(
                Matches.class
                        .getClassLoader()
                        .getResourceAsStream("worldcup.json"),
                Matches.class
        );
        matches.sortMatches();
        return matches;
    }

    private void sortMatches() {
        matches.sort(Comparator.comparing(Match::date));
    }
}
