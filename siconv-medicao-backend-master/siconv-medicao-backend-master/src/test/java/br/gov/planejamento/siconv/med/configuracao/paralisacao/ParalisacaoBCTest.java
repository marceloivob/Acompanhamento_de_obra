package br.gov.planejamento.siconv.med.configuracao.paralisacao;

import static br.gov.planejamento.siconv.med.test.builder.ContratoMedicaoBuilder.newContratoMedicaoBuilder;
import static br.gov.planejamento.siconv.med.test.builder.ContratoSiconvBuilder.newContratoDTOBuilder;
import static br.gov.planejamento.siconv.med.test.builder.ParalisacaoBDBuilder.newParalisacaoBD;
import static br.gov.planejamento.siconv.med.test.builder.ParalisacaoDTOBuilder.newParalisacao;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.dao.DocumentoComplementarDAO;
import br.gov.planejamento.siconv.med.configuracao.paralisacao.business.ParalisacaoBC;
import br.gov.planejamento.siconv.med.configuracao.paralisacao.dao.AnexoParalisacaoDAO;
import br.gov.planejamento.siconv.med.configuracao.paralisacao.dao.ParalisacaoDAO;
import br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.database.ParalisacaoBD;
import br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.dto.AnexoParalisacaoDTO;
import br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.dto.ParalisacaoDTO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao.ContratoResponsavelTecnicoDAO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao.ContratoResponsavelTecnicoSocialDAO;
import br.gov.planejamento.siconv.med.contrato.business.ContratosBC;
import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.contrato.entity.dto.ContratoSiconvDTO;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.infra.security.UsuarioLogado;
import br.gov.planejamento.siconv.med.integration.ceph.CephActions;
import br.gov.planejamento.siconv.med.test.extension.BusinessControllerBaseTest;

class ParalisacaoBCTest extends BusinessControllerBaseTest {

	final UsuarioLogado usuarioLogado = mock(UsuarioLogado.class);

	@Mock
	private ParalisacaoDAO paralisacaoDAO;
	
	@Mock
	private AnexoParalisacaoDAO anexoParalisacaoDAO;
	
	@Mock
	private DocumentoComplementarDAO documentoComplementarDAO;

	@Mock
	private ContratoResponsavelTecnicoDAO contratoResponsavelTecnicoDAO;

	@Mock
	private ContratoResponsavelTecnicoSocialDAO contratoResponsavelTecnicoSocialDAO;

	@InjectMocks
	private ParalisacaoBC paralisacaoBC;
	
	@Mock
	private ContratosBC contratoBC;
	
	@Mock
    private CephActions cephActions;

	private final Long idParalisacao = 321L;
	private final Long idUltimaParalisacao = 123L;
	private final Long idParalisacaoAnterior = 231L;
	private final Long idContratoSiconv = 4321L;
	private final Long idMedContrato = 222L;

	static Stream<Arguments> booleanIntAndListProvider() {
	    return Stream.of(
	    		Arguments.of(false, false, false, false, 1),
	    		Arguments.of(true, false, true, true, 0),
	    		Arguments.of(false, false, true, true, 0),
	    		Arguments.of(false, false, false, true, 0),
	    		Arguments.of(false, false, true, false, 0),
	    		
	    		Arguments.of(false, true, true, true, 0),
	    		Arguments.of(false, false, true, true, 0),
	    		Arguments.of(false, false, false, true, 0),
	    		Arguments.of(false, false, true, false, 0)
	    );
	}
	
	@BeforeEach
	void setup() throws Exception {
		setupDaoMock(ParalisacaoDAO.class, paralisacaoDAO);
		setupDaoMock(AnexoParalisacaoDAO.class, anexoParalisacaoDAO);
        setupDaoMock(DocumentoComplementarDAO.class, documentoComplementarDAO);
        setupDaoMock(ContratoResponsavelTecnicoDAO.class, contratoResponsavelTecnicoDAO);
        setupDaoMock(ContratoResponsavelTecnicoSocialDAO.class, contratoResponsavelTecnicoSocialDAO);
	}

	@Test
	void testExcluirParalisacao_inexistente() {

		Optional<ParalisacaoDTO> paralisacao = Optional.ofNullable(null);

		when(paralisacaoDAO.consultarParalisacao(idParalisacao)).thenReturn(paralisacao);

		assertThrowsMedicaoRestException(MessageKey.ERRO_PARALISACAO_OBRA_INEXISTENTE,
				() -> paralisacaoBC.excluirParalisacao(idParalisacao));
	}

