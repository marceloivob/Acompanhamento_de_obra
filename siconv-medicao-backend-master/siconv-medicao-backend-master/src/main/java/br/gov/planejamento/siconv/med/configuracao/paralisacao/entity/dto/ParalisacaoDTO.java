package br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.dto;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.database.ParalisacaoBD;
import br.gov.planejamento.siconv.med.infra.validation.UpdateGroup;
import lombok.Data;

@Data
public class ParalisacaoDTO {

	@NotNull(groups = { UpdateGroup.class })
	@ColumnName("id")
	private Long id;

	@NotNull
	@PastOrPresent
	@ColumnName("dt_inicio")
	private LocalDate dataInicio;

	@PastOrPresent
	@ColumnName("dt_fim")
	private LocalDate dataFim;

	@NotNull
	@ColumnName("in_responsavel")
	private ResponsavelParalisacaoEnum responsavel;

	@NotNull
	@ColumnName("in_indicativo")
	private IndicativoParalisacaoEnum indicativo;

	@NotNull
	@ColumnName("in_motivo")
	private MotivoParalisacaoEnum motivo;

	@NotEmpty
	@Size(min = 1, max = 1000)
	@ColumnName("tx_observacao")
	private String observacao;

	@ColumnName("med_contrato_fk")
	@JsonProperty(access = READ_ONLY)
	private Long medContratoFk;

	@ColumnName("id_contrato_siconv")
	@JsonProperty(access = READ_ONLY)
	private Long idContratoSiconv;

	@NotNull(groups = { UpdateGroup.class })
	@ColumnName("versao")
	@JsonInclude(value = Include.NON_NULL)
	private Long versao;

	@JsonProperty(access = READ_ONLY)
	@Valid
	private List<AnexoParalisacaoDTO> anexos = new ArrayList<>();

	public AnexoParalisacaoDTO addAnexos(AnexoParalisacaoDTO anexoParalisacaoDTO) {
		int pos = this.anexos.indexOf(anexoParalisacaoDTO);
		if (pos == -1) {
			this.anexos.add(anexoParalisacaoDTO);
			return anexoParalisacaoDTO;
		}

		return this.anexos.get(pos);
	}

	public ParalisacaoBD converterParaBD() {

		ParalisacaoBD paralisacaoBD = new ParalisacaoBD();

		paralisacaoBD.setId(this.id);
		paralisacaoBD.setDtInicio(this.dataInicio);
		paralisacaoBD.setDtFim(this.dataFim);
		paralisacaoBD.setTxObservacao(this.observacao);
		paralisacaoBD.setInResponsavel(this.responsavel);
		paralisacaoBD.setInIndicativo(this.indicativo);
		paralisacaoBD.setInMotivo(this.motivo);
		paralisacaoBD.setMedContratoFk(this.medContratoFk);
		paralisacaoBD.setVersao(this.versao);
		
		return paralisacaoBD;
	}

}
