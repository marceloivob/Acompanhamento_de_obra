package br.gov.planejamento.siconv.med.medicao.business.builder.util;

import java.util.List;
import java.util.function.Predicate;

import br.gov.planejamento.siconv.med.medicao.entity.dto.EventoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;

/**
 * Implementações de funcões {@link Predicate} para objetos {@link EventoVrplDTO} que
 * são úteis para aplicar filtros em Collections ou Streams.
 * <p>
 * Exemplo:
 * 
 * <pre>{@code 
 * boolean existeMarcacaoPendenteConvenente = 
 *   eventos.stream().anyMatch(
 *     eventoConcluidoEmpresa(medicao).and(
 *       not(eventoConcluidoConvenente(medicao))));
 * }</pre>
 */
public final class EventoPredicate {

    private EventoPredicate() { }

    public static Predicate<EventoVrplDTO> eventoConcluidoEmpresa(MedicaoDTO medicao) {
        return evento -> evento.getIdMedicaoEmpresa() != null
                && evento.getIdMedicaoEmpresa().longValue() <= medicao.getId().longValue();
    }

    public static Predicate<EventoVrplDTO> eventoConcluidoEmpresaMedicaoAtual(MedicaoDTO medicao) {
        return evento -> evento.getIdMedicaoEmpresa() != null && evento.getIdMedicaoEmpresa().equals(medicao.getId());
    }

    public static Predicate<EventoVrplDTO> eventoConcluidoEmpresaMedicaoAcumulada(
            List<Long> listaIdMedicoesAcumuladas) {
        return evento -> evento.getIdMedicaoEmpresa() != null
                && listaIdMedicoesAcumuladas.contains(evento.getIdMedicaoEmpresa());
    }

    public static Predicate<EventoVrplDTO> eventoConcluidoConvenente(MedicaoDTO medicao) {
        return evento -> evento.getIdMedicaoConvenente() != null
                && evento.getIdMedicaoConvenente().longValue() <= medicao.getId().longValue();
    }

    public static Predicate<EventoVrplDTO> eventoConcluidoConvenenteMedicaoAtual(MedicaoDTO medicao) {
        return evento -> evento.getIdMedicaoConvenente() != null
                && evento.getIdMedicaoConvenente().equals(medicao.getId());
    }

    public static Predicate<EventoVrplDTO> eventoConcluidoConvenenteMedicaoAcumulada(
            List<Long> listaIdMedicoesAcumuladas) {
        return evento -> evento.getIdMedicaoConvenente() != null
                && listaIdMedicoesAcumuladas.contains(evento.getIdMedicaoConvenente());
    }

    public static Predicate<EventoVrplDTO> eventoConcluidoConcedente(MedicaoDTO medicao) {
        return evento -> evento.getIdMedicaoConcedente() != null
                && evento.getIdMedicaoConcedente().longValue() <= medicao.getId().longValue();
    }

    public static Predicate<EventoVrplDTO> eventoConcluidoConcedenteMedicaoAtual(MedicaoDTO medicao) {
        return evento -> evento.getIdMedicaoConcedente() != null
                && evento.getIdMedicaoConcedente().equals(medicao.getId());
    }
}
