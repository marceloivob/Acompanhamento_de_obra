package br.gov.planejamento.siconv.med.configuracao.documentocomplementar.business;

import static br.gov.planejamento.siconv.med.test.builder.ContratoMedicaoBuilder.newContratoMedicaoBuilder;
import static br.gov.planejamento.siconv.med.test.builder.ContratoSiconvBuilder.newContratoDTOBuilder;
import static br.gov.planejamento.siconv.med.test.builder.DocumentoComplementarDTOBuilder.newDocumentoComplementar;
import static br.gov.planejamento.siconv.med.test.builder.MedicaoBuilder.newMedicaoBuilder;
import static br.gov.planejamento.siconv.med.test.builder.SubmetaVrplDTOBuilder.newSubmetaMedicaoBuilder;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.dao.DocumentoComplementarDAO;
import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.dao.DocumentoComplementarSubmetaDAO;
import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.DocumentoComplementarDTO;
import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.TipoDocumentoEnum;
import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.TipoManifestoEnum;
import br.gov.planejamento.siconv.med.configuracao.paralisacao.dao.ParalisacaoDAO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao.ContratoResponsavelTecnicoDAO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao.ContratoResponsavelTecnicoSocialDAO;
import br.gov.planejamento.siconv.med.contrato.business.ContratosBC;
import br.gov.planejamento.siconv.med.contrato.dao.ContratoDAO;
import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.contrato.entity.dto.ContratoSiconvDTO;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.infra.util.TemporalUtil;
import br.gov.planejamento.siconv.med.medicao.dao.MedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.database.MedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;
import br.gov.planejamento.siconv.med.test.builder.ContratoMedicaoBuilder;
import br.gov.planejamento.siconv.med.test.builder.DocumentoComplementarDTOBuilder;
import br.gov.planejamento.siconv.med.test.builder.SubmetaVrplDTOBuilder;
import br.gov.planejamento.siconv.med.test.extension.BusinessControllerBaseTest;

class DocumentoComplementarBCTest extends BusinessControllerBaseTest {
	
	@Mock
	private DocumentoComplementarDAO docCompleDAO;
	
	@Mock
    private ContratoDAO contratoDAO;
	
	@Mock
	private MedicaoDAO medicaoDAO;
	
	@Mock
	private DocumentoComplementarSubmetaDAO documentoComplementarSubmetaDAO;

	@Mock
	private ContratoResponsavelTecnicoDAO contratoResponsavelTecnicoDAO;

	@Mock
	private ContratoResponsavelTecnicoSocialDAO contratoResponsavelTecnicoSocialDAO;
	
	@Mock
	private ParalisacaoDAO paralisacaoDAO;
	
	@Mock
	private ContratosBC contratoBC;
	
	@InjectMocks
	private DocumentoComplementarBC docCompleBC;
	
    private final Long idDocumentoComplementar = 123L;
    private final Long idContrato = 771L;
    private final Long idContratoSiconv = 542L;
    private final Long idMedicao = 891L;
    private final Short nrSequencial = 1;
    private final String nrDocumento = "123";

