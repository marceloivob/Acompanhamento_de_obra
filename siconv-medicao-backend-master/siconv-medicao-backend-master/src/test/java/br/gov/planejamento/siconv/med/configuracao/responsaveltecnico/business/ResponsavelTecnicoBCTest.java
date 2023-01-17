package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.business;

import static br.gov.planejamento.siconv.med.test.builder.ContratoMedicaoBuilder.newContratoMedicaoBuilder;
import static br.gov.planejamento.siconv.med.test.builder.ContratoSiconvBuilder.newContratoDTOBuilder;
import static br.gov.planejamento.siconv.med.test.builder.UsuarioDTOBuilder.newUsuarioDTOBuilder;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Optional;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleCallback;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.dao.DocumentoComplementarDAO;
import br.gov.planejamento.siconv.med.configuracao.paralisacao.dao.ParalisacaoDAO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao.ContratoResponsavelTecnicoDAO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao.RegistroProfissionalDAO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao.ResponsavelTecnicoDAO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.database.ContratoResponsavelTecnicoBD;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.database.RegistroProfissionalBD;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.database.ResponsavelTecnicoBD;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.AtividadeRegistroProfissionalEnum;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.ContratoResponsavelTecnicoDTO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.RegistroProfissionalDTO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.ResponsavelTecnicoDTO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.TipoResponsavelTecnicoEnum;
import br.gov.planejamento.siconv.med.contrato.business.ContratosBC;
import br.gov.planejamento.siconv.med.contrato.business.ItemMedicaoBC;
import br.gov.planejamento.siconv.med.contrato.dao.ContratoDAO;
import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.contrato.entity.dto.ContratoSiconvDTO;
import br.gov.planejamento.siconv.med.infra.database.DAOFactory;
import br.gov.planejamento.siconv.med.infra.exception.MedicaoRestException;
import br.gov.planejamento.siconv.med.infra.message.Message;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.infra.security.UsuarioLogado;
import br.gov.planejamento.siconv.med.integration.UsuarioConsumer;
import br.gov.planejamento.siconv.med.integration.contratos.ContratosGrpcConsumer;
import br.gov.planejamento.siconv.med.integration.dto.UsuarioDTO;
import br.gov.planejamento.siconv.med.test.builder.ContratoResponsavelTecnicoBDBuilder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

class ResponsavelTecnicoBCTest {
	
	final String CPF = "11111111111";
	final String CNPJ = "2222222222222222";
	final String NOME = "usuario";
	final UsuarioLogado usuarioLogado = mock(UsuarioLogado.class);
	
	@Mock
	private Jdbi jdbi;
	
	@Getter(AccessLevel.PROTECTED)
	@Setter(AccessLevel.PROTECTED)
	@Mock
	private DAOFactory dao;

	@Mock
	private Handle handle;
	
	@Mock
	private UsuarioConsumer usuarioConsumer;

	@Mock
	private ContratosGrpcConsumer contratoConsumer;
	
	@Mock
	private RegistroProfissionalDTO regProfDTO;

	@Mock
	private ContratoResponsavelTecnicoDTO contratoRegistroDTO;

	@Mock
	private ResponsavelTecnicoDAO respTecDAO;
	
	@Mock
	private RegistroProfissionalDAO regProfDAO;
	
	@Mock
	private ContratoResponsavelTecnicoDAO contratoRespTecDAO;
	
	@Mock
	private ContratoDAO contratoDAO;
	
	@Mock
	private ItemMedicaoBC itemMedicaoBC;
	
	@Mock
	private ContratosBC contratoBC;
	
	@Mock
	private DocumentoComplementarDAO docComplementarDAO;
	
	@Mock
	private ParalisacaoDAO paralisacaoDAO;
	
	@InjectMocks
	private ResponsavelTecnicoBC respTecBC;
	
	@Captor
	private ArgumentCaptor<ResponsavelTecnicoBD> responsavelCaptor;
	
