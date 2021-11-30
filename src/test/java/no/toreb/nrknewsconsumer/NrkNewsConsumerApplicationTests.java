package no.toreb.nrknewsconsumer;

import lombok.SneakyThrows;
import no.toreb.nrknewsconsumer.controller.ArticleResponse;
import no.toreb.nrknewsconsumer.controller.HideArticleRequest;
import no.toreb.nrknewsconsumer.controller.ReadLaterRequest;
import no.toreb.nrknewsconsumer.model.Article;
import no.toreb.nrknewsconsumer.repository.ArticleRepository;
import no.toreb.nrknewsconsumer.task.ArticleFeedParser;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.client.RestTemplate;
import org.sqlite.SQLiteException;

import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = NrkNewsConsumerApplication.class,
                properties = {
                        "spring.datasource.url=jdbc:sqlite:file::memory:?cache=shared",
                        "spring.datasource.username=sa",
                        "logging.level.no.toreb=debug"
                }, webEnvironment = WebEnvironment.RANDOM_PORT)
class NrkNewsConsumerApplicationTests {

    @MockBean
    @SuppressWarnings("unused")
    private BuildProperties buildProperties;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @LocalServerPort
    private int port;

    private final TestRestTemplate testRestTemplate = new TestRestTemplate();

    private String getApiUrl(final String path) {
        return "http://localhost:" + port + "/api/" + path;
    }

    @AfterEach
    @SuppressWarnings("SqlWithoutWhere")
    void resetDatabase() {
        jdbcTemplate.update("delete from article_category", Collections.emptyMap());
        jdbcTemplate.update("delete from article_media", Collections.emptyMap());
        jdbcTemplate.update("delete from hidden_articles", Collections.emptyMap());
        jdbcTemplate.update("delete from read_later_articles", Collections.emptyMap());
        jdbcTemplate.update("delete from article", Collections.emptyMap());
    }

    @Test
    void shouldScheduledTaskToFetchesArticles() {
        final String testFeedContent = getTestFeedContent();
        when(restTemplate.getForEntity(any(),
                                       ArgumentMatchers.<Class<String>>any(),
                                       ArgumentMatchers.<Object>any()))
                .thenReturn(ResponseEntity.ok(testFeedContent));

        await().atMost(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(1))
               .untilAsserted(() -> {
                   try {
                       assertThat(getArticlesCount()).isEqualTo(98);
                   } catch (final Exception e) {
                       // Sqlite in-memory with shared cache throws exception if reading from db when other connection
                       // is writing.
                       if (e.getCause() instanceof SQLiteException) {
                           fail(); // means retry in this case
                       } else {
                           throw e; // fails the assertion.
                       }
                   }
               });
    }

    @Test
    void shouldProvideApiForFetchingNonHandledArticles() {
        insertTestData();

        final List<Article> articles = getAllNonHandledArticles();
        final List<Article> firstPage = articles.stream()
                                                .limit(10)
                                                .collect(Collectors.toList());
        final List<Article> secondPage = articles.stream()
                                                 .skip(10)
                                                 .limit(10)
                                                 .collect(Collectors.toList());
        final List<Article> thirdPageSize5 = articles.stream()
                                                     .skip(10)
                                                     .limit(5)
                                                     .collect(Collectors.toList());

        assertArticlesResponse(testRestTemplate.exchange(getApiUrl("/articles"),
                                                         HttpMethod.GET,
                                                         null,
                                                         ArticleResponse.class),
                               firstPage);
        assertArticlesResponse(testRestTemplate.exchange(getApiUrl("/articles?page=1&size=10"),
                                                         HttpMethod.GET,
                                                         null,
                                                         ArticleResponse.class),
                               firstPage);
        assertArticlesResponse(testRestTemplate.exchange(getApiUrl("/articles?page=2"),
                                                         HttpMethod.GET,
                                                         null,
                                                         ArticleResponse.class),
                               secondPage);
        assertArticlesResponse(testRestTemplate.exchange(getApiUrl("/articles?page=3&size=5"),
                                                         HttpMethod.GET,
                                                         null,
                                                         ArticleResponse.class),
                               thirdPageSize5);
    }

    @Test
    void shouldProvideApiForFetchingCovid19Articles() {
        insertTestData();

        final List<Article> covid19Articles = getAllCovid19Articles();
        final List<Article> firstPage = covid19Articles.stream()
                                                       .limit(10)
                                                       .collect(Collectors.toList());
        final List<Article> secondPage = covid19Articles.stream()
                                                        .skip(10)
                                                        .limit(10)
                                                        .collect(Collectors.toList());
        final List<Article> thirdPageSize5 = covid19Articles.stream()
                                                            .skip(10)
                                                            .limit(5)
                                                            .collect(Collectors.toList());

        assertArticlesResponse(testRestTemplate.exchange(getApiUrl("/articles/covid-19"),
                                                         HttpMethod.GET,
                                                         null,
                                                         ArticleResponse.class),
                               firstPage);
        assertArticlesResponse(testRestTemplate.exchange(getApiUrl("/articles/covid-19?page=1&size=10"),
                                                         HttpMethod.GET,
                                                         null,
                                                         ArticleResponse.class),
                               firstPage);
        assertArticlesResponse(testRestTemplate.exchange(getApiUrl("/articles/covid-19?page=2"),
                                                         HttpMethod.GET,
                                                         null,
                                                         ArticleResponse.class),
                               secondPage);
        assertArticlesResponse(testRestTemplate.exchange(getApiUrl("/articles/covid-19?page=3&size=5"),
                                                         HttpMethod.GET,
                                                         null,
                                                         ArticleResponse.class),
                               thirdPageSize5);
    }

