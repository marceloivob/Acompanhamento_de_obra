package br.gov.planejamento.siconv.med.infra.database;

import java.lang.reflect.Method;

import javax.enterprise.context.Dependent;

import org.jdbi.v3.sqlobject.Handler;
import org.jdbi.v3.sqlobject.HandlerDecorator;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

@Dependent
public class SiconvHandlerDecorator implements HandlerDecorator {

    @Override
    public Handler decorateHandler(Handler base, Class<?> sqlObjectType, Method method) {

        if (method.isAnnotationPresent(SqlQuery.class)) {
            return base;
        }

        return (target, args, supplier) -> supplier.getHandle().inTransaction(t -> base.invoke(target, args, supplier));
    }
}
