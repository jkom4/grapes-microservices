package grapes.microservices.salesservice.controllers;

import grapes.microservices.salesservice.models.Family;
import grapes.microservices.salesservice.repositories.FamilyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clm/families")
public class FamilyController {

    @Autowired
    private FamilyRepository familyRepository;

    @PostMapping
    public ResponseEntity<?> createFamily(@RequestBody Family family) {
        try {
            Family saved = familyRepository.save(family);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to create family: " + e.getMessage());
        }
    }
}
