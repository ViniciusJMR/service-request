package dev.viniciusjmr.servicerequest.api.controller;

import dev.viniciusjmr.servicerequest.api.model.login.LoginRequest;
import dev.viniciusjmr.servicerequest.api.model.login.LoginResponse;
import dev.viniciusjmr.servicerequest.api.model.user.RegisterClientRequest;
import dev.viniciusjmr.servicerequest.api.model.user.RegisterClientResponse;
import dev.viniciusjmr.servicerequest.auth.service.AuthService;
import dev.viniciusjmr.servicerequest.domain.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Auth",
        description = "Autenticacao e cadastro de clientes"
)
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @Operation(
            description = "Cria um novo usuario com role CLIENT."
    )
    @PostMapping("/register")
    public ResponseEntity<RegisterClientResponse> registerClient(@RequestBody RegisterClientRequest client) {
        var user = userService.createClient(client.name(), client.email(), client.password());
        var response = RegisterClientResponse.from(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            description = "Autentica um usuario e retorna um token JWT para acessar endpoints protegidos."
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest body) {
        var token = authService.login(body.email(), body.password());

        var response = new LoginResponse(token);

        return ResponseEntity.ok(response);
    }
}
