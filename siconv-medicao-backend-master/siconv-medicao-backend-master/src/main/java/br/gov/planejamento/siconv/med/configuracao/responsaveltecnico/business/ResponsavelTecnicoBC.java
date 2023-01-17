package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.business;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.normalizeSpace;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.jdbi.v3.core.Handle;

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
import br.gov.planejamento.siconv.med.contrato.dao.ContratoDAO;
import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.contrato.entity.dto.ContratoSiconvDTO;
import br.gov.planejamento.siconv.med.infra.database.DAOFactory;
import br.gov.planejamento.siconv.med.infra.exception.MedicaoRestException;
import br.gov.planejamento.siconv.med.infra.message.Message;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.integration.UsuarioConsumer;
import br.gov.planejamento.siconv.med.integration.contratos.ContratosGrpcConsumer;
import br.gov.planejamento.siconv.med.integration.dto.UsuarioDTO;

@ApplicationScoped
public class ResponsavelTecnicoBC {

	private static final Logger LOGGER = Logger.getLogger(ResponsavelTecnicoBC.class);

	@Inject
	private DAOFactory dao;
	
	@Inject
	private UsuarioConsumer usuarioConsumer;
	
	@Inject
	private ContratosGrpcConsumer contratosConsumer;
	
	@Inject
	private ContratosBC contratoBC;

	/**
	 * Consulta Todos os Responsáveis Técnicos vinculados ao contrato
	 * 
	 * @param idMedContrato
	 */
	public List<ResponsavelTecnicoDTO> listarResponsavelTecnicoPorContrato(Long idMedContrato) {
		List<ResponsavelTecnicoDTO> listaResponsavelTecnico = dao.get(ResponsavelTecnicoDAO.class)
				.listarResponsavelTecnicoPorContrato(idMedContrato);
		List<ResponsavelTecnicoDTO> listaRetorno = new ArrayList<>();
		for (ResponsavelTecnicoDTO responsavel : listaResponsavelTecnico) {
			RegistroProfissionalDTO registro = responsavel.obterRegistroProfissional(idMedContrato);
			ContratoResponsavelTecnicoDTO contrato = null;
			if (registro != null) {
				contrato = responsavel.obterRegistroProfissional(idMedContrato).obterContratoVinculado(idMedContrato);

				UsuarioDTO usuario = this.consultarUsuario(responsavel.getCpf(), contrato.getTipo(), idMedContrato,
						false);

				if (usuario != null) {
					responsavel.setNome(usuario.getNome());
				}

				listaRetorno.add(responsavel);
			}

		}
		return listaRetorno;
	}

	/**
	 * Consulta Todos os Responsáveis Técnicos disponíveis para combo do ART para um
	 * tipo
	 * 
	 * @param idMedContrato
	 * @param tipo
	 */
	public List<ResponsavelTecnicoDTO> listarResponsavelTecnicoPorContratoTipo(Long idMedContrato, String tipo) {
		List<ResponsavelTecnicoDTO> listaResponsavelTecnico = dao.get(ResponsavelTecnicoDAO.class)
				.listarResponsavelTecnicoPorContratoTipo(idMedContrato, tipo);
		List<ResponsavelTecnicoDTO> listaRetorno = new ArrayList<>();
		for (ResponsavelTecnicoDTO responsavel : listaResponsavelTecnico) {
			UsuarioDTO usuario = this.consultarUsuario(responsavel.getCpf(),
					TipoResponsavelTecnicoEnum.fromCodigo(tipo), idMedContrato, false);
			if (usuario != null && usuario.getNome() != null && !usuario.getNome().equals("")) {
				responsavel.setNome(usuario.getNome());
				listaRetorno.add(responsavel);
			}
		}
		return listaRetorno.stream().sorted(Comparator.comparing(ResponsavelTecnicoDTO::getNome))
				.collect(Collectors.toList());
	}

	/**
	 * Incluir/Alterar
	 * 
	 * @param idMedContrato
	 * @param tipo
	 */
	public void salvar(ResponsavelTecnicoDTO responsavelTecnicoDTO, Long contratoFk) {
		Optional<ContratoSiconvDTO> contrato = contratosConsumer.consultarContratoPorId(contratoFk); 
		
		contrato.ifPresentOrElse( contratoSiconvDTO -> salvarRegistro(contratoSiconvDTO, responsavelTecnicoDTO, contratoFk) , 
				() -> new MedicaoRestException(MessageKey.CONTRATO_INEXISTENTE));
		
	}

