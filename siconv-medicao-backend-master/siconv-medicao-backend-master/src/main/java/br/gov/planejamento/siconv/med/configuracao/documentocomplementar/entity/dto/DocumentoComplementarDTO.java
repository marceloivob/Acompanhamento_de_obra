package br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto;

import static java.util.stream.Collectors.toSet;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.database.DocumentoComplementarBD;
import br.gov.planejamento.siconv.med.infra.validation.InsertGroup;
import br.gov.planejamento.siconv.med.infra.validation.UpdateGroup;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentoComplementarDTO {

	@NotNull(groups = { UpdateGroup.class })
	@ColumnName("id")
	private Long id;

	@NotNull
	@ColumnName("in_tipo_documento")
	private TipoDocumentoEnum tipoDocumento;

	@ColumnName("in_tipo_manifesto")
	private TipoManifestoEnum tipoManifestoAmbiental;

	@ColumnName("dt_emissao")
	private LocalDate dtEmissao;

	@ColumnName("dt_validade")
	private LocalDate dtValidade;

	@ColumnName("nr_documento")
	@Size(max=40)
	private String nrDocumento;
	
	@ColumnName("tx_descricao")
	@Size(max=100)
	private String txDescricao;

	@ColumnName("nm_orgao_emissor")
	@Size(max=100)
	private String nmOrgaoEmissor;

	@NotEmpty(groups = { InsertGroup.class })
	@ColumnName("nm_arquivo")
	@Size(max=100, message = "{err002}")
	private String nmArquivo;

	@JsonIgnore
	@ColumnName("co_ceph")
	private String coCeph;

	@ColumnName("in_bloqueio")
	private boolean bloqueado;

	@ColumnName("versao")
	private Long versao;

	@ColumnName("contrato_fk")
	private Long idContratoSiconv;
	
	@ColumnName("tx_descricao_outros")
	@Size(max=200)
	private String txDescricaoOutros;

	@ColumnName("in_eq_lic_inst")
	private Boolean eqLicencaInstalacao;

	@JsonIgnore
	private byte[] arquivo;

	private String url;

	private Long medContratoFk;

	private Boolean possuiMedicao;

	private List<SubmetaVrplDTO> submetas = new ArrayList<>();

	public SubmetaVrplDTO addSubmetas(SubmetaVrplDTO submetaDTO) {
		int pos = this.submetas.indexOf(submetaDTO);
		if (pos == -1) {
			this.submetas.add(submetaDTO);
			return submetaDTO;
		}

		return this.submetas.get(pos);
	}

	@JsonIgnore
	public Set<Long> getIdSubmetas() {
		return submetas.stream().map(SubmetaVrplDTO::getId).collect(toSet());
	}

	public DocumentoComplementarBD converterParaBD() {

		DocumentoComplementarBD documentoComplementarBD = new DocumentoComplementarBD();

		documentoComplementarBD.setId(this.id);
		documentoComplementarBD.setTipoDocumento(this.tipoDocumento);
		documentoComplementarBD.setTipoManifestoAmbiental(this.tipoManifestoAmbiental);
		documentoComplementarBD.setDtEmissao(this.dtEmissao);
		documentoComplementarBD.setDtValidade(this.dtValidade);
		documentoComplementarBD.setNrDocumento(this.nrDocumento);
		documentoComplementarBD.setTxDescricao(this.txDescricao);
		documentoComplementarBD.setNmOrgaoEmissor(this.nmOrgaoEmissor);
		documentoComplementarBD.setNmArquivo(this.nmArquivo);
		documentoComplementarBD.setCoCeph(this.coCeph);
		documentoComplementarBD.setMedContratoFk(this.medContratoFk);
		documentoComplementarBD.setVersao(this.versao);
		documentoComplementarBD.setTxDescricaoOutros(this.txDescricaoOutros);
		if (this.eqLicencaInstalacao != null) {
			documentoComplementarBD.setEqLicencaInstalacao(this.eqLicencaInstalacao);
		}  else {
			documentoComplementarBD.setEqLicencaInstalacao(false);
		}

		return documentoComplementarBD;
	}

}
