package br.gov.planejamento.siconv.med.contrato.entity.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.gov.planejamento.siconv.med.contrato.entity.ModalidadeEnum;
import lombok.Data;

@Data
public class ContratoSiconvDTO {

    private Long id;
    private String cnpj;
    private String numeroContrato;
    private String dtInicioVigencia;
    private LocalDate dtFimVigencia;
    private LocalDate dtAssinatura;
    private String nomeObjetoContratoFornecimento;
    private String valorContrato;
    private Integer numeroConvenioRepasse;
    private Integer anoConvenioRepasse;
    private String localidade;
    private String nomeObjetoContratoRepasse;
    private ModalidadeEnum modalidade;
    private Long fornecedorId;
    private String urlSiconvMedicao;
    private Boolean inSocial;
    private Boolean inAcompEvento;
    private Long propostaFk;
    private LocalDate dtAssinaturaTipoInstrumento;
    private int qtdeMedicoes;
    private Boolean isConfiguradoParaMedicao;
    private BigDecimal valorTipoInstrumento;
    private Long qtdeDiasSemMedicao;
    private Boolean inContratoAtrasado;
    private Boolean inContratoParalisado;
    private String nomeConvenente;

    public String getDescricaoModalidade() {
        if (this.modalidade == null) {
            return "";
        }

        return this.modalidade.getDescricao();
    }
}
