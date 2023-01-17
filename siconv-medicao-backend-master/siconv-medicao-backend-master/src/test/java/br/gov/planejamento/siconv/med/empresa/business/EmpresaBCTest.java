package br.gov.planejamento.siconv.med.empresa.business;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import br.gov.planejamento.siconv.med.empresa.entity.dto.EmpresaDTO;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.integration.contratos.ContratosGrpcConsumer;
import br.gov.planejamento.siconv.med.integration.maisbrasil.MaisBrasilGRPCConsumer;
import br.gov.planejamento.siconv.med.test.extension.BusinessControllerBaseTest;

class EmpresaBCTest extends BusinessControllerBaseTest {

    @Mock
    private MaisBrasilGRPCConsumer maisBrasilCadastroConsumer;

    @Mock
    private ContratosGrpcConsumer contratosConsumer;

    @InjectMocks
    private EmpresaBC bc;

    @Test
    void testListarEmpresasVinculadasUsuario() {

        EmpresaDTO empresaB = createEmpresa(1L, "11111111111111", "Empresa B");
        EmpresaDTO empresaC = createEmpresa(2L, "22222222222222", "Empresa C");
        EmpresaDTO empresaA = createEmpresa(3L, "33333333333333", "Empresa A");

        List<EmpresaDTO> listaOrdenadaPorId = List.of(empresaB, empresaC, empresaA);

        int qtdContratosEmpA = 10;
        int qtdContratosEmpB = 20;
        int qtdContratosEmpC = 30;

        Map<String, Integer> mapQtdContratos = Map.of(
                empresaA.getCnpj(), qtdContratosEmpA,
                empresaB.getCnpj(), qtdContratosEmpB,
                empresaC.getCnpj(), qtdContratosEmpC);

        when(maisBrasilCadastroConsumer.listarEmpresasVinculadasMaisBrasilPorCpf(any())).thenReturn(listaOrdenadaPorId);
        when(contratosConsumer.consultarQtdeContratosAptosPorEmpresa(any())).thenReturn(mapQtdContratos);

        List<EmpresaDTO> empresas = bc.listarEmpresasVinculadasUsuario("99999999999");

        // retorno ordenado por razao social
        assertEquals(empresaA, empresas.get(0));
        assertEquals(empresaB, empresas.get(1));
        assertEquals(empresaC, empresas.get(2));

        assertEquals(qtdContratosEmpA, empresaA.getQtdContratos());
        assertEquals(qtdContratosEmpB, empresaB.getQtdContratos());
        assertEquals(qtdContratosEmpC, empresaC.getQtdContratos());
    }

    @Test
    void testConsultarEmpresaPorId() {
        EmpresaDTO empresa = createEmpresa(1L, "11111111111111", "Empresa Mock");
        when(maisBrasilCadastroConsumer.consultarEmpresaPorId(empresa.getId())).thenReturn(Optional.of(empresa));
        assertEquals(empresa, bc.consultarEmpresaPorId(empresa.getId()));
    }

    @Test
    void testConsultarEmpresaPorCnpj() {
        EmpresaDTO empresa = createEmpresa(1L, "11111111111111", "Empresa Mock");
        when(maisBrasilCadastroConsumer.consultarEmpresaPorCnpj(empresa.getCnpj())).thenReturn(Optional.of(empresa));
        assertEquals(empresa, bc.consultarEmpresaPorCnpj(empresa.getCnpj()));
    }

    @Test
    void testNenhumaEmpresaVinculadaUsuario() {
        when(maisBrasilCadastroConsumer.listarEmpresasVinculadasMaisBrasilPorCpf(any())).thenReturn(emptyList());
        assertThat(bc.listarEmpresasVinculadasUsuario("99999999999"), is(empty()));
    }

    @Test
    void testConsultarPorIdEmpresaInexistente() {
        when(maisBrasilCadastroConsumer.consultarEmpresaPorId(any())).thenReturn(Optional.empty());
        assertThrowsMedicaoRestException(MessageKey.ERRO_EMPRESA_INEXISTENTE, () -> bc.consultarEmpresaPorId(1L));
    }

    @Test
    void testConsultarPorCnpjEmpresaInexistente() {
        when(maisBrasilCadastroConsumer.consultarEmpresaPorCnpj(any())).thenReturn(Optional.empty());
        assertThrowsMedicaoRestException(MessageKey.ERRO_EMPRESA_INEXISTENTE,
                () -> bc.consultarEmpresaPorCnpj("11111111111111"));
    }

    private EmpresaDTO createEmpresa(Long id, String cnpj, String razao) {
        EmpresaDTO empB = new EmpresaDTO();
        empB.setId(id);
        empB.setCnpj(cnpj);
        empB.setRazaoSocial(razao);
        return empB;
    }

}
