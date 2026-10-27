package com.example.smarthome;

import com.example.smarthome.dto.RegistrationDto;
import com.example.smarthome.entity.AppUser;
import com.example.smarthome.repository.AppUserRepository;
import com.example.smarthome.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class SecurityAndAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void unauthenticatedAccessToDashboardRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/devices"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/rooms"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void loginAndRegisterPagesArePubliclyAccessible() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    void userRegistrationSavesHashedPasswordAndEnforcesValidation() throws Exception {
        RegistrationDto dto = new RegistrationDto();
        dto.setUsername("testuser");
        dto.setPassword("Secret123!");
        dto.setConfirmPassword("Secret123!");

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", dto.getUsername())
                        .param("password", dto.getPassword())
                        .param("confirmPassword", dto.getConfirmPassword()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("successMessage"));

        AppUser saved = userRepository.findByUsernameIgnoreCase("testuser").orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getPassword()).isNotEqualTo("Secret123!");
        assertThat(saved.getPassword()).startsWith("$2a$");
        assertThat(passwordEncoder.matches("Secret123!", saved.getPassword())).isTrue();

        // Duplicate registration check
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "testuser")
                        .param("password", "Different123!")
                        .param("confirmPassword", "Different123!"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("user", "username"));

        // Password mismatch check
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "newuser")
                        .param("password", "Secret123!")
                        .param("confirmPassword", "Mismatch123!"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("user", "confirmPassword"));
    }

    @Test
    void authenticationSuccessAndFailureFlow() throws Exception {
        RegistrationDto dto = new RegistrationDto();
        dto.setUsername("alice");
        dto.setPassword("password123");
        dto.setConfirmPassword("password123");
        userService.registerNewUser(dto);

        // Failed login
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "alice")
                        .param("password", "wrongpass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));

        // Successful login
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "alice")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @WithMockUser(username = "bob", roles = {"USER"})
    void authenticatedUserCanAccessDashboardAndLogout() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        mockMvc.perform(post("/logout")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"));
    }
}
