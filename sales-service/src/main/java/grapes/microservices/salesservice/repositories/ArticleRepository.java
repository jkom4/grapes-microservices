package grapes.microservices.salesservice.repositories;

import grapes.microservices.salesservice.models.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Integer> {

    List<Article> findByCategoryId(Integer categoryId);
    boolean existsByNameIgnoreCase(String name);

    List<Article> findByFamilyId(Integer familyId);

    List<Article> findByNameContainingIgnoreCase(String name); // partial search by name
}
