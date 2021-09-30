package no.toreb.nrknewsconsumer.repository;

import lombok.RequiredArgsConstructor;
import no.toreb.nrknewsconsumer.model.Article;
import no.toreb.nrknewsconsumer.model.ArticleCategory;
import no.toreb.nrknewsconsumer.model.ArticleMedia;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class ArticleRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final ArticleRowMapper rowMapper;

    public ArticleRepository(final NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.rowMapper = new ArticleRowMapper(namedParameterJdbcTemplate);
    }

    @Transactional(readOnly = true)
    public List<String> distinctArticleIds() {
        final String sql = "select distinct article_id from article";

        return namedParameterJdbcTemplate.queryForList(sql, new EmptySqlParameterSource(), String.class);
    }

    @Transactional(readOnly = true)
    public List<Article> findAll() {
        final String sql = "select a.*, " +
                           "ha.article is not null as hidden, " +
                           "rla.article is not null as read_later " +
                           "from article a " +
                           "left outer join hidden_articles ha on a.gen_id = ha.article " +
                           "left outer join read_later_articles rla on a.gen_id = rla.article " +
                           "order by a.published_at, a.gen_id ";

        return namedParameterJdbcTemplate.query(sql, Collections.emptyMap(), rowMapper);
    }

    @Transactional(readOnly = true)
    public List<Article> findAllNonHandled(final long limit, final long offset) {
        final String sql = "select a.*, false as hidden, false as read_later " +
                           "from article a " +
                           "left outer join hidden_articles ha on a.gen_id = ha.article " +
                           "left outer join read_later_articles rla on a.gen_id = rla.article " +
                           "where ha.article is null " +
                           "and rla.article is null " +
                           "order by a.published_at, a.gen_id " +
                           "limit :limit " +
                           "offset :offset";

        return namedParameterJdbcTemplate.query(sql, Map.of("limit", limit, "offset", offset), rowMapper);
    }

    @Transactional(readOnly = true)
    public List<Article> findAllHidden(final long limit, final long offset) {
        //noinspection SqlResolve
        final String sql = "select a.*, true as hidden, rla.article is not null as read_later " +
                           "from article a " +
                           "left outer join hidden_articles ha on a.gen_id = ha.article " +
                           "left outer join read_later_articles rla on a.gen_id = rla.article " +
                           "where ha.article is not null " +
                           "order by ha.hidden_at desc, a.gen_id " +
                           "limit :limit " +
                           "offset :offset";

        return namedParameterJdbcTemplate.query(sql, Map.of("limit", limit, "offset", offset), rowMapper);
    }

    @Transactional(readOnly = true)
    public List<Article> findAllReadLater(final long limit, final long offset) {
        final String sql = "select a.*, false as hidden, true as read_later " +
                           "from article a " +
                           "left outer join hidden_articles ha on a.gen_id = ha.article " +
                           "left outer join read_later_articles rla on a.gen_id = rla.article " +
                           "where ha.article is null " +
                           "and rla.article is not null " +
                           "order by a.published_at, a.GEN_ID " +
                           "limit :limit " +
                           "offset :offset";

        return namedParameterJdbcTemplate.query(sql, Map.of("limit", limit, "offset", offset), rowMapper);
    }

    @Transactional(readOnly = true)
    public List<Article> findAllCovid19(final long limit, final long offset) {
        final List<String> patterns = List.of("%korona%", "%covid%19%", "%vaksine%");
        final List<String> columns = List.of("ac.category", "a.description", "a.title");

        final String patternMatchingClause =
                "and (" +
                patterns.stream()
                        .map(pattern -> columns.stream()
                                               .map(column -> String.format("lower(%s) like '%s'", column, pattern))
                                               .collect(Collectors.joining(" or ")))
                        .collect(Collectors.joining(" or ")) +
                ")";

        final String sql = "select distinct a.*, false as hidden, false as read_later " +
                           "from article a " +
                           "left outer join article_category ac on a.gen_id = ac.article " +
                           "left outer join hidden_articles ha on a.gen_id = ha.article " +
                           "left outer join read_later_articles rla on a.gen_id = rla.article " +
                           "where ha.article is null " +
                           "and rla.article is null " +
                           patternMatchingClause +
                           "order by a.published_at, a.gen_id " +
                           "limit :limit " +
                           "offset :offset";

        return namedParameterJdbcTemplate.query(sql, Map.of("limit", limit, "offset", offset), rowMapper);
    }

    @Transactional
    public void save(final Article article) {
        final long genId = insertArticle(article);
        insertArticleMedia(article, genId);
        insertArticleCategories(article, genId);
    }

    private long insertArticle(final Article article) {
        //noinspection SqlInsertValues
        final String sql = "insert into article (" +
                           "    article_id, title, description, link, author, published_at" +
                           ") values (" +
                           "    :articleId, :title, :description, :link, :author, :publishedAt" +
                           ")";

        final SqlParameterSource params = new MapSqlParameterSource()
                .addValue("articleId", article.getArticleId())
                .addValue("title", article.getTitle())
                .addValue("description", article.getDescription())
                .addValue("link", article.getLink())
                .addValue("author", article.getAuthor())
                .addValue("publishedAt", article.getPublishedAt()
                                                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        final KeyHolder keyHolder = new GeneratedKeyHolder();

        namedParameterJdbcTemplate.update(sql, params, keyHolder, new String[] { "gen_id" });
        //noinspection ConstantConditions
        return keyHolder.getKey().longValue();
    }

    private void insertArticleMedia(final Article article, final long articleGenId) {
        final String sql = "insert into article_media (" +
                           "    article, medium, type, url, credit, title" +
                           ") values (" +
                           "    :article, :medium, :type, :url, :credit, :title" +
                           ")";

        article.getMedia().forEach(media -> {
            final SqlParameterSource params = new MapSqlParameterSource()
                    .addValue("article", articleGenId)
                    .addValue("medium", media.getMedium())
                    .addValue("type", media.getType())
                    .addValue("url", media.getUrl())
                    .addValue("credit", media.getCredit())
                    .addValue("title", media.getTitle());

            namedParameterJdbcTemplate.update(sql, params);
        });
    }

    private void insertArticleCategories(final Article article, final long articleGenId) {
        final String sql = "insert into article_category (article, category) values (:article, :category)";

        article.getCategories().forEach(category -> {
            final SqlParameterSource params = new MapSqlParameterSource()
                    .addValue("article", articleGenId)
                    .addValue("category", category.getCategory());

            namedParameterJdbcTemplate.update(sql, params);
        });
    }

    @Transactional
    public void hideArticle(final String articleId, final String hiddenAt) {
        //noinspection SqlResolve
        final String sql = "insert into hidden_articles (article, hidden_at) " +
                           "select GEN_ID, :hiddenAt from article where ARTICLE_ID = :articleId";

        namedParameterJdbcTemplate.update(sql, Map.of("articleId", articleId, "hiddenAt", hiddenAt));
    }

    @Transactional
    public void showArticle(final String articleId) {
        final String sql = "delete from hidden_articles " +
                           "where article = (select gen_id from article where ARTICLE_ID = :articleId)";

        namedParameterJdbcTemplate.update(sql, Map.of("articleId", articleId));
    }

    @Transactional
    public void addReadLater(final String articleId, final String addedAt) {
        final String sql = "insert into read_later_articles (article, added_at) " +
                           "select GEN_ID, :addedAt from article where ARTICLE_ID = :articleId";

        namedParameterJdbcTemplate.update(sql, Map.of("articleId", articleId, "addedAt", addedAt));
    }

    @Transactional
    public void removeReadLater(final String articleId) {
        final String sql = "delete from read_later_articles " +
                           "where article = (select gen_id from article where ARTICLE_ID = :articleId)";

        namedParameterJdbcTemplate.update(sql, Map.of("articleId", articleId));
    }

    @RequiredArgsConstructor
    private static class ArticleRowMapper implements RowMapper<Article> {

        private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

        @Override
        public Article mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            final long genId = rs.getLong("gen_id");
            return Article.builder()
                          .genId(genId)
                          .articleId(rs.getString("article_id"))
                          .title(rs.getString("title"))
                          .description(rs.getString("description"))
                          .link(rs.getString("link"))
                          .author(rs.getString("author"))
                          .publishedAt(OffsetDateTime.parse(rs.getString("published_at")))
                          .categories(getArticleCategories(genId))
                          .media(getArticleMedia(genId))
                          .build();
        }

        private Set<ArticleCategory> getArticleCategories(final long articleGenId) {
            final String sql = "select * from article_category where article = :articleGenId";

            final List<ArticleCategory> categories =
                    namedParameterJdbcTemplate.query(sql,
                                                     Map.of("articleGenId", articleGenId),
                                                     (rs, rowNum) -> new ArticleCategory(rs.getString("category")));
            return new HashSet<>(categories);
        }

        private Set<ArticleMedia> getArticleMedia(final long articleGenId) {
            final String sql = "select * from article_media where article = :articleGenId";

            final List<ArticleMedia> media =
                    namedParameterJdbcTemplate.query(sql,
                                                     Map.of("articleGenId", articleGenId),
                                                     (rs, rowNum) -> ArticleMedia.builder()
                                                                                 .medium(rs.getString("medium"))
                                                                                 .type(rs.getString("type"))
                                                                                 .url(rs.getString("url"))
                                                                                 .credit(rs.getString("credit"))
                                                                                 .title(rs.getString("title"))
                                                                                 .build());
            return new HashSet<>(media);
        }
    }
}
