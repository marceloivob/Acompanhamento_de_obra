package br.gov.planejamento.siconv.med.configuracao.paralisacao.business;

import static com.google.common.collect.Sets.difference;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.groups.ConvertGroup;

import org.jdbi.v3.core.Jdbi;

import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.dao.DocumentoComplementarDAO;
import br.gov.planejamento.siconv.med.configuracao.paralisacao.dao.AnexoParalisacaoDAO;
import br.gov.planejamento.siconv.med.configuracao.paralisacao.dao.ParalisacaoDAO;
import br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.database.AnexoParalisacaoBD;
import br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.database.ParalisacaoBD;
import br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.dto.AnexoParalisacaoDTO;
import br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.dto.ParalisacaoDTO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao.ContratoResponsavelTecnicoDAO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao.ContratoResponsavelTecnicoSocialDAO;
import br.gov.planejamento.siconv.med.contrato.business.ContratosBC;
import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.contrato.entity.dto.ContratoSiconvDTO;
import br.gov.planejamento.siconv.med.infra.exception.MedicaoRestException;
import br.gov.planejamento.siconv.med.infra.message.Message;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.infra.validation.InsertGroup;
import br.gov.planejamento.siconv.med.infra.validation.UpdateGroup;
import br.gov.planejamento.siconv.med.integration.ceph.CephActions;

@ApplicationScoped
public class ParalisacaoBC {

	@Inject
	private Jdbi jdbi;

	@Inject
	private CephActions cephActions;

	@Inject
	private ContratosBC contratoBC;

	/**
	 * Listar paralisações de um contrato por idContratoSiconv
	 * 
	 * @param idContratoSiconv
	 * @return
	 */
	public List<ParalisacaoDTO> listarParalisacoes(Long idContratoSiconv) {
		List<ParalisacaoDTO> lista = getParalisacaoDAO().listarParalisacoes(idContratoSiconv);
		lista.forEach(this::decorateParalisacaoDTO);
		return lista;
	}

	/**
	 * Consulta de paralisação por idParalisação
	 * 
	 * @param idParalisacao
	 * @return
	 */
	public ParalisacaoDTO consultarParalisacao(Long idParalisacao) {
		ParalisacaoDTO paralisacao = consultarParalisacaoDTO(idParalisacao);
		decorateParalisacaoDTO(paralisacao);
		return paralisacao;
	}

	private void decorateParalisacaoDTO(ParalisacaoDTO paralisacao) {
		paralisacao.getAnexos().forEach(anexo -> anexo.setUrl(cephActions.getPresignedUrl(anexo.getCoCeph())));
	}

	private ParalisacaoDTO consultarParalisacaoDTO(Long idParalisacao) {
		return getParalisacaoDAO().consultarParalisacao(idParalisacao)
				.orElseThrow(() -> new MedicaoRestException(MessageKey.ERRO_PARALISACAO_OBRA_INEXISTENTE));
	}

	/**
	 * Excluir paralisação por idParalisacao
	 * 
	 * @param idParalisacao
	 */
	public void excluirParalisacao(Long idParalisacao) {

		ParalisacaoDTO paralisacao = consultarParalisacaoDTO(idParalisacao);

		validarPermissaoManterParalisacao(paralisacao);

		jdbi.useTransaction(handle -> {
			handle.attach(AnexoParalisacaoDAO.class).excluirAnexoPorParalisacaoId(idParalisacao);
			handle.attach(ParalisacaoDAO.class).excluirParalisacaoPorId(idParalisacao);

			if (permiteExclusaoEstruturaContrato(paralisacao.getMedContratoFk())) {
				contratoBC.excluirEstruturaContrato(paralisacao.getMedContratoFk(), handle);
			}
		});
	}

	private void validarPermissaoManterParalisacao(ParalisacaoDTO paralisacao) {
		if (!isUltimaParalisacao(paralisacao)) {
			throw new MedicaoRestException(MessageKey.ERRO_MANTER_PARALISACAO);
		}
	}

