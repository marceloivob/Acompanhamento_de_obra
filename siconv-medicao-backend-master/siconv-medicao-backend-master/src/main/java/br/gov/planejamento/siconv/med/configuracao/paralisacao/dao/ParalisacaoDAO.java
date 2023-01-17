package br.gov.planejamento.siconv.med.configuracao.paralisacao.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.jdbi.v3.sqlobject.config.RegisterArgumentFactory;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.statement.UseRowReducer;

import br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.database.ParalisacaoBD;
import br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.dto.AnexoParalisacaoDTO;
import br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.dto.ParalisacaoDTO;

public interface ParalisacaoDAO {

	@SqlQuery(" select paralisa.id as paralisa_id, " +
			"       paralisa.dt_inicio as paralisa_dt_inicio, " +
			"       paralisa.dt_fim as paralisa_dt_fim, " +
			"       paralisa.in_responsavel as paralisa_in_responsavel, " +
			"       paralisa.in_indicativo as paralisa_in_indicativo, " +
			"       paralisa.in_motivo as paralisa_in_motivo, " +
			"       paralisa.tx_observacao as paralisa_tx_observacao, " +
			"       paralisa.med_contrato_fk as paralisa_med_contrato_fk, " +
			"       crt.contrato_fk as paralisa_id_contrato_siconv, " +
			"       anexo.id as anexo_id, " +
			"       anexo.nm_arquivo as anexo_nm_arquivo, " +
			"       anexo.co_ceph as anexo_co_ceph, " +
			"       anexo.paralisacao_fk as anexo_paralisacao_fk " +
			" FROM med_paralisacao paralisa " +
			"      join med_contrato crt on (paralisa.med_contrato_fk = crt.id)" +
			"      left join med_anexo_paralisacao anexo on paralisa.id = anexo.paralisacao_fk " +
			" where crt.contrato_fk = :idContratoSiconv " +
			" order by paralisa_dt_inicio desc ")
	@RegisterFieldMapper(value = ParalisacaoDTO.class, prefix = "paralisa")
	@RegisterFieldMapper(value = AnexoParalisacaoDTO.class, prefix = "anexo")
	@RegisterColumnMapper(IndicativoParalisacaoColumnMapper.class)
	@RegisterColumnMapper(MotivoParalisacaoColumnMapper.class)
	@UseRowReducer(ParalisacaoReducer.class)
	List<ParalisacaoDTO> listarParalisacoes(@Bind("idContratoSiconv") Long idContratoSiconv);

	@SqlQuery(" select paralisa.id as paralisa_id, " +
			"       paralisa.dt_inicio as paralisa_dt_inicio, " +
			"       paralisa.dt_fim as paralisa_dt_fim, " +
			"       paralisa.in_responsavel as paralisa_in_responsavel, " +
			"       paralisa.in_indicativo as paralisa_in_indicativo, " +
			"       paralisa.in_motivo as paralisa_in_motivo, " +
			"       paralisa.tx_observacao as paralisa_tx_observacao, " +
			"       paralisa.med_contrato_fk as paralisa_med_contrato_fk, " +
			"       crt.contrato_fk as paralisa_id_contrato_siconv, " +
			"       paralisa.versao as paralisa_versao, " +
			"       anexo.id as anexo_id, " +
			"       anexo.nm_arquivo as anexo_nm_arquivo, " +
			"       anexo.co_ceph as anexo_co_ceph, " +
			"       anexo.paralisacao_fk as anexo_paralisacao_fk " +
			" from med_paralisacao paralisa " +
			"      join med_contrato crt on (paralisa.med_contrato_fk = crt.id) " +
			"      left join med_anexo_paralisacao anexo on paralisa.id = anexo.paralisacao_fk " +
			" where paralisa.id = :idParalisacao ")
	@RegisterFieldMapper(value = ParalisacaoDTO.class, prefix = "paralisa")
	@RegisterFieldMapper(value = AnexoParalisacaoDTO.class, prefix = "anexo")
	@RegisterColumnMapper(IndicativoParalisacaoColumnMapper.class)
	@RegisterColumnMapper(MotivoParalisacaoColumnMapper.class)
	@UseRowReducer(ParalisacaoReducer.class)
	Optional<ParalisacaoDTO> consultarParalisacao(@Bind("idParalisacao") Long idParalisacao);

