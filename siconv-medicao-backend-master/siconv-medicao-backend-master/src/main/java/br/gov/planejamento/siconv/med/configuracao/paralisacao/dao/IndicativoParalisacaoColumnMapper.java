package br.gov.planejamento.siconv.med.configuracao.paralisacao.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

import br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.dto.IndicativoParalisacaoEnum;

public class IndicativoParalisacaoColumnMapper implements ColumnMapper<IndicativoParalisacaoEnum> {
	 
    @Override
    public IndicativoParalisacaoEnum map(ResultSet rs, int columnNumber, StatementContext ctx) throws SQLException {
        Integer codigo = rs.getInt(columnNumber);
        return IndicativoParalisacaoEnum.fromCodigo(codigo);
    }
}
