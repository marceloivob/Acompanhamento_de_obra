package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.business;

import static com.google.common.collect.Sets.difference;
import static java.util.Collections.sort;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.groups.ConvertGroup;

import org.apache.log4j.Logger;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.dao.DocumentoComplementarDAO;
import br.gov.planejamento.siconv.med.configuracao.paralisacao.dao.ParalisacaoDAO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao.ContratoResponsavelTecnicoSocialDAO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao.ResponsavelTecnicoDAO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.database.ContratoResponsavelTecnicoSocialBD;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.database.ResponsavelTecnicoBD;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.database.SubmetaResponsavelTecnicoSocialBD;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.AtividadeRegistroProfissionalEnum;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.ContratoResponsavelTecnicoSocialDTO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.ContratoResponsavelTecnicoSocialDTO.ExecucaoGroup;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.ContratoResponsavelTecnicoSocialDTO.FiscalizacaoGroup;
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
import br.gov.planejamento.siconv.med.infra.validation.InsertGroup;
import br.gov.planejamento.siconv.med.infra.validation.UpdateGroup;
import br.gov.planejamento.siconv.med.integration.ceph.CephActions;
import br.gov.planejamento.siconv.med.integration.dto.UsuarioDTO;
import br.gov.planejamento.siconv.med.medicao.business.SubmetaBC;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;


@ApplicationScoped
public class ContratoResponsavelTecnicoSocialBC {

	private static final Logger LOGGER = Logger.getLogger(ContratoResponsavelTecnicoSocialBC.class);

	@Inject
	private DAOFactory dao;
	
	@Inject
	private Jdbi jdbi;

	@Inject
	private CephActions cephActions;

	@Inject
	private ContratosBC contratoBC;
	
	@Inject
	private SubmetaBC submetaBC;
	
	@Inject
	private ResponsavelTecnicoBC responsavelTecnicoBC;

	@Inject
	private Validator validator;

	/**
	 * Consulta todos contratos responsaveis tecnicos sociais
	 * 
	 * @param idContratoSiconv
	 * @return
	 */
	public List<ContratoResponsavelTecnicoSocialDTO> listarResponsavelTecnicoSocialPorContrato(Long idContratoSiconv) {

		List<ContratoResponsavelTecnicoSocialDTO> lista = dao.get(ContratoResponsavelTecnicoSocialDAO.class)
				.listarResponsavelTecnicoSocialPorContrato(idContratoSiconv);

		lista.forEach(contratoRTSocial -> decorateContratoResponsavelTecnicoDTO(contratoRTSocial, idContratoSiconv));

		return lista;
	}

	private ContratoResponsavelTecnicoSocialDTO decorateContratoResponsavelTecnicoDTO(
			ContratoResponsavelTecnicoSocialDTO contratoRTSocial, Long idContratoSiconv) {

		ContratoBD contratoMedicao = dao.get(ContratoDAO.class).consultarContratoPorContratoFK(idContratoSiconv);

		// Responsavel Tecnico
		UsuarioDTO usuario = responsavelTecnicoBC.consultarUsuario(contratoRTSocial.getResponsavelTecnico().getCpf(),
				contratoRTSocial.getTipo(), contratoMedicao.getContratoFk(), false);

		if (usuario != null) {
			contratoRTSocial.getResponsavelTecnico().setNome(usuario.getNome());
			contratoRTSocial.getResponsavelTecnico().setEmail(usuario.getEmail());
		}

		// Submetas
		Map<Long, SubmetaVrplDTO> submetasDoContrato = submetaBC.listarSubmetasPorContrato(idContratoSiconv).stream()
				.collect(toMap(SubmetaVrplDTO::getId, Function.identity()));
		contratoRTSocial.getSubmetas().replaceAll(submeta -> submetasDoContrato.getOrDefault(submeta.getId(), submeta));
		sort(contratoRTSocial.getSubmetas(), SubmetaVrplDTO.ORDENACAO_PADRAO);

		// URL Curriculo
		contratoRTSocial.setUrlArquivo(cephActions.getPresignedUrl(contratoRTSocial.getCodigoCephArquivo()));
		
		//Atibui o id do Contrato Siconv ao ContratoResponsavelTecnicoSocial
		contratoRTSocial.setIdContratoSiconv(idContratoSiconv);

		return contratoRTSocial;
	}

