package worldcupbraket.service;

import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import worldcupbraket.domain.livematch.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Service
public class LiveMatchService {
    public static String footballEventId = "5193";
    HttpClient client = HttpClient.newHttpClient();
    private volatile LiveMatch cachedMatch;
    private volatile long cacheTime;

    private static final long CACHE_DURATION_MS = 120_000; // 2min

    public List<LiveMatch> get() throws IOException, InterruptedException {
        String url = "https://sport.api.swisstxt.ch/v1/live_events?lang=de";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();

        String body = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        ObjectMapper mapper = new ObjectMapper();

        List<LiveMatch> matches = mapper.readValue(
            body,
                new TypeReference<>() {}
        );
        return matches.stream()
            .filter(liveMatch ->
                Objects.equals(
                    liveMatch
                        .contestInfo()
                        .contestSeason()
                        .id(),
                    footballEventId
                )
            ).toList();
    }

    public LiveMatch getCurrent() throws IOException, InterruptedException {
        List<LiveMatch> allFootball = get();
        return allFootball.stream().filter(
            live -> Objects.equals(live.state(), "Live")
        ).findFirst().orElse(null);
    }

    public LiveMatch getCurrentOrCached() throws IOException, InterruptedException {
        long now = System.currentTimeMillis();

        if (cachedMatch != null && now - cacheTime < CACHE_DURATION_MS) {
            return cachedMatch;
        }

        synchronized (this) {
            if (cachedMatch != null &&
                    now - cacheTime < CACHE_DURATION_MS) {
                return cachedMatch;
            }

            LiveMatch matches = getCurrent();

            cachedMatch = matches;
            cacheTime = now;

            return matches;
        }
    }

}
