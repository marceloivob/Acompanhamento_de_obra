package br.gov.planejamento.siconv.med.medicao.business;

import static br.gov.planejamento.siconv.med.test.builder.ObservacaoBDBuilder.newObservacaoBuilder;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.collection.IsEmptyCollection;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleCallback;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import br.gov.planejamento.siconv.med.infra.database.DAOFactory;
import br.gov.planejamento.siconv.med.infra.exception.MedicaoRestException;
import br.gov.planejamento.siconv.med.infra.message.Message;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.infra.security.UsuarioLogado;
import br.gov.planejamento.siconv.med.infra.security.domain.Profile;
import br.gov.planejamento.siconv.med.integration.UsuarioConsumer;
import br.gov.planejamento.siconv.med.integration.ceph.CephActions;
import br.gov.planejamento.siconv.med.integration.dto.UsuarioDTO;
import br.gov.planejamento.siconv.med.medicao.business.ObservacaoBC;
import br.gov.planejamento.siconv.med.medicao.dao.AnexoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.MedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.ObservacaoDAO;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.database.ObservacaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.dto.AnexoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ObservacaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.PerfilEnum;
import br.gov.planejamento.siconv.med.test.extension.MockUsuario;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
class ObservacaoBCTest {
	
	@Mock
	private Jdbi jdbi;
	
	@Getter(AccessLevel.PROTECTED)
	@Setter(AccessLevel.PROTECTED)
	@Mock
	private DAOFactory dao;

	@Mock
	private Handle handle;

	@Mock
	private SecurityContext securityContext;

	@Mock
    private CephActions cephActions;
	
	@Mock
	private UsuarioConsumer usuarioConsumer;
	
	@Mock
	private ObservacaoDAO obsDAO;
	
	@Mock
	private AnexoDAO anexoDAO;
	
	@Mock
	private MedicaoDAO medicaoDAO;
	
	@InjectMocks
	private ObservacaoBC obsBC;
	
	private Long idMedicao = 1L;
	private Long idMedicaoAgrupadora = 2L;
	private Long idObservacao = 1L;
	private Long idAnexo = 1L;
	private String nrCpfUsuario = "11111111111";
		