	private void salvarRegistro(ContratoSiconvDTO contratoSiconvDTO, ResponsavelTecnicoDTO responsavelTecnicoDTO, Long contratoFk) {
		this.validarSeContratoDiferenteDeSocial(contratoSiconvDTO);
		
		ContratoBD contratoBD = dao.get(ContratoDAO.class).consultarContratoPorContratoFK(contratoFk);

		dao.getJdbi().useTransaction(transaction -> {
				
			LOGGER.debug("--------------- Inicio da Transação de Criação do Contrato -------------------");
							
			if (!Optional.ofNullable(contratoBD).isPresent()) {
				// Senão houver ContratoBD Associado insere toda a estrutura Contrato
				contratoBC.incluir(contratoSiconvDTO, transaction);
			}
			
			this.salvarResponsavelTecnico(responsavelTecnicoDTO, contratoFk, transaction);
			
			LOGGER.debug("--------------- Fim da Transação de Criação do Contrato -------------------");
			
		});
	}

	/**
	 * Inclui Responsável Técnico, registros e vinculos
	 * 
	 * @param contratoBDPersistir
	 * @param medicao
	 * @param transaction
	 */
	public ResponsavelTecnicoBD salvarResponsavelTecnico(ResponsavelTecnicoDTO responsavelTecnico, Long contratoFk,
			Handle transaction) {

		this.validarResponsavelTecnico(responsavelTecnico);

		ResponsavelTecnicoBD responsavelTecnicoRetorno = responsavelTecnico.converterParaBD();

		ResponsavelTecnicoDTO responsavelTecnicoBanco = transaction.attach(ResponsavelTecnicoDAO.class)
				.consultarRegistrosRespTecnico(responsavelTecnico.getCpf());

		if (responsavelTecnicoRetorno.getId() == null) {
			LOGGER.debug("--------------- Antes da inclusão do Responsável Técnico - Início -------------------");

			// Verifica se o CPF já está cadastrado.
			if (responsavelTecnicoBanco != null) {
				throw new MedicaoRestException(new Message(MessageKey.ERRO_RESPONSAVEL_TECNICO_JA_EXISTENTE));
			}

			responsavelTecnicoRetorno = transaction.attach(ResponsavelTecnicoDAO.class)
					.inserir(responsavelTecnicoRetorno);
			responsavelTecnico.setId(responsavelTecnicoRetorno.getId());

			LOGGER.debug("--------------- Depois da inclusão do Responsável Técnico - Fim  -------------------");
		} else {
			LOGGER.debug("--------------- Antes da alteração do Responsável Técnico - Início -------------------");

			responsavelTecnicoRetorno = transaction.attach(ResponsavelTecnicoDAO.class)
					.alterar(responsavelTecnicoRetorno);

			LOGGER.debug("--------------- Depois da alteração do Responsável Técnico - Fim  -------------------");

		}

		return this.salvarRegistrosProfissionais(responsavelTecnico, responsavelTecnicoRetorno, responsavelTecnicoBanco,
				contratoFk, transaction);

	}

