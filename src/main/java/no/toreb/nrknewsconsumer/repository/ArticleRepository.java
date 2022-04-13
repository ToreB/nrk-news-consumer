package no.toreb.nrknewsconsumer.repository;

import lombok.RequiredArgsConstructor;
import no.toreb.nrknewsconsumer.model.Article;
import no.toreb.nrknewsconsumer.model.ArticleCategory;
import no.toreb.nrknewsconsumer.model.ArticleMedia;
import no.toreb.nrknewsconsumer.model.SortOrder;
import org.springframework.jdbc.core.RowMapper;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@SuppressWarnings({ "java:S1192" })
public class ArticleRepository {

    private static final List<String> UKRAINE_RUSSIA_PATTERNS = List.of(
            "%ukrain%", "%russland%", "%russer%", "%russar%", "%russisk%"
    );
    private static final List<String> COVID_19_PATTERNS = List.of(
            "%korona%", "%covid%19%", "%vaksine%", "%vaksinasjon%", "%smitte%", "%pandemi%", "%epidemi%",
            "%omikron%", "%omicron%", "%virus%"
    );

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final ArticleRowMapper rowMapper;

    public ArticleRepository(final NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.rowMapper = new ArticleRowMapper(namedParameterJdbcTemplate);
    }

    @Transactional(readOnly = true)
    public List<Article> filterOutExistingArticles(final List<Article> articles) {
        final String sql = "select article_id " +
                           "from article " +
                           "where article_id in (:articleIds)";

        final List<String> articleIds = articles.stream()
                                                .map(Article::getArticleId)
                                                .toList();
        final List<String> existingArticleIds =
                namedParameterJdbcTemplate.queryForList(sql, Map.of("articleIds", articleIds), String.class);

        return articles.stream()
                       .filter(article -> !existingArticleIds.contains(article.getArticleId()))
                       .toList();
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
    public List<Article> findAllNonHandled(final int limit, final int offset, final SortOrder sortOrder) {
        final SortOrder resolvedSortOrder = Optional.ofNullable(sortOrder).orElse(SortOrder.ASC);
        final String sql = buildNonHandledArticlesSql(
                "select *",
                "order by published_at %s, gen_id %s".formatted(resolvedSortOrder, resolvedSortOrder),
                limit,
                offset);

        return namedParameterJdbcTemplate.query(sql, Map.of("limit", limit, "offset", offset), rowMapper);
    }

    @Transactional(readOnly = true)
    public long countAllNonHandled() {
        final String sql = buildNonHandledArticlesSql("select count(1)", null, null, null);

        //noinspection ConstantConditions
        return namedParameterJdbcTemplate.queryForObject(sql, Collections.emptyMap(), Long.class);
    }

    private String buildNonHandledArticlesSql(final String select,
                                              final String orderBy,
                                              final Integer limit,
                                              final Integer offset) {
        return select +
               " from (" +
               "    select a.*, false as hidden, false as read_later" +
               "    from article a " +
               "    left outer join hidden_articles ha on a.gen_id = ha.article " +
               "    left outer join read_later_articles rla on a.gen_id = rla.article " +
               "    where ha.article is null " +
               "    and rla.article is null" +
               ") tmp " +
               (orderBy != null ? orderBy : "") +
               (limit != null ? " limit :limit " : "") +
               (offset != null ? " offset :offset" : "");
    }

    @Transactional(readOnly = true)
    public List<Article> findAllHidden(final int limit, final int offset, final SortOrder sortOrder) {
        final SortOrder resolvedSortOrder = Optional.ofNullable(sortOrder).orElse(SortOrder.DESC);
        final String orderBy = "order by ha.hidden_at %s, a.gen_id %s ".formatted(resolvedSortOrder, resolvedSortOrder);
        final String sql = "select a.*, true as hidden, rla.article is not null as read_later " +
                           "from article a " +
                           "left outer join hidden_articles ha on a.gen_id = ha.article " +
                           "left outer join read_later_articles rla on a.gen_id = rla.article " +
                           "where ha.article is not null " +
                           orderBy +
                           "limit :limit " +
                           "offset :offset";

        return namedParameterJdbcTemplate.query(sql, Map.of("limit", limit, "offset", offset), rowMapper);
    }

    @Transactional(readOnly = true)
    public List<Article> findAllReadLater(final int limit, final int offset, final SortOrder sortOrder) {
        final SortOrder resolvedSortOrder = Optional.ofNullable(sortOrder).orElse(SortOrder.ASC);
        final String sql = buildReadLaterArticlesSql(
                "select *",
                "order by published_at %s, gen_id %s".formatted(resolvedSortOrder, resolvedSortOrder),
                limit,
                offset);

        return namedParameterJdbcTemplate.query(sql, Map.of("limit", limit, "offset", offset), rowMapper);
    }

    @Transactional(readOnly = true)
    public long countReadLater() {
        final String sql = buildReadLaterArticlesSql("select count(1)", null, null, null);

        //noinspection ConstantConditions
        return namedParameterJdbcTemplate.queryForObject(sql, Collections.emptyMap(), Long.class);
    }

    private String buildReadLaterArticlesSql(final String select,
                                             final String orderBy,
                                             final Integer limit,
                                             final Integer offset) {
        return select +
               " from (" +
               "    select a.*, false as hidden, true as read_later " +
               "    from article a " +
               "    left outer join hidden_articles ha on a.gen_id = ha.article " +
               "    left outer join read_later_articles rla on a.gen_id = rla.article " +
               "    where ha.article is null " +
               "    and rla.article is not null " +
               ") tmp " +
               (orderBy != null ? orderBy : "") +
               (limit != null ? " limit :limit " : "") +
               (offset != null ? " offset :offset" : "");
    }

    @Transactional(readOnly = true)
    public List<Article> findAllCovid19(final int limit, final int offset, final SortOrder sortOrder) {
        final SortOrder resolvedSortOrder = Optional.ofNullable(sortOrder).orElse(SortOrder.ASC);
        final String sql = buildArticlesFilterSql(
                "select *",
                "order by published_at %s, gen_id %s".formatted(resolvedSortOrder, resolvedSortOrder),
                COVID_19_PATTERNS,
                limit,
                offset);

        return namedParameterJdbcTemplate.query(sql, Map.of("limit", limit, "offset", offset), rowMapper);
    }

    @Transactional(readOnly = true)
    public long countCovid19() {
        final String sql = buildArticlesFilterSql("select count(1)", null, COVID_19_PATTERNS, null, null);

        //noinspection ConstantConditions
        return namedParameterJdbcTemplate.queryForObject(sql, Collections.emptyMap(), Long.class);
    }

    @Transactional(readOnly = true)
    public List<Article> findAllUkraineRussia(final int limit, final int offset, final SortOrder sortOrder) {
        final SortOrder resolvedSortOrder = Optional.ofNullable(sortOrder).orElse(SortOrder.ASC);
        final String sql = buildArticlesFilterSql(
                "select *",
                "order by published_at %s, gen_id %s".formatted(resolvedSortOrder, resolvedSortOrder),
                UKRAINE_RUSSIA_PATTERNS,
                limit,
                offset);

        return namedParameterJdbcTemplate.query(sql, Map.of("limit", limit, "offset", offset), rowMapper);
    }

    @Transactional(readOnly = true)
    public long countUkraineRussia() {
        final String sql = buildArticlesFilterSql("select count(1)", null, UKRAINE_RUSSIA_PATTERNS, null, null);

        //noinspection ConstantConditions
        return namedParameterJdbcTemplate.queryForObject(sql, Collections.emptyMap(), Long.class);
    }

    private String buildArticlesFilterSql(final String select,
                                          final String orderBy,
                                          final List<String> filterPatterns,
                                          final Integer limit,
                                          final Integer offset) {
        final List<String> columns = List.of("ac.category", "a.description", "a.title");

        final String patternMatchingClause =
                "and (" +
                filterPatterns.stream()
                              .map(pattern -> columns.stream()
                                                     .map(column -> String.format("lower(%s) like '%s'",
                                                                                  column, pattern))
                                                     .collect(Collectors.joining(" or ")))
                              .collect(Collectors.joining(" or ")) +
                ")";

        return select +
               " from (" +
               "    select distinct a.*, false as hidden, false as read_later " +
               "    from article a " +
               "    left outer join article_category ac on a.gen_id = ac.article " +
               "    left outer join hidden_articles ha on a.gen_id = ha.article " +
               "    left outer join read_later_articles rla on a.gen_id = rla.article " +
               "    where ha.article is null " +
               "    and rla.article is null " +
               patternMatchingClause +
               ") tmp " +
               (orderBy != null ? orderBy : "") +
               (limit != null ? " limit :limit " : "") +
               (offset != null ? " offset :offset" : "");
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
                .addValue("publishedAt", format(article.getPublishedAt()));

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
    public void hideArticle(final String articleId, final OffsetDateTime hiddenAt) {
        //noinspection SqlResolve
        final String sql = "insert into hidden_articles (article, hidden_at) " +
                           "select GEN_ID, :hiddenAt from article where ARTICLE_ID = :articleId";

        namedParameterJdbcTemplate.update(sql, Map.of("articleId", articleId,
                                                      "hiddenAt", format(hiddenAt)));
    }

    @Transactional
    public void showArticle(final String articleId) {
        final String sql = "delete from hidden_articles " +
                           "where article = (select gen_id from article where ARTICLE_ID = :articleId)";

        namedParameterJdbcTemplate.update(sql, Map.of("articleId", articleId));
    }

    @Transactional
    public void addReadLater(final String articleId, final OffsetDateTime addedAt) {
        final String sql = "insert into read_later_articles (article, added_at) " +
                           "select GEN_ID, :addedAt from article where ARTICLE_ID = :articleId";

        namedParameterJdbcTemplate.update(sql, Map.of("articleId", articleId,
                                                      "addedAt", format(addedAt)));
    }

    @Transactional
    public void removeReadLater(final String articleId) {
        final String sql = "delete from read_later_articles " +
                           "where article = (select gen_id from article where ARTICLE_ID = :articleId)";

        namedParameterJdbcTemplate.update(sql, Map.of("articleId", articleId));
    }

    private String format(final OffsetDateTime offsetDateTime) {
        return offsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
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
                          .hidden(rs.getBoolean("hidden"))
                          .readLater(rs.getBoolean("read_later"))
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
