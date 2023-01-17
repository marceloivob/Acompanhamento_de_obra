package br.gov.planejamento.siconv.med.configuracao.documentocomplementar.business;

import static br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.TipoManifestoEnum.LIN;
import static br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.TipoManifestoEnum.LOP;
import static br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.TipoManifestoEnum.LPR;
import static br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.TipoManifestoEnum.OUT;
import static com.google.common.collect.Sets.difference;
import static java.util.Collections.sort;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.groups.ConvertGroup;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.dao.DocumentoComplementarDAO;
import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.dao.DocumentoComplementarSubmetaDAO;
import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.database.DocumentoComplementarSubmetaBD;
import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.DocumentoComplementarDTO;
import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.TipoDocumentoEnum;
import br.gov.planejamento.siconv.med.configuracao.paralisacao.dao.ParalisacaoDAO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao.ContratoResponsavelTecnicoDAO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao.ContratoResponsavelTecnicoSocialDAO;
import br.gov.planejamento.siconv.med.contrato.business.ContratosBC;
import br.gov.planejamento.siconv.med.contrato.business.ItemMedicaoBC;
import br.gov.planejamento.siconv.med.contrato.dao.ContratoDAO;
import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.contrato.entity.dto.ContratoSiconvDTO;
import br.gov.planejamento.siconv.med.infra.exception.MedicaoRestException;
import br.gov.planejamento.siconv.med.infra.message.Message;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.infra.util.TemporalUtil;
import br.gov.planejamento.siconv.med.infra.validation.InsertGroup;
import br.gov.planejamento.siconv.med.infra.validation.UpdateGroup;
import br.gov.planejamento.siconv.med.integration.ceph.CephActions;
import br.gov.planejamento.siconv.med.medicao.business.SubmetaBC;
import br.gov.planejamento.siconv.med.medicao.dao.MedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.entity.database.MedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.dto.EventoFrenteObraTotalizadoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoFrenteObraTotalizadoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;

@ApplicationScoped
public class DocumentoComplementarBC {

	@Inject
	private Jdbi jdbi;
	
	@Inject
	private CephActions cephActions;
	
	@Inject
	private ContratosBC contratoBC;
	
	@Inject
	private SubmetaBC submetaBC;

    @Inject
    private SecurityContext securityContext;
    
	@Inject
	private ItemMedicaoBC itemMedicaoBC;
	
	
	public static final String MANIFESTO_AMBIENTAL = " Manifesto Ambiental ";
	public static final String NUMERO_DOC = " Número do Documento "; 
	public static final String ORGAO_EMISSOR = " Órgão Emissor ";
	public static final String DT_EMISSAO = " Data de Emissão ";
	public static final String DT_VALIDADE = " Data de Validade ";
	public static final String DESC_TP_MANIF_OUTROS = " Descrição Tipo Manifesto Outros";
	public static final String SUBMETA = " Submeta ";
	public static final String TX_DESCRICAO = " Descrição ";
	 

	public Long incluirDocumentoComplementar(Long idContratoSiconv,
			@Valid @ConvertGroup(to = InsertGroup.class) DocumentoComplementarDTO documentoInput) {

		ContratoSiconvDTO contratoSiconv = this.validarParametros(documentoInput, idContratoSiconv);

		final boolean configurarMedicao;

		ContratoBD contratoMedicao = getContratoDAO().consultarContratoPorContratoFK(idContratoSiconv);

		if (Optional.ofNullable(contratoMedicao).isPresent()) {

			configurarMedicao = false;
			documentoInput.setMedContratoFk(contratoMedicao.getId());
			if (documentoInput.getSubmetas() != null && !documentoInput.getSubmetas().isEmpty()) {
				validarSubmetasDocumentoVinculadasContrato(documentoInput, contratoMedicao);
			}

		} else {
			configurarMedicao = true;
		}

		return jdbi.inTransaction(transaction -> {

			DocumentoComplementarDAO documentoDAO = transaction.attach(DocumentoComplementarDAO.class);

			if (configurarMedicao) {
				
				ContratoBD contratoBDPersistir = contratoBC.incluir(contratoSiconv, transaction);
				
				if (documentoInput.getSubmetas() != null && !documentoInput.getSubmetas().isEmpty()) {
					validarSubmetasDocumentoVinculadasSubmetasVRPL(documentoInput, contratoSiconv);
				}

				// Setar no documento o idMedContrato do seu contrato.
				documentoInput.setMedContratoFk(contratoBDPersistir.getId());
			}

			documentoInput
					.setCoCeph(cephActions.uploadFile(documentoInput.getArquivo(), documentoInput.getNmArquivo()));

			Long idDocumentoInserido = documentoDAO.inserirDocumento(documentoInput.converterParaBD());

			// Verifica se existe submeta associada. Se positivo, inserir
			if (documentoInput.getSubmetas() != null) {

				DocumentoComplementarSubmetaDAO documentoSubmetaDAO = transaction
						.attach(DocumentoComplementarSubmetaDAO.class);

				Set<Long> idSubmetasDocumentoInput = documentoInput.getIdSubmetas();

				documentoSubmetaDAO.inserirDocumentoSubmeta(idSubmetasDocumentoInput.stream()
						.map(idSubmeta -> new DocumentoComplementarSubmetaBD(idSubmeta, idDocumentoInserido))
						.collect(toList()));

			}

			return idDocumentoInserido;
		});
	}

   

