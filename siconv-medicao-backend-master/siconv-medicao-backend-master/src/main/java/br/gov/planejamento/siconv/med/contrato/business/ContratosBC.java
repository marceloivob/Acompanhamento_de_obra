package br.gov.planejamento.siconv.med.contrato.business;

import static br.gov.planejamento.siconv.med.infra.util.TemporalUtil.diferenca;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.log4j.Logger;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.dao.DocumentoComplementarDAO;
import br.gov.planejamento.siconv.med.configuracao.paralisacao.dao.ParalisacaoDAO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao.AnotacaoRegistroRespTecnicoDAO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao.ContratoResponsavelTecnicoSocialDAO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.TipoResponsavelTecnicoEnum;
import br.gov.planejamento.siconv.med.contrato.dao.ContratoDAO;
import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.contrato.entity.dto.AndamentoContratoDTO;
import br.gov.planejamento.siconv.med.contrato.entity.dto.ContratoSiconvDTO;
import br.gov.planejamento.siconv.med.empresa.business.EmpresaBC;
import br.gov.planejamento.siconv.med.empresa.entity.dto.EmpresaDTO;
import br.gov.planejamento.siconv.med.infra.database.DAOFactory;
import br.gov.planejamento.siconv.med.infra.exception.MedicaoRestException;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.integration.contratos.ContratosGrpcConsumer;
import br.gov.planejamento.siconv.med.medicao.dao.HistoricoMedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.ItemMedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.MedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.SubmetaDAO;
import br.gov.planejamento.siconv.med.medicao.entity.database.ItemMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.ItemMedicaoBMBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.MedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.dto.EventoFrenteObraTotalizadoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoFrenteObraTotalizadoDTO;

@ApplicationScoped
public class ContratosBC {

	private static final Logger LOGGER = Logger.getLogger(ContratosBC.class);

	@Inject
    private DAOFactory dao;
	
	@Inject
	private Jdbi jdbi;

	@Inject
	private ContratosGrpcConsumer contratosConsumer;

	@Inject
	private EmpresaBC empresaBC;
	
	@Inject
	private ItemMedicaoBC itemMedicaoBC;

	public List<ContratoSiconvDTO> listarContratosPorEmpresa(Long idEmpresa) {

		EmpresaDTO empresa = empresaBC.consultarEmpresaPorId(idEmpresa);

		List<ContratoSiconvDTO> lista = contratosConsumer.listarContratosAptosPorEmpresa(empresa.getCnpj());

		return lista.stream().sorted(comparing(ContratoSiconvDTO::getDtInicioVigencia).reversed()).map(contrato -> {

			AndamentoContratoDTO andamentoContrato = consultarAndamentoContrato(contrato.getId());
			contrato.setQtdeDiasSemMedicao(andamentoContrato.getQtdeDiasSemMedicao());
			contrato.setInContratoAtrasado(andamentoContrato.isAtrasado());

			return decorateContratoDTO(contrato, empresa);

		}).collect(toList());
	}
	

	public ContratoSiconvDTO consultarContratoPorId(Long idContratoSiconv) {

		ContratoSiconvDTO contrato = contratosConsumer.consultarContratoPorId(idContratoSiconv)
				.orElseThrow(() -> new MedicaoRestException(MessageKey.CONTRATO_INEXISTENTE));

		EmpresaDTO empresa = empresaBC.consultarEmpresaPorCnpj(contrato.getCnpj());

		return decorateContratoDTO(contrato, empresa);
	}

	/**
	 * Retorna se contrato tem submetas a serem executadas pela Empresa
	 * @param idContratoSiconv
	 * @return
	 */
	public boolean temSubmetasAExecutar(Long idContratoSiconv) {
		
		ContratoSiconvDTO contrato = contratosConsumer.consultarContratoPorId(idContratoSiconv)
				.orElseThrow(() -> new MedicaoRestException(MessageKey.CONTRATO_INEXISTENTE));
		
		return contrato.getInAcompEvento()?
				dao.get(SubmetaDAO.class).isContratoComSubmetaAExecutar(idContratoSiconv):
				dao.get(SubmetaDAO.class).isContratoBMComSubmetaAExecutar(idContratoSiconv);
	}
	