	/**
	 * 
	 * @param idContratoSiconv
	 * @param cpf
	 * @param tipo
	 * @return
	 */
	public ResponsavelTecnicoElegivelDTO consultarResponsavelTecnicoElegivel(Long idContratoSiconv, String cpf,
			String tipo) {

		return this.consultarResponsavelTecnicoSocial(idContratoSiconv, cpf, tipo);
	}
	
	/**
	 * Salvar contrato responsavel tecnico social
	 * 
	 * @param rtSocialDTO
	 * @param idContratoSiconv
	 */
	public void salvar(@Valid @ConvertGroup(to = InsertGroup.class) ContratoResponsavelTecnicoSocialDTO rtSocialDTO, Long idContratoSiconv) {

		validarOrgaoResponsavelTecnicoSocial(rtSocialDTO);
		
		if(rtSocialDTO.getIdSubmetas().isEmpty()) {
			throw new MedicaoRestException(MessageKey.ERRO_SUBMETA_NAO_SELECIONADA_PARA_RT_SOCIAL);
		}

		ContratoSiconvDTO contratoSiconv = contratoBC.consultarContratoPorId(idContratoSiconv);
		

		if (Optional.ofNullable(contratoSiconv).isPresent()) {
			if (!contratoSiconv.getInSocial()) {
				throw new MedicaoRestException(MessageKey.ERRO_EDITAR_RT_ART_RRT_CONTRATO_SOCIAL);
			}

			ContratoBD contratoBD = dao.get(ContratoDAO.class).consultarContratoPorContratoFK(idContratoSiconv);

			rtSocialDTO.setAtividade(AtividadeRegistroProfissionalEnum.SOC);

			// Já existe contrato cadastrado no módulo
			if (Optional.ofNullable(contratoBD).isPresent()) {

				rtSocialDTO.setMedContratoFk(contratoBD.getId());
				dao.getJdbi()
						.useTransaction(transaction -> this.salvarContratoResponsavelTecnicoSocial(rtSocialDTO, idContratoSiconv, transaction));

			} else {// Insere a primeira Configuração - não existe contrato configurado
					// Se não houver ContratoBD Associado insere toda a estrutura Contrato

				dao.getJdbi().useTransaction(transaction -> {

					LOGGER.debug("--------------- Inicio da Transação -------------------");

					LOGGER.debug("--------------- Criação do Medição Contrato -------------------");
					
					ContratoBD contratoBDPersistir = contratoBC.incluir(contratoSiconv, transaction);
					
					rtSocialDTO.setMedContratoFk(contratoBDPersistir.getId());
					this.salvarContratoResponsavelTecnicoSocial(rtSocialDTO, idContratoSiconv, transaction);

				});
			}
		} else {
			throw new MedicaoRestException(MessageKey.CONTRATO_INEXISTENTE);
		}
	}
	
    
    private void validarSeContratoDiferenteDeSocial(ContratoBD contratoMedicao) {
		if (!contratoMedicao.isInSocial()) {
			throw new MedicaoRestException(MessageKey.ERRO_EDITAR_RT_ART_RRT_CONTRATO_SOCIAL);
		}
	}
	