	/**
	 * Inserir Registros Profissionais
	 * 
	 * @param contratoBD
	 * @param listaEventoFrenteObraVrplDTO
	 * @param transaction
	 */
	private ResponsavelTecnicoBD salvarRegistrosProfissionais(ResponsavelTecnicoDTO responsavelTecnicoTela,
			ResponsavelTecnicoBD responsavelTecnicoRetorno, ResponsavelTecnicoDTO responsavelTecnicoBanco,
			Long contratoFk, Handle transaction) {

		this.validarRegistrosProfissionais(responsavelTecnicoTela.getRegistrosProfissional(), contratoFk);

		// Inclusão e Alteração dos Registros Profissionais.
		LOGGER.debug("--------------- Antes da inclusão / Alteração dos Registros Profissionais -------------------");
		responsavelTecnicoTela.getRegistrosProfissional().forEach(registroProfissionalDTO -> {
			RegistroProfissionalBD registroProfissionalBD = registroProfissionalDTO.converterParaBD();
			registroProfissionalBD.setAtividade(
					AtividadeRegistroProfissionalEnum.fromDescricao(registroProfissionalBD.getAtividade()).name());
			registroProfissionalBD.setResponsavelTecnicoFk(responsavelTecnicoTela.getId());

			if (registroProfissionalDTO.getId() == null) {
				registroProfissionalBD.setResponsavelTecnicoFk(responsavelTecnicoTela.getId());

				registroProfissionalBD = transaction.attach(RegistroProfissionalDAO.class)
						.inserir(registroProfissionalBD);

				registroProfissionalDTO.setId(registroProfissionalBD.getId());
			} else {

				transaction.attach(RegistroProfissionalDAO.class).alterar(registroProfissionalBD);
			}
		});

		this.salvarContratoResponsavelTecnico(responsavelTecnicoBanco,
				responsavelTecnicoTela.obterRegistroProfissional(contratoFk),
				responsavelTecnicoTela.obterRegistroProfissional(contratoFk).obterContratoVinculado(contratoFk),
				contratoFk, transaction);
		LOGGER.debug("--------------- Após a inclusão / Alteração dos Registros Profissionais -------------------");

		// Faz por último a exclusão, com o registro profissional adequado já setado no
		// ContratoRespTecnico
		if (responsavelTecnicoBanco != null) {
			this.processarExclusaoRegistrosProfissionais(responsavelTecnicoTela, responsavelTecnicoBanco, contratoFk,
					transaction);
		}

		return responsavelTecnicoRetorno;
	}

	private void processarExclusaoRegistrosProfissionais(ResponsavelTecnicoDTO responsavelTecnicoTela,
			ResponsavelTecnicoDTO responsavelTecnicoBanco, Long contratoFk, Handle transaction) {
		
		if (!responsavelTecnicoTela.getRegistrosProfissional().isEmpty()) {
			LOGGER.debug("--------------- Antes da exlusao dos Registros Profissional-------------------");

			List<RegistroProfissionalDTO> listaRegistrosExcluidos = new ArrayList<>();
			List<Long> listaIdExcluidos = new ArrayList<>();
			List<Long> listaVersaoExcluidos = new ArrayList<>();
			responsavelTecnicoBanco.getRegistrosProfissional().forEach(rp -> 
				gerarListaExclusao(responsavelTecnicoTela, listaRegistrosExcluidos, listaIdExcluidos,
						listaVersaoExcluidos, rp)
			);

			if (!listaRegistrosExcluidos.isEmpty()) {
				this.validarExclusaoRegistroProfissional(listaRegistrosExcluidos, contratoFk);

				try {
					transaction.attach(RegistroProfissionalDAO.class).excluirRegistrosPorListaId(listaIdExcluidos,
							listaVersaoExcluidos);
				} catch (Exception e) {
					LOGGER.debug(e);
					throw new MedicaoRestException(MessageKey.ERRO_EXCLUIR_REGISTRO_PROFISSIONAL);
				}

			}

			LOGGER.debug("--------------- Depois da exlusao dos Registros Profissionais-------------------");
		}
	}

	private void gerarListaExclusao(ResponsavelTecnicoDTO responsavelTecnicoTela,
			List<RegistroProfissionalDTO> listaRegistrosExcluidos, List<Long> listaIdExcluidos,
			List<Long> listaVersaoExcluidos, RegistroProfissionalDTO rp) {
		boolean excluido = true;

		for (RegistroProfissionalDTO rpDTO : responsavelTecnicoTela.getRegistrosProfissional()) {
			if (rp.getId().equals(rpDTO.getId())) {
				excluido = false;
				break;
			}
		}

		if (excluido) {
			listaRegistrosExcluidos.add(rp);
			listaIdExcluidos.add(rp.getId());
			listaVersaoExcluidos.add(rp.getVersao());
		}
	}

