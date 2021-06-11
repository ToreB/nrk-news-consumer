package no.toreb.nrknewsconsumer;

import no.toreb.nrknewsconsumer.controller.HideArticleRequest;
import no.toreb.nrknewsconsumer.model.Article;
import no.toreb.nrknewsconsumer.repository.ArticleRepository;
import org.apache.commons.io.IOUtils;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = NrkNewsConsumerApplication.class,
                properties = {
                        "spring.datasource.url=jdbc:hsqldb:mem:testdb",
                        "spring.datasource.username=sa",
                        "logging.level.no.toreb=debug"
                }, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(OrderAnnotation.class)
class NrkNewsConsumerApplicationTests {

    @MockBean
    @SuppressWarnings("unused")
    private BuildProperties buildProperties;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private ArticleRepository articleRepository;

    @LocalServerPort
    private int port;

    private final TestRestTemplate testRestTemplate = new TestRestTemplate();

    private String getApiUrl(final String path) {
        return "http://localhost:" + port + "/api/" + path;
    }

    @Test
    @Order(10)
    void shouldScheduledTaskToFetchesArticles() throws IOException {
        //noinspection ConstantConditions
        final List<String> testFeedContent =
                IOUtils.readLines(getClass().getResourceAsStream("/test-feed-toppsaker.rss"),
                                  Charset.defaultCharset());
        when(restTemplate.getForEntity(any(),
                                       ArgumentMatchers.<Class<String>>any(),
                                       ArgumentMatchers.<Object>any()))
                .thenReturn(ResponseEntity.ok(String.join("", testFeedContent)));

        await().atMost(Duration.ofSeconds(30))
               .untilAsserted(() -> assertThat(articleRepository.count()).isEqualTo(98));
    }

    @Test
    @Order(20)
    @SuppressWarnings("Convert2Diamond")
    void shouldProvideRestApiForFetchingArticles() {
        final List<Article> articles = getAllArticles();
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
                                                         new ParameterizedTypeReference<List<Article>>() {}),
                               firstPage);
        assertArticlesResponse(testRestTemplate.exchange(getApiUrl("/articles?page=1&size=10"),
                                                         HttpMethod.GET,
                                                         null,
                                                         new ParameterizedTypeReference<List<Article>>() {}),
                               firstPage);
        assertArticlesResponse(testRestTemplate.exchange(getApiUrl("/articles?page=2"),
                                                         HttpMethod.GET,
                                                         null,
                                                         new ParameterizedTypeReference<List<Article>>() {}),
                               secondPage);
        assertArticlesResponse(testRestTemplate.exchange(getApiUrl("/articles?page=3&size=5"),
                                                         HttpMethod.GET,
                                                         null,
                                                         new ParameterizedTypeReference<List<Article>>() {}),
                               thirdPageSize5);
    }

    private List<Article> getAllArticles() {
        return IterableUtil.toCollection(articleRepository.findAll())
                           .stream()
                           .sorted(Comparator.comparing(Article::getPublishedAt))
                           .collect(Collectors.toList());
    }

    @Test
    @Order(30)
    void shouldProvideRestApiForControllingVisibilityArticles() {
        final List<Article> articles = getAllArticles();

        hideArticle(articles.get(0), true);
        assertThat(articleRepository.findAllHidden(10, 0)).isEqualTo(List.of(articles.get(0)));

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
    @Order(40)
    @SuppressWarnings("Convert2Diamond")
    void shouldProvideRestApiForFetchingHiddenArticles() {
        assertEmptyArticlesResponse(testRestTemplate.exchange(getApiUrl("/articles/hidden"),
                                                              HttpMethod.GET,
                                                              null,
                                                              new ParameterizedTypeReference<List<Article>>() {}));

        final List<Article> articles = getAllArticles();
        IntStream.range(0, 20).mapToObj(articles::get).forEach(article -> hideArticle(article, true));

        final List<Article> hiddenArticles = IterableUtil.toCollection(articleRepository.findAllHidden(1000, 0))
                                                         .stream()
                                                         .sorted(Comparator.comparing(Article::getPublishedAt))
                                                         .collect(Collectors.toList());
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
                                                         new ParameterizedTypeReference<List<Article>>() {}),
                               firstPage);
        assertArticlesResponse(testRestTemplate.exchange(getApiUrl("/articles/hidden?page=1&size=10"),
                                                         HttpMethod.GET,
                                                         null,
                                                         new ParameterizedTypeReference<List<Article>>() {}),
                               firstPage);
        assertArticlesResponse(testRestTemplate.exchange(getApiUrl("/articles/hidden?page=2"),
                                                         HttpMethod.GET,
                                                         null,
                                                         new ParameterizedTypeReference<List<Article>>() {}),
                               secondPage);
        assertArticlesResponse(testRestTemplate.exchange(getApiUrl("/articles/hidden?page=3&size=5"),
                                                         HttpMethod.GET,
                                                         null,
                                                         new ParameterizedTypeReference<List<Article>>() {}),
                               thirdPageSize5);
    }

    private void assertArticlesResponse(final ResponseEntity<List<Article>> response,
                                        final List<Article> expectedArticles) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty()
                                      .isEqualTo(expectedArticles);
    }

    private void assertEmptyArticlesResponse(final ResponseEntity<List<Article>> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }
}
