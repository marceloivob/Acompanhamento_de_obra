package br.gov.planejamento.siconv.med.medicao.business;

import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.EMPRESA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.MANDATARIA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.PROPONENTE_CONVENENTE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response.Status;

import org.jdbi.v3.core.Handle;

import br.gov.planejamento.siconv.med.contrato.dao.ContratoDAO;
import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.infra.database.DAOFactory;
import br.gov.planejamento.siconv.med.infra.exception.MedicaoRestException;
import br.gov.planejamento.siconv.med.infra.message.Message;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.infra.security.domain.Profile;
import br.gov.planejamento.siconv.med.integration.UsuarioConsumer;
import br.gov.planejamento.siconv.med.integration.ceph.CephActions;
import br.gov.planejamento.siconv.med.medicao.dao.AnexoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.MedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.ObservacaoDAO;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.database.AnexoBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.MedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.ObservacaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.dto.AnexoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ObservacaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.PerfilEnum;

@ApplicationScoped
public class ObservacaoBC {

	@Inject
	private DAOFactory daoFactory;
	
    @Inject
    private CephActions cephActions;

    @Inject
	private UsuarioConsumer usuarioConsumer;
	
	@Inject
	private SecurityContext securityContext;
	
	@Inject
    private PerfilHelper perfilHelper;
    
	/**
	 * Insere a Observação e, se existir, anexo(s).
	 * 
	 * @param medMedicaoFk Long representando o ID da medição.
	 * @param observacao   {@link ObservacaoDTO} representando a observação.
	 * @return ID da observação inserida.
	 * @throws IOException 
	 */
	public Long inserirObservacao(Long medMedicaoFk, @NotNull ObservacaoDTO observacao) {

		this.validarCamposObrigatoriosObservacao(observacao);

		MedicaoDTO medicao = getDao().get(MedicaoDAO.class).obterMedicao(medMedicaoFk);

		this.validarAutorizacaoManutencao(medicao);

		Long[] idObsInserida = new Long[1];

		if (observacao.getAnexos() == null || observacao.getAnexos().isEmpty()) {

			getDao().getJdbi().useTransaction(transaction -> idObsInserida[0] = transaction.attach(ObservacaoDAO.class)
					.inserirObservacao(medMedicaoFk, observacao.converterParaBD()));

		} else {

			getDao().getJdbi().useTransaction(transaction -> {

				idObsInserida[0] = transaction.attach(ObservacaoDAO.class).inserirObservacao(medMedicaoFk,
						observacao.converterParaBD());

				List<AnexoDTO> anexosDTO = observacao.getAnexos();
				List<AnexoBD> anexosBD = new ArrayList<>();
				for (AnexoDTO anexoDTO : anexosDTO) {
					anexoDTO.setCoCeph(this.anexarArquivo(anexoDTO.getNmArquivo(), anexoDTO.getArquivo()));
					anexoDTO.setObservacaoFk(idObsInserida[0]);
					anexosBD.add(anexoDTO.converterParaBD());
				}

				transaction.attach(AnexoDAO.class).inserirAnexo(anexosBD);
			});

		}

		return idObsInserida[0];

	}
	
	private void validarAutorizacaoManutencao(MedicaoDTO medicao) {

		// Só pode realizar manutenção se:
		// 1) Medição não for Agrupada
		Optional.ofNullable(medicao.getIdMedicaoAgrupadora()).ifPresent(idMedicaoAgrupadora -> {
			throw new MedicaoRestException(MessageKey.ERRO_MANTER_OBSERVACAO_MEDICAO_AGRUPADA);
		});
		// 2) Medição não está bloqueada.
		if (medicao.isBloqueada()) {
			throw new MedicaoRestException(MessageKey.ERRO_MEDICAO_BLOQUEADA);
		}
		//3) Usuário possui perfil adequado
		if ((securityContext.isUserInProfile(EMPRESA) && !medicao.getSituacao().permiteManutencaoEmpresa())
				|| (securityContext.isUserInProfile(PROPONENTE_CONVENENTE) && !medicao.getSituacao().permiteManutencaoConvenente())
				|| ((securityContext.isUserInProfile(CONCEDENTE)|| securityContext.isUserInProfile(MANDATARIA)) && !medicao.getSituacao().permiteManutencaoConcedente())) {
			throw new MedicaoRestException(MessageKey.ERRO_ACESSO_PERFIL_NAO_AUTORIZADO);
		}
		
	
	}
	