	/**
	 * Inserir Vinculo Contrato e Registros Profissionais
	 * 
	 * @param contratoBD
	 * @param listaEventoFrenteObraVrplDTO
	 * @param transaction
	 */
	private ContratoResponsavelTecnicoBD salvarContratoResponsavelTecnico(ResponsavelTecnicoDTO responsavelTecnicoBanco,
			RegistroProfissionalDTO registroProfissionalTela,
			ContratoResponsavelTecnicoDTO contratoVinculadoResponsavelTecnicoTelaDTO, Long contratoFk, Handle transaction) {

		ContratoResponsavelTecnicoBD contratoVinculadoResponsavelTecnicoTelaBD = contratoVinculadoResponsavelTecnicoTelaDTO.converterParaBD();
		contratoVinculadoResponsavelTecnicoTelaBD.setRegistro(registroProfissionalTela.getId());

		if (contratoVinculadoResponsavelTecnicoTelaBD.getId() == null) {
			LOGGER.debug(
					"--------------- Antes da inclusão do vínculo Contrato Responsavel Tecnico-------------------");
			ContratoBD contrato = transaction.attach(ContratoDAO.class)
					.consultarContratoPorContratoFK(contratoVinculadoResponsavelTecnicoTelaDTO.getContratoFk());
			contratoVinculadoResponsavelTecnicoTelaBD.setContrato(contrato.getId());

			contratoVinculadoResponsavelTecnicoTelaBD = transaction.attach(ContratoResponsavelTecnicoDAO.class)
					.inserir(contratoVinculadoResponsavelTecnicoTelaBD);

			LOGGER.debug(
					"--------------- Depois da inclusão do vínculo Contrato Responsavel Tecnico-------------------");
		} else {
			LOGGER.debug(
					"--------------- Antes da alteração do vínculo Contrato Responsavel Tecnico-------------------");

			if (transaction.attach(ContratoResponsavelTecnicoDAO.class)
					.isContRespTecnicoAssinado(contratoVinculadoResponsavelTecnicoTelaBD.getId())) {
				throw new MedicaoRestException(MessageKey.ERRO_EDITAR_RT_POSSUI_SUBMETA_ASSINADA);
			}
			
			if (responsavelTecnicoBanco == null) {
				throw new MedicaoRestException(new Message(MessageKey.ERRO_RESPONSAVEL_TECNICO_INEXISTENTE));
			}
			
			RegistroProfissionalDTO registroBanco = responsavelTecnicoBanco.obterRegistroProfissional(contratoFk);
			
			if(registroBanco == null) {
				throw new MedicaoRestException(new Message(MessageKey.ERRO_RESPONSAVEL_TECNICO_INEXISTENTE), Status.NOT_FOUND.getStatusCode());
			}
			// Verifica se o Registro Profissional que foi enviado da tela é o mesmo que está no Banco
			// Se for diferente então atualiza o Registro Profissional do banco com o Registro Profissional que veio da Tela
			if (!registroBanco.equals(registroProfissionalTela)) {
				contratoVinculadoResponsavelTecnicoTelaBD.setDataInclusao(LocalDateTime.now());

				contratoVinculadoResponsavelTecnicoTelaBD = transaction.attach(ContratoResponsavelTecnicoDAO.class)
						.alterar(contratoVinculadoResponsavelTecnicoTelaBD);
			}

			LOGGER.debug(
					"--------------- Depois da alteração do vínculo Contrato Responsavel Tecnico-------------------");
		}

		return contratoVinculadoResponsavelTecnicoTelaBD;

	}

	/**
	 * Verifica se o @param contratoFk está associado a exatamente um Registro
	 * Profissional do Responsável Técnico.
	 * 
	 * @param registrosProfissionais
	 * @param contratoFk
	 */
	private void validarRegistrosProfissionais(List<RegistroProfissionalDTO> registrosProfissionaisTela,
			Long contratoFk) {

		if (registrosProfissionaisTela.isEmpty()) {
			throw new MedicaoRestException(new Message(MessageKey.ERRO_REGISTRO_PROFISSIONAL_NAO_CADASTRADO));
		}

		// Percorre todos os registros Profissionais em busca do contrato em um deles.
		List<ContratoResponsavelTecnicoDTO> lista = new ArrayList<>();
		for (RegistroProfissionalDTO registroProfissionalDTO : registrosProfissionaisTela) {
			for (ContratoResponsavelTecnicoDTO contrato : registroProfissionalDTO.getContratos()) {

				if (contrato.getContratoFk().equals(contratoFk)) {
					lista.add(contrato);
				}
			}
		}

		// Só deve ter um Contrato na lista
		if (lista.size() != 1) {
			throw new MedicaoRestException(
					new Message(MessageKey.ERRO_CONTRATO_NAO_ASSOCIADO_A_EXATAMENTE_UM_REGISTRO_PROFISSIONAL));
		}

		// E10 - SOMENTE UM CREA PARA A MESMA UF OU UM CAU CADASTRADOS
		this.validarRegistroDuplicado(registrosProfissionaisTela);

	}

