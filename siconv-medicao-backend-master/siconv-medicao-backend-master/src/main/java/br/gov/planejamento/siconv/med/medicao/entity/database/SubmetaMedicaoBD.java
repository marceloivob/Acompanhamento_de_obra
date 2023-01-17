package br.gov.planejamento.siconv.med.medicao.entity.database;

import java.time.Instant;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import br.gov.planejamento.siconv.med.medicao.entity.SituacaoSubmetaEnum;
import lombok.Data;

@Data
public class SubmetaMedicaoBD {
		@ColumnName("id")
		public Long idSubmetaMedicao; 
	
		@ColumnName("vrpl_submeta_fk")
		public Long idSubmetaVrpl;
	
		@ColumnName("medicao_fk")
		public Long idMedicao;
	
		@ColumnName("in_situacao_empresa")
		public SituacaoSubmetaEnum situacaoEmpresa; 
	
		@ColumnName("nr_cpf_resp_empresa")
		public String nrCpfResponsavelAssinaturaEmpresa;
	
		@ColumnName("dt_assinatura_empresa")
		public Instant dtAssinaturaEmpresa;
		
		@ColumnName("in_situacao_convenente")
		public SituacaoSubmetaEnum situacaoConvenente; 

		@ColumnName("nr_cpf_resp_convenente")
		public String nrCpfResponsavelAssinaturaConvenente;
	
		@ColumnName("dt_assinatura_convenente")
		public Instant dtAssinaturaConvenente;
		
		@ColumnName("in_situacao_concedente")
		public SituacaoSubmetaEnum situacaoConcedente; 

		@ColumnName("nr_cpf_resp_concedente")
		public String nrCpfResponsavelAssinaturaConcedente;
	
		@ColumnName("dt_assinatura_concedente")
		public Instant dtAssinaturaConcedente;
		
		@ColumnName("in_perfil_resp_concedente")
		public String inPerfilRespConcedente;
		
		@ColumnName("versao")
		private Long versao;

}