	private ContratoSiconvDTO validarParametros(DocumentoComplementarDTO documentoInput, Long idContratoSiconv) {

		ContratoSiconvDTO contratoSiconv = contratoBC.consultarContratoPorId(idContratoSiconv);

		if (Optional.ofNullable(contratoSiconv).isPresent()) {

			if (documentoInput.getTipoDocumento().equals(TipoDocumentoEnum.MAM)) {

				this.validarManifestoAmbiental(documentoInput);

			} else if (documentoInput.getTipoDocumento().equals(TipoDocumentoEnum.AUT)
					|| documentoInput.getTipoDocumento().equals(TipoDocumentoEnum.DEC)
					|| documentoInput.getTipoDocumento().equals(TipoDocumentoEnum.OTG)) {

				this.validarAutDecOut(documentoInput);

			} else if (documentoInput.getTipoDocumento().equals(TipoDocumentoEnum.OSE)) {

				this.validarOrdemServico(documentoInput, contratoSiconv);

			} else if (documentoInput.getTipoDocumento().equals(TipoDocumentoEnum.OUT)) {

				this.validarOutros(documentoInput);

			}

		} else {
			throw new MedicaoRestException(MessageKey.CONTRATO_INEXISTENTE);
		}

		return contratoSiconv;

	}

	private void validarOutros(DocumentoComplementarDTO documentoInput) {

		StringBuilder parametrosNaoInformados = new StringBuilder();

		if (StringUtils.isBlank(documentoInput.getTxDescricao())) {
			parametrosNaoInformados.append(TX_DESCRICAO);
		}
		// Emissão, se informada, não pode ser futura
		if (documentoInput.getDtEmissao() != null && documentoInput.getDtEmissao().isAfter(LocalDate.now())) {
			throw new MedicaoRestException(new Message(MessageKey.ERRO_DOCUMENTO_COMPLEMENTAR_DATA_EMISSAO_FUTURA,
					new String[] { TemporalUtil.formataDataPtBR(LocalDate.now()) }));
		}
		if (parametrosNaoInformados.length() > 0) {
			throw new MedicaoRestException(new Message(MessageKey.ERRO_PARAMETRO_OBRIGATORIO_NAO_INFORMADO,
					new String[] { parametrosNaoInformados.toString() }));
		} else {
			// Limpar campos que não compõem Tipo documento "Outros"
			documentoInput.setTipoManifestoAmbiental(null);
			documentoInput.setDtValidade(null);
			documentoInput.setSubmetas(null);
			documentoInput.setTxDescricaoOutros(null);
		}

	}

