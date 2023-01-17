package br.gov.planejamento.siconv.med.configuracao.paralisacao.dao;

import java.sql.Types;

import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

import br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.dto.IndicativoParalisacaoEnum;

public class IndicativoParalisacaoArgumentFactory extends AbstractArgumentFactory<IndicativoParalisacaoEnum> {

    public IndicativoParalisacaoArgumentFactory() {
        super(Types.INTEGER);
    }

    @Override
    protected Argument build(IndicativoParalisacaoEnum indicativo, ConfigRegistry config) {
        return (position, statement, ctx) -> statement.setInt(position, indicativo.getCodigo());
    }
}
