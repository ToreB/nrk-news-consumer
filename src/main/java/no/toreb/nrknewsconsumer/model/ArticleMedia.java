package no.toreb.nrknewsconsumer.model;

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
}
