package br.gov.planejamento.siconv.med.integration.vrpl;

import static java.lang.String.format;
import static java.util.Collections.sort;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.normalizeSpace;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.RequestScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.SimplyTimed;

import br.gov.planejamento.siconv.med.infra.exception.GrpcIntegrationException;
import br.gov.planejamento.siconv.med.integration.util.AbstractGRPCConsumer;
import br.gov.planejamento.siconv.med.medicao.entity.dto.EventoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.FrenteObraVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MacroservicoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;
import br.gov.serpro.vrpl.grpc.ListaSubmetaResponse;
import br.gov.serpro.vrpl.grpc.PropostaLote;
import br.gov.serpro.vrpl.grpc.PropostaLotesResponse;
import br.gov.serpro.vrpl.grpc.Servico;
import br.gov.serpro.vrpl.grpc.Submeta;
import br.gov.serpro.vrpl.grpc.Submeta.FrenteObra;
import br.gov.serpro.vrpl.grpc.Submeta.FrenteObra.Evento;
import br.gov.serpro.vrpl.grpc.Submeta.FrenteObra.MacroServico;
import br.gov.serpro.vrpl.grpc.SubmetaResponse;
import br.gov.serpro.vrpl.grpc.client.VRPLGrpcClient;
import io.grpc.Status;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SimplyTimed
@RequestScoped
public class VrplGRPCConsumer extends AbstractGRPCConsumer {

    @ConfigProperty(name = "vrpl.grpc.host")
    private String host;

    @ConfigProperty(name = "vrpl.grpc.port")
    private int port;

    /**
     * Utilizado na carga da tabela Item Medição
     * 
     * @param ids
     * @return
     */
    public List<SubmetaVrplDTO> getListaSubmetasPorId(List<Long> ids) {

        List<SubmetaVrplDTO> listaSubmetas = new ArrayList<>();

        try (VRPLGrpcClientWrapper wrapper = newClientWrapper()) {

            ListaSubmetaResponse response = wrapper.getClient().consultarListaSubmetas(ids);

            if (response != null && !isEmpty(response.getSubmetaList())) {
                response.getSubmetaList().forEach(sub -> listaSubmetas.add(converterSubmeta(sub)));
            }

        } catch (Exception e) {

            if (checkStatusException(e, Status.NOT_FOUND)) {
                log.warn(normalizeSpace(format("Não foram encontradas submetas com os ids: %s.", ids)));

            } else {
                throw new GrpcIntegrationException(e);
            }
        }

        sort(listaSubmetas, SubmetaVrplDTO.ORDENACAO_PADRAO);

        return listaSubmetas;
    }

    /**
     * Utilizado na tela do Preencher Submeta
     * 
     * @param ids
     * @return
     */
    public Optional<SubmetaVrplDTO> getSubmetaPorId(Long id) {

        SubmetaVrplDTO submeta = null;

        try (VRPLGrpcClientWrapper wrapper = newClientWrapper()) {

            SubmetaResponse response = wrapper.getClient().consultarSubmetaPorId(id);

            if (response != null) {
                submeta = converterSubmeta(response.getSubmeta());
            }

        } catch (Exception e) {

            if (checkStatusException(e, Status.NOT_FOUND)) {
                log.warn(normalizeSpace(format("Não foi encontrada submeta com o id %s.", id)));

            } else {
                throw new GrpcIntegrationException(e);
            }
        }

        return Optional.ofNullable(submeta);
    }

    /**
     * Consulta os Lotes das Submetas quando existe pelo menos uma VRPL Aceita
     * 
     * 
     * @param idPropostaSiconv
     * @return
     */
    public Optional<PropostaLote> consultarSubmetasPropostaVRPLAceita(Long idPropostaSiconv) {

        PropostaLote propostaLote = null;

        try (VRPLGrpcClientWrapper wrapper = newClientWrapper()) {

            PropostaLotesResponse response = wrapper.getClient().consultarListaLotesComSubmetas(idPropostaSiconv);

            if (response != null) {
                propostaLote = response.getPropostaLote();
            }

        } catch (Exception e) {

            if (checkStatusException(e, Status.NOT_FOUND)) {
                log.warn(normalizeSpace(format("Não foram encontrados lotes para o id : %s.", idPropostaSiconv)));

            } else {
                throw new GrpcIntegrationException(e);
            }
        }

        return Optional.ofNullable(propostaLote);
    }

