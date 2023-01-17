package br.gov.planejamento.siconv.med.infra.database;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jdbi.v3.core.Jdbi;

@ApplicationScoped
public class DAOFactory {

    @Inject
	private Jdbi jdbi;

    public <T> T get(Class<T> clazz) {
    	return jdbi.onDemand(clazz);
    }

    public Jdbi getJdbi() {
        return jdbi;
    }

    public void setJdbi(Jdbi jdbi) {
        this.jdbi = jdbi;
    }
    
}