	private void validarAutorizacaoAtivarInativarAnexo(MedicaoDTO medicao) {

		// 1) Medição não está bloqueada.
		if (medicao.isBloqueada()) {
			throw new MedicaoRestException(MessageKey.ERRO_MEDICAO_BLOQUEADA);
		}
		
		// 2) Usuário possui perfil adequado
		if ((securityContext.isUserInProfile(EMPRESA) && !medicao.getSituacao().permiteManutencaoEmpresa())
			 || (securityContext.isUserInProfile(PROPONENTE_CONVENENTE) && !medicao.getSituacao().permiteManutencaoConvenente())
			 || ((securityContext.isUserInProfile(CONCEDENTE)||securityContext.isUserInProfile(MANDATARIA)) && !medicao.getSituacao().permiteManutencaoConcedente())) {
			throw new MedicaoRestException(MessageKey.ERRO_ACESSO_PERFIL_NAO_AUTORIZADO);
		}
	}	
	
	/**
	 * Valida se a observação {@link ObservacaoDTO} possui os campos obrigatórios,
	 * lançando exceções caso contrário.
	 * 
	 * @param observacao {@link ObservacaoDTO} representando a observação.
	 * 
	 */
	private void validarCamposObrigatoriosObservacao(@NotNull ObservacaoDTO observacao) {

		if (observacao.getTxObservacao() == null || observacao.getTxObservacao().trim().isEmpty()) {
			throw new MedicaoRestException(
					new Message(MessageKey.ERRO_PARAMETRO_OBRIGATORIO_NAO_INFORMADO, new String[] { "txObservacao" }));
		}

		if (observacao.getAnexos() != null && !observacao.getAnexos().isEmpty()) {
			validaAnexosDaObservacao(observacao);
		}

	}

	private void validaAnexosDaObservacao(ObservacaoDTO observacao) {
		for (AnexoDTO anexoDTO : observacao.getAnexos()) {
			if (anexoDTO.getNmArquivo() == null || anexoDTO.getNmArquivo().trim().isEmpty()) {
				throw new MedicaoRestException(new Message(MessageKey.ERRO_PARAMETRO_OBRIGATORIO_NAO_INFORMADO,
						new String[] { "nmArquivo" }));
			}
			if (anexoDTO.getNmArquivo().length() > 100) {
				throw new MedicaoRestException(MessageKey.ERRO_LIMITE_NOME_ANEXO_EXCEDIDO);
			}
			if (anexoDTO.getArquivo() == null && anexoDTO.getId() == null) {
				throw new MedicaoRestException(new Message(MessageKey.ERRO_PARAMETRO_OBRIGATORIO_NAO_INFORMADO,
						new String[] { "arquivo" }));
			}
		}
	}

	/**
	 * Exclui a Observação e seus anexos. <br>
	 * Obs.: <b>Por motivo de auditoria</b>, os anexos não são removidos no Ceph.
	 * @param idMedicao Long
	 * @param idObservacao Long
	 */
	public void excluirObservacao(Long idMedicao, Long idObservacao) {

		MedicaoDTO medicao = getDao().get(MedicaoDAO.class).obterMedicao(idMedicao);
		
		this.validarAutorizacaoManutencao(medicao);

		ObservacaoDTO obsBase = getDao().get(ObservacaoDAO.class).recuperarObservacaoPorId(idObservacao, idMedicao);
		

		if (obsBase != null) {
			validarPerfilObservacao(obsBase);
			// verifica se a Observação está bloqueada
			if (!obsBase.isInBloqueio()) {

				getDao().getJdbi().useTransaction(transaction -> {

					transaction.attach(AnexoDAO.class).excluirAnexoPorObservacaoFK(idObservacao);

					transaction.attach(ObservacaoDAO.class).excluirObservacao(idMedicao, idObservacao);
				});
			} else {
				throw new MedicaoRestException(MessageKey.ERRO_OBSERVACAO_NAO_PODE_SER_EXCLUIDA);
			}
		} else {
			throw new MedicaoRestException(MessageKey.ERRO_OBSERVACAO_NAO_ENCONTRADA);
		}
	}

