package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto;

import static br.gov.planejamento.siconv.med.infra.security.domain.SensitiveDataType.URL;
import static java.util.stream.Collectors.toSet;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.database.ContratoResponsavelTecnicoSocialBD;
import br.gov.planejamento.siconv.med.infra.security.annotation.SensitiveData;
import br.gov.planejamento.siconv.med.infra.validation.InsertGroup;
import br.gov.planejamento.siconv.med.infra.validation.UpdateGroup;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContratoResponsavelTecnicoSocialDTO {

	@NotNull(groups = UpdateGroup.class)
	@ColumnName("id")
	private Long id;
	
	@ColumnName("med_contrato_fk")
    public Long medContratoFk;
	
	@ColumnName("dt_inclusao")
	private LocalDateTime dtInclusao;

	@ColumnName("dt_inativacao")
	private LocalDateTime dtInativacao;

	@ColumnName("versao")
	private Long versao;

	private Long idContratoSiconv;
	
	@NotBlank
	@Size(max = 100)
	@ColumnName("nm_formacao")
	private String formacao;
	
	@Size(max = 100)
	@ColumnName("nm_registro_profissional")
	private String registroProfissional;
	
	@NotNull
	private ResponsavelTecnicoDTO responsavelTecnico;
	
	@NotEmpty
	private List<SubmetaVrplDTO> submetas = new ArrayList<>();

	
	public Set<Long> getIdSubmetas() {
		return submetas.stream().map(SubmetaVrplDTO::getId).collect(toSet());
	}
	
	
	public SubmetaVrplDTO addSubmetas(SubmetaVrplDTO submetaDTO) {
		int posicao = this.submetas.indexOf(submetaDTO);
		if (posicao == -1) {
			this.submetas.add(submetaDTO);
			return submetaDTO;
		}

		return this.submetas.get(posicao);
	}


    @NotNull
    @ColumnName("in_tipo")
	private TipoResponsavelTecnicoEnum tipo;
	
	@ColumnName("in_atividade")
	private AtividadeRegistroProfissionalEnum atividade;

	@JsonProperty(access = Access.READ_ONLY)
	@NotBlank(groups = InsertGroup.class)
	@Size(max = 100, message = "{err002}", groups = InsertGroup.class)
	@ColumnName("nm_arquivo_curriculo")
	private String nomeArquivo;
	
	@JsonIgnore
	@ColumnName("co_ceph_curriculo")
	private String codigoCephArquivo;
	
	@JsonIgnore
	@NotNull(groups = InsertGroup.class)
	private byte[] arquivo;
	
	@SensitiveData(type = URL)
	private String urlArquivo;
	
	@JsonInclude(value = Include.NON_NULL)
	@NotNull(groups = FiscalizacaoGroup.class)
	@Null(groups = ExecucaoGroup.class)
	@Valid
	private Orgao orgao;

	@Data
	public static class Orgao {
		
		@NotBlank(groups = FiscalizacaoGroup.class)
		@Size(max = 100, groups = FiscalizacaoGroup.class)
		@ColumnName("nm_orgao_responsavel")
		private String nome;
		
		@NotBlank(groups = FiscalizacaoGroup.class)
		@Pattern(regexp="(\\(\\d{2}\\)\\s?)(\\d{4,5}\\-\\d{4})", message = "{err073}", groups = FiscalizacaoGroup.class)
		@ColumnName("nr_telefone_orgao")
		private String telefone;
		
		@NotBlank(groups = FiscalizacaoGroup.class)
		@Size(max = 100, groups = FiscalizacaoGroup.class)
		@Email(groups = FiscalizacaoGroup.class)
		@ColumnName("tx_email_orgao")
		private String email;	
	}
	
	@ColumnName("possui_submeta_assinada")
	private boolean possuiSubmetaAssinada = false;
	
	public ContratoResponsavelTecnicoSocialBD converterDTOParaBD() {

		ContratoResponsavelTecnicoSocialBD contratoResponsavelTecnicoBD = new ContratoResponsavelTecnicoSocialBD();

		contratoResponsavelTecnicoBD.setId(this.id);
		contratoResponsavelTecnicoBD.setDataInclusao(this.dtInclusao);
		contratoResponsavelTecnicoBD.setFormacao(this.formacao);
		contratoResponsavelTecnicoBD.setRegistroProfissional(this.registroProfissional);
		
		contratoResponsavelTecnicoBD.setTipo(this.tipo);
		contratoResponsavelTecnicoBD.setAtividade(this.atividade);
		contratoResponsavelTecnicoBD.setNmArquivoCurriculo(this.nomeArquivo);
		contratoResponsavelTecnicoBD.setCoCephCurriculo(this.codigoCephArquivo);

		if (this.orgao != null) {
    		contratoResponsavelTecnicoBD.setNmOrgaoResponsavel(this.orgao.getNome());
    		contratoResponsavelTecnicoBD.setNrTelefoneOrgao(this.orgao.getTelefone());
    		contratoResponsavelTecnicoBD.setTxEmailOrgao(this.orgao.getEmail());
		}

		contratoResponsavelTecnicoBD.setResponsavelTecnico(this.responsavelTecnico.getId());
		contratoResponsavelTecnicoBD.setContrato(this.medContratoFk);
		contratoResponsavelTecnicoBD.setVersao(this.versao);

		return contratoResponsavelTecnicoBD;

	}
	
	public interface ExecucaoGroup {
	}
	
	public interface FiscalizacaoGroup {
	}
}