    /**
     * Consulta a lista de Submetas do Projeto Basico com base na Lista de Submetas
     * do VRPL
     * 
     * @param servico
     * @return
     */
    public Map<Long, Long> consultarListaSubmetasProjetoBasico(List<Long> listaIdSubmetasVRPL) {

        Map<Long, Long> mapaIdSubmetasVRPLProjetoBasico = new HashMap<>();

        try (VRPLGrpcClientWrapper wrapper = newClientWrapper()) {

            mapaIdSubmetasVRPLProjetoBasico = wrapper.getClient()
                    .recuperarSubmetasDoProjetoBasicoAPartirDasSubmetasDoVRPL(listaIdSubmetasVRPL);

        } catch (Exception e) {

            if (checkStatusException(e, Status.NOT_FOUND)) {
                log.warn(normalizeSpace(format(
                        "Não foram encontradas Submetas no Projeto básico para as submetas: %s de VRPL informadas.",
                        listaIdSubmetasVRPL)));

            } else {
                throw new GrpcIntegrationException(e);
            }
        }

        return mapaIdSubmetasVRPLProjetoBasico;
    }

    private SubmetaVrplDTO converterSubmeta(Submeta submeta) {

        SubmetaVrplDTO submetaDto = new SubmetaVrplDTO();

        submetaDto.setId(submeta.getId());
        submetaDto.setDescricao(submeta.getDescricao());
        submetaDto.setNrSubmetaAnalise(submeta.getNumero());
        submetaDto.setValor(new BigDecimal(submeta.getValorLicitado()));

        submeta.getFrentesObrasList().forEach(fo -> submetaDto.addFrentesObras(converterFrenteObra(fo)));

        return submetaDto;
    }

    private FrenteObraVrplDTO converterFrenteObra(FrenteObra frenteObra) {

        FrenteObraVrplDTO frenteObraDto = new FrenteObraVrplDTO();

        frenteObraDto.setId(frenteObra.getId());
        frenteObraDto.setDescricao(frenteObra.getDescricao());

        frenteObra.getEventosList().forEach(ev -> frenteObraDto.addEventos(converterEvento(ev)));

        frenteObra.getMacroServicoList()
                .forEach(macro -> frenteObraDto.addMacroservicosView(converterMacroservico(macro)));

        return frenteObraDto;
    }

    private EventoVrplDTO converterEvento(Evento evento) {

        EventoVrplDTO eventoDto = new EventoVrplDTO();

        eventoDto.setId(evento.getId());
        eventoDto.setDescricao(evento.getDescricao());
        eventoDto.setValor(new BigDecimal(evento.getValorTotal()));

        evento.getServicosList().forEach(serv -> eventoDto.addServicos(converterServico(serv)));

        return eventoDto;
    }

    private ServicoVrplDTO converterServico(Servico servico) {

        ServicoVrplDTO servicoDto = new ServicoVrplDTO();

        servicoDto.setId(servico.getId());
        servicoDto.setNumero(servico.getNumero());
        servicoDto.setDescricao(servico.getDescricao());
        servicoDto.setQtd(new BigDecimal(servico.getQtdeItens()));
        servicoDto.setSgUnidade(servico.getSgUnidade());
        if (servico.getValorUnitario() != null && !isEmpty(servico.getValorUnitario())) {
            servicoDto.setPreco(new BigDecimal(servico.getValorUnitario()));
        }

        return servicoDto;
    }

    private MacroservicoVrplDTO converterMacroservico(MacroServico macroservico) {

        MacroservicoVrplDTO macroservicoDto = new MacroservicoVrplDTO();

        macroservicoDto.setId(macroservico.getId());
        macroservicoDto.setNumero(macroservico.getNumero());
        macroservicoDto.setDescricao(macroservico.getDescricao());

        macroservico.getServicosList().forEach(serv -> macroservicoDto.addServicos(converterServico(serv)));

        return macroservicoDto;
    }

    private VRPLGrpcClientWrapper newClientWrapper() {
        return new VRPLGrpcClientWrapper(host, port);
    }

    private static class VRPLGrpcClientWrapper implements AutoCloseable {

        @Getter
        private VRPLGrpcClient client;

        public VRPLGrpcClientWrapper(String host, Integer port) {
            client = new VRPLGrpcClient(host, port, false);
        }

        @Override
        public void close() throws Exception {
            client.shutdown();
        }
    }
}