	/**
	 * 
	 * @param contrato
	 * @return
	 */
	private ContratoSiconvDTO decorateContratoDTO(ContratoSiconvDTO contrato, EmpresaDTO empresa) {

		contrato.setFornecedorId(empresa.getId());

		contrato.setQtdeMedicoes(getMedicaoDAO().consultarQtdeMedicoesPorContrato(contrato.getId()));

		contrato.setIsConfiguradoParaMedicao(isConfiguradoParaMedicao(contrato));
		
		contrato.setInContratoParalisado(isContratoParalisado(contrato.getId()));

		return contrato;
	}

	public boolean isContratoConfiguradoParaMedicao(Long idContratoSiconv) {

		ContratoSiconvDTO contrato = contratosConsumer.consultarContratoPorId(idContratoSiconv)
				.orElseThrow(() -> new MedicaoRestException(MessageKey.CONTRATO_INEXISTENTE));

		return isConfiguradoParaMedicao(contrato);
	}

	private boolean isConfiguradoParaMedicao(ContratoSiconvDTO contrato) {

		if (getDocComplementarDAO().consultarDocumentoComplementarOSParaContrato(contrato.getId()).isEmpty()) {
			return false;
		}

		if (contrato.getInSocial()) {
			if (!getContratoRTSocialDAO().existeRTSocialAtivo(contrato.getId(),
					TipoResponsavelTecnicoEnum.EXE.getCodigo())
					|| !getContratoRTSocialDAO().existeRTSocialAtivo(contrato.getId(),
							TipoResponsavelTecnicoEnum.FIS.getCodigo())) {
				return false;
			}
		} else {
			if (!getAnotacaoDAO().existeAnotacaoComRTAtiva(contrato.getId(), TipoResponsavelTecnicoEnum.EXE.getCodigo())
					|| !getAnotacaoDAO().existeAnotacaoComRTAtiva(contrato.getId(),
							TipoResponsavelTecnicoEnum.FIS.getCodigo())) {
				return false;
			}
		}

		return true;
	}

	public ContratoBD consultarContratoAssociadoMedicao(Long idMedicao) {

		return getContratoDAO().consultarContratoAssociadoMedicao(idMedicao);
	}

	public ContratoBD consultarContratoAssociadoObservacao(Long idObservacao) {

		return getContratoDAO().consultarContratoAssociadoObservacao(idObservacao);
	}

	public ContratoBD consultarContratoMedicaoPorContratoFK(Long contratoFk) {

		return getContratoDAO().consultarContratoPorContratoFK(contratoFk);
	}

	public ContratoBD consultarContratoMedicaoPorPropostaFK(Long propostaFk) {

		return getContratoDAO().consultarContratoPorPropostaFK(propostaFk);
	}
	
	public ContratoBD consultarContratoAssociadoAnotacao(Long idAnotacao) {

		return getContratoDAO().consultarContratoAssociadoAnotacao(idAnotacao);
	}

	public ContratoBD consultarContratoAssociadoContratoRespTecnico(Long idContratoRespTec) {

		return getContratoDAO().consultarContratoAssociadoContratoRespTecnico(idContratoRespTec);
	}

	public ContratoBD consultarContratoAssociadoContratoRespTecnicoSocial(Long idContratoRespTecSocial) {

		return getContratoDAO().consultarContratoAssociadoContratoRespTecnicoSocial(idContratoRespTecSocial);
	}

	public ContratoBD consultarContratoAssociadoDocumentoComplementar(Long idDocumentoComplementar) {

		return getContratoDAO().consultarContratoAssociadoDocumentoComplementar(idDocumentoComplementar);
	}

	public ContratoBD consultarContratoAssociadoParalisacao(Long idParalisacao) {

		return getContratoDAO().consultarContratoAssociadoParalisacao(idParalisacao);
	}
	
	private ContratoDAO getContratoDAO() {
		return jdbi.onDemand(ContratoDAO.class);
	}

	private MedicaoDAO getMedicaoDAO() {
		return jdbi.onDemand(MedicaoDAO.class);
	}

	private AnotacaoRegistroRespTecnicoDAO getAnotacaoDAO() {
		return jdbi.onDemand(AnotacaoRegistroRespTecnicoDAO.class);
	}

	private ContratoResponsavelTecnicoSocialDAO getContratoRTSocialDAO() {
		return jdbi.onDemand(ContratoResponsavelTecnicoSocialDAO.class);
	}

