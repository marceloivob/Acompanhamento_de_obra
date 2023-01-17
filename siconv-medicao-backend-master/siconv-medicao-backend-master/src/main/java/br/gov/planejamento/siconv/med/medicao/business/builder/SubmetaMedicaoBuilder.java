package br.gov.planejamento.siconv.med.medicao.business.builder;

import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.infra.database.DAOFactory;
import br.gov.planejamento.siconv.med.integration.contratos.ContratosGrpcConsumer;
import br.gov.planejamento.siconv.med.medicao.dao.MedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.SubmetaDAO;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.database.SubmetaMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
public class SubmetaMedicaoBuilder {

    public interface Step {
        void process(SubmetaMedicaoDTO dto, Context builderContext);
    }

    public interface Context {
        ContratoBD getContrato();

        MedicaoDTO getMedicao();

        Map<Long, SubmetaVrplDTO> getCacheSubmetasContrato();

        Map<Long, SituacaoMedicaoEnum> getCacheSituacaoMedicao();

        List<SubmetaMedicaoBD> getCacheSubmetaMedicaoBD();

        List<Long> getCacheIdMedicoesAcumuladas();

        Map<Long, Boolean> getCacheIndComplementacaoValor();
    }

    @RequiredArgsConstructor
    public static class Pipeline {

        private final List<Step> steps = new ArrayList<>();

        private final Context context;

        private boolean flag = true;

        public <T extends Step> Pipeline add(Class<T> stepClass) {
            return add(CDI.current().select(stepClass).get());
        }

        public Pipeline add(Step step) {
            if (flag) {
                steps.add(step);
            }
            return this;
        }

        public void build(SubmetaMedicaoDTO dto) {
            steps.forEach(step -> step.process(dto, context));
        }

        public void build(List<SubmetaMedicaoDTO> list) {
            list.forEach(this::build);
        }

        public Pipeline when(boolean condition) {
            this.flag = condition;
            return this;
        }

        public Pipeline orElse() {
            flag = !flag;
            return this;
        }

        public Pipeline anyway() {
            flag = true;
            return this;
        }
    }

    @Inject
    private DAOFactory dao;

    @Inject
    private ContratosGrpcConsumer contratosConsumer;

    public Pipeline of(ContratoBD contrato, MedicaoDTO medicao) {
        return new Pipeline(new LazyContext(contrato, medicao));
    }

    private class LazyContext implements Context {

        @Getter
        private final ContratoBD contrato;

        @Getter
        private final MedicaoDTO medicao;

        @Getter(lazy = true)
        private final Map<Long, SubmetaVrplDTO> cacheSubmetasContrato = listarSubmetasPorContratoId();

        @Getter(lazy = true)
        private final Map<Long, SituacaoMedicaoEnum> cacheSituacaoMedicao = listarSituacoesMedicoes();

        @Getter(lazy = true)
        private final List<SubmetaMedicaoBD> cacheSubmetaMedicaoBD = consultarSubmetasMedicaoPorContrato();

        @Getter(lazy = true)
        private final List<Long> cacheIdMedicoesAcumuladas = listarIdMedicoesAcumuladas();

        @Getter(lazy = true)
        private final Map<Long, Boolean> cacheIndComplementacaoValor = listarIndicadorComplementacaoValorMedicoes();

        public LazyContext(ContratoBD contrato, MedicaoDTO medicao) {
            this.contrato = contrato;
            this.medicao = medicao;
        }

        private Map<Long, SubmetaVrplDTO> listarSubmetasPorContratoId() {
            return contratosConsumer.listarSubmetasPorContratoId(contrato.getContratoFk()).stream()
                    .collect(toMap(SubmetaVrplDTO::getId, Function.identity()));
        }

        private Map<Long, SituacaoMedicaoEnum> listarSituacoesMedicoes() {
            return dao.get(MedicaoDAO.class).listarSituacoesMedicoes(medicao.getIdContrato());
        }

        private List<SubmetaMedicaoBD> consultarSubmetasMedicaoPorContrato() {
            return dao.get(SubmetaDAO.class).consultarSubmetasMedicaoPorContrato(medicao.getIdContrato());
        }

        private List<Long> listarIdMedicoesAcumuladas() {
            return dao.get(MedicaoDAO.class).listarIdMedicoesAcumuladas(medicao.getId());
        }

        private Map<Long, Boolean> listarIndicadorComplementacaoValorMedicoes() {
            return dao.get(MedicaoDAO.class).listarIndicadorComplementacaoValorMedicoes(medicao.getIdContrato());
        }
    }
}