	@Test
	void testExcluirParalisacao_diferenteDeUltima() {

		Optional<ParalisacaoDTO> paralisacaoOptional = 
				Optional.of(newParalisacao().setId(idParalisacao)
						.setIdContratoSiconv(idContratoSiconv).create());
		
		when(paralisacaoDAO.consultarParalisacao(idParalisacao)).thenReturn(paralisacaoOptional);
		when(paralisacaoDAO.consultarUltimaParalisacao(idContratoSiconv)).thenReturn(null);

		assertThrowsMedicaoRestException(MessageKey.ERRO_MANTER_PARALISACAO,
				() -> paralisacaoBC.excluirParalisacao(idParalisacao));
	}
	
	@ParameterizedTest
	@MethodSource("booleanIntAndListProvider")
	void testExcluirParalisacao_configuracaoContrato(boolean existeRT, boolean existeRTSocial, 
			boolean existeDocComlpementar, boolean existeParalisacao,
			int excluirEstruturaTimes) {

		Optional<ParalisacaoDTO> paralisacaoOptional = 
				Optional.of(newParalisacao().setId(idParalisacao)
						.setIdContratoSiconv(idContratoSiconv)
						.setMedContratoFk(idMedContrato)
						.create());
		
		ParalisacaoDTO paralisacao = paralisacaoOptional.get();
		
		when(paralisacaoDAO.consultarParalisacao(idParalisacao)).thenReturn(paralisacaoOptional);

		ParalisacaoBD ultimaParalisacao = newParalisacaoBD().setId(idParalisacao).create();
		
		when(paralisacaoDAO.consultarUltimaParalisacao(idContratoSiconv)).thenReturn(ultimaParalisacao);
		doReturn(existeRT).when (contratoResponsavelTecnicoDAO).existeRespTecnicoContrato(paralisacao.getMedContratoFk());
		doReturn(existeRTSocial).when (contratoResponsavelTecnicoSocialDAO).existeRespTecnicoSocialContrato(paralisacao.getMedContratoFk());
		doReturn(existeParalisacao).when (paralisacaoDAO).existeParalisacaoContrato(paralisacao.getMedContratoFk());
		doReturn(existeDocComlpementar).when(documentoComplementarDAO).existeDocumentoComplementarContrato(paralisacao.getMedContratoFk());

		paralisacaoBC.excluirParalisacao(idParalisacao);
        verify(paralisacaoDAO, times(1)).excluirParalisacaoPorId(idParalisacao);
		verify(contratoBC,times(excluirEstruturaTimes)).excluirEstruturaContrato(paralisacao.getMedContratoFk(), handle);
	}
	
	@Test
    void testIncluirParalisacao_paralisacaoEmAberto() {		
		
		ParalisacaoDTO paralisacao = newParalisacao()
						.setIdContratoSiconv(idContratoSiconv).create();
		
		when(paralisacaoDAO.existeParalisacaoEmAberto(idContratoSiconv)).thenReturn(true);

        assertThrowsMedicaoRestException(MessageKey.ERRO_INCLUSAO_EXISTE_PARALISACAO_EM_ABERTO,
                () -> paralisacaoBC.incluirParalisacao(idContratoSiconv, paralisacao));
    }
	
	@Test
    void testIncluirParalisacao_paralisacaoDtInicioAnteriorDtFim() {		
		
		ParalisacaoDTO paralisacao = newParalisacao()
						.setId(idParalisacao)
						.setIdContratoSiconv(idContratoSiconv)
						.setDataInicio(LocalDate.now().plusDays(1))
						.setDataFim(LocalDate.now()).create();

		ParalisacaoBD ultimaParalisacao = newParalisacaoBD().setId(idUltimaParalisacao).create();

		when(paralisacaoDAO.existeParalisacaoEmAberto(idContratoSiconv)).thenReturn(false);
		when(paralisacaoDAO.consultarUltimaParalisacao(idContratoSiconv)).thenReturn(ultimaParalisacao);

        assertThrowsMedicaoRestException(MessageKey.ERRO_DATA_INICIO_PARALISACAO_POSTERIOR_DATA_FIM,
                () -> paralisacaoBC.incluirParalisacao(idContratoSiconv, paralisacao));
    }
	
