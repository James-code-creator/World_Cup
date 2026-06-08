package worldcupbraket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    @Value("${spring.application.name}")
    private String appName;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("nameOfApplication", appName);
        return "index";
    }
}
