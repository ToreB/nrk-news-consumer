package no.toreb.nrknewsconsumer.repository;

import no.toreb.nrknewsconsumer.model.Article;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface ArticleRepository extends CrudRepository<Article, Long> {

    @Query("select distinct article_id from article")
    List<String> distinctArticleIds();

    @Query("select * from article " +
           "where GEN_ID not in (select article from hidden_articles) " +
           "order by PUBLISHED_AT limit :limit offset :offset")
    List<Article> findAll(@Param("limit") long limit, @Param("offset") long offset);

    @Query("select * from article " +
           "where GEN_ID in (select article from hidden_articles) " +
           "order by PUBLISHED_AT desc limit :limit offset :offset")
    List<Article> findAllHidden(@Param("limit") long limit, @Param("offset") long offset);

    @Modifying
    @Query("insert into hidden_articles (article) " +
           "select GEN_ID from article where ARTICLE_ID = :articleId")
    void hideArticle(@Param("articleId") String articleId);

    @Modifying
    @Query("delete from hidden_articles " +
           "where article = (select gen_id from article where ARTICLE_ID = :articleId)")
    void showArticle(@Param("articleId") String articleId);
}
