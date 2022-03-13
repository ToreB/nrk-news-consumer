package no.toreb.nrknewsconsumer.controller;

import lombok.Data;
import lombok.NoArgsConstructor;
import no.toreb.nrknewsconsumer.model.SortOrder;

@Data
@NoArgsConstructor
public class PageParam {

    private int page = 1;
    private int size = 10;
    private String sortOrder;

    public SortOrder getSortOrder() {
        return SortOrder.fromValue(sortOrder);
    }
}
