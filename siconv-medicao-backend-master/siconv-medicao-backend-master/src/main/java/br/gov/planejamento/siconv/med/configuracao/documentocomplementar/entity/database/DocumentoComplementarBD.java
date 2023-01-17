package br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.database;

import java.time.LocalDate;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.TipoDocumentoEnum;
import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.TipoManifestoEnum;
import lombok.Data;

@Data
public class DocumentoComplementarBD {

	@ColumnName("id")
	private Long id;

	@ColumnName("in_tipo_documento")
	private TipoDocumentoEnum tipoDocumento;

	@ColumnName("in_tipo_manifesto")
	private TipoManifestoEnum tipoManifestoAmbiental;

	@ColumnName("dt_emissao")
	private LocalDate dtEmissao;

	@ColumnName("dt_validade")
	private LocalDate dtValidade;

	@ColumnName("nr_documento")
	private String nrDocumento;

	@ColumnName("tx_descricao")
	private String txDescricao;

	@ColumnName("nm_orgao_emissor")
	private String nmOrgaoEmissor;

	@ColumnName("nm_arquivo")
	private String nmArquivo;

	@ColumnName("co_ceph")
	private String coCeph;

	@ColumnName("in_bloqueio")
	private boolean bloqueado;

	@ColumnName("med_contrato_fk")
	private Long medContratoFk;

	@ColumnName("versao")
	private Long versao;
	
	@ColumnName("tx_descricao_outros")
	private String txDescricaoOutros;

	@ColumnName("in_eq_lic_inst")
	private Boolean eqLicencaInstalacao;

}