	/**
	 * Alterar contrato responsável tecnico social
	 * 
	 * @param rtSocialDTO
	 * @param idResponsavelContrato
	 */
	public void alterar(@Valid @ConvertGroup(to = UpdateGroup.class) ContratoResponsavelTecnicoSocialDTO rtSocialInputDTO) {

		validarOrgaoResponsavelTecnicoSocial(rtSocialInputDTO);
		
		ContratoResponsavelTecnicoSocialDTO rtSocial = this.consultarContratoResponsavelTecnicoSocialDTO(rtSocialInputDTO.getId());
	
		this.validarAlteracao(rtSocialInputDTO);
		
        jdbi.useTransaction(transaction -> {

        	ContratoResponsavelTecnicoSocialDAO contratoRtSocialDAO = transaction.attach(ContratoResponsavelTecnicoSocialDAO.class);

        	// Atualizar RT
        	ContratoBD contratoMedicao = transaction.attach(ContratoDAO.class).consultarContrato(rtSocial.getMedContratoFk());
        	this.validarSeContratoDiferenteDeSocial(contratoMedicao);
        	this.salvarResponsavelTecnico(rtSocialInputDTO, contratoMedicao.getContratoFk(), false, transaction);
        	
        	// CURRICULO 
            if (!isEmpty(rtSocialInputDTO.getNomeArquivo())) {
            	rtSocialInputDTO
                        .setCodigoCephArquivo(cephActions.uploadFile(rtSocialInputDTO.getArquivo(), rtSocialInputDTO.getNomeArquivo()));

            } else {
            	rtSocialInputDTO.setNomeArquivo(rtSocial.getNomeArquivo());
            	rtSocialInputDTO.setCodigoCephArquivo(rtSocial.getCodigoCephArquivo());
            }

            // ALTERA AS SUBMETAS
            Set<Long> idSubmetasRtSocialInput = rtSocialInputDTO.getIdSubmetas();
            Set<Long> idSubmetasRtSocial = rtSocial.getIdSubmetas();
            
            validarSubmetasVinculadasContrato(idSubmetasRtSocialInput, contratoMedicao.getContratoFk());

            // Insere as novas submetas
            transaction.attach(ContratoResponsavelTecnicoSocialDAO.class)
				.inserirResponsavelTecnicoSocialSubmeta(difference(idSubmetasRtSocialInput, idSubmetasRtSocial).stream()
					.map(idSubmeta -> new SubmetaResponsavelTecnicoSocialBD(idSubmeta, rtSocialInputDTO.getId()))
					.collect(toList()));
            
            // deleta as submetas antigas
            transaction.attach(ContratoResponsavelTecnicoSocialDAO.class)
				.deletarResponsavelTecnicoSocialSubmeta(difference(idSubmetasRtSocial, idSubmetasRtSocialInput).stream()
				.map(idSubmeta -> new SubmetaResponsavelTecnicoSocialBD(idSubmeta, rtSocial.getId()))
				.collect(toList()));
            
            // ALTERA O CONTRATO RESP TECNICO SOCIAL
            contratoRtSocialDAO.alterar(rtSocialInputDTO.converterDTOParaBD());
        });
	}

	/**
	 * @param rtSocialInputDTO
	 */
	private void validarOrgaoResponsavelTecnicoSocial(ContratoResponsavelTecnicoSocialDTO rtSocialInputDTO) {
		Set<ConstraintViolation<ContratoResponsavelTecnicoSocialDTO>> violations = null;

		if (rtSocialInputDTO.getTipo() == TipoResponsavelTecnicoEnum.EXE) {
			violations = validator.validate(rtSocialInputDTO, ExecucaoGroup.class);

		} else if (rtSocialInputDTO.getTipo() == TipoResponsavelTecnicoEnum.FIS) {
			violations = validator.validate(rtSocialInputDTO, FiscalizacaoGroup.class);
		}

		if (!isEmpty(violations)) {
			throw new ConstraintViolationException(violations);
		}
	}

