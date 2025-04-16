package grapes.microservices.salesservice.services;

import grapes.microservices.salesservice.exceptions.ResourceNotFoundException;
import grapes.microservices.salesservice.models.Article;
import grapes.microservices.salesservice.repositories.ArticleRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;

    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @CacheEvict(value = "articles", allEntries = true)
    public Article createArticle(Article article) {
        validateArticleForCreation(article);
        return articleRepository.save(article);
    }

    @CacheEvict(value = "articles", allEntries = true)
    public Article updateArticle(Integer id, Article updatedData) {
        if (!articleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Article not found with id: " + id);
        }
        validateArticleForUpdate(updatedData);
        updatedData.setId(id);
        return articleRepository.save(updatedData);
    }

    @Cacheable(value = "articles", key = "'all'")  // Explicit key for the entire list
    public List<Article> getAllArticles() {
        return articleRepository.findAll();
    }

    @Cacheable(value = "articles", key = "'search_' + #name")  // Cache by search name
    public List<Article> searchByName(String name) {
        return articleRepository.findByNameContainingIgnoreCase(name);
    }

    @Cacheable(value = "articles", key = "'available_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Article> getAvailableArticles(Pageable pageable) {
        return articleRepository.findByStockKgGreaterThanOrStockUnitGreaterThan(
                BigDecimal.ZERO, BigDecimal.ZERO, pageable);
    }

    @Cacheable(value = "articles", key = "#id")  // Cache by unique ID
    public Article getArticleById(Integer id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + id));
    }

    public void deleteArticleById(Integer id) {
        if (!articleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Article not found with ID: " + id);
        }
        articleRepository.deleteById(id);
    }

    private void validateArticleForCreation(Article article) {
        if (article.getName() == null || article.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Article name is required.");
        }
        if (articleRepository.existsByNameIgnoreCase(article.getName())) {
            throw new IllegalArgumentException("An article with this name already exists.");
        }
        validateCommonFields(article);
    }

    private void validateArticleForUpdate(Article article) {
        if (article.getName() != null && article.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Article name cannot be empty.");
        }
        validateCommonFields(article);
    }

    private void validateCommonFields(Article article) {
        if (stockPositive(article.getStockKg()) &&
                (article.getPriceKg() == null || article.getPriceKg().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException("Stock in kg requires a valid price.");
        }
        if (stockPositive(article.getStockUnit()) &&
                (article.getPriceUnit() == null || article.getPriceUnit().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException("Stock in unit requires a valid price.");
        }
        if (isZero(article.getPriceKg()) || isZero(article.getPriceUnit())) {
            throw new IllegalArgumentException("Price cannot be zero.");
        }
        if (isZero(article.getStockKg()) && isZero(article.getStockUnit())) {
            throw new IllegalArgumentException("Out of stock: both stock values are zero.");
        }
        if (article.getPicturePath() != null &&
                !article.getPicturePath().matches(".*\\.(jpg|jpeg|png|webp)$")) {
            throw new IllegalArgumentException("Image must be in .jpg, .jpeg, .png or .webp format.");
        }
        if (article.getOrigin() != null && article.getOrigin().length() < 2) {
            throw new IllegalArgumentException("The 'origin' field is too short.");
        }
    }

    private boolean isZero(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) == 0;
    }

    private boolean stockPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }
}