	private boolean permiteExclusaoEstruturaContrato(Long idContratoMedicao) {

		Boolean existeRTContrato = getContratoResponsavelTecnicoDAO().existeRespTecnicoContrato(idContratoMedicao)
				|| getContratoResponsavelTecnicoSocialDAO().existeRespTecnicoSocialContrato(idContratoMedicao);

		Boolean existeDCContrato = getDocumentoComplementarDAO().existeDocumentoComplementarContrato(idContratoMedicao);

		Boolean existeParalisacao = getParalisacaoDAO().existeParalisacaoContrato(idContratoMedicao);

		return (!existeRTContrato && !existeDCContrato && !existeParalisacao);
	}

	private boolean isUltimaParalisacao(ParalisacaoDTO paralisacao) {

		ParalisacaoBD ultimaParalisacao = getParalisacaoDAO()
				.consultarUltimaParalisacao(paralisacao.getIdContratoSiconv());

		return ultimaParalisacao != null && ultimaParalisacao.getId().equals(paralisacao.getId());
	}

	/**
	 * Incluir paralisação
	 * 
	 * @param idContratoSiconv
	 * @param paralisacaoDTO
	 * @return
	 */
	public Long incluirParalisacao(Long idContratoSiconv,
			@Valid @ConvertGroup(to = InsertGroup.class) ParalisacaoDTO paralisacaoDTO) {

		validarInclusaoParalisacao(idContratoSiconv, paralisacaoDTO);

		return jdbi.inTransaction(handle -> {

			ContratoBD contratoMedicao = contratoBC.consultarContratoMedicaoPorContratoFK(idContratoSiconv);

			if (contratoMedicao == null) {
				ContratoSiconvDTO contratoSiconv = contratoBC.consultarContratoPorId(idContratoSiconv);
				contratoMedicao = contratoBC.incluir(contratoSiconv, handle);
			}

			paralisacaoDTO.setMedContratoFk(contratoMedicao.getId());

			ParalisacaoDAO paralisacaoDAO = handle.attach(ParalisacaoDAO.class);
			AnexoParalisacaoDAO anexoParalisacaoDAO = handle.attach(AnexoParalisacaoDAO.class);

			Long idParalisacaoInserida = paralisacaoDAO.inserirParalisacao(paralisacaoDTO.converterParaBD());

			if (isNotEmpty(paralisacaoDTO.getAnexos())) {
				List<AnexoParalisacaoBD> listaAnexosBD = prepararAnexosParalisacao(paralisacaoDTO.getAnexos(),
						idParalisacaoInserida);
				anexoParalisacaoDAO.inserirAnexosParalisacao(listaAnexosBD);
			}

			return idParalisacaoInserida;
		});
	}

	private void validarInclusaoParalisacao(Long idContratoSiconv, ParalisacaoDTO paralisacaoDTO) {

		// Tem Paralisação em aberto
		if (getParalisacaoDAO().existeParalisacaoEmAberto(idContratoSiconv)) {
			throw new MedicaoRestException(new Message(MessageKey.ERRO_INCLUSAO_EXISTE_PARALISACAO_EM_ABERTO));
		}

		ParalisacaoBD ultimaParalisacao = getParalisacaoDAO().consultarUltimaParalisacao(idContratoSiconv);

		validarDatasParalisacao(paralisacaoDTO, ultimaParalisacao);
	}

	private void validarDatasParalisacao(ParalisacaoDTO paralisacaoAtual, ParalisacaoBD paralisacaoAnterior) {

		// A Data Fim da paralisação deve ser maior que a Data de Início.
		if (paralisacaoAtual.getDataFim() != null
				&& !paralisacaoAtual.getDataFim().isAfter(paralisacaoAtual.getDataInicio())) {
			throw new MedicaoRestException(new Message(MessageKey.ERRO_DATA_INICIO_PARALISACAO_POSTERIOR_DATA_FIM));
		}

		// A Data de Início da paralisação deve ser maior que Data Fim da última
		// paralisação.
		if (paralisacaoAnterior != null && !paralisacaoAnterior.getId().equals(paralisacaoAtual.getId())
				&& (paralisacaoAtual.getDataInicio().isBefore(paralisacaoAnterior.getDtFim()) || 
						paralisacaoAtual.getDataInicio().isEqual(paralisacaoAnterior.getDtFim()))) {
			throw new MedicaoRestException(
					new Message(MessageKey.ERRO_DATA_INICIO_ANTERIOR_DATA_FIM_ULTIMA_PARALISACAO));
		}
	}

