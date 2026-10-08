package br.com.tst.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthApiController {

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        if (!"12312312312".equals(request.email())
                || !"123".equals(request.senha())) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        return ResponseEntity.ok(
                new LoginResponse("token-simples-123")
        );
    }

    public record LoginRequest(
            @NotBlank String email,
            @NotBlank String senha
    ) {}

    public record LoginResponse(
            String access_token
    ) {}
}