	@Test
    void testIncluirParalisacao_paralisacaoDtInicioPosteriorDtFimUltimaParalisacao() {		
		
		ParalisacaoDTO paralisacao = newParalisacao()
						.setId(idParalisacao)
						.setIdContratoSiconv(idContratoSiconv)
						.setDataInicio(LocalDate.now()).create();

		ParalisacaoBD ultimaParalisacao = newParalisacaoBD().setId(idUltimaParalisacao)
				.setDtFim(LocalDate.now().plusDays(1)).create();

		when(paralisacaoDAO.existeParalisacaoEmAberto(idContratoSiconv)).thenReturn(false);
		when(paralisacaoDAO.consultarUltimaParalisacao(idContratoSiconv)).thenReturn(ultimaParalisacao);

	   assertThrowsMedicaoRestException(MessageKey.ERRO_DATA_INICIO_ANTERIOR_DATA_FIM_ULTIMA_PARALISACAO,
                () -> paralisacaoBC.incluirParalisacao(idContratoSiconv, paralisacao));
	}
	
	@Test
    void testIncluirParalisacao_estruturaMedContratoExistente_comAnexo() {		
		
		Long idAnexo = 1L;
		AnexoParalisacaoDTO anexoDTO = new AnexoParalisacaoDTO();
		anexoDTO.setId(idAnexo);
		anexoDTO.setCoCeph("caminhoCoCeph");
		
		List<AnexoParalisacaoDTO> anexos = new ArrayList<AnexoParalisacaoDTO>();
		anexos.add(anexoDTO);
		
		ParalisacaoDTO paralisacao = newParalisacao()
						.setId(idParalisacao)
						.setIdContratoSiconv(idContratoSiconv)
						.setDataInicio(LocalDate.now())
						.setDataFim(LocalDate.now().plusDays(1))
						.setAnexos(anexos)
						.create();

		ParalisacaoBD ultimaParalisacao = newParalisacaoBD().setId(idUltimaParalisacao)
				.setDtFim(LocalDate.now().minusDays(1)).create();
		
		ContratoBD contratoBD = newContratoMedicaoBuilder().setId(idMedContrato).create();

		when(paralisacaoDAO.existeParalisacaoEmAberto(idContratoSiconv)).thenReturn(false);
		when(paralisacaoDAO.consultarUltimaParalisacao(idContratoSiconv)).thenReturn(ultimaParalisacao);
		when(contratoBC.consultarContratoMedicaoPorContratoFK(idContratoSiconv)).thenReturn(contratoBD);
		when(cephActions.getPresignedUrl(anexoDTO.getCoCeph())).thenReturn("url");
		
        paralisacaoBC.incluirParalisacao(idContratoSiconv, paralisacao);
        verify(paralisacaoDAO, times(1)).inserirParalisacao(paralisacao.converterParaBD());
    }
	
	@Test
    void testIncluirParalisacao_estruturaMedContratoInexistente() {		
				
		ParalisacaoDTO paralisacao = newParalisacao()
						.setId(idParalisacao)
						.setIdContratoSiconv(idContratoSiconv)
						.setDataInicio(LocalDate.now())
						.setDataFim(LocalDate.now().plusDays(1))
						.create();

		ParalisacaoBD ultimaParalisacao = newParalisacaoBD().setId(idUltimaParalisacao)
				.setDtFim(LocalDate.now().minusDays(1)).create();
		
		ContratoSiconvDTO contratoSiconv = newContratoDTOBuilder().setId(idContratoSiconv).create();

		ContratoBD contratoBD = newContratoMedicaoBuilder().setId(idMedContrato).create();

		when(paralisacaoDAO.existeParalisacaoEmAberto(idContratoSiconv)).thenReturn(false);
		when(paralisacaoDAO.consultarUltimaParalisacao(idContratoSiconv)).thenReturn(ultimaParalisacao);
		when(contratoBC.consultarContratoMedicaoPorContratoFK(idContratoSiconv)).thenReturn(null);
		when(contratoBC.consultarContratoPorId(idContratoSiconv)).thenReturn(contratoSiconv);
		when(contratoBC.incluir(contratoSiconv, handle)).thenReturn(contratoBD);
		
        paralisacaoBC.incluirParalisacao(idContratoSiconv, paralisacao);
        verify(paralisacaoDAO, times(1)).inserirParalisacao(paralisacao.converterParaBD());
    }
	
