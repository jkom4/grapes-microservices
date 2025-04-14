package grapes.microservices.authservice.masiid;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/masiid")
@RequiredArgsConstructor
public class MasiIdController {

    private final MasiIdService masiIdService;

    @PostMapping("/register")
    public MasiIdResponseDto register(@RequestBody MasiIdRequestDto request) {
        return masiIdService.registerUser(request);
    }

    @GetMapping("/all")
    public List<MasiIdResponseDto> getAllUsers() {
        return masiIdService.getAllUsers();
    }

}