	private List<AnexoParalisacaoBD> prepararAnexosParalisacao(List<AnexoParalisacaoDTO> listaAnexosDTO,
			Long idParalisacao) {

		List<AnexoParalisacaoBD> listaAnexosBD = new ArrayList<>();

		for (AnexoParalisacaoDTO anexoDTO : listaAnexosDTO) {
			anexoDTO.setCoCeph(this.anexarArquivo(anexoDTO.getNmArquivo(), anexoDTO.getArquivo()));
			anexoDTO.setParalisacaoFk(idParalisacao);
			listaAnexosBD.add(anexoDTO.converterParaBD());
		}

		return listaAnexosBD;
	}

	/**
	 * Alterar paralisação e seus anexos
	 * 
	 * @param idParalisacao
	 * @param paralisacaoDTO
	 */
	public void alterarParalisacao(@Valid @ConvertGroup(to = UpdateGroup.class) ParalisacaoDTO paralisacaoDTO) {

		validarAlteracaoParalisacao(paralisacaoDTO);

		if (isEmpty(paralisacaoDTO.getAnexos())) {

			jdbi.useTransaction(transaction -> {
				transaction.attach(AnexoParalisacaoDAO.class).excluirAnexoPorParalisacaoId(paralisacaoDTO.getId());
				transaction.attach(ParalisacaoDAO.class).alterarParalisacao(paralisacaoDTO.converterParaBD());
			});

		} else {

			Set<Long> idAnexosInput = paralisacaoDTO.getAnexos().stream().map(AnexoParalisacaoDTO::getId)
					.filter(Objects::nonNull).collect(toSet());

			Set<Long> idAnexosBanco = getAnexoParalisacaoDAO().buscarIdAnexoPorIdParalisacao(paralisacaoDTO.getId());

			Set<Long> idAnexosExcluir = difference(idAnexosBanco, idAnexosInput);

			List<AnexoParalisacaoDTO> novosAnexos = paralisacaoDTO.getAnexos().stream()
					.filter(anexo -> anexo.getId() == null).collect(toList());

			jdbi.useTransaction(transaction -> {

				if (isNotEmpty(idAnexosExcluir)) {
					transaction.attach(AnexoParalisacaoDAO.class).excluirAnexoPorListaIdAnexo(idAnexosExcluir);
				}

				if (isNotEmpty(novosAnexos)) {
					List<AnexoParalisacaoBD> listaAnexosBD = prepararAnexosParalisacao(novosAnexos,
							paralisacaoDTO.getId());
					transaction.attach(AnexoParalisacaoDAO.class).inserirAnexosParalisacao(listaAnexosBD);
				}

				transaction.attach(ParalisacaoDAO.class).alterarParalisacao(paralisacaoDTO.converterParaBD());
			});

		}
	}

	private void validarAlteracaoParalisacao(ParalisacaoDTO paralisacaoDTO) {

		ParalisacaoDTO paralisacaoBanco = consultarParalisacaoDTO(paralisacaoDTO.getId());

		validarPermissaoManterParalisacao(paralisacaoBanco);

		ParalisacaoBD paralisacaoAnterior = getParalisacaoDAO()
				.consultarParalisacaoAnterior(paralisacaoBanco.getMedContratoFk(), paralisacaoBanco.getDataInicio());

		this.validarDatasParalisacao(paralisacaoDTO, paralisacaoAnterior);
	}

	private String anexarArquivo(String nomeArquivo, byte[] arquivo) {
		return cephActions.uploadFile(arquivo, nomeArquivo);
	}

	private ParalisacaoDAO getParalisacaoDAO() {
		return jdbi.onDemand(ParalisacaoDAO.class);
	}

	private AnexoParalisacaoDAO getAnexoParalisacaoDAO() {
		return jdbi.onDemand(AnexoParalisacaoDAO.class);
	}

	private DocumentoComplementarDAO getDocumentoComplementarDAO() {
		return jdbi.onDemand(DocumentoComplementarDAO.class);
	}

	private ContratoResponsavelTecnicoSocialDAO getContratoResponsavelTecnicoSocialDAO() {
		return jdbi.onDemand(ContratoResponsavelTecnicoSocialDAO.class);
	}

	private ContratoResponsavelTecnicoDAO getContratoResponsavelTecnicoDAO() {
		return jdbi.onDemand(ContratoResponsavelTecnicoDAO.class);
	}
}
