package worldcupbraket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Controller
public class WebController {
    @Value("${spring.application.name}")
    private String appName;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;


    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("nameOfApplication", appName);
        return "index";
    }

    @PostMapping("/")
    public String login(
        @RequestParam String username,
        @RequestParam String password,
        HttpServletRequest request
    ) {
       if (userService.authenticate(username, password)) {

            Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
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
           return "error";
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
    public String getMatches(Model model) {
        Tournament tournament = Tournament.load();
        model.addAttribute("matches", tournament.matches());
        return "matches";
    }
}