	@Captor
	private ArgumentCaptor<RegistroProfissionalBD> registroCaptor;
	
	@BeforeEach
	void setup() throws Exception {
		

		MockitoAnnotations.initMocks(this);
		
		when(handle.attach(ResponsavelTecnicoDAO.class)).thenReturn(respTecDAO);
		when(handle.attach(RegistroProfissionalDAO.class)).thenReturn(regProfDAO);
		when(handle.attach(ContratoResponsavelTecnicoDAO.class)).thenReturn(contratoRespTecDAO);
		when(jdbi.onDemand(ResponsavelTecnicoDAO.class)).thenReturn(respTecDAO);
		when(jdbi.onDemand(RegistroProfissionalDAO.class)).thenReturn(regProfDAO);
		when(dao.get(ResponsavelTecnicoDAO.class)).thenReturn(respTecDAO);
		when(dao.get(RegistroProfissionalDAO.class)).thenReturn(regProfDAO);
		when(dao.get(ContratoDAO.class)).thenReturn(contratoDAO);
		when(dao.get(DocumentoComplementarDAO.class)).thenReturn(docComplementarDAO);
		when(dao.get(ParalisacaoDAO.class)).thenReturn(paralisacaoDAO);
		when(dao.get(ContratoResponsavelTecnicoDAO.class)).thenReturn(contratoRespTecDAO);
		when(dao.getJdbi()).thenReturn(jdbi);
		
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
	void testConsultarUsuario_naoCadastradoSiconv() {

		ContratoSiconvDTO contrato = newContratoDTOBuilder().setId(1L).create();

		when(contratoConsumer.consultarContratoPorId(1L)).thenReturn(Optional.of(contrato));
		when(usuarioConsumer.getUsuario(CPF, TipoResponsavelTecnicoEnum.FIS, contrato, true)).thenReturn(null);

		assertThrowsMedicaoRestException(MessageKey.ERRO_USUARIO_NAO_CADASTRADO_SICONV,
				() -> respTecBC.consultarUsuario(CPF, TipoResponsavelTecnicoEnum.FIS, 1L, true));
	}

	@Test
	void testConsultarUsuario_naoCadastradoSiconv_semValidacao() {

		ContratoSiconvDTO contrato = newContratoDTOBuilder().setId(1L).create();

		when(contratoConsumer.consultarContratoPorId(1L)).thenReturn(Optional.of(contrato));
		when(usuarioConsumer.getUsuario(CPF, TipoResponsavelTecnicoEnum.FIS, contrato, true)).thenReturn(null);

		assertNull(respTecBC.consultarUsuario(CPF, TipoResponsavelTecnicoEnum.FIS, 1L, false));
	}

	@Test
	void testConsultarUsuario_naoCadastradoMaisBrasil() {

		ContratoSiconvDTO contrato = newContratoDTOBuilder().setId(1L).create();

		when(contratoConsumer.consultarContratoPorId(1L)).thenReturn(Optional.of(contrato));
		when(usuarioConsumer.getUsuario(CPF, TipoResponsavelTecnicoEnum.EXE, contrato, true)).thenReturn(null);

		assertThrowsMedicaoRestException(MessageKey.ERRO_USUARIO_NAO_CADASTRADO_MAIS_BRASIL,
				() -> respTecBC.consultarUsuario(CPF, TipoResponsavelTecnicoEnum.EXE, 1L, true));
	}

	@Test
	void testConsultarUsuario_naoCadastradoMaisBrasil_semValidacao() {

		ContratoSiconvDTO contrato = newContratoDTOBuilder().setId(1L).create();

		when(contratoConsumer.consultarContratoPorId(1L)).thenReturn(Optional.of(contrato));
		when(usuarioConsumer.getUsuario(CPF, TipoResponsavelTecnicoEnum.EXE, contrato, true)).thenReturn(null);

		assertNull(respTecBC.consultarUsuario(CPF, TipoResponsavelTecnicoEnum.EXE, 1L, false));
	}

	@Test
	void testConsultarUsuario_contratoInexistente() {
		
		
		Optional<ContratoSiconvDTO> contratoOptional = Optional.ofNullable(null);
		
		when(contratoConsumer.consultarContratoPorId(1L)).thenReturn(contratoOptional);
		
		assertThrowsMedicaoRestException(MessageKey.CONTRATO_INEXISTENTE,
				() -> respTecBC.consultarUsuario(CPF, TipoResponsavelTecnicoEnum.EXE, 1L, true));
	}
	
	@Test
	void testConsultarUsuario_RT_ANS() {
		
		
		ContratoSiconvDTO contrato = newContratoDTOBuilder().setId(1L).setCnpj(CNPJ).create();
		Optional<ContratoSiconvDTO> contratoOptional = Optional.of(contrato);
		
		when(contratoConsumer.consultarContratoPorId(1L)).thenReturn(contratoOptional);
		when(usuarioConsumer.getUsuario(CPF, TipoResponsavelTecnicoEnum.ANS, contrato, true)).thenReturn(null);
				
		UsuarioDTO usuarioDTO = respTecBC.consultarUsuario(CPF, TipoResponsavelTecnicoEnum.ANS, 1L, false);
		
		assertThat(usuarioDTO, nullValue(UsuarioDTO.class));
	}
	
	@Test
	void testConsultarUsuario_RT_EXE_semVinculo() {
		
		UsuarioDTO usuario = newUsuarioDTOBuilder().setCPF(CPF).setNome(NOME).create();
		
		ContratoSiconvDTO contrato = newContratoDTOBuilder().setId(1L).setCnpj(CNPJ).create();
		Optional<ContratoSiconvDTO> contratoOptional = Optional.of(contrato);
		
		when(contratoConsumer.consultarContratoPorId(1L)).thenReturn(contratoOptional);
		when(usuarioConsumer.getUsuario(CPF, TipoResponsavelTecnicoEnum.EXE, contrato, true)).thenReturn(usuario);
				
		assertThrowsMedicaoRestException(MessageKey.CPF_EXISTENTE_NAO_VINCULADO_EMPRESA,
				() -> respTecBC.consultarUsuario(CPF, TipoResponsavelTecnicoEnum.EXE, 1L, true));
	}
	
	@Test
	void testConsultarUsuario_RT_EXE_comVinculo_SemAcessoFuncionalidade() {
		
		UsuarioDTO usuario = newUsuarioDTOBuilder().setCPF(CPF).setNome(NOME)
				.vinculadoEmpresa().create();
		
		ContratoSiconvDTO contrato = newContratoDTOBuilder().setId(1L).setCnpj(CNPJ).create();
		Optional<ContratoSiconvDTO> contratoOptional = Optional.of(contrato);
		
		when(contratoConsumer.consultarContratoPorId(1L)).thenReturn(contratoOptional);
		when(usuarioConsumer.getUsuario(CPF, TipoResponsavelTecnicoEnum.EXE, contrato, true)).thenReturn(usuario);
				
		assertThrowsMedicaoRestException(MessageKey.ERRO_CPF_EXISTENTE_SEM_ACESSO_FUNCIONALIDADE,
				() -> respTecBC.consultarUsuario(CPF, TipoResponsavelTecnicoEnum.EXE, 1L, true));
	}
	
	@Test
	void testConsultarUsuario_RT_EXE_naoValidaUsuario() {
				
		UsuarioDTO usuario = newUsuarioDTOBuilder().setCPF(CPF).setNome(NOME).create();
		
		ContratoSiconvDTO contrato = newContratoDTOBuilder().setId(1L).setCnpj(CNPJ).create();
		Optional<ContratoSiconvDTO> contratoOptional = Optional.of(contrato);
		
		when(contratoConsumer.consultarContratoPorId(1L)).thenReturn(contratoOptional);
		when(usuarioConsumer.getUsuario(CPF, TipoResponsavelTecnicoEnum.EXE, contrato, true)).thenReturn(usuario);
		
		UsuarioDTO usuarioDTO = respTecBC.consultarUsuario(CPF, TipoResponsavelTecnicoEnum.EXE, 1L, false);
		
		assertEquals(usuario, usuarioDTO);
	}
	
	@Test
	void testConsultarUsuario_RT_EXE() {
		
		UsuarioDTO usuario = newUsuarioDTOBuilder().setCPF(CPF).setNome(NOME)
				.vinculadoEmpresa().assinanteSubmetaEmpresa().create();
		
		ContratoSiconvDTO contrato = newContratoDTOBuilder().setId(1L).setCnpj(CNPJ).create();
		Optional<ContratoSiconvDTO> contratoOptional = Optional.of(contrato);
		
		when(contratoConsumer.consultarContratoPorId(1L)).thenReturn(contratoOptional);
		when(usuarioConsumer.getUsuario(CPF, TipoResponsavelTecnicoEnum.EXE, contrato, true)).thenReturn(usuario);
		
		UsuarioDTO usuarioDTO = respTecBC.consultarUsuario(CPF, TipoResponsavelTecnicoEnum.EXE, 1L, true);
		
		assertEquals(usuario, usuarioDTO);
	}
	
	@Test
	void testConsultarUsuario_RT_FIS_semVinculo() {
		
		Integer numeroConvenio = 1;
		Integer anoConvenio = 2020;
		
		UsuarioDTO usuario = newUsuarioDTOBuilder().setCPF(CPF).setNome(NOME).create();
		
		ContratoSiconvDTO contrato = newContratoDTOBuilder().setId(1L).setAnoConvenioRepasse(anoConvenio)
				.setNumeroConvenioRepasse(numeroConvenio).create();
		Optional<ContratoSiconvDTO> contratoOptional = Optional.of(contrato);
		
		when(contratoConsumer.consultarContratoPorId(1L)).thenReturn(contratoOptional);
		when(usuarioConsumer.getUsuario(CPF, TipoResponsavelTecnicoEnum.FIS, contrato, true)).thenReturn(usuario);
				
		assertThrowsMedicaoRestException(MessageKey.CONTRATO_EXISTENTE_NAO_VINCULADO,
				() -> respTecBC.consultarUsuario(CPF, TipoResponsavelTecnicoEnum.FIS, 1L, true));
	}
	
	@Test
	void testConsultarUsuario_RT_FIS_comVinculo_naoEhFiscal() {
		
		Integer numeroConvenio = 1;
		Integer anoConvenio = 2020;
		
		UsuarioDTO usuario = newUsuarioDTOBuilder().setCPF(CPF).setNome(NOME).vinculadoConvenioAtual().create();
		
		ContratoSiconvDTO contrato = newContratoDTOBuilder().setId(1L).setAnoConvenioRepasse(anoConvenio)
				.setNumeroConvenioRepasse(numeroConvenio).create();
		Optional<ContratoSiconvDTO> contratoOptional = Optional.of(contrato);
		
		when(contratoConsumer.consultarContratoPorId(1L)).thenReturn(contratoOptional);
		when(usuarioConsumer.getUsuario(CPF, TipoResponsavelTecnicoEnum.FIS, contrato, true)).thenReturn(usuario);
				
		assertThrowsMedicaoRestException(MessageKey.ERRO_USUARIO_VINCULADO_SEM_PERFIL_FISCAL_CONVENENTE,
				() -> respTecBC.consultarUsuario(CPF, TipoResponsavelTecnicoEnum.FIS, 1L, true));
	}

	@Test
	void testConsultarUsuario_RT_FIS_naoValidaUsuario() {
		
		Integer numeroConvenio = 1;
		Integer anoConvenio = 2020;
		
		UsuarioDTO usuario = newUsuarioDTOBuilder().setCPF(CPF).setNome(NOME).create();
		
		ContratoSiconvDTO contrato = newContratoDTOBuilder().setId(1L).setAnoConvenioRepasse(anoConvenio)
				.setNumeroConvenioRepasse(numeroConvenio).create();
		Optional<ContratoSiconvDTO> contratoOptional = Optional.of(contrato);
		
		when(contratoConsumer.consultarContratoPorId(1L)).thenReturn(contratoOptional);
		when(usuarioConsumer.getUsuario(CPF, TipoResponsavelTecnicoEnum.FIS, contrato, true)).thenReturn(usuario);
				
		UsuarioDTO usuarioDTO = respTecBC.consultarUsuario(CPF, TipoResponsavelTecnicoEnum.FIS, 1L, false);
		
		assertEquals(usuario, usuarioDTO);
	}
	
	@Test
	void testConsultarUsuario_RT_FIS_inativo() {

		Integer numeroConvenio = 1;
		Integer anoConvenio = 2020;

		UsuarioDTO usuario = newUsuarioDTOBuilder().setCPF(CPF).setNome(NOME).vinculadoConvenioAtual()
				.fiscalConvenente().inativo().create();

		ContratoSiconvDTO contrato = newContratoDTOBuilder().setId(1L).setAnoConvenioRepasse(anoConvenio)
				.setNumeroConvenioRepasse(numeroConvenio).create();
		Optional<ContratoSiconvDTO> contratoOptional = Optional.of(contrato);

		when(contratoConsumer.consultarContratoPorId(1L)).thenReturn(contratoOptional);
		when(usuarioConsumer.getUsuario(CPF, TipoResponsavelTecnicoEnum.FIS, contrato, true)).thenReturn(usuario);

		assertThrowsMedicaoRestException(MessageKey.ERRO_USUARIO_INATIVO_SICONV,
				() -> respTecBC.consultarUsuario(CPF, TipoResponsavelTecnicoEnum.FIS, 1L, true));
	}

	@Test
	void testConsultarUsuario_RT_FIS() {

		Integer numeroConvenio = 1;
		Integer anoConvenio = 2020;
		
		UsuarioDTO usuario = newUsuarioDTOBuilder().setCPF(CPF).setNome(NOME).vinculadoConvenioAtual()
				.fiscalConvenente().create();
		
		ContratoSiconvDTO contrato = newContratoDTOBuilder().setId(1L).setAnoConvenioRepasse(anoConvenio)
				.setNumeroConvenioRepasse(numeroConvenio).create();
		Optional<ContratoSiconvDTO> contratoOptional = Optional.of(contrato);
		
		when(contratoConsumer.consultarContratoPorId(1L)).thenReturn(contratoOptional);
		when(usuarioConsumer.getUsuario(CPF, TipoResponsavelTecnicoEnum.FIS, contrato, true)).thenReturn(usuario);
				
		UsuarioDTO usuarioDTO = respTecBC.consultarUsuario(CPF, TipoResponsavelTecnicoEnum.FIS, 1L, true);
		
		assertEquals(usuario, usuarioDTO);
	}
	
	@Test
	void testSalvar_contratoSocial() {
		
		ResponsavelTecnicoDTO respTecDTO = new ResponsavelTecnicoDTO();
		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setInSocial(Boolean.TRUE);
		
		Optional<ContratoSiconvDTO> contratoOptional = Optional.of(contrato);
		
		when(contratoConsumer.consultarContratoPorId(1L)).thenReturn(contratoOptional);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_EDITAR_RT_ART_RRT_CONTRATO_SOCIAL,
				() -> respTecBC.salvar(respTecDTO, 1L));
	}
	
