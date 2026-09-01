package dev.viniciusjmr.servicerequest.auth.service;

import dev.viniciusjmr.servicerequest.auth.jwt.JwtService;
import dev.viniciusjmr.servicerequest.auth.model.AuthenticatedUser;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    // Validates User and return Jwt token as string
    public String login(String email, String password) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        AuthenticatedUser authUser = (AuthenticatedUser) auth.getPrincipal();

        return jwtService.generateToken(authUser);

    }
}