    public String anexarArquivo(String nomeArquivo, byte[] arquivo) {
        return cephActions.uploadFile(arquivo, nomeArquivo);
    }
    
	/**
	 * Altera a Observação e seus anexos, incluindo os novos e excluindo <br>
	 * aqueles que deixaram de pertencer à observação <br><br>
	 * Obs.:<b> Por motivo de auditoria</b>, os anexos não são removidos no Ceph.
	 * Obs.: <b>A versão do anexo sempre será 1 (não há update no mesmo arquivo), por isso sua versão é desconsiderada.
	 * @param idMedicao
	 * @param idObservacao
	 * @param observacao 
	 * @throws IOException
	 */
	public void alterarObservacao(Long idMedicao, Long idObservacao, ObservacaoDTO observacaoDTO) {
		
		this.validarAlteracaoObservacao(idObservacao);
		
		this.validarCamposObrigatoriosObservacao(observacaoDTO);
		
		MedicaoDTO medicao = getDao().get(MedicaoDAO.class).obterMedicao(idMedicao);
		
		this.validarAutorizacaoManutencao(medicao);
		
			// Verificar se a observação não possui anexos
			if (observacaoDTO.getAnexos() == null || observacaoDTO.getAnexos().isEmpty()) {
				
				// excluir todos os Anexos com idObservacao e dar Update na Observação
				getDao().getJdbi().useTransaction(transaction -> {
		
					transaction.attach(AnexoDAO.class).excluirAnexoPorObservacaoFK(idObservacao);
		
					transaction.attach(ObservacaoDAO.class).alterarObservacao(idMedicao, idObservacao, observacaoDTO.converterParaBD());
				});
			} else {
		
				// Percorrer a lista de Anexos identificando os NOVOS e os que permanecem sem
				// alteração
				List<Long> listaIdAnexoAtuais = new ArrayList<>();
				List<AnexoBD> anexosBdIncluir = new ArrayList<>();
		
				for (AnexoDTO anexoDTO : observacaoDTO.getAnexos()) {
					if (anexoDTO.getId() == null) {
						//Vincular o novo anexo à Observação
						anexoDTO.setObservacaoFk(idObservacao);
						//Gravar arquivo no Ceph e setar seu código no anexoDTO
						anexoDTO.setCoCeph(this.anexarArquivo(anexoDTO.getNmArquivo(), anexoDTO.getArquivo()));
						anexosBdIncluir.add(anexoDTO.converterParaBD());
					} else {
						listaIdAnexoAtuais.add(anexoDTO.getId());
					}
				}
		
				// Resgatar os ID's dos anexos antigos, i.e. existentes ANTES da Alteração
				List<Long> listaIdAnexoAntigos = getDao().get(AnexoDAO.class).buscarIdAnexoPorIdObservacao(idObservacao);
				// Definir os anexos que deixaram de existir na Alteração, i.e. Antigos - Atuais
				listaIdAnexoAntigos.removeAll(listaIdAnexoAtuais);
		
				// Excluir anexos que deixaram de existir, inserir os novos e atualizar a
				// observação
				getDao().getJdbi().useTransaction(transaction -> {
		
					if(!listaIdAnexoAntigos.isEmpty()) {
						transaction.attach(AnexoDAO.class).excluirAnexoPorListaIdAnexo(listaIdAnexoAntigos);					
					}
					
					if(!anexosBdIncluir.isEmpty()) {
						transaction.attach(AnexoDAO.class).inserirAnexo(anexosBdIncluir);
					}				
		
					transaction.attach(ObservacaoDAO.class).alterarObservacao(idMedicao, idObservacao , observacaoDTO.converterParaBD());
				});
		
			}
	}
	
	private void validarAlteracaoObservacao(Long idObservacao) {
		ObservacaoDTO observacaoBD = this.buscarObservacaoId(idObservacao);

		if (!Optional.ofNullable(observacaoBD).isPresent()) {
			throw new MedicaoRestException(MessageKey.ERRO_OBSERVACAO_NAO_ENCONTRADA);
		} else if (observacaoBD.isInBloqueio()) {
			throw new MedicaoRestException(MessageKey.ERRO_OBSERVACAO_NAO_PODE_SER_EDITADA);
		}
		validarPerfilObservacao(observacaoBD);
	}

