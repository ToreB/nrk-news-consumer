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

    @Override
    @Query("select * " +
           "from article " +
           "order by PUBLISHED_AT, GEN_ID ")
    List<Article> findAll();

    @Query("select a.* " +
           "from article a " +
           "left outer join hidden_articles ha on a.gen_id = ha.article " +
           "left outer join read_later_articles rla on a.gen_id = rla.article " +
           "where ha.article is null " +
           "and rla.article is null " +
           "order by PUBLISHED_AT, GEN_ID " +
           "limit :limit " +
           "offset :offset")
    List<Article> findAllNonHandled(@Param("limit") long limit, @Param("offset") long offset);

    @Query("select a.* " +
           "from article a " +
           "left outer join hidden_articles ha on a.gen_id = ha.article " +
           "where ha.article is not null " +
           "order by ha.hidden_at desc, a.GEN_ID " +
           "limit :limit " +
           "offset :offset")
    List<Article> findAllHidden(@Param("limit") long limit, @Param("offset") long offset);

    @Query("select a.* " +
           "from article a " +
           "left outer join hidden_articles ha on a.gen_id = ha.article " +
           "left outer join read_later_articles rla on a.gen_id = rla.article " +
           "where ha.article is null " +
           "and rla.article is not null " +
           "order by a.published_at, a.GEN_ID " +
           "limit :limit " +
           "offset :offset")
    List<Article> findAllReadLater(@Param("limit") long limit, @Param("offset") long offset);

    @Query("select distinct a.* " +
           "from article a " +
           "left outer join article_category ac on a.gen_id = ac.article " +
           "left outer join hidden_articles ha on a.gen_id = ha.article " +
           "left outer join read_later_articles rla on a.gen_id = rla.article " +
           "where ha.article is null " +
           "and rla.article is null " +
           "and (lower(ac.category) like '%korona%' " +
           "    or lower(a.description) like '%korona%' " +
           "    or lower(a.title) like '%korona%' " +
           "    or lower(ac.category) like '%covid%19%' " +
           "    or lower(a.description) like '%covid%19%' " +
           "    or lower(a.title) like '%covid%19%' " +
           "    or lower(ac.category) like '%vaksine%' " +
           "    or lower(a.title) like '%vaksine%' " +
           "    or lower(a.description) like '%vaksine%') " +
           "order by a.published_at, a.gen_id " +
           "limit :limit " +
           "offset :offset")
    List<Article> findAllCovid19(@Param("limit") long limit, @Param("offset") long offset);

    @Modifying
    @Query("insert into hidden_articles (article, hidden_at) " +
           "select GEN_ID, :hiddenAt from article where ARTICLE_ID = :articleId")
    void hideArticle(@Param("articleId") String articleId, @Param("hiddenAt") String hiddenAt);

    @Modifying
    @Query("delete from hidden_articles " +
           "where article = (select gen_id from article where ARTICLE_ID = :articleId)")
    void showArticle(@Param("articleId") String articleId);

    @Modifying
    @Query("insert into read_later_articles (article, added_at) " +
           "select GEN_ID, :addedAt from article where ARTICLE_ID = :articleId")
    void addReadLater(@Param("articleId") String articleId, @Param("addedAt") String addedAt);

    @Modifying
    @Query("delete from read_later_articles " +
           "where article = (select gen_id from article where ARTICLE_ID = :articleId)")
    void removeReadLater(@Param("articleId") String articleId);
}
