package no.toreb.nrknewsconsumer.controller;

import lombok.Value;

@Value
public class ReadLaterRequest {

    String articleId;
    boolean readLater;
}
