package grapes.microservices.salesservice.controllers;

import grapes.microservices.salesservice.models.Family;
import grapes.microservices.salesservice.repositories.FamilyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clm/families")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyRepository familyRepository;

    @PostMapping
    public ResponseEntity<?> createFamily(@RequestBody Family family) {
        try {
            Family saved = familyRepository.save(family);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to create family: " + e.getMessage());
        }
    }
    @GetMapping
    public ResponseEntity<List<Family>> getAllFamilies() {
        List<Family> families = familyRepository.findAll();
        return ResponseEntity.ok(families);
    }
}