    @Test
    void shouldProvideApiForControllingVisibilityArticles() {
        insertTestData();

        final List<Article> articles = getAllNonHandledArticles();

        hideArticle(articles.get(0), true);
        assertThat(articleRepository.findAllHidden(10, 0))
                .isEqualTo(List.of(articles.get(0).toBuilder().hidden(true).build()));

        hideArticle(articles.get(0), false);
        assertThat(articleRepository.findAllHidden(10, 0)).isEqualTo(Collections.emptyList());
    }

    private void hideArticle(final Article article, final boolean hide) {
        final ResponseEntity<Void> hideResponse =
                testRestTemplate.exchange(getApiUrl("/articles/hidden"),
                                          HttpMethod.PUT,
                                          new HttpEntity<>(new HideArticleRequest(article.getArticleId(), hide)),
                                          Void.class);
        assertThat(hideResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldProvideApiForFetchingHiddenArticles() {
        insertTestData();

        assertEmptyArticlesResponse(testRestTemplate.exchange(getApiUrl("/articles/hidden"),
                                                              HttpMethod.GET,
                                                              null,
                                                              ArticleResponse.class));

        getAllNonHandledArticles()
                .stream()
                .limit(20)
                .forEach(article -> hideArticle(article, true));

        final List<Article> hiddenArticles = getAllHiddenArticles();
        Collections.reverse(hiddenArticles);

        final List<Article> firstPage = hiddenArticles.stream()
                                                      .limit(10)
                                                      .collect(Collectors.toList());
        final List<Article> secondPage = hiddenArticles.stream()
                                                       .skip(10)
                                                       .limit(10)
                                                       .collect(Collectors.toList());
        final List<Article> thirdPageSize5 = hiddenArticles.stream()
                                                           .skip(10)
                                                           .limit(5)
                                                           .collect(Collectors.toList());

        assertArticlesResponse(testRestTemplate.exchange(getApiUrl("/articles/hidden"),
                                                         HttpMethod.GET,
                                                         null,
                                                         ArticleResponse.class),
                               firstPage);
        assertArticlesResponse(testRestTemplate.exchange(getApiUrl("/articles/hidden?page=1&size=10"),
                                                         HttpMethod.GET,
                                                         null,
                                                         ArticleResponse.class),
                               firstPage);
        assertArticlesResponse(testRestTemplate.exchange(getApiUrl("/articles/hidden?page=2"),
                                                         HttpMethod.GET,
                                                         null,
                                                         ArticleResponse.class),
                               secondPage);
        assertArticlesResponse(testRestTemplate.exchange(getApiUrl("/articles/hidden?page=3&size=5"),
                                                         HttpMethod.GET,
                                                         null,
                                                         ArticleResponse.class),
                               thirdPageSize5);
    }

    @Test
    void shouldProvideApiForMarkingArticlesForLaterReading() {
        insertTestData();

        final List<Article> articles = getAllNonHandledArticles();

        addReadLater(articles.get(0), true);
        assertThat(articleRepository.findAllReadLater(10, 0))
                .isEqualTo(List.of(articles.get(0).toBuilder().readLater(true).build()));

        addReadLater(articles.get(0), false);
        assertThat(articleRepository.findAllReadLater(10, 0)).isEqualTo(Collections.emptyList());
    }

    @Test
    void shouldProvideApiForFetchingReadLaterArticles() {
        insertTestData();

        assertEmptyArticlesResponse(testRestTemplate.exchange(getApiUrl("/articles/read-later"),
                                                              HttpMethod.GET,
                                                              null,
                                                              ArticleResponse.class));

        getAllArticles()
                .stream()
                .limit(20)
                .forEach(article -> addReadLater(article, true));

        final List<Article> readLaterArticles = getAllReadLaterArticles();

        final List<Article> firstPage = readLaterArticles.stream()
                                                         .limit(10)
                                                         .collect(Collectors.toList());
        final List<Article> secondPage = readLaterArticles.stream()
                                                          .skip(10)
                                                          .limit(10)
                                                          .collect(Collectors.toList());
        final List<Article> thirdPageSize5 = readLaterArticles.stream()
                                                              .skip(10)
                                                              .limit(5)
                                                              .collect(Collectors.toList());

        assertArticlesResponse(testRestTemplate.exchange(getApiUrl("/articles/read-later"),
                                                         HttpMethod.GET,
                                                         null,
                                                         ArticleResponse.class),
                               firstPage);
        assertArticlesResponse(testRestTemplate.exchange(getApiUrl("/articles/read-later?page=1&size=10"),
                                                         HttpMethod.GET,
                                                         null,
                                                         ArticleResponse.class),
                               firstPage);
        assertArticlesResponse(testRestTemplate.exchange(getApiUrl("/articles/read-later?page=2"),
                                                         HttpMethod.GET,
                                                         null,
                                                         ArticleResponse.class),
                               secondPage);
        assertArticlesResponse(testRestTemplate.exchange(getApiUrl("/articles/read-later?page=3&size=5"),
                                                         HttpMethod.GET,
                                                         null,
                                                         ArticleResponse.class),
                               thirdPageSize5);
    }

    @Test
    void shouldProvideApiForFetchingNonHiddenArticlesCount() {
        insertTestData();

        final int articleCount = getAllNonHandledArticles().size();

        final ResponseEntity<ArticleResponse> response = testRestTemplate.exchange(getApiUrl("/articles"),
                                                                                   HttpMethod.GET,
                                                                                   null,
                                                                                   ArticleResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        final ArticleResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getTotalCount()).isEqualTo(articleCount);
    }

    @Test
    void shouldProvideApiForFetchingCovid19ArticlesCount() {
        insertTestData();

        final int articleCount = getAllCovid19Articles().size();

        final ResponseEntity<ArticleResponse> response = testRestTemplate.exchange(getApiUrl("/articles/covid-19"),
                                                                                   HttpMethod.GET,
                                                                                   null,
                                                                                   ArticleResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        final ArticleResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getTotalCount()).isEqualTo(articleCount);
    }

    @Test
    void shouldProvideApiForFetchingReadLaterArticlesCount() {
        insertTestData();

        final int count = 20;
        getAllArticles()
                .stream()
                .limit(count)
                .forEach(article -> addReadLater(article, true));

        final ResponseEntity<ArticleResponse> response = testRestTemplate.exchange(getApiUrl("/articles/read-later"),
                                                                                   HttpMethod.GET,
                                                                                   null,
                                                                                   ArticleResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        final ArticleResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getTotalCount()).isEqualTo(count);
    }

    private void insertTestData() {
        final String testFeedContent = getTestFeedContent();
        new ArticleFeedParser()
                .parseFeed(testFeedContent)
                .forEach(articleRepository::save);
    }

    @SneakyThrows
    @SuppressWarnings("ConstantConditions")
    private String getTestFeedContent() {
        final List<String> lines = IOUtils.readLines(getClass().getResourceAsStream("/test-feed-toppsaker.rss"),
                                                     Charset.defaultCharset());
        return String.join("", lines);
    }

    private List<Article> getAllArticles() {
        return articleRepository.findAll();
    }

    private List<Article> getAllNonHandledArticles() {
        return getAllArticles().stream()
                               .filter(article -> !article.isReadLater() && !article.isHidden())
                               .collect(Collectors.toList());
    }

    private List<Article> getAllCovid19Articles() {
        final String regex = "(.*?korona.*|covid[ -]?19|.*?vaksine.*|.*?smitte.*|.*?pandemi.*|.*?epidemi.*)";
        return getAllArticles()
                .stream()
                .filter(article -> article.getCategories()
                                          .stream()
                                          .anyMatch(it -> it.getCategory()
                                                            .toLowerCase()
                                                            .matches(regex))
                                   || article.getDescription()
                                             .toLowerCase()
                                             .matches(regex)
                                   || article.getTitle().toLowerCase().matches(regex))
                .filter(article -> !article.isHidden())
                .collect(Collectors.toList());
    }

    private List<Article> getAllReadLaterArticles() {
        return getAllArticles().stream()
                               .filter(article -> article.isReadLater() && !article.isHidden())
                               .collect(Collectors.toList());
    }

    private List<Article> getAllHiddenArticles() {
        return getAllArticles().stream()
                               .filter(Article::isHidden)
                               .collect(Collectors.toList());
    }

    private void addReadLater(final Article article, final boolean readLater) {
        final ResponseEntity<Void> readLaterResponse =
                testRestTemplate.exchange(getApiUrl("/articles/read-later"),
                                          HttpMethod.PUT,
                                          new HttpEntity<>(new ReadLaterRequest(article.getArticleId(), readLater)),
                                          Void.class);
        assertThat(readLaterResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    private void assertArticlesResponse(final ResponseEntity<ArticleResponse> response,
                                        final List<Article> expectedArticles) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        final ArticleResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getArticles()).isEqualTo(expectedArticles);
    }

    private void assertEmptyArticlesResponse(final ResponseEntity<ArticleResponse> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        final ArticleResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getArticles()).isEmpty();
    }

    @SuppressWarnings("ConstantConditions")
    private long getArticlesCount() {
        return jdbcTemplate.queryForObject("select count(*) from article", Collections.emptyMap(), Long.class);
    }
}