	/**
	 * @param rtSocialInputDTO
	 */
	private void validarAlteracao(ContratoResponsavelTecnicoSocialDTO rtSocialInputDTO) {
		if (rtSocialInputDTO.getDtInativacao() != null) {
			throw new MedicaoRestException(MessageKey.ERRO_EDITAR_RESP_TEC_INATIVO);
		}
		
		if (this.existeSubmetaAssinada(rtSocialInputDTO.getId())) {
            throw new MedicaoRestException(MessageKey.ERRO_EDITAR_RT_POSSUI_SUBMETA_ASSINADA);
        }
		
		if(rtSocialInputDTO.getIdSubmetas().isEmpty()) {
			throw new MedicaoRestException(MessageKey.ERRO_SUBMETA_NAO_SELECIONADA_PARA_RT_SOCIAL);
		}
	}
	
	public ContratoResponsavelTecnicoSocialDTO consultarContratoResponsavelTecnicoSocialPorId(
			Long idResponsavelContrato) {

		ContratoResponsavelTecnicoSocialDTO rtSocial = this
				.consultarContratoResponsavelTecnicoSocialDTO(idResponsavelContrato);

		ContratoBD contratoMedicao = dao.get(ContratoDAO.class).consultarContrato(rtSocial.getMedContratoFk());

		this.decorateContratoResponsavelTecnicoDTO(rtSocial, contratoMedicao.getContratoFk());

		return rtSocial;
	}

	private boolean permiteExcluirEstruturaContratoRTSocial(Long idMedContratoFk) {
		return !dao.get(ContratoResponsavelTecnicoSocialDAO.class).existeRespTecnicoSocialContrato(idMedContratoFk)
				&& !dao.get(DocumentoComplementarDAO.class).existeDocumentoComplementarContrato(idMedContratoFk)
				&& !dao.get(ParalisacaoDAO.class).existeParalisacaoContrato(idMedContratoFk);
	}
	
	
	public void excluir(Long idResponsavelContrato) {
		ContratoResponsavelTecnicoSocialDTO rtSocial = consultarContratoResponsavelTecnicoSocialDTO(
				idResponsavelContrato);

		this.validarExclusao(rtSocial);

		jdbi.useTransaction(handle -> {

			ContratoResponsavelTecnicoSocialDAO contratoRTSocialDAO = handle
					.attach(ContratoResponsavelTecnicoSocialDAO.class);

			contratoRTSocialDAO.excluirSubmetaPorIdRTContratoSocial(idResponsavelContrato);
			contratoRTSocialDAO.excluir(idResponsavelContrato);
			
			if(permiteExcluirEstruturaContratoRTSocial(rtSocial.getMedContratoFk())) {
					contratoBC.excluirEstruturaContrato(rtSocial.getMedContratoFk(), handle);                   					
			}
		});
	}

	private void validarExclusao(ContratoResponsavelTecnicoSocialDTO rtSocial) {
		if (this.existeSubmetaAssinada(rtSocial.getId())) {
			throw new MedicaoRestException(MessageKey.ERRO_RESPONSAVEL_TECNICO_SOCIAL_POSSUI_SUBMETA_ASSINADA);
		}
	}

	public void inativar(Long idResponsavelContrato) {

		ContratoResponsavelTecnicoSocialDTO crtRespTecSocial = consultarContratoResponsavelTecnicoSocialDTO(
				idResponsavelContrato);

		validarInativacao(crtRespTecSocial);

		getContratoResponsavelTecnicoSocialDAO().inativar(crtRespTecSocial.converterDTOParaBD());
	}

	private void validarInativacao(ContratoResponsavelTecnicoSocialDTO crtRespTecSocial) {

		if (crtRespTecSocial.getDtInativacao() != null) {
			throw new MedicaoRestException(MessageKey.ERRO_INATIVAR_RESP_TEC_SOCIAL_INATIVO);
		}
	}

