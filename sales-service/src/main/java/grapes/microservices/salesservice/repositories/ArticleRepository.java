package grapes.microservices.salesservice.repositories;

import grapes.microservices.salesservice.models.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Integer> {

    List<Article> findByCategoryId(Integer categoryId);
    List<Article> findByFamilyId(Integer familyId);
    boolean existsByNameIgnoreCase(String name);
    List<Article> findByNameContainingIgnoreCase(String name);

    //  pagination method
    Page<Article> findByStockKgGreaterThanOrStockUnitGreaterThan(BigDecimal minKg, BigDecimal minUnit, Pageable pageable);
}
