package br.gov.planejamento.siconv.med.medicao.business.submeta;

import static br.gov.planejamento.siconv.med.infra.security.domain.Permission.ASSINAR_SUBMETA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.EMPRESA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.MANDATARIA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.FISCAL_ACOMPANHAMENTO;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.FISCAL_CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.GESTOR_CONVENIO_CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.GESTOR_FINANCEIRO_CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.OPERACIONAL_CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.TECNICO_TERCEIRO;
import static br.gov.planejamento.siconv.med.test.builder.ContratoMedicaoBuilder.newContratoMedicaoBuilder;
import static br.gov.planejamento.siconv.med.test.builder.MedicaoBuilder.newMedicaoBuilder;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
import br.gov.planejamento.siconv.med.infra.message.Message;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.infra.security.UsuarioLogado;
import br.gov.planejamento.siconv.med.infra.security.domain.Profile;
import br.gov.planejamento.siconv.med.infra.security.domain.Role;
import br.gov.planejamento.siconv.med.integration.siconv.SiconvGRPCConsumer;
import br.gov.planejamento.siconv.med.integration.vrpl.VrplGRPCConsumer;
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
import br.gov.planejamento.siconv.med.medicao.entity.database.ItemMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.MedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.SubmetaMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.dto.EventoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.FrenteObraVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.PerfilEnum;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.submetaservicosalvar.EventoSubmetaSalvarDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.submetaservicosalvar.FrenteObraSubmetaSalvarDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.submetaservicosalvar.SubmetaSalvarDTO;

class SubmetaBCTest {

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
	private SiconvGRPCConsumer siconvGRPCConsumer;
	
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
	private ArgumentCaptor<SubmetaMedicaoBD> subMedicaoCaptor;
	
	final String nrCpfUsuario = "11111111111";
	final String tipoExecucao = TipoResponsavelTecnicoEnum.EXE.getCodigo();
	final String tipoFiscalizacao = TipoResponsavelTecnicoEnum.FIS.getCodigo();
	final UsuarioLogado usuarioLogado = mock(UsuarioLogado.class);
	
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

		when(pipeline.add(Mockito.any(Class.class))).thenReturn(pipeline);
		when(pipeline.add(Mockito.any(AbstractSubmetaMedicaoStep.class))).thenReturn(pipeline);
		when(pipeline.when(Mockito.anyBoolean())).thenReturn(pipeline);
		when(pipeline.orElse()).thenReturn(pipeline);
		when(pipeline.anyway()).thenReturn(pipeline);