	@Test
	void testAlterarParalisacao_diferenteDeUltima() {

		Optional<ParalisacaoDTO> paralisacaoOptional = 
				Optional.of(newParalisacao().setId(idParalisacao)
						.setIdContratoSiconv(idContratoSiconv).create());
		
		ParalisacaoBD ultimaParalisacao = newParalisacaoBD().setId(idUltimaParalisacao).create();

		when(paralisacaoDAO.consultarParalisacao(idParalisacao)).thenReturn(paralisacaoOptional);
		when(paralisacaoDAO.consultarUltimaParalisacao(idContratoSiconv)).thenReturn(ultimaParalisacao);

		assertThrowsMedicaoRestException(MessageKey.ERRO_MANTER_PARALISACAO,
				() -> paralisacaoBC.alterarParalisacao(paralisacaoOptional.get()));
	}
	
	@Test
    void testAlterarParalisacao_paralisacaoDtInicioAnteriorDtFim() {		
		
		Optional<ParalisacaoDTO> paralisacaoOptional = 
				Optional.of(newParalisacao()
						.setId(idParalisacao)
						.setIdContratoSiconv(idContratoSiconv)
						.setDataInicio(LocalDate.now().plusDays(1))
						.setDataFim(LocalDate.now()).create());
		
		ParalisacaoDTO paralisacao = paralisacaoOptional.get();

		ParalisacaoBD paralisacaoAnterior = newParalisacaoBD().setId(idUltimaParalisacao).create();

		when(paralisacaoDAO.consultarParalisacao(idParalisacao)).thenReturn(paralisacaoOptional);
		when(paralisacaoDAO.consultarUltimaParalisacao(idContratoSiconv)).thenReturn(paralisacao.converterParaBD());
		when(paralisacaoDAO.consultarParalisacaoAnterior(idMedContrato,paralisacaoAnterior.getDtInicio())).thenReturn(paralisacaoAnterior);

        assertThrowsMedicaoRestException(MessageKey.ERRO_DATA_INICIO_PARALISACAO_POSTERIOR_DATA_FIM,
                () -> paralisacaoBC.alterarParalisacao(paralisacao));
    }
	
	@Test
    void testAlterarParalisacao_paralisacaoDtInicioPosteriorDtFimUltimaParalisacao() {		
		
		Optional<ParalisacaoDTO> paralisacaoOptional = 
				Optional.of(newParalisacao()
						.setId(idParalisacao)
						.setMedContratoFk(idMedContrato)
						.setIdContratoSiconv(idContratoSiconv)
						.setDataInicio(LocalDate.now())
						.setDataFim(LocalDate.now().plusDays(1)).create());
		
		ParalisacaoDTO paralisacao = paralisacaoOptional.get();

		ParalisacaoBD paralisacaoAnterior = newParalisacaoBD()
				.setId(idParalisacaoAnterior)
				.setMedContratoFk(idMedContrato)
				.setDtInicio(LocalDate.now().minusDays(5))
				.setDtFim(LocalDate.now().plusDays(1)).create();
		
		when(paralisacaoDAO.consultarParalisacao(idParalisacao)).thenReturn(paralisacaoOptional);
		when(paralisacaoDAO.consultarUltimaParalisacao(idContratoSiconv)).thenReturn(paralisacao.converterParaBD());
		when(paralisacaoDAO.consultarParalisacaoAnterior(idMedContrato, paralisacao.getDataInicio())).thenReturn(paralisacaoAnterior);

        assertThrowsMedicaoRestException(MessageKey.ERRO_DATA_INICIO_ANTERIOR_DATA_FIM_ULTIMA_PARALISACAO,
                () -> paralisacaoBC.alterarParalisacao(paralisacao));
    }
	
	@Test
    void testAlterarParalisacao_paralisacaoDtInicioIgualDtFimUltimaParalisacao() {		
		
		Optional<ParalisacaoDTO> paralisacaoOptional = 
				Optional.of(newParalisacao()
						.setId(idParalisacao)
						.setMedContratoFk(idMedContrato)
						.setIdContratoSiconv(idContratoSiconv)
						.setDataInicio(LocalDate.now())
						.setDataFim(LocalDate.now().plusDays(1)).create());
		
		ParalisacaoDTO paralisacao = paralisacaoOptional.get();

		ParalisacaoBD paralisacaoAnterior = newParalisacaoBD()
				.setId(idParalisacaoAnterior)
				.setMedContratoFk(idMedContrato)
				.setDtInicio(LocalDate.now().minusDays(5))
				.setDtFim(LocalDate.now()).create();
		
		when(paralisacaoDAO.consultarParalisacao(idParalisacao)).thenReturn(paralisacaoOptional);
		when(paralisacaoDAO.consultarUltimaParalisacao(idContratoSiconv)).thenReturn(paralisacao.converterParaBD());
		when(paralisacaoDAO.consultarParalisacaoAnterior(idMedContrato, paralisacao.getDataInicio())).thenReturn(paralisacaoAnterior);

        assertThrowsMedicaoRestException(MessageKey.ERRO_DATA_INICIO_ANTERIOR_DATA_FIM_ULTIMA_PARALISACAO,
                () -> paralisacaoBC.alterarParalisacao(paralisacao));
    }
	