	private void validarPerfilObservacao(ObservacaoDTO observacaoBD) {
		if (observacaoBD.getInPerfilResponsavel() != perfilHelper.getPerfilUsuarioLogado()) {
			throw new MedicaoRestException(MessageKey.ERRO_ACESSO_PERFIL_NAO_AUTORIZADO);
		}
	}
	

	public List<ObservacaoDTO> buscarObservacoesMedicao(Long medicaoFk, boolean medicoesAgrupadas) {

		List<ObservacaoDTO> observacoes = new ArrayList<>();

		SituacaoMedicaoEnum situacaoMedicao = daoFactory.get(MedicaoDAO.class).obterMedicao(medicaoFk).getSituacao();

		if (!(situacaoMedicao.equals(SituacaoMedicaoEnum.EM) && !securityContext.hasAnyPermissionInProfile(EMPRESA))) {

			if (medicoesAgrupadas) {
				observacoes = daoFactory.get(ObservacaoDAO.class).recuperarObservacaoMedicoesAgrupadas(medicaoFk,
						this.apenasBloqueada(situacaoMedicao));
			} else {
				observacoes = daoFactory.get(ObservacaoDAO.class).recuperarObservacaoPorMedicao(medicaoFk,
						this.apenasBloqueada(situacaoMedicao));
			}
			
			observacoes.forEach(this::decorateObservacaoDTO);
			
		}
		return observacoes;
	}

	private void decorateObservacaoDTO(ObservacaoDTO obsDTO) {
		obsDTO.setNomeResponsavel(usuarioConsumer.getNomeUsuario(
				obsDTO.getNrCpfResponsavel(), obsDTO.getInPerfilResponsavel(), true));
		obsDTO.getAnexos().forEach(anexo -> {
			anexo.setUrl(cephActions.getPresignedUrl(anexo.getCoCeph()));
			
			if(anexo.getNrCpfInativo() != null) {
				anexo.setNomeCpfInativo(usuarioConsumer.getNomeUsuarioSiconv(
						anexo.getNrCpfInativo(), true));
			}
		});
	}
	
	private boolean apenasBloqueada(SituacaoMedicaoEnum situacaoMedicao) {

		return (!securityContext.hasAnyPermissionInProfile(EMPRESA) && situacaoMedicao.permiteManutencaoEmpresa())
				|| (!securityContext.isUserInProfile(PROPONENTE_CONVENENTE)
						&& situacaoMedicao.permiteManutencaoConvenente())
				|| (!securityContext.isUserInProfile(CONCEDENTE) && !securityContext.isUserInProfile(MANDATARIA)
						&& situacaoMedicao.permiteManutencaoConcedente());
	}

	// Metodos necessarios para realizar Teste Unitarios com o Mock
	
	public DAOFactory getDao() {
		return daoFactory;
	}

	public void setDao(DAOFactory dao) {
		this.daoFactory = dao;
	}
	