	private void validarOrdemServico(DocumentoComplementarDTO documentoInput, ContratoSiconvDTO contratoSiconv) {

		StringBuilder parametrosNaoInformados = new StringBuilder();

		if (StringUtils.isBlank(documentoInput.getNrDocumento())) {
			parametrosNaoInformados.append(NUMERO_DOC);
		}
		if (StringUtils.isBlank(documentoInput.getNmOrgaoEmissor())) {
			parametrosNaoInformados.append(ORGAO_EMISSOR);
		}
		// Emissão é obrigatória e não pode ser futura
		if (documentoInput.getDtEmissao() == null) {
			parametrosNaoInformados.append(DT_EMISSAO);
		} else {
			LocalDate dtAssinatura = contratoSiconv.getDtAssinaturaTipoInstrumento();
			if (documentoInput.getDtEmissao().isAfter(LocalDate.now())) {
				throw new MedicaoRestException(new Message(MessageKey.ERRO_DOCUMENTO_COMPLEMENTAR_DATA_EMISSAO_FUTURA,
						new String[] { TemporalUtil.formataDataPtBR(LocalDate.now()) }));
			} else if (documentoInput.getDtEmissao().isBefore(dtAssinatura)) {
				throw new MedicaoRestException(
						new Message(MessageKey.ERRO_DOCUMENTO_COMPLEMENTAR_DATA_EMISSAO_MENOR_ASSINATURA,
								new String[] { TemporalUtil.formataDataPtBR(dtAssinatura) }));
			}
		}
		if (parametrosNaoInformados.length() > 0) {
			throw new MedicaoRestException(new Message(MessageKey.ERRO_PARAMETRO_OBRIGATORIO_NAO_INFORMADO,
					new String[] { parametrosNaoInformados.toString() }));
		} else {
			// Limpar campos que não compõem uma Ordem de Serviço
			documentoInput.setTipoManifestoAmbiental(null);
			documentoInput.setDtValidade(null);
			documentoInput.setTxDescricao(null);
			documentoInput.setSubmetas(null);
			documentoInput.setTxDescricaoOutros(null);
		}

	}

	/**
	 * Validar Autorização, Declaração ou Outorga
	 * 
	 * @param documentoInput
	 */
	private void validarAutDecOut(DocumentoComplementarDTO documentoInput) {

		StringBuilder parametrosNaoInformados = new StringBuilder();

		if (StringUtils.isBlank(documentoInput.getNrDocumento())) {
			parametrosNaoInformados.append(NUMERO_DOC);
		}
		if (StringUtils.isBlank(documentoInput.getNmOrgaoEmissor())) {
			parametrosNaoInformados.append(ORGAO_EMISSOR);
		}
		// Emissão é obrigatória e não pode ser futura
		if (documentoInput.getDtEmissao() == null) {
			parametrosNaoInformados.append(DT_EMISSAO);
		} else {
			if (documentoInput.getDtEmissao().isAfter(LocalDate.now())) {
				throw new MedicaoRestException(new Message(MessageKey.ERRO_DOCUMENTO_COMPLEMENTAR_DATA_EMISSAO_FUTURA,
						new String[] { TemporalUtil.formataDataPtBR(LocalDate.now()) }));
			}
		}
		// Se informado, deve ser maior ou igual a Data de Emissão
		validarDtValidadeMaiorIgualDtEmissao(documentoInput);

		if (documentoInput.getSubmetas() == null || documentoInput.getSubmetas().isEmpty()) {
			parametrosNaoInformados.append(SUBMETA);
		}

		if (parametrosNaoInformados.length() > 0) {
			throw new MedicaoRestException(new Message(MessageKey.ERRO_PARAMETRO_OBRIGATORIO_NAO_INFORMADO,
					new String[] { parametrosNaoInformados.toString() }));
		} else {
			// Limpar campos que não compõem uma Autorização, Declaração ou Outorga.
			documentoInput.setTipoManifestoAmbiental(null);
			documentoInput.setTxDescricao(null);
			documentoInput.setTxDescricaoOutros(null);
		}

	}

	private void validarDtValidadeMaiorIgualDtEmissao(DocumentoComplementarDTO documentoInput) {
		if (documentoInput.getDtValidade() != null && documentoInput.getDtEmissao() != null
				&& (!(documentoInput.getDtValidade().isAfter(documentoInput.getDtEmissao())
						|| documentoInput.getDtValidade().isEqual(documentoInput.getDtEmissao())))) {

			throw new MedicaoRestException(new Message(MessageKey.ERRO_DOCUMENTO_COMPLEMENTAR_DATA_MAIOR_IGUAL_EMISSAO,
					new String[] { TemporalUtil.formataDataPtBR(documentoInput.getDtEmissao()) }));

		}
	}