	static Stream<Arguments> booleanIntAndListProvider() {
	    return Stream.of(
	    		Arguments.of(false, false, false, false, 1, Collections.EMPTY_LIST, 0),
	    		Arguments.of(false, false, false, true, 0, Collections.EMPTY_LIST, 0),
	    		Arguments.of(false, false, true, false, 0, Collections.EMPTY_LIST, 0),
	    		Arguments.of(false, false, true, true, 0, Collections.EMPTY_LIST, 0),
	    		Arguments.of(false, true, false, false, 0, Collections.EMPTY_LIST, 0),
	    		Arguments.of(false, true, false, true, 0, Collections.EMPTY_LIST, 0),
	    		Arguments.of(false, true, true, false, 0, Collections.EMPTY_LIST, 0),
	    		Arguments.of(false, true, true,  true, 0, Collections.EMPTY_LIST, 0),
	    		Arguments.of(true, false, false, false, 0, Collections.EMPTY_LIST, 0),
	    		Arguments.of(true, false, false, true, 0, Collections.EMPTY_LIST, 0),
	    		Arguments.of(true, false, true, false, 0, Collections.EMPTY_LIST, 0),
	    		Arguments.of(true, false, true, true, 0, Collections.EMPTY_LIST, 0),
	    		Arguments.of(true, true, false, false, 0, Collections.EMPTY_LIST, 0),
	    		Arguments.of(true, true, false, true, 0, Collections.EMPTY_LIST, 0),
	    		Arguments.of(true, true, true, false, 0, Collections.EMPTY_LIST, 0),
	    		Arguments.of(true, true, true,  true, 0, Collections.EMPTY_LIST, 0),
	    		
	    		Arguments.of(false, false, false, false, 1, List.of(SubmetaVrplDTOBuilder.newSubmetaMedicaoBuilder().create()), 1),
	    		Arguments.of(false, false, false, true, 0, List.of(SubmetaVrplDTOBuilder.newSubmetaMedicaoBuilder().create()), 1),
	    		Arguments.of(false, false, true, false, 0, List.of(SubmetaVrplDTOBuilder.newSubmetaMedicaoBuilder().create()), 1),
	    		Arguments.of(false, false, true, true, 0, List.of(SubmetaVrplDTOBuilder.newSubmetaMedicaoBuilder().create()), 1),
	    		Arguments.of(false, true, false, false, 0, List.of(SubmetaVrplDTOBuilder.newSubmetaMedicaoBuilder().create()), 1),
	    		Arguments.of(false, true, false, true, 0, List.of(SubmetaVrplDTOBuilder.newSubmetaMedicaoBuilder().create()), 1),
	    		Arguments.of(false, true, true, false, 0, List.of(SubmetaVrplDTOBuilder.newSubmetaMedicaoBuilder().create()), 1),
	    		Arguments.of(false, true, true, true, 0, List.of(SubmetaVrplDTOBuilder.newSubmetaMedicaoBuilder().create()), 1),
	    		Arguments.of(true, false, false, false, 0, List.of(SubmetaVrplDTOBuilder.newSubmetaMedicaoBuilder().create()), 1),
	    		Arguments.of(true, false, false, true, 0, List.of(SubmetaVrplDTOBuilder.newSubmetaMedicaoBuilder().create()), 1),
	    		Arguments.of(true, false, true, false, 0, List.of(SubmetaVrplDTOBuilder.newSubmetaMedicaoBuilder().create()), 1),
	    		Arguments.of(true, false, true, true, 0, List.of(SubmetaVrplDTOBuilder.newSubmetaMedicaoBuilder().create()), 1),
	    		Arguments.of(true, true, false, false, 0, List.of(SubmetaVrplDTOBuilder.newSubmetaMedicaoBuilder().create()), 1),
	    		Arguments.of(true, true, false, true, 0, List.of(SubmetaVrplDTOBuilder.newSubmetaMedicaoBuilder().create()), 1),
	    		Arguments.of(true, true, true, false, 0, List.of(SubmetaVrplDTOBuilder.newSubmetaMedicaoBuilder().create()), 1),
	    		Arguments.of(true, true, true, true, 0, List.of(SubmetaVrplDTOBuilder.newSubmetaMedicaoBuilder().create()), 1)
	    );
	}

	@BeforeEach
	void setup() throws Exception {

        setupDaoMock(DocumentoComplementarDAO.class, docCompleDAO);
        setupDaoMock(ContratoDAO.class, contratoDAO);
        setupDaoMock(MedicaoDAO.class, medicaoDAO);
        setupDaoMock(DocumentoComplementarSubmetaDAO.class, documentoComplementarSubmetaDAO);
        setupDaoMock(ContratoResponsavelTecnicoDAO.class, contratoResponsavelTecnicoDAO);
        setupDaoMock(ContratoResponsavelTecnicoSocialDAO.class, contratoResponsavelTecnicoSocialDAO);
        setupDaoMock(ParalisacaoDAO.class, paralisacaoDAO);
	}

	@Test
    void testExcluirDocumentoComplementarContrato_docInexistente() {

		Optional<DocumentoComplementarDTO> docComplementar = Optional.ofNullable(null);

        when(docCompleDAO.consultarDocumentoComplementar(idDocumentoComplementar)).thenReturn(docComplementar);

        assertThrowsMedicaoRestException(MessageKey.ERRO_DOCUMENTO_COMPLEMENTAR_INEXISTENTE,
                () -> docCompleBC.excluirDocumentoComplementarContrato(idDocumentoComplementar));
    }
    