	public ObservacaoDTO buscarObservacaoId(Long observacaoFk) {

		List<AnexoDTO> anexoDto = new ArrayList<>();
		ObservacaoDTO observacaoDto;

		Optional<MedicaoBD> medicaoBase = daoFactory.get(MedicaoDAO.class).consultarMedicaoPorIdObs(observacaoFk);

		if (!medicaoBase.isPresent() || (medicaoBase.get().getSituacao().equals(SituacaoMedicaoEnum.EM)
				&& !securityContext.hasAnyPermissionInProfile(EMPRESA))) {
			// Não encontrará medição para uma observação inexistente
			throw new MedicaoRestException(MessageKey.ERRO_OBSERVACAO_NAO_ENCONTRADA, Status.NOT_FOUND.getStatusCode());
		}

		observacaoDto = daoFactory.get(ObservacaoDAO.class).recuperarObservacaoPorId(observacaoFk, this.apenasBloqueada(medicaoBase.get().getSituacao()));

		if (observacaoDto == null) {
			throw new MedicaoRestException(MessageKey.ERRO_OBSERVACAO_NAO_ENCONTRADA, Status.NOT_FOUND.getStatusCode());
		}

		List<Long> listaAnexo = daoFactory.get(AnexoDAO.class).buscarIdAnexoPorIdObservacao(observacaoFk);

		if (!listaAnexo.isEmpty()) {

			for (Long idAnexo : listaAnexo) {
				anexoDto.add(daoFactory.get(AnexoDAO.class).buscarAnexoPorId(idAnexo));
			}
			observacaoDto.setAnexos(anexoDto);
			observacaoDto.getAnexos().forEach(anexo -> anexo.setUrl(cephActions.getPresignedUrl(anexo.getCoCeph())));
		}
		// Obter Id do Contrato
		ContratoBD contratoBD = daoFactory.get(ContratoDAO.class).consultarContratoAssociadoObservacao(observacaoDto.getId());
		observacaoDto.setIdContratoSiconv(contratoBD.getContratoFk());

		return observacaoDto;
	}

	
	/**
	 * Bloqueia as medições de acordo com o perfil.
	 * 
	 * @param transaction
	 * @param id
	 * @param empresa
	 */
	public void bloquearObservacaoMedicaoFilha(Handle transaction, MedicaoBD medicao, Profile perfil) {

		Optional.ofNullable(medicao.getIdMedicaoAgrupadora()).
			ifPresentOrElse(idMedicaAgrupadora -> this.bloquearObservacao(transaction, medicao.getId(), perfil), 
				() -> new MedicaoRestException(new Message(MessageKey.ERRO_MEDICAO_DEVE_SER_AGRUPADA)));
	}

	
	/**
	 * Bloqueia as Observações da medição de acordo com o perfil.
	 * 
	 * @param idMedicao
	 * @param empresa
	 */
	public void bloquearObservacao(Handle transaction, Long idMedicao, Profile perfil) {

		List<ObservacaoBD> listaObservacoes = new ArrayList<>();
		
		switch (perfil) {
		case EMPRESA:
			listaObservacoes = transaction.attach(ObservacaoDAO.class).consultarObservacoesPorBloqueioPerfil (idMedicao,false, true, false, false);
			break;
		case PROPONENTE_CONVENENTE:
			listaObservacoes = transaction.attach(ObservacaoDAO.class).consultarObservacoesPorBloqueioPerfil (idMedicao, false, false, true, false);
			break;
		case CONCEDENTE:
			listaObservacoes = transaction.attach(ObservacaoDAO.class).consultarObservacoesPorBloqueioPerfil (idMedicao,false, false, false, true);
			break;
		case MANDATARIA:
			listaObservacoes = transaction.attach(ObservacaoDAO.class).consultarObservacoesPorBloqueioPerfil (idMedicao,false, false, false, true);
			break;

		default:
			break;
		}
		
		listaObservacoes.forEach(obs -> transaction.attach(ObservacaoDAO.class).bloquearObservacao(obs));
	}

	/**
	 * Move as Observações inseridas pelo Convenente que ainda Não estão bloqueadas para a Medição Agrupadora
	 * 
	 * @param id
	 * @param idMedicao
	 */
	public void moverObservacaoNaoBloqueadaMedicaoAgrupadaParaMedicaoAtual(Handle transaction, MedicaoBD medicaoAgrupada) {

		
		if (!securityContext.getUser().getProfile().equals(Profile.PROPONENTE_CONVENENTE)) {
			throw new MedicaoRestException(new Message(MessageKey.ERRO_FUNCIONALIDADE_DISPONIVEL_APENAS_PARA_CONVENENTE));
		}

		Optional.ofNullable(medicaoAgrupada.getIdMedicaoAgrupadora()).
		ifPresentOrElse(idMedAgrupadora -> {
				List<ObservacaoBD> listaObservacoes = transaction.attach(ObservacaoDAO.class).consultarObservacoesPorBloqueioPerfil (idMedAgrupadora, false, false, true, false);
			
				listaObservacoes.forEach(obs -> transaction.attach(ObservacaoDAO.class)
					.moverObservacaoMedicaoAgrupadaMedicaoAgrupadora(obs, idMedAgrupadora));
		}, 
				() -> new MedicaoRestException(new Message(MessageKey.ERRO_MEDICAO_DEVE_SER_AGRUPADA)));
		
	}

	
	/**
	 * Move as Observações inseridas pelo Concedente/Mandataria para a Medição Agrupadora
	 * 
	 * @param id
	 * @param idMedicao
	 */
	public void moverObservacaoMedicaoAgrupadaParaMedicaoAtualConcedenteMandataria(Handle transaction, MedicaoBD medicaoAgrupada) {

		
		if (!securityContext.getUser().getProfile().equals(Profile.MANDATARIA) && !securityContext.getUser().getProfile().equals(Profile.CONCEDENTE)) {
			throw new MedicaoRestException(new Message(MessageKey.ERRO_FUNCIONALIDADE_DISPONIVEL_APENAS_PARA_CONCEDENTE_MANDATARIA));
		}
		
		Optional.ofNullable(medicaoAgrupada.getIdMedicaoAgrupadora()).
			ifPresentOrElse(idMedicaoAgrupadora -> {
				List<ObservacaoBD> listaObservacoes = transaction.attach(ObservacaoDAO.class).consultarObservacoesPorBloqueioPerfil (medicaoAgrupada.getId(), false, false, false, true);
				
				listaObservacoes.forEach(obs -> transaction.attach(ObservacaoDAO.class)
						.moverObservacaoMedicaoAgrupadaMedicaoAgrupadora(obs, idMedicaoAgrupadora));
			}, () -> new MedicaoRestException(new Message(MessageKey.ERRO_MEDICAO_DEVE_SER_AGRUPADA)));
		
	}
	
