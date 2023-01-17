package br.gov.planejamento.siconv.med.infra.util;

import java.lang.reflect.Method;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.apache.log4j.Logger;

/**
 * Interceptador que loga execução de métodos com o tempo de resposta.
 */
@Priority(4000)
@Interceptor
@Log
public class LoggingInterceptor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(LoggingInterceptor.class);

    /**
     * Template msg log.
     */
    private static final String MSG_LOG_ANTES_METODO = "Iniciando execucao de metodo %s.";

    /**
     * Template msg log.
     */
    private static final String MSG_LOG_APOS_METODO = "%d milisegundos foi o tempo execucao do metodo %s.";

    /**
     * Constante.
     */
    private static final String STRING_DESCONHECIDO = "DESCONHECIDO";

    @AroundInvoke
    public Object aroundInvoke(final InvocationContext ic) throws Exception {
        final long tempoInicio = System.currentTimeMillis();
        try {
            logarInicioExecucao(ic);
            return ic.proceed();
        } finally {
            logarFimExecucao(ic, tempoInicio);
        }
    }

    private void logarFimExecucao(final InvocationContext ic, final long tempoInicio) {
        final long tempoFim = System.currentTimeMillis();
        final long duracaoExecucao = tempoFim - tempoInicio;
        LOGGER.info(String.format(MSG_LOG_APOS_METODO, duracaoExecucao, getNomeMetodo(ic)));
    }

    private void logarInicioExecucao(final InvocationContext ic) {
        LOGGER.info(String.format(MSG_LOG_ANTES_METODO, getNomeMetodo(ic)));
    }

    private String getNomeMetodo(final InvocationContext ctx) {
        if (ctx == null) {
            return STRING_DESCONHECIDO;
        }

        final Method method = ctx.getMethod();
        if (method == null) {
            return STRING_DESCONHECIDO;
        }

        return method.getName();
    }
}
