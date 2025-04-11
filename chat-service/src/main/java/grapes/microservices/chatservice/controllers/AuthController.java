package grapes.microservices.chatservice.controllers;

import grapes.microservices.chatservice.dto.UserDto;
import grapes.microservices.chatservice.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/{token}")
    public UserDto validateToken(@PathVariable String token) {
        return authService.validateToken(token);
    }
}