	@Test
    void testExcluirDocumentoComplementarContrato_docBloqueado() {

		DocumentoComplementarDTO docComplementar = newDocumentoComplementar().setId(idDocumentoComplementar)
				.setBloqueado(true).create();
		Optional<DocumentoComplementarDTO> docComplementarOptional = Optional.of(docComplementar);


        when(docCompleDAO.consultarDocumentoComplementar(idDocumentoComplementar)).thenReturn(docComplementarOptional);

        assertThrowsMedicaoRestException(MessageKey.ERRO_DOCUMENTO_COMPLEMENTAR_BLOQUEADO,
                () -> docCompleBC.excluirDocumentoComplementarContrato(idDocumentoComplementar));
        
    }
	
	@Test
    void testExcluirDocumentoComplementarContrato_ordemServicoComMedicao() {

		DocumentoComplementarDTO docComplementar = newDocumentoComplementar().setId(idDocumentoComplementar)
				.setBloqueado(false).setTipoDocumento(TipoDocumentoEnum.OSE).setMedContratoFk(idContrato).create();
		Optional<DocumentoComplementarDTO> docComplementarOptional = Optional.of(docComplementar);


        when(docCompleDAO.consultarDocumentoComplementar(idDocumentoComplementar)).thenReturn(docComplementarOptional);

        ContratoBD contrato = newContratoMedicaoBuilder().setId(idContrato).setContratoSiconv(idContratoSiconv)
        		.create();
        when(contratoDAO.consultarContrato(idContrato)).thenReturn(contrato);

        MedicaoBD medicao = newMedicaoBuilder().setId(idMedicao).setMedContrato(idContrato)
                .setNrSequencial(nrSequencial).comSituacao(SituacaoMedicaoEnum.EM).setBloqueada(false).create();
        
        when(medicaoDAO.consultarUltimaMedicao(idContratoSiconv)).thenReturn(medicao);
        
        
        assertThrowsMedicaoRestException(MessageKey.ERRO_DOCUMENTO_COMPLEMENTAR_POSSUI_MEDICAO,
                () -> docCompleBC.excluirDocumentoComplementarContrato(idDocumentoComplementar));
        
    }	
	
	@Test
    void testExcluirDocumentoComplementarContrato_ordemServicoSemMedicao() {

		DocumentoComplementarDTO docComplementar = newDocumentoComplementar().setId(idDocumentoComplementar)
				.setBloqueado(false).setTipoDocumento(TipoDocumentoEnum.OSE).setMedContratoFk(idContrato).create();
		Optional<DocumentoComplementarDTO> docComplementarOptional = Optional.of(docComplementar);


        when(docCompleDAO.consultarDocumentoComplementar(idDocumentoComplementar)).thenReturn(docComplementarOptional);

        ContratoBD contrato = newContratoMedicaoBuilder().setId(idContrato).setContratoSiconv(idContratoSiconv)
        		.create();
        when(contratoDAO.consultarContrato(idContrato)).thenReturn(contrato);
        
        when(medicaoDAO.consultarUltimaMedicao(idContratoSiconv)).thenReturn(null);
                
        docCompleBC.excluirDocumentoComplementarContrato(idDocumentoComplementar);
        verify(docCompleDAO, times(1)).excluir(idDocumentoComplementar);
        
    }
	
	@Test
    void testExcluirDocumentoComplementarContrato_comSubmetas() {

		List<SubmetaVrplDTO> submetas = new ArrayList<>(); 
		submetas.add(newSubmetaMedicaoBuilder().create());
		
		DocumentoComplementarDTO docComplementar = newDocumentoComplementar().setId(idDocumentoComplementar)
				.setBloqueado(false).setTipoDocumento(TipoDocumentoEnum.OSE).setMedContratoFk(idContrato)
				.setSubmetas(submetas).create();
		Optional<DocumentoComplementarDTO> docComplementarOptional = Optional.of(docComplementar);


        when(docCompleDAO.consultarDocumentoComplementar(idDocumentoComplementar)).thenReturn(docComplementarOptional);

        ContratoBD contrato = newContratoMedicaoBuilder().setId(idContrato).setContratoSiconv(idContratoSiconv)
        		.create();
        when(contratoDAO.consultarContrato(idContrato)).thenReturn(contrato);
        
        when(medicaoDAO.consultarUltimaMedicao(idContratoSiconv)).thenReturn(null);
        
        docCompleBC.excluirDocumentoComplementarContrato(idDocumentoComplementar);        
        
        verify(documentoComplementarSubmetaDAO, times(1)).deletarPorIdDocumentoComplementar(idDocumentoComplementar);        
        verify(docCompleDAO, times(1)).excluir(idDocumentoComplementar);
        
    }
	