	/**
	 * Inclui Responsável Técnico e vinculos
	 * 
	 * @param rtSocial
	 * @param idContratoSiconv
	 * @param transaction
	 * @return
	 */
	private ContratoResponsavelTecnicoSocialBD salvarContratoResponsavelTecnicoSocial(
			ContratoResponsavelTecnicoSocialDTO rtSocialDTO, Long idContratoSiconv, Handle transaction) {

		ResponsavelTecnicoBD responsavelTecnicoBD = this.salvarResponsavelTecnico(rtSocialDTO, idContratoSiconv, true, transaction);
		rtSocialDTO.getResponsavelTecnico().setId(responsavelTecnicoBD.getId());

		rtSocialDTO.setCodigoCephArquivo(cephActions.uploadFile(rtSocialDTO.getArquivo(), rtSocialDTO.getNomeArquivo()));

		ContratoResponsavelTecnicoSocialBD contratoResponsavelTecnicoBD = rtSocialDTO.converterDTOParaBD();

		contratoResponsavelTecnicoBD = transaction.attach(ContratoResponsavelTecnicoSocialDAO.class)
				.inserir(contratoResponsavelTecnicoBD);
		rtSocialDTO.setId(contratoResponsavelTecnicoBD.getId());

		this.incluirSubmetaResponsavelTecnicoSocial(idContratoSiconv, contratoResponsavelTecnicoBD.getId(), rtSocialDTO, transaction);

		return contratoResponsavelTecnicoBD;
	}

	private ResponsavelTecnicoBD salvarResponsavelTecnico(
			ContratoResponsavelTecnicoSocialDTO rtSocial, 
			Long idContratoSiconv,
			boolean validaUsuario,
			Handle transaction) {
				
		ResponsavelTecnicoDTO responsavelTecnicoDTO = new ResponsavelTecnicoDTO();
			
		this.consultarUsuarioResponsavelTecnico(
				rtSocial.getResponsavelTecnico().getCpf(), 
				rtSocial.getTipo().getCodigo(), 
				idContratoSiconv, responsavelTecnicoDTO, validaUsuario);
		
		responsavelTecnicoDTO = transaction.attach((ResponsavelTecnicoDAO.class))
				.consultarResponsavelTecnicoPorCpf(rtSocial.getResponsavelTecnico().getCpf());
		
		ResponsavelTecnicoBD responsavelTecnicoBD;

		if (Optional.ofNullable(responsavelTecnicoDTO).isPresent()) {

			// Para alteração de ContratoSocial
			if (rtSocial.getId() != null) {
				ContratoResponsavelTecnicoSocialDTO rtSocialBD = this.consultarContratoResponsavelTecnicoSocialDTO(rtSocial.getId());
				if(!rtSocialBD.getResponsavelTecnico().getId().equals(responsavelTecnicoDTO.getId())) {
					throw new MedicaoRestException(
							new Message(MessageKey.ERRO_ALTERAR_RESPONSAVEL_TECNICO_NAO_ASSOCIADO_CONTRATO)); 
				}
			} else {//Para inclusão de ContratoSocial
				validaResponsavelTecnicoJaIncluidoNoContrato(responsavelTecnicoDTO.getId(), rtSocial.getMedContratoFk());
			}
			
			LOGGER.debug("--------------- Antes da alteração do Responsável Técnico - Início -------------------");
			responsavelTecnicoBD = responsavelTecnicoDTO.converterParaBD();
			responsavelTecnicoBD.setTelefone(rtSocial.getResponsavelTecnico().getTelefone());
			responsavelTecnicoBD.setVersao(rtSocial.getResponsavelTecnico().getVersao());
			transaction.attach(ResponsavelTecnicoDAO.class).alterar(responsavelTecnicoBD);
			LOGGER.debug("--------------- Depois da alteração do Responsável Técnico - Fim  -------------------");
			
		} else {
			LOGGER.debug("--------------- Antes da inclusão do Responsável Técnico - Início -------------------");
			responsavelTecnicoBD = rtSocial.getResponsavelTecnico().converterParaBD();
			responsavelTecnicoBD = transaction.attach(ResponsavelTecnicoDAO.class)
					.inserir(responsavelTecnicoBD);
			LOGGER.debug("--------------- Depois da inclusão do Responsável Técnico - Fim  -------------------");
		}

		return responsavelTecnicoBD;
	}

