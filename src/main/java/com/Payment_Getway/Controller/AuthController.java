package com.Payment_Getway.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.Payment_Getway.Model.User;
import com.Payment_Getway.Service.UserService;

import org.springframework.ui.Model;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String registerPage(Model model) {

        model.addAttribute("user",
                new User());

        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @ModelAttribute User user,
            Model model
    ) {

        try {
            userService.registerUser(user);
        } catch (IllegalArgumentException exception) {
            user.setPassword(null);
            model.addAttribute("user", user);
            model.addAttribute("error", exception.getMessage());
            return "register";
        }

        return "redirect:/auth/login?registered";
    }

    @GetMapping("/login")
    public String loginPage() {

        return "login";
    }
}
