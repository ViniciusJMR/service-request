package dev.viniciusjmr.servicerequest.auth.service;

import dev.viniciusjmr.servicerequest.auth.model.AuthenticatedUser;
import dev.viniciusjmr.servicerequest.domain.model.User;
import dev.viniciusjmr.servicerequest.domain.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));

        return new AuthenticatedUser(user);
    }
}