	private void validarManifestoAmbiental(DocumentoComplementarDTO documentoInput) {

		StringBuilder parametrosNaoInformados = new StringBuilder();

		validarParametros(documentoInput, parametrosNaoInformados);

		if (parametrosNaoInformados.length() > 0) {
			throw new MedicaoRestException(new Message(MessageKey.ERRO_PARAMETRO_OBRIGATORIO_NAO_INFORMADO,
					new String[] { parametrosNaoInformados.toString() }));
		} else {
			// Limpar campos que não compõem Manifesto Ambiental
			documentoInput.setTxDescricao(null);

			if (!documentoInput.getTipoManifestoAmbiental().equals(OUT)) {
				documentoInput.setTxDescricaoOutros(null);
			}
		}

	}



	private void validarParametros(DocumentoComplementarDTO documentoInput, StringBuilder parametrosNaoInformados) {
		if (documentoInput.getTipoManifestoAmbiental() == null) {
			parametrosNaoInformados.append(MANIFESTO_AMBIENTAL);
		}
		if (StringUtils.isBlank(documentoInput.getNrDocumento())) {
			parametrosNaoInformados.append(NUMERO_DOC);
		}
		if (StringUtils.isBlank(documentoInput.getNmOrgaoEmissor())) {
			parametrosNaoInformados.append(ORGAO_EMISSOR);
		}
		if (documentoInput.getDtEmissao() == null) {
			parametrosNaoInformados.append(DT_EMISSAO);
		}
		// Valida obrigatoriedade Data de Validade
		if (documentoInput.getDtValidade() == null && documentoInput.getTipoManifestoAmbiental() != null
				&& (documentoInput.getTipoManifestoAmbiental().equals(LPR)
						|| documentoInput.getTipoManifestoAmbiental().equals(LIN)
						|| documentoInput.getTipoManifestoAmbiental().equals(LOP)
						|| (documentoInput.getTipoManifestoAmbiental().equals(OUT) && documentoInput.getEqLicencaInstalacao() != null && documentoInput.getEqLicencaInstalacao())
						)) {
			parametrosNaoInformados.append(DT_VALIDADE);
		}
		
		if (documentoInput.getTipoManifestoAmbiental() != null && documentoInput.getTipoManifestoAmbiental().equals(OUT) && StringUtils.isBlank(documentoInput.getTxDescricaoOutros()) ) {
			parametrosNaoInformados.append(DESC_TP_MANIF_OUTROS);
		}

		validarDtValidadeMaiorIgualDtEmissao(documentoInput);

		if (documentoInput.getSubmetas() == null || documentoInput.getSubmetas().isEmpty()) {
			parametrosNaoInformados.append(SUBMETA);
		}
	}

	private void validarSubmetasDocumentoVinculadasContrato(DocumentoComplementarDTO documento,
			ContratoBD contratoMedicao) {

		Set<Long> idSubmetasContrato = consultarSubmetasContrato(contratoMedicao).keySet();
		Set<Long> idSubmetasDocumento = documento.getIdSubmetas();

		if (!idSubmetasContrato.containsAll(idSubmetasDocumento)) {
			throw new MedicaoRestException(MessageKey.ERRO_DOCUMENTO_COMPLEMENTAR_SUBMETA_NAO_VINCULADA_CONTRATO);
		}
	}

	private void validarSubmetasDocumentoVinculadasSubmetasVRPL(DocumentoComplementarDTO documento,
			ContratoSiconvDTO contratoSiconv) {

		Set<Long> idSubmetaVrpl;
				
		if(BooleanUtils.isTrue(contratoSiconv.getInAcompEvento())) {
			
			List<EventoFrenteObraTotalizadoDTO> listaEventoFrenteObraVrplDTO = 
					itemMedicaoBC.getEventoFO(contratoSiconv.getId());
			
			idSubmetaVrpl = listaEventoFrenteObraVrplDTO.stream()
					.map(EventoFrenteObraTotalizadoDTO::getIdSubmetaVrpl).collect(toSet());			
			
		} else {
			List<ServicoFrenteObraTotalizadoDTO> listaMacroServicoFrenteObraVrplDTO = 
					itemMedicaoBC.getMacroServicoFO(contratoSiconv.getId());
			
			idSubmetaVrpl = listaMacroServicoFrenteObraVrplDTO.stream()
					.map(ServicoFrenteObraTotalizadoDTO::getIdSubmetaVrpl).collect(toSet());

		}
		
		Set<Long> idSubmetasDocumento = documento.getIdSubmetas();
		
		if (!idSubmetaVrpl.containsAll(idSubmetasDocumento)) {
			throw new MedicaoRestException(MessageKey.ERRO_DOCUMENTO_COMPLEMENTAR_SUBMETA_NAO_VINCULADA_CONTRATO);
		}
	}

