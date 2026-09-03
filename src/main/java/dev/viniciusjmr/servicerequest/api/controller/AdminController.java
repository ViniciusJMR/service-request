package dev.viniciusjmr.servicerequest.api.controller;

import dev.viniciusjmr.servicerequest.api.model.user.AdminRegisterResponse;
import dev.viniciusjmr.servicerequest.api.model.user.AdminRegisterUserRequest;
import dev.viniciusjmr.servicerequest.domain.model.Role;
import dev.viniciusjmr.servicerequest.domain.service.AdminService;
import dev.viniciusjmr.servicerequest.infrastructure.audit.Audit;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Audit(action = "ADMIN_CREATE_USER")
    @PostMapping("/users")
    public ResponseEntity<AdminRegisterResponse> createUser(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody AdminRegisterUserRequest body
    ) {
        var analyst = adminService.createUser(
                body.name(),
                body.email(),
                body.password(),
                Role.valueOf(body.role()),
                body.states()
                );

        return ResponseEntity.status(HttpStatus.CREATED).body(AdminRegisterResponse.from(analyst));
    }
}
