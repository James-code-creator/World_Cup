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
import java.util.ArrayList;
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
        this.checkIfMatchHasStarted();
    }

    public List<LiveMatchResult> get() throws IOException, InterruptedException {
        String body = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        ObjectMapper mapper = new ObjectMapper();

        List<LiveMatchResult> matches = mapper.readValue(body, new TypeReference<>() {});
        return matches.stream()
            .filter(liveMatchResult ->
                Objects.equals(
                    liveMatchResult
                        .contestInfo()
                        .contestSeason()
                        .id(),
                    footballEventId
                ) &&
                Objects.equals(liveMatchResult.state(), "Live")
            ).toList();
    }

    public boolean hasLiveMatch() {
        return hasLiveMatch;
    }

    public List<Match> getLiveMatches() throws IOException, InterruptedException {
        List<Match> liveMatches = new ArrayList<>();
        if (hasLiveMatch) {
            List<LiveMatchResult> liveResults = get();
            List<Match> startedMatches = getStartedMatches();
            int count = Math.min(startedMatches.size(), liveResults.size());
            for (int i = 0; i < count; i++) {
                Match match = startedMatches.get(startedMatches.size() - count + i);
                liveMatches.add(match);
            }
        }
        return liveMatches;
    }

    @Scheduled(cron = "0 0-5 18-23,0-9 * * *")
    public void checkIfMatchHasStarted() throws IOException, InterruptedException {
        hasLiveMatch = this.get().stream().findFirst().isPresent();
    }

    @Scheduled(fixedDelay = 60000)
    public void updateLiveMatchResults() {
        if (!hasLiveMatch) {
            return;
        }

        try {
            List<LiveMatchResult> liveResults = get();

            if (liveResults.isEmpty()) {
                hasLiveMatch = false;
                return;
            }

            List<Match> startedMatches = getStartedMatches();

            addLiveResults(startedMatches, liveResults);

        } catch (InterruptedException | IOException _) {}
    }

    private List<Match> getStartedMatches() throws IOException, InterruptedException {
        return Matches.loadOverNet()
                .matches()
                .stream()
                .filter(Match::hasStarted)
                .sorted(Comparator.comparing(Match::getStartTime))
                .toList();
    }

    private void addLiveResults(List<Match> startedMatches, List<LiveMatchResult> liveResults) {
        int count = Math.min(startedMatches.size(), liveResults.size());

        for (int i = 0; i < count; i++) {
            Match match = startedMatches.get(startedMatches.size() - count + i);
            LiveMatchResult liveResult = liveResults.get(i);

            addLiveResult(match, liveResult);
        }
    }

    private void addLiveResult(Match match, LiveMatchResult liveResult) {
        worldCupBraketService.addResult(
                match.date(),
                match.time(),
                match.team1(),
                match.team2(),
                liveResult.competitor1().results().main(),
                liveResult.competitor2().results().main()
        );
    }
}
