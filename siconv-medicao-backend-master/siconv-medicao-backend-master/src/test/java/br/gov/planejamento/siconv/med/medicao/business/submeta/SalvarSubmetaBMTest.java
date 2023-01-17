package br.gov.planejamento.siconv.med.medicao.business.submeta;

import static br.gov.planejamento.siconv.med.infra.message.MessageKey.ERRO_ACUMULADO_SERVICO_CONVENENTE_MEDICAO_POSTERIOR_MAIOR_ACUMULADO_SERVICO_EMPRESA;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.AT;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.CC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.CE;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.EM;
import static br.gov.planejamento.siconv.med.test.builder.ContratoMedicaoBuilder.newContratoMedicaoBuilder;
import static br.gov.planejamento.siconv.med.test.builder.MedicaoBuilder.newMedicaoBuilder;
import static java.math.BigDecimal.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import br.gov.planejamento.siconv.med.contrato.dao.ContratoDAO;
import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.infra.security.domain.Permission;
import br.gov.planejamento.siconv.med.infra.security.domain.Profile;
import br.gov.planejamento.siconv.med.medicao.business.PerfilHelper;
import br.gov.planejamento.siconv.med.medicao.business.SubmetaBC;
import br.gov.planejamento.siconv.med.medicao.business.builder.AbstractSubmetaMedicaoStep;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Pipeline;
import br.gov.planejamento.siconv.med.medicao.dao.ItemMedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.MedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.SubmetaDAO;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoSubmetaEnum;
import br.gov.planejamento.siconv.med.medicao.entity.database.ItemMedicaoBMBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.ItemMedicaoBMValorBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.MedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.SubmetaMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoVrplDTO.ValorServicoBM;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.submetaservicosalvar.FrenteObraSubmetaSalvarDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.submetaservicosalvar.ServicoSubmetaSalvarDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.submetaservicosalvar.SubmetaSalvarDTO;
import br.gov.planejamento.siconv.med.test.builder.SubmetaMedicaoDTOBuilder;
import br.gov.planejamento.siconv.med.test.extension.BusinessControllerBaseTest;
import br.gov.planejamento.siconv.med.test.extension.MockUsuario;

class SalvarSubmetaBMTest extends BusinessControllerBaseTest {

    @Mock
    private ContratoDAO contratoDAO;

    @Mock
    private MedicaoDAO medicaoDAO;

    @Mock
    private SubmetaDAO submetaDAO;

    @Mock
    private ItemMedicaoDAO itemMedicaoDAO;

    @Mock
    private PerfilHelper perfilHelper;

    @Mock
    private SubmetaMedicaoBuilder submetaMedicaoBuilder;

    @Mock
    private Pipeline pipeline;

    @InjectMocks
    private SubmetaBC submetaBC;

    @Captor
    private ArgumentCaptor<ItemMedicaoBMValorBD> itemMedicaoValorCaptor;

    private final Long idContrato = 771L;
    private final Long idMedicao = 891L;
    private final Long idSubmetaVrpl = 321L;
    private final Long idFrenteObra = 99881L;
    private final Long idServico = 66612L;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setup() throws Exception {

        setupDaoMock(ContratoDAO.class, contratoDAO);
        setupDaoMock(MedicaoDAO.class, medicaoDAO);
        setupDaoMock(SubmetaDAO.class, submetaDAO);
        setupDaoMock(ItemMedicaoDAO.class, itemMedicaoDAO);

        when(pipeline.add(Mockito.any(Class.class))).thenReturn(pipeline);
        when(pipeline.add(Mockito.any(AbstractSubmetaMedicaoStep.class))).thenReturn(pipeline);
        when(pipeline.when(Mockito.anyBoolean())).thenReturn(pipeline);
        when(pipeline.orElse()).thenReturn(pipeline);
        when(pipeline.anyway()).thenReturn(pipeline);

        when(submetaMedicaoBuilder.of(Mockito.any(), Mockito.any())).thenReturn(pipeline);

        buildContrato();
        buildMedicao();
    }