	/**
	 * Valida se possui Registros Profissionais na lista
	 * 
	 * @param registrosExcluidos
	 */
	private void validarRegistroDuplicado(List<RegistroProfissionalDTO> registrosProfissionaisTela) {

		List<RegistroProfissionalDTO> registrosTela2 = new ArrayList<>();
		registrosTela2.addAll(registrosProfissionaisTela);
		for (RegistroProfissionalDTO registroTela : registrosProfissionaisTela) {
			for (RegistroProfissionalDTO regTela2 : registrosTela2) {
				if (registroTela.getId() != null && !registroTela.getId().equals(regTela2.getId())
						&& registroTela.getAtividade().equals(regTela2.getAtividade())) {
					if ((registroTela.getAtividade().equals(AtividadeRegistroProfissionalEnum.ENG.getDescricao())
							&& registroTela.getUf().equals(regTela2.getUf()))) {
						throw new MedicaoRestException(
								new Message(MessageKey.ERRO_REGISTRO_PROFISSIONAL_DUPLICADO_CREA_UF));
					} else if (registroTela.getAtividade()
							.equals(AtividadeRegistroProfissionalEnum.ARQ.getDescricao())) {
						throw new MedicaoRestException(
								new Message(MessageKey.ERRO_REGISTRO_PROFISSIONAL_DUPLICADO_CAU));
					}
				}
			}
		}
	}

	/**
	 * Valida se os Registros Profissionais podem ser Exluídos
	 * 
	 * @param registrosExcluidos
	 */
	private void validarExclusaoRegistroProfissional(List<RegistroProfissionalDTO> registrosExcluidos,
			Long contratoFk) {

		StringBuilder creaCau = new StringBuilder();
		for (RegistroProfissionalDTO registroProfissionalDTO : registrosExcluidos) {
			// Só deve existir UM elemento na lista de contratos via
			// 'validarRegistrosProfissionais'
			// Não é permitido excluir Registro Profissional associado à outro contrato
			if (!registroProfissionalDTO.getContratos().isEmpty()
					&& registroProfissionalDTO.getContratos().get(0).getContratoFk().compareTo(contratoFk) != 0) {
				if (registroProfissionalDTO.getUf() != null && !registroProfissionalDTO.getUf().isEmpty()) {
					creaCau.append("\n" + "- CREA" + " " + registroProfissionalDTO.getNrCreaCau() + "/"
							+ registroProfissionalDTO.getUf());
				} else {
					creaCau.append("\n" + "- CAU" + " " + registroProfissionalDTO.getNrCreaCau());
				}
			}
		}
		if (creaCau.length() > 0) {
			throw new MedicaoRestException(new Message(MessageKey.ERRO_REGISTRO_PROFISSIONAL_VINCULADO_CONTRATO,
					new String[] { creaCau.toString() }));
		}

	}

	/**
	 * Valida se os parâmetros informados são inválidos.
	 * 
	 * @param responsavelTecnico
	 */
	private void validarResponsavelTecnico(ResponsavelTecnicoDTO responsavelTecnico) {

		if ((responsavelTecnico.getId() == null && responsavelTecnico.getCpf() == null)
				|| (responsavelTecnico.getId() != null && responsavelTecnico.getCpf() == null)) {

			throw new MedicaoRestException(new Message(MessageKey.ERRO_RESPONSAVEL_TECNICO_PARAMETROS_INVALIDOS));

		}
	}

	private void validarSeContratoDiferenteDeSocial(ContratoSiconvDTO contrato) {
		if (contrato.getInSocial().booleanValue()) {
			throw new MedicaoRestException(MessageKey.ERRO_EDITAR_RT_ART_RRT_CONTRATO_SOCIAL);
		}
	}
	
	private boolean permiteExcluirEstruturaContratoRT(Long idMedContratoRespTec) {
		return !dao.get(ContratoResponsavelTecnicoDAO.class).existeRespTecnicoContrato(idMedContratoRespTec)
				&& !dao.get(DocumentoComplementarDAO.class).existeDocumentoComplementarContrato(idMedContratoRespTec)
				&& !dao.get(ParalisacaoDAO.class).existeParalisacaoContrato(idMedContratoRespTec);
	}
	 
