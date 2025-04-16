package grapes.microservices.salesservice.controllers;

import grapes.microservices.salesservice.dto.ArticleDTO;
import grapes.microservices.salesservice.mapper.ArticleMapper;
import grapes.microservices.salesservice.models.Article;
import grapes.microservices.salesservice.services.ArticleService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping(value = "/clm/articles", produces = MediaType.APPLICATION_JSON_VALUE)
public class ArticleController {

    private final ArticleService articleService;
    private final ArticleMapper articleMapper = ArticleMapper.INSTANCE;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    /**
     * Retrieves all articles without pagination (admin only).
     *  // @PreAuthorize("hasRole('ADMIN')")
     */
    @GetMapping
    public ResponseEntity<?> getAllArticles() {
        try {
            List<Article> articles = articleService.getAllArticles();
            List<ArticleDTO> dtos = articles.stream()
                    .map(articleMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while retrieving all articles.");
        }
    }

    /**
     * Retrieves all available (in-stock) articles with pagination.
     * This is typically used by client-facing apps.
     * // @PreAuthorize("hasRole('USER')") // TODO: activate when security is in place
     */
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableArticles(@ParameterObject Pageable pageable) {
        try {
            Page<Article> available = articleService.getAvailableArticles(pageable);
            Page<ArticleDTO> dtoPage = available.map(articleMapper::toDTO);
            return ResponseEntity.ok(dtoPage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while retrieving available articles.");
        }
    }

    /**
     * Searches articles by name (partial match).
     */


    @GetMapping("/search")
    public ResponseEntity<?> searchArticlesByName(@RequestParam String name) {
        try {
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("The 'name' field cannot be empty.");
            }

            List<Article> results = articleService.searchByName(name);
            if (results.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No items found with the name: '" + name + "'");
            }


            List<ArticleDTO> dtos = results.stream()
                    .map(articleMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while searching articles.");
        }
    }


    /**
     * Creates a new article (admin only).
     *  // @PreAuthorize("hasRole('ADMIN')")
     */
    @PostMapping
    public ResponseEntity<?> createArticle(@Valid @RequestBody ArticleDTO articleDTO) {
        try {
            Article article = articleMapper.toEntity(articleDTO);
            Article created = articleService.createArticle(article);
            return ResponseEntity.status(HttpStatus.CREATED).body(articleMapper.toDTO(created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    /**
     * Updates an existing article by ID (admin only).
     *  // @PreAuthorize("hasRole('ADMIN')")
     */
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateArticle(
            @PathVariable Integer id,
            @Valid @RequestBody ArticleDTO articleDTO) {
        try {
            Article entity = articleMapper.toEntity(articleDTO);
            Article updated = articleService.updateArticle(id, entity);
            return ResponseEntity.ok(articleMapper.toDTO(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleDTO> getArticleById(@PathVariable Integer id) {
        Article article = articleService.getArticleById(id);
        return ResponseEntity.ok(articleMapper.toDTO(article));
    }



}