	@Test
    void testIncluirDocumentoComplementar_contratoInexistente() {		
		
		List<SubmetaVrplDTO> submetas = new ArrayList<>(); 
		submetas.add(newSubmetaMedicaoBuilder().create());
		
		DocumentoComplementarDTO docComplementar = newDocumentoComplementar().setId(idDocumentoComplementar)
				.setBloqueado(false).setTipoDocumento(TipoDocumentoEnum.OSE).setMedContratoFk(idContrato)
				.setSubmetas(submetas).create();
		
        when(contratoBC.consultarContratoPorId(idContratoSiconv)).thenReturn(null);

        assertThrowsMedicaoRestException(MessageKey.CONTRATO_INEXISTENTE,
                () -> docCompleBC.incluirDocumentoComplementar(idContratoSiconv, docComplementar));
    }
	
	@Test
    void testIncluirDocumentoComplementar_paramObrigatorioNaoInformado() {

		List<SubmetaVrplDTO> submetas = new ArrayList<>(); 
		submetas.add(newSubmetaMedicaoBuilder().create());
		 
		
		DocumentoComplementarDTO docComplementar = newDocumentoComplementar().setId(idDocumentoComplementar)
				.setBloqueado(false).setTipoDocumento(TipoDocumentoEnum.OSE).setMedContratoFk(idContrato)
				.setSubmetas(submetas).setNrDocumento(nrDocumento).create();

		StringBuilder parametrosNaoInformados = new StringBuilder();
		parametrosNaoInformados.append(DocumentoComplementarBC.ORGAO_EMISSOR);		
		parametrosNaoInformados.append(DocumentoComplementarBC.DT_EMISSAO);

		
		ContratoSiconvDTO contratoSiconv = newContratoDTOBuilder().setId(idContratoSiconv).create();
      
		when(contratoBC.consultarContratoPorId(idContratoSiconv)).thenReturn(contratoSiconv);
		
        assertThrowsMedicaoRestException(MessageKey.ERRO_PARAMETRO_OBRIGATORIO_NAO_INFORMADO,
        		List.of(parametrosNaoInformados.toString()),
        		() -> docCompleBC.incluirDocumentoComplementar(idContratoSiconv, docComplementar));
        
    }
	
	@Test
	void testIncluirDocumentoComplementar_ManifestoAmbientaTpOutrosParamObrigatorioNaoInformado() {

		DocumentoComplementarDTO docComplementar = newDocumentoComplementar().setId(idDocumentoComplementar)
				.setBloqueado(false).setTipoDocumento(TipoDocumentoEnum.MAM)
				.setTipoManifestoAmbiental(TipoManifestoEnum.OUT).setEquivaleALicencaInstalacao(true)
				.setMedContratoFk(idContrato).setNrDocumento(nrDocumento).create();

		StringBuilder parametrosNaoInformados = new StringBuilder();
		parametrosNaoInformados.append(DocumentoComplementarBC.ORGAO_EMISSOR);
		parametrosNaoInformados.append(DocumentoComplementarBC.DT_EMISSAO);
		parametrosNaoInformados.append(DocumentoComplementarBC.DT_VALIDADE);
		parametrosNaoInformados.append(DocumentoComplementarBC.DESC_TP_MANIF_OUTROS);
		parametrosNaoInformados.append(DocumentoComplementarBC.SUBMETA);

		ContratoSiconvDTO contratoSiconv = newContratoDTOBuilder().setId(idContratoSiconv).create();

		when(contratoBC.consultarContratoPorId(idContratoSiconv)).thenReturn(contratoSiconv);

		assertThrowsMedicaoRestException(MessageKey.ERRO_PARAMETRO_OBRIGATORIO_NAO_INFORMADO,
				List.of(parametrosNaoInformados.toString()),
				() -> docCompleBC.incluirDocumentoComplementar(idContratoSiconv, docComplementar));

	}
	