	private ContratoDAO getContratoDAO() {
		return jdbi.onDemand(ContratoDAO.class);
	}

	private MedicaoDAO getMedicaoDAO() {
		return jdbi.onDemand(MedicaoDAO.class);
	}

	private DocumentoComplementarDAO getDocumentoComplementarDAO() {
		return jdbi.onDemand(DocumentoComplementarDAO.class);
	}

	private Map<Long, SubmetaVrplDTO> consultarSubmetasContrato(ContratoBD contratoMedicao) {
		return submetaBC.listarSubmetasPorContrato(contratoMedicao.getContratoFk()).stream()
				.collect(toMap(SubmetaVrplDTO::getId, Function.identity()));
	}

	private DocumentoComplementarDTO consultarDocumentoComplementarDTO(Long idDocumentoComplementar) {
		return getDocumentoComplementarDAO().consultarDocumentoComplementar(idDocumentoComplementar)
				.orElseThrow(() -> new MedicaoRestException(MessageKey.ERRO_DOCUMENTO_COMPLEMENTAR_INEXISTENTE));
	}
	
	private ContratoResponsavelTecnicoDAO getContratoResponsavelTecnicoDAO() {
		return jdbi.onDemand(ContratoResponsavelTecnicoDAO.class);
	}
	
	private ContratoResponsavelTecnicoSocialDAO getContratoResponsavelTecnicoSocialDAO() {
		return jdbi.onDemand(ContratoResponsavelTecnicoSocialDAO.class);
	}

	private ParalisacaoDAO getParalisacaoDAO() {
		return jdbi.onDemand(ParalisacaoDAO.class);
	}
	
	/**
	 * Exclui Documento Complementar vinculado ao contrato.
	 * 
	 * @param idDocumentoComplementar
	 */
	public void excluirDocumentoComplementarContrato(Long idDocumentoComplementar) {

		DocumentoComplementarDTO docComplementar = this.consultarDocumentoComplementarDTO(idDocumentoComplementar);
		
		validarExclusaoDocumento(docComplementar);

		jdbi.useTransaction(handle -> {
			DocumentoComplementarDAO documentoComplementarDAO = handle.attach(DocumentoComplementarDAO.class);
			DocumentoComplementarSubmetaDAO documentoComplementarSubmetaDAO = handle
					.attach(DocumentoComplementarSubmetaDAO.class);

			if (docComplementar.getSubmetas() != null && !docComplementar.getSubmetas().isEmpty()) {
				documentoComplementarSubmetaDAO.deletarPorIdDocumentoComplementar(idDocumentoComplementar);
			}
			documentoComplementarDAO.excluir(idDocumentoComplementar);
					
			if(permiteExclusaoEstruturaContrato(docComplementar.getMedContratoFk())) {
				contratoBC.excluirEstruturaContrato(docComplementar.getMedContratoFk(),handle); 
			}
		});
	}

	private void validarExclusaoDocumento(DocumentoComplementarDTO docComplementar) {

		validarDocumentoBloqueado(docComplementar);

		if (docComplementar.getTipoDocumento().equals(TipoDocumentoEnum.OSE)) {
			ContratoBD contratoMedicao = getContratoDAO().consultarContrato(docComplementar.getMedContratoFk());
			MedicaoBD medicao = getMedicaoDAO().consultarUltimaMedicao(contratoMedicao.getContratoFk());
			if (medicao != null) {
				throw new MedicaoRestException(new Message(MessageKey.ERRO_DOCUMENTO_COMPLEMENTAR_POSSUI_MEDICAO));
			}
		}
	}
	
