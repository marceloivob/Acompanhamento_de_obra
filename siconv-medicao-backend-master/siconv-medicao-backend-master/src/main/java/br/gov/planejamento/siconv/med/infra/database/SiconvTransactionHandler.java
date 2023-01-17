package br.gov.planejamento.siconv.med.infra.database;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;

import org.apache.log4j.Logger;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.transaction.LocalTransactionHandler;

import br.gov.planejamento.siconv.med.infra.security.SecurityContext;

@ApplicationScoped
public class SiconvTransactionHandler extends LocalTransactionHandler {

    private static final Logger LOGGER = Logger.getLogger(SiconvTransactionHandler.class);

    @Override
    public void begin(Handle handle) {

        super.begin(handle);

        LOGGER.debug("----------- Inicio do callback de SET de Variável de usuário na Transação-------------------");

        SecurityContext securityContext = CDI.current().select(SecurityContext.class).get();

        handle.attach(CacheDAO.class).definirUsuarioLogado(securityContext.getUser().getCpf());

        LOGGER.debug("----------- Fim do callback de SET de Variável de usuário na Transação-------------------");
    }
}
