package no.toreb.nrknewsconsumer;

import no.toreb.nrknewsconsumer.repository.ArticleRepository;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = NrkNewsConsumerApplication.class,
                properties = {
                        "spring.datasource.url=jdbc:hsqldb:mem:testdb",
                        "spring.datasource.username=sa",
                        "logging.level.no.toreb=debug"
                })
class NrkNewsConsumerApplicationTests {

    @MockBean
    private BuildProperties buildProperties;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private ArticleRepository articleRepository;

    @Test
    void shouldScheduledTaskToFetchesArticles() throws IOException {
        //noinspection ConstantConditions
        final List<String> testFeedContent = IOUtils.readLines(getClass().getResourceAsStream("/test-feed.rss"),
                                                               Charset.defaultCharset());
        when(restTemplate.getForEntity(any(),
                                       ArgumentMatchers.<Class<String>>any(),
                                       ArgumentMatchers.<Object>any()))
                .thenReturn(ResponseEntity.ok(String.join("", testFeedContent)));

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            assertThat(articleRepository.count()).isGreaterThan(0);
        });
    }

}
