package br.gov.planejamento.siconv.med.contrato.entity;

import java.time.LocalDate;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import lombok.Data;

@Data
public class ContratoBD {

    @ColumnName("id")
    public Long id;

    @ColumnName("dt_inicio_obra")
    public LocalDate dataInicioObra;

    @ColumnName("contrato_fk")
    public Long contratoFk;

    @ColumnName("in_social")
    private boolean inSocial;

    @ColumnName("cnpj_fornecedor")
    private String cnpjFornecedor;

    @ColumnName("proposta_fk")
    private Long propostaFk;
    
    @ColumnName("in_acompanhamento_eventos")
    private boolean inAcompanhamentoEventos;

    public ContratoBD(Long contratoFk, boolean inSocial, String cnpjFornecedor, 
    		Long propostaFk, boolean inAcompanhamentoEventos) {
        super();
        this.contratoFk = contratoFk;
        this.inSocial = inSocial;
        this.cnpjFornecedor = cnpjFornecedor;
        this.propostaFk = propostaFk;
        this.inAcompanhamentoEventos = inAcompanhamentoEventos;
    }

    public ContratoBD() {
        super();
    }
}
