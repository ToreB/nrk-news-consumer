package no.toreb.nrknewsconsumer.repository;

import no.toreb.nrknewsconsumer.NrkNewsConsumerApplication;
import no.toreb.nrknewsconsumer.model.Article;
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
import java.util.List;

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
        repository = new ArticleRepository(namedParameterJdbcTemplate);
    }

    @Test
    void filterOutExistingArticles_whenNoArticlesAlreadyExist_shouldReturnAllArticles() {
        repository.save(article("1"));
        repository.save(article("2"));
        repository.save(article("3"));

        final List<Article> articles = List.of(
                article("4"),
                article("5")
        );

        final List<Article> result = repository.filterOutExistingArticles(articles);

        assertThat(result).isEqualTo(articles);
    }

    @Test
    void filterOutExistingArticles_whenSomeArticlesAlreadyExist_shouldReturnAllOtherArticles() {
        final Article existing1 = article("1");
        repository.save(existing1);
        final Article existing2 = article("2");
        repository.save(existing2);
        final Article existing3 = article("3");
        repository.save(existing3);

        final Article new1 = article("4");
        final Article new2 = article("5");
        final List<Article> articles = List.of(existing1, new1, existing2, new2, existing3);

        final List<Article> result = repository.filterOutExistingArticles(articles);

        assertThat(result).isEqualTo(List.of(new1, new2));
    }

    private Article article(final String articleId) {
        return Article.builder()
                      .articleId(articleId)
                      .title("Article " + articleId)
                      .link("link " + articleId)
                      .publishedAt(OffsetDateTime.now())
                      .build();
    }
}