    @MockUsuario(profile = Profile.EMPRESA, permissions = Permission.EDITAR_SUBMETA)
    @Test
    void testSalvarSubmetaEmpresa_novoItemMedicaoValor() {

        final BigDecimal qtdPlanejado = valueOf(10);
        final BigDecimal qtdInformada = valueOf(1);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().servicoQtdPlanejado(qtdPlanejado).create();

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));

        SubmetaSalvarDTO submetaJson = buildSubmetaJson(qtdInformada);

        submetaBC.salvarSubmeta(idMedicao, idSubmetaVrpl, submetaJson, false);

        verify(itemMedicaoDAO, times(1)).inserirItemMedicaoBMValor(itemMedicaoValorCaptor.capture());
        assertEquals(qtdInformada, itemMedicaoValorCaptor.getValue().getQtEmpresa());
    }

    @MockUsuario(profile = Profile.EMPRESA, permissions = Permission.EDITAR_SUBMETA)
    @Test
    void testSalvarSubmetaEmpresa_alteracaoItemMedicaoValor() {

        final BigDecimal qtdPlanejado = valueOf(10);
        final BigDecimal qtdRealizadoEmpresa = valueOf(1.1);
        final BigDecimal qtdAcumuladoEmpresa = valueOf(1.1);
        final BigDecimal qtdInformada = valueOf(1);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().servicoQtdPlanejado(qtdPlanejado)
                .servicoQtdRealizadoEmpresa(qtdRealizadoEmpresa).servicoQtdAcumuladoEmpresa(qtdAcumuladoEmpresa)
                .create();

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        ItemMedicaoBMValorBD itemMedicaoValor = buildItemMedicaoValor(qtdRealizadoEmpresa, null, null);

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBMValor(idMedicao, idSubmetaVrpl)).thenReturn(List.of(itemMedicaoValor));

        SubmetaSalvarDTO submetaJson = buildSubmetaJson(qtdInformada);

        submetaBC.salvarSubmeta(idMedicao, idSubmetaVrpl, submetaJson, false);

        verify(itemMedicaoDAO, times(1)).atualizarItemMedicaoBMValor(itemMedicaoValorCaptor.capture());
        assertEquals(qtdInformada, itemMedicaoValorCaptor.getValue().getQtEmpresa());
    }

    @MockUsuario(profile = Profile.EMPRESA)
    @Test
    void testSalvarSubmetaEmpresa_submetaPermiteEdicaoOutroAtor() {

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilderConcedente().permiteMarcacaoEmpresa(false)
                .permiteMarcacaoConvenente(true).permiteMarcacaoConcedente(false).create();

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));

        assertThrowsMedicaoRestException(MessageKey.ERRO_MEDICAO_NAO_PODE_SER_ALTERADA, () -> submetaBC
                .salvarSubmeta(idMedicao, idSubmetaVrpl, buildSubmetaJson(BigDecimal.valueOf(1)), false));
    }

    @MockUsuario(profile = Profile.EMPRESA, permissions = Permission.EDITAR_SUBMETA)
    @Test
    void testSalvarSubmetaEmpresa_servicoNaoPermiteMedicao() {

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().servicoPermiteMedicao(false).create();

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));

        assertThrowsMedicaoRestException(MessageKey.ERRO_ITEM_MEDICAO_NAO_PERMITE_MUDANCA, () -> submetaBC
                .salvarSubmeta(idMedicao, idSubmetaVrpl, buildSubmetaJson(BigDecimal.valueOf(1)), false));
    }

    @MockUsuario(profile = Profile.EMPRESA, permissions = Permission.EDITAR_SUBMETA)
    @Test
    void testSalvarSubmetaEmpresa_acumuladoMaiorPlanejado() {

        final BigDecimal qtdPlanejada = valueOf(10);
        final BigDecimal qtdAcumuladoEmpresa = valueOf(9.5);
        final BigDecimal qtdInformada = valueOf(0.55);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().servicoQtdPlanejado(qtdPlanejada)
                .servicoQtdAcumuladoEmpresa(qtdAcumuladoEmpresa).create();

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));

        SubmetaSalvarDTO submetaJson = buildSubmetaJson(qtdInformada);

        assertThrowsMedicaoRestException(MessageKey.ERRO_ACUMULADO_SERVICO_MAIOR_PLANEJADO,
                List.of(idServico.toString(), idFrenteObra.toString()),
                () -> submetaBC.salvarSubmeta(idMedicao, idSubmetaVrpl, submetaJson, false));
    }

    @MockUsuario(profile = Profile.EMPRESA, permissions = Permission.EDITAR_SUBMETA)
    @Test
    void testSalvarSubmetaEmpresa_valorServicoNaoAlterado() {

        final BigDecimal qtdPlanejada = valueOf(10);
        final BigDecimal qtdRealizadoEmpresa = valueOf(0.5);
        final BigDecimal qtdAcumuladoEmpresa = valueOf(9.5);
        final BigDecimal qtdInformada = qtdRealizadoEmpresa;

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().servicoQtdPlanejado(qtdPlanejada)
                .servicoQtdRealizadoEmpresa(qtdRealizadoEmpresa).servicoQtdAcumuladoEmpresa(qtdAcumuladoEmpresa)
                .create();

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));

        SubmetaSalvarDTO submetaJson = buildSubmetaJson(qtdInformada);

        submetaBC.salvarSubmeta(idMedicao, idSubmetaVrpl, submetaJson, false);

        verify(itemMedicaoDAO, times(0)).atualizarItemMedicaoBMValor(Mockito.any());
        verify(itemMedicaoDAO, times(0)).inserirItemMedicaoBMValor(Mockito.any());
    }

    @MockUsuario(profile = Profile.EMPRESA, permissions = Permission.EDITAR_SUBMETA)
    @Test
    void testSalvarSubmetaEmpresa_valorInformadoZerado() {

        final BigDecimal qtdPlanejada = valueOf(10);
        final BigDecimal qtdRealizadoEmpresa = valueOf(0.2);
        final BigDecimal qtdAcumuladoEmpresa = valueOf(5.5);
        final BigDecimal qtdInformada = BigDecimal.ZERO;

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().servicoQtdPlanejado(qtdPlanejada)
                .servicoQtdRealizadoEmpresa(qtdRealizadoEmpresa).servicoQtdAcumuladoEmpresa(qtdAcumuladoEmpresa)
                .create();

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        ItemMedicaoBMValorBD itemMedicaoValor = buildItemMedicaoValor(qtdRealizadoEmpresa, null, null);

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBMValor(idMedicao, idSubmetaVrpl)).thenReturn(List.of(itemMedicaoValor));

        SubmetaSalvarDTO submetaJson = buildSubmetaJson(qtdInformada);

        submetaBC.salvarSubmeta(idMedicao, idSubmetaVrpl, submetaJson, false);

        verify(itemMedicaoDAO, times(1)).excluirItemMedicaoBMValor(Mockito.any());
    }

    @MockUsuario(profile = Profile.EMPRESA, permissions = Permission.EDITAR_SUBMETA)
    @Test
    void testSalvarSubmetaEmpresa_itemMedicaoInexistente() {

        final Long idFrenteObraInexistente = 999L;
        final Long idServicoInexistente = 999L;

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().create();

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));

        assertThrowsMedicaoRestException(MessageKey.ITEM_MEDICAO_INEXISTENTE, () -> submetaBC.salvarSubmeta(idMedicao,
                idSubmetaVrpl, buildSubmetaJson(idFrenteObraInexistente, idServico, valueOf(1)), false));

        assertThrowsMedicaoRestException(MessageKey.ITEM_MEDICAO_INEXISTENTE, () -> submetaBC.salvarSubmeta(idMedicao,
                idSubmetaVrpl, buildSubmetaJson(idFrenteObra, idServicoInexistente, valueOf(1)), false));
    }

    @MockUsuario(profile = Profile.EMPRESA, permissions = Permission.EDITAR_SUBMETA)
    @Test
    void testSalvarSubmetaEmpresaComplementacao_alteracaoItemMedicaoValor() {

        final BigDecimal qtdPlanejado = valueOf(10);
        final BigDecimal qtdRealizadoEmpresa = valueOf(1.1);
        final BigDecimal qtdAcumuladoEmpresa = valueOf(1.1);
        final BigDecimal qtdInformada = valueOf(1);

        buildMedicaoComplementacaoEmpresa();

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().servicoQtdPlanejado(qtdPlanejado)
                .servicoQtdRealizadoEmpresa(qtdRealizadoEmpresa).servicoQtdAcumuladoEmpresa(qtdAcumuladoEmpresa)
                .create();

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        ItemMedicaoBMValorBD itemMedicaoValor = buildItemMedicaoValor(qtdRealizadoEmpresa, null, null);

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBMValor(idMedicao, idSubmetaVrpl)).thenReturn(List.of(itemMedicaoValor));

        SubmetaSalvarDTO submetaJson = buildSubmetaJson(qtdInformada);

        submetaBC.salvarSubmeta(idMedicao, idSubmetaVrpl, submetaJson, false);

        verify(itemMedicaoDAO, times(1)).atualizarItemMedicaoBMValor(itemMedicaoValorCaptor.capture());
        assertEquals(qtdInformada, itemMedicaoValorCaptor.getValue().getQtEmpresa());
    }

    @MockUsuario(profile = Profile.EMPRESA, permissions = Permission.EDITAR_SUBMETA)
    @Test
    void testSalvarSubmetaEmpresaComplementacao_valorInformadoZerado() {

        final BigDecimal qtdPlanejada = valueOf(10);
        final BigDecimal qtdRealizadoEmpresa = valueOf(0.2);
        final BigDecimal qtdAcumuladoEmpresa = valueOf(5.5);
        final BigDecimal qtdInformada = BigDecimal.ZERO;

        buildMedicaoComplementacaoEmpresa();

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().servicoQtdPlanejado(qtdPlanejada)
                .servicoQtdRealizadoEmpresa(qtdRealizadoEmpresa).servicoQtdAcumuladoEmpresa(qtdAcumuladoEmpresa)
                .create();

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        ItemMedicaoBMValorBD itemMedicaoValor = buildItemMedicaoValor(qtdRealizadoEmpresa, null, null);

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBMValor(idMedicao, idSubmetaVrpl)).thenReturn(List.of(itemMedicaoValor));

        SubmetaSalvarDTO submetaJson = buildSubmetaJson(qtdInformada);

        submetaBC.salvarSubmeta(idMedicao, idSubmetaVrpl, submetaJson, false);

        verify(itemMedicaoDAO, times(1)).excluirItemMedicaoBMValor(Mockito.any());
    }

    @MockUsuario(profile = Profile.EMPRESA, permissions = Permission.EDITAR_SUBMETA)
    @Test
    void testSalvarSubmetaEmpresaComplementacao_valorInformadoNuloMedicaoAcumulada() {

        final BigDecimal qtdPlanejada = valueOf(10);
        final BigDecimal qtdRealizadoEmpresa = valueOf(0.2);
        final BigDecimal qtdAcumuladoEmpresa = valueOf(5.5);
        final BigDecimal qtdInformada = null;

        buildMedicaoComplementacaoEmpresaAcumulada();

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().servicoQtdPlanejado(qtdPlanejada)
                .servicoQtdRealizadoEmpresa(qtdRealizadoEmpresa).servicoQtdAcumuladoEmpresa(qtdAcumuladoEmpresa)
                .create();

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        ItemMedicaoBMValorBD itemMedicaoValor = buildItemMedicaoValor(qtdRealizadoEmpresa, null, null);

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBMValor(idMedicao, idSubmetaVrpl)).thenReturn(List.of(itemMedicaoValor));

        SubmetaSalvarDTO submetaJson = buildSubmetaJson(qtdInformada);

        assertThrowsMedicaoRestException(MessageKey.ERRO_QTDE_INFORMADA_OBRIGATORIA,
                List.of(idServico.toString(), idFrenteObra.toString()),
                () -> submetaBC.salvarSubmeta(idMedicao, idSubmetaVrpl, submetaJson, false));
    }

    @MockUsuario(profile = Profile.EMPRESA, permissions = Permission.EDITAR_SUBMETA)
    @Test
    void testSalvarSubmetaEmpresaComplementacao_valorInformadoZeradoMedicaoAcumulada() {

        final BigDecimal qtdPlanejada = valueOf(10);
        final BigDecimal qtdRealizadoEmpresa = valueOf(0.2);
        final BigDecimal qtdAcumuladoEmpresa = valueOf(5.5);
        final BigDecimal qtdInformada = BigDecimal.ZERO;

        buildMedicaoComplementacaoEmpresaAcumulada();

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().servicoQtdPlanejado(qtdPlanejada)
                .servicoQtdRealizadoEmpresa(qtdRealizadoEmpresa).servicoQtdAcumuladoEmpresa(qtdAcumuladoEmpresa)
                .create();

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        ItemMedicaoBMValorBD itemMedicaoValor = buildItemMedicaoValor(qtdRealizadoEmpresa, null, null);

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBMValor(idMedicao, idSubmetaVrpl)).thenReturn(List.of(itemMedicaoValor));

        SubmetaSalvarDTO submetaJson = buildSubmetaJson(qtdInformada);

        submetaBC.salvarSubmeta(idMedicao, idSubmetaVrpl, submetaJson, false);

        verify(itemMedicaoDAO, times(1)).atualizarItemMedicaoBMValor(itemMedicaoValorCaptor.capture());
        assertEquals(qtdInformada, itemMedicaoValorCaptor.getValue().getQtEmpresa());
    }

    @MockUsuario(profile = Profile.EMPRESA, permissions = Permission.EDITAR_SUBMETA)
    @Test
    void testSalvarSubmetaEmpresaComplementacao_acumuladoMaiorPlanejadoMedicaoPosterior() {

        final BigDecimal qtdPlanejada = valueOf(10);
        final BigDecimal qtdRealizadoEmpresaMedicaoAtual = valueOf(2.5);
        final BigDecimal qtdAcumuladoEmpresaAteMedicaoAtual = valueOf(2.5);
        final BigDecimal qtdRealizadaEmpresaMedicaoPosterior = valueOf(7.5);
        final BigDecimal qtdInformada = valueOf(3);

        buildMedicaoComplementacaoEmpresaAcumulada();

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().servicoQtdPlanejado(qtdPlanejada)
                .servicoQtdRealizadoEmpresa(qtdRealizadoEmpresaMedicaoAtual)
                .servicoQtdAcumuladoEmpresa(qtdAcumuladoEmpresaAteMedicaoAtual).create();

        ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getServicos().get(0);
        servico.getValoresPorIdMedicao().put(1L, new ValorServicoBM(qtdRealizadoEmpresaMedicaoAtual, null, null));
        servico.getValoresPorIdMedicao().put(2L, new ValorServicoBM(qtdRealizadaEmpresaMedicaoPosterior, null, null));

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        ItemMedicaoBMValorBD itemMedicaoValor = buildItemMedicaoValor(qtdRealizadoEmpresaMedicaoAtual, null, null);

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBMValor(idMedicao, idSubmetaVrpl)).thenReturn(List.of(itemMedicaoValor));

        SubmetaSalvarDTO submetaJson = buildSubmetaJson(qtdInformada);

        assertThrowsMedicaoRestException(MessageKey.ERRO_ACUMULADO_SERVICO_MEDICAO_POSTERIOR_MAIOR_PLANEJADO,
                List.of(idServico.toString(), idFrenteObra.toString()),
                () -> submetaBC.salvarSubmeta(idMedicao, idSubmetaVrpl, submetaJson, false));
    }

    @MockUsuario(profile = Profile.EMPRESA, permissions = Permission.EDITAR_SUBMETA)
    @Test
    void testSalvarSubmetaEmpresaComplementacao_reflexoMedicaoEmElaboracaoBloqueada() {

        final BigDecimal qtdPlanejada = valueOf(10);
        final BigDecimal qtdRealizadoEmpresa = valueOf(0.2);
        final BigDecimal qtdAcumuladoEmpresa = valueOf(5.5);
        final BigDecimal qtdInformada = valueOf(1);

        // Configura dados da medição em complementação
        buildMedicaoComplementacaoEmpresa();

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilder().servicoQtdPlanejado(qtdPlanejada)
                .servicoQtdRealizadoEmpresa(qtdRealizadoEmpresa).servicoQtdAcumuladoEmpresa(qtdAcumuladoEmpresa)
                .create();

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        ItemMedicaoBMValorBD itemMedicaoValor = buildItemMedicaoValor(qtdRealizadoEmpresa, null, null);

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBMValor(idMedicao, idSubmetaVrpl)).thenReturn(List.of(itemMedicaoValor));

        // Configura dados da medição em elaboração (bloqueada)
        MedicaoBD medicaoEmElaboracao = newMedicaoBuilder().setId(2L).setMedContrato(idContrato).comSituacao(EM)
                .setBloqueada(true).create();

        SubmetaMedicaoBD submetaMedicaoEmElaboracao = new SubmetaMedicaoBD();
        submetaMedicaoEmElaboracao.setIdSubmetaVrpl(idSubmetaVrpl);
        submetaMedicaoEmElaboracao.setIdMedicao(medicaoEmElaboracao.getId());
        submetaMedicaoEmElaboracao.setSituacaoEmpresa(SituacaoSubmetaEnum.ASS);
        submetaMedicaoEmElaboracao.setDtAssinaturaEmpresa(Instant.now());
        submetaMedicaoEmElaboracao.setNrCpfResponsavelAssinaturaEmpresa("11111111111");

        ItemMedicaoBMValorBD itemMedicaoValorEmElaboracao = new ItemMedicaoBMValorBD(1L, medicaoEmElaboracao.getId());

        when(medicaoDAO.consultarMedicaoporSituacao(idContrato, EM)).thenReturn(List.of(medicaoEmElaboracao));
        when(submetaDAO.consultarSubmetaMedicao(medicaoEmElaboracao.getId(), idSubmetaVrpl))
                .thenReturn(submetaMedicaoEmElaboracao);
        when(itemMedicaoDAO.consultarItemMedicaoBMValor(1L, medicaoEmElaboracao.getId()))
                .thenReturn(Optional.of(itemMedicaoValorEmElaboracao));

        // Efetua chamada do serviço
        submetaBC.salvarSubmeta(idMedicao, idSubmetaVrpl, buildSubmetaJson(qtdInformada), false);

        // Verifica se valor na medição em elaboração foi removido e assinatura apagada
        verify(itemMedicaoDAO, times(1)).excluirItemMedicaoBMValor(itemMedicaoValorEmElaboracao);
        verify(submetaDAO, times(1)).atualizarAssinaturaEmpresa(submetaMedicaoEmElaboracao);
        assertNull(submetaMedicaoEmElaboracao.getDtAssinaturaEmpresa());
        assertNull(submetaMedicaoEmElaboracao.getNrCpfResponsavelAssinaturaEmpresa());
        assertEquals(SituacaoSubmetaEnum.RAS, submetaMedicaoEmElaboracao.getSituacaoEmpresa());
    }

    @MockUsuario(profile = Profile.PROPONENTE_CONVENENTE, permissions = Permission.EDITAR_SUBMETA)
    @Test
    void testSalvarSubmetaConvenente_novoItemMedicaoValor() {
        final BigDecimal qtdPlanejado = valueOf(10);
        final BigDecimal qtdRealizadoEmpresa = valueOf(5);
        final BigDecimal qtdAcumuladoEmpresa = valueOf(5);
        final BigDecimal qtdInformada = valueOf(1);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilderConvenente().servicoQtdPlanejado(qtdPlanejado)
                .servicoQtdRealizadoEmpresa(qtdRealizadoEmpresa).servicoQtdAcumuladoEmpresa(qtdAcumuladoEmpresa)
                .create();

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));

        SubmetaSalvarDTO submetaJson = buildSubmetaJson(qtdInformada);

        submetaBC.salvarSubmeta(idMedicao, idSubmetaVrpl, submetaJson, false);

        verify(itemMedicaoDAO, times(1)).inserirItemMedicaoBMValor(itemMedicaoValorCaptor.capture());
        assertEquals(qtdInformada, itemMedicaoValorCaptor.getValue().getQtConvenente());
    }

    @MockUsuario(profile = Profile.PROPONENTE_CONVENENTE, permissions = Permission.EDITAR_SUBMETA)
    @Test
    void testSalvarSubmetaConvenente_alteracaoItemMedicaoValor() {

        final BigDecimal qtdPlanejado = valueOf(10);
        final BigDecimal qtdAcumuladoEmpresa = valueOf(1.1);
        final BigDecimal qtdRealizadoConvenente = valueOf(0.5);
        final BigDecimal qtdAcumuladoConvenente = valueOf(0.5);
        final BigDecimal qtdInformada = valueOf(0.7);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilderConvenente().servicoQtdPlanejado(qtdPlanejado)
                .servicoQtdAcumuladoEmpresa(qtdAcumuladoEmpresa).servicoQtdRealizadoConvenente(qtdRealizadoConvenente)
                .servicoQtdAcumuladoConvenente(qtdAcumuladoConvenente).create();

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        ItemMedicaoBMValorBD itemMedicaoValor = buildItemMedicaoValor(null, qtdRealizadoConvenente, null);

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBMValor(idMedicao, idSubmetaVrpl)).thenReturn(List.of(itemMedicaoValor));

        SubmetaSalvarDTO submetaJson = buildSubmetaJson(qtdInformada);

        submetaBC.salvarSubmeta(idMedicao, idSubmetaVrpl, submetaJson, false);

        verify(itemMedicaoDAO, times(1)).atualizarItemMedicaoBMValor(itemMedicaoValorCaptor.capture());
        assertEquals(qtdInformada, itemMedicaoValorCaptor.getValue().getQtConvenente());
    }

    @MockUsuario(profile = Profile.PROPONENTE_CONVENENTE)
    @Test
    void testSalvarSubmetaConvenente_submetaPermiteEdicaoOutroAtor() {

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilderConcedente().permiteMarcacaoEmpresa(false)
                .permiteMarcacaoConvenente(false).permiteMarcacaoConcedente(true).create();

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));

        assertThrowsMedicaoRestException(MessageKey.ERRO_MEDICAO_NAO_PODE_SER_ALTERADA, () -> submetaBC
                .salvarSubmeta(idMedicao, idSubmetaVrpl, buildSubmetaJson(BigDecimal.valueOf(1)), false));
    }

    @MockUsuario(profile = Profile.PROPONENTE_CONVENENTE, permissions = Permission.EDITAR_SUBMETA)
    @Test
    void testSalvarSubmetaConvenente_servicoNaoPermiteMedicao() {

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilderConvenente().servicoPermiteMedicao(false).create();

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));

        assertThrowsMedicaoRestException(MessageKey.ERRO_ITEM_MEDICAO_NAO_PERMITE_MUDANCA, () -> submetaBC
                .salvarSubmeta(idMedicao, idSubmetaVrpl, buildSubmetaJson(BigDecimal.valueOf(1)), false));
    }

    @MockUsuario(profile = Profile.PROPONENTE_CONVENENTE, permissions = Permission.EDITAR_SUBMETA)
    @Test
    void testSalvarSubmetaConvenente_acumuladoConvenenteMaiorAcumuladoEmpresa() {

        final BigDecimal qtdPlanejada = valueOf(10);
        final BigDecimal qtdAcumuladoEmpresa = valueOf(9.5);
        final BigDecimal qtdRealizadoConvenente = valueOf(8.0);
        final BigDecimal qtdAcumuladoConvenente = valueOf(9.5);
        final BigDecimal qtdInformada = valueOf(8.5);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilderConvenente().servicoQtdPlanejado(qtdPlanejada)
                .servicoQtdAcumuladoEmpresa(qtdAcumuladoEmpresa).servicoQtdRealizadoConvenente(qtdRealizadoConvenente)
                .servicoQtdAcumuladoConvenente(qtdAcumuladoConvenente).create();

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));

        SubmetaSalvarDTO submetaJson = buildSubmetaJson(qtdInformada);

        assertThrowsMedicaoRestException(MessageKey.ERRO_ACUMULADO_SERVICO_CONVENENTE_MAIOR_ACUMULADO_SERVICO_EMPRESA,
                List.of(idServico.toString(), idFrenteObra.toString()),
                () -> submetaBC.salvarSubmeta(idMedicao, idSubmetaVrpl, submetaJson, false));
    }
    
    @MockUsuario(profile = Profile.PROPONENTE_CONVENENTE, permissions = Permission.EDITAR_SUBMETA)
    @Test
    void testSalvarSubmetaConvenente_acumuladoConvenenteMaiorAcumuladoEmpresaMedicaoPosterior() {

    	// medicao 1: 5 0   
    	// medicao 2: 5 5  -> 10 (alterou na medição 2 para 10)
    	// medicao 3: 2 7        Mas na acumuladora, qtd empresa acumula 12, e qtd convenente acumularia 17
    	
    	final BigDecimal qtdPlanejada = valueOf(15);
    	
        final BigDecimal qtdAcumuladoEmpresaAteMedicao2 = valueOf(10);
        final BigDecimal qtdRealizadoEmpresaAteMedicao3 = valueOf(2);
        
        final BigDecimal qtdRealizadoConvenenteMedicao2 = valueOf(5);
        final BigDecimal qtdRealizadoConvenenteMedicao3 = valueOf(5);
        final BigDecimal qtdAcumuladoConvenenteAteMedicao2 = valueOf(5);
        final BigDecimal qtdInformada = valueOf(10);

        MedicaoBD medicaoBD = newMedicaoBuilder()
        		.setId(2L)
        		.setMedContrato(idContrato)
        		.comSituacao(CC)
                .setAgrupadora(3L).create();
        
        MedicaoDTO medicaoDTO = new MedicaoDTO();
        medicaoDTO.setId(2L);
        medicaoDTO.setIdContrato(idContrato);
        medicaoDTO.setIdMedicaoAgrupadora(3L);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilderConvenente()
        		.servicoQtdPlanejado(qtdPlanejada)
                .servicoQtdAcumuladoEmpresa(qtdAcumuladoEmpresaAteMedicao2)
                .servicoQtdRealizadoConvenente(qtdRealizadoConvenenteMedicao2)
                .servicoQtdAcumuladoConvenente(qtdAcumuladoConvenenteAteMedicao2).create();
        
        ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getServicos().get(0);
        servico.getValoresPorIdMedicao().put(3L, new ValorServicoBM(qtdRealizadoEmpresaAteMedicao3, qtdRealizadoConvenenteMedicao3, null));

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        when(medicaoDAO.consultarMedicao(2L)).thenReturn(medicaoBD);
        when(medicaoDAO.obterMedicao(2L)).thenReturn(medicaoDTO);
        when(medicaoDAO.consultarMedicao(2L)).thenReturn(medicaoBD);
        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, 2L)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));

        SubmetaSalvarDTO submetaJson = buildSubmetaJson(qtdInformada);
        
        assertThrowsMedicaoRestException(ERRO_ACUMULADO_SERVICO_CONVENENTE_MEDICAO_POSTERIOR_MAIOR_ACUMULADO_SERVICO_EMPRESA,
                List.of(idServico.toString(), idFrenteObra.toString()),
                () -> submetaBC.salvarSubmeta(2L, idSubmetaVrpl, submetaJson, false));
    }
    
    @MockUsuario(profile = Profile.PROPONENTE_CONVENENTE, permissions = Permission.EDITAR_SUBMETA)
    @Test
    void testSalvarSubmetaConvenente_acumuladoConvenenteMenorAcumuladoEmpresaMedicaoPosterior() {

    	// medicao 1: 5 0   
    	// medicao 2: 5 3  -> 5 (alterou na medição 2 para 5)
    	// medicao 3: 2 7        Mas na acumuladora, qtd empresa acumula 12, e qtd convenente acumularia 12
    	
    	final BigDecimal qtdPlanejada = valueOf(15);
    	
        final BigDecimal qtdAcumuladoEmpresaAteMedicao2 = valueOf(10);
        final BigDecimal qtdRealizadoEmpresaAteMedicao3 = valueOf(2);
        
        final BigDecimal qtdRealizadoConvenenteMedicao2 = valueOf(3);
        final BigDecimal qtdRealizadoConvenenteMedicao3 = valueOf(5);
        final BigDecimal qtdAcumuladoConvenenteAteMedicao2 = valueOf(3);
        final BigDecimal qtdInformada = valueOf(5);

        MedicaoBD medicaoBD = newMedicaoBuilder()
        		.setId(2L)
        		.setMedContrato(idContrato)
        		.comSituacao(CC)
                .setAgrupadora(3L).create();
        
        MedicaoDTO medicaoDTO = new MedicaoDTO();
        medicaoDTO.setId(2L);
        medicaoDTO.setIdContrato(idContrato);
        medicaoDTO.setIdMedicaoAgrupadora(3L);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilderConvenente()
        		.servicoQtdPlanejado(qtdPlanejada)
                .servicoQtdAcumuladoEmpresa(qtdAcumuladoEmpresaAteMedicao2)
                .servicoQtdRealizadoConvenente(qtdRealizadoConvenenteMedicao2)
                .servicoQtdAcumuladoConvenente(qtdAcumuladoConvenenteAteMedicao2).create();
        
        ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getServicos().get(0);
        servico.getValoresPorIdMedicao().put(3L, new ValorServicoBM(qtdRealizadoEmpresaAteMedicao3, qtdRealizadoConvenenteMedicao3, null));

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        when(medicaoDAO.consultarMedicao(2L)).thenReturn(medicaoBD);
        when(medicaoDAO.obterMedicao(2L)).thenReturn(medicaoDTO);
        when(medicaoDAO.consultarMedicao(2L)).thenReturn(medicaoBD);
        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, 2L)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));

        SubmetaSalvarDTO submetaJson = buildSubmetaJson(qtdInformada);
        
        submetaBC.salvarSubmeta(2L, idSubmetaVrpl, submetaJson, false);

        verify(itemMedicaoDAO, times(1)).inserirItemMedicaoBMValor(itemMedicaoValorCaptor.capture());
        assertEquals(qtdInformada, itemMedicaoValorCaptor.getValue().getQtConvenente());
    }
    
    @MockUsuario(profile = Profile.PROPONENTE_CONVENENTE, permissions = Permission.EDITAR_SUBMETA)
    @Test
    void testSalvarSubmetaConvenente_complementacaoConvenenteComMedicaoPosteriorEmAteste() {

    	// medicao 1: 5 0   
    	// medicao 2: 5 3  -> 5 (alterou na medição 2 para 5)
    	// medicao 3: 2 7        Mas na posterior em ateste, empresa acumula 12, e convenente acumularia 12
    	
    	final BigDecimal qtdPlanejada = valueOf(15);
    	
        final BigDecimal qtdAcumuladoEmpresaAteMedicao2 = valueOf(10);
        final BigDecimal qtdRealizadoEmpresaAteMedicao3 = valueOf(2);
        
        final BigDecimal qtdRealizadoConvenenteMedicao2 = valueOf(3);
        final BigDecimal qtdRealizadoConvenenteMedicao3 = valueOf(5);
        final BigDecimal qtdAcumuladoConvenenteAteMedicao2 = valueOf(3);
        final BigDecimal qtdInformada = valueOf(5);

        MedicaoBD medicaoBD = newMedicaoBuilder()
        		.setId(2L)
        		.setMedContrato(idContrato)
        		.comSituacao(CC).create();
        
        MedicaoDTO medicaoDTO = new MedicaoDTO();
        medicaoDTO.setId(2L);
        medicaoDTO.setIdContrato(idContrato);
        medicaoDTO.setIdMedicaoAgrupadora(3L);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilderConvenente()
        		.servicoQtdPlanejado(qtdPlanejada)
                .servicoQtdAcumuladoEmpresa(qtdAcumuladoEmpresaAteMedicao2)
                .servicoQtdRealizadoConvenente(qtdRealizadoConvenenteMedicao2)
                .servicoQtdAcumuladoConvenente(qtdAcumuladoConvenenteAteMedicao2).create();
        
        ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getServicos().get(0);
        servico.getValoresPorIdMedicao().put(3L, new ValorServicoBM(qtdRealizadoEmpresaAteMedicao3, qtdRealizadoConvenenteMedicao3, null));

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();
        
        // Configura dados da medição em elaboração (bloqueada)
        MedicaoBD medicaoEmAteste = newMedicaoBuilder().setId(3L).setMedContrato(idContrato).comSituacao(AT)
                .setBloqueada(true).create();
        
        SubmetaMedicaoBD submetaMedicaoEmAteste = new SubmetaMedicaoBD();
        submetaMedicaoEmAteste.setIdSubmetaVrpl(idSubmetaVrpl);
        submetaMedicaoEmAteste.setIdMedicao(medicaoEmAteste.getId());
        submetaMedicaoEmAteste.setSituacaoConvenente(SituacaoSubmetaEnum.ASS);
        submetaMedicaoEmAteste.setDtAssinaturaConvenente(Instant.now());
        submetaMedicaoEmAteste.setNrCpfResponsavelAssinaturaConvenente("11111111111");
        
        ItemMedicaoBMValorBD itemMedicaoValorEmAteste = new ItemMedicaoBMValorBD(3L, medicaoEmAteste.getId());
        itemMedicaoValorEmAteste.setQtEmpresa(qtdRealizadoEmpresaAteMedicao3);
        itemMedicaoValorEmAteste.setQtConvenente(qtdRealizadoConvenenteMedicao3);

        when(medicaoDAO.consultarMedicao(2L)).thenReturn(medicaoBD);
        when(medicaoDAO.obterMedicao(2L)).thenReturn(medicaoDTO);
        when(medicaoDAO.consultarMedicao(2L)).thenReturn(medicaoBD);
        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, 2L)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));
        when(medicaoDAO.consultarMedicaoporSituacao(idContrato, AT)).thenReturn(List.of(medicaoEmAteste));
        when(submetaDAO.consultarSubmetaMedicao(medicaoEmAteste.getId(), idSubmetaVrpl)).thenReturn(submetaMedicaoEmAteste);
        when(itemMedicaoDAO.consultarItemMedicaoBMValor(1L, medicaoEmAteste.getId())).thenReturn(Optional.of(itemMedicaoValorEmAteste));
        
        SubmetaSalvarDTO submetaJson = buildSubmetaJson(qtdInformada);
        
        submetaBC.salvarSubmeta(2L, idSubmetaVrpl, submetaJson, false);

        verify(itemMedicaoDAO, times(1)).limparMedicaoConvenenteBM(Mockito.any());
        verify(submetaDAO, times(1)).atualizarAssinaturaConvenente(Mockito.any());
    }
    
    @MockUsuario(profile = Profile.PROPONENTE_CONVENENTE, permissions = Permission.EDITAR_SUBMETA)
    @Test
    void testSalvarSubmetaConvenente_complementacaoConvenenteComMedicaoPosteriorEmAtesteSemQtdEmpresa() {

    	// medicao 1: 5 0   
    	// medicao 2: 0 3  -> 5 (alterou na medição 2 para 5)
    	// medicao 3: 7 7        Mas na posterior em ateste, empresa acumula 12, e convenente acumularia 12
    	
    	final BigDecimal qtdPlanejada = valueOf(15);
    	
        final BigDecimal qtdAcumuladoEmpresaAteMedicao2 = valueOf(5);
        final BigDecimal qtdRealizadoEmpresaAteMedicao3 = valueOf(7);
        
        final BigDecimal qtdRealizadoConvenenteMedicao2 = valueOf(3);
        final BigDecimal qtdRealizadoConvenenteMedicao3 = valueOf(5);
        final BigDecimal qtdAcumuladoConvenenteAteMedicao2 = valueOf(3);
        final BigDecimal qtdInformada = valueOf(5);

        MedicaoBD medicaoBD = newMedicaoBuilder()
        		.setId(2L)
        		.setMedContrato(idContrato)
        		.comSituacao(CC).create();
        
        MedicaoDTO medicaoDTO = new MedicaoDTO();
        medicaoDTO.setId(2L);
        medicaoDTO.setIdContrato(idContrato);
        medicaoDTO.setIdMedicaoAgrupadora(3L);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilderConvenente()
        		.servicoQtdPlanejado(qtdPlanejada)
                .servicoQtdAcumuladoEmpresa(qtdAcumuladoEmpresaAteMedicao2)
                .servicoQtdRealizadoConvenente(qtdRealizadoConvenenteMedicao2)
                .servicoQtdAcumuladoConvenente(qtdAcumuladoConvenenteAteMedicao2).create();
        
        ServicoVrplDTO servico = submetaMedicao.getFrentesObra().get(0).getServicos().get(0);
        servico.getValoresPorIdMedicao().put(3L, new ValorServicoBM(qtdRealizadoEmpresaAteMedicao3, qtdRealizadoConvenenteMedicao3, null));

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();
        
        // Configura dados da medição em elaboração (bloqueada)
        MedicaoBD medicaoEmAteste = newMedicaoBuilder().setId(3L).setMedContrato(idContrato).comSituacao(AT)
                .setBloqueada(true).create();
        
        SubmetaMedicaoBD submetaMedicaoEmAteste = new SubmetaMedicaoBD();
        submetaMedicaoEmAteste.setIdSubmetaVrpl(idSubmetaVrpl);
        submetaMedicaoEmAteste.setIdMedicao(medicaoEmAteste.getId());
        
        ItemMedicaoBMValorBD itemMedicaoValorEmAteste = new ItemMedicaoBMValorBD(3L, medicaoEmAteste.getId());
        itemMedicaoValorEmAteste.setQtConvenente(qtdRealizadoConvenenteMedicao3);

        when(medicaoDAO.consultarMedicao(2L)).thenReturn(medicaoBD);
        when(medicaoDAO.obterMedicao(2L)).thenReturn(medicaoDTO);
        when(medicaoDAO.consultarMedicao(2L)).thenReturn(medicaoBD);
        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, 2L)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));
        when(medicaoDAO.consultarMedicaoporSituacao(idContrato, AT)).thenReturn(List.of(medicaoEmAteste));
        when(submetaDAO.consultarSubmetaMedicao(medicaoEmAteste.getId(), idSubmetaVrpl)).thenReturn(submetaMedicaoEmAteste);
        when(itemMedicaoDAO.consultarItemMedicaoBMValor(1L, medicaoEmAteste.getId())).thenReturn(Optional.of(itemMedicaoValorEmAteste));
        
        SubmetaSalvarDTO submetaJson = buildSubmetaJson(qtdInformada);
        
        submetaBC.salvarSubmeta(2L, idSubmetaVrpl, submetaJson, false);

        verify(itemMedicaoDAO, times(1)).excluirItemMedicaoBMValor(Mockito.any());
        verify(submetaDAO, times(0)).atualizarAssinaturaConvenente(Mockito.any());
    }

    @MockUsuario(profile = Profile.PROPONENTE_CONVENENTE, permissions = Permission.EDITAR_SUBMETA)
    @Test
    void testSalvarSubmetaConvenente_valorServicoNaoAlterado() {

        final BigDecimal qtdPlanejada = valueOf(10);
        final BigDecimal qtdAcumuladoEmpresa = valueOf(9.5);
        final BigDecimal qtdRealizadoConvenente = valueOf(8.0);
        final BigDecimal qtdAcumuladoConvenente = valueOf(1.0);
        final BigDecimal qtdInformada = qtdRealizadoConvenente;

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilderConvenente().servicoQtdPlanejado(qtdPlanejada)
                .servicoQtdAcumuladoEmpresa(qtdAcumuladoEmpresa).servicoQtdRealizadoConvenente(qtdRealizadoConvenente)
                .servicoQtdAcumuladoConvenente(qtdAcumuladoConvenente).create();

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));

        SubmetaSalvarDTO submetaJson = buildSubmetaJson(qtdInformada);

        submetaBC.salvarSubmeta(idMedicao, idSubmetaVrpl, submetaJson, false);

        verify(itemMedicaoDAO, times(0)).atualizarItemMedicaoBMValor(Mockito.any());
    }

    @MockUsuario(profile = Profile.PROPONENTE_CONVENENTE, permissions = Permission.EDITAR_SUBMETA)
    @Test
    void testSalvarSubmetaConvenente_valorInformadoZerado() {

        final BigDecimal qtdPlanejada = valueOf(10);
        final BigDecimal qtdAcumuladoEmpresa = valueOf(5.5);
        final BigDecimal qtdRealizadoConvenente = valueOf(2.0);
        final BigDecimal qtdAcumuladoConvenente = valueOf(1.0);
        final BigDecimal qtdInformada = BigDecimal.ZERO;

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilderConvenente().servicoQtdPlanejado(qtdPlanejada)
                .servicoQtdAcumuladoEmpresa(qtdAcumuladoEmpresa).servicoQtdRealizadoConvenente(qtdRealizadoConvenente)
                .servicoQtdAcumuladoConvenente(qtdAcumuladoConvenente).create();

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        ItemMedicaoBMValorBD itemMedicaoValor = buildItemMedicaoValor(null, qtdRealizadoConvenente, null);

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBMValor(idMedicao, idSubmetaVrpl)).thenReturn(List.of(itemMedicaoValor));

        SubmetaSalvarDTO submetaJson = buildSubmetaJson(qtdInformada);

        submetaBC.salvarSubmeta(idMedicao, idSubmetaVrpl, submetaJson, false);

        verify(itemMedicaoDAO, times(1)).atualizarItemMedicaoBMValor(itemMedicaoValorCaptor.capture());
        assertEquals(qtdInformada, itemMedicaoValorCaptor.getValue().getQtConvenente());
    }

    @MockUsuario(profile = Profile.PROPONENTE_CONVENENTE, permissions = Permission.EDITAR_SUBMETA)
    @Test
    void testSalvarSubmetaConvenente_valorInformadoNulo() {

        final BigDecimal qtdPlanejada = valueOf(10);
        final BigDecimal qtdAcumuladoEmpresa = valueOf(5.5);
        final BigDecimal qtdRealizadoConvenente = valueOf(2.0);
        final BigDecimal qtdAcumuladoConvenente = valueOf(1.0);
        final BigDecimal qtdInformada = null;

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilderConvenente().servicoQtdPlanejado(qtdPlanejada)
                .servicoQtdAcumuladoEmpresa(qtdAcumuladoEmpresa).servicoQtdRealizadoConvenente(qtdRealizadoConvenente)
                .servicoQtdAcumuladoConvenente(qtdAcumuladoConvenente).create();

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        ItemMedicaoBMValorBD itemMedicaoValor = buildItemMedicaoValor(null, qtdRealizadoConvenente, null);

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBMValor(idMedicao, idSubmetaVrpl)).thenReturn(List.of(itemMedicaoValor));

        SubmetaSalvarDTO submetaJson = buildSubmetaJson(qtdInformada);

        assertThrowsMedicaoRestException(MessageKey.ERRO_QTDE_INFORMADA_OBRIGATORIA,
                List.of(idServico.toString(), idFrenteObra.toString()),
                () -> submetaBC.salvarSubmeta(idMedicao, idSubmetaVrpl, submetaJson, false));
    }

    @MockUsuario(profile = Profile.CONCEDENTE)
    @Test
    void testSalvarSubmetaConcedente_novoItemMedicaoValor() {
        final BigDecimal qtdRealizadoConvenente = valueOf(5);
        final BigDecimal qtdAcumuladoConvenente = valueOf(5);
        final BigDecimal qtdInformada = valueOf(1);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilderConcedente()
                .servicoQtdRealizadoConvenente(qtdRealizadoConvenente)
                .servicoQtdAcumuladoConvenente(qtdAcumuladoConvenente).create();

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));

        SubmetaSalvarDTO submetaJson = buildSubmetaJson(qtdInformada);

        submetaBC.salvarSubmeta(idMedicao, idSubmetaVrpl, submetaJson, false);

        verify(itemMedicaoDAO, times(1)).inserirItemMedicaoBMValor(itemMedicaoValorCaptor.capture());
        assertEquals(qtdInformada, itemMedicaoValorCaptor.getValue().getQtConcedente());
    }

    @MockUsuario(profile = Profile.MANDATARIA)
    @Test
    void testSalvarSubmetaMandataria_novoItemMedicaoValor() {
        final BigDecimal qtdRealizadoConvenente = valueOf(5);
        final BigDecimal qtdAcumuladoConvenente = valueOf(5);
        final BigDecimal qtdInformada = valueOf(1);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilderConcedente()
                .servicoQtdRealizadoConvenente(qtdRealizadoConvenente)
                .servicoQtdAcumuladoConvenente(qtdAcumuladoConvenente).create();

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));

        SubmetaSalvarDTO submetaJson = buildSubmetaJson(qtdInformada);

        submetaBC.salvarSubmeta(idMedicao, idSubmetaVrpl, submetaJson, false);

        verify(itemMedicaoDAO, times(1)).inserirItemMedicaoBMValor(itemMedicaoValorCaptor.capture());
        assertEquals(qtdInformada, itemMedicaoValorCaptor.getValue().getQtConcedente());
    }

    @MockUsuario(profile = Profile.CONCEDENTE)
    @Test
    void testSalvarSubmetaConcedente_alteracaoItemMedicaoValor() {

        final BigDecimal qtdAcumuladoConvenente = valueOf(1.1);
        final BigDecimal qtdRealizadoConcedente = valueOf(0.5);
        final BigDecimal qtdAcumuladoConcedente = valueOf(0.5);
        final BigDecimal qtdInformada = valueOf(0.7);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilderConcedente()
                .servicoQtdAcumuladoConvenente(qtdAcumuladoConvenente)
                .servicoQtdRealizadoConcedente(qtdRealizadoConcedente)
                .servicoQtdAcumuladoConcedente(qtdAcumuladoConcedente).create();

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        ItemMedicaoBMValorBD itemMedicaoValor = buildItemMedicaoValor(null, null, qtdRealizadoConcedente);

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBMValor(idMedicao, idSubmetaVrpl)).thenReturn(List.of(itemMedicaoValor));

        SubmetaSalvarDTO submetaJson = buildSubmetaJson(qtdInformada);

        submetaBC.salvarSubmeta(idMedicao, idSubmetaVrpl, submetaJson, false);

        verify(itemMedicaoDAO, times(1)).atualizarItemMedicaoBMValor(itemMedicaoValorCaptor.capture());
        assertEquals(qtdInformada, itemMedicaoValorCaptor.getValue().getQtConcedente());
    }

    @MockUsuario(profile = Profile.MANDATARIA)
    @Test
    void testSalvarSubmetaMandataria_alteracaoItemMedicaoValor() {

        final BigDecimal qtdAcumuladoConvenente = valueOf(1.1);
        final BigDecimal qtdRealizadoConcedente = valueOf(0.5);
        final BigDecimal qtdAcumuladoConcedente = valueOf(0.5);
        final BigDecimal qtdInformada = valueOf(0.7);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilderConcedente()
                .servicoQtdAcumuladoConvenente(qtdAcumuladoConvenente)
                .servicoQtdRealizadoConcedente(qtdRealizadoConcedente)
                .servicoQtdAcumuladoConcedente(qtdAcumuladoConcedente).create();

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        ItemMedicaoBMValorBD itemMedicaoValor = buildItemMedicaoValor(null, null, qtdRealizadoConcedente);

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBMValor(idMedicao, idSubmetaVrpl)).thenReturn(List.of(itemMedicaoValor));

        SubmetaSalvarDTO submetaJson = buildSubmetaJson(qtdInformada);

        submetaBC.salvarSubmeta(idMedicao, idSubmetaVrpl, submetaJson, false);

        verify(itemMedicaoDAO, times(1)).atualizarItemMedicaoBMValor(itemMedicaoValorCaptor.capture());
        assertEquals(qtdInformada, itemMedicaoValorCaptor.getValue().getQtConcedente());
    }

    @MockUsuario(profile = Profile.CONCEDENTE)
    @Test
    void testSalvarSubmetaConcedente_submetaPermiteEdicaoOutroAtor() {

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilderConcedente().permiteMarcacaoEmpresa(true)
                .permiteMarcacaoConvenente(false).permiteMarcacaoConcedente(false).create();

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));

        assertThrowsMedicaoRestException(MessageKey.ERRO_MEDICAO_NAO_PODE_SER_ALTERADA, () -> submetaBC
                .salvarSubmeta(idMedicao, idSubmetaVrpl, buildSubmetaJson(BigDecimal.valueOf(1)), false));
    }

    @MockUsuario(profile = Profile.CONCEDENTE)
    @Test
    void testSalvarSubmetaConcedente_servicoNaoPermiteMedicao() {

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilderConcedente().servicoPermiteMedicao(false).create();

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));

        assertThrowsMedicaoRestException(MessageKey.ERRO_ITEM_MEDICAO_NAO_PERMITE_MUDANCA, () -> submetaBC
                .salvarSubmeta(idMedicao, idSubmetaVrpl, buildSubmetaJson(BigDecimal.valueOf(1)), false));
    }

    @MockUsuario(profile = Profile.CONCEDENTE)
    @Test
    void testSalvarSubmetaConcedente_acumuladoConcedenteMaiorAcumuladoConvenente() {

        final BigDecimal qtdAcumuladoConvenente = valueOf(9.5);
        final BigDecimal qtdRealizadoConcedente = valueOf(8.0);
        final BigDecimal qtdAcumuladoConcedente = valueOf(9.5);
        final BigDecimal qtdInformada = valueOf(8.5);

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilderConcedente()
                .servicoQtdAcumuladoConvenente(qtdAcumuladoConvenente)
                .servicoQtdRealizadoConcedente(qtdRealizadoConcedente)
                .servicoQtdAcumuladoConcedente(qtdAcumuladoConcedente).create();

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));

        SubmetaSalvarDTO submetaJson = buildSubmetaJson(qtdInformada);

        assertThrowsMedicaoRestException(MessageKey.ERRO_ACUMULADO_SERVICO_CONCEDENTE_MAIOR_ACUMULADO_SERVICO_CONVENENTE,
                List.of(idServico.toString(), idFrenteObra.toString()),
                () -> submetaBC.salvarSubmeta(idMedicao, idSubmetaVrpl, submetaJson, false));
    }

    @MockUsuario(profile = Profile.CONCEDENTE)
    @Test
    void testSalvarSubmetaConcedente_valorServicoNaoAlterado() {

        final BigDecimal qtdAcumuladoConvenente = valueOf(9.5);
        final BigDecimal qtdRealizadoConcedente = valueOf(8.0);
        final BigDecimal qtdAcumuladoConcedente = valueOf(1.0);
        final BigDecimal qtdInformada = qtdRealizadoConcedente;

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilderConcedente()
                .servicoQtdAcumuladoConvenente(qtdAcumuladoConvenente)
                .servicoQtdRealizadoConcedente(qtdRealizadoConcedente)
                .servicoQtdAcumuladoConcedente(qtdAcumuladoConcedente).create();

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));

        SubmetaSalvarDTO submetaJson = buildSubmetaJson(qtdInformada);

        submetaBC.salvarSubmeta(idMedicao, idSubmetaVrpl, submetaJson, false);

        verify(itemMedicaoDAO, times(0)).atualizarItemMedicaoBMValor(Mockito.any());
    }

    @MockUsuario(profile = Profile.CONCEDENTE)
    @Test
    void testSalvarSubmetaConcedente_valorInformadoZerado() {

        final BigDecimal qtdAcumuladoConvenente = valueOf(5.5);
        final BigDecimal qtdRealizadoConcedente = valueOf(2.0);
        final BigDecimal qtdAcumuladoConcedente = valueOf(1.0);
        final BigDecimal qtdInformada = BigDecimal.ZERO;

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilderConcedente()
                .servicoQtdAcumuladoConvenente(qtdAcumuladoConvenente)
                .servicoQtdRealizadoConcedente(qtdRealizadoConcedente)
                .servicoQtdAcumuladoConcedente(qtdAcumuladoConcedente).create();

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        ItemMedicaoBMValorBD itemMedicaoValor = buildItemMedicaoValor(null, null, qtdRealizadoConcedente);

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBMValor(idMedicao, idSubmetaVrpl)).thenReturn(List.of(itemMedicaoValor));

        SubmetaSalvarDTO submetaJson = buildSubmetaJson(qtdInformada);

        submetaBC.salvarSubmeta(idMedicao, idSubmetaVrpl, submetaJson, false);

        verify(itemMedicaoDAO, times(1)).atualizarItemMedicaoBMValor(itemMedicaoValorCaptor.capture());
        assertEquals(qtdInformada, itemMedicaoValorCaptor.getValue().getQtConcedente());
    }

    @MockUsuario(profile = Profile.CONCEDENTE)
    @Test
    void testSalvarSubmetaConcedente_valorInformadoNulo() {

        final BigDecimal qtdAcumuladoConvenente = valueOf(5.5);
        final BigDecimal qtdRealizadoConcedente = valueOf(2.0);
        final BigDecimal qtdAcumuladoConcedente = valueOf(1.0);
        final BigDecimal qtdInformada = null;

        SubmetaMedicaoDTO submetaMedicao = getSubmetaBuilderConcedente()
                .servicoQtdAcumuladoEmpresa(qtdAcumuladoConvenente)
                .servicoQtdRealizadoConvenente(qtdRealizadoConcedente)
                .servicoQtdAcumuladoConvenente(qtdAcumuladoConcedente).create();

        ItemMedicaoBMBD itemMedicao = buildItemMedicao();

        ItemMedicaoBMValorBD itemMedicaoValor = buildItemMedicaoValor(null, null, qtdRealizadoConcedente);

        when(submetaDAO.listarSubmetasMedicaoBM(idContrato, idMedicao)).thenReturn(List.of(submetaMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBM(idContrato, idSubmetaVrpl)).thenReturn(List.of(itemMedicao));
        when(itemMedicaoDAO.listarItemMedicaoBMValor(idMedicao, idSubmetaVrpl)).thenReturn(List.of(itemMedicaoValor));

        SubmetaSalvarDTO submetaJson = buildSubmetaJson(qtdInformada);

        assertThrowsMedicaoRestException(MessageKey.ERRO_QTDE_INFORMADA_OBRIGATORIA,
                List.of(idServico.toString(), idFrenteObra.toString()),
                () -> submetaBC.salvarSubmeta(idMedicao, idSubmetaVrpl, submetaJson, false));
    }

    // =========================== Métodos utilitários ===========================

    private void buildContrato() {

        ContratoBD contrato = newContratoMedicaoBuilder().setId(idContrato).porEventos(false).create();

        when(contratoDAO.consultarContratoAssociadoMedicao(idMedicao)).thenReturn(contrato);
        when(contratoDAO.consultarContrato(idContrato)).thenReturn(contrato);
    }

    private void buildMedicao() {
        buildMedicao(EM, null);
    }

    private void buildMedicaoComplementacaoEmpresa() {
        buildMedicao(CE, null);
    }

    private void buildMedicaoComplementacaoEmpresaAcumulada() {
        buildMedicao(CE, 2L);
    }

    private void buildMedicao(SituacaoMedicaoEnum situacao, Long idAgrupadora) {

        MedicaoBD medicaoBD = newMedicaoBuilder().setId(idMedicao).setMedContrato(idContrato).comSituacao(situacao)
                .setAgrupadora(idAgrupadora).create();

        MedicaoDTO medicaoDTO = new MedicaoDTO();
        medicaoDTO.setId(idMedicao);
        medicaoDTO.setIdContrato(idContrato);
        medicaoDTO.setIdMedicaoAgrupadora(idAgrupadora);

        when(medicaoDAO.obterMedicao(idMedicao)).thenReturn(medicaoDTO);
        when(medicaoDAO.consultarMedicao(idMedicao)).thenReturn(medicaoBD);
    }

    private SubmetaMedicaoDTOBuilder getSubmetaBuilder() {
        return new SubmetaMedicaoDTOBuilder(idSubmetaVrpl, idFrenteObra, idServico).permiteMarcacaoEmpresa(true)
                .servicoPermiteMedicao(true);
    }

    private SubmetaMedicaoDTOBuilder getSubmetaBuilderConvenente() {
        return new SubmetaMedicaoDTOBuilder(idSubmetaVrpl, idFrenteObra, idServico).permiteMarcacaoConvenente(true)
                .servicoPermiteMedicao(true);
    }

    private SubmetaMedicaoDTOBuilder getSubmetaBuilderConcedente() {
        return new SubmetaMedicaoDTOBuilder(idSubmetaVrpl, idFrenteObra, idServico).permiteMarcacaoConcedente(true)
                .servicoPermiteMedicao(true);
    }

    private SubmetaSalvarDTO buildSubmetaJson(BigDecimal qtdInformadaServico) {
        return buildSubmetaJson(idFrenteObra, idServico, qtdInformadaServico);
    }

    private SubmetaSalvarDTO buildSubmetaJson(Long idFrenteObra, Long idServico, BigDecimal qtdInformadaServico) {

        ServicoSubmetaSalvarDTO servicoJson = new ServicoSubmetaSalvarDTO();
        servicoJson.setId(idServico);
        servicoJson.setQtdInformada(qtdInformadaServico);

        FrenteObraSubmetaSalvarDTO frenteObraJson = new FrenteObraSubmetaSalvarDTO();
        frenteObraJson.setId(idFrenteObra);
        frenteObraJson.setServicos(List.of(servicoJson));

        SubmetaSalvarDTO submetaJson = new SubmetaSalvarDTO();
        submetaJson.setFrentesObra(List.of(frenteObraJson));

        return submetaJson;
    }

    private ItemMedicaoBMBD buildItemMedicao() {
        ItemMedicaoBMBD item = new ItemMedicaoBMBD(idContrato, idSubmetaVrpl, idFrenteObra, idServico,
                BigDecimal.valueOf(10), null);
        item.setIdItemMedicaoBM(1L);
        return item;
    }

    private ItemMedicaoBMValorBD buildItemMedicaoValor(BigDecimal qtdRealizadoEmpresa,
            BigDecimal qtdRealizadoConvenente, BigDecimal qtdRealizadoConcedente) {
        ItemMedicaoBMValorBD itemMedicaoValor = new ItemMedicaoBMValorBD(1L, idMedicao);
        itemMedicaoValor.setIdItemMedicaoBMValor(1L);
        itemMedicaoValor.setQtEmpresa(qtdRealizadoEmpresa);
        itemMedicaoValor.setQtConvenente(qtdRealizadoConvenente);
        itemMedicaoValor.setQtConcedente(qtdRealizadoConcedente);
        return itemMedicaoValor;
    }
}
