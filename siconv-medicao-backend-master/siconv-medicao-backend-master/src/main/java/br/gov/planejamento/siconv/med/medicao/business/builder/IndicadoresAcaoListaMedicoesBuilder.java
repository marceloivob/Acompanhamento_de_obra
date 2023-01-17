package br.gov.planejamento.siconv.med.medicao.business.builder;

import static br.gov.planejamento.siconv.med.medicao.business.builder.util.MedicaoCollector.ultimaMedicao;
import static br.gov.planejamento.siconv.med.medicao.business.builder.util.MedicaoCollector.ultimaMedicaoPorSituacao;
import static br.gov.planejamento.siconv.med.medicao.business.builder.util.MedicaoPredicate.medicaoEnviadaConvenenteNaoAcumulada;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.AC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ACT;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.AT;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ATD;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.CC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.EC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ECC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ECE;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.EM;
import static java.util.Arrays.asList;

import java.util.List;

import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;

public class IndicadoresAcaoListaMedicoesBuilder {

    private List<MedicaoDTO> listaMedicoes;

    private IndicadoresAcaoListaMedicoesBuilder(List<MedicaoDTO> listaMedicoes) {
        this.listaMedicoes = listaMedicoes;
    }

    public void build() {
        configurarPermiteCancelar();
        configurarPermiteIniciar();
        configurarPermiteExcluir();
    }

    private void configurarPermiteCancelar() {

        listaMedicoes.stream().collect(ultimaMedicaoPorSituacao()).forEach((situacao, ultimaMedicao) -> {

            if (situacao == EC && !existeSituacao(EM) || situacao == ATD && !existeSituacao(AT)) {
                ultimaMedicao.get().setPermiteCancelarEnvio(true);
            }

            if (situacao == ECC && !existeSituacao(AC) || situacao == ECE && !existeSituacao(AT, CC, ECC)) {
                ultimaMedicao.get().setPermiteCancelarEnvioParaComplementacao(true);
            }

            if (situacao == ACT && !existeSituacao(AC) && !existeComplementacaoAnalise()) {
                ultimaMedicao.get().setPermiteCancelarAceite(true);
            }
        });
    }

    private void configurarPermiteIniciar() {

        listaMedicoes.stream().filter(medicaoEnviadaConvenenteNaoAcumulada()).forEach(medicaoEnviadaConvenente -> {

            if (!existeComplementacaoAnaliseAnteriorNaoAtestada(medicaoEnviadaConvenente)) {

                medicaoEnviadaConvenente.setPermiteIniciarAteste(true);
            }
        });
    }

    private void configurarPermiteExcluir() {

        listaMedicoes.stream().collect(ultimaMedicao())
                .ifPresent(ultimaMedicao -> ultimaMedicao.setPermiteExcluir(true));
    }

    private boolean existeSituacao(SituacaoMedicaoEnum... situacoes) {

        return listaMedicoes.stream().anyMatch(medicao -> asList(situacoes).contains(medicao.getSituacao()));
    }

    private boolean existeComplementacaoAnalise() {

        return listaMedicoes.stream().anyMatch(medicao -> medicao.getPermiteComplementacaoValor() != null);
    }

    private boolean existeComplementacaoAnaliseAnteriorNaoAtestada(MedicaoDTO medicao) {

        return listaMedicoes.stream()
                .anyMatch(anterior -> anterior.getSequencial() < medicao.getSequencial()
                        && !anterior.isAcumulada()
                        && anterior.getPermiteComplementacaoValor() != null // complementacao gerada na analise
                        && anterior.getSituacao() != ATD);
    }

    public static IndicadoresAcaoListaMedicoesBuilder of(List<MedicaoDTO> listaMedicoes) {
        return new IndicadoresAcaoListaMedicoesBuilder(listaMedicoes);
    }
}
