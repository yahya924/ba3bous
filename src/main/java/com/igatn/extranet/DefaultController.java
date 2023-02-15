package com.igatn.extranet;

import com.google.api.client.util.Lists;
import com.igatn.extranet.domainjpa.api.data.UserRepository;
import com.igatn.extranet.domainjpa.impl.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Set;

@Controller
public class DefaultController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String home() {
        return "default";
    }

    @PostMapping("/")
    public String homePost() {
        return "default";
    }

    @ResponseBody
    @GetMapping("/a")
    public String getEnpointB() {
        return "endpoint a!";
    }

    @GetMapping("/a/b")
    public String getEnpointC() {
        return "endpoint a/b!";
    }

    @GetMapping("/test/notifications")
    public String testNotifications(Model model) {
        
        List<User> users = Lists.newArrayList(userRepository.findAll());
        
        String premiumConfig[] = new String[] { "PR", "New premium to pay" };
        String reimbursementConfig[] = new String[] { "RM", "Reimbursement received" };
        String policyConfig[] = new String[] { "PL", "Renew your policy" };

        model.addAttribute("USERS", users);
        model.addAttribute("EVENT_TYPES", List.of(premiumConfig, reimbursementConfig, policyConfig));
        model.addAttribute("PARAMETERS", new Parameters("", ""));
        
        return "test-notifications";
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class Parameters {
        String username;
        String evtType;
    }
}
