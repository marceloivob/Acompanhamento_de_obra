package br.gov.planejamento.siconv.med.medicao.entity.dto;

import static br.gov.planejamento.siconv.med.infra.security.domain.SensitiveDataType.CPF;
import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import br.gov.planejamento.siconv.med.medicao.entity.database.AnexoBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.ObservacaoBD;
import br.gov.planejamento.siconv.med.infra.security.annotation.SensitiveData;
import lombok.Data;

@Data
public class ObservacaoDTO {

    @JsonProperty(access = READ_ONLY)
    private Long id;
	
	private Long versao;
	
	@JsonProperty(access = READ_ONLY)
	private Instant dtRegistro;

	@JsonProperty(access = READ_ONLY)
	private PerfilEnum inPerfilResponsavel;

	@JsonProperty(access = READ_ONLY)
	@SensitiveData(type = CPF)
	private String nrCpfResponsavel;
	
	@JsonProperty(access = READ_ONLY)
	private String nomeResponsavel;

	private String txObservacao;

	@JsonProperty(access = READ_ONLY)
	private Long medicaoFk;

	@JsonProperty(access = READ_ONLY)
	private List<AnexoDTO> anexos = new ArrayList<>();
	
	@JsonProperty(access = READ_ONLY)
	private Long idContratoSiconv;
	
	@JsonProperty(access = READ_ONLY)
	private Short sequencialMedicaoAgrupada;
	
	private boolean inBloqueio;
	
	public AnexoDTO addAnexos(AnexoDTO anexoDTO) {
		int pos = this.anexos.indexOf(anexoDTO);
		if (pos == -1) {
			this.anexos.add(anexoDTO);
			return anexoDTO;
		}

		return this.anexos.get(pos);
	}
	
	public ObservacaoBD converterParaBD() {

		ObservacaoBD observacaoBD = new ObservacaoBD();
		
		observacaoBD.setId(this.id);
		observacaoBD.setVersao(this.versao);
		observacaoBD.setDtRegistro(this.dtRegistro);
		observacaoBD.setInPerfilResponsavel(this.inPerfilResponsavel.name());
		observacaoBD.setNrCpfResponsavel(this.nrCpfResponsavel);
		observacaoBD.setTxObservacao(this.txObservacao);
		observacaoBD.setMedicaoFk(this.medicaoFk);
		observacaoBD.setInBloqueio(this.inBloqueio);
		
		List<AnexoBD> anexosBD = new ArrayList<>();
		
		for (AnexoDTO anexoDTO : this.anexos) {
			anexosBD.add(anexoDTO.converterParaBD());			
		}
		
		observacaoBD.setAnexos(anexosBD);

		return observacaoBD;

	}

}
