package br.gov.planejamento.siconv.med.medicao.business.submeta;

import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.EMPRESA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static java.lang.String.format;


import java.util.ArrayList;
import java.util.List;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleCallback;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.TipoResponsavelTecnicoEnum;
import br.gov.planejamento.siconv.med.contrato.dao.ContratoDAO;
import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.infra.database.DAOFactory;
import br.gov.planejamento.siconv.med.infra.exception.MedicaoRestException;
import br.gov.planejamento.siconv.med.integration.contratos.ContratosGrpcConsumer;
import br.gov.planejamento.siconv.med.integration.siconv.SiconvGRPCConsumer;
import br.gov.planejamento.siconv.med.integration.vrpl.VrplGRPCConsumer;
import br.gov.planejamento.siconv.med.medicao.business.SubmetaBC;
import br.gov.planejamento.siconv.med.medicao.dao.ItemMedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.MedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.SubmetaDAO;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoSubmetaEnum;
import br.gov.planejamento.siconv.med.medicao.entity.database.ItemMedicaoBMValorBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.MedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.SubmetaMedicaoBD;
import br.gov.planejamento.siconv.med.infra.message.Message;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.infra.security.UsuarioLogado;
import br.gov.planejamento.siconv.med.infra.security.domain.Profile;


class SubmetaBC_ExcluirRascunhoSubmetaTest {

	@Mock
	private Jdbi jdbi;
	
	@Mock
	private DAOFactory dao;

	@Mock
	private Handle handle;

	@Mock
	private SecurityContext securityContext;
	
	@Mock
	private VrplGRPCConsumer vrplConsumer;
	
	@Mock
	private ContratosGrpcConsumer contratosConsumer;
	
	@Mock
	private SiconvGRPCConsumer siconvGRPCConsumer;
	
	@Mock
	private ContratoDAO contratoDAO;
	
	@Mock
	private MedicaoDAO medicaoDAO;
	
	@Mock
	private SubmetaDAO submetaDAO;
	
	@Mock
	private ItemMedicaoDAO itemMedicaoDAO;
	
	@InjectMocks
	private SubmetaBC submetaBC;
	
	@Captor
	private ArgumentCaptor<SubmetaMedicaoBD> subMedicaoCaptor;
	
	@BeforeEach
	void setup() throws Exception {

		MockitoAnnotations.initMocks(this);

		dao = mock(DAOFactory.class);
		submetaBC.setDao(dao);
		when(dao.get(ContratoDAO.class)).thenReturn(contratoDAO);
		when(dao.get(MedicaoDAO.class)).thenReturn(medicaoDAO);
		when(dao.get(ItemMedicaoDAO.class)).thenReturn(itemMedicaoDAO);
		when(dao.get(SubmetaDAO.class)).thenReturn(submetaDAO);
		when(dao.getJdbi()).thenReturn(jdbi);
		
		when(handle.attach(ItemMedicaoDAO.class)).thenReturn(itemMedicaoDAO);
		when(handle.attach(SubmetaDAO.class)).thenReturn(submetaDAO);
		when(jdbi.onDemand(SubmetaDAO.class)).thenReturn(submetaDAO);
		
		when(jdbi.inTransaction(Mockito.any())).then(invocation -> {
			HandleCallback<?, ?> callback = invocation.getArgument(0);
			return callback.withHandle(handle);
		});

		doAnswer(invocation -> {
			HandleConsumer<?> consumer = invocation.getArgument(0);
			consumer.useHandle(handle);
			return null;
		}).when(jdbi).useTransaction(Mockito.any());

	}

