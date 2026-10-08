package br.com.tst.dto;

import br.com.tst.model.NR;

public record NRResponse(Long id, String nome) {

    public static NRResponse from(NR nr) {
        return new NRResponse(nr.getId(), nr.getNome());
    }
}
