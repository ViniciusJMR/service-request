package dev.viniciusjmr.servicerequest.domain.service;

import dev.viniciusjmr.servicerequest.domain.exception.FieldException;
import dev.viniciusjmr.servicerequest.domain.exception.InvalidOperation;
import dev.viniciusjmr.servicerequest.domain.exception.ResourceAlreadyExistsException;
import dev.viniciusjmr.servicerequest.domain.model.Role;
import dev.viniciusjmr.servicerequest.domain.model.User;
import dev.viniciusjmr.servicerequest.domain.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createClient(String name, String email, String password) {
        return createUser(name, email, password, Role.CLIENT);
    }

    public User createUser(
            String name,
            String email,
            String password,
            Role role
    ) {

        if(userRepository.findByEmail(email).isPresent())
            throw new ResourceAlreadyExistsException("User with this e-mail already exists");

        var user = new User(
                name,
                email,
                passwordEncoder.encode(password),
                role,
                true
        );

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new InvalidOperation(
                    String.format("Error saving user: %s", e.getMessage())
            );
        }
    }
}
