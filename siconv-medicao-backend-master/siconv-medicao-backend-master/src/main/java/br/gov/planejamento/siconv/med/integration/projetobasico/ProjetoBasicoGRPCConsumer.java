package br.gov.planejamento.siconv.med.integration.projetobasico;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.MapUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.normalizeSpace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.metrics.annotation.SimplyTimed;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.DocumentoComplementarDTO;
import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.TipoDocumentoEnum;
import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.TipoManifestoEnum;
import br.gov.planejamento.siconv.med.infra.exception.GrpcIntegrationException;
import br.gov.planejamento.siconv.med.integration.util.AbstractGRPCConsumer;
import br.gov.planejamento.siconv.med.integration.vrpl.VrplGRPCConsumer;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;
import br.gov.serpro.plataformamaisbrasil.projetobasico.grpc.ParametrosInput;
import br.gov.serpro.plataformamaisbrasil.projetobasico.grpc.TipoDeManifestoAmbiental;
import br.gov.serpro.siconv.projetobasico.client.DocumentoComplementarEnriched;
import br.gov.serpro.siconv.projetobasico.client.ListaDocumentosComplementaresResponseUnwrapper;
import br.gov.serpro.siconv.projetobasico.client.ProjetoBasicoGrpcClient;
import io.grpc.Status;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SimplyTimed
@RequestScoped
public class ProjetoBasicoGRPCConsumer extends AbstractGRPCConsumer {

    @Inject
    private VrplGRPCConsumer vrplConsumer;

    @ConfigProperty(name = "projeto_basico.grpc.host")
    private String host;

    @ConfigProperty(name = "projeto_basico.grpc.port")
    private int port;

    /**
     * Consulta a lista de Documentos Complementares do Tipo Manifesto Ambiental de
     * um Contrato
     * 
     * @param servico
     * @return
     */
    public List<DocumentoComplementarDTO> consultarDocumentosComplementaresProjetoBasico(List<Long> listaIdSubmetasVRPL,
            List<TipoManifestoEnum> listaTiposManifesto) {

        Map<Long, Long> mapaIdSubmetasProjetoBasico = vrplConsumer
                .consultarListaSubmetasProjetoBasico(listaIdSubmetasVRPL);

        if (isEmpty(mapaIdSubmetasProjetoBasico)) {
            return emptyList();
        }

        try (ProjetoBasicoGrpcClientWrapper wrapper = newClientWrapper()) {

            // Usar a lista listaIdSubmetasProjetoBasico e listaTiposManifesto como entrada
            // para o novo serviço do projeto básico.
            ParametrosInput.Builder builder = ParametrosInput.newBuilder();
            builder.addAllIdSubmeta(mapaIdSubmetasProjetoBasico.values());
            builder.addAllTipoDeManifestoAmbiental(
                    obterTipoManifestoPadraProjetoBasicoApartirTipoManifestoAmbientalMedicao(listaTiposManifesto));

            ListaDocumentosComplementaresResponseUnwrapper response = wrapper.getClient()
                    .recuperarDocumentosComplementaresPorTipoDeManifestoAmbiental(builder.build());

            return obterListaDocumentosComplementares(response.getRelacaoDeDocumentosComplementares(),
                    mapaIdSubmetasProjetoBasico);

        } catch (Exception e) {

            if (checkStatusException(e, Status.NOT_FOUND)) {

                log.warn(normalizeSpace(format(
                        "Não foram encontrados Documentos Complementares para as submetas : %s do Projeto Básico informadas. ",
                        mapaIdSubmetasProjetoBasico.values())));

                return emptyList();

            } else {
                throw new GrpcIntegrationException(e);
            }
        }
    }

    private List<DocumentoComplementarDTO> obterListaDocumentosComplementares(
            List<DocumentoComplementarEnriched> relacaoDeDocumentosComplementares,
            Map<Long, Long> mapaIdSubmetasProjetoBasico) {

        return relacaoDeDocumentosComplementares.stream().map(docProjBasico -> {

            DocumentoComplementarDTO doc = new DocumentoComplementarDTO();

            doc.setDtEmissao(docProjBasico.getDataDeEmissao());
            doc.setDtValidade(docProjBasico.getDataDeValidade());
            doc.setNrDocumento(docProjBasico.getNumeroDoDocumento());
            doc.setTipoDocumento(TipoDocumentoEnum.MAM);
            doc.setTipoManifestoAmbiental(
                    TipoManifestoEnum.fromCodigoProjetoBasico(docProjBasico.getTipoDeManifestoAmbiental().name()));

            SubmetaVrplDTO submetaVrpl = obterSubmetaVrplCorrespondente(docProjBasico.getIdSubmeta(),
                    mapaIdSubmetasProjetoBasico);

            if (submetaVrpl != null) {
                doc.setSubmetas(Arrays.asList(submetaVrpl));
            }

            return doc;
        }).collect(toList());
    }

    private SubmetaVrplDTO obterSubmetaVrplCorrespondente(Long idProjetoBasico,
            Map<Long, Long> mapaIdSubmetasProjetoBasico) {

        Optional<Long> idSubmetaVrplOpt = mapaIdSubmetasProjetoBasico.keySet().stream()
                .filter(key -> mapaIdSubmetasProjetoBasico.get(key).equals(idProjetoBasico)).findAny();

        SubmetaVrplDTO submetaVrplDTO = new SubmetaVrplDTO();
        idSubmetaVrplOpt.ifPresentOrElse(submetaVrplDTO::setId, () -> submetaVrplDTO.setId(null));

        return submetaVrplDTO;
    }

    private Iterable<? extends TipoDeManifestoAmbiental> obterTipoManifestoPadraProjetoBasicoApartirTipoManifestoAmbientalMedicao(
            List<TipoManifestoEnum> listaTiposManifesto) {

        return listaTiposManifesto.stream()
                .map((TipoManifestoEnum tipoManifesto) -> TipoDeManifestoAmbiental
                        .valueOf(tipoManifesto.getCodigoProjetobasico()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private ProjetoBasicoGrpcClientWrapper newClientWrapper() {
        return new ProjetoBasicoGrpcClientWrapper(host, port);
    }

    private static class ProjetoBasicoGrpcClientWrapper implements AutoCloseable {

        @Getter
        private ProjetoBasicoGrpcClient client;

        public ProjetoBasicoGrpcClientWrapper(String host, Integer port) {
            client = new ProjetoBasicoGrpcClient(host, port, false);
        }

        @Override
        public void close() throws Exception {
            client.shutdown();
        }
    }
}
