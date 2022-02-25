package no.toreb.nrknewsconsumer.config;

import no.toreb.nrknewsconsumer.repository.ArticleRepository;
import no.toreb.nrknewsconsumer.task.ArticleFetcher;
import no.toreb.nrknewsconsumer.task.FetchArticlesTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Configuration
@EnableScheduling
class AppConfig {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplateBuilder()
                .messageConverters(new StringHttpMessageConverter(StandardCharsets.UTF_8))
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "task.fetch-toppsaker.enabled", havingValue = "true", matchIfMissing = true)
    FetchArticlesTask fetchToppsakerTask(
            final ArticleRepository articleRepository,
            final ArticleFetcher articleFetcher,
            @Value("${task.fetch-toppsaker.articles-feed-url}") final String articlesFeedUrl,
            @Value("${task.fetch-toppsaker.fetch-rate}") final Duration syncRateDuration) {
        return new FetchArticlesTask("FetchToppsakerTask",
                                     articleRepository,
                                     articleFetcher,
                                     articlesFeedUrl,
                                     syncRateDuration);
    }

    @Bean
    @ConditionalOnProperty(name = "task.fetch-coronavirus.enabled", havingValue = "true", matchIfMissing = true)
    FetchArticlesTask fetchCoronaVirusTask(
            final ArticleRepository articleRepository,
            final ArticleFetcher articleFetcher,
            @Value("${task.fetch-coronavirus.articles-feed-url}") final String articlesFeedUrl,
            @Value("${task.fetch-coronavirus.fetch-rate}") final Duration syncRateDuration) {
        return new FetchArticlesTask("FetchCoronaVirusTask",
                                     articleRepository,
                                     articleFetcher,
                                     articlesFeedUrl,
                                     syncRateDuration);
    }
}
