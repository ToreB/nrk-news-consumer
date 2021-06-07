package no.toreb.nrknewsconsumer.repository;

import no.toreb.nrknewsconsumer.model.Article;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface ArticleRepository extends CrudRepository<Article, Long> {

    @Query("select distinct article_id from article")
    List<String> distinctArticleIds();
}
