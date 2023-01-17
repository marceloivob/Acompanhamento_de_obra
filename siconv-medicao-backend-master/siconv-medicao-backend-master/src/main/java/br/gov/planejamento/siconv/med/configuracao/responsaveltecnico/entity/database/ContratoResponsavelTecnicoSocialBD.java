package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.database;

import java.time.LocalDateTime;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.AtividadeRegistroProfissionalEnum;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.TipoResponsavelTecnicoEnum;
import lombok.Data;

@Data
public class ContratoResponsavelTecnicoSocialBD {
	
	@ColumnName("id") // tabela interna - med_contrato_resp_tecnico_social
	private Long id;

	@ColumnName("med_contrato_fk") // tabela interna - med_contrato
	private Long contrato;

	@ColumnName("med_responsavel_tecnico_fk") // tabela interna - med_responsavel_tecnico
	private Long responsavelTecnico;
	
	@ColumnName("dt_inclusao")
	private LocalDateTime dataInclusao;

	@ColumnName("in_tipo")
	private TipoResponsavelTecnicoEnum tipo;
	
	@ColumnName("formacao")
	private String formacao;
	
	@ColumnName("nm_registro_profissional")
	private String registroProfissional;
	
	@ColumnName("atividade")
	private AtividadeRegistroProfissionalEnum atividade;
	
	@ColumnName("nm_arquivo_curriculo")
	private String nmArquivoCurriculo;
	
	@ColumnName("co_ceph_curriculo")
	private String coCephCurriculo;
	
	@ColumnName("nm_orgao_responsavel")
	private String nmOrgaoResponsavel;
	
	@ColumnName("nr_telefone_orgao")
	private String nrTelefoneOrgao;
	
	@ColumnName("tx_email_orgao")
	private String txEmailOrgao;
	
	@ColumnName("versao")
	private Long versao;
	
}