	private void incluirSubmetaResponsavelTecnicoSocial(Long idContratoSiconv, Long idMedContratoRTSocial,
			@Valid @ConvertGroup(to = InsertGroup.class) ContratoResponsavelTecnicoSocialDTO rtSocial,
			Handle transaction) {

		Set<Long> idSubmetas = rtSocial.getIdSubmetas();
		validarSubmetasVinculadasContrato(idSubmetas, idContratoSiconv);

		transaction.attach(ContratoResponsavelTecnicoSocialDAO.class)
				.inserirResponsavelTecnicoSocialSubmeta(idSubmetas.stream()
						.map(idSubmeta -> new SubmetaResponsavelTecnicoSocialBD(idSubmeta, idMedContratoRTSocial))
						.collect(toList()));
	}
	
	private void validarSubmetasVinculadasContrato(Set<Long> idSubmetas,
            Long idContratoSiconv) {

        Set<Long> idSubmetasContrato = consultarSubmetasContrato(idContratoSiconv).keySet();
        
        if (!idSubmetasContrato.containsAll(idSubmetas)) {
            throw new MedicaoRestException(MessageKey.ERRO_RT_SOCIAL_SUBMETA_NAO_VINCULADA_CONTRATO);
        }
    }
	
	private Map<Long, SubmetaVrplDTO> consultarSubmetasContrato(Long idContratoSiconv) {
        return submetaBC.listarSubmetasPorContrato(idContratoSiconv).stream()
                .collect(toMap(SubmetaVrplDTO::getId, Function.identity()));
    }
	
	private boolean validaResponsavelTecnicoJaIncluidoNoContrato(
			Long responsavelTecnicoId, Long medContratoFk) {

		Optional<ContratoResponsavelTecnicoSocialDTO> responsavelTecnicoNoContrato = getContratoResponsavelTecnicoSocial(responsavelTecnicoId,
				medContratoFk);
		
		// Verifica se o CPF já está cadastrado para o contrato
		if ( responsavelTecnicoNoContrato.isPresent()) {
			if (responsavelTecnicoNoContrato.get().getDtInativacao() == null) {
				throw new MedicaoRestException(new Message(MessageKey.ERRO_RESPONSAVEL_TECNICO_JA_EXISTENTE));
			} else {
				throw new MedicaoRestException(new Message(MessageKey.ERRO_RESPONSAVEL_TECNICO_JA_EXISTENTE_INCLUSIVE_INATIVO));
			}
		}
		
		return false;
	}

	/**
	 * @param responsavelTecnicoId
	 * @param medContratoFk
	 * @return
	 */
	private Optional<ContratoResponsavelTecnicoSocialDTO> getContratoResponsavelTecnicoSocial(Long responsavelTecnicoId, Long medContratoFk) {
		return getContratoResponsavelTecnicoSocialDAO()
				.consultarContratoResponsavelTecnicoSocialPorIdRtAtivoNoContrato(
						responsavelTecnicoId, medContratoFk);
	}
	
	private boolean existeSubmetaAssinada(Long idContratoResponsavelTecnicoSocial) {
        return this.getContratoResponsavelTecnicoSocialDAO().isContratoSocialAssinadoPeloResponsavelTecnico(idContratoResponsavelTecnicoSocial);
    }

	private ContratoResponsavelTecnicoSocialDTO consultarContratoResponsavelTecnicoSocialDTO(
			Long idResponsavelContrato) {
		return getContratoResponsavelTecnicoSocialDAO()
				.consultarContratoResponsavelTecnicoSocialPorId(idResponsavelContrato)
				.orElseThrow(() -> new MedicaoRestException(MessageKey.ERRO_RESPONSAVEL_TECNICO_SOCIAL_INEXISTENTE));
	}
	
