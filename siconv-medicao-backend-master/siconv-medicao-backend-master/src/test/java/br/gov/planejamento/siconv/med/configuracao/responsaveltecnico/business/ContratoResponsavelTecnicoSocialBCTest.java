package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.business;

import static br.gov.planejamento.siconv.med.test.builder.ContratoMedicaoBuilder.newContratoMedicaoBuilder;
import static br.gov.planejamento.siconv.med.test.builder.ContratoResponsavelTecnicoSocialDTOBuilder.newContratoResponsavelTecnicoSocialDTOBuilder;
import static br.gov.planejamento.siconv.med.test.builder.ResponsavelTecnicoDTOBuilder.newResponsavelTecnicoDTOBuilder;
import static br.gov.planejamento.siconv.med.test.builder.UsuarioDTOBuilder.newUsuarioDTOBuilder;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Validator;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleCallback;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.dao.DocumentoComplementarDAO;
import br.gov.planejamento.siconv.med.configuracao.paralisacao.dao.ParalisacaoDAO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao.ContratoResponsavelTecnicoSocialDAO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao.RegistroProfissionalDAO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao.ResponsavelTecnicoDAO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.ContratoResponsavelTecnicoSocialDTO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.ContratoResponsavelTecnicoSocialDTO.ExecucaoGroup;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.ResponsavelTecnicoDTO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.ResponsavelTecnicoElegivelDTO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.TipoResponsavelTecnicoEnum;
import br.gov.planejamento.siconv.med.contrato.business.ContratosBC;
import br.gov.planejamento.siconv.med.contrato.dao.ContratoDAO;
import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.contrato.entity.dto.ContratoSiconvDTO;
import br.gov.planejamento.siconv.med.infra.database.DAOFactory;
import br.gov.planejamento.siconv.med.infra.exception.MedicaoRestException;
import br.gov.planejamento.siconv.med.infra.message.Message;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.infra.security.UsuarioLogado;
import br.gov.planejamento.siconv.med.integration.UsuarioConsumer;
import br.gov.planejamento.siconv.med.integration.ceph.CephActions;
import br.gov.planejamento.siconv.med.integration.contratos.ContratosGrpcConsumer;
import br.gov.planejamento.siconv.med.integration.dto.UsuarioDTO;
import br.gov.planejamento.siconv.med.medicao.business.SubmetaBC;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;
import br.gov.planejamento.siconv.med.test.builder.ContratoResponsavelTecnicoSocialDTOBuilder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

class ContratoResponsavelTecnicoSocialBCTest {
	
	final String CPF = "11111111111";
	final String CNPJ = "2222222222222222";
	final String NOME = "usuario";
	final UsuarioLogado usuarioLogado = mock(UsuarioLogado.class);
	final Long idContratoSiconv = 1L;
	
	@Mock
	private Jdbi jdbi;
	
	@Getter(AccessLevel.PROTECTED)
	@Setter(AccessLevel.PROTECTED)
	
	@Mock
	private DAOFactory dao;

	@Mock
	private Handle handle;
	
	@Mock
	private ContratoDAO contratoDAO;

	@Mock
	private ResponsavelTecnicoDAO respTecDAO;
	
	@Mock
	private RegistroProfissionalDAO regProfDAO;
	
	@Mock
	private ParalisacaoDAO paralisacaoDAO;
	
	@Mock
	private UsuarioConsumer usuarioConsumer;

	@Mock
	private ContratosGrpcConsumer contratoConsumer;
	
	@Mock
	private ContratoResponsavelTecnicoSocialDAO contratoRespTecSocialDAO;

	@Mock
	private DocumentoComplementarDAO docComplementarDAO;
	
	@Mock
	private ResponsavelTecnicoBC responsavelTecnicoBC;
	
	@Mock
	private SubmetaBC submetaBC;

	@Mock
	private ContratosBC contratoBC;

	@Mock
	private CephActions cephActions;
	
	@Mock
	private Validator validator;
	
