package dev.viniciusjmr.servicerequest.auth.jwt;

import dev.viniciusjmr.servicerequest.auth.model.AuthenticatedUser;
import dev.viniciusjmr.servicerequest.config.JwtConfig;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {
    private final JwtEncoder encoder;
    private final JwtConfig.JwtProperties jwtProperties;

    public JwtService(JwtEncoder encoder, JwtConfig.JwtProperties jwtProperties) {
        this.encoder = encoder;
        this.jwtProperties = jwtProperties;
    }

    public String generateToken(AuthenticatedUser authUser) {
        Instant now = Instant.now();

        var user = authUser.getUser();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(jwtProperties.accessTokenExpiration()))
                .claim("role", user.getRole().name())
                .build();

        return encoder
                .encode(JwtEncoderParameters.from(claims))
                .getTokenValue();
    }
}
