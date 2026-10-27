package com.example.smarthome.controller;

import com.example.smarthome.dto.RegistrationDto;
import com.example.smarthome.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    private boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }

    @GetMapping("/login")
    public String login() {
        if (isAuthenticated()) {
            return "redirect:/";
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        if (isAuthenticated()) {
            return "redirect:/";
        }
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new RegistrationDto());
        }
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") RegistrationDto registrationDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (isAuthenticated()) {
            return "redirect:/";
        }

        if (registrationDto.getUsername() != null &&
                userService.usernameExists(registrationDto.getUsername().trim())) {
            bindingResult.rejectValue("username", "duplicate.user.username",
                    "A user with this username already exists.");
        }

        if (registrationDto.getPassword() != null && registrationDto.getConfirmPassword() != null
                && !registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "match.user.confirmPassword",
                    "Passwords do not match.");
        }

        if (bindingResult.hasErrors()) {
            // Keep user object and return register view so errors are displayed with input retained
            return "register";
        }

        try {
            userService.registerNewUser(registrationDto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Account created successfully! Please log in.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            bindingResult.reject("registration.error", e.getMessage());
            return "register";
        } catch (Exception e) {
            bindingResult.reject("registration.error", "An unexpected error occurred during registration. Please try again.");
            return "register";
        }
    }
}