		when(submetaMedicaoBuilder.of(Mockito.any(), Mockito.any())).thenReturn(pipeline);
	}
	
	@Test
	void testAssinarSubmetaContratoInexistente() {
		
		SubmetaSalvarDTO submetaDTO = new SubmetaSalvarDTO();
		
		when(contratoDAO.consultarContratoPorContratoFK(1L)).thenReturn(null);
		
		assertThrowsMedicaoRestException(MessageKey.CONTRATO_INEXISTENTE,
				() -> submetaBC.assinarSubmeta(1L, 1L, 1L, submetaDTO, nrCpfUsuario));
		
	}
	
	@Test
	void testAssinarSubmetaMedicaoNaoEncontrada() {
		
		ContratoBD contrato = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(7L).create();
		
		SubmetaSalvarDTO submetaDTO = new SubmetaSalvarDTO();
		
		when(contratoDAO.consultarContratoPorContratoFK(contrato.contratoFk)).thenReturn(contrato);
		when(medicaoDAO.consultarMedicao(1L)).thenReturn(null);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_MEDICAO_NAO_ENCONTRADA,
				() -> submetaBC.assinarSubmeta(contrato.contratoFk, 1L, 1L, submetaDTO, nrCpfUsuario));
		
	}
	
	@Test
	void testAssinarSubmetaUsuarioNaoResponsavelEmpresaSocial() {
		
		ContratoBD contrato = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(7L).isSocial().create();
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(SituacaoMedicaoEnum.EM).create();
		
		SubmetaSalvarDTO submetaDTO = new SubmetaSalvarDTO();
		
		when(contratoDAO.consultarContratoPorContratoFK(contrato.contratoFk)).thenReturn(contrato);
		when(medicaoDAO.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(securityContext.isUserInProfile(Profile.EMPRESA)).thenReturn(true);
		when(securityContext.isUserInProfile(Profile.PROPONENTE_CONVENENTE)).thenReturn(false);
		when(dao.get(SubmetaDAO.class).isSubmetaContratoSocialAssinavelPeloCpf(contrato.id, 1L,
					nrCpfUsuario, tipoExecucao)).thenReturn(false);
				
		assertThrowsMedicaoRestException(MessageKey.ERRO_USUARIO_NAO_RESPONSAVEL_SUBMETA,
				() -> submetaBC.assinarSubmeta(contrato.contratoFk, medicao.id, 1L, submetaDTO, nrCpfUsuario));
		
	}
	
	@Test
	void testAssinarSubmetaUsuarioNaoResponsavelEmpresaObra() {
		
		ContratoBD contrato = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(7L).create();
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(SituacaoMedicaoEnum.EM).create();
		
		SubmetaSalvarDTO submetaDTO = new SubmetaSalvarDTO();
		
		when(contratoDAO.consultarContratoPorContratoFK(contrato.contratoFk)).thenReturn(contrato);
		when(medicaoDAO.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(securityContext.isUserInProfile(Profile.EMPRESA)).thenReturn(true);
		when(securityContext.isUserInProfile(Profile.PROPONENTE_CONVENENTE)).thenReturn(false);
		when(dao.get(SubmetaDAO.class).isSubmetaContratoArqEngAssinavelPeloCpf(contrato.id, 1L,
					nrCpfUsuario, tipoExecucao)).thenReturn(false);
				
		assertThrowsMedicaoRestException(MessageKey.ERRO_USUARIO_NAO_RESPONSAVEL_SUBMETA,
				() -> submetaBC.assinarSubmeta(contrato.contratoFk, medicao.id, 1L, submetaDTO, nrCpfUsuario));
		
	}
	
	@Test
	void testAssinarSubmetaUsuarioNaoResponsavelConvenenteSocial() {

		ContratoBD contrato = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(7L).isSocial().create();
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(SituacaoMedicaoEnum.AT).create();
		
		SubmetaSalvarDTO submetaDTO = new SubmetaSalvarDTO();
		
		when(contratoDAO.consultarContratoPorContratoFK(contrato.contratoFk)).thenReturn(contrato);
		when(medicaoDAO.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(securityContext.isUserInProfile(Profile.EMPRESA)).thenReturn(false);
		when(securityContext.isUserInProfile(Profile.PROPONENTE_CONVENENTE)).thenReturn(true);
		when(dao.get(SubmetaDAO.class).isSubmetaContratoSocialAssinavelPeloCpf(contrato.id, 1L,
					nrCpfUsuario, tipoFiscalizacao)).thenReturn(false);
				
		assertThrowsMedicaoRestException(MessageKey.ERRO_USUARIO_NAO_RESPONSAVEL_SUBMETA,
				() -> submetaBC.assinarSubmeta(contrato.contratoFk, medicao.id, 1L, submetaDTO, nrCpfUsuario));
		
	}
	
	@Test
	void testAssinarSubmetaUsuarioNaoResponsavelConvenenteObra() {
		
		ContratoBD contrato = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(7L).create();
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(SituacaoMedicaoEnum.AT).create();
		
		SubmetaSalvarDTO submetaDTO = new SubmetaSalvarDTO();
		
		when(contratoDAO.consultarContratoPorContratoFK(contrato.contratoFk)).thenReturn(contrato);
		when(medicaoDAO.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(securityContext.isUserInProfile(Profile.EMPRESA)).thenReturn(false);
		when(securityContext.isUserInProfile(Profile.PROPONENTE_CONVENENTE)).thenReturn(true);
		when(dao.get(SubmetaDAO.class).isSubmetaContratoArqEngAssinavelPeloCpf(contrato.id, 1L,
					nrCpfUsuario, tipoFiscalizacao)).thenReturn(false);
				
		assertThrowsMedicaoRestException(MessageKey.ERRO_USUARIO_NAO_RESPONSAVEL_SUBMETA,
				() -> submetaBC.assinarSubmeta(contrato.contratoFk, medicao.id, 1L, submetaDTO, nrCpfUsuario));
		
	}
	
	@Test
	void testAssinarSubmetaUsuarioNaoResponsavelConcedente() {
		
		ContratoBD contrato = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(7L).create();
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(SituacaoMedicaoEnum.EM).create();
		
		SubmetaSalvarDTO submetaDTO = new SubmetaSalvarDTO();
		
		when(contratoDAO.consultarContratoPorContratoFK(contrato.contratoFk)).thenReturn(contrato);
		when(medicaoDAO.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(securityContext.isUserInProfile(Profile.EMPRESA)).thenReturn(false);
		when(securityContext.isUserInProfile(Profile.PROPONENTE_CONVENENTE)).thenReturn(false);
		when(securityContext.hasRoleInProfile(CONCEDENTE,Arrays.asList(Role.FISCAL_CONCEDENTE))).thenReturn(true);
		when(securityContext.hasRoleInProfile(MANDATARIA,Arrays.asList(Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA))).thenReturn(false);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_USUARIO_NAO_RESPONSAVEL_SUBMETA,
				() -> submetaBC.assinarSubmeta(contrato.contratoFk, medicao.id, 1L, submetaDTO, nrCpfUsuario));
		
	}
	
	@Test
	void testAssinarSubmetaUsuarioNaoResponsavelMandataria() {
		
		ContratoBD contrato = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(7L).create();
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(SituacaoMedicaoEnum.EM).create();
		
		SubmetaSalvarDTO submetaDTO = new SubmetaSalvarDTO();
		
		when(contratoDAO.consultarContratoPorContratoFK(contrato.contratoFk)).thenReturn(contrato);
		when(medicaoDAO.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(securityContext.isUserInProfile(Profile.EMPRESA)).thenReturn(false);
		when(securityContext.isUserInProfile(Profile.PROPONENTE_CONVENENTE)).thenReturn(false);
		when(securityContext.hasRoleInProfile(CONCEDENTE,Arrays.asList(Role.FISCAL_CONCEDENTE))).thenReturn(false);
		when(securityContext.hasRoleInProfile(MANDATARIA,Arrays.asList(Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA))).thenReturn(true);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_USUARIO_NAO_RESPONSAVEL_SUBMETA,
				() -> submetaBC.assinarSubmeta(contrato.contratoFk, medicao.id, 1L, submetaDTO, nrCpfUsuario));
		
	}
	
	@Test
	void testAssinarSubmetaErroSubmetaInexistente() {

		ContratoBD contrato = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(7L).create();
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.setMedContrato(contrato.id)
				.comSituacao(SituacaoMedicaoEnum.AC).create();
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(1L);
		medicaoDTO.setIdContrato(contrato.id);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.AC);
		medicaoDTO.setIdMedicaoAgrupadora(null);
		medicaoDTO.setBloqueada(false);
		
		SubmetaVrplDTO submeta = new SubmetaVrplDTO();
		submeta.setId(1L);
		
		SubmetaMedicaoDTO submetaMedicao = new SubmetaMedicaoDTO();
		submetaMedicao.setId(1L);
		List<SubmetaMedicaoDTO> submetasMedicao = new ArrayList<SubmetaMedicaoDTO>();
		submetasMedicao.add(submetaMedicao);
		
		SubmetaSalvarDTO submetaDTO = new SubmetaSalvarDTO();
		
		when(contratoDAO.consultarContratoPorContratoFK(contrato.contratoFk)).thenReturn(contrato);
		when(contratoDAO.consultarContrato(contrato.id)).thenReturn(contrato);
		when(medicaoDAO.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(securityContext.isUserInProfile(Profile.EMPRESA)).thenReturn(false);
		when(securityContext.isUserInProfile(Profile.PROPONENTE_CONVENENTE)).thenReturn(false);
		when(securityContext.hasAnyRoleInProfile(CONCEDENTE)).thenReturn(false);

		when(securityContext.hasRoleInProfile(MANDATARIA,Arrays.asList(Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA))).thenReturn(true);
		
		when(securityContext.hasAnyPermissionInProfile(EMPRESA)).thenReturn(false);
		when(medicaoDAO.obterMedicao(medicao.id)).thenReturn(medicaoDTO);
		
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_SUBMETA_INEXISTENTE,
				() -> submetaBC.assinarSubmeta(contrato.contratoFk, medicao.id, 1L, submetaDTO, nrCpfUsuario));
		
	}
	
	@Test
	void testAssinarSubmetaMedicaoNaoPodeSerAlterada() {
		
		ContratoBD contrato = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(7L).create();
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.comSituacao(SituacaoMedicaoEnum.AC).create();
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(1L);
		medicaoDTO.setIdContrato(contrato.id);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.AC);
		medicaoDTO.setIdMedicaoAgrupadora(null);
		medicaoDTO.setBloqueada(false);
		
		SubmetaVrplDTO submeta = new SubmetaVrplDTO();
		submeta.setId(1L);
		submeta.setNrSubmetaAnalise("1.1");
		submeta.setDescricao("Submeta 1");
		submeta.setValor(new BigDecimal("10.00"));
		Optional<SubmetaVrplDTO> submetaVrpl = Optional.ofNullable(submeta);
		
		SubmetaMedicaoDTO submetaMedicao = new SubmetaMedicaoDTO();
		submetaMedicao.setId(1L);
		submetaMedicao.setPermiteMarcacaoConcedente(true);
		List<SubmetaMedicaoDTO> submetasMedicao = new ArrayList<SubmetaMedicaoDTO>();
		submetasMedicao.add(submetaMedicao);
		
		SubmetaSalvarDTO submetaDTO = new SubmetaSalvarDTO();
		
		// isSubmetaAssinavelPeloCpf
		when(contratoDAO.consultarContratoPorContratoFK(contrato.contratoFk)).thenReturn(contrato);
		when(medicaoDAO.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(securityContext.isUserInProfile(Profile.EMPRESA)).thenReturn(false);
		when(securityContext.isUserInProfile(Profile.PROPONENTE_CONVENENTE)).thenReturn(false);
		when(securityContext.isUserInProfile(Profile.CONCEDENTE)).thenReturn(false);
		when(securityContext.hasAnyRoleInProfile(CONCEDENTE)).thenReturn(false);
		when(securityContext.hasRoleInProfile(MANDATARIA,Arrays.asList(Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA))).thenReturn(true);
		
		// salvarSubmeta > validarManutencaoMedicao
		when(securityContext.hasAnyPermissionInProfile(EMPRESA)).thenReturn(false);
		
		// salvarSubmeta > recuperarSubmetaPorMedicao
		when(medicaoDAO.obterMedicao(medicao.id)).thenReturn(medicaoDTO);
		when(contratoDAO.consultarContrato(contrato.id)).thenReturn(contrato);
		when(vrplConsumer.getSubmetaPorId(1L)).thenReturn(submetaVrpl);
		when(submetaDAO.listarSubmetasMedicao(contrato.id, medicao.id)).thenReturn(submetasMedicao);
				
		assertThrowsMedicaoRestException(MessageKey.ERRO_MEDICAO_NAO_PODE_SER_ALTERADA,
				() -> submetaBC.assinarSubmeta(contrato.contratoFk, medicao.id, 1L, submetaDTO, nrCpfUsuario));
		
	}
	
	@Test
	void testAssinarSubmetaItemMedicaoInexistente() {
		
		ContratoBD contrato = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(7L).create();
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.setMedContrato(contrato.id)
				.comSituacao(SituacaoMedicaoEnum.AC)
				.setBloqueada(false).create();
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(medicao.id);
		medicaoDTO.setIdContrato(contrato.id);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.AC);
		medicaoDTO.setIdMedicaoAgrupadora(null);
		medicaoDTO.setBloqueada(false);
		
		SubmetaVrplDTO submeta = new SubmetaVrplDTO();
		submeta.setId(1L);
		submeta.setNrSubmetaAnalise("1.1");
		submeta.setDescricao("Submeta 1");
		submeta.setValor(new BigDecimal("10.00"));
		
		FrenteObraVrplDTO frenteObra = new FrenteObraVrplDTO();
		EventoVrplDTO evento = new EventoVrplDTO();
		List<EventoVrplDTO> eventos = new ArrayList<EventoVrplDTO>();
		List<FrenteObraVrplDTO> frentesObras = new ArrayList<FrenteObraVrplDTO>();

		evento.setPermiteMarcacao(true);
		eventos.add(evento);
		frenteObra.setEventos(eventos);
		frentesObras.add(frenteObra);
		submeta.setFrentesObras(frentesObras);
		Optional<SubmetaVrplDTO> submetaVrpl = Optional.ofNullable(submeta);
		
		SubmetaMedicaoDTO submetaMedicao = new SubmetaMedicaoDTO();
		List<SubmetaMedicaoDTO> submetasMedicao = new ArrayList<SubmetaMedicaoDTO>();
		submetaMedicao.setId(1L);
		submetaMedicao.setPermiteMarcacaoConcedente(true);
		submetasMedicao.add(submetaMedicao);
		
		SubmetaSalvarDTO submetaDTO = new SubmetaSalvarDTO();
		FrenteObraSubmetaSalvarDTO fo = new FrenteObraSubmetaSalvarDTO();
		EventoSubmetaSalvarDTO ev = new EventoSubmetaSalvarDTO();
		List<FrenteObraSubmetaSalvarDTO> foSalvar = new ArrayList<FrenteObraSubmetaSalvarDTO>();
		List<EventoSubmetaSalvarDTO> evSalvar = new ArrayList<EventoSubmetaSalvarDTO>();
		ev.setId(1L);
		ev.setIndRealizado(true);
		evSalvar.add(ev);
		fo.setId(1L);
		fo.setEventos(evSalvar);
		foSalvar.add(fo);
		submetaDTO.setVersao(1L);
		submetaDTO.setFrentesObra(foSalvar);
				
		// isSubmetaAssinavelPeloCpf
		when(contratoDAO.consultarContratoPorContratoFK(contrato.contratoFk)).thenReturn(contrato);
		when(medicaoDAO.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(securityContext.isUserInProfile(Profile.EMPRESA)).thenReturn(false);
		when(securityContext.isUserInProfile(Profile.PROPONENTE_CONVENENTE)).thenReturn(false);
		when(securityContext.hasRoleInProfile(CONCEDENTE,Arrays.asList(FISCAL_ACOMPANHAMENTO, TECNICO_TERCEIRO))).thenReturn(true);
		when(securityContext.hasRoleInProfile(MANDATARIA,Arrays.asList(Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA))).thenReturn(false);
		
		// salvarSubmeta > validarManutencaoMedicao
		when(securityContext.hasAnyPermissionInProfile(EMPRESA)).thenReturn(false);
		
		// salvarSubmeta > recuperarSubmetaPorMedicao
		when(medicaoDAO.obterMedicao(medicao.id)).thenReturn(medicaoDTO);
		when(contratoDAO.consultarContrato(contrato.id)).thenReturn(contrato);
		when(vrplConsumer.getSubmetaPorId(1L)).thenReturn(submetaVrpl);
		when(submetaDAO.listarSubmetasMedicao(contrato.id, medicao.id)).thenReturn(submetasMedicao);
		
		// salvarSubmeta
		when(securityContext.isUserInProfile(Profile.CONCEDENTE)).thenReturn(true);
		when(securityContext.hasAnyRoleInProfile(Profile.CONCEDENTE)).thenReturn(true);
		
		// salvarSubmeta > atualizarIndicadorPorEvento
		when(itemMedicaoDAO.consultarItemMedicao(1L, 1L, 1L)).thenReturn(null);
		
		assertThrowsMedicaoRestException(MessageKey.ITEM_MEDICAO_INEXISTENTE,
				() -> submetaBC.assinarSubmeta(contrato.contratoFk, medicao.id, 1L, submetaDTO, nrCpfUsuario));
		
	}
	
	@Test
	void testAssinarSubmetaItemMedicaoNaoPermiteMudanca() {
				
		ContratoBD contrato = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(7L).create();
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.setMedContrato(contrato.id)
				.comSituacao(SituacaoMedicaoEnum.AC)
				.setBloqueada(false).create();
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(medicao.id);
		medicaoDTO.setIdContrato(contrato.id);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.AC);
		medicaoDTO.setIdMedicaoAgrupadora(null);
		medicaoDTO.setBloqueada(false);
		
		ItemMedicaoBD itemMedicaoBD = new ItemMedicaoBD(1L, 1L, 1L, contrato.id, new BigDecimal("10.00"));
		
		SubmetaVrplDTO submeta = new SubmetaVrplDTO();
		submeta.setId(1L);
		submeta.setNrSubmetaAnalise("1.1");
		submeta.setDescricao("Submeta 1");
		submeta.setValor(new BigDecimal("10.00"));
		
		FrenteObraVrplDTO frenteObra = new FrenteObraVrplDTO();
		EventoVrplDTO evento = new EventoVrplDTO();
		List<EventoVrplDTO> eventos = new ArrayList<EventoVrplDTO>();
		List<FrenteObraVrplDTO> frentesObras = new ArrayList<FrenteObraVrplDTO>();

		evento.setPermiteMarcacao(true);
		eventos.add(evento);
		frenteObra.setEventos(eventos);
		frentesObras.add(frenteObra);
		submeta.setFrentesObras(frentesObras);
		Optional<SubmetaVrplDTO> submetaVrpl = Optional.ofNullable(submeta);
		
		SubmetaMedicaoDTO submetaMedicao = new SubmetaMedicaoDTO();
		List<SubmetaMedicaoDTO> submetasMedicao = new ArrayList<SubmetaMedicaoDTO>();
		submetaMedicao.setId(1L);
		submetaMedicao.setPermiteMarcacaoConcedente(true);
		submetasMedicao.add(submetaMedicao);
		
		SubmetaSalvarDTO submetaDTO = new SubmetaSalvarDTO();
		FrenteObraSubmetaSalvarDTO fo = new FrenteObraSubmetaSalvarDTO();
		EventoSubmetaSalvarDTO ev = new EventoSubmetaSalvarDTO();
		List<FrenteObraSubmetaSalvarDTO> foSalvar = new ArrayList<FrenteObraSubmetaSalvarDTO>();
		List<EventoSubmetaSalvarDTO> evSalvar = new ArrayList<EventoSubmetaSalvarDTO>();
		ev.setId(1L);
		ev.setIndRealizado(true);
		evSalvar.add(ev);
		fo.setId(1L);
		fo.setEventos(evSalvar);
		foSalvar.add(fo);
		submetaDTO.setVersao(1L);
		submetaDTO.setFrentesObra(foSalvar);
				
		// isSubmetaAssinavelPeloCpf
		when(contratoDAO.consultarContratoPorContratoFK(contrato.contratoFk)).thenReturn(contrato);
		when(medicaoDAO.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(securityContext.isUserInProfile(Profile.EMPRESA)).thenReturn(false);
		when(securityContext.isUserInProfile(Profile.PROPONENTE_CONVENENTE)).thenReturn(false);
		when(securityContext.hasRoleInProfile(CONCEDENTE,Arrays.asList(FISCAL_ACOMPANHAMENTO, TECNICO_TERCEIRO))).thenReturn(true);
		when(securityContext.hasRoleInProfile(MANDATARIA,Arrays.asList(Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA))).thenReturn(false);
		
		// salvarSubmeta > validarManutencaoMedicao
		when(securityContext.hasAnyPermissionInProfile(EMPRESA)).thenReturn(false);
		
		// salvarSubmeta > recuperarSubmetaPorMedicao
		when(medicaoDAO.obterMedicao(medicao.id)).thenReturn(medicaoDTO);
		when(contratoDAO.consultarContrato(contrato.id)).thenReturn(contrato);
		when(vrplConsumer.getSubmetaPorId(1L)).thenReturn(submetaVrpl);
		when(submetaDAO.listarSubmetasMedicao(contrato.id, medicao.id)).thenReturn(submetasMedicao);
		
		// salvarSubmeta
		when(securityContext.isUserInProfile(Profile.CONCEDENTE)).thenReturn(true);
		
		// salvarSubmeta > atualizarIndicadorPorEvento
		when(itemMedicaoDAO.consultarItemMedicao(1L, 1L, 1L)).thenReturn(itemMedicaoBD);

		// salvarSubmeta > salvarSubmetaMedicao > salvarSubmetaMedicaoConcedenteMandataria
		when(securityContext.hasAnyRoleInProfile(Profile.CONCEDENTE)).thenReturn(true);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_ITEM_MEDICAO_NAO_PERMITE_MUDANCA,
				() -> submetaBC.assinarSubmeta(contrato.contratoFk, medicao.id, 1L, submetaDTO, nrCpfUsuario));
	}
	
	@Test
	void testAssinarSubmetaNaoExisteParaMedicao() {
				
		ContratoBD contrato = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(7L).create();
		
		MedicaoBD medicao = new MedicaoBD();
		medicao.setId(1L);
		medicao.setSituacao(SituacaoMedicaoEnum.AC);
		medicao.setIdMedicaoAgrupadora(null);
		medicao.setBloqueada(false);
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(1L);
		medicaoDTO.setIdContrato(contrato.id);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.AC);
		medicaoDTO.setIdMedicaoAgrupadora(null);
		medicaoDTO.setBloqueada(false);
		
		ItemMedicaoBD itemMedicaoBD = new ItemMedicaoBD(1L, 1L, 1L, 1L, new BigDecimal("10.00"));
		
		SubmetaVrplDTO submeta = new SubmetaVrplDTO();
		submeta.setId(1L);
		submeta.setNrSubmetaAnalise("1.1");
		submeta.setDescricao("Submeta 1");
		submeta.setValor(new BigDecimal("10.00"));
		
		FrenteObraVrplDTO frenteObra = new FrenteObraVrplDTO();
		EventoVrplDTO evento = new EventoVrplDTO();
		List<EventoVrplDTO> eventos = new ArrayList<EventoVrplDTO>();
		List<FrenteObraVrplDTO> frentesObras = new ArrayList<FrenteObraVrplDTO>();

		evento.setPermiteMarcacao(true);
		evento.setId(1L);
		eventos.add(evento);
		frenteObra.setEventos(eventos);
		frenteObra.setId(1L);
		frentesObras.add(frenteObra);
		submeta.setFrentesObras(frentesObras);
		Optional<SubmetaVrplDTO> submetaVrpl = Optional.ofNullable(submeta);
		
		SubmetaMedicaoDTO submetaMedicao = new SubmetaMedicaoDTO();
		List<SubmetaMedicaoDTO> submetasMedicao = new ArrayList<SubmetaMedicaoDTO>();
		submetaMedicao.setId(1L);
		submetaMedicao.setPermiteMarcacaoConcedente(true);
		submetaMedicao.setFrentesObra(frentesObras);
		submetasMedicao.add(submetaMedicao);
		
		SubmetaSalvarDTO submetaDTO = new SubmetaSalvarDTO();
		FrenteObraSubmetaSalvarDTO fo = new FrenteObraSubmetaSalvarDTO();
		EventoSubmetaSalvarDTO ev = new EventoSubmetaSalvarDTO();
		List<FrenteObraSubmetaSalvarDTO> foSalvar = new ArrayList<FrenteObraSubmetaSalvarDTO>();
		List<EventoSubmetaSalvarDTO> evSalvar = new ArrayList<EventoSubmetaSalvarDTO>();
		ev.setId(1L);
		ev.setIndRealizado(false);
		evSalvar.add(ev);
		fo.setId(1L);
		fo.setEventos(evSalvar);
		foSalvar.add(fo);
		submetaDTO.setVersao(1L);
		submetaDTO.setFrentesObra(foSalvar);
		
		// isSubmetaAssinavelPeloCpf
		when(contratoDAO.consultarContratoPorContratoFK(contrato.contratoFk)).thenReturn(contrato);
		when(contratoDAO.consultarContrato(medicao.getIdContratoMedicao())).thenReturn(contrato);
		when(medicaoDAO.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(securityContext.isUserInProfile(Profile.EMPRESA)).thenReturn(false);
		when(securityContext.isUserInProfile(Profile.PROPONENTE_CONVENENTE)).thenReturn(false);
		when(securityContext.hasRoleInProfile(CONCEDENTE,Arrays.asList(FISCAL_ACOMPANHAMENTO, TECNICO_TERCEIRO))).thenReturn(true);
		when(securityContext.hasRoleInProfile(MANDATARIA,Arrays.asList(Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA))).thenReturn(false);
		when(perfilHelper.getPerfilUsuarioLogado()).thenReturn(PerfilEnum.FSA);
		
		// salvarSubmeta > validarManutencaoMedicao
		when(securityContext.hasAnyPermissionInProfile(EMPRESA)).thenReturn(false);
		
		// salvarSubmeta > recuperarSubmetaPorMedicao
		when(medicaoDAO.obterMedicao(medicao.id)).thenReturn(medicaoDTO);
		when(contratoDAO.consultarContrato(contrato.id)).thenReturn(contrato);
		when(vrplConsumer.getSubmetaPorId(1L)).thenReturn(submetaVrpl);
		when(submetaDAO.listarSubmetasMedicao(contrato.id, medicao.id)).thenReturn(submetasMedicao);
		
		// salvarSubmeta
		when(securityContext.isUserInProfile(Profile.CONCEDENTE)).thenReturn(true);
		
		// salvarSubmeta > atualizarIndicadorPorEvento
		when(itemMedicaoDAO.consultarItemMedicao(1L, 1L, 1L)).thenReturn(itemMedicaoBD);

		// salvarSubmeta > salvarSubmetaMedicao > salvarSubmetaMedicaoConcedenteMandataria
		when(securityContext.hasAnyRoleInProfile(Profile.CONCEDENTE)).thenReturn(true);
		when(securityContext.getUser()).thenReturn(usuarioLogado);
		when(usuarioLogado.getCpf()).thenReturn(nrCpfUsuario);
		
		submetaBC.assinarSubmeta(contrato.contratoFk, medicao.id, 1L, submetaDTO, nrCpfUsuario);
		
		verify(submetaDAO, times(1)).inserirSubmetaConcedenteMandataria(subMedicaoCaptor.capture());
		
		assertEquals(SituacaoSubmetaEnum.ASS, subMedicaoCaptor.getValue().getSituacaoConcedente());
		assertEquals(nrCpfUsuario, subMedicaoCaptor.getValue().getNrCpfResponsavelAssinaturaConcedente());
		
	}
	
	@Test
	void testAssinarSubmetaExisteParaMedicao() {
				
		ContratoBD contrato = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(7L).create();

		MedicaoBD medicao = new MedicaoBD();
		medicao.setId(1L);
		medicao.setSituacao(SituacaoMedicaoEnum.AC);
		medicao.setIdMedicaoAgrupadora(null);
		medicao.setBloqueada(false);
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(1L);
		medicaoDTO.setIdContrato(contrato.id);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.AC);
		medicaoDTO.setIdMedicaoAgrupadora(null);
		medicaoDTO.setBloqueada(false);
		
		ItemMedicaoBD itemMedicaoBD = new ItemMedicaoBD(1L, 1L, 1L, 1L, new BigDecimal("10.00"));
		
		SubmetaMedicaoBD submetaMedicaoBD = new SubmetaMedicaoBD();
		submetaMedicaoBD.setIdMedicao(medicao.id);
		submetaMedicaoBD.setIdSubmetaMedicao(1L);
		
		SubmetaVrplDTO submeta = new SubmetaVrplDTO();
		submeta.setId(1L);
		submeta.setNrSubmetaAnalise("1.1");
		submeta.setDescricao("Submeta 1");
		submeta.setValor(new BigDecimal("10.00"));
		
		FrenteObraVrplDTO frenteObra = new FrenteObraVrplDTO();
		EventoVrplDTO evento = new EventoVrplDTO();
		List<EventoVrplDTO> eventos = new ArrayList<EventoVrplDTO>();
		List<FrenteObraVrplDTO> frentesObras = new ArrayList<FrenteObraVrplDTO>();

		evento.setPermiteMarcacao(true);
		evento.setId(1L);
		eventos.add(evento);
		frenteObra.setEventos(eventos);
		frenteObra.setId(1L);
		frentesObras.add(frenteObra);
		submeta.setFrentesObras(frentesObras);
		Optional<SubmetaVrplDTO> submetaVrpl = Optional.ofNullable(submeta);
		
		SubmetaMedicaoDTO submetaMedicao = new SubmetaMedicaoDTO();
		List<SubmetaMedicaoDTO> submetasMedicao = new ArrayList<SubmetaMedicaoDTO>();
		submetaMedicao.setId(1L);
		submetaMedicao.setPermiteMarcacaoConcedente(true);
		submetaMedicao.setFrentesObra(frentesObras);
		submetasMedicao.add(submetaMedicao);
		
		SubmetaSalvarDTO submetaDTO = new SubmetaSalvarDTO();
		FrenteObraSubmetaSalvarDTO fo = new FrenteObraSubmetaSalvarDTO();
		EventoSubmetaSalvarDTO ev = new EventoSubmetaSalvarDTO();
		List<FrenteObraSubmetaSalvarDTO> foSalvar = new ArrayList<FrenteObraSubmetaSalvarDTO>();
		List<EventoSubmetaSalvarDTO> evSalvar = new ArrayList<EventoSubmetaSalvarDTO>();
		ev.setId(1L);
		ev.setIndRealizado(false);
		evSalvar.add(ev);
		fo.setId(1L);
		fo.setEventos(evSalvar);
		foSalvar.add(fo);
		submetaDTO.setVersao(1L);
		submetaDTO.setFrentesObra(foSalvar);
		
		// isSubmetaAssinavelPeloCpf
		when(contratoDAO.consultarContratoPorContratoFK(contrato.contratoFk)).thenReturn(contrato);
		when(contratoDAO.consultarContrato(medicao.getIdContratoMedicao())).thenReturn(contrato);
		when(medicaoDAO.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(securityContext.isUserInProfile(Profile.EMPRESA)).thenReturn(false);
		when(securityContext.isUserInProfile(Profile.PROPONENTE_CONVENENTE)).thenReturn(false);
		when(securityContext.hasRoleInProfile(CONCEDENTE,Arrays.asList(FISCAL_ACOMPANHAMENTO, TECNICO_TERCEIRO))).thenReturn(true);
		when(securityContext.hasRoleInProfile(MANDATARIA,Arrays.asList(Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA))).thenReturn(false);
		when(perfilHelper.getPerfilUsuarioLogado()).thenReturn(PerfilEnum.FSA);
		
		// salvarSubmeta > validarManutencaoMedicao
		when(securityContext.hasAnyPermissionInProfile(EMPRESA)).thenReturn(false);
		
		// salvarSubmeta > recuperarSubmetaPorMedicao
		when(medicaoDAO.obterMedicao(medicao.id)).thenReturn(medicaoDTO);
		when(contratoDAO.consultarContrato(contrato.id)).thenReturn(contrato);
		when(vrplConsumer.getSubmetaPorId(1L)).thenReturn(submetaVrpl);
		when(submetaDAO.listarSubmetasMedicao(contrato.id, medicao.id)).thenReturn(submetasMedicao);
		
		// salvarSubmeta
		when(securityContext.isUserInProfile(Profile.CONCEDENTE)).thenReturn(true);
		
		// salvarSubmeta > atualizarIndicadorPorEvento
		when(itemMedicaoDAO.consultarItemMedicao(1L, 1L, 1L)).thenReturn(itemMedicaoBD);

		// salvarSubmeta > salvarSubmetaMedicao > salvarSubmetaMedicaoConcedenteMandataria
		when(securityContext.hasAnyRoleInProfile(Profile.CONCEDENTE)).thenReturn(true);
		when(securityContext.getUser()).thenReturn(usuarioLogado);
		when(usuarioLogado.getCpf()).thenReturn(nrCpfUsuario);
		
		medicao.setId(1L);
		when(medicaoDAO.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(submetaDAO.consultarSubmetaMedicao(medicao.id, 1L)).thenReturn(submetaMedicaoBD);
		
		submetaBC.assinarSubmeta(contrato.contratoFk, medicao.id, 1L, submetaDTO, nrCpfUsuario);

		verify(submetaDAO, times(1)).atualizarAssinaturaConcedente(subMedicaoCaptor.capture());
		
		assertEquals(SituacaoSubmetaEnum.ASS, subMedicaoCaptor.getValue().getSituacaoConcedente());
		assertEquals(nrCpfUsuario, subMedicaoCaptor.getValue().getNrCpfResponsavelAssinaturaConcedente());
		
	}
	
	@Test
	void testAssinarSubmetaExisteParaMedicaoLimpaAssinatura() {
				
		ContratoBD contrato = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(7L).create();
		
		MedicaoBD medicao = newMedicaoBuilder().setId(1L)
				.setMedContrato(contrato.id)
				.comSituacao(SituacaoMedicaoEnum.AC)
				.setBloqueada(false).create();

		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(medicao.id);
		medicaoDTO.setIdContrato(contrato.id);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.AC);
		medicaoDTO.setIdMedicaoAgrupadora(null);
		medicaoDTO.setBloqueada(false);
		
		ItemMedicaoBD itemMedicaoBD = new ItemMedicaoBD(1L, 1L, 1L, 1L, new BigDecimal("10.00"));
		
		SubmetaMedicaoBD submetaMedicaoBD = new SubmetaMedicaoBD();
		submetaMedicaoBD.setIdMedicao(medicao.id);
		
		SubmetaVrplDTO submeta = new SubmetaVrplDTO();
		submeta.setId(1L);
		submeta.setNrSubmetaAnalise("1.1");
		submeta.setDescricao("Submeta 1");
		submeta.setValor(new BigDecimal("10.00"));
		
		FrenteObraVrplDTO frenteObra = new FrenteObraVrplDTO();
		EventoVrplDTO evento = new EventoVrplDTO();
		List<EventoVrplDTO> eventos = new ArrayList<EventoVrplDTO>();
		List<FrenteObraVrplDTO> frentesObras = new ArrayList<FrenteObraVrplDTO>();

		evento.setPermiteMarcacao(true);
		evento.setId(1L);
		eventos.add(evento);
		frenteObra.setEventos(eventos);
		frenteObra.setId(1L);
		frentesObras.add(frenteObra);
		submeta.setFrentesObras(frentesObras);
		Optional<SubmetaVrplDTO> submetaVrpl = Optional.ofNullable(submeta);
		
		SubmetaMedicaoDTO submetaMedicao = new SubmetaMedicaoDTO();
		List<SubmetaMedicaoDTO> submetasMedicao = new ArrayList<SubmetaMedicaoDTO>();
		submetaMedicao.setId(1L);
		submetaMedicao.setPermiteMarcacaoConcedente(true);
		submetaMedicao.setFrentesObra(frentesObras);
		submetasMedicao.add(submetaMedicao);
		
		SubmetaSalvarDTO submetaDTO = new SubmetaSalvarDTO();
		FrenteObraSubmetaSalvarDTO fo = new FrenteObraSubmetaSalvarDTO();
		EventoSubmetaSalvarDTO ev = new EventoSubmetaSalvarDTO();
		List<FrenteObraSubmetaSalvarDTO> foSalvar = new ArrayList<FrenteObraSubmetaSalvarDTO>();
		List<EventoSubmetaSalvarDTO> evSalvar = new ArrayList<EventoSubmetaSalvarDTO>();
		ev.setId(1L);
		ev.setIndRealizado(false);
		evSalvar.add(ev);
		fo.setId(1L);
		fo.setEventos(evSalvar);
		foSalvar.add(fo);
		submetaDTO.setVersao(1L);
		submetaDTO.setFrentesObra(foSalvar);
		
		// isSubmetaAssinavelPeloCpf
		when(contratoDAO.consultarContratoPorContratoFK(contrato.contratoFk)).thenReturn(contrato);
		when(medicaoDAO.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(securityContext.isUserInProfile(Profile.EMPRESA)).thenReturn(false);
		when(securityContext.isUserInProfile(Profile.PROPONENTE_CONVENENTE)).thenReturn(false);
		when(securityContext.hasRoleInProfile(CONCEDENTE,Arrays.asList(FISCAL_ACOMPANHAMENTO, TECNICO_TERCEIRO))).thenReturn(true);
		when(securityContext.hasRoleInProfile(MANDATARIA,Arrays.asList(Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA))).thenReturn(false);
		
		// salvarSubmeta > validarManutencaoMedicao
		when(securityContext.hasAnyPermissionInProfile(EMPRESA)).thenReturn(false);
		
		// salvarSubmeta > recuperarSubmetaPorMedicao
		when(medicaoDAO.obterMedicao(medicao.id)).thenReturn(medicaoDTO);
		when(contratoDAO.consultarContrato(contrato.id)).thenReturn(contrato);
		when(vrplConsumer.getSubmetaPorId(1L)).thenReturn(submetaVrpl);
		when(submetaDAO.listarSubmetasMedicao(contrato.id, medicao.id)).thenReturn(submetasMedicao);
		
		// salvarSubmeta
		when(securityContext.isUserInProfile(Profile.CONCEDENTE)).thenReturn(true);
		
		// salvarSubmeta > atualizarIndicadorPorEvento
		when(itemMedicaoDAO.consultarItemMedicao(1L, 1L, 1L)).thenReturn(itemMedicaoBD);

		// salvarSubmeta > salvarSubmetaMedicao > salvarSubmetaMedicaoConcedenteMandataria
		when(securityContext.hasAnyRoleInProfile(Profile.CONCEDENTE)).thenReturn(true);
		when(securityContext.getUser()).thenReturn(usuarioLogado);
		when(usuarioLogado.getCpf()).thenReturn(nrCpfUsuario);
		
		when(medicaoDAO.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(submetaDAO.consultarSubmetaMedicao(medicao.id, 1L)).thenReturn(submetaMedicaoBD);
		
		submetaBC.assinarSubmeta(contrato.contratoFk, medicao.id, submetaVrpl.get().getId(), submetaDTO, nrCpfUsuario);

		verify(submetaDAO, times(1)).atualizarAssinaturaConcedente(subMedicaoCaptor.capture());
		
		assertEquals(SituacaoSubmetaEnum.RAS, subMedicaoCaptor.getValue().getSituacaoConcedente());
		assertNull(subMedicaoCaptor.getValue().getNrCpfResponsavelAssinaturaConcedente());
		assertNull(subMedicaoCaptor.getValue().getDtAssinaturaConcedente());
		assertNull(subMedicaoCaptor.getValue().getInPerfilRespConcedente());
		
	}
	
	@Test
	void testAssinarSubmeta_AtualizaIndicadorFrenteObraEventoEmpresa_SemAlteracaoEventoTela() {
				
		ContratoBD contrato = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(7L).create();
		contrato.setInSocial(false);

		MedicaoBD medicao = new MedicaoBD();
		medicao.setId(1L);
		medicao.setSituacao(SituacaoMedicaoEnum.EM);
		medicao.setIdMedicaoAgrupadora(null);
		medicao.setBloqueada(false);
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(1L);
		medicaoDTO.setIdContrato(contrato.id);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.EM);
		medicaoDTO.setIdMedicaoAgrupadora(null);
		medicaoDTO.setBloqueada(false);
		
		ItemMedicaoBD itemMedicaoBD = new ItemMedicaoBD(1L, 1L, 1L, 1L, new BigDecimal("10.00"));
		
		SubmetaMedicaoBD submetaMedicaoBD = new SubmetaMedicaoBD();
		submetaMedicaoBD.setIdMedicao(medicao.id);
		submetaMedicaoBD.setIdSubmetaMedicao(1L);
		
		SubmetaVrplDTO submeta = new SubmetaVrplDTO();
		submeta.setId(1L);
		submeta.setNrSubmetaAnalise("1.1");
		submeta.setDescricao("Submeta 1");
		submeta.setValor(new BigDecimal("10.00"));
		
		FrenteObraVrplDTO frenteObra = new FrenteObraVrplDTO();
		EventoVrplDTO evento = new EventoVrplDTO();
		List<EventoVrplDTO> eventos = new ArrayList<EventoVrplDTO>();
		List<FrenteObraVrplDTO> frentesObras = new ArrayList<FrenteObraVrplDTO>();

		evento.setPermiteMarcacao(true);
		evento.setId(1L);
		eventos.add(evento);
		frenteObra.setEventos(eventos);
		frenteObra.setId(1L);
		frentesObras.add(frenteObra);
		submeta.setFrentesObras(frentesObras);
		Optional<SubmetaVrplDTO> submetaVrpl = Optional.ofNullable(submeta);
		
		SubmetaMedicaoDTO submetaMedicao = new SubmetaMedicaoDTO();
		List<SubmetaMedicaoDTO> submetasMedicao = new ArrayList<SubmetaMedicaoDTO>();
		submetaMedicao.setId(1L);
		submetaMedicao.setPermiteMarcacaoEmpresa(true);
		submetaMedicao.setFrentesObra(frentesObras);
		submetasMedicao.add(submetaMedicao);
		
		SubmetaSalvarDTO submetaDTO = new SubmetaSalvarDTO();
		FrenteObraSubmetaSalvarDTO fo = new FrenteObraSubmetaSalvarDTO();
		EventoSubmetaSalvarDTO ev = new EventoSubmetaSalvarDTO();
		List<FrenteObraSubmetaSalvarDTO> foSalvar = new ArrayList<FrenteObraSubmetaSalvarDTO>();
		List<EventoSubmetaSalvarDTO> evSalvar = new ArrayList<EventoSubmetaSalvarDTO>();
		ev.setId(1L);
		ev.setIndRealizado(false);
		evSalvar.add(ev);
		fo.setId(1L);
		fo.setEventos(evSalvar);
		foSalvar.add(fo);
		submetaDTO.setVersao(1L);
		submetaDTO.setFrentesObra(foSalvar);
		
		// isSubmetaAssinavelPeloCpf
		when(contratoDAO.consultarContratoPorContratoFK(contrato.contratoFk)).thenReturn(contrato);
		when(contratoDAO.consultarContrato(medicao.getIdContratoMedicao())).thenReturn(contrato);
		when(medicaoDAO.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(securityContext.isUserInProfile(Profile.EMPRESA)).thenReturn(true);
		when(securityContext.hasPermissionInProfile(EMPRESA, asList(ASSINAR_SUBMETA))).thenReturn(true);
		when(securityContext.isUserInProfile(Profile.PROPONENTE_CONVENENTE)).thenReturn(false);
		when(securityContext.hasRoleInProfile(CONCEDENTE,Arrays.asList(FISCAL_CONCEDENTE, OPERACIONAL_CONCEDENTE, GESTOR_CONVENIO_CONCEDENTE, GESTOR_FINANCEIRO_CONCEDENTE, FISCAL_ACOMPANHAMENTO, TECNICO_TERCEIRO))).thenReturn(false);
		when(securityContext.hasRoleInProfile(MANDATARIA,Arrays.asList(Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA))).thenReturn(false);
		
		// salvarSubmeta > validarManutencaoMedicao
		when(securityContext.hasAnyPermissionInProfile(EMPRESA)).thenReturn(true);
		
		// salvarSubmeta > recuperarSubmetaPorMedicao
		when(medicaoDAO.obterMedicao(medicao.id)).thenReturn(medicaoDTO);
		when(contratoDAO.consultarContrato(contrato.id)).thenReturn(contrato);
		when(vrplConsumer.getSubmetaPorId(1L)).thenReturn(submetaVrpl);
		when(submetaDAO.listarSubmetasMedicao(contrato.id, medicao.id)).thenReturn(submetasMedicao);
		
		// salvarSubmeta
		when(securityContext.isUserInProfile(Profile.CONCEDENTE)).thenReturn(false);
		when(submetaDAO.isSubmetaContratoArqEngAssinavelPeloCpf(7L, 1L, "11111111111", "EXE")).thenReturn(true);
		
		// salvarSubmeta > atualizarIndicadorPorEvento
		when(itemMedicaoDAO.consultarItemMedicao(1L, 1L, 1L)).thenReturn(itemMedicaoBD);

		// salvarSubmeta > salvarSubmetaMedicao > salvarSubmetaMedicaoConcedenteMandataria
		when(securityContext.hasAnyRoleInProfile(Profile.CONCEDENTE)).thenReturn(false);
		when(securityContext.getUser()).thenReturn(usuarioLogado);
		when(usuarioLogado.getCpf()).thenReturn(nrCpfUsuario);
		
		medicao.setId(1L);
		when(medicaoDAO.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(submetaDAO.consultarSubmetaMedicao(medicao.id, 1L)).thenReturn(submetaMedicaoBD);
		
		submetaBC.assinarSubmeta(contrato.contratoFk, medicao.id, 1L, submetaDTO, nrCpfUsuario);

		verify(submetaDAO, times(1)).atualizarAssinaturaEmpresa(subMedicaoCaptor.capture());
		
		assertEquals(SituacaoSubmetaEnum.ASS, subMedicaoCaptor.getValue().getSituacaoEmpresa());
		assertEquals(nrCpfUsuario, subMedicaoCaptor.getValue().getNrCpfResponsavelAssinaturaEmpresa());
		
	}
	
	@Test
	void testAssinarSubmeta_AtualizaIndicadorFrenteObraEventoEmpresa_ComAlteracaoEventoTela() {
				
		ContratoBD contrato = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(7L).create();
		contrato.setInSocial(false);

		MedicaoBD medicao = new MedicaoBD();
		medicao.setId(1L);
		medicao.setSituacao(SituacaoMedicaoEnum.EM);
		medicao.setIdMedicaoAgrupadora(null);
		medicao.setBloqueada(false);
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(1L);
		medicaoDTO.setIdContrato(contrato.id);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.EM);
		medicaoDTO.setIdMedicaoAgrupadora(null);
		medicaoDTO.setBloqueada(false);
		
		ItemMedicaoBD itemMedicaoBD = new ItemMedicaoBD(null, 1L, 1L, 1L, new BigDecimal("10.00"));
		
		SubmetaMedicaoBD submetaMedicaoBD = new SubmetaMedicaoBD();
		submetaMedicaoBD.setIdMedicao(medicao.id);
		submetaMedicaoBD.setIdSubmetaMedicao(1L);
		
		SubmetaVrplDTO submeta = new SubmetaVrplDTO();
		submeta.setId(1L);
		submeta.setNrSubmetaAnalise("1.1");
		submeta.setDescricao("Submeta 1");
		submeta.setValor(new BigDecimal("10.00"));
		
		FrenteObraVrplDTO frenteObra = new FrenteObraVrplDTO();
		EventoVrplDTO evento = new EventoVrplDTO();
		List<EventoVrplDTO> eventos = new ArrayList<EventoVrplDTO>();
		List<FrenteObraVrplDTO> frentesObras = new ArrayList<FrenteObraVrplDTO>();

		evento.setPermiteMarcacao(true);
		evento.setId(1L);
		eventos.add(evento);
		frenteObra.setEventos(eventos);
		frenteObra.setId(1L);
		frentesObras.add(frenteObra);
		submeta.setFrentesObras(frentesObras);
		Optional<SubmetaVrplDTO> submetaVrpl = Optional.ofNullable(submeta);
		
		SubmetaMedicaoDTO submetaMedicao = new SubmetaMedicaoDTO();
		List<SubmetaMedicaoDTO> submetasMedicao = new ArrayList<SubmetaMedicaoDTO>();
		submetaMedicao.setId(1L);
		submetaMedicao.setPermiteMarcacaoEmpresa(true);
		submetaMedicao.setFrentesObra(frentesObras);
		submetasMedicao.add(submetaMedicao);
		
		SubmetaSalvarDTO submetaDTO = new SubmetaSalvarDTO();
		FrenteObraSubmetaSalvarDTO fo = new FrenteObraSubmetaSalvarDTO();
		EventoSubmetaSalvarDTO ev = new EventoSubmetaSalvarDTO();
		List<FrenteObraSubmetaSalvarDTO> foSalvar = new ArrayList<FrenteObraSubmetaSalvarDTO>();
		List<EventoSubmetaSalvarDTO> evSalvar = new ArrayList<EventoSubmetaSalvarDTO>();
		ev.setId(1L);
		ev.setIndRealizado(true);
		evSalvar.add(ev);
		fo.setId(1L);
		fo.setEventos(evSalvar);
		foSalvar.add(fo);
		submetaDTO.setVersao(1L);
		submetaDTO.setFrentesObra(foSalvar);
		
		// isSubmetaAssinavelPeloCpf
		when(contratoDAO.consultarContratoPorContratoFK(contrato.contratoFk)).thenReturn(contrato);
		when(contratoDAO.consultarContrato(medicao.getIdContratoMedicao())).thenReturn(contrato);
		when(medicaoDAO.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(securityContext.isUserInProfile(Profile.EMPRESA)).thenReturn(true);
		when(securityContext.hasPermissionInProfile(EMPRESA, asList(ASSINAR_SUBMETA))).thenReturn(true);
		when(securityContext.isUserInProfile(Profile.PROPONENTE_CONVENENTE)).thenReturn(false);
		when(securityContext.hasRoleInProfile(CONCEDENTE,Arrays.asList(Role.FISCAL_CONCEDENTE))).thenReturn(false);
		when(securityContext.hasRoleInProfile(MANDATARIA,Arrays.asList(Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA))).thenReturn(false);
		
		// salvarSubmeta > validarManutencaoMedicao
		when(securityContext.hasAnyPermissionInProfile(EMPRESA)).thenReturn(true);
		
		// salvarSubmeta > recuperarSubmetaPorMedicao
		when(medicaoDAO.obterMedicao(medicao.id)).thenReturn(medicaoDTO);
		when(contratoDAO.consultarContrato(contrato.id)).thenReturn(contrato);
		when(vrplConsumer.getSubmetaPorId(1L)).thenReturn(submetaVrpl);
		when(submetaDAO.listarSubmetasMedicao(contrato.id, medicao.id)).thenReturn(submetasMedicao);
		
		// salvarSubmeta
		when(securityContext.isUserInProfile(Profile.CONCEDENTE)).thenReturn(false);
		when(submetaDAO.isSubmetaContratoArqEngAssinavelPeloCpf(7L, 1L, "11111111111", "EXE")).thenReturn(true);
		
		// salvarSubmeta > atualizarIndicadorPorEvento
		when(itemMedicaoDAO.consultarItemMedicao(1L, 1L, 1L)).thenReturn(itemMedicaoBD);

		// salvarSubmeta > salvarSubmetaMedicao > salvarSubmetaMedicaoConcedenteMandataria
		when(securityContext.hasAnyRoleInProfile(Profile.CONCEDENTE)).thenReturn(false);
		when(securityContext.getUser()).thenReturn(usuarioLogado);
		when(usuarioLogado.getCpf()).thenReturn(nrCpfUsuario);
		
		medicao.setId(1L);
		when(medicaoDAO.consultarMedicao(medicao.id)).thenReturn(medicao);
		when(submetaDAO.consultarSubmetaMedicao(medicao.id, 1L)).thenReturn(submetaMedicaoBD);
		
		submetaBC.assinarSubmeta(contrato.contratoFk, medicao.id, 1L, submetaDTO, nrCpfUsuario);

		verify(submetaDAO, times(1)).atualizarAssinaturaEmpresa(subMedicaoCaptor.capture());
		
		assertEquals(SituacaoSubmetaEnum.ASS, subMedicaoCaptor.getValue().getSituacaoEmpresa());
		assertEquals(nrCpfUsuario, subMedicaoCaptor.getValue().getNrCpfResponsavelAssinaturaEmpresa());
		
	}
	
	private void assertThrowsMedicaoRestException(MessageKey expectedMessageKey, Executable executable) {

		MedicaoRestException exception = assertThrows(MedicaoRestException.class, executable);

		exception.getMessages().stream().map(Message::getKey).findFirst().ifPresentOrElse(
				actualMessageKey -> assertEquals(expectedMessageKey, actualMessageKey),
				() -> fail(format("A messageKey esperada era %s, mas nenhuma foi obtida", expectedMessageKey)));
	}
}
