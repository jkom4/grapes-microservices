package grapes.microservices.salesservice.mapper;

import grapes.microservices.salesservice.dto.ArticleDTO;
import grapes.microservices.salesservice.models.Article;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ArticleMapper {
    ArticleMapper INSTANCE = Mappers.getMapper(ArticleMapper.class);


    Article toEntity(ArticleDTO articleDTO);

    ArticleDTO toDTO(Article article);
}