	@Test
    void testAlterarParalisacao_semAnexoSemParalisacaoAnterior() {
		
		Optional<ParalisacaoDTO> paralisacaoOptional = 
				Optional.of(newParalisacao()
						.setId(idParalisacao)
						.setMedContratoFk(idMedContrato)
						.setIdContratoSiconv(idContratoSiconv)
						.setDataInicio(LocalDate.now().plusDays(3)).create());
		
		ParalisacaoDTO paralisacao = paralisacaoOptional.get();
		
		when(paralisacaoDAO.consultarParalisacao(idParalisacao)).thenReturn(paralisacaoOptional);
		when(paralisacaoDAO.consultarUltimaParalisacao(idContratoSiconv)).thenReturn(paralisacao.converterParaBD());
		when(paralisacaoDAO.consultarParalisacaoAnterior(idMedContrato, paralisacao.getDataInicio())).thenReturn(null);

        paralisacaoBC.alterarParalisacao(paralisacao);
        verify(paralisacaoDAO, times(1)).alterarParalisacao(paralisacao.converterParaBD());
    }
	
	@Test
    void testAlterarParalisacao_semAnexoSemParalisacaoAnteriorMesmoId() {
		
		Optional<ParalisacaoDTO> paralisacaoOptional = 
				Optional.of(newParalisacao()
						.setId(idParalisacao)
						.setMedContratoFk(idMedContrato)
						.setIdContratoSiconv(idContratoSiconv)
						.setDataInicio(LocalDate.now().plusDays(3)).create());
		
		ParalisacaoDTO paralisacao = paralisacaoOptional.get();
		
		ParalisacaoBD paralisacaoAnterior = newParalisacaoBD()
				.setId(idParalisacao)
				.setMedContratoFk(idMedContrato)
				.setDtInicio(LocalDate.now().minusDays(5))
				.setDtFim(LocalDate.now()).create();
		
		when(paralisacaoDAO.consultarParalisacao(idParalisacao)).thenReturn(paralisacaoOptional);
		when(paralisacaoDAO.consultarUltimaParalisacao(idContratoSiconv)).thenReturn(paralisacao.converterParaBD());
		when(paralisacaoDAO.consultarParalisacaoAnterior(idMedContrato, paralisacao.getDataInicio())).thenReturn(paralisacaoAnterior);

        paralisacaoBC.alterarParalisacao(paralisacao);
        verify(paralisacaoDAO, times(1)).alterarParalisacao(paralisacao.converterParaBD());
    }
	
	@Test
    void testAlterarParalisacao_semAnexo() {
		
		Optional<ParalisacaoDTO> paralisacaoOptional = 
				Optional.of(newParalisacao()
						.setId(idParalisacao)
						.setMedContratoFk(idMedContrato)
						.setIdContratoSiconv(idContratoSiconv)
						.setDataInicio(LocalDate.now().plusDays(3)).create());
		
		ParalisacaoDTO paralisacao = paralisacaoOptional.get();

		ParalisacaoBD paralisacaoAnterior = newParalisacaoBD()
				.setId(idParalisacaoAnterior)
				.setMedContratoFk(idMedContrato)
				.setDtInicio(LocalDate.now().minusDays(5))
				.setDtFim(LocalDate.now()).create();
		
		when(paralisacaoDAO.consultarParalisacao(idParalisacao)).thenReturn(paralisacaoOptional);
		when(paralisacaoDAO.consultarUltimaParalisacao(idContratoSiconv)).thenReturn(paralisacao.converterParaBD());
		when(paralisacaoDAO.consultarParalisacaoAnterior(idMedContrato, paralisacao.getDataInicio())).thenReturn(paralisacaoAnterior);

        paralisacaoBC.alterarParalisacao(paralisacao);
        verify(paralisacaoDAO, times(1)).alterarParalisacao(paralisacao.converterParaBD());
    }
	
