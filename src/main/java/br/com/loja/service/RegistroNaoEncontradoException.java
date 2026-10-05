package br.com.loja.service;

public class RegistroNaoEncontradoException extends RuntimeException {

    public RegistroNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