	public void excluirVinculoRespTecContrato(Long idMedContratoRespTec) {

		dao.getJdbi().useTransaction(transaction -> 
		
		transaction.attach(ContratoResponsavelTecnicoDAO.class).consultar(idMedContratoRespTec).

			ifPresentOrElse( contratoRespTec -> {
			
					if (transaction.attach(ContratoResponsavelTecnicoDAO.class)
							.isContRespTecnicoAssinado(idMedContratoRespTec)) {
		
						throw new MedicaoRestException(MessageKey.ERRO_EXCLUIR_RT_POSSUI_SUBMETA_ASSINADA);
		
					} else if (transaction.attach(ContratoResponsavelTecnicoDAO.class)
							.isContRespTecnicoAnotado(idMedContratoRespTec)) {
		
						throw new MedicaoRestException(MessageKey.ERRO_EXCLUIR_RT_POSSUI_ANOTACAO);
		
					} else {
						
						transaction.attach(ContratoResponsavelTecnicoDAO.class).excluir(idMedContratoRespTec);
						
						if(this.permiteExcluirEstruturaContratoRT(contratoRespTec.getContrato())) {
							contratoBC.excluirEstruturaContrato(contratoRespTec.getContrato(), transaction);                   					
						}
					}
				}, () -> {
							throw new MedicaoRestException (MessageKey.CONTRATO_RESP_TEC_INEXISTENTE );
					}
			) 
		);
	}
	
	public ResponsavelTecnicoDTO recuperarResponsavelTecnicoPorCPFTipo(String numeroCPF, String tipoRespTec,
			Long contratoFk, Boolean validate) {

		ResponsavelTecnicoDTO respTecnico;

		if(!Optional.ofNullable(validate).isPresent()) {
			validate = true;
		}
		
		// Sistema verifica se existe RT cadastrado no Medicao.
		ResponsavelTecnicoDTO rt = dao.get(ResponsavelTecnicoDAO.class).consultarResponsavelTecnicoPorCpf(numeroCPF);
		
		if(Optional.ofNullable(rt).isPresent()) {
			
			// Sistema verifica se o RT já existe cadastrado como de eng/arquitetura e recupera os registros
			respTecnico = dao.get(ResponsavelTecnicoDAO.class).consultarRegistrosRespTecnico(numeroCPF);
	
			if (Optional.ofNullable(respTecnico).isPresent()) {
				RegistroProfissionalDTO registro = respTecnico.obterRegistroProfissional(contratoFk);
	
				if (registro != null) {
					ContratoResponsavelTecnicoDTO contrato = registro.obterContratoVinculado(contratoFk);
	
					return obterUsuario(numeroCPF, tipoRespTec, contratoFk, validate, respTecnico, contrato);
	
				}
	
			} else {
	
				respTecnico = rt;
	
			}
		} else {
			respTecnico = new ResponsavelTecnicoDTO();
		}

		respTecnico.setCpf(numeroCPF);
		respTecnico.setIdContratoSiconv(contratoFk);

		UsuarioDTO usuario = this.consultarUsuario(numeroCPF, TipoResponsavelTecnicoEnum.fromCodigo(tipoRespTec),
				contratoFk, validate);

		if (usuario != null) {
			respTecnico.setNome(usuario.getNome());
			respTecnico.setEmail(usuario.getEmail());
		}

		return respTecnico;
	}

	private ResponsavelTecnicoDTO obterUsuario(String numeroCPF, String tipoRespTec, Long contratoFk, Boolean validate,
			ResponsavelTecnicoDTO respTecnico, ContratoResponsavelTecnicoDTO contrato) {
		if (contrato != null && contrato.getTipo().getCodigo().equals(tipoRespTec)) {

			respTecnico.setCpf(numeroCPF);
			respTecnico.setIdContratoSiconv(contratoFk);

			UsuarioDTO usuario = this.consultarUsuario(numeroCPF, TipoResponsavelTecnicoEnum.fromCodigo(tipoRespTec),
					contratoFk, validate);

			if (usuario != null) {
				respTecnico.setNome(usuario.getNome());
				respTecnico.setEmail(usuario.getEmail());
			}

			return respTecnico;

		} else {
			throw new MedicaoRestException(MessageKey.ERRO_RESPTECNICO_CADASTRADO_TIPO_DIFERENTE);
		}
	}