	private DocumentoComplementarDAO getDocComplementarDAO() {
		return jdbi.onDemand(DocumentoComplementarDAO.class);
	}

	/**
	 * Obtém a qtde de dias (Hoje - data fim) da última Medição enviada para
	 * Convenente ou que Já passou por esse estado, caso não tenha nenhuma medição
	 * que já tenha sido enviada para Convenete, então obtém a qtde de dias (Hoje -
	 * data de emissão) do doc complementar do tipo OS, se não tiver cadastrado OS
	 * para o contrato Levanta exceção.
	 * 
	 * @param idContrato
	 * @return
	 */
	public AndamentoContratoDTO consultarAndamentoContrato(Long idContrato) {

		AndamentoContratoDTO andamentoContrato = new AndamentoContratoDTO();

		MedicaoBD ultimaMedicao = getMedicaoDAO().consultarUltimaMedicao(idContrato);

		if (ultimaMedicao != null) {
			andamentoContrato.setIdUltimaMedicao(ultimaMedicao.getId());
			andamentoContrato.setSequencialUltimaMedicao(ultimaMedicao.getNrSequencial());
		}

		Optional<MedicaoBD> ultimaMedicaoPublicaEmpresa = getMedicaoDAO()
				.consultarUltimaMedicaoPublicaEmpresa(idContrato);

		if (ultimaMedicaoPublicaEmpresa.isPresent()) {
			andamentoContrato.setQtdeDiasSemMedicao(
					diferenca(ultimaMedicaoPublicaEmpresa.get().getDtFim(), LocalDate.now(), ChronoUnit.DAYS));
		} else {
			getDocComplementarDAO().consultarDocumentoComplementarOSParaContrato(idContrato)
					.ifPresent(os -> andamentoContrato
							.setQtdeDiasSemMedicao(diferenca(os.getDtEmissao(), LocalDate.now(), ChronoUnit.DAYS)));
		}

		andamentoContrato.setAtrasado(
				andamentoContrato.getQtdeDiasSemMedicao() != null && andamentoContrato.getQtdeDiasSemMedicao() > 30);

		return andamentoContrato;
	}

	/**
	 * Insere a estrutura de ContratoBD e os Itens Medição a serem medidos no
	 * contrato.
	 * 
	 * 
	 * @param contratoSiconv
	 * @param listaEventoFrenteObraVrplDTO
	 * @param transaction
	 * @return
	 */
	public ContratoBD incluir(ContratoSiconvDTO contratoSiconv, Handle transaction) {
		
		if(BooleanUtils.isTrue(contratoSiconv.getInAcompEvento())) {
			
			List<EventoFrenteObraTotalizadoDTO> listaEventoFrenteObraVrplDTO = 
					itemMedicaoBC.getEventoFO(contratoSiconv.getId());
			
			if (!listaEventoFrenteObraVrplDTO.isEmpty()) {

				ContratoBD contratoBDPersistir = this.incluirContrato(contratoSiconv, transaction);

				// Insere os Itens de Medição.
				this.criarItemMedicao(contratoBDPersistir, listaEventoFrenteObraVrplDTO, transaction);

				return contratoBDPersistir;
			} else {
				throw new MedicaoRestException(MessageKey.ITENS_MEDICOES_INEXISTENTES);
			}
			
		} else {
			
			List<ServicoFrenteObraTotalizadoDTO> listaMacroServicoFrenteObraVrplDTO = 
					itemMedicaoBC.getMacroServicoFO(contratoSiconv.getId());
			
			if (!listaMacroServicoFrenteObraVrplDTO.isEmpty()) {

				ContratoBD contratoBDPersistir = this.incluirContrato(contratoSiconv, transaction);

				// Insere os Itens de Medição (BM)
				this.criarItemMedicaoBM(contratoBDPersistir, listaMacroServicoFrenteObraVrplDTO, transaction);

				return contratoBDPersistir;
			} else {
				throw new MedicaoRestException(MessageKey.ITENS_MEDICOES_BM_INEXISTENTES);
			}
		}

	}


