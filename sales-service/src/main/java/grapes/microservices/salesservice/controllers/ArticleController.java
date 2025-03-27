package grapes.microservices.salesservice.controllers;

import grapes.microservices.salesservice.dto.ArticleDTO;
import grapes.microservices.salesservice.services.ArticleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/articles", produces = MediaType.APPLICATION_JSON_VALUE)
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    // Endpoint pour obtenir tous les articles
    @GetMapping
    public ResponseEntity<List<ArticleDTO>> getAllArticles() {
        return ResponseEntity.ok(articleService.getAllArticles());
    }

    // ✅ Endpoint de recherche d'articles par nom avec gestion d'erreurs
    @GetMapping("/search")
    public ResponseEntity<?> searchArticlesByName(@RequestParam String name) {
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Le champ 'name' ne peut pas être vide.");
        }

        List<ArticleDTO> results = articleService.searchByName(name);

        if (results.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Aucun article trouvé avec le nom : '" + name + "'");
        }

        return ResponseEntity.ok(
                Map.of(
                        "message", "Article(s) trouvé(s) avec succès.",
                        "data", results
                )
        );
    }

    // Endpoint pour créer un article
    @PostMapping
    public ResponseEntity<?> createArticle(@Valid @RequestBody ArticleDTO articleDTO) {
        ArticleDTO createdArticle = articleService.createArticle(articleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "message", "Article ajouté avec succès.",
                        "data", createdArticle
                )
        );
    }


    // ✅ Modification d'un article avec gestion d'erreurs et message de succès
   // @PreAuthorize("hasRole('ADMIN')") A ACTIVER SI ON A LE ROLE
    @PutMapping("/{id}")
    public ResponseEntity<?> updateArticle(
            @PathVariable Integer id,
            @Valid @RequestBody ArticleDTO articleDTO) {

        try {
            ArticleDTO updatedArticle = articleService.updateArticle(id, articleDTO);
            return ResponseEntity.ok(
                    Map.of(
                            "message", "Article mis à jour avec succès.",
                            "data", updatedArticle
                    )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erreur de validation : " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Erreur : " + e.getMessage());
        }
    }
}
