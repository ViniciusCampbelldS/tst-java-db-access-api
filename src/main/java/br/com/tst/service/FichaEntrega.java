package br.com.tst.service;

public record FichaEntrega(String nome, byte[] conteudo) {
    public static final long LIMITE_BYTES = 5 * 1024 * 1024;
}