	private ContratoBD incluirContrato(ContratoSiconvDTO contratoSiconv, Handle transaction) {
		ContratoBD contratoBDPersistir = new ContratoBD(contratoSiconv.getId(), contratoSiconv.getInSocial(),
				contratoSiconv.getCnpj(), contratoSiconv.getPropostaFk(), contratoSiconv.getInAcompEvento());
		
		// Insere o Contrato
		contratoBDPersistir = transaction.attach(ContratoDAO.class).inserir(contratoBDPersistir);
		return contratoBDPersistir;
	}

	/**
	 * Criar os Itens de Medição.
	 * 
	 * @param contratoBD
	 * @param listaEventoFrenteObraVrplDTO
	 * @param transaction
	 */
	private void criarItemMedicao(ContratoBD contratoBD,
			List<EventoFrenteObraTotalizadoDTO> listaEventoFrenteObraVrplDTO, Handle transaction) {

		LOGGER.debug("--------------- Antes da criação dos Itens da Medição-------------------");
		listaEventoFrenteObraVrplDTO.forEach(eventoFrenteObra -> {
			ItemMedicaoBD itemMedicaoBD = new ItemMedicaoBD(eventoFrenteObra.getIdSubmetaVrpl(),
					eventoFrenteObra.getIdEvento(), eventoFrenteObra.getIdFrenteObra(), contratoBD.getId(),
					eventoFrenteObra.getTotalEvento());
			LOGGER.debug("--------------- Criação do Item da Medição ------------------------");

			transaction.attach(ItemMedicaoDAO.class).inserir(itemMedicaoBD);
		});

	}
	
	/**
	 * Criar os Itens de Medição - Boletim de Medição.
	 * 
	 * @param contratoBD
	 * @param listaServicoFrenteObraVrplDTO
	 * @param transaction
	 */
	private void criarItemMedicaoBM(ContratoBD contratoBD,
			List<ServicoFrenteObraTotalizadoDTO> listaServicoFrenteObraVrplDTO, Handle transaction) {

		LOGGER.debug("--------------- Antes da criação dos Itens da Medição (BM) -------------------");
		listaServicoFrenteObraVrplDTO.forEach(servicoFrenteObra -> {
			ItemMedicaoBMBD itemMedicaoBMBD = new ItemMedicaoBMBD(
					contratoBD.getId(),
					servicoFrenteObra.getIdSubmetaVrpl(),
					servicoFrenteObra.getIdFrenteObra(), 
					servicoFrenteObra.getIdServico(),
					servicoFrenteObra.getQtdeServico(),
					servicoFrenteObra.getVlPrecoUnitarioLicitado());
			LOGGER.debug("--------------- Criação do Item da Medição (BM) ------------------------");

			transaction.attach(ItemMedicaoDAO.class).inserirItemBM(itemMedicaoBMBD);
		});

	}
	
	public void excluirEstruturaContrato (Long idContrato, Handle transaction){
		
		if (Objects.isNull(idContrato)){
			throw new MedicaoRestException(MessageKey.CONTRATO_INEXISTENTE, Status.NOT_FOUND.getStatusCode());
		} else {
			LOGGER.debug("--------------- Início Exclusão Estrutura de Contrato -------------------");
			ContratoBD contratoBD = transaction.attach(ContratoDAO.class).consultarContrato(idContrato); 	
			
			if(contratoBD.isInAcompanhamentoEventos()) {
				LOGGER.debug("--------------- Exclusão de Itens de Medição para PLE -------------------");			
				transaction.attach(ItemMedicaoDAO.class).excluirItemMedicaoPLEPorContrato(idContrato);
			}else {			
				LOGGER.debug("--------------- Exclusão dos Itens de Medição para BM -------------------");
				transaction.attach(ItemMedicaoDAO.class).excluirItemMedicaoBMPorContrato(idContrato);
			}
			
			LOGGER.debug("--------------- Exclusão Histórico da Medição -------------------");		
			transaction.attach(HistoricoMedicaoDAO.class).excluir(idContrato);
			
			LOGGER.debug("--------------- Exclusão Contrato -------------------");
			transaction.attach(ContratoDAO.class).excluir(idContrato);
			
			LOGGER.debug("--------------- Fim Exclusão Estrutura de Contrato -------------------");					
		}
	}
	
	public boolean isContratoParalisado(Long idContratoSiconv) {
		return dao.get(ParalisacaoDAO.class).existeParalisacaoEmAberto(idContratoSiconv);
	}
}
