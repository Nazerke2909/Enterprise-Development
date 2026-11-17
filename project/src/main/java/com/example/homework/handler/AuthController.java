package com.example.homework.handler;

import com.example.homework.config.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authentication;
    private final TokenService tokens;

    public AuthController(AuthenticationManager authentication, TokenService tokens) {
        this.authentication = authentication;
        this.tokens = tokens;
    }

    @Operation(summary = "Log in and get a 15-minute bearer token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token issued",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "VALIDATION",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "UNAUTHENTICATED: bad username or password",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication result = authentication.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        return new LoginResponse(tokens.issue(result), "Bearer", tokens.expiresInSeconds());
    }
}
