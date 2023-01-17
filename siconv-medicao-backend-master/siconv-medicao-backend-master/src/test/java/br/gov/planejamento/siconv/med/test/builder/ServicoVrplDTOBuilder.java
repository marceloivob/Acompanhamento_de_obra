package br.gov.planejamento.siconv.med.test.builder;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoVrplDTO.ValorServicoBM;

public class ServicoVrplDTOBuilder {

    private ServicoVrplDTO servico = new ServicoVrplDTO();

    private Map<Long, ValorServicoBM> valores = new HashMap<>();

    public ServicoVrplDTOBuilder comQtdPlanejada(BigDecimal qtd) {
        servico.setQtd(qtd);
        return this;
    }

    public ServicoVrplDTOBuilder comPreco(BigDecimal preco) {
        servico.setPreco(preco);
        return this;
    }

    public ServicoVrplDTOBuilder comPreenchimentoMedicao(Long idMedicao, BigDecimal qtdEmpresa,
            BigDecimal qtdConvenente, BigDecimal qtdConcedente) {
        valores.put(idMedicao, new ValorServicoBM(qtdEmpresa, qtdConvenente, qtdConcedente));
        return this;
    }

    public ServicoVrplDTO build() {
        servico.setValoresPorIdMedicao(valores);
        return servico;
    }
}