	@SqlQuery(" SELECT DISTINCT ON (mp.med_contrato_fk) mp.*"
			+ " FROM med_paralisacao mp"
			+ " INNER JOIN med_contrato mc ON mc.id = mp.med_contrato_fk"
			+ " WHERE mc.contrato_fk = :idContratoSiconv"
			+ " ORDER BY mp.med_contrato_fk, mp.dt_inicio DESC")
	@RegisterFieldMapper(ParalisacaoBD.class)
	@RegisterColumnMapper(IndicativoParalisacaoColumnMapper.class)
	@RegisterColumnMapper(MotivoParalisacaoColumnMapper.class)
	ParalisacaoBD consultarUltimaParalisacao(@Bind("idContratoSiconv") Long idContratoSiconv);

	@SqlQuery(" SELECT DISTINCT ON (mp.med_contrato_fk) mp.*"
			+ " FROM med_paralisacao mp"
			+ " WHERE mp.med_contrato_fk = :idContratoMedicao"
			+ "   AND mp.dt_inicio < :dataReferencia"
			+ " ORDER BY mp.med_contrato_fk, mp.dt_inicio DESC")
	@RegisterFieldMapper(ParalisacaoBD.class)
	@RegisterColumnMapper(IndicativoParalisacaoColumnMapper.class)
	@RegisterColumnMapper(MotivoParalisacaoColumnMapper.class)
	ParalisacaoBD consultarParalisacaoAnterior(@Bind("idContratoMedicao") Long idContratoMedicao,
			@Bind("dataReferencia") LocalDate dataReferencia);

	@SqlUpdate("DELETE FROM siconv.med_paralisacao WHERE id = :idParalisacao")
	void excluirParalisacaoPorId(@Bind("idParalisacao") Long idParalisacao);

	@SqlQuery(" SELECT"
			+ "     COUNT(id) > 0"
			+ " FROM"
			+ "     siconv.med_paralisacao"
			+ " WHERE"
			+ "     med_contrato_fk = ("
			+ "         SELECT id FROM med_contrato "
			+ "         WHERE contrato_fk = :idContratoSiconv)"
			+ " AND dt_fim is null")
	boolean existeParalisacaoEmAberto(@Bind("idContratoSiconv") Long idContratoSiconv);

	@SqlUpdate("INSERT INTO siconv.med_paralisacao "
			+ "(med_contrato_fk, dt_inicio, dt_fim, tx_observacao, in_responsavel, in_indicativo, in_motivo, versao, adt_login, adt_data_hora, adt_operacao)"
			+ " VALUES (:medContratoFk, :dtInicio, :dtFim, :txObservacao, :inResponsavel, :inIndicativo, :inMotivo, 1, current_setting('med.cpf_usuario'), LOCALTIMESTAMP, 'INSERT')")
	@GetGeneratedKeys
	@RegisterArgumentFactory(IndicativoParalisacaoArgumentFactory.class)
	@RegisterArgumentFactory(MotivoParalisacaoArgumentFactory.class)
	Long inserirParalisacao(@BindBean ParalisacaoBD paralisacao);

	@SqlQuery(" SELECT"
            + "     COUNT(id) > 0 "
            + " FROM"
            + "     siconv.med_paralisacao "
            + " WHERE"
            + "     med_contrato_fk = :idContratoMedicao ")
	boolean existeParalisacaoContrato(@Bind("idContratoMedicao") Long idContratoMedicao);

	@SqlUpdate(" UPDATE siconv.med_paralisacao"
			+ " SET dt_inicio = :dtInicio,"
			+ "     dt_fim = :dtFim,"
			+ "     tx_observacao = :txObservacao,"
			+ "     in_responsavel = :inResponsavel,"
			+ "     in_indicativo = :inIndicativo,"
			+ "     in_motivo = :inMotivo,"
            + "     versao = :versao + 1,"
			+ "     adt_login = current_setting('med.cpf_usuario'),"
			+ "     adt_data_hora = LOCALTIMESTAMP,"
			+ "     adt_operacao = 'UPDATE'"
			+ " WHERE id = :id")
	@RegisterArgumentFactory(IndicativoParalisacaoArgumentFactory.class)
	@RegisterArgumentFactory(MotivoParalisacaoArgumentFactory.class)
	boolean alterarParalisacao(@BindBean ParalisacaoBD paralisacao);
}
