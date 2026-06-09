package worldcupbraket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class WebController {
    @Value("${spring.application.name}")
    private String appName;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("nameOfApplication", appName);
        return "index";
    }

    @PostMapping("/")
    public String login(
        @RequestParam String username,
        @RequestParam String password
    ) {
        if ("12345678".equals(password)) {
            return "redirect:/matches";
        } else {
            return "error";
        }
    }

    @GetMapping("/matches")
    public String getMatches(Model model) {
        Tournament tournament = Tournament.load();
        model.addAttribute("matches", tournament.matches());
        return "matches";
    }
}
