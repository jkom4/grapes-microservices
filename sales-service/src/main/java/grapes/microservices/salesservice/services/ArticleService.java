package grapes.microservices.salesservice.services;

import grapes.microservices.salesservice.dto.ArticleDTO;
import grapes.microservices.salesservice.mapper.ArticleMapper;
import grapes.microservices.salesservice.models.Article;
import grapes.microservices.salesservice.repositories.ArticleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper = ArticleMapper.INSTANCE;

    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public ArticleDTO createArticle(ArticleDTO articleDTO) {

        if (articleDTO.getName() == null || articleDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de l'article est obligatoire.");
        }

        if (articleRepository.existsByNameIgnoreCase(articleDTO.getName())) {
            throw new IllegalArgumentException("Un article avec ce nom existe déjà.");
        }

        // Vérifier stock > 0 → prix obligatoire
        if (stockPositive(articleDTO.getStockKg()) &&
                (articleDTO.getPriceKg() == null || articleDTO.getPriceKg().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException("Un article avec un stock au kg doit avoir un prix au kg.");
        }

        if (stockPositive(articleDTO.getStockUnit()) &&
                (articleDTO.getPriceUnit() == null || articleDTO.getPriceUnit().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException("Un article avec un stock à l'unité doit avoir un prix à l'unité.");
        }

        // Prix = 0
        if (isZero(articleDTO.getPriceKg()) || isZero(articleDTO.getPriceUnit())) {
            throw new IllegalArgumentException("Le prix ne peut pas être nul.");
        }

        // Stock = 0
        if (isZero(articleDTO.getStockKg()) && isZero(articleDTO.getStockUnit())) {
            throw new IllegalArgumentException("Rupture de stock : le stock est à zéro !");
        }

        // Image
        if (articleDTO.getPicturePath() != null &&
                !articleDTO.getPicturePath().matches(".*\\.(jpg|jpeg|png|webp)$")) {
            throw new IllegalArgumentException("Le format de l'image doit être .jpg, .jpeg, .png ou .webp");
        }

        // Origine trop courte
        if (articleDTO.getOrigin() != null && articleDTO.getOrigin().length() < 2) {
            throw new IllegalArgumentException("Le champ 'origin' est trop court.");
        }

        Article article = articleMapper.toEntity(articleDTO);
        Article saved = articleRepository.save(article);
        return articleMapper.toDTO(saved);
    }

    public ArticleDTO updateArticle(Integer id, ArticleDTO articleDTO) {
        Article existingArticle = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé avec l'id : " + id));

        if (articleDTO.getName() != null && articleDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de l'article ne peut pas être vide.");
        }

        if (articleDTO.getPriceKg() != null && articleDTO.getPriceKg().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le prix au kg doit être strictement positif.");
        }

        if (articleDTO.getPriceUnit() != null && articleDTO.getPriceUnit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le prix à l'unité doit être strictement positif.");
        }

        if (articleDTO.getStockKg() != null && articleDTO.getStockKg().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le stock au kg ne peut pas être négatif.");
        }

        if (articleDTO.getStockUnit() != null && articleDTO.getStockUnit().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le stock à l'unité ne peut pas être négatif.");
        }

        // Stock > 0 → prix obligatoire
        if (stockPositive(articleDTO.getStockKg()) &&
                (articleDTO.getPriceKg() == null || articleDTO.getPriceKg().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException("Un article avec un stock au kg doit avoir un prix au kg.");
        }

        if (stockPositive(articleDTO.getStockUnit()) &&
                (articleDTO.getPriceUnit() == null || articleDTO.getPriceUnit().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException("Un article avec un stock à l'unité doit avoir un prix à l'unité.");
        }

        // Stock = 0
        if (isZero(articleDTO.getStockKg()) && isZero(articleDTO.getStockUnit())) {
            throw new IllegalArgumentException("Rupture de stock : le stock est à zéro !");
        }

        // Image
        if (articleDTO.getPicturePath() != null &&
                !articleDTO.getPicturePath().matches(".*\\.(jpg|jpeg|png|webp)$")) {
            throw new IllegalArgumentException("Le format de l'image doit être .jpg, .jpeg, .png ou .webp");
        }

        if (articleDTO.getOrigin() != null && articleDTO.getOrigin().length() < 2) {
            throw new IllegalArgumentException("Le champ 'origin' est trop court.");
        }

        // MAJ des champs
        if (articleDTO.getName() != null) existingArticle.setName(articleDTO.getName());
        if (articleDTO.getPriceKg() != null) existingArticle.setPriceKg(articleDTO.getPriceKg());
        if (articleDTO.getPriceUnit() != null) existingArticle.setPriceUnit(articleDTO.getPriceUnit());
        if (articleDTO.getStockKg() != null) existingArticle.setStockKg(articleDTO.getStockKg());
        if (articleDTO.getStockUnit() != null) existingArticle.setStockUnit(articleDTO.getStockUnit());
        if (articleDTO.getDescription() != null) existingArticle.setDescription(articleDTO.getDescription());
        if (articleDTO.getOrigin() != null) existingArticle.setOrigin(articleDTO.getOrigin());
        if (articleDTO.getPicturePath() != null) existingArticle.setPicturePath(articleDTO.getPicturePath());

        Article updated = articleRepository.save(existingArticle);
        return articleMapper.toDTO(updated);
    }

    public List<ArticleDTO> getAllArticles() {
        return articleRepository.findAll()
                .stream()
                .map(articleMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ArticleDTO> searchByName(String name) {
        return articleRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(articleMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ➕ Méthodes utilitaires
    private boolean isZero(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) == 0;
    }

    private boolean stockPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }
}
