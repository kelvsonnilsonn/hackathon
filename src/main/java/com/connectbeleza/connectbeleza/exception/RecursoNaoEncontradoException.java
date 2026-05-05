package com.connectbeleza.connectbeleza.exception;

public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String recurso, Object id) {
        super(recurso + " não encontrado(a) com id: " + id);
    }
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
