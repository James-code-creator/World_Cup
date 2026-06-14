package worldcupbraket;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import worldcupbraket.domain.*;
import worldcupbraket.service.PredictionStatsCalculatorService;
import worldcupbraket.service.UserService;
import worldcupbraket.service.WorldCupBraketService;

import java.util.*;

import static worldcupbraket.service.PredictionStatsCalculatorService.calculateOutcome;

@Controller
public class WebController {
    @Value("${spring.application.name}")
    private String appName;

    private final UserService userService;

    private final WorldCupBraketService worldCupBraketService;

    public WebController(
            UserService userService,
            WorldCupBraketService worldCupBraketService
    ) {
        this.userService = userService;
        this.worldCupBraketService = worldCupBraketService;
    }

    @GetMapping("/")
    public String index(Model model, Authentication authentication) {

        if (authentication != null &&
                authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/matches";
        }

        model.addAttribute("nameOfApplication", appName);
        return "index";
    }

    @GetMapping("/login")
    public String getLogin() {
        return "redirect:/";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request
    ) {
        if (userService.authenticate(username, password)) {

            List<SimpleGrantedAuthority> authorities = userService.isAdmin(username)
                    ? List.of(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_ADMIN"))
                    : List.of(new SimpleGrantedAuthority("ROLE_USER"));

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            authorities
                    );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            HttpSession session = request.getSession(true);
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    context
            );

            return "redirect:/matches";
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @PostMapping("/signup")
    public String createUser(
            @RequestParam String username,
            @RequestParam String password
    ) {
        System.out.println("Creating user: " + username);
        userService.createUser(username, password);
        return "redirect:/";
    }

    @GetMapping("/matches")
    public String getMatches(
            Authentication authentication,
            Model model
    ) {
        String username = authentication.getName();
        Map<Match, Result> results = worldCupBraketService.getInstance().getResults();
        Map<Match, Prediction> predictions = worldCupBraketService.getInstance().getPlayer(username).getPredictions();
        Map<Match, List<Prediction>> allPredictions = worldCupBraketService.getInstance().getAllPredictions();
        List<Match> allMatches = worldCupBraketService.getInstance().getMatches();
        List<Match> notStarted = allMatches.stream()
                .filter(m -> !m.hasStarted())
                .toList();

        List<Match> inProgress = allMatches.stream()
                .filter(m -> m.hasStarted() && !results.containsKey(m))
                .toList();

        List<Match> matches = new ArrayList<>();

        matches.addAll(inProgress);
        matches.addAll(notStarted);

        Map<Match, PredictionStats> predictionStats = new HashMap<>();

        allPredictions.forEach((match, pred) ->
                predictionStats.put(match, calculateOutcome(pred)));

        model.addAttribute("predictionStats", predictionStats);

        model.addAttribute("matches", matches);
        model.addAttribute("predictions", predictions);
        model.addAttribute("predictionStats", predictionStats);
        model.addAttribute("results", results);
        return "matches";
    }

    @PostMapping("/matches")
    public String savePredictions(
            @RequestParam String team1,
            @RequestParam String team2,
            @RequestParam String date,
            @RequestParam String time,
            @RequestParam int score1,
            @RequestParam int score2,
            Authentication authentication,
            Model model
    ) {
        String username = authentication.getName();

        WorldCupBraket braket = worldCupBraketService.addPrediction(
                username,
                date,
                time,
                team1,
                team2,
                score1,
                score2
        );

        Map<Match, Prediction> predictions = worldCupBraketService.getInstance().getPlayer(username).getPredictions();
        List<Match> matches = braket.getMatches();
        Map<Match, Result> results = worldCupBraketService.getInstance().getResults();

        model.addAttribute("matches", matches);
        model.addAttribute("predictions", predictions);
        model.addAttribute("results", results);

        return "matches";
    }

    @GetMapping("/scoreboard")
    public String getScoreboard(Model model) {
        List<Player> players = worldCupBraketService.getInstance().getScoreboard();
        players.sort(Comparator.comparing(Player::getPoints).reversed());
        model.addAttribute("players", players);
        return "scoreboard";
    }

    @GetMapping("/results")
    public String getResults(
            Authentication authentication,
            Model model
    ) {
        String username = authentication.getName();

        Map<Match, Prediction> predictions = worldCupBraketService.getInstance().getPlayer(username).getPredictions();
        Map<Match, Result> results = worldCupBraketService.getInstance().getResults();
        List<Match> matches = worldCupBraketService.getInstance().getMatches().stream()
                .filter(Match::hasStarted).toList().reversed();
        model.addAttribute("isAdmin", userService.isAdmin(username));
        model.addAttribute("matches", matches);
        model.addAttribute("results", results);
        model.addAttribute("predictions", predictions);
        return "results";
    }

    @PostMapping("/results")
    public String saveResults(
            @RequestParam String team1,
            @RequestParam String team2,
            @RequestParam String date,
            @RequestParam String time,
            @RequestParam int score1,
            @RequestParam int score2,
            Model model,
            Authentication authentication
    ) {

        WorldCupBraket braket = worldCupBraketService.addResult(
                date,
                time,
                team1,
                team2,
                score1,
                score2
        );

        Map<Match, Result> results = worldCupBraketService.getInstance().getResults();
        List<Match> matches = braket.getMatches();
        String username = authentication.getName();
        model.addAttribute("isAdmin", userService.isAdmin(username));
        model.addAttribute("matches", matches);
        model.addAttribute("results", results);

        return "results";
    }
}
