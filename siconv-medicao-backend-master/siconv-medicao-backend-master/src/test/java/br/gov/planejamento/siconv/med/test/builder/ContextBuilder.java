package br.gov.planejamento.siconv.med.test.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Context;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.database.SubmetaMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;
import lombok.Data;

public class ContextBuilder {

    private ContextImpl context = new ContextImpl();

    public static ContextBuilder newContextBuilder() {
        ContextBuilder contextBuilder = new ContextBuilder();
        return contextBuilder;
    }

    public static Context buildContextComMedicao(Long idMedicao, SituacaoMedicaoEnum situacao) {

        MedicaoDTO medicao = new MedicaoDTO();
        medicao.setId(idMedicao);
        medicao.setSituacao(situacao);

        return buildContextComMedicao(medicao);
    }

    public static Context buildContextComMedicao(MedicaoDTO medicao) {
        return newContextBuilder().withMedicao(medicao).create();
    }

    public Context create() {
        return this.context;
    }

    public ContextBuilder setContext(ContratoBD contrato, MedicaoDTO medicao,
            Map<Long, SituacaoMedicaoEnum> cacheSituacaoMedicao, List<SubmetaMedicaoBD> cacheSubmetaMedicao) {
        this.context = new ContextImpl(contrato, medicao, cacheSituacaoMedicao, cacheSubmetaMedicao);
        return this;
    }

    public ContextBuilder withContrato(ContratoBD contrato) {
        this.context.setContrato(contrato);
        return this;
    }

    public ContextBuilder withMedicao(MedicaoDTO medicao) {
        this.context.getCacheSituacaoMedicao().put(medicao.getId(), medicao.getSituacao());
        this.context.getCacheIndComplementacaoValor().put(medicao.getId(), medicao.getPermiteComplementacaoValor());
        this.context.setMedicao(medicao);
        return this;
    }

    public ContextBuilder withCacheSituacaoMedicao(Map<Long, SituacaoMedicaoEnum> cacheSituacaoMedicao) {
        this.context.setCacheSituacaoMedicao(cacheSituacaoMedicao);
        return this;
    }

    public ContextBuilder withCacheIndComplementacaoValor(Map<Long, Boolean> cacheIndComplementacaoValor) {
        this.context.setCacheIndComplementacaoValor(cacheIndComplementacaoValor);
        return this;
    }

    public ContextBuilder withCacheIdMedicoesAcumuladas(List<Long> cacheIdMedicoesAcumuladas) {
        this.context.setCacheIdMedicoesAcumuladas(cacheIdMedicoesAcumuladas);
        return this;
    }

    public ContextBuilder withCacheSubmetaMedicaoBD(List<SubmetaMedicaoBD> cacheSubmetaMedicaoBD) {
        this.context.setCacheSubmetaMedicaoBD(cacheSubmetaMedicaoBD);
        return this;
    }

    @Data
    private class ContextImpl implements Context {

        private ContratoBD contrato;

        private MedicaoDTO medicao;

        private Map<Long, SubmetaVrplDTO> cacheSubmetasContrato = new HashMap<>();

        private Map<Long, SituacaoMedicaoEnum> cacheSituacaoMedicao = new HashMap<>();;

        private List<SubmetaMedicaoBD> cacheSubmetaMedicaoBD = new ArrayList<>();

        private List<Long> cacheIdMedicoesAcumuladas = new ArrayList<>();

        private Map<Long, Boolean> cacheIndComplementacaoValor = new HashMap<>();

        public ContextImpl() {
            super();
        }

        public ContextImpl(ContratoBD contrato, MedicaoDTO medicao, Map<Long, SituacaoMedicaoEnum> cacheSituacaoMedicao,
                List<SubmetaMedicaoBD> cacheSubmetaMedicao) {
            this.contrato = contrato;
            this.medicao = medicao;
            this.cacheSituacaoMedicao = cacheSituacaoMedicao;
            this.cacheSubmetaMedicaoBD = cacheSubmetaMedicao;
        }
    }
}