	public void ativarInativarAnexo(String operacao, Long idAnexo, Long idObservacao, Long idMedicao ) {	
		
		MedicaoDTO medicao = getDao().get(MedicaoDAO.class).obterMedicao(idMedicao);
		
		this.validarAutorizacaoAtivarInativarAnexo(medicao);		
		
		ObservacaoDTO observacaoDto = getDao().get(ObservacaoDAO.class).recuperarObservacaoPorId(idObservacao, idMedicao);
		
		List<Long> listaAnexoId = getDao().get(AnexoDAO.class).buscarIdAnexoPorIdObservacao(idObservacao);
		
		if(observacaoDto != null) {

			if(observacaoDto.getInPerfilResponsavel().equals(PerfilEnum.EMP) && securityContext.isUserInProfile(PROPONENTE_CONVENENTE)
			   ||
			   observacaoDto.getInPerfilResponsavel().equals(PerfilEnum.CVE) && 
			                 (securityContext.isUserInProfile(CONCEDENTE) || securityContext.isUserInProfile(MANDATARIA))) {

				gravarAtivarInativarAnexo(operacao, idAnexo, listaAnexoId);

				
			}else {
				throw new MedicaoRestException(MessageKey.ERRO_ATIVAR_INATIVAR_ANEXO_PERFIL_RESPONSAVEL_NAO_PERMITIDO);
			}
			
		}else {
			throw new MedicaoRestException(MessageKey.ERRO_OBSERVACAO_NAO_ENCONTRADA);
		}
						
	}

	private void gravarAtivarInativarAnexo(String operacao, Long idAnexo, List<Long> listaAnexoId) {
		if(listaAnexoId.stream().anyMatch(idAnexo::equals)) {
								
			getDao().getJdbi().useTransaction(transaction -> {
				
				if (operacao.equals("ativar")) {
					
					AnexoDTO resultAnexo = getDao().get(AnexoDAO.class).buscarAnexoPorId(idAnexo);
					
					if(!resultAnexo.isInInativo()) {
					   throw new MedicaoRestException(new Message(MessageKey.ERRO_ANEXO_ATIVO_ANTERIORMENTE));	
					}
					
					transaction.attach(AnexoDAO.class).ativarAnexoPorId(idAnexo);
					
				}else {
					
					AnexoDTO resultAnexo = getDao().get(AnexoDAO.class).buscarAnexoPorId(idAnexo);
					
					if(resultAnexo.isInInativo()) {
					   throw new MedicaoRestException(new Message(MessageKey.ERRO_ANEXO_INATIVO_ANTERIORMENTE));	
					}
												
					transaction.attach(AnexoDAO.class).inativarAnexoPorId(idAnexo, securityContext.getUser().getCpf());
					
				}			
			});					
			
		}else {
			throw new MedicaoRestException(MessageKey.ERRO_ANEXO_INEXISTENTE);
		}
	}

	public boolean existeObservacaoCadastradaPorUsuarioDesbloqueadaNaMedicao(Long idMedicao, String cpfResponsavel) {
		return daoFactory.get(ObservacaoDAO.class).existeObservacaoCadastradaPorUsuarioDesbloqueadaNaMedicao(idMedicao, cpfResponsavel);
	}
}