package br.gov.planejamento.siconv.med.integration.maisbrasil;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.net.ssl.SSLException;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.SimplyTimed;

import br.gov.economia.maisbrasil.cadastro.grpc.EmpresaResponse;
import br.gov.economia.maisbrasil.cadastro.grpc.ListaEmpresaResponse;
import br.gov.economia.maisbrasil.cadastro.grpc.MaisBrasilCadastroGRPCClient;
import br.gov.economia.maisbrasil.cadastro.grpc.TokenResponse;
import br.gov.economia.maisbrasil.cadastro.grpc.UsuarioVinculadoResponse;
import br.gov.planejamento.siconv.med.empresa.entity.dto.EmpresaDTO;
import br.gov.planejamento.siconv.med.infra.exception.GrpcIntegrationException;
import br.gov.planejamento.siconv.med.infra.security.domain.Permission;
import br.gov.planejamento.siconv.med.integration.dto.UsuarioDTO;
import br.gov.planejamento.siconv.med.integration.util.AbstractGRPCConsumer;
import lombok.Getter;

@SimplyTimed
@RequestScoped
public class MaisBrasilGRPCConsumer extends AbstractGRPCConsumer {

    @ConfigProperty(name = "maisbrasil.cadastro.grpc.url")
    private String host;

    @ConfigProperty(name = "maisbrasil.cadastro.grpc.port")
    private int port;

    public UsuarioDTO consultaUsuarioMaisBrasil(String numeroCPF, String cnpj) {
        return consultarUsuario(numeroCPF, cnpj, Permission.ASSINAR_SUBMETA.getKey());
    }

    public UsuarioDTO consultaUsuarioMaisBrasil(String numeroCPF) {
        return consultarUsuario(numeroCPF, "0", "");
    }

    private UsuarioDTO consultarUsuario(String numeroCPF, String cnpj, String permissionKey) {

        UsuarioDTO usuario = null;

        try (MaisBrasilCadastroGRPCClientWrapper wrapper = newClientWrapper()) {

            UsuarioVinculadoResponse response = wrapper.getClient().getUsuarioVinculado(numeroCPF, cnpj, permissionKey);

            if (response != null && !isEmpty(response.getCpf())) {
                usuario = new UsuarioDTO();
                usuario.setCpf(response.getCpf());
                usuario.setEmail(response.getEmail());
                usuario.setNome(response.getNome());
                usuario.setAssinanteSubmetaEmpresa(response.getPossuiFunc());
                usuario.setVinculadoEmpresa(response.getEmpresaId() != 0);
            }

        } catch (Exception e) {
            throw new GrpcIntegrationException(e);
        }

        return usuario;
    }

    public List<EmpresaDTO> listarEmpresasVinculadasMaisBrasilPorCpf(String numeroCPF) {

        try (MaisBrasilCadastroGRPCClientWrapper wrapper = newClientWrapper()) {

            ListaEmpresaResponse response = wrapper.getClient().getListaEmpresasByCpf(numeroCPF);

            List<EmpresaDTO> listaEmpresas = new ArrayList<>();

            if (response != null && !isEmpty(response.getListaEmpresaList())) {

                response.getListaEmpresaList().forEach(emp -> {
                    EmpresaDTO empresa = new EmpresaDTO();
                    empresa.setId(emp.getId());
                    empresa.setCnpj(emp.getCnpj());
                    empresa.setRazaoSocial(emp.getRazaoSocial());

                    listaEmpresas.add(empresa);
                });
            }

            return listaEmpresas;

        } catch (Exception e) {
            throw new GrpcIntegrationException(e);
        }
    }

    public Optional<EmpresaDTO> consultarEmpresaPorCnpj(String numeroCnpj) {

        try (MaisBrasilCadastroGRPCClientWrapper wrapper = newClientWrapper()) {

            EmpresaResponse response = wrapper.getClient().getEmpresaByCnpj(numeroCnpj);

            return carregarEmpresa(response);

        } catch (Exception e) {
            throw new GrpcIntegrationException(e);
        }
    }

    public Optional<EmpresaDTO> consultarEmpresaPorId(Long idEmpresa) {

        try (MaisBrasilCadastroGRPCClientWrapper wrapper = newClientWrapper()) {

            EmpresaResponse response = wrapper.getClient().getEmpresaById(idEmpresa);

            return carregarEmpresa(response);

        } catch (Exception e) {
            throw new GrpcIntegrationException(e);
        }
    }

    private Optional<EmpresaDTO> carregarEmpresa(EmpresaResponse response) {
        EmpresaDTO empresa = null;
        if (response != null && !isBlank(response.getCnpj())) {
            empresa = new EmpresaDTO();
            empresa.setCnpj(response.getCnpj());
            empresa.setId(response.getId());
            empresa.setRazaoSocial(response.getRazaoSocial());
        }
        return Optional.ofNullable(empresa);
    }

    public TokenResponse getInformacoesToken(String cpf) {

        try (MaisBrasilCadastroGRPCClientWrapper wrapper = newClientWrapper()) {

            return wrapper.getClient().getInformacoesToken(cpf);

        } catch (Exception e) {
            throw new GrpcIntegrationException(e);
        }
    }

    private MaisBrasilCadastroGRPCClientWrapper newClientWrapper() throws SSLException {
        return new MaisBrasilCadastroGRPCClientWrapper(host, port);
    }

    private static class MaisBrasilCadastroGRPCClientWrapper implements AutoCloseable {

        @Getter
        private MaisBrasilCadastroGRPCClient client;

        public MaisBrasilCadastroGRPCClientWrapper(String host, Integer port) throws SSLException {
            client = new MaisBrasilCadastroGRPCClient(host, port, false);
        }

        @Override
        public void close() throws Exception {
            client.shutdown();
        }
    }
}