	private boolean permiteExclusaoEstruturaContrato(Long idContratoMedicao) {
		
		Boolean existeRTContrato = getContratoResponsavelTecnicoDAO().existeRespTecnicoContrato(idContratoMedicao) || 
				getContratoResponsavelTecnicoSocialDAO().existeRespTecnicoSocialContrato(idContratoMedicao);
		
		Boolean existeDCContrato = getDocumentoComplementarDAO().existeDocumentoComplementarContrato(idContratoMedicao);
		
		Boolean existeParalisacao = getParalisacaoDAO().existeParalisacaoContrato(idContratoMedicao);
		
		return (!existeRTContrato && !existeDCContrato && !existeParalisacao);
	}

	private void validarDocumentoBloqueado(DocumentoComplementarDTO docComplementar) {

		if (docComplementar.isBloqueado()) {
			throw new MedicaoRestException(MessageKey.ERRO_DOCUMENTO_COMPLEMENTAR_BLOQUEADO);
		}
	}

	public List<DocumentoComplementarDTO> listarDocumentosComplementares(Long idContratoSiconv) {

		List<DocumentoComplementarDTO> lista = new ArrayList<>();

		ContratoBD contratoMedicao = getContratoDAO().consultarContratoPorContratoFK(idContratoSiconv);
		Integer qtdMedicoesContrato = getMedicaoDAO().consultarQtdeMedicoesPorContrato(idContratoSiconv);

		if (contratoMedicao != null) {
			Map<Long, SubmetaVrplDTO> submetasContrato = consultarSubmetasContrato(contratoMedicao);
			lista = getDocumentoComplementarDAO().listarDocumentosComplementares(idContratoSiconv);
			lista.forEach(doc -> decorateDocumentoComplementarDTO(doc, qtdMedicoesContrato, submetasContrato));

		}

		return lista;
	}

	private DocumentoComplementarDTO decorateDocumentoComplementarDTO(DocumentoComplementarDTO doc,
			Integer qtdMedicoesContrato, Map<Long, SubmetaVrplDTO> submetasContrato) {

		doc.setPossuiMedicao(qtdMedicoesContrato > 0);
		
		doc.getSubmetas().replaceAll((SubmetaVrplDTO submeta) -> 
			submetasContrato.getOrDefault(submeta.getId(), submeta)
		);
		sort(doc.getSubmetas(), SubmetaVrplDTO.ORDENACAO_PADRAO);

        if (securityContext.isSensitiveDataObfuscationEnabled() && doc.getTipoDocumento() == TipoDocumentoEnum.OUT) {
            doc.setUrl(null);
        } else {
            doc.setUrl(cephActions.getPresignedUrl(doc.getCoCeph()));
        }

		return doc;
	}

	public DocumentoComplementarDTO consultarDocumentoComplementar(Long idDocumentoComplementar) {

		DocumentoComplementarDTO doc = consultarDocumentoComplementarDTO(idDocumentoComplementar);

		ContratoBD contratoMedicao = getContratoDAO().consultarContrato(doc.getMedContratoFk());

		Map<Long, SubmetaVrplDTO> submetasContrato = consultarSubmetasContrato(contratoMedicao);
		
		Integer qtdMedicoesContrato = getMedicaoDAO().consultarQtdeMedicoesPorContrato(contratoMedicao.getContratoFk());
		return decorateDocumentoComplementarDTO(doc, qtdMedicoesContrato, submetasContrato);

	}