	@Test
	void testIncluirDocumentoComplementar_Outros_DtEmissaoFutura() {

		DocumentoComplementarDTO docComplementar = newDocumentoComplementar().setId(idDocumentoComplementar)
				.setBloqueado(false).setTipoDocumento(TipoDocumentoEnum.OUT)
				.setDtEmissao(LocalDate.now().plusDays(1))
				.setMedContratoFk(idContrato).create();

		StringBuilder parametrosNaoInformados = new StringBuilder();
		parametrosNaoInformados.append(TemporalUtil.formataDataPtBR(LocalDate.now()));
		

		ContratoSiconvDTO contratoSiconv = newContratoDTOBuilder().setId(idContratoSiconv).create();

		when(contratoBC.consultarContratoPorId(idContratoSiconv)).thenReturn(contratoSiconv);

		assertThrowsMedicaoRestException(MessageKey.ERRO_DOCUMENTO_COMPLEMENTAR_DATA_EMISSAO_FUTURA,
				List.of(parametrosNaoInformados.toString()),
				() -> docCompleBC.incluirDocumentoComplementar(idContratoSiconv, docComplementar));

	}
	
	@Test
	void testIncluirDocumentoComplementar_Outros_ParamObrigatorioNaoInformado() {

		DocumentoComplementarDTO docComplementar = newDocumentoComplementar().setId(idDocumentoComplementar)
				.setTipoDocumento(TipoDocumentoEnum.OUT)
				.setTipoManifestoAmbiental(TipoManifestoEnum.OUT)
				.setEquivaleALicencaInstalacao(true)
				.setMedContratoFk(idContrato).create();

		StringBuilder parametrosNaoInformados = new StringBuilder();
		parametrosNaoInformados.append(DocumentoComplementarBC.TX_DESCRICAO);
		

		ContratoSiconvDTO contratoSiconv = newContratoDTOBuilder().setId(idContratoSiconv).create();

		when(contratoBC.consultarContratoPorId(idContratoSiconv)).thenReturn(contratoSiconv);

		assertThrowsMedicaoRestException(MessageKey.ERRO_PARAMETRO_OBRIGATORIO_NAO_INFORMADO,
				List.of(parametrosNaoInformados.toString()),
				() -> docCompleBC.incluirDocumentoComplementar(idContratoSiconv, docComplementar));

	}
	
	@Test
	void testIncluirDocumentoComplementar_OrdemDeServiço_ParamObrigatorioNaoInformado() {

		DocumentoComplementarDTO docComplementar = newDocumentoComplementar().setId(idDocumentoComplementar)
				.setTipoDocumento(TipoDocumentoEnum.OSE)
				.setMedContratoFk(idContrato).create();

		StringBuilder parametrosNaoInformados = new StringBuilder();
		parametrosNaoInformados.append(DocumentoComplementarBC.NUMERO_DOC);
		parametrosNaoInformados.append(DocumentoComplementarBC.ORGAO_EMISSOR);
		parametrosNaoInformados.append(DocumentoComplementarBC.DT_EMISSAO);
		

		ContratoSiconvDTO contratoSiconv = newContratoDTOBuilder().setId(idContratoSiconv).create();

		when(contratoBC.consultarContratoPorId(idContratoSiconv)).thenReturn(contratoSiconv);

		assertThrowsMedicaoRestException(MessageKey.ERRO_PARAMETRO_OBRIGATORIO_NAO_INFORMADO,
				List.of(parametrosNaoInformados.toString()),
				() -> docCompleBC.incluirDocumentoComplementar(idContratoSiconv, docComplementar));

	}
	
	//Autorização, Declaração ou Outorga possuem os mesmos parâmetros obrigratórios
	@Test
	void testIncluirDocumentoComplementar_Autorização_ParamObrigatorioNaoInformado() {

		DocumentoComplementarDTO docComplementar = newDocumentoComplementar().setId(idDocumentoComplementar)
				.setTipoDocumento(TipoDocumentoEnum.AUT)
				.setMedContratoFk(idContrato).create();

		StringBuilder parametrosNaoInformados = new StringBuilder();
		parametrosNaoInformados.append(DocumentoComplementarBC.NUMERO_DOC);
		parametrosNaoInformados.append(DocumentoComplementarBC.ORGAO_EMISSOR);
		parametrosNaoInformados.append(DocumentoComplementarBC.DT_EMISSAO);
		parametrosNaoInformados.append(DocumentoComplementarBC.SUBMETA);
		

		ContratoSiconvDTO contratoSiconv = newContratoDTOBuilder().setId(idContratoSiconv).create();

		when(contratoBC.consultarContratoPorId(idContratoSiconv)).thenReturn(contratoSiconv);

		assertThrowsMedicaoRestException(MessageKey.ERRO_PARAMETRO_OBRIGATORIO_NAO_INFORMADO,
				List.of(parametrosNaoInformados.toString()),
				() -> docCompleBC.incluirDocumentoComplementar(idContratoSiconv, docComplementar));

	}

	
	@Test
	void testConsultarDocumentoComplementar_docInexistente() {		
		
		Optional<DocumentoComplementarDTO> docComplementar = Optional.ofNullable(null);
		
		when(docCompleDAO.consultarDocumentoComplementar(idDocumentoComplementar)).thenReturn(docComplementar);
				
		assertThrowsMedicaoRestException(MessageKey.ERRO_DOCUMENTO_COMPLEMENTAR_INEXISTENTE,
				() -> docCompleBC.consultarDocumentoComplementar(idDocumentoComplementar));
		
	}
	
