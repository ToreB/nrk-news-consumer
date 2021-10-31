package no.toreb.nrknewsconsumer.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import no.toreb.nrknewsconsumer.model.Article;

import java.util.List;

@Value
public class ArticleResponse {

    List<Article> articles;
    long totalCount;

    @JsonCreator
    public ArticleResponse(@JsonProperty("articles") final List<Article> articles,
                           @JsonProperty("totalCount") final long totalCount) {
        this.articles = List.copyOf(articles);
        this.totalCount = totalCount;
    }
}