	@Test
	void excluirRascunhoSubmeta_SubmetaNula() {
		
		Long idMedicao = 1L;
		Long idSubmetaVrpl = 1L;

		SubmetaMedicaoBD submetaMedicaoBD = new SubmetaMedicaoBD();
		
		MedicaoBD medicao = new MedicaoBD();
		
		when(submetaDAO.consultarSubmetaMedicao(idMedicao, idSubmetaVrpl)).thenReturn(submetaMedicaoBD);
		when(medicaoDAO.consultarMedicao(idMedicao)).thenReturn(medicao);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_SUBMETA_INEXISTENTE,
				() -> submetaBC.excluirRascunhoSubmeta(medicao.getId(), submetaMedicaoBD.getIdSubmetaMedicao()));
		
	}
	
	@Test
	void excluirRascunhoSubmeta_MedicaoAgrupadora() {
		
		Long idMedicao = 2L;
		Long idSubmetaVrpl = 1L;
		Long idSubmetaMedicao = 1L;
		Long idContratoMedicao = 1L;
		
		ContratoBD contratoBD = new ContratoBD();
		contratoBD.setId(idContratoMedicao);
		contratoBD.setInAcompanhamentoEventos(true);

		SubmetaMedicaoBD submetaMedicaoBD = new SubmetaMedicaoBD();
		submetaMedicaoBD.setIdMedicao(idMedicao);
		submetaMedicaoBD.setIdSubmetaMedicao(idSubmetaMedicao);
		
		MedicaoBD medicao = new MedicaoBD();
		medicao.setId(idMedicao);
		medicao.setSituacao(SituacaoMedicaoEnum.EM);
		medicao.setIdMedicaoAgrupadora(1L);
		medicao.setBloqueada(false);
		medicao.setIdContratoMedicao(idContratoMedicao);
		
		when(submetaDAO.consultarSubmetaMedicao(medicao.getId(), idSubmetaVrpl)).thenReturn(submetaMedicaoBD);
		when(medicaoDAO.consultarMedicao(idMedicao)).thenReturn(medicao);
		when(contratoDAO.consultarContrato(idContratoMedicao)).thenReturn(contratoBD);
		when(securityContext.isUserInProfile(Profile.EMPRESA)).thenReturn(true);
		when(securityContext.hasAnyPermissionInProfile(EMPRESA)).thenReturn(true);		
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_MANTER_SUBMETA_MEDICAO_AGRUPADA,
				() -> submetaBC.excluirRascunhoSubmeta(medicao.getId(), submetaMedicaoBD.getIdSubmetaMedicao()));
		
	}
	
	@Test
	void excluirRascunhoSubmeta_MedicaoBloqueada() {
		
		Long idMedicao = 1L;
		Long idSubmetaVrpl = 1L;
		Long idSubmetaMedicao = 1L;
		
		SubmetaMedicaoBD submetaMedicaoBD = new SubmetaMedicaoBD();
		submetaMedicaoBD.setIdMedicao(idMedicao);
		submetaMedicaoBD.setIdSubmetaMedicao(idSubmetaMedicao);
		
		MedicaoBD medicao = new MedicaoBD();
		medicao.setId(idMedicao);
		medicao.setSituacao(SituacaoMedicaoEnum.EM);
		medicao.setIdMedicaoAgrupadora(null);
		medicao.setBloqueada(true);
		
		when(submetaDAO.consultarSubmetaMedicao(medicao.getId(), idSubmetaVrpl)).thenReturn(submetaMedicaoBD);
		when(medicaoDAO.consultarMedicao(idMedicao)).thenReturn(medicao);
		when(securityContext.isUserInProfile(Profile.EMPRESA)).thenReturn(true);
		when(securityContext.hasAnyPermissionInProfile(EMPRESA)).thenReturn(true);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_MEDICAO_BLOQUEADA,
				() -> submetaBC.excluirRascunhoSubmeta(medicao.getId(), submetaMedicaoBD.getIdSubmetaMedicao()));
		
	}
	
	@Test
	void excluirRascunhoSubmeta_UsuarioSemPermissao() {
		
		Long idMedicao = 1L;
		Long idSubmetaVrpl = 1L;
		Long idSubmetaMedicao = 1L;
		Long idContratoMedicao = 1L;
		
		ContratoBD contratoBD = new ContratoBD();
		contratoBD.setId(idContratoMedicao);
		contratoBD.setInAcompanhamentoEventos(true);
		
		SubmetaMedicaoBD submetaMedicaoBD = new SubmetaMedicaoBD();
		submetaMedicaoBD.setIdMedicao(idMedicao);
		submetaMedicaoBD.setIdSubmetaMedicao(idSubmetaMedicao);
		submetaMedicaoBD.setSituacaoEmpresa(SituacaoSubmetaEnum.RAS);
		
		MedicaoBD medicao = new MedicaoBD();
		medicao.setId(idMedicao);
		medicao.setSituacao(SituacaoMedicaoEnum.EM);
		medicao.setIdMedicaoAgrupadora(null);
		medicao.setBloqueada(false);
		medicao.setIdContratoMedicao(idContratoMedicao);
		
		when(submetaDAO.consultarSubmetaMedicao(medicao.getId(), idSubmetaVrpl)).thenReturn(submetaMedicaoBD);
		when(medicaoDAO.consultarMedicao(idMedicao)).thenReturn(medicao);
		when(contratoDAO.consultarContratoAssociadoMedicao(idMedicao)).thenReturn(contratoBD);
		when(securityContext.isUserInProfile(Profile.EMPRESA)).thenReturn(false);
		when(securityContext.hasAnyPermissionInProfile(EMPRESA)).thenReturn(false);		
	
		assertThrowsMedicaoRestException(MessageKey.ERRO_MEDICAO_NAO_PODE_SER_ALTERADA,
				() -> submetaBC.excluirRascunhoSubmeta(medicao.getId(), submetaMedicaoBD.getIdSubmetaMedicao()));
		
	}
	
	@Test
	void excluirRascunhoSubmeta_RascunhoEmpresa() {
		
		Long idMedicao = 1L;
		Long idSubmetaVrpl = 1L;
		Long idSubmetaMedicao = 1L;
		Long idContratoMedicao = 1L;
		
		ContratoBD contratoBD = new ContratoBD();
		contratoBD.setId(idContratoMedicao);
		contratoBD.setInAcompanhamentoEventos(true);
		
		SubmetaMedicaoBD submetaMedicaoBD = new SubmetaMedicaoBD();
		submetaMedicaoBD.setIdMedicao(idMedicao);
		submetaMedicaoBD.setIdSubmetaMedicao(idSubmetaMedicao);
		submetaMedicaoBD.setSituacaoEmpresa(SituacaoSubmetaEnum.RAS);
		
		MedicaoBD medicao = new MedicaoBD();
		medicao.setId(idMedicao);
		medicao.setSituacao(SituacaoMedicaoEnum.EM);
		medicao.setIdMedicaoAgrupadora(null);
		medicao.setBloqueada(false);
		medicao.setIdContratoMedicao(idContratoMedicao);
		
		when(submetaDAO.consultarSubmetaMedicao(medicao.getId(), idSubmetaVrpl)).thenReturn(submetaMedicaoBD);
		when(medicaoDAO.consultarMedicao(idMedicao)).thenReturn(medicao);
		when(contratoDAO.consultarContrato(idContratoMedicao)).thenReturn(contratoBD);
		
		when(securityContext.isUserInProfile(Profile.EMPRESA)).thenReturn(true);
		when(securityContext.hasAnyPermissionInProfile(EMPRESA)).thenReturn(true);
	
		submetaBC.excluirRascunhoSubmeta(medicao.getId(), submetaMedicaoBD.getIdSubmetaMedicao());
		
		verify(submetaDAO,  times(1)).excluirSubmetaMedicao(medicao.getId(), idSubmetaVrpl);
	}
	
	@Test
	void excluirRascunhoSubmeta_RascunhoEmpresaBM_QtdConvenenteNula() {
			
		Long idMedicao = 1L;
		Long idSubmetaVrpl = 1L;
		Long idSubmetaMedicao = 1L;
		Long idContratoMedicao = 1L;
		
		ContratoBD contratoBD = new ContratoBD();
		contratoBD.setId(idContratoMedicao);
		contratoBD.setInAcompanhamentoEventos(false);
		
		SubmetaMedicaoBD submetaMedicaoBD = new SubmetaMedicaoBD();
		submetaMedicaoBD.setIdMedicao(idMedicao);
		submetaMedicaoBD.setIdSubmetaMedicao(idSubmetaMedicao);
		submetaMedicaoBD.setSituacaoEmpresa(SituacaoSubmetaEnum.RAS);
		
		MedicaoBD medicao = new MedicaoBD();
		medicao.setId(idMedicao);
		medicao.setSituacao(SituacaoMedicaoEnum.EM);
		medicao.setIdMedicaoAgrupadora(null);
		medicao.setBloqueada(false);
		medicao.setIdContratoMedicao(idContratoMedicao);
		
		ItemMedicaoBMValorBD itemMedicaoBMValorBD = new ItemMedicaoBMValorBD();
		itemMedicaoBMValorBD.setIdItemMedicaoBMValor(1L);
		itemMedicaoBMValorBD.setQtConvenente(null);
		List<ItemMedicaoBMValorBD> listaItensMedicaoBMValor = new ArrayList<ItemMedicaoBMValorBD>();
		listaItensMedicaoBMValor.add(itemMedicaoBMValorBD);
		
		when(submetaDAO.consultarSubmetaMedicao(medicao.getId(), idSubmetaVrpl)).thenReturn(submetaMedicaoBD);
		when(medicaoDAO.consultarMedicao(idMedicao)).thenReturn(medicao);
		when(contratoDAO.consultarContrato(idContratoMedicao)).thenReturn(contratoBD);
		when(itemMedicaoDAO.listarItensMedicaoBMValor(idSubmetaVrpl, medicao.getId())).thenReturn(listaItensMedicaoBMValor);
		when(securityContext.isUserInProfile(Profile.EMPRESA)).thenReturn(true);
		when(securityContext.hasAnyPermissionInProfile(EMPRESA)).thenReturn(true);
	
		submetaBC.excluirRascunhoSubmeta(medicao.getId(), submetaMedicaoBD.getIdSubmetaMedicao());
		
		verify(submetaDAO,  times(1)).excluirSubmetaMedicao(medicao.getId(), idSubmetaVrpl);
		verify(itemMedicaoDAO,  times(1)).excluirItemMedicaoBMEmpresa(itemMedicaoBMValorBD.getIdItemMedicaoBMValor());
	}
	
	@Test
	void excluirRascunhoSubmeta_RascunhoConvenente() {
		
		Long idMedicao = 1L;
		Long idSubmetaVrpl = 1L;
		Long idSubmetaMedicao = 1L;
		Long idContratoMedicao = 1L;
		
		ContratoBD contratoBD = new ContratoBD();
		contratoBD.setId(idContratoMedicao);
		contratoBD.setInAcompanhamentoEventos(true);
		
		SubmetaMedicaoBD submetaMedicaoBD = new SubmetaMedicaoBD();
		submetaMedicaoBD.setIdMedicao(idMedicao);
		submetaMedicaoBD.setIdSubmetaMedicao(idSubmetaMedicao);
		submetaMedicaoBD.setSituacaoConvenente(SituacaoSubmetaEnum.RAS);
		
		MedicaoBD medicao = new MedicaoBD();
		medicao.setId(idMedicao);
		medicao.setSituacao(SituacaoMedicaoEnum.AT);
		medicao.setIdMedicaoAgrupadora(null);
		medicao.setBloqueada(false);
		medicao.setIdContratoMedicao(idContratoMedicao);
		
		when(submetaDAO.consultarSubmetaMedicao(medicao.getId(), idSubmetaVrpl)).thenReturn(submetaMedicaoBD);
		when(medicaoDAO.consultarMedicao(idMedicao)).thenReturn(medicao);
		when(contratoDAO.consultarContrato(idContratoMedicao)).thenReturn(contratoBD);
		when(securityContext.isUserInProfile(Profile.PROPONENTE_CONVENENTE)).thenReturn(true);
		when(securityContext.hasAnyPermissionInProfile(Profile.PROPONENTE_CONVENENTE)).thenReturn(true);
	
		submetaBC.excluirRascunhoSubmeta(medicao.getId(), submetaMedicaoBD.getIdSubmetaMedicao());
		
		verify(submetaDAO,  times(1)).excluirSubmetaMedicao(medicao.getId(), idSubmetaVrpl);
	}
	
	@Test
	void excluirRascunhoSubmeta_RascunhoConcedente() {
		
		Long idMedicao = 1L;
		Long idSubmetaVrpl = 1L;
		Long idSubmetaMedicao = 1L;
		Long idContratoMedicao = 1L;
		
		ContratoBD contratoBD = new ContratoBD();
		contratoBD.setId(idContratoMedicao);
		contratoBD.setInAcompanhamentoEventos(true);		
		
		SubmetaMedicaoBD submetaMedicaoBD = new SubmetaMedicaoBD();
		submetaMedicaoBD.setIdMedicao(idMedicao);
		submetaMedicaoBD.setIdSubmetaMedicao(idSubmetaMedicao);
		submetaMedicaoBD.setSituacaoConcedente(SituacaoSubmetaEnum.RAS);
		
		MedicaoBD medicao = new MedicaoBD();
		medicao.setId(idMedicao);
		medicao.setSituacao(SituacaoMedicaoEnum.AC);
		medicao.setIdMedicaoAgrupadora(null);
		medicao.setBloqueada(false);
		medicao.setIdContratoMedicao(idContratoMedicao);
		
		when(submetaDAO.consultarSubmetaMedicao(medicao.getId(), idSubmetaVrpl)).thenReturn(submetaMedicaoBD);
		when(medicaoDAO.consultarMedicao(idMedicao)).thenReturn(medicao);
		when(contratoDAO.consultarContrato(idContratoMedicao)).thenReturn(contratoBD);		
		when(securityContext.isUserInProfile(Profile.CONCEDENTE)).thenReturn(true);
		when(securityContext.hasAnyPermissionInProfile(Profile.CONCEDENTE)).thenReturn(true);
	
		submetaBC.excluirRascunhoSubmeta(medicao.getId(), submetaMedicaoBD.getIdSubmetaMedicao());
		
		verify(submetaDAO,  times(1)).excluirSubmetaMedicao(medicao.getId(), idSubmetaVrpl);
	}		
	
	
	private void assertThrowsMedicaoRestException(MessageKey expectedMessageKey, Executable executable) {

		MedicaoRestException exception = assertThrows(MedicaoRestException.class, executable);

		exception.getMessages().stream().map(Message::getKey).findFirst().ifPresentOrElse(
				actualMessageKey -> assertEquals(expectedMessageKey, actualMessageKey),
				() -> fail(format("A messageKey esperada era %s, mas nenhuma foi obtida", expectedMessageKey)));
	}
	
	
	
}
