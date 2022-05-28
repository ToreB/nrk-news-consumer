package no.toreb.nrknewsconsumer.config;

import lombok.RequiredArgsConstructor;
import no.toreb.nrknewsconsumer.repository.ArticleRepository;
import no.toreb.nrknewsconsumer.task.ArticleFetcher;
import no.toreb.nrknewsconsumer.task.FetchArticlesTask;
import no.toreb.nrknewsconsumer.task.FetchArticlesTaskConfigProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.IntervalTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.List;

@Configuration
@EnableConfigurationProperties
class TasksConfig {

    @Bean
    @ConfigurationProperties("task.fetch-toppsaker")
    FetchArticlesTaskConfigProperties fetchToppsakerTaskConfigProps() {
        return new FetchArticlesTaskConfigProperties();
    }

    @Bean
    @ConfigurationProperties("task.fetch-coronavirus")
    FetchArticlesTaskConfigProperties fetchCoronaVirusTaskConfigProps() {
        return new FetchArticlesTaskConfigProperties();
    }

    @Bean
    @ConfigurationProperties("task.fetch-urix")
    FetchArticlesTaskConfigProperties fetchUrixTaskConfigProps() {
        return new FetchArticlesTaskConfigProperties();
    }

    @Bean
    @ConfigurationProperties("task.fetch-monkeypox")
    FetchArticlesTaskConfigProperties fetchMonkeypoxConfigProps() {
        return new FetchArticlesTaskConfigProperties();
    }

    @Configuration
    @EnableScheduling
    @RequiredArgsConstructor
    static class SchedulingConfig implements SchedulingConfigurer {

        private final List<FetchArticlesTaskConfigProperties> fetchArticlesTaskConfigProperties;
        private final ArticleRepository articleRepository;
        private final ArticleFetcher articleFetcher;

        @Bean
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
        FetchArticlesTask fetchArticlesTask(final FetchArticlesTaskConfigProperties properties) {
            return new FetchArticlesTask(properties.getName(),
                                         articleRepository,
                                         articleFetcher,
                                         properties.getArticlesFeedUrl(),
                                         properties.getFetchRate());
        }

        @Override
        public void configureTasks(final ScheduledTaskRegistrar taskRegistrar) {
            fetchArticlesTaskConfigProperties
                    .stream()
                    .filter(FetchArticlesTaskConfigProperties::isEnabled)
                    .forEach(properties -> {
                        final FetchArticlesTask task = fetchArticlesTask(properties);
                        taskRegistrar.addFixedDelayTask(new IntervalTask(task::run,
                                                                         properties.getFixedDelay().toMillis(),
                                                                         properties.getInitialDelay().toMillis()));
                    });
        }
    }
}
