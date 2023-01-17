package br.gov.planejamento.siconv.med.empresa.business;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.gov.planejamento.siconv.med.empresa.entity.dto.EmpresaDTO;
import br.gov.planejamento.siconv.med.infra.exception.MedicaoRestException;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.integration.contratos.ContratosGrpcConsumer;
import br.gov.planejamento.siconv.med.integration.maisbrasil.MaisBrasilGRPCConsumer;

@ApplicationScoped
public class EmpresaBC {

    @Inject
    private MaisBrasilGRPCConsumer maisBrasilCadastroConsumer;

    @Inject
    private ContratosGrpcConsumer contratosConsumer;

    public List<EmpresaDTO> listarEmpresasVinculadasUsuario(String cpfUsuario) {

        List<EmpresaDTO> empresas = maisBrasilCadastroConsumer.listarEmpresasVinculadasMaisBrasilPorCpf(cpfUsuario)
                .stream().sorted(comparing(EmpresaDTO::getRazaoSocial)).collect(toList());

        if (!empresas.isEmpty()) {
            Map<String, Integer> qtdeContratos = contratosConsumer.consultarQtdeContratosAptosPorEmpresa(
                    empresas.stream().map(EmpresaDTO::getCnpj).collect(toList()));

            empresas.forEach(emp -> emp.setQtdContratos(qtdeContratos.get(emp.getCnpj())));
        }

        return empresas;
    }

    public EmpresaDTO consultarEmpresaPorId(Long idEmpresa) {
        return maisBrasilCadastroConsumer.consultarEmpresaPorId(idEmpresa)
                .orElseThrow(this::buildExceptionEmpresaInexistente);
    }

    public EmpresaDTO consultarEmpresaPorCnpj(String cnpj) {
        return maisBrasilCadastroConsumer.consultarEmpresaPorCnpj(cnpj)
                .orElseThrow(this::buildExceptionEmpresaInexistente);
    }

    private MedicaoRestException buildExceptionEmpresaInexistente() {
        return new MedicaoRestException(MessageKey.ERRO_EMPRESA_INEXISTENTE);
    }
}
