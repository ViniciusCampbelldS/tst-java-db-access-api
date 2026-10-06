package br.com.tst.dto;

import br.com.tst.model.DiasNotificacao;

public record DiasNotificacaoResponse(
    Long id,
    String caOuNr,
    boolean isNorma,
    Integer diasAviso
) {
    public static DiasNotificacaoResponse from(DiasNotificacao diasNotificacao) {
        return new DiasNotificacaoResponse(
            diasNotificacao.getId(),
            diasNotificacao.getCaOuNr(),
            diasNotificacao.isNorma(),
            diasNotificacao.getDiasAviso()
        );
    }
}