	private ResponsavelTecnicoElegivelDTO consultarResponsavelTecnicoSocial(Long idContratoSiconv, String cpf,
			String tipo) {
		
		ResponsavelTecnicoElegivelDTO respTecnico = new ResponsavelTecnicoElegivelDTO();

		ResponsavelTecnicoDTO rt = dao.get(ResponsavelTecnicoDAO.class).consultarResponsavelTecnicoPorCpf(cpf);
		
		ContratoBD contratoBD = dao.get(ContratoDAO.class).consultarContratoPorContratoFK(idContratoSiconv);

		// Se RT e Contrato Siconv existirem no Medição
		if (rt != null && contratoBD != null) {
			Optional<ContratoResponsavelTecnicoSocialDTO> contratoRTSocialDTO = getContratoResponsavelTecnicoSocial(rt.getId(),
					contratoBD.getId());
			
			// Verifica se o CPF é RT no CTEF 
			if(contratoRTSocialDTO.isPresent()) {
				
				// Se for do mesmo tipo
				if(contratoRTSocialDTO.get().getTipo().getCodigo().equals(tipo)) {
					
					// Se for um RT ativo associa ao contrato, se for inativo, levanta um erro
					if (contratoRTSocialDTO.get().getDtInativacao() == null) {
						respTecnico.setIdContratoResponsavelTecnicoSocial(contratoBD.getId());
					} else {
						throw new MedicaoRestException(MessageKey.ERRO_EDITAR_RESP_TEC_INATIVO);
					}
				} else {
					throw new MedicaoRestException(new Message(MessageKey.ERRO_RESPTECNICOSOCIAL_CADASTRADO_TIPO_DIFERENTE, new String[]{TipoResponsavelTecnicoEnum.fromCodigo(tipo).getDescricao(), contratoRTSocialDTO.get().getTipo().getDescricao()}));
				}
			}
		}
		
		if (rt == null) {
			rt = new ResponsavelTecnicoDTO();
		}
		
		consultarUsuarioResponsavelTecnico(cpf, tipo, idContratoSiconv, rt, true);
		this.decorateRTElegivel(respTecnico, rt);
		
		return respTecnico;
	}

	private void decorateRTElegivel(ResponsavelTecnicoElegivelDTO rtElegivel, ResponsavelTecnicoDTO rt) {
		rtElegivel.setId(rt.getId());
		rtElegivel.setCpf(rt.getCpf());
		rtElegivel.setEmail(rt.getEmail());
		rtElegivel.setNome(rt.getNome());
		rtElegivel.setTelefone(rt.getTelefone());
		rtElegivel.setVersao(rt.getVersao());
	}

	/**
	 * @param numeroCPF
	 * @param tipoRespTec
	 * @param contratoFk
	 * @param respTecnico
	 */
	private ResponsavelTecnicoDTO consultarUsuarioResponsavelTecnico(String numeroCPF, 
			String tipoRespTec, Long contratoFk, ResponsavelTecnicoDTO respTecnico, 
			boolean validaUsuario) {

		respTecnico.setCpf(numeroCPF);

		UsuarioDTO usuario = this.responsavelTecnicoBC.consultarUsuario(numeroCPF, TipoResponsavelTecnicoEnum.fromCodigo(tipoRespTec),
				contratoFk, validaUsuario);

		if (usuario != null) {
			respTecnico.setNome(usuario.getNome());
			respTecnico.setEmail(usuario.getEmail());
		}

		return respTecnico;
	}

	private ContratoResponsavelTecnicoSocialDAO getContratoResponsavelTecnicoSocialDAO() {
		return jdbi.onDemand(ContratoResponsavelTecnicoSocialDAO.class);
	}
	
	public boolean existeRTSocialAtivo(Long idContrato, String tipo) {
        return getContratoResponsavelTecnicoSocialDAO().existeRTSocialAtivo(idContrato, tipo);
    }

}