	@Test
    void testAlterarParalisacao_comAnexoMasSemAlteracao() {
		
		Long idAnexo = 1L;
		
		AnexoParalisacaoDTO anexoDTO = new AnexoParalisacaoDTO();
		anexoDTO.setId(idAnexo);
		anexoDTO.setCoCeph("caminhoCoCeph");
		
		AnexoParalisacaoDTO anexoNovoDTO = new AnexoParalisacaoDTO();
		anexoNovoDTO.setCoCeph("caminhoCoCeph");
		
		List<AnexoParalisacaoDTO> anexos = new ArrayList<AnexoParalisacaoDTO>();
		anexos.add(anexoDTO);
		anexos.add(anexoNovoDTO);
		
		Optional<ParalisacaoDTO> paralisacaoOptional = 
				Optional.of(newParalisacao()
						.setId(idParalisacao)
						.setMedContratoFk(idMedContrato)
						.setIdContratoSiconv(idContratoSiconv)
						.setAnexos(anexos)
						.setDataInicio(LocalDate.now().plusDays(3)).create());
		
		ParalisacaoDTO paralisacao = paralisacaoOptional.get();

		ParalisacaoBD paralisacaoAnterior = newParalisacaoBD()
				.setId(idParalisacaoAnterior)
				.setMedContratoFk(idMedContrato)
				.setDtInicio(LocalDate.now().minusDays(5))
				.setDtFim(LocalDate.now()).create();
		
		when(paralisacaoDAO.consultarParalisacao(idParalisacao)).thenReturn(paralisacaoOptional);
		when(paralisacaoDAO.consultarUltimaParalisacao(idContratoSiconv)).thenReturn(paralisacao.converterParaBD());
		when(paralisacaoDAO.consultarParalisacaoAnterior(idMedContrato, paralisacao.getDataInicio())).thenReturn(paralisacaoAnterior);
		when(anexoParalisacaoDAO.buscarIdAnexoPorIdParalisacao(idParalisacao)).thenReturn(Set.of());

        paralisacaoBC.alterarParalisacao(paralisacao);
        verify(paralisacaoDAO, times(1)).alterarParalisacao(paralisacao.converterParaBD());
    }
	
	@Test
    void testAlterarParalisacao() {
		
		Long idAnexo = 1L;
		Long idAnexoExcluido = 2L;
		
		AnexoParalisacaoDTO anexoDTO = new AnexoParalisacaoDTO();
		anexoDTO.setId(idAnexo);
		anexoDTO.setCoCeph("caminhoCoCeph");
		
		AnexoParalisacaoDTO anexoNovoDTO = new AnexoParalisacaoDTO();
		anexoDTO.setId(null);
		anexoNovoDTO.setCoCeph("caminhoCoCeph");
		
		List<AnexoParalisacaoDTO> anexos = new ArrayList<AnexoParalisacaoDTO>();
		anexos.add(anexoDTO);
		anexos.add(anexoNovoDTO);
		
		Optional<ParalisacaoDTO> paralisacaoOptional = 
				Optional.of(newParalisacao()
						.setId(idParalisacao)
						.setMedContratoFk(idMedContrato)
						.setIdContratoSiconv(idContratoSiconv)
						.setAnexos(anexos)
						.setDataInicio(LocalDate.now().plusDays(3)).create());
		
		ParalisacaoDTO paralisacao = paralisacaoOptional.get();

		ParalisacaoBD paralisacaoAnterior = newParalisacaoBD()
				.setId(idParalisacaoAnterior)
				.setMedContratoFk(idMedContrato)
				.setDtInicio(LocalDate.now().minusDays(5))
				.setDtFim(LocalDate.now()).create();
		
		when(paralisacaoDAO.consultarParalisacao(idParalisacao)).thenReturn(paralisacaoOptional);
		when(paralisacaoDAO.consultarUltimaParalisacao(idContratoSiconv)).thenReturn(paralisacao.converterParaBD());
		when(paralisacaoDAO.consultarParalisacaoAnterior(idMedContrato, paralisacao.getDataInicio())).thenReturn(paralisacaoAnterior);
		when(anexoParalisacaoDAO.buscarIdAnexoPorIdParalisacao(idParalisacao)).thenReturn(Set.of(idAnexo, idAnexoExcluido));

        paralisacaoBC.alterarParalisacao(paralisacao);
        verify(paralisacaoDAO, times(1)).alterarParalisacao(paralisacao.converterParaBD());
    }

}