	@InjectMocks
	private ContratoResponsavelTecnicoSocialBC contratoRespTecSocialBC;
	
	
	@BeforeEach
	void setup() throws Exception {

		MockitoAnnotations.initMocks(this);
		
		when(handle.attach(ContratoResponsavelTecnicoSocialDAO.class)).thenReturn(contratoRespTecSocialDAO);
		when(handle.attach(ResponsavelTecnicoDAO.class)).thenReturn(respTecDAO);
		when(handle.attach(RegistroProfissionalDAO.class)).thenReturn(regProfDAO);
		when(handle.attach(DocumentoComplementarDAO.class)).thenReturn(docComplementarDAO);

		when(jdbi.onDemand(ContratoResponsavelTecnicoSocialDAO.class)).thenReturn(contratoRespTecSocialDAO);
		when(jdbi.onDemand(ResponsavelTecnicoDAO.class)).thenReturn(respTecDAO);
		when(jdbi.onDemand(RegistroProfissionalDAO.class)).thenReturn(regProfDAO);
		when(jdbi.onDemand(DocumentoComplementarDAO.class)).thenReturn(docComplementarDAO);
		
		when(dao.get(ContratoResponsavelTecnicoSocialDAO.class)).thenReturn(contratoRespTecSocialDAO);
		when(dao.get(ContratoDAO.class)).thenReturn(contratoDAO);
		when(dao.get(ResponsavelTecnicoDAO.class)).thenReturn(respTecDAO);
		when(dao.get(RegistroProfissionalDAO.class)).thenReturn(regProfDAO);
		when(dao.get(DocumentoComplementarDAO.class)).thenReturn(docComplementarDAO);
		when(dao.get(ParalisacaoDAO.class)).thenReturn(paralisacaoDAO);
		
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
	void testListarResponsavelTecnicoSocialPorContrato() {
		
		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setInSocial(Boolean.TRUE);
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
		
		UsuarioDTO usuario = newUsuarioDTOBuilder().setCPF(CPF).setNome(NOME).vinculadoEmpresa()
				.assinanteSubmetaEmpresa().create();
		
				
		ContratoResponsavelTecnicoSocialDTO contratoDTO = new ContratoResponsavelTecnicoSocialDTO();
		contratoDTO.setId(1L);
		contratoDTO.setTipo(TipoResponsavelTecnicoEnum.EXE);
		contratoDTO.setCodigoCephArquivo("teste.com.br");
		ResponsavelTecnicoDTO respTecDTO = new ResponsavelTecnicoDTO();
		respTecDTO.setCpf(CPF);
		contratoDTO.setResponsavelTecnico(respTecDTO);
		List<ContratoResponsavelTecnicoSocialDTO> listaContratos = new ArrayList<ContratoResponsavelTecnicoSocialDTO>();
		listaContratos.add(contratoDTO);	
		
		SubmetaVrplDTO submetaVrplDTO = new SubmetaVrplDTO();
		submetaVrplDTO.setId(1L);
		List<SubmetaVrplDTO> submetasMedicao = new ArrayList<SubmetaVrplDTO>();
		submetasMedicao.add(submetaVrplDTO);
		
		Optional<ContratoSiconvDTO> contratoOptional = Optional.of(contrato);
				
		when(contratoRespTecSocialDAO.listarResponsavelTecnicoSocialPorContrato(idContratoSiconv)).thenReturn(listaContratos);
		when(contratoDAO.consultarContratoPorContratoFK(contrato.getId())).thenReturn(contratoMedicao);
		when(contratoConsumer.consultarContratoPorId(1L)).thenReturn(contratoOptional);
		when(usuarioConsumer.getUsuario(CPF, TipoResponsavelTecnicoEnum.EXE, contrato, true)).thenReturn(usuario);
		when(responsavelTecnicoBC.consultarUsuario(CPF, TipoResponsavelTecnicoEnum.EXE, contrato.getId(), false)).thenReturn(usuario);
		when(submetaBC.listarSubmetasPorContrato(contrato.getId())).thenReturn(submetasMedicao);
		when(cephActions.getPresignedUrl(contratoDTO.getCodigoCephArquivo())).thenReturn(contratoDTO.getCodigoCephArquivo());
		
		List<ContratoResponsavelTecnicoSocialDTO> listaRetorno = contratoRespTecSocialBC.listarResponsavelTecnicoSocialPorContrato(idContratoSiconv); 
		
		assertTrue(!listaRetorno.isEmpty());
	}
	
	@Test
	void testSalvar_submetaNaoSelecionadaRTSocial() {
			
		ContratoResponsavelTecnicoSocialDTO contratoDTO = new ContratoResponsavelTecnicoSocialDTO();
		contratoDTO.setId(1L);
		contratoDTO.setTipo(TipoResponsavelTecnicoEnum.EXE);
		contratoDTO.setCodigoCephArquivo("teste.com.br");
		ResponsavelTecnicoDTO respTecDTO = new ResponsavelTecnicoDTO();
		respTecDTO.setCpf(CPF);
		contratoDTO.setResponsavelTecnico(respTecDTO);
		List<ContratoResponsavelTecnicoSocialDTO> listaContratos = new ArrayList<ContratoResponsavelTecnicoSocialDTO>();
		listaContratos.add(contratoDTO);	
			
		when(validator.validate(contratoDTO, ExecucaoGroup.class)).thenReturn(null);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_SUBMETA_NAO_SELECIONADA_PARA_RT_SOCIAL,
				() -> contratoRespTecSocialBC.salvar(contratoDTO, idContratoSiconv));

	}
	
