package br.gov.planejamento.siconv.med.medicao.entity.dto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import lombok.Data;

@Data
public class ServicoVrplDTO {

    private Long id;

    private Integer numero;

    private String descricao;

    private BigDecimal preco;

    private BigDecimal qtd;

    private String sgUnidade;

    private BigDecimal vlTotalServico;

    private BigDecimal qtdRealizadoEmpresa;
    private BigDecimal valorRealizadoEmpresa;
    private BigDecimal qtdAcumuladoEmpresa;
    private BigDecimal valorAcumuladoEmpresa;

    private BigDecimal qtdRealizadoConvenente;
    private BigDecimal valorRealizadoConvenente;
    private BigDecimal qtdAcumuladoConvenente;
    private BigDecimal valorAcumuladoConvenente;

    private BigDecimal qtdRealizadoConcedente;
    private BigDecimal valorRealizadoConcedente;
    private BigDecimal qtdAcumuladoConcedente;
    private BigDecimal valorAcumuladoConcedente;

    private boolean permiteMedicao = false;
    private BigDecimal qtdMaxPermitido;

    private boolean possuiGlosasAnterioresConvenente;
    private boolean possuiGlosasAnterioresConcedente;
    
    // Dados da tabela item_medicao_bm_vl
    private Map<Long, ValorServicoBM> valoresPorIdMedicao = new HashMap<>();

    @Data
    public static class ValorServicoBM {
        private BigDecimal qtdEmpresa;
        private BigDecimal qtdConvenente;
        private BigDecimal qtdConcedente;
        
        public ValorServicoBM(BigDecimal qtdEmpresa, 
				BigDecimal qtdConvenente, 
				BigDecimal qtdConcedente) {
			super();
			this.qtdEmpresa = qtdEmpresa;
			this.qtdConvenente = qtdConvenente;
			this.qtdConcedente = qtdConcedente;
		}
    }
    
    public ValorServicoBM obterValorMedicao (Long id) {
    	
    	if (valoresPorIdMedicao != null ) {
    		return valoresPorIdMedicao.get(id);
    	} else {
    		return null;
    	}
    }

    public BigDecimal getQtdAcumuladoEmpresaTodasMedicoes() {
        return getValoresPorIdMedicao().values().stream().map(ValorServicoBM::getQtdEmpresa).filter(Objects::nonNull)
                .reduce(BigDecimal::add).orElse(null);
    }
    
    public BigDecimal getQtdAcumuladoConvenenteTodasMedicoes() {
    	return getValoresPorIdMedicao().values().stream().map(ValorServicoBM::getQtdConvenente).filter(Objects::nonNull)
    			.reduce(BigDecimal::add).orElse(null);
	}

}
