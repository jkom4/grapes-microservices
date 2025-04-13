package grapes.microservices.authservice.controllers;

import grapes.microservices.authservice.dto.EIDCardInfo;
import grapes.microservices.authservice.dto.UserDTO;
import grapes.microservices.authservice.mapper.UserMapper;
import grapes.microservices.authservice.models.User;
import grapes.microservices.authservice.models.Gender;
import grapes.microservices.authservice.services.UserService;
import grapes.microservices.authservice.services.eid.EIDCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/eid")
public class EIDController {

    @Autowired
    private EIDCardService eidCardService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/login")
    public ResponseEntity<?> loginWithEID() {
        try {
            EIDCardInfo cardInfo = eidCardService.readCard();
            UserDTO userDTO = userMapper.toDTOFromEID(cardInfo);

            userDTO.setEmail("eid.temp@user.com");
            userDTO.setPassword("TempPass123!");
            userDTO.setPhoneNumber("0490000000");
            userDTO.setGender(Gender.UNKNOWN);

            User user = userMapper.toEntity(userDTO);
            User savedUser = userService.registerUser(user);
            return ResponseEntity.ok(userMapper.toDTO(savedUser));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur eID : " + e.getMessage());
        }
    }
}