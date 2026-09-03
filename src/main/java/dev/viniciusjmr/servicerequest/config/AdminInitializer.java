package dev.viniciusjmr.servicerequest.config;

import dev.viniciusjmr.servicerequest.domain.model.Role;
import dev.viniciusjmr.servicerequest.domain.model.User;
import dev.viniciusjmr.servicerequest.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.initial-admin.name:}")
    private String adminName;

    @Value("${app.initial-admin.email:}")
    private String adminEmail;

    @Value("${app.initial-admin.password:}")
    private String adminPassword;

    public AdminInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            return;
        }

        boolean hasAdmin = userRepository.findByEmail(adminEmail).isPresent();

        if (hasAdmin) {
            return;
        }

        var admin = new User(
                adminName.isBlank() ? "Admin" : adminName,
                adminEmail,
                passwordEncoder.encode(adminPassword),
                Role.ADMIN,
                true
        );

        userRepository.save(admin);
    }
}
