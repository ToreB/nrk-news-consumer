package no.toreb.nrknewsconsumer.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ArticleMedia {

    String medium;
    String type;
    String url;
    String credit;
    String title;

    @JsonCreator
    public ArticleMedia(@JsonProperty("medium") final String medium,
                        @JsonProperty("type") final String type,
                        @JsonProperty("url") final String url,
                        @JsonProperty("credit") final String credit,
                        @JsonProperty("title") final String title) {
        this.medium = medium;
        this.type = type;
        this.url = url;
        this.credit = credit;
        this.title = title;
    }
}
