package worldcupbraket.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import worldcupbraket.domain.Match;
import worldcupbraket.domain.Matches;
import worldcupbraket.domain.livematch.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class LiveMatchService {
    public static String footballEventId = "5193";
    HttpClient client = HttpClient.newHttpClient();
    String url = "https://sport.api.swisstxt.ch/v1/live_events?lang=de";
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .timeout(Duration.ofSeconds(5))
            .build();
    WorldCupBraketService worldCupBraketService;
    volatile boolean hasLiveMatch = false;


    public LiveMatchService(WorldCupBraketService worldCupBraketService) throws IOException, InterruptedException {
        this.worldCupBraketService = worldCupBraketService;
        this.refreshLiveStatus();
    }

    public List<LiveMatch> get() throws IOException, InterruptedException {
        String body = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        ObjectMapper mapper = new ObjectMapper();

        List<LiveMatch> matches = mapper.readValue(body, new TypeReference<>() {});
        return matches.stream()
            .filter(liveMatch ->
                Objects.equals(
                    liveMatch
                        .contestInfo()
                        .contestSeason()
                        .id(),
                    footballEventId
                ) &&
                Objects.equals(liveMatch.state(), "Live")
            ).toList();
    }

    public boolean hasLiveMatch() {
        return hasLiveMatch;
    }

    @Scheduled(cron = "0 */5 18-23,0-9 * * *")
    public void refreshLiveStatus() throws IOException, InterruptedException {
        hasLiveMatch = this.get().stream().findFirst().isPresent();
    }

    @Scheduled(fixedDelay = 60000)
    public void updateLiveMatch() {
        if (!hasLiveMatch) {
            return;
        }
        try {
            LiveMatch liveMatch = this.get().stream()
                .findFirst()
                .orElse(null);
            if (liveMatch != null) {
                Match latest = Matches.loadOverNet().matches().stream()
                    .filter(Match::hasStarted)
                    .sorted(Comparator.comparing(Match::getStartTime))
                    .toList()
                    .getLast();
                worldCupBraketService.addResult(
                    latest.date(),
                    latest.time(),
                    latest.team1(),
                    latest.team2(),
                    liveMatch.competitor1().results().main(),
                    liveMatch.competitor2().results().main()
                );
            } else {
                hasLiveMatch = false;
            }
        } catch (InterruptedException | IOException _) {}
    }
}
