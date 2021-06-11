package no.toreb.nrknewsconsumer.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class ArticleCategory {

    String category;

    @JsonCreator
    public ArticleCategory(@JsonProperty("category") final String category) {
        this.category = category;
    }
}