	@ParameterizedTest
	@MethodSource("booleanIntAndListProvider")
	void teste_excluirDocComplementar_excluirEstrutura (boolean existeRT, boolean existeRTSocial, boolean existeDocComlpementar, boolean existeParalisacao, int excluirEstruturaTimes, List<SubmetaVrplDTO> listaSubmetas, int deletarSubmetaTimes) {
		
		Long idDocComplementar = 2L;
		Long idContrato = 1L;
		Long idContratoVRPL = 1000L;
		
		
		DocumentoComplementarDTOBuilder builder = DocumentoComplementarDTOBuilder.newDocumentoComplementar();
		builder.setId(idDocComplementar).
		desBloquearDocumento().
		setTipoDocumento(TipoDocumentoEnum.AUT).
		setMedContratoFk(idContrato).
		setSubmetas(listaSubmetas).
		setIdContratoSiconv(idContratoVRPL);
		
		Optional<DocumentoComplementarDTO> optDocComplementar = Optional.of(builder.create());

		DocumentoComplementarDTO docCompl = optDocComplementar.get();
		
		ContratoMedicaoBuilder contratoBuilder = ContratoMedicaoBuilder.newContratoMedicaoBuilder();
		contratoBuilder.setId(idContrato).setContratoFk (idContratoVRPL);
		
		ContratoBD contratoBD = contratoBuilder.create();
		
		doReturn(optDocComplementar).when (docCompleDAO).consultarDocumentoComplementar(docCompl.getId());
		doReturn(contratoBD).when (contratoDAO).consultarContrato(docCompl.getMedContratoFk());
		doReturn(null).when (medicaoDAO).consultarUltimaMedicao(contratoBD.getContratoFk());
		doReturn(existeRT).when (contratoResponsavelTecnicoDAO).existeRespTecnicoContrato(docCompl.getMedContratoFk());
		doReturn(existeRTSocial).when (contratoResponsavelTecnicoSocialDAO).existeRespTecnicoSocialContrato(docCompl.getMedContratoFk());
		doReturn(existeParalisacao).when (paralisacaoDAO).existeParalisacaoContrato(docCompl.getMedContratoFk());
		doReturn(existeDocComlpementar).when (docCompleDAO).existeDocumentoComplementarContrato(docCompl.getMedContratoFk());

		docCompleBC.excluirDocumentoComplementarContrato(docCompl.getId());
		verify(documentoComplementarSubmetaDAO, times(deletarSubmetaTimes)).deletarPorIdDocumentoComplementar(docCompl.getId());
		verify(docCompleDAO, times(1)).excluir(docCompl.getId());
		verify(contratoBC,times(excluirEstruturaTimes)).excluirEstruturaContrato(docCompl.getMedContratoFk(), handle);
	}
	
	@Test
	void testSetarBloqueioDocumentoComplementar() {

		docCompleBC.setarBloqueioDocumentoComplementar(idDocumentoComplementar, true);

		verify(docCompleDAO, times(1)).setarBloqueioDocumentoComplementar(idDocumentoComplementar, true);

	}
	
	@Test
	void testeBloquearDesbloquearDocumentoComplementar() {
		
		//Desbloquear
		docCompleBC.bloquearDocumentosComplementares(handle, idContrato);
		verify(docCompleDAO, times(1)).listarIdDocumentoComplementar(idContrato, false);
		
		//Bloquear
		docCompleBC.desbloquearDocumentosComplementares(handle, idContrato);
		verify(docCompleDAO, times(1)).listarIdDocumentoComplementar(idContrato, true);

	}
	
	

}