	@Test
	void testSalvar_contratoNaoPodeSerEditado() {
		
		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setInSocial(Boolean.FALSE);
		
		SubmetaVrplDTO submetaVrplDTO = new SubmetaVrplDTO();
		submetaVrplDTO.setId(1L);
		List<SubmetaVrplDTO> submetasMedicao = new ArrayList<SubmetaVrplDTO>();
		submetasMedicao.add(submetaVrplDTO);
				
		ContratoResponsavelTecnicoSocialDTO contratoDTO = new ContratoResponsavelTecnicoSocialDTO();
		contratoDTO.setId(1L);
		contratoDTO.setTipo(TipoResponsavelTecnicoEnum.EXE);
		contratoDTO.setCodigoCephArquivo("teste.com.br");
		contratoDTO.setSubmetas(submetasMedicao);
		ResponsavelTecnicoDTO respTecDTO = new ResponsavelTecnicoDTO();
		respTecDTO.setCpf(CPF);
		contratoDTO.setResponsavelTecnico(respTecDTO);
		List<ContratoResponsavelTecnicoSocialDTO> listaContratos = new ArrayList<ContratoResponsavelTecnicoSocialDTO>();
		listaContratos.add(contratoDTO);	
		
		when(contratoBC.consultarContratoPorId(1L)).thenReturn(contrato);
		when(validator.validate(contratoDTO, ExecucaoGroup.class)).thenReturn(null);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_EDITAR_RT_ART_RRT_CONTRATO_SOCIAL,
				() -> contratoRespTecSocialBC.salvar(contratoDTO, idContratoSiconv));

	}
	

	@Test
	void testSalvar_contratoInexistente() {
		
		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setInSocial(Boolean.TRUE);
		

		SubmetaVrplDTO submetaVrplDTO = new SubmetaVrplDTO();
		submetaVrplDTO.setId(1L);
		List<SubmetaVrplDTO> submetasMedicao = new ArrayList<SubmetaVrplDTO>();
		submetasMedicao.add(submetaVrplDTO);
				
		ContratoResponsavelTecnicoSocialDTO contratoDTO = new ContratoResponsavelTecnicoSocialDTO();
		contratoDTO.setId(1L);
		contratoDTO.setTipo(TipoResponsavelTecnicoEnum.EXE);
		contratoDTO.setCodigoCephArquivo("teste.com.br");
		contratoDTO.setSubmetas(submetasMedicao);
		ResponsavelTecnicoDTO respTecDTO = new ResponsavelTecnicoDTO();
		respTecDTO.setCpf(CPF);
		contratoDTO.setResponsavelTecnico(respTecDTO);
		List<ContratoResponsavelTecnicoSocialDTO> listaContratos = new ArrayList<ContratoResponsavelTecnicoSocialDTO>();
		listaContratos.add(contratoDTO);	
		
		when(contratoBC.consultarContratoPorId(1L)).thenReturn(null);
		when(validator.validate(contratoDTO, ExecucaoGroup.class)).thenReturn(null);
		
		assertThrowsMedicaoRestException(MessageKey.CONTRATO_INEXISTENTE,
				() -> contratoRespTecSocialBC.salvar(contratoDTO, idContratoSiconv));

	}
		
