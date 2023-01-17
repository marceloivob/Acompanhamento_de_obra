package br.gov.planejamento.siconv.med.integration.contratos;

import static java.util.Collections.sort;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.net.ssl.SSLException;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.SimplyTimed;

import br.gov.planejamento.siconv.med.contrato.entity.ModalidadeEnum;
import br.gov.planejamento.siconv.med.contrato.entity.dto.ContratoSiconvDTO;
import br.gov.planejamento.siconv.med.infra.exception.GrpcIntegrationException;
import br.gov.planejamento.siconv.med.integration.util.AbstractGRPCConsumer;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;
import br.gov.serpro.siconv.contratos.grpc.dto.ContratoDetalhadoDTO;
import br.gov.serpro.siconv.contratos.grpc.dto.PropostaDTO;
import br.gov.serpro.siconv.contratos.grpc.dto.SubmetaDTO;
import br.gov.serpro.siconv.contratos.grpc.services.ContratosGRPCClient;
import lombok.Getter;

@SimplyTimed
@RequestScoped
public class ContratosGrpcConsumer extends AbstractGRPCConsumer {

    @ConfigProperty(name = "contratos.grpc.host")
    private String host;

    @ConfigProperty(name = "contratos.grpc.port")
    private int port;

    public Map<String, Integer> consultarQtdeContratosAptosPorEmpresa(List<String> listaCnpj) {

        try (ContratosGRPCClientWrapper wrapper = newClientWrapper()) {

            return wrapper.getClient().getQTDContratos(listaCnpj);

        } catch (Exception e) {
            throw new GrpcIntegrationException(e);
        }
    }

    public List<ContratoSiconvDTO> listarContratosAptosPorEmpresa(String cnpj) {

        try (ContratosGRPCClientWrapper wrapper = newClientWrapper()) {

            return wrapper.getClient().getContratos(cnpj).stream().map(this::converterContrato).collect(toList());

        } catch (Exception e) {
            throw new GrpcIntegrationException(e);
        }
    }

    public Optional<ContratoSiconvDTO> consultarContratoPorId(Long idContrato) {

        try (ContratosGRPCClientWrapper wrapper = newClientWrapper()) {

            return wrapper.getClient().getContratoPorId(idContrato).map(this::converterContrato);

        } catch (Exception e) {
            throw new GrpcIntegrationException(e);
        }
    }

    private ContratoSiconvDTO converterContrato(ContratoDetalhadoDTO contrato) {

        ContratoSiconvDTO contratoDto = new ContratoSiconvDTO();

        contratoDto.setId(contrato.getId());
        contratoDto.setNumeroContrato(contrato.getNumero());
        contratoDto.setCnpj(contrato.getCnpjFornecedor());
        contratoDto.setInSocial(contrato.getIsTrabSocial());
        contratoDto.setInAcompEvento(contrato.getIsAcompEventos());
        contratoDto.setDtInicioVigencia(contrato.getDtInicioVigencia().toString());
        contratoDto.setDtFimVigencia(contrato.getDtFimVigencia());
        contratoDto.setDtAssinatura(contrato.getDtAssinatura());
        contratoDto.setDtAssinaturaTipoInstrumento(contrato.getDtAssinaturaTipoInstrumento());
        contratoDto.setNomeObjetoContratoFornecimento(contrato.getObjeto());
        contratoDto.setValorContrato(contrato.getValorTotal().toString());
        contratoDto.setPropostaFk(contrato.getIdSiconv());
        contratoDto.setLocalidade(contrato.getLocalidade());
        contratoDto.setModalidade(
                ModalidadeEnum.fromCodigo(contrato.getModalidade(), contrato.getPossuiInstituicaoMandataria()));
        contratoDto.setNumeroConvenioRepasse(contrato.getNumeroTipoInstrumento().intValue());
        contratoDto.setAnoConvenioRepasse(contrato.getAnoTipoInstrumento().intValue());
        contratoDto.setNomeObjetoContratoRepasse(contrato.getObjetoTipoInstrumento());
        contratoDto.setValorTipoInstrumento(contrato.getValorTipoInstrumento());
        contratoDto.setNomeConvenente(contrato.getNomeProponente());

        return contratoDto;
    }

    public List<SubmetaVrplDTO> listarSubmetasPorContratoId(Long idContrato) {

        try (ContratosGRPCClientWrapper wrapper = newClientWrapper()) {

            List<SubmetaDTO> response = wrapper.getClient().getSubmetasPorIdContrato(idContrato);

            List<SubmetaVrplDTO> submetas = new ArrayList<>();

            response.forEach(submeta -> {
                SubmetaVrplDTO sub = new SubmetaVrplDTO();
                sub.setId(submeta.getId_vrpl());
                sub.setNrSubmetaAnalise(submeta.getNumero());
                sub.setDescricao(submeta.getDescricao());
                sub.setValor(submeta.getValorTotal());

                submetas.add(sub);
            });

            sort(submetas, SubmetaVrplDTO.ORDENACAO_PADRAO);

            return submetas;

        } catch (Exception e) {
            throw new GrpcIntegrationException(e);
        }
    }

    public Optional<PropostaDTO> consultarTipoInstrumentoConvenente(Long idProposta) {

        try (ContratosGRPCClientWrapper wrapper = newClientWrapper()) {

            return wrapper.getClient().getPropostaPorId(idProposta);

        } catch (Exception e) {
            throw new GrpcIntegrationException(e);
        }
    }

    private ContratosGRPCClientWrapper newClientWrapper() throws SSLException {
        return new ContratosGRPCClientWrapper(host, port);
    }

    private static class ContratosGRPCClientWrapper implements AutoCloseable {

        @Getter
        private ContratosGRPCClient client;

        public ContratosGRPCClientWrapper(String host, Integer port) throws SSLException {
            client = new ContratosGRPCClient(host, port, false);
        }

        @Override
        public void close() throws Exception {
            client.shutdown();
        }
    }
}
