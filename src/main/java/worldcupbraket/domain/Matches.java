package worldcupbraket.domain;

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;

public record Matches(
        String name,
        List<Match> matches
) {
    private static final String URL =
        "https://raw.githubusercontent.com/openfootball/worldcup.json/refs/heads/master/2026/worldcup.json";

    public static Matches load() {
        ObjectMapper mapper = new ObjectMapper();
        Matches matches;

        try {
            matches = mapper.readValue(
                    getFromGithub(),
                    Matches.class
            );
            assert matches.matches.getFirst().score() != null;
            assert matches.matches.getFirst().score().ft().getFirst() == 2;

        } catch (IOException | InterruptedException | AssertionError e) {
            matches = mapper.readValue(
                    getFromResources(),
                    Matches.class
            );
        }
        matches.sortMatches();
        return matches;
    }

    private static String getFromGithub() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .GET()
                .timeout(Duration.ofSeconds(1))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    private static InputStream getFromResources() {
        return Matches
                .class
                .getClassLoader()
                .getResourceAsStream("worldcup.json");
    }

    private void sortMatches() {
        matches.sort(Comparator.comparing(Match::getStartTime));
    }
}
