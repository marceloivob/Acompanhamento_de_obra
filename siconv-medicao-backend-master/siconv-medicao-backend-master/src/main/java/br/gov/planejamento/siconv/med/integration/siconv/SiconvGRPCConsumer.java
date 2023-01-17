package br.gov.planejamento.siconv.med.integration.siconv;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.normalizeSpace;
import static org.apache.commons.lang3.StringUtils.trim;

import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.net.ssl.SSLException;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.SimplyTimed;

import br.gov.planejamento.siconv.grpc.SiconvGRPCClient;
import br.gov.planejamento.siconv.grpc.UsuarioMembroAtivoConvenioResponse;
import br.gov.planejamento.siconv.grpc.UsuarioResponse;
import br.gov.planejamento.siconv.med.infra.exception.GrpcIntegrationException;
import br.gov.planejamento.siconv.med.integration.dto.UsuarioDTO;
import br.gov.planejamento.siconv.med.integration.util.AbstractGRPCConsumer;
import io.grpc.Status;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SimplyTimed
@RequestScoped
public class SiconvGRPCConsumer extends AbstractGRPCConsumer {

    @ConfigProperty(name = "siconv.grpc.host")
    private String host;

    @ConfigProperty(name = "siconv.grpc.port")
    private int port;

    public UsuarioDTO consultarUsuarioMembroConvenio(String cpf, Integer sequencialConvenio, Integer anoConvenio) {

        UsuarioDTO usuario = null;

        try (SiconvGRPCClientWrapper wrapper = newClientWrapper()) {

            UsuarioMembroAtivoConvenioResponse response = wrapper.getClient().consultarUsuarioMembroAtivoConvenio(cpf,
                    sequencialConvenio.longValue(), anoConvenio);

            if (response != null && !isEmpty(response.getLogin())) {
                usuario = new UsuarioDTO();
                usuario.setCpf(response.getLogin());
                usuario.setEmail(response.getEmail());
                usuario.setNome(response.getNome());
                usuario.setVinculadoConvenioAtual(converterIndicadorSimNao(response.getVinculo()));
                usuario.setVinculadoOutroConvenio(converterIndicadorSimNao(response.getOutrovinculo()));
                usuario.setFiscalConvenente(converterIndicadorSimNao(response.getFiscal()));
                usuario.setAtivo(converterIndicadorSimNao(response.getAtivo()));
            }

        } catch (Exception e) {

            if (checkStatusException(e, Status.NOT_FOUND)) {
                log.warn(normalizeSpace(format("Não foi encontrado usuário no Siconv com o cpf %s.", cpf)));

            } else {
                throw new GrpcIntegrationException(e);
            }
        }

        return usuario;
    }

    private boolean converterIndicadorSimNao(String indicador) {
        return "S".equalsIgnoreCase(trim(indicador));
    }

    public UsuarioDTO consultarUsuarioPorCpf(String cpf) {

        UsuarioDTO usuario = null;

        try (SiconvGRPCClientWrapper wrapper = newClientWrapper()) {

            UsuarioResponse response = wrapper.getClient().consultarUsuarioPorCpf(cpf);

            if (response != null && !isEmpty(response.getLogin())) {
                usuario = new UsuarioDTO();
                usuario.setCpf(response.getLogin());
                usuario.setEmail(response.getEmail());
                usuario.setNome(response.getNome());
            }

        } catch (Exception e) {
            throw new GrpcIntegrationException(e);
        }

        return usuario;
    }

    public Long getIdConvenio(Integer sequencial, Integer ano) {

        try (SiconvGRPCClientWrapper wrapper = newClientWrapper()) {

            return wrapper.getClient().getIdConvenio(sequencial, ano).getId();

        } catch (Exception e) {
            throw new GrpcIntegrationException(e);
        }
    }

    /**
     * Segue os tipos de retornos e valores possíveis
     * 
     * True = Proposta tratada pelo VRPL
     * False = Proposta NÂO é tratada pelo VRPL
     * null = Proposta não existe.
     * 
     * @param idProposta
     * @return
     */
    public Optional<Boolean> isVRPLResponsavelAceiteProcessoExecucao(Long idProposta) {

        Boolean retorno = null;

        try (SiconvGRPCClientWrapper wrapper = newClientWrapper()) {

            retorno = wrapper.getClient().isVRPLResponsavelAceiteProcessoExecucao(idProposta).getRetorno();

        } catch (Exception e) {

            if (checkStatusException(e, Status.NOT_FOUND)) {
                log.warn(normalizeSpace(format("Não foi encontrada proposta com o id %o.", idProposta)));

            } else {
                throw new GrpcIntegrationException(e);
            }
        }

        return Optional.ofNullable(retorno);
    }

    private SiconvGRPCClientWrapper newClientWrapper() throws SSLException {
        return new SiconvGRPCClientWrapper(host, port);
    }

    private static class SiconvGRPCClientWrapper implements AutoCloseable {

        @Getter
        private SiconvGRPCClient client;

        public SiconvGRPCClientWrapper(String host, Integer port) throws SSLException {
            client = new SiconvGRPCClient(host, port, false);
        }

        @Override
        public void close() throws Exception {
            client.shutdown();
        }
    }
}