	public void alterarDocumentoComplementar(
			@Valid @ConvertGroup(to = UpdateGroup.class) DocumentoComplementarDTO docComplementarInput) {

		DocumentoComplementarDTO docComplementarBase = consultarDocumentoComplementarDTO(docComplementarInput.getId());

		docComplementarInput.setMedContratoFk(docComplementarBase.getMedContratoFk());

		validarAlteracao(docComplementarInput, docComplementarBase);

		jdbi.useTransaction(handle -> {

			DocumentoComplementarDAO documentoComplementarDAO = handle.attach(DocumentoComplementarDAO.class);
			DocumentoComplementarSubmetaDAO documentoSubmetaDAO = handle.attach(DocumentoComplementarSubmetaDAO.class);

			if (!isEmpty(docComplementarInput.getNmArquivo())) {
				docComplementarInput.setCoCeph(
						cephActions.uploadFile(docComplementarInput.getArquivo(), docComplementarInput.getNmArquivo()));

			} else {
				docComplementarInput.setNmArquivo(docComplementarBase.getNmArquivo());
				docComplementarInput.setCoCeph(docComplementarBase.getCoCeph());
			}

			Set<Long> idSubmetasAnotacaoInput = docComplementarInput.getSubmetas() != null
					? docComplementarInput.getIdSubmetas()
					: new HashSet<>();
			Set<Long> idSubmetasAnotacaoBase = docComplementarBase.getSubmetas() != null
					? docComplementarBase.getIdSubmetas()
					: new HashSet<>();

			documentoSubmetaDAO.inserirDocumentoSubmeta(difference(idSubmetasAnotacaoInput, idSubmetasAnotacaoBase)
					.stream()
					.map(idSubmeta -> new DocumentoComplementarSubmetaBD(idSubmeta, docComplementarInput.getId()))
					.collect(toList()));

			documentoSubmetaDAO.deletar(difference(idSubmetasAnotacaoBase, idSubmetasAnotacaoInput).stream()
					.map(idSubmeta -> new DocumentoComplementarSubmetaBD(idSubmeta, docComplementarBase.getId()))
					.collect(toList()));
			
			documentoComplementarDAO.alterar(docComplementarInput.converterParaBD());
		});
	}

	private void validarAlteracao(DocumentoComplementarDTO docComplementarInput,
			DocumentoComplementarDTO docComplementarBase) {

		ContratoBD contratoMedicaoBase = getContratoDAO().consultarContrato(docComplementarBase.getMedContratoFk());

		validarDocumentoBloqueado(docComplementarBase);

		if (docComplementarBase.getTipoDocumento().equals(TipoDocumentoEnum.OSE)
				&& existeMedicaoParaContrato(contratoMedicaoBase)) {
			throw new MedicaoRestException(MessageKey.ERRO_EDITAR_DOCUMENTO_COMPLEMENTAR_EXISTE_MEDICAO);
		}

		ContratoBD contratoMedicaoInput = getContratoDAO().consultarContrato(docComplementarInput.getMedContratoFk());

		if (docComplementarInput.getSubmetas() != null && !docComplementarInput.getSubmetas().isEmpty()) {
			validarSubmetasDocumentoVinculadasContrato(docComplementarInput, contratoMedicaoInput);
		}

		validarParametros(docComplementarInput,
				contratoMedicaoInput != null ? contratoMedicaoInput.getContratoFk() : null);

	}

	private boolean existeMedicaoParaContrato(ContratoBD contratoMedicao) {
		return getMedicaoDAO().consultarUltimaMedicao(contratoMedicao.getContratoFk()) != null;
	}

	/**
	 * Bloqueia os documentos (ainda não bloqueados) do Contrato de Fornecimento. As
	 * operações são realizadas dentro de uma mesma transação.
	 */
	public void bloquearDocumentosComplementares(Handle handle, Long idContratoMedicao) {

		DocumentoComplementarDAO dao = handle.attach(DocumentoComplementarDAO.class);

		dao.listarIdDocumentoComplementar(idContratoMedicao, false).forEach(
				idDocumentoDesbloqueado -> dao.setarBloqueioDocumentoComplementar(idDocumentoDesbloqueado, true));
	}

	/**
	 * Desbloqueia os documentos do Contrato de Fornecimento. As operações são
	 * realizadas dentro de uma mesma transação.
	 */
	public void desbloquearDocumentosComplementares(Handle handle, Long idContratoMedicao) {

		DocumentoComplementarDAO dao = handle.attach(DocumentoComplementarDAO.class);

		dao.listarIdDocumentoComplementar(idContratoMedicao, true)
				.forEach(idDocumentoBloqueado -> dao.setarBloqueioDocumentoComplementar(idDocumentoBloqueado, false));
	}

	public void setarBloqueioDocumentoComplementar(Long idDocumentoComplementar, boolean bloqueio) {

		getDocumentoComplementarDAO().setarBloqueioDocumentoComplementar(idDocumentoComplementar, bloqueio);

	}
}
