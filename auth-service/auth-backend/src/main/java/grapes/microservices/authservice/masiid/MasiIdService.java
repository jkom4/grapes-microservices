package grapes.microservices.authservice.masiid;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class MasiIdService {
    private final MasiidUserRepository masiidUserRepository;

    public MasiIdResponseDto registerUser(MasiIdRequestDto request) {
        // Map RequestDto to MasiIdUser
        MasiIdUser user = MasiIdUser.builder()
                .clientName(request.getClientName())
                .email(request.getEmail())
                .birthDate(request.getBirthDate())
                .nationalRegistryNumber(request.getNationalRegistryNumber())
                .build();

        MasiIdUser savedUser = masiidUserRepository.save(user);

        return new MasiIdResponseDto("Registration Successful");
    }
    public List<MasiIdResponseDto> getAllUsers() {
        List<MasiIdUser> users = masiidUserRepository.findAll();
        return users.stream()
                .map(user -> new MasiIdResponseDto("User: " + user.getClientName()))
                .toList();
    }

}