	@Test
	void testSalvarResponsavelTecnico_parametrosInvalidos() {
		
		ResponsavelTecnicoDTO respTecDTO = new ResponsavelTecnicoDTO();
		respTecDTO.setId(1L);
		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setInSocial(Boolean.FALSE);
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
			
		Optional<ContratoSiconvDTO> contratoOptional = Optional.of(contrato);
	
		when(contratoConsumer.consultarContratoPorId(1L)).thenReturn(contratoOptional);
		when(contratoDAO.consultarContratoPorContratoFK(contrato.getId())).thenReturn(contratoMedicao);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_RESPONSAVEL_TECNICO_PARAMETROS_INVALIDOS,
				() -> respTecBC.salvar(respTecDTO, 1L));
	}
	
	@Test
	void testSalvarResponsavelTecnico_jaExistente() {
		
		ResponsavelTecnicoDTO respTecDTO = new ResponsavelTecnicoDTO();
	//	respTecDTO.setId(1L);
		respTecDTO.setCpf(CPF);
		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setInSocial(Boolean.FALSE);
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
			
		Optional<ContratoSiconvDTO> contratoOptional = Optional.of(contrato);
	
		when(contratoConsumer.consultarContratoPorId(1L)).thenReturn(contratoOptional);
		when(contratoDAO.consultarContratoPorContratoFK(contrato.getId())).thenReturn(contratoMedicao);
		when(respTecDAO.consultarRegistrosRespTecnico(CPF)).thenReturn(respTecDTO);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_RESPONSAVEL_TECNICO_JA_EXISTENTE,
				() -> respTecBC.salvar(respTecDTO, 1L));
	}
	
	@Test
	void testSalvarResponsavelTecnico_alterarRegistroProfissionalNaoCadastrado() {
		
		ResponsavelTecnicoDTO respTecDTO = new ResponsavelTecnicoDTO();
		respTecDTO.setId(1L);
		respTecDTO.setCpf(CPF);
		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setInSocial(Boolean.FALSE);
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
			
		Optional<ContratoSiconvDTO> contratoOptional = Optional.of(contrato);
	
		when(contratoConsumer.consultarContratoPorId(1L)).thenReturn(contratoOptional);
		when(contratoDAO.consultarContratoPorContratoFK(contrato.getId())).thenReturn(contratoMedicao);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_REGISTRO_PROFISSIONAL_NAO_CADASTRADO,
				() -> respTecBC.salvar(respTecDTO, 1L));
	}
	
	@Test
	void testSalvarResponsavelTecnico_variosContratosAssociados() {
		
		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setInSocial(Boolean.FALSE);
		
		ResponsavelTecnicoDTO respTecDTO = new ResponsavelTecnicoDTO();
		respTecDTO.setId(1L);
		respTecDTO.setCpf(CPF);
		
		RegistroProfissionalDTO regProfDTO = new RegistroProfissionalDTO();
		regProfDTO.setId(1L);
		
		ContratoResponsavelTecnicoDTO contratoRegistroDTO = new ContratoResponsavelTecnicoDTO();
		contratoRegistroDTO.setContratoFk(contrato.getId());
		ArrayList<ContratoResponsavelTecnicoDTO> contratos= new ArrayList<>();
		contratos.add(contratoRegistroDTO);
		contratos.add(contratoRegistroDTO);
		regProfDTO.setContratos(contratos);
		respTecDTO.addRegistros(regProfDTO);
		
		
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
			
		Optional<ContratoSiconvDTO> contratoOptional = Optional.of(contrato);
	
		when(contratoConsumer.consultarContratoPorId(1L)).thenReturn(contratoOptional);
		when(contratoDAO.consultarContratoPorContratoFK(contrato.getId())).thenReturn(contratoMedicao);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_CONTRATO_NAO_ASSOCIADO_A_EXATAMENTE_UM_REGISTRO_PROFISSIONAL,
				() -> respTecBC.salvar(respTecDTO, 1L));
	}
	
	@Test
	void testSalvarResponsavelTecnico_registroDuplicadoCrea() {
		
		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setInSocial(Boolean.FALSE);
		
		ResponsavelTecnicoDTO respTecDTO = new ResponsavelTecnicoDTO();
		respTecDTO.setId(1L);
		respTecDTO.setCpf(CPF);
		
		RegistroProfissionalDTO regProfDTO = new RegistroProfissionalDTO();
		regProfDTO.setId(1L);
		regProfDTO.setAtividade(AtividadeRegistroProfissionalEnum.ENG.getDescricao());
		regProfDTO.setUf("PE");
		
		RegistroProfissionalDTO regProfDTO2 = new RegistroProfissionalDTO();
		regProfDTO2.setId(2L);
		regProfDTO2.setAtividade(AtividadeRegistroProfissionalEnum.ENG.getDescricao());
		regProfDTO2.setUf("PE");
		
		
		ContratoResponsavelTecnicoDTO contratoRegistroDTO = new ContratoResponsavelTecnicoDTO();
		contratoRegistroDTO.setContratoFk(contrato.getId());
		ArrayList<ContratoResponsavelTecnicoDTO> contratos= new ArrayList<>();
		contratos.add(contratoRegistroDTO);
		regProfDTO.setContratos(contratos);
		respTecDTO.addRegistros(regProfDTO);
		respTecDTO.addRegistros(regProfDTO2);		
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
			
		Optional<ContratoSiconvDTO> contratoOptional = Optional.of(contrato);
	
		when(contratoConsumer.consultarContratoPorId(1L)).thenReturn(contratoOptional);
		when(contratoDAO.consultarContratoPorContratoFK(contrato.getId())).thenReturn(contratoMedicao);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_REGISTRO_PROFISSIONAL_DUPLICADO_CREA_UF,
				() -> respTecBC.salvar(respTecDTO, 1L));
	}
	
	@Test
	void testSalvarResponsavelTecnico_registroDuplicadoCau() {
		
		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setInSocial(Boolean.FALSE);
		
		ResponsavelTecnicoDTO respTecDTO = new ResponsavelTecnicoDTO();
		respTecDTO.setId(1L);
		respTecDTO.setCpf(CPF);
		
		RegistroProfissionalDTO regProfDTO = new RegistroProfissionalDTO();
		regProfDTO.setId(1L);
		regProfDTO.setAtividade(AtividadeRegistroProfissionalEnum.ARQ.getDescricao());
		
		RegistroProfissionalDTO regProfDTO2 = new RegistroProfissionalDTO();
		regProfDTO2.setId(2L);
		regProfDTO2.setAtividade(AtividadeRegistroProfissionalEnum.ARQ.getDescricao());
				
		ContratoResponsavelTecnicoDTO contratoRegistroDTO = new ContratoResponsavelTecnicoDTO();
		contratoRegistroDTO.setContratoFk(contrato.getId());
		ArrayList<ContratoResponsavelTecnicoDTO> contratos= new ArrayList<>();
		contratos.add(contratoRegistroDTO);
		regProfDTO.setContratos(contratos);
		respTecDTO.addRegistros(regProfDTO);
		respTecDTO.addRegistros(regProfDTO2);		
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
			
		Optional<ContratoSiconvDTO> contratoOptional = Optional.of(contrato);
	
		when(contratoConsumer.consultarContratoPorId(1L)).thenReturn(contratoOptional);
		when(contratoDAO.consultarContratoPorContratoFK(contrato.getId())).thenReturn(contratoMedicao);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_REGISTRO_PROFISSIONAL_DUPLICADO_CAU,
				() -> respTecBC.salvar(respTecDTO, 1L));
	}
	
	@Test
	void teste_excluirVinculoRespTecContrato_Inexistente () {
		
		Long idContratoRespTec = 2L;
		
		Optional<ContratoResponsavelTecnicoBD> optContratoResponsavelTecnicoBD = Optional.empty();
		
		doReturn(optContratoResponsavelTecnicoBD).when(contratoRespTecDAO).consultar(idContratoRespTec);
		
		assertThrowsMedicaoRestException(MessageKey.CONTRATO_RESP_TEC_INEXISTENTE,() -> respTecBC.excluirVinculoRespTecContrato(idContratoRespTec));
		
	}

	@Test
	void teste_excluirVinculoRespTecContrato_comSubmetaAssinada () {
		
		Long idContratoRespTec = 2L;
		Long idContrato = 1L;
		
		ContratoResponsavelTecnicoBDBuilder builder = ContratoResponsavelTecnicoBDBuilder.newContratoResponsavelTecnicoBDBuilder();
		builder.setId(idContratoRespTec);
		builder.setIdMedContrato(idContrato);
		
		Optional<ContratoResponsavelTecnicoBD> optContratoResponsavelTecnicoBD = Optional.of(builder.create());
		
		doReturn(optContratoResponsavelTecnicoBD).when(contratoRespTecDAO).consultar(idContratoRespTec);
		doReturn(true).when(contratoRespTecDAO).isContRespTecnicoAssinado(idContratoRespTec);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_EXCLUIR_RT_POSSUI_SUBMETA_ASSINADA,
				() -> respTecBC.excluirVinculoRespTecContrato(idContratoRespTec));		
		
	}
	
	
	@Test
	void teste_excluirVinculoRespTecContrato_comART () {
		
		Long idContratoRespTec = 2L;
		Long idContrato = 1L;
		
		ContratoResponsavelTecnicoBDBuilder builder = ContratoResponsavelTecnicoBDBuilder.newContratoResponsavelTecnicoBDBuilder();
		builder.setId(idContratoRespTec);
		builder.setIdMedContrato(idContrato);
		
		Optional<ContratoResponsavelTecnicoBD> optContratoResponsavelTecnicoBD = Optional.of(builder.create());
		
		doReturn(optContratoResponsavelTecnicoBD).when(contratoRespTecDAO).consultar(idContratoRespTec);
		doReturn(true).when(contratoRespTecDAO).isContRespTecnicoAnotado(idContratoRespTec);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_EXCLUIR_RT_POSSUI_ANOTACAO,
				() -> respTecBC.excluirVinculoRespTecContrato(idContratoRespTec));		
		
	}

	
	@ParameterizedTest
	@CsvSource ({
		"false, false, false, 1",
		"false, false, true, 0",
		"false, true,  false, 0",
		"false, true,  true, 0",
		"true, false, false, 0",
		"true, false, true, 0",
		"true, true,  false, 0",
		"true, true,  true, 0",
	})
	void teste_excluirRT_excluirEstrutura (boolean existeRT, boolean existeDoc, boolean existeParalisacao, byte excluirEstruturaTimes) {
		
		Long idContratoRespTec = 2L;
		Long idContrato = 1L;
		
		ContratoResponsavelTecnicoBDBuilder builder = ContratoResponsavelTecnicoBDBuilder.newContratoResponsavelTecnicoBDBuilder();
		builder.setId(idContratoRespTec);
		builder.setIdMedContrato(idContrato);
		
		Optional<ContratoResponsavelTecnicoBD> optContratoResponsavelTecnicoBD = Optional.of(builder.create());
		
		doReturn(optContratoResponsavelTecnicoBD).when(contratoRespTecDAO).consultar(idContratoRespTec);
		doReturn(existeRT).when (contratoRespTecDAO).existeRespTecnicoContrato(idContrato);
		doReturn(existeDoc).when (docComplementarDAO).existeDocumentoComplementarContrato(idContrato);
		doReturn(existeParalisacao).when (paralisacaoDAO).existeParalisacaoContrato(idContrato);

		respTecBC.excluirVinculoRespTecContrato(idContratoRespTec);
		verify(contratoRespTecDAO, times (1)).excluir(idContratoRespTec);
		verify(contratoBC,times(excluirEstruturaTimes)).excluirEstruturaContrato(idContrato, handle);
	}

	private void assertThrowsMedicaoRestException(MessageKey expectedMessageKey, Executable executable) {

		MedicaoRestException exception = assertThrows(MedicaoRestException.class, executable);

		exception.getMessages().stream().map(Message::getKey).findFirst().ifPresentOrElse(
				actualMessageKey -> assertEquals(expectedMessageKey, actualMessageKey),
				() -> fail(format("A messageKey esperada era %s, mas nenhuma foi obtida", expectedMessageKey)));
	}
	
	
}
