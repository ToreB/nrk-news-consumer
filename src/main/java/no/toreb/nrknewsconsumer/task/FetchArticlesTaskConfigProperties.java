package no.toreb.nrknewsconsumer.task;

import lombok.Data;

import java.time.Duration;

@Data
public class FetchArticlesTaskConfigProperties {

    private boolean enabled = true;
    private Duration fixedDelay = Duration.parse("PT5M");
    private Duration initialDelay = Duration.parse("PT3S");
    private Duration fetchRate = Duration.parse("PT1H");
    private String name;
    private String articlesFeedUrl;
}
