package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto;

import static br.gov.planejamento.siconv.med.infra.security.domain.SensitiveDataType.URL;
import static java.util.stream.Collectors.toSet;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.database.AnotacaoRegistroRespTecnicoBD;
import br.gov.planejamento.siconv.med.infra.security.annotation.SensitiveData;
import br.gov.planejamento.siconv.med.infra.validation.InsertGroup;
import br.gov.planejamento.siconv.med.infra.validation.UpdateGroup;
import lombok.Data;

@Data
public class AnotacaoRegistroRespTecnicoDTO {

	@NotNull(groups = {UpdateGroup.class})
    private Long id;

	@NotBlank
	private String numero;
	
	@NotNull
	private LocalDate dataEmissao;

	@NotNull
	@ColumnName("in_tipo")
	private TipoResponsavelTecnicoEnum tipo;

	@JsonProperty(access = Access.READ_ONLY)
	private LocalDate dataInativacao;
	
	@NotEmpty(groups = {InsertGroup.class})
	@Size(max=100, message = "{err002}")
	private String nmArquivo;
	
	@JsonIgnore
	private String coCeph;

	private ResponsavelTecnicoDTO responsavelTecnico;	

	@NotNull
	private Long idMedContratoRespTec;

	@NotNull(groups = {InsertGroup.class})
	@JsonProperty(access = Access.WRITE_ONLY)
	private byte[] arquivo;

	@SensitiveData(type = URL)
	private String url;
	
	private Long versao;
	
	private Boolean possuiSubmetaAssinada;
	
	private Long idContratoSiconv;

	@JsonProperty(access = Access.READ_ONLY)
	public String getDescricaoTipo() {
		return this.getTipo().getDescricao();
	}

	@NotEmpty
	private List<SubmetaVrplDTO> submetas = new ArrayList<>();

	public SubmetaVrplDTO addSubmetas(SubmetaVrplDTO submetaDTO) {
		int pos = this.submetas.indexOf(submetaDTO);
		if (pos == -1) {
			this.submetas.add(submetaDTO);
			return submetaDTO;
		}

		return this.submetas.get(pos);
	}

    public Set<Long> getIdSubmetas() {
        return submetas.stream().map(SubmetaVrplDTO::getId).collect(toSet());
    }

	public AnotacaoRegistroRespTecnicoBD converterParaBD() {

		AnotacaoRegistroRespTecnicoBD anotacaoRegistroRespTecnicoBD = new AnotacaoRegistroRespTecnicoBD();

		anotacaoRegistroRespTecnicoBD.setId(this.id);
		anotacaoRegistroRespTecnicoBD.setNumero(this.numero);
		anotacaoRegistroRespTecnicoBD.setDataEmissao(this.dataEmissao);
		anotacaoRegistroRespTecnicoBD.setTipo(this.tipo);
		anotacaoRegistroRespTecnicoBD.setDataInativacao(this.dataInativacao);
		anotacaoRegistroRespTecnicoBD.setNmArquivo(this.nmArquivo);
		anotacaoRegistroRespTecnicoBD.setCoCeph(this.coCeph);
        anotacaoRegistroRespTecnicoBD.setIdMedContratoRespTec(this.getIdMedContratoRespTec());
        anotacaoRegistroRespTecnicoBD.setVersao(this.versao);

		return anotacaoRegistroRespTecnicoBD;
	}
}