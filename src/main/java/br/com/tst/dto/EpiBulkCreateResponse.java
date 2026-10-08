package br.com.tst.dto;

import java.util.List;

public record EpiBulkCreateResponse(
        List<EpiResponse> items
) {}