	/**
	 * Recupera o nome do Responsável Técnico de acordo com o @tipoRT informado.
	 * 
	 * @param cpf
	 * @param tipoRT
	 * @param idContrato
	 * @return
	 * 
	 */
	public UsuarioDTO consultarUsuario(String cpf, TipoResponsavelTecnicoEnum tipoRT, Long idContrato,
			boolean validaUsuario) {

        ContratoSiconvDTO contrato = contratosConsumer.consultarContratoPorId(idContrato)
                .orElseThrow(() -> new MedicaoRestException(MessageKey.CONTRATO_INEXISTENTE));
        
        UsuarioDTO usuario = usuarioConsumer.getUsuario(cpf, tipoRT, contrato, true);

		switch (tipoRT) {
		case EXE:

            if(validaUsuario) {
            	validaVinculoUsuarioExecucao(usuario);
            }
			break;

		case FIS:

			if (validaUsuario) {
				validaVinculoUsuarioFiscalizacao(usuario);
			}
			break;

		default:
			break;
		}

		// Quando não se encontra um usuário no SICONV/Plataforma com o CPF informado e
		// não é exigido validação do usuário (validaUsuario=false), a aplicação não
		// levantará uma exceção, mas registrará um evento de erro no Sentry.
		if (usuario == null && !validaUsuario) {
			LOGGER.error(normalizeSpace(format(
					"Responsável Técnico não encontrado no cadastro de usuários do SICONV/Plataforma [cpf=%s, tipoRT=%s, idContrato=%s, proposta=%s]",
					cpf, tipoRT.getCodigo(), idContrato, contrato.getPropostaFk())));
		}

		return usuario;
	}

	private void validaVinculoUsuarioFiscalizacao(UsuarioDTO usuario) {
		if (usuario == null) {
			throw new MedicaoRestException(MessageKey.ERRO_USUARIO_NAO_CADASTRADO_SICONV);

		} else if (!usuario.isVinculadoConvenioAtual()) {
			throw new MedicaoRestException(MessageKey.CONTRATO_EXISTENTE_NAO_VINCULADO);

		} else if (usuario.isVinculadoConvenioAtual() && !usuario.isFiscalConvenente()) {
			throw new MedicaoRestException(MessageKey.ERRO_USUARIO_VINCULADO_SEM_PERFIL_FISCAL_CONVENENTE);

		} else if (!usuario.isAtivo()) {
			throw new MedicaoRestException(MessageKey.ERRO_USUARIO_INATIVO_SICONV);
		}
	}

	private void validaVinculoUsuarioExecucao(UsuarioDTO usuario) {
		if (usuario == null) {
			throw new MedicaoRestException(MessageKey.ERRO_USUARIO_NAO_CADASTRADO_MAIS_BRASIL);

		} else if (!usuario.isVinculadoEmpresa()) {
			throw new MedicaoRestException(MessageKey.CPF_EXISTENTE_NAO_VINCULADO_EMPRESA);

		} else if (usuario.isVinculadoEmpresa() && !usuario.isAssinanteSubmetaEmpresa()) {
			throw new MedicaoRestException(MessageKey.ERRO_CPF_EXISTENTE_SEM_ACESSO_FUNCIONALIDADE);
		}
	}

	public ResponsavelTecnicoDTO consultarResponsavelTecnicoPorCPF(String numeroCPF) {

		return dao.get(ResponsavelTecnicoDAO.class).consultarRegistrosRespTecnico(numeroCPF);

	}
	
	public ResponsavelTecnicoDTO consultarContratoResponsavelTecnicoPorId(Long id) {

		//passar false
		
		ContratoResponsavelTecnicoBD contratoRespTecnico = dao.get(ContratoResponsavelTecnicoDAO.class).consultar(id).orElseThrow(IllegalArgumentException::new);
		
		ResponsavelTecnicoDTO responsavelTecnicoDTO = dao.get(ResponsavelTecnicoDAO.class).consultarResponsavelTecnicoPorContratoRespTecnicoId(id);
		
		ContratoBD contratoBD = dao.get(ContratoDAO.class).consultarContrato(responsavelTecnicoDTO.getContratoFk());
		
		return this.recuperarResponsavelTecnicoPorCPFTipo(responsavelTecnicoDTO.getCpf() , contratoRespTecnico.getTipo().getCodigo(), contratoBD.getContratoFk(), false);

	}
}