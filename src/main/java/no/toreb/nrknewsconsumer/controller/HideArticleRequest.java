package no.toreb.nrknewsconsumer.controller;

import lombok.Value;

@Value
class HideArticleRequest {

    String articleId;
    boolean hide;
}