/*	@Test
	void testSalvar_() {
		
		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setInSocial(Boolean.TRUE);
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
		
		UsuarioDTO usuario = newUsuarioDTOBuilder().setCPF(CPF).setNome(NOME).setFiscal("S")
				.setVinculo("S").setPossuiPermissaoAssinarSubmeta(true).create();
		
				
		ContratoResponsavelTecnicoSocialDTO contratoDTO = new ContratoResponsavelTecnicoSocialDTO();
		contratoDTO.setId(1L);
		contratoDTO.setTipo(TipoResponsavelTecnicoEnum.EXE);
		contratoDTO.setCodigoCephArquivo("teste.com.br");
		ResponsavelTecnicoDTO respTecDTO = new ResponsavelTecnicoDTO();
		respTecDTO.setCpf(CPF);
		contratoDTO.setResponsavelTecnico(respTecDTO);
		List<ContratoResponsavelTecnicoSocialDTO> listaContratos = new ArrayList<ContratoResponsavelTecnicoSocialDTO>();
		listaContratos.add(contratoDTO);	
		
		SubmetaVrplDTO submetaVrplDTO = new SubmetaVrplDTO();
		submetaVrplDTO.setId(1L);
		List<SubmetaVrplDTO> submetasMedicao = new ArrayList<SubmetaVrplDTO>();
		submetasMedicao.add(submetaVrplDTO);
		
		Optional<ContratoSiconvDTO> contratoOptional = Optional.of(contrato);
				
		when(contratoRespTecSocialDAO.listarResponsavelTecnicoSocialPorContrato(idContratoSiconv)).thenReturn(listaContratos);
		when(contratoDAO.consultarContratoPorContratoFK(contrato.getId())).thenReturn(contratoMedicao);
		when(contratoConsumer.consultarContratoPorId(1L)).thenReturn(contratoOptional);
		when(usuarioConsumer.getUsuario(CPF, TipoResponsavelTecnicoEnum.EXE, contrato, true)).thenReturn(usuario);
		when(responsavelTecnicoBC.consultarUsuario(CPF, TipoResponsavelTecnicoEnum.EXE, contrato.getId(), false)).thenReturn(usuario);
	    when(submetaBC.listarSubmetasPorContrato(contrato.getId())).thenReturn(submetasMedicao);
		when(cephActions.getPresignedUrl(contratoDTO.getCodigoCephArquivo())).thenReturn(contratoDTO.getCodigoCephArquivo());
		when(validator.validate(contratoDTO, ExecucaoGroup.class)).thenReturn(null);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_EDITAR_RT_ART_RRT_CONTRATO_SOCIAL,
				() -> contratoRespTecSocialBC.salvar(contratoDTO, idContratoSiconv));

	}*/
	
	@Test
	void testConsultarResponsavelTecnicoSocial_RespTecnicoTipoDiferente() {

		ResponsavelTecnicoDTO rt = newResponsavelTecnicoDTOBuilder().setId(1L).setCpf(CPF).create();
		
		ContratoResponsavelTecnicoSocialDTO contratoDTO = newContratoResponsavelTecnicoSocialDTOBuilder().setId(1L)
				.setTipo(TipoResponsavelTecnicoEnum.EXE).setCodigoCephArquivo("teste.com.br").setResponsavelTecnico(rt).create();
		
		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setInSocial(Boolean.TRUE);	
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
		
		Optional<ContratoResponsavelTecnicoSocialDTO> contratoRespTecRtAtivo = Optional.of(contratoDTO);
		
		UsuarioDTO usuario = newUsuarioDTOBuilder().setCPF(CPF).setNome(NOME).vinculadoEmpresa().assinanteSubmetaEmpresa().create();
		
		when(contratoRespTecSocialDAO.consultarContratoResponsavelTecnicoSocialPorIdRtAtivoNoContrato(rt.getId(), contratoMedicao.getId()))
		.thenReturn(contratoRespTecRtAtivo);
		when(respTecDAO.consultarResponsavelTecnicoPorCpf(CPF)).thenReturn(rt);
		when(contratoDAO.consultarContratoPorContratoFK(idContratoSiconv)).thenReturn(contratoMedicao);	
		when(usuarioConsumer.getUsuario(CPF, TipoResponsavelTecnicoEnum.EXE, contrato, true)).thenReturn(usuario);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_RESPTECNICOSOCIAL_CADASTRADO_TIPO_DIFERENTE,
				() -> contratoRespTecSocialBC.consultarResponsavelTecnicoElegivel(contrato.getId(),CPF,TipoResponsavelTecnicoEnum.FIS.getCodigo() ));

	}
	
	@Test
	void testConsultarResponsavelTecnicoSocial_RespTecnicoInativo() {

		ResponsavelTecnicoDTO rt = newResponsavelTecnicoDTOBuilder().setId(1L).setCpf(CPF).create();
		
		ContratoResponsavelTecnicoSocialDTO contratoDTO = newContratoResponsavelTecnicoSocialDTOBuilder().setId(1L).setDtInativacao(LocalDateTime.of(2021, 10, 12, 00, 00))
				.setTipo(TipoResponsavelTecnicoEnum.EXE).setCodigoCephArquivo("teste.com.br").setResponsavelTecnico(rt).create();
		
		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setInSocial(Boolean.TRUE);	
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
		
		Optional<ContratoResponsavelTecnicoSocialDTO> contratoRespTecRtAtivo = Optional.of(contratoDTO);
		
		UsuarioDTO usuario = newUsuarioDTOBuilder().setCPF(CPF).setNome(NOME).vinculadoEmpresa().assinanteSubmetaEmpresa().create();
		
		when(contratoRespTecSocialDAO.consultarContratoResponsavelTecnicoSocialPorIdRtAtivoNoContrato(rt.getId(), contratoMedicao.getId()))
		.thenReturn(contratoRespTecRtAtivo);
		when(respTecDAO.consultarResponsavelTecnicoPorCpf(CPF)).thenReturn(rt);
		when(contratoDAO.consultarContratoPorContratoFK(idContratoSiconv)).thenReturn(contratoMedicao);	
		when(usuarioConsumer.getUsuario(CPF, TipoResponsavelTecnicoEnum.EXE, contrato, true)).thenReturn(usuario);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_EDITAR_RESP_TEC_INATIVO,
				() -> contratoRespTecSocialBC.consultarResponsavelTecnicoElegivel(contrato.getId(),CPF,TipoResponsavelTecnicoEnum.EXE.getCodigo() ));

	}
	
	@Test
	void testConsultarResponsavelTecnicoSocial_RespTecnicoAtivoValido() {

		ResponsavelTecnicoDTO rt = newResponsavelTecnicoDTOBuilder().setId(1L).setCpf(CPF).create();
		
		ContratoResponsavelTecnicoSocialDTO contratoRespTecSocialDTO = newContratoResponsavelTecnicoSocialDTOBuilder().setId(1L).setDtInativacao(null)
				.setTipo(TipoResponsavelTecnicoEnum.EXE).setCodigoCephArquivo("teste.com.br").setResponsavelTecnico(rt).create();
		
		ContratoSiconvDTO contratoSiconvDTO = new ContratoSiconvDTO();
		contratoSiconvDTO.setId(1L);
		contratoSiconvDTO.setInSocial(Boolean.TRUE);	
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contratoSiconvDTO.getId()).create();
		
		Optional<ContratoResponsavelTecnicoSocialDTO> contratoRespTecRtAtivo = Optional.of(contratoRespTecSocialDTO);
		
		UsuarioDTO usuario = newUsuarioDTOBuilder().setCPF(CPF).setNome(NOME).vinculadoEmpresa().assinanteSubmetaEmpresa().create();
		
		when(contratoRespTecSocialDAO.consultarContratoResponsavelTecnicoSocialPorIdRtAtivoNoContrato(rt.getId(), contratoMedicao.getId()))
		.thenReturn(contratoRespTecRtAtivo);
		when(respTecDAO.consultarResponsavelTecnicoPorCpf(CPF)).thenReturn(rt);
		when(contratoDAO.consultarContratoPorContratoFK(idContratoSiconv)).thenReturn(contratoMedicao);
		when(contratoConsumer.consultarContratoPorId(1L)).thenReturn(Optional.of(contratoSiconvDTO));
		when(usuarioConsumer.getUsuario(CPF, TipoResponsavelTecnicoEnum.EXE, contratoSiconvDTO, true)).thenReturn(usuario);		
        when(responsavelTecnicoBC.consultarUsuario(CPF, TipoResponsavelTecnicoEnum.EXE, contratoSiconvDTO.getId(), true)).thenReturn(usuario);
		
		ResponsavelTecnicoElegivelDTO respTecElegivelDTO = contratoRespTecSocialBC.consultarResponsavelTecnicoElegivel(contratoSiconvDTO.getId(),CPF,TipoResponsavelTecnicoEnum.EXE.getCodigo());

		assertNotNull(respTecElegivelDTO);
		
	}	
	
	
	@Test
	void testConsultarResponsavelTecnicoSocial_RespTecnicoNulo_NaoCadastradoMaisBrasil() {

		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setInSocial(Boolean.TRUE);	
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
		
		UsuarioDTO usuario = null;
		
		when(contratoDAO.consultarContratoPorContratoFK(idContratoSiconv)).thenReturn(contratoMedicao);	
		when(usuarioConsumer.getUsuario(CPF, TipoResponsavelTecnicoEnum.EXE, contrato, true)).thenReturn(usuario);
		when(responsavelTecnicoBC.consultarUsuario(CPF, TipoResponsavelTecnicoEnum.EXE, contrato.getId(), true))
				.thenThrow(new MedicaoRestException(MessageKey.ERRO_USUARIO_NAO_CADASTRADO_MAIS_BRASIL));
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_USUARIO_NAO_CADASTRADO_MAIS_BRASIL,
				() -> contratoRespTecSocialBC.consultarResponsavelTecnicoElegivel(contrato.getId(),CPF,TipoResponsavelTecnicoEnum.EXE.getCodigo() ));

	}
	
	@Test
	void testConsultarResponsavelTecnicoSocial_RespTecnicoNulo_NaoCadastradoSiconv() {

		ContratoSiconvDTO contrato = new ContratoSiconvDTO();
		contrato.setId(1L);
		contrato.setInSocial(Boolean.TRUE);	
		
		ContratoBD contratoMedicao = newContratoMedicaoBuilder().setId(1L)
				.setContratoSiconv(contrato.getId()).create();
		
		UsuarioDTO usuario = null;
		
		when(contratoDAO.consultarContratoPorContratoFK(idContratoSiconv)).thenReturn(contratoMedicao);	
		when(usuarioConsumer.getUsuario(CPF, TipoResponsavelTecnicoEnum.FIS, contrato, true)).thenReturn(usuario);
		when(responsavelTecnicoBC.consultarUsuario(CPF, TipoResponsavelTecnicoEnum.FIS, contrato.getId(), true))
		.thenThrow(new MedicaoRestException(MessageKey.ERRO_USUARIO_NAO_CADASTRADO_SICONV));
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_USUARIO_NAO_CADASTRADO_SICONV,
				() -> contratoRespTecSocialBC.consultarResponsavelTecnicoElegivel(contrato.getId(),CPF,TipoResponsavelTecnicoEnum.FIS.getCodigo() ));

	}	
	
	
	@Test
	void teste_excluirVinculoRespTecSocialContrato_contratoInexistente () {
		
		Optional<ContratoResponsavelTecnicoSocialDTO> retornoVazio = Optional.empty();
		
		when(contratoRespTecSocialDAO.consultarContratoResponsavelTecnicoSocialPorId(999L)).thenReturn(retornoVazio);
		
		assertThrowsMedicaoRestException(MessageKey.ERRO_RESPONSAVEL_TECNICO_SOCIAL_INEXISTENTE,() -> contratoRespTecSocialBC.excluir(999L));
		
	}

	@Test
	void teste_excluirVinculoRespTecSocialContrato_comSubmetaAssinada() {
		
		ContratoResponsavelTecnicoSocialDTOBuilder builder =  ContratoResponsavelTecnicoSocialDTOBuilder.newContratoResponsavelTecnicoSocialDTOBuilder();
		builder.setId(1L);
		
		
		Optional<ContratoResponsavelTecnicoSocialDTO> optContratoRespTecSocial = Optional.of(builder.create());
		
		when(contratoRespTecSocialDAO.consultarContratoResponsavelTecnicoSocialPorId(1L)).thenReturn(optContratoRespTecSocial);
		when(contratoRespTecSocialDAO.isContratoSocialAssinadoPeloResponsavelTecnico(1L)).thenReturn(Boolean.TRUE);

		assertThrowsMedicaoRestException(MessageKey.ERRO_RESPONSAVEL_TECNICO_SOCIAL_POSSUI_SUBMETA_ASSINADA,() -> contratoRespTecSocialBC.excluir(1L));
		
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
	void teste_excluirVinculoRespTecSocialContrato_semSubmetaAssinada(boolean existeRTSocial, boolean existeDoc, boolean existeParalisacao, byte excluirEstruturaTimes) {
		
		Long idContratoRespTecSocial = 2L;
		Long idContrato = 1L;
		
		ContratoResponsavelTecnicoSocialDTOBuilder builder =  ContratoResponsavelTecnicoSocialDTOBuilder.newContratoResponsavelTecnicoSocialDTOBuilder();
		builder.
		setId(idContratoRespTecSocial).
		setMedContratoFk(idContrato);
		
		Optional<ContratoResponsavelTecnicoSocialDTO> optContratoRespTecSocial = Optional.of(builder.create());
		
		doReturn(optContratoRespTecSocial).when(contratoRespTecSocialDAO).consultarContratoResponsavelTecnicoSocialPorId(idContratoRespTecSocial);
		doReturn(Boolean.FALSE).when(contratoRespTecSocialDAO).isContratoSocialAssinadoPeloResponsavelTecnico(idContratoRespTecSocial);
		doReturn(existeRTSocial).when (contratoRespTecSocialDAO).existeRespTecnicoSocialContrato(idContrato);
		doReturn(existeDoc).when (docComplementarDAO).existeDocumentoComplementarContrato(idContrato);
		doReturn(existeParalisacao).when (paralisacaoDAO).existeParalisacaoContrato(idContrato);
		
		contratoRespTecSocialBC.excluir(idContratoRespTecSocial);
		
		verify(contratoRespTecSocialDAO, times(1)).excluirSubmetaPorIdRTContratoSocial (idContratoRespTecSocial);
		verify(contratoRespTecSocialDAO, times(1)).excluir (idContratoRespTecSocial);
		verify(contratoBC,times(excluirEstruturaTimes)).excluirEstruturaContrato(idContrato, handle);
		
	}

	private void assertThrowsMedicaoRestException(MessageKey expectedMessageKey, Executable executable) {

		MedicaoRestException exception = assertThrows(MedicaoRestException.class, executable);

		exception.getMessages().stream().map(Message::getKey).findFirst().ifPresentOrElse(
				actualMessageKey -> assertEquals(expectedMessageKey, actualMessageKey),
				() -> fail(format("A messageKey esperada era %s, mas nenhuma foi obtida", expectedMessageKey)));
	}
	
}
