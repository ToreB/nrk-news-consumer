package no.toreb.nrknewsconsumer.controller;

import lombok.Value;

@Value
public class HideArticleRequest {

    String articleId;
    boolean hide;
}
