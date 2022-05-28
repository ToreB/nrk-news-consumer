package no.toreb.nrknewsconsumer.repository;

import no.toreb.nrknewsconsumer.NrkNewsConsumerApplication;
import no.toreb.nrknewsconsumer.model.Article;
import no.toreb.nrknewsconsumer.model.ArticleCategory;
import no.toreb.nrknewsconsumer.model.ArticleMedia;
import org.flywaydb.test.annotation.FlywayTest;
import org.flywaydb.test.junit5.annotation.FlywayTestExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest(properties = {
        "spring.datasource.url=jdbc:sqlite:file::memory:?cache=shared",
        "spring.datasource.username=sa"
})
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ContextConfiguration(classes = NrkNewsConsumerApplication.class)
@FlywayTestExtension
@DirtiesContext
class ArticleRepositoryTest {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private ArticleRepository repository;

    @BeforeEach
    @FlywayTest
    void setUp() {
        repository = new ArticleRepository(namedParameterJdbcTemplate,
                                           Collections.emptyList(),
                                           Collections.emptyList());
    }

    @Test
    void save_whenArticleExist_shouldUpdateArticle() {
        final Article article = article("1")
                                        .toBuilder()
                                        .categories(Set.of(new ArticleCategory("cat1"),
                                                           new ArticleCategory("cat2")))
                                        .media(Set.of(ArticleMedia.builder()
                                                                  .url("url1")
                                                                  .type("image")
                                                                  .medium("image")
                                                                  .credit("credit1")
                                                                  .title("title1")
                                                                  .build()))
                                        .build();
        repository.save(article);

        final Article updatedArticle = article.toBuilder()
                                              .title("title2")
                                              .link("link2")
                                              .author("author2")
                                              .description("description2")
                                              .publishedAt(OffsetDateTime.now().minusDays(1))
                                              .categories(Set.of(new ArticleCategory("cat3")))
                                              .media(Set.of(ArticleMedia.builder()
                                                                        .url("url2")
                                                                        .type("type2")
                                                                        .medium("medium2")
                                                                        .credit("credit2")
                                                                        .title("title2")
                                                                        .build()))
                                              .build();

        repository.save(updatedArticle);

        final List<Article> articles = getAll();

        assertThat(articles).containsExactly(updatedArticle);
    }

    private List<Article> getAll() {
        return repository.findAll()
                         .stream()
                         .map(it -> it.toBuilder().genId(null).build())
                         .toList();
    }

    private Article article(final String articleId) {
        return Article.builder()
                      .articleId(articleId)
                      .title("Article " + articleId)
                      .link("link " + articleId)
                      .author("author " + articleId)
                      .description("description " + articleId)
                      .publishedAt(OffsetDateTime.now())
                      .build();
    }
}