	@BeforeEach
	void setup() throws Exception {

		MockitoAnnotations.initMocks(this);

		dao = mock(DAOFactory.class);
		obsBC.setDao(dao);
		when(dao.get(ObservacaoDAO.class)).thenReturn(obsDAO);
		when(dao.get(AnexoDAO.class)).thenReturn(anexoDAO);
		when(dao.get(MedicaoDAO.class)).thenReturn(medicaoDAO);
		when(getDao().getJdbi()).thenReturn(jdbi);
		
		when(handle.attach(ObservacaoDAO.class)).thenReturn(obsDAO);
		when(handle.attach(AnexoDAO.class)).thenReturn(anexoDAO);
		when(jdbi.onDemand(AnexoDAO.class)).thenReturn(anexoDAO);
		when(jdbi.onDemand(ObservacaoDAO.class)).thenReturn(obsDAO);
		
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
	void testAtivarInativarAnexoMedicaoBloqueada() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(idMedicao);
		medicaoDTO.setBloqueada(true);
		
		when(medicaoDAO.obterMedicao(idMedicao)).thenReturn(medicaoDTO);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_MEDICAO_BLOQUEADA,
				() -> obsBC.ativarInativarAnexo("ativar", idAnexo, idObservacao, idMedicao));
		
	}
	
	@Test
	void testInativarAnexoMedicaoBloqueada() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(idMedicao);
		medicaoDTO.setBloqueada(true);
		
		when(medicaoDAO.obterMedicao(idMedicao)).thenReturn(medicaoDTO);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_MEDICAO_BLOQUEADA,
				() -> obsBC.ativarInativarAnexo("inativar", idAnexo, idObservacao, idMedicao));
		
	}
	
	@Test
	void testAtivarInativarAnexoAcessoPerfilNaoAutorizadoConcedenteMandataria() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(idMedicao);
		medicaoDTO.setBloqueada(false);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.EM);
		
		when(medicaoDAO.obterMedicao(idMedicao)).thenReturn(medicaoDTO);
		when(securityContext.isUserInProfile(Profile.CONCEDENTE)).thenReturn(true);
		when(securityContext.isUserInProfile(Profile.MANDATARIA)).thenReturn(false);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_ACESSO_PERFIL_NAO_AUTORIZADO,
				() -> obsBC.ativarInativarAnexo("ativar", idAnexo, idObservacao, idMedicao));
		
		when(securityContext.isUserInProfile(Profile.CONCEDENTE)).thenReturn(false);
		when(securityContext.isUserInProfile(Profile.MANDATARIA)).thenReturn(true);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_ACESSO_PERFIL_NAO_AUTORIZADO,
				() -> obsBC.ativarInativarAnexo("ativar", idAnexo, idObservacao, idMedicao));
		
		when(securityContext.isUserInProfile(Profile.CONCEDENTE)).thenReturn(true);
		when(securityContext.isUserInProfile(Profile.MANDATARIA)).thenReturn(true);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_ACESSO_PERFIL_NAO_AUTORIZADO,
				() -> obsBC.ativarInativarAnexo("ativar", idAnexo, idObservacao, idMedicao));
		
	}
	
	@Test
	void testAtivarInativarAnexoPerfilNaoPermitido() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(idMedicao);
		medicaoDTO.setBloqueada(false);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.AC);
		
		ObservacaoDTO obsDTO = new ObservacaoDTO();
		obsDTO.setInPerfilResponsavel(PerfilEnum.EMP);
		
		when(medicaoDAO.obterMedicao(idMedicao)).thenReturn(medicaoDTO);
		when(securityContext.isUserInProfile(Profile.CONCEDENTE)).thenReturn(true);
		when(obsDAO.recuperarObservacaoPorId(idObservacao, idMedicao)).thenReturn(obsDTO);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_ATIVAR_INATIVAR_ANEXO_PERFIL_RESPONSAVEL_NAO_PERMITIDO,
				() -> obsBC.ativarInativarAnexo("ativar", idAnexo, idObservacao, idMedicao));
		
	}
	
	@Test
	void testAtivarInativarAnexoObservacaoNaoEncontrada() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(idMedicao);
		medicaoDTO.setBloqueada(false);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.AC);
		
		ObservacaoDTO obsDTO = new ObservacaoDTO();
		obsDTO.setInPerfilResponsavel(PerfilEnum.CVE);
		
		List<Long> listaAnexoId = new ArrayList<Long>();
		
		when(medicaoDAO.obterMedicao(idMedicao)).thenReturn(medicaoDTO);
		when(securityContext.isUserInProfile(Profile.CONCEDENTE)).thenReturn(true);
		when(obsDAO.recuperarObservacaoPorId(idObservacao, idMedicao)).thenReturn(null);
		when(anexoDAO.buscarIdAnexoPorIdObservacao(idObservacao)).thenReturn(listaAnexoId);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_OBSERVACAO_NAO_ENCONTRADA,
				() -> obsBC.ativarInativarAnexo("ativar", idAnexo, idObservacao, idMedicao));
		
	}
	
	@Test
	void testAtivarInativarAnexoInexistente() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(idMedicao);
		medicaoDTO.setBloqueada(false);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.AC);
		
		ObservacaoDTO obsDTO = new ObservacaoDTO();
		obsDTO.setInPerfilResponsavel(PerfilEnum.CVE);
		
		List<Long> listaAnexoId = new ArrayList<Long>();
		
		when(medicaoDAO.obterMedicao(idMedicao)).thenReturn(medicaoDTO);
		when(securityContext.isUserInProfile(Profile.CONCEDENTE)).thenReturn(true);
		when(obsDAO.recuperarObservacaoPorId(idObservacao, idMedicao)).thenReturn(obsDTO);
		when(anexoDAO.buscarIdAnexoPorIdObservacao(idObservacao)).thenReturn(listaAnexoId);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_ANEXO_INEXISTENTE,
				() -> obsBC.ativarInativarAnexo("ativar", idAnexo, idObservacao, idMedicao));
		
	}
	
	@Test
	void testAtivarAnexoAtivoAnteriormente() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(idMedicao);
		medicaoDTO.setBloqueada(false);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.AC);
		
		ObservacaoDTO obsDTO = new ObservacaoDTO();
		obsDTO.setInPerfilResponsavel(PerfilEnum.CVE);
		
		AnexoDTO anexoDTO = new AnexoDTO();
		anexoDTO.setInInativo(false);
		
		List<Long> listaAnexoId = new ArrayList<Long>();
		listaAnexoId.add(idAnexo);
		
		when(medicaoDAO.obterMedicao(idMedicao)).thenReturn(medicaoDTO);
		when(securityContext.isUserInProfile(Profile.CONCEDENTE)).thenReturn(true);
		when(obsDAO.recuperarObservacaoPorId(idObservacao, idMedicao)).thenReturn(obsDTO);
		when(anexoDAO.buscarIdAnexoPorIdObservacao(idObservacao)).thenReturn(listaAnexoId);
		when(anexoDAO.buscarAnexoPorId(idAnexo)).thenReturn(anexoDTO);
				
		assertThrowsMedicaoRestException(MessageKey.ERRO_ANEXO_ATIVO_ANTERIORMENTE,
				() -> obsBC.ativarInativarAnexo("ativar", idAnexo, idObservacao, idMedicao));
		
	}
	
	@Test
	void testInativarAnexoInativoAnteriormente() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(idMedicao);
		medicaoDTO.setBloqueada(false);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.AC);
		
		ObservacaoDTO obsDTO = new ObservacaoDTO();
		obsDTO.setInPerfilResponsavel(PerfilEnum.CVE);
		
		AnexoDTO anexoDTO = new AnexoDTO();
		anexoDTO.setInInativo(true);
		
		List<Long> listaAnexoId = new ArrayList<Long>();
		listaAnexoId.add(idAnexo);
		
		when(medicaoDAO.obterMedicao(idMedicao)).thenReturn(medicaoDTO);
		when(securityContext.isUserInProfile(Profile.CONCEDENTE)).thenReturn(true);
		when(obsDAO.recuperarObservacaoPorId(idObservacao, idMedicao)).thenReturn(obsDTO);
		when(anexoDAO.buscarIdAnexoPorIdObservacao(idObservacao)).thenReturn(listaAnexoId);
		when(anexoDAO.buscarAnexoPorId(idAnexo)).thenReturn(anexoDTO);
				
		assertThrowsMedicaoRestException(MessageKey.ERRO_ANEXO_INATIVO_ANTERIORMENTE,
				() -> obsBC.ativarInativarAnexo("inativar", idAnexo, idObservacao, idMedicao));
		
	}
	
	@Test
	void testInativarAnexo() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(idMedicao);
		medicaoDTO.setBloqueada(false);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.AC);
		
		ObservacaoDTO obsDTO = new ObservacaoDTO();
		obsDTO.setInPerfilResponsavel(PerfilEnum.CVE);
		
		AnexoDTO anexoDTO = new AnexoDTO();
		anexoDTO.setInInativo(false);
		
		List<Long> listaAnexoId = new ArrayList<Long>();
		listaAnexoId.add(idAnexo);
		
		UsuarioLogado usuarioLogado = mock(UsuarioLogado.class);
		
		when(medicaoDAO.obterMedicao(idMedicao)).thenReturn(medicaoDTO);
		when(securityContext.isUserInProfile(Profile.CONCEDENTE)).thenReturn(true);
		when(obsDAO.recuperarObservacaoPorId(idObservacao, idMedicao)).thenReturn(obsDTO);
		when(anexoDAO.buscarIdAnexoPorIdObservacao(idObservacao)).thenReturn(listaAnexoId);
		when(anexoDAO.buscarAnexoPorId(idAnexo)).thenReturn(anexoDTO);
		
		when(securityContext.getUser()).thenReturn(usuarioLogado);
		when(usuarioLogado.getCpf()).thenReturn(nrCpfUsuario);
		
		obsBC.ativarInativarAnexo("inativar", idAnexo, idObservacao, idMedicao);
		
		verify(anexoDAO, times(1)).inativarAnexoPorId(idAnexo,nrCpfUsuario);
		
	}
	
	@Test
	void testAtivarAnexo() {

		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(idMedicao);
		medicaoDTO.setBloqueada(false);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.AC);
		
		ObservacaoDTO obsDTO = new ObservacaoDTO();
		obsDTO.setInPerfilResponsavel(PerfilEnum.CVE);
		
		AnexoDTO anexoDTO = new AnexoDTO();
		anexoDTO.setInInativo(true);
		
		List<Long> listaAnexoId = new ArrayList<Long>();
		listaAnexoId.add(idAnexo);
		
		when(medicaoDAO.obterMedicao(idMedicao)).thenReturn(medicaoDTO);
		when(securityContext.isUserInProfile(Profile.CONCEDENTE)).thenReturn(true);
		when(obsDAO.recuperarObservacaoPorId(idObservacao, idMedicao)).thenReturn(obsDTO);
		when(anexoDAO.buscarIdAnexoPorIdObservacao(idObservacao)).thenReturn(listaAnexoId);
		when(anexoDAO.buscarAnexoPorId(idAnexo)).thenReturn(anexoDTO);
		
		obsBC.ativarInativarAnexo("ativar", idAnexo, idObservacao, idMedicao);
		
		verify(anexoDAO, times(1)).ativarAnexoPorId(idAnexo);
		
	}
	
	@Test
	void testAtivarInativarAnexoAcessoPerfilNaoAutorizadoEmpresa() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(idMedicao);
		medicaoDTO.setBloqueada(false);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.AC);
		
		when(medicaoDAO.obterMedicao(idMedicao)).thenReturn(medicaoDTO);
		when(securityContext.isUserInProfile(Profile.EMPRESA)).thenReturn(true);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_ACESSO_PERFIL_NAO_AUTORIZADO,
				() -> obsBC.ativarInativarAnexo("ativar", idAnexo, idObservacao, idMedicao));
		
	}
	
	
	@Test
	void testAtivarInativarAnexoAcessoPerfilNaoAutorizadoConvenente() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(idMedicao);
		medicaoDTO.setBloqueada(false);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.AC);
		
		when(medicaoDAO.obterMedicao(idMedicao)).thenReturn(medicaoDTO);
		when(securityContext.isUserInProfile(Profile.PROPONENTE_CONVENENTE)).thenReturn(true);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_ACESSO_PERFIL_NAO_AUTORIZADO,
				() -> obsBC.ativarInativarAnexo("ativar", idAnexo, idObservacao, idMedicao));
		
	}
	
	@Test
	void testAtivarInativarAnexoAcessoPerfilNaoAutorizadoConcedente() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(idMedicao);
		medicaoDTO.setBloqueada(false);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.AT);
		
		when(medicaoDAO.obterMedicao(idMedicao)).thenReturn(medicaoDTO);
		when(securityContext.isUserInProfile(Profile.CONCEDENTE)).thenReturn(true);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_ACESSO_PERFIL_NAO_AUTORIZADO,
				() -> obsBC.ativarInativarAnexo("ativar", idAnexo, idObservacao, idMedicao));
		
	}
	
	@Test
	void testAtivarInativarAnexoAcessoPerfilNaoAutorizadoMandataria() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(idMedicao);
		medicaoDTO.setBloqueada(false);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.EM);
		
		when(medicaoDAO.obterMedicao(idMedicao)).thenReturn(medicaoDTO);
		when(securityContext.isUserInProfile(Profile.MANDATARIA)).thenReturn(true);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_ACESSO_PERFIL_NAO_AUTORIZADO,
				() -> obsBC.ativarInativarAnexo("ativar", idAnexo, idObservacao, idMedicao));
		
	}
	
	@Test
	void testBuscarObservacoesMedicaoEmElaboracao_empresa() {
		
		UsuarioDTO usuario = new UsuarioDTO();
		usuario.setCpf(nrCpfUsuario);
		usuario.setNome("empresa");
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(idMedicao);
		medicaoDTO.setBloqueada(false);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.EM);
		
		ObservacaoDTO obsDTO = new ObservacaoDTO();
		obsDTO.setNrCpfResponsavel(nrCpfUsuario);
		obsDTO.setInPerfilResponsavel(PerfilEnum.EMP);
		
		List<ObservacaoDTO> observacoes = new ArrayList<ObservacaoDTO>();
		observacoes.add(obsDTO);
		
		when(medicaoDAO.obterMedicao(idMedicao)).thenReturn(medicaoDTO);
		when(securityContext.hasAnyPermissionInProfile(Profile.EMPRESA)).thenReturn(true);
		when(obsDAO.recuperarObservacaoPorMedicao(idMedicao, false)).thenReturn(observacoes);
		when(usuarioConsumer.getNomeUsuario(nrCpfUsuario, PerfilEnum.EMP, true)).thenReturn(usuario.getNome());
		
		List<ObservacaoDTO> listaObs = obsBC.buscarObservacoesMedicao(idMedicao, false);
		
		assertThat(listaObs, not(IsEmptyCollection.empty()));
		assertThat(listaObs, hasItem(obsDTO));
	}
	
	@Test
	void testBuscarObservacoesMedicaoEmElaboracao_NaoEhEmpresa() {
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(idMedicao);
		medicaoDTO.setBloqueada(false);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.EM);
		
		ObservacaoDTO obsDTO = new ObservacaoDTO();
		obsDTO.setInPerfilResponsavel(PerfilEnum.EMP);
		
		List<ObservacaoDTO> observacoes = new ArrayList<ObservacaoDTO>();
		observacoes.add(obsDTO);
		
		when(medicaoDAO.obterMedicao(idMedicao)).thenReturn(medicaoDTO);
		when(securityContext.hasAnyPermissionInProfile(Profile.EMPRESA)).thenReturn(false);
		when(obsDAO.recuperarObservacaoPorMedicao(idMedicao, false)).thenReturn(observacoes);
		
		List<ObservacaoDTO> listaObs = obsBC.buscarObservacoesMedicao(idMedicao, false);
		
		assertThat(listaObs, IsEmptyCollection.empty());
	}
	
	@Test
	void testBuscarObservacoesMedicao_semAgrupadas_comAnexoInativo() {
		UsuarioDTO usuario = new UsuarioDTO();
		usuario.setCpf(nrCpfUsuario);
		usuario.setNome("empresa");
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(idMedicao);
		medicaoDTO.setBloqueada(false);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.AC);
		
		AnexoDTO anexoDTO1 = new AnexoDTO();
		anexoDTO1.setInInativo(true);
		anexoDTO1.setCoCeph("caminhoCoCeph");
		anexoDTO1.setNrCpfInativo("22222222222");
		
		AnexoDTO anexoDTO2 = new AnexoDTO();
		anexoDTO2.setInInativo(false);
		anexoDTO2.setCoCeph("caminhoCoCeph");
		
		List<AnexoDTO> anexos = new ArrayList<AnexoDTO>();
		anexos.add(anexoDTO1);
		anexos.add(anexoDTO2);
		
		ObservacaoDTO obsDTO = new ObservacaoDTO();
		obsDTO.setNrCpfResponsavel(nrCpfUsuario);
		obsDTO.setInPerfilResponsavel(PerfilEnum.EMP);
		obsDTO.setAnexos(anexos);
		
		List<ObservacaoDTO> observacoes = new ArrayList<ObservacaoDTO>();
		observacoes.add(obsDTO);
		
		when(medicaoDAO.obterMedicao(idMedicao)).thenReturn(medicaoDTO);
		when(securityContext.hasAnyPermissionInProfile(Profile.PROPONENTE_CONVENENTE)).thenReturn(true);
		when(obsDAO.recuperarObservacaoPorMedicao(idMedicao, true)).thenReturn(observacoes);
		when(usuarioConsumer.getNomeUsuario(nrCpfUsuario, PerfilEnum.EMP, true)).thenReturn(usuario.getNome());
		when(cephActions.getPresignedUrl(anexoDTO1.getCoCeph())).thenReturn("url");
		
		List<ObservacaoDTO> listaObs = obsBC.buscarObservacoesMedicao(idMedicao, false);
		
		assertThat(listaObs, not(IsEmptyCollection.empty()));
		assertThat(listaObs, hasItem(obsDTO));
	}
	
	@Test
	void testBuscarObservacoesMedicao_comAgrupadas() {
		UsuarioDTO usuario = new UsuarioDTO();
		usuario.setCpf(nrCpfUsuario);
		usuario.setNome("empresa");
		
		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(idMedicao);
		medicaoDTO.setBloqueada(false);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.AC);
		
		ObservacaoDTO obsDTO = new ObservacaoDTO();
		obsDTO.setNrCpfResponsavel(nrCpfUsuario);
		obsDTO.setInPerfilResponsavel(PerfilEnum.EMP);
		
		List<ObservacaoDTO> observacoes = new ArrayList<ObservacaoDTO>();
		observacoes.add(obsDTO);
		
		when(medicaoDAO.obterMedicao(idMedicao)).thenReturn(medicaoDTO);
		when(securityContext.hasAnyPermissionInProfile(Profile.CONCEDENTE)).thenReturn(true);
		when(obsDAO.recuperarObservacaoMedicoesAgrupadas(idMedicao, true)).thenReturn(observacoes);
		when(usuarioConsumer.getNomeUsuario(nrCpfUsuario, PerfilEnum.EMP, true)).thenReturn(usuario.getNome());
		
		List<ObservacaoDTO> listaObs = obsBC.buscarObservacoesMedicao(idMedicao, true);
		
		assertThat(listaObs, not(IsEmptyCollection.empty()));
		assertThat(listaObs, hasItem(obsDTO));
	}
	
	@Test
	void testInserirObsSemTxObservacao() {


		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(idMedicao);
		medicaoDTO.setBloqueada(false);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.EM);

		ObservacaoDTO obsDTO = new ObservacaoDTO();
		obsDTO.setNrCpfResponsavel(nrCpfUsuario);
		obsDTO.setInPerfilResponsavel(PerfilEnum.EMP);

		when(securityContext.isUserInProfile(Profile.MANDATARIA)).thenReturn(true);

		assertThrowsMedicaoRestException(MessageKey.ERRO_PARAMETRO_OBRIGATORIO_NAO_INFORMADO,
				() -> obsBC.inserirObservacao(medicaoDTO.getId(), obsDTO));

	}
	
	@Test
	void testInserirObsTxObservacaoVazio() {


		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(idMedicao);
		medicaoDTO.setBloqueada(false);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.EM);

		ObservacaoDTO obsDTO = new ObservacaoDTO();
		obsDTO.setNrCpfResponsavel(nrCpfUsuario);
		obsDTO.setInPerfilResponsavel(PerfilEnum.EMP);
		obsDTO.setTxObservacao("  ");

		when(securityContext.isUserInProfile(Profile.MANDATARIA)).thenReturn(true);

		assertThrowsMedicaoRestException(MessageKey.ERRO_PARAMETRO_OBRIGATORIO_NAO_INFORMADO,
				() -> obsBC.inserirObservacao(medicaoDTO.getId(), obsDTO));

	}
	
	@Test
	void testInserirObservacaoMedicaoBloqueada() {

		UsuarioDTO usuario = new UsuarioDTO();
		usuario.setCpf(nrCpfUsuario);
		usuario.setNome("empresa");

		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(idMedicao);
		medicaoDTO.setBloqueada(true);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.EM);

		ObservacaoDTO obsDTO = new ObservacaoDTO();
		obsDTO.setNrCpfResponsavel(nrCpfUsuario);
		obsDTO.setInPerfilResponsavel(PerfilEnum.EMP);
		obsDTO.setTxObservacao("observação");

		when(medicaoDAO.obterMedicao(idMedicao)).thenReturn(medicaoDTO);
		when(securityContext.isUserInProfile(Profile.EMPRESA)).thenReturn(true);

		assertThrowsMedicaoRestException(MessageKey.ERRO_MEDICAO_BLOQUEADA,
				() -> obsBC.inserirObservacao(medicaoDTO.getId(), obsDTO));

	}
	
	
	@Test
	void testInserirObservacaoMedicaoAgrupada() {

		UsuarioDTO usuario = new UsuarioDTO();
		usuario.setCpf(nrCpfUsuario);
		usuario.setNome("empresa");

		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(idMedicao);
		medicaoDTO.setBloqueada(true);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.EM);
		medicaoDTO.setIdMedicaoAgrupadora(idMedicaoAgrupadora);

		ObservacaoDTO obsDTO = new ObservacaoDTO();
		obsDTO.setNrCpfResponsavel(nrCpfUsuario);
		obsDTO.setInPerfilResponsavel(PerfilEnum.EMP);
		obsDTO.setTxObservacao("observação");

		when(medicaoDAO.obterMedicao(idMedicao)).thenReturn(medicaoDTO);
		when(securityContext.isUserInProfile(Profile.EMPRESA)).thenReturn(true);

		assertThrowsMedicaoRestException(MessageKey.ERRO_MANTER_OBSERVACAO_MEDICAO_AGRUPADA,
				() -> obsBC.inserirObservacao(medicaoDTO.getId(), obsDTO));

	}
	
	@Test
	void testInserirObsPerfilNaoAutorizadoEmpresa() {


		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(idMedicao);
		medicaoDTO.setBloqueada(false);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.EC);

		ObservacaoDTO obsDTO = new ObservacaoDTO();
		obsDTO.setNrCpfResponsavel(nrCpfUsuario);
		obsDTO.setInPerfilResponsavel(PerfilEnum.EMP);
		obsDTO.setTxObservacao("TESTE");

		when(medicaoDAO.obterMedicao(idMedicao)).thenReturn(medicaoDTO);
		when(securityContext.isUserInProfile(Profile.EMPRESA)).thenReturn(true);

		assertThrowsMedicaoRestException(MessageKey.ERRO_ACESSO_PERFIL_NAO_AUTORIZADO,
				() -> obsBC.inserirObservacao(medicaoDTO.getId(), obsDTO));

	}
	@Test
	void testInserirObsPerfilNaoAutorizadoConvenente() {


		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(idMedicao);
		medicaoDTO.setBloqueada(false);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.EM);

		ObservacaoDTO obsDTO = new ObservacaoDTO();
		obsDTO.setNrCpfResponsavel(nrCpfUsuario);
		obsDTO.setInPerfilResponsavel(PerfilEnum.CVE);
		obsDTO.setTxObservacao("TESTE");

		when(medicaoDAO.obterMedicao(idMedicao)).thenReturn(medicaoDTO);
		when(securityContext.isUserInProfile(Profile.PROPONENTE_CONVENENTE)).thenReturn(true);

		assertThrowsMedicaoRestException(MessageKey.ERRO_ACESSO_PERFIL_NAO_AUTORIZADO,
				() -> obsBC.inserirObservacao(medicaoDTO.getId(), obsDTO));

	}
	
	@Test
	void testInserirObsPerfilNaoAutorizadoConcedente() {


		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(idMedicao);
		medicaoDTO.setBloqueada(false);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.EM);

		ObservacaoDTO obsDTO = new ObservacaoDTO();
		obsDTO.setNrCpfResponsavel(nrCpfUsuario);
		obsDTO.setInPerfilResponsavel(PerfilEnum.CCE);
		obsDTO.setTxObservacao("TESTE");

		when(medicaoDAO.obterMedicao(idMedicao)).thenReturn(medicaoDTO);
		when(securityContext.isUserInProfile(Profile.CONCEDENTE)).thenReturn(true);

		assertThrowsMedicaoRestException(MessageKey.ERRO_ACESSO_PERFIL_NAO_AUTORIZADO,
				() -> obsBC.inserirObservacao(medicaoDTO.getId(), obsDTO));

	}
	
	@Test
	void testInserirObservacao() {


		MedicaoDTO medicaoDTO = new MedicaoDTO();
		medicaoDTO.setId(idMedicao);
		medicaoDTO.setBloqueada(false);
		medicaoDTO.setSituacao(SituacaoMedicaoEnum.EM);

	
		AnexoDTO anexoDTO1 = new AnexoDTO();
		anexoDTO1.setId(idAnexo);
		anexoDTO1.setNmArquivo("Arq1");
		anexoDTO1.setInInativo(true);
		anexoDTO1.setCoCeph("caminhoCoCeph");
		anexoDTO1.setNrCpfInativo("22222222222");
		

		List<AnexoDTO> anexos = new ArrayList<AnexoDTO>();
		anexos.add(anexoDTO1);
		
		ObservacaoDTO obsDTO = new ObservacaoDTO();
		obsDTO.setNrCpfResponsavel(nrCpfUsuario);
		obsDTO.setInPerfilResponsavel(PerfilEnum.EMP);
		obsDTO.setTxObservacao("TESTE");
		//obsDTO.setAnexos(anexos);
		
		ObservacaoBD obsBD = newObservacaoBuilder()
				.setTxObservacao(obsDTO.getTxObservacao())
				.setNrCpfResponsavel(nrCpfUsuario)
				.setInPerfilResponsavel(PerfilEnum.EMP)
				.setTxObservacao("TESTE")
				.create();


		when(medicaoDAO.obterMedicao(idMedicao)).thenReturn(medicaoDTO);
		when(securityContext.isUserInProfile(Profile.EMPRESA)).thenReturn(true);

		obsBC.inserirObservacao(medicaoDTO.getId(), obsDTO);

		verify(obsDAO, times(1)).inserirObservacao(idMedicao, obsBD);

	}
	
	
	private void assertThrowsMedicaoRestException(MessageKey expectedMessageKey, Executable executable) {

		MedicaoRestException exception = assertThrows(MedicaoRestException.class, executable);

		exception.getMessages().stream().map(Message::getKey).findFirst().ifPresentOrElse(
				actualMessageKey -> assertEquals(expectedMessageKey, actualMessageKey),
				() -> fail(format("A messageKey esperada era %s, mas nenhuma foi obtida", expectedMessageKey)));
	}
}
