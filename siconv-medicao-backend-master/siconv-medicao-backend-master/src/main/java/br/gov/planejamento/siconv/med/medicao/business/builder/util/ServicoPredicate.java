package br.gov.planejamento.siconv.med.medicao.business.builder.util;

import static br.gov.planejamento.siconv.med.infra.util.MathUtil.is;

import java.util.function.Predicate;

import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoVrplDTO;

/**
 * Implementações de funcões {@link Predicate} para objetos
 * {@link ServicoVrplDTO} que são úteis para aplicar filtros em Collections ou
 * Streams.
 */
public final class ServicoPredicate {

    private ServicoPredicate() {
    }

    public static Predicate<ServicoVrplDTO> servicoPendentePreenchimentoEmpresa() {
        return servico -> !is(servico.getQtd()).equalTo(servico.getQtdAcumuladoEmpresa());
    }

    public static Predicate<ServicoVrplDTO> servicoPreenchidoEmpresaMedicaoAtual() {
        return servico -> servico.getQtdRealizadoEmpresa() != null;
    }

    public static Predicate<ServicoVrplDTO> servicoPendentePreenchimentoConvenente() {
        return servico -> !is(servico.getQtdAcumuladoEmpresa()).equalTo(servico.getQtdAcumuladoConvenente());
    }

    public static Predicate<ServicoVrplDTO> servicoPreenchidoConvenenteMedicaoAtual() {
        return servico -> servico.getQtdRealizadoConvenente() != null;
    }

    public static Predicate<ServicoVrplDTO> servicoPendentePreenchimentoConcedente() {
        return servico -> !is(servico.getQtdAcumuladoConvenente()).equalTo(servico.getQtdAcumuladoConcedente());
    }

    public static Predicate<ServicoVrplDTO> servicoPreenchidoConcedenteMedicaoAtual() {
        return servico -> servico.getQtdRealizadoConcedente() != null;
    }
}
