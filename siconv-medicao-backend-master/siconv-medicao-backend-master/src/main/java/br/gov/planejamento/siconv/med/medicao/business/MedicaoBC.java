package br.gov.planejamento.siconv.med.medicao.business;

import static br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.TipoDocumentoEnum.MAM;
import static br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.TipoManifestoEnum.DIS;
import static br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.TipoManifestoEnum.LIN;
import static br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.TipoManifestoEnum.OUT;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.EMPRESA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.MANDATARIA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.PROPONENTE_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.util.ConstantesMedicao.MEDICAO_INICIAL;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.AT;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ATD;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.CC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.CE;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.EC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ECC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ECE;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.EM;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.commons.lang3.BooleanUtils.isNotFalse;
import static org.apache.commons.lang3.BooleanUtils.isNotTrue;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.business.DocumentoComplementarBC;
import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.DocumentoComplementarDTO;
import br.gov.planejamento.siconv.med.contrato.business.ContratosBC;
import br.gov.planejamento.siconv.med.contrato.dao.ContratoDAO;
import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.contrato.entity.dto.ContratoSiconvDTO;
import br.gov.planejamento.siconv.med.infra.exception.MedicaoRestException;
import br.gov.planejamento.siconv.med.infra.message.Message;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.infra.security.domain.Profile;
import br.gov.planejamento.siconv.med.infra.security.domain.Role;
import br.gov.planejamento.siconv.med.infra.util.TemporalUtil;
import br.gov.planejamento.siconv.med.integration.projetobasico.ProjetoBasicoGRPCConsumer;
import br.gov.planejamento.siconv.med.integration.vrpl.VrplGRPCConsumer;
import br.gov.planejamento.siconv.med.medicao.business.builder.IndicadoresAcaoListaMedicoesBuilder;
import br.gov.planejamento.siconv.med.medicao.dao.AnexoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.ItemMedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.MedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.ObservacaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.SubmetaDAO;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoSubmetaEnum;
import br.gov.planejamento.siconv.med.medicao.entity.database.HistoricoMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.MedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.SubmetaMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoAgrupadaDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.VistoriaExtraDTO;

@ApplicationScoped
public class MedicaoBC {

	@Inject
	private Jdbi jdbi;

	@Inject
	private VrplGRPCConsumer vrplConsumer;

	@Inject
	private ProjetoBasicoGRPCConsumer projetoBasicoConsumer;
	
	@Inject
	private SecurityContext securityContext;

	@Inject
	private ContratosBC contratoBC;

	@Inject
	private SubmetaBC submetaBc;

	@Inject
	private HistoricoMedicaoBC historicoBC;

	@Inject
	private ObservacaoBC observacaoBC;

	@Inject
	private DocumentoComplementarBC documentoComplementarBC;

	public List<MedicaoDTO> listarMedicoes(Long idContrato) {

		List<MedicaoDTO> listaMedicoes = getMedicaoDAO().listarMedicoes(idContrato);

		if (!listaMedicoes.isEmpty()) {
			// Filtra os Dados da Medição
			listaMedicoes.stream().filter(med -> (med.getSituacao() == EM || med.getSituacao() == CE)).forEach(this::filtrarDadosPublicosMedicao);

			IndicadoresAcaoListaMedicoesBuilder.of(listaMedicoes).build();

			submetaBc.totalizarValoresSubmetasPorMedicao(listaMedicoes);
		}

		return listaMedicoes;
	}

	private boolean usuarioTemPermissaoEmpresa() {
		return securityContext.hasAnyPermissionInProfile(EMPRESA);
	}

	private boolean usuarioTemPermissaoConvenente() {
		return securityContext.isUserInProfile(Profile.PROPONENTE_CONVENENTE);
	}

	private boolean usuarioTemPermissaoAdministrador() {
		return securityContext.hasRoleInProfile(Profile.CONCEDENTE,
				List.of(Role.ADMINISTRADOR_SISTEMA, Role.ADMINISTRADOR_SISTEMA_ORGAO_EXTERNO));
	}

	private String getCpfUsuario() {
		return securityContext.getUser().getCpf();
	}

	public MedicaoDTO obterMedicao(Long idMedicao) {

		MedicaoDTO medicao = getMedicaoDAO().obterMedicao(idMedicao);

		if (medicao == null) {
			throw new MedicaoRestException(MessageKey.ERRO_MEDICAO_NAO_ENCONTRADA, Status.NOT_FOUND.getStatusCode());
		}

		filtrarDadosPublicosMedicao(medicao);

		return medicao;
	}

	private void filtrarDadosPublicosMedicao(MedicaoDTO medicao) {

		
		
		// Filtra exibicao da data fim para medicoes em complementacao pela empresa
		if ((medicao.getSituacao().equals(EM) || (medicao.getSituacao().equals(CE) && isNotFalse(medicao.getPermiteComplementacaoValor()) && medicao.getIdMedicaoAgrupadora() == null))
				&& !usuarioTemPermissaoEmpresa()) {
			medicao.setDataFim(null);
			//Quando se tratar da primeira medição
			if(medicao.getSequencial().shortValue() == 1) {
				medicao.setDataInicioObra(null);
				medicao.setDataInicio(null);
			}
		}
		
		//Filtra exibição dos dados de vistoria para medicao em edição pelo concedente/mandataria
		if(!medicao.getSituacao().equals(SituacaoMedicaoEnum.ACT) &&
				!(securityContext.hasAnyRoleInProfile(Profile.CONCEDENTE) || 
				  securityContext.hasAnyRoleInProfile(Profile.MANDATARIA))) {
			medicao.setVistoriaExtra(false);
			medicao.setDataVistoriaExtra(null);
			medicao.setSolicitanteVistoriaExtra(null);
		}
	}

	/**
	 * Inclui uma Medição.
	 * 
	 * @param medicaoDTO
	 * @param contratoFK
	 * @return
	 */
	public MedicaoDTO incluir(MedicaoDTO medicaoDTO, Long contratoFK) {

		ContratoSiconvDTO contrato = contratoBC.consultarContratoPorId(contratoFK);
		ContratoBD contratoMedicao = contratoBC.consultarContratoMedicaoPorContratoFK(contratoFK);

		validarContratoPermiteCriacaoMedicao(contrato, contratoMedicao);

		medicaoDTO.setSequencial((short) (contrato.getQtdeMedicoes() + 1));
		validarDadosMedicao(medicaoDTO, contrato);

		Long idMedicao = jdbi.inTransaction(handle -> {

			alterarDataInicioObra(handle, medicaoDTO, contratoMedicao);

			return handle.attach(MedicaoDAO.class)
					.inserir(new MedicaoBD(medicaoDTO.getSequencial(), medicaoDTO.getDataInicio(),
							medicaoDTO.getDataFim(), SituacaoMedicaoEnum.EM, contratoMedicao.getId()));
		});

		return getMedicaoDAO().obterMedicao(idMedicao);
	}

	private void validarContratoPermiteCriacaoMedicao(ContratoSiconvDTO contrato, ContratoBD contratoMedicao) {

		if (isNotTrue(contrato.getIsConfiguradoParaMedicao())) {
			throw new MedicaoRestException(MessageKey.ERRO_CONTRATO_NAO_CONFIGURADO);
		}

		if (!contratoBC.temSubmetasAExecutar(contrato.getId())) {
			throw new MedicaoRestException(MessageKey.ERRO_CONTRATO_TODAS_SUBMETAS_FINALIZADAS_EMPRESA);
		}

		Map<Long, SituacaoMedicaoEnum> situacoesMedicoesContrato = getMedicaoDAO()
				.listarSituacoesMedicoes(contratoMedicao.getId());

		if (existeSituacaoEmElaboracao(situacoesMedicoesContrato)) {
			throw new MedicaoRestException(MessageKey.ERRO_CRIACAO_MEDICAO_QUANDO_JA_EXISTE_EM_ELABORACAO);
		}

		if (existeSituacaoComplementacaoEmpresa(situacoesMedicoesContrato)) {
			throw new MedicaoRestException(MessageKey.ERRO_CONTRATO_POSSUI_MEDICAO_COMPL_EMP_OU_ENVIADA_COMPL_EMP);
		}
		
		if (contratoBC.isContratoParalisado(contrato.getId())) {
			throw new MedicaoRestException(MessageKey.ERRO_CRIACAO_MEDICAO_QUANDO_CONTRATO_PARALISADO);
		}
			
	}

	private void validarDadosMedicao(MedicaoDTO medicaoDTO, ContratoSiconvDTO contrato) {

		validarCamposObrigatorios(medicaoDTO);
		validarDataInicioObjetoFutura(medicaoDTO);
		validarDataInicioMedicaoDataFimMedicao(medicaoDTO);
//		validarDataAssinaturaContrato(medicaoDTO, contrato.getDtAssinatura());
//		validarDataFimVigenciaContrato(medicaoDTO, contrato.getDtFimVigencia());

		if (medicaoDTO.getSequencial().equals(MEDICAO_INICIAL)) {
			validarDataInicioObraDataInicioMedicao(medicaoDTO);

		} else {
			MedicaoBD medicaoAnterior = getMedicaoDAO().consultarMedicaoPorSequencial(contrato.getId(),
					(short) (medicaoDTO.getSequencial() - 1));

			validarDataInicioPosteriorDataFim(medicaoDTO, medicaoAnterior.getDtFim());
		}
	}

	//	Vistoria Extra
	private void validarDadosVistoriaExtra(VistoriaExtraDTO vistoriaExtra, MedicaoDTO medicao, boolean isDataObrigatoria) {		
		if (securityContext.isUserInProfile(Profile.CONCEDENTE) || securityContext.isUserInProfile(Profile.MANDATARIA)){
			
			if(isDataObrigatoria && vistoriaExtra.getDataVistoriaExtra() == null) {
					throw new MedicaoRestException(MessageKey.ERRO_PARAMETRO_OBRIGATORIO_NAO_INFORMADO, "Data Vistoria");
			}
			
			if(vistoriaExtra.getVersao() == null) {
				throw new MedicaoRestException(MessageKey.ERRO_PARAMETRO_OBRIGATORIO_NAO_INFORMADO, "Versão");
			}
			
			if(vistoriaExtra.getDataVistoriaExtra() != null && vistoriaExtra.getDataVistoriaExtra().isBefore(medicao.getDataInicioObra())) {
					throw new MedicaoRestException(MessageKey.ERRO_DATA_VISTORIA_EXTRA_INVALIDA, TemporalUtil.formataDataPtBR(medicao.getDataInicioObra()));
			}
			
			if (vistoriaExtra.isVistoriaExtra() && !Optional.ofNullable(vistoriaExtra.getSolicitanteVistoriaExtra()).isPresent()) {					
					throw new MedicaoRestException(MessageKey.ERRO_CAMPO_SOLICITANTE_VISTORIA_EXTRA_OBRIGATORIO);
			}
		}
	}

	/**
	 * Atualiza, se necessário, a data de início da obra na tabela
	 * <i>med_contrato</i>, quando a medição for a inicial.
	 * 
	 * @param handle
	 * @param medicaoDTO
	 * @param contratoMedicao
	 */
	private void alterarDataInicioObra(Handle handle, MedicaoDTO medicaoDTO, ContratoBD contratoMedicao) {

		if (medicaoDTO.getSequencial().equals(MEDICAO_INICIAL)
				&& !Objects.equals(contratoMedicao.getDataInicioObra(), medicaoDTO.getDataInicioObra())) {
			contratoMedicao.setDataInicioObra(medicaoDTO.getDataInicioObra());
			handle.attach(ContratoDAO.class).alterar(contratoMedicao);
		}
	}

	/**
	 * Altera uma Medição.
	 * 
	 * @param medicaoDTO
	 */
	public MedicaoDTO alterar(MedicaoDTO medicaoDTO) {

		MedicaoBD medicaoBD = consultarMedicaoBD(medicaoDTO.getId());
		ContratoBD contratoMedicao = contratoBC.consultarContratoAssociadoMedicao(medicaoDTO.getId());
		ContratoSiconvDTO contrato = contratoBC.consultarContratoPorId(contratoMedicao.getContratoFk());

		validarMedicaoPermiteAlteracao(medicaoBD);

		medicaoDTO.setSequencial(medicaoBD.getNrSequencial());
		validarDadosMedicao(medicaoDTO, contrato);

		jdbi.useTransaction(handle -> {

			alterarDataInicioObra(handle, medicaoDTO, contratoMedicao);

			medicaoBD.setDtInicio(medicaoDTO.getDataInicio());
			medicaoBD.setDtFim(medicaoDTO.getDataFim());
			medicaoBD.setVersao(medicaoDTO.getVersao());
	
			alterarMedicao(handle, medicaoBD, false);
		});

		return getMedicaoDAO().obterMedicao(medicaoDTO.getId());
	}
	
	/**
	 * Altera dados da Medição com relação à Vistoria Extra para o Concedente.
	 * 
	 * @param VistoriaExtraDTO
	 */
	public MedicaoDTO alterarConcedenteMandataria(VistoriaExtraDTO vistoriaExtraDTO, Long idMedicao) {
         		MedicaoBD medicaoBD = consultarMedicaoBD(idMedicao);
         		
         		validaAlterarConcedenteMandataria(vistoriaExtraDTO, medicaoBD, false);

				jdbi.useTransaction(handle -> {
				    if(!vistoriaExtraDTO.isVistoriaExtra()) {
						medicaoBD.setSolicitanteVistoriaExtra(null);
					}
				    
				    alterarMedicao(handle, medicaoBD, false);
				});

				return getMedicaoDAO().obterMedicao(idMedicao);							
	}
	
	private void validaAlterarConcedenteMandataria(VistoriaExtraDTO vistoriaExtraDTO, MedicaoBD medicaoBD, boolean isDataVistoriaObrigatoria) {
				
		if (medicaoBD.getIdMedicaoAgrupadora() != null || medicaoBD.getSituacao() != SituacaoMedicaoEnum.AC) {
			throw new MedicaoRestException(MessageKey.ERRO_MEDICAO_NAO_PODE_SER_ALTERADA);
		}

		validarMedicaoBloqueada(medicaoBD);				
		MedicaoDTO medicaoDTO = getMedicaoDAO().obterMedicao(medicaoBD.getId());		
		validarDadosVistoriaExtra(vistoriaExtraDTO, medicaoDTO, isDataVistoriaObrigatoria);
		
		medicaoBD.setVersao(vistoriaExtraDTO.getVersao());
		medicaoBD.setSolicitanteVistoriaExtra(vistoriaExtraDTO.getSolicitanteVistoriaExtra());
	    medicaoBD.setDataVistoriaExtra(vistoriaExtraDTO.getDataVistoriaExtra());
	    medicaoBD.setVistoriaExtra(vistoriaExtraDTO.isVistoriaExtra());
	}
	
	private void validarMedicaoPermiteAlteracao(MedicaoBD medicao) {

		if (medicao.getIdMedicaoAgrupadora() != null
				|| (medicao.getSituacao() != SituacaoMedicaoEnum.EM && medicao.getSituacao() != SituacaoMedicaoEnum.CE)
				|| isFalse(medicao.getPermiteComplementacaoValor())) {
			throw new MedicaoRestException(MessageKey.ERRO_MEDICAO_NAO_PODE_SER_ALTERADA);
		}

		validarMedicaoBloqueada(medicao);
	}
	
	private void validarMedicaoBloqueada(MedicaoBD medicao) {

		if (medicao.isBloqueada()) {
			throw new MedicaoRestException(MessageKey.ERRO_MEDICAO_BLOQUEADA);
		}
	}

	/**
	 * Validar se a data de Início da Medição que está sendo cadastrada é o dia
	 * seguinte da data fim da Medição anterior.
	 * 
	 * @param medicao
	 * @param dataFimUltimaMedicao
	 */
	private void validarDataInicioPosteriorDataFim(MedicaoDTO medicao, LocalDate dataFimUltimaMedicao) {

		if (!dataFimUltimaMedicao.plusDays(1l).equals(medicao.getDataInicio())) {

			throw new MedicaoRestException(MessageKey.ERRO_DATA_INICIO_MEDICAO_DIFERENTE_DIA_SEGUINTE_MEDICAO_ANTERIOR);
		}
	}

	/**
	 * Validar se a data de Início do Objeto para a medição que está sendo
	 * cadastrada é maior que a data atual.
	 * 
	 * @param medicao
	 */
	private void validarDataInicioObjetoFutura(MedicaoDTO medicao) {

		if (medicao.getDataInicioObra() == null || medicao.getDataInicioObra().isAfter(LocalDate.now())) {
			throw new MedicaoRestException(new Message(MessageKey.ERRO_DATA_INICIO_OBJETO_MAIOR_QUE_ATUAL,
					new String[] { TemporalUtil.formataDataPtBR(LocalDate.now()) }));
		}
	}

	/**
	 * Valida os campos obrigatório de uma Medição.
	 * 
	 * @param medicao
	 */
	private void validarCamposObrigatorios(MedicaoDTO medicao) {
		
		if (medicao.getDataInicioObra() == null || medicao.getDataInicio() == null) {
			throw new MedicaoRestException(MessageKey.ERRO_DATA_INICIO_OBRIGATORIA);
		}

		if (medicao.getId() != null && medicao.getVersao() == null) {
			throw new MedicaoRestException(MessageKey.ERRO_PARAMETRO_OBRIGATORIO_NAO_INFORMADO, "versao");
		}

	}

//	/**
//	 * Valida se a Data de Início da Obra é anterior a Assinatura do Contrato, se
//	 * for levanta exceção.
//	 * 
//	 * @param medicao
//	 * @param dataAssinaturaContrato
//	 */
//	private void validarDataAssinaturaContrato(MedicaoDTO medicao, LocalDate dataAssinaturaContrato) {
//
//		if (medicao.getDataInicioObra().isBefore(dataAssinaturaContrato)) {
//			throw new MedicaoRestException(new Message(MessageKey.ERRO_DATA_INICIO_ANTERIOR_DATA_ASSINATURA_CONTRATO,
//					new String[] { TemporalUtil.formataDataPtBR(dataAssinaturaContrato) }));
//		}
//	}

	/**
	 * Valida se a data de Inicio de Obra é Igual a da de Inicio da Medição.
	 * 
	 * @param medicao
	 */
	private void validarDataInicioObraDataInicioMedicao(MedicaoDTO medicao) {

		if (!medicao.getDataInicioObra().equals(medicao.getDataInicio())) {
			throw new MedicaoRestException(MessageKey.ERRO_DATA_INICIO_OBRA_DIFERENTE_DATA_INICIO_MEDICAO);
		}
	}

	private void validarDataFimObrigatoria(MedicaoDTO medicao) {

		if (medicao.getDataFim() == null) {
			throw new MedicaoRestException(MessageKey.ERRO_PARAMETRO_OBRIGATORIO_NAO_INFORMADO, "dataFim");
		}
	}

	/**
	 * A data de Inicio deve ser Menor ou igual que a data Fim da Medição.
	 * 
	 * @param medicao
	 */
	private void validarDataInicioMedicaoDataFimMedicao(MedicaoDTO medicao) {
		// Data de Inicio menor ou igual que dataFim
		if (medicao.getDataFim() != null && medicao.getDataInicio().isAfter(medicao.getDataFim())) {
			// Data de Início da Medição deve ser anterior a data fim da Medição
			throw new MedicaoRestException(new Message(MessageKey.ERRO_DATA_INICIO_ANTERIOR_DATA_FIM,
					new String[] { TemporalUtil.formataDataPtBR(medicao.getDataInicio()) }));
		}

	}

//	/**
//	 * Valida se a Data de Fim da medição é posterior a data de fim de vigência do contrato
//	 * de fornecimento, se for levanta exceção.
//	 * 
//	 * @param medicao
//	 * @param dataFimVigenciaCTEF
//	 */
//	private void validarDataFimVigenciaContrato(MedicaoDTO medicao, LocalDate dataFimVigenciaCTEF) {
//		if (medicao.getDataFim() != null && medicao.getDataFim().isAfter(dataFimVigenciaCTEF)) {
//			throw new MedicaoRestException(new Message(MessageKey.ERRO_DATA_FIM_MEDICAO_IGUAL_OU_ANTERIOR_DATA_FIM_VIGENCIA_CTEF,
//					new String[] { TemporalUtil.formataDataPtBR(dataFimVigenciaCTEF) }));
//		}
//	}
	
	/**
	 * Concatena em uma única string o número e a descrição de todas as submetas
	 * informadas. Os dados de cada submetas são delimitados por uma quebra de linha
	 * e também são prefixados por hífen.
	 * 
	 * @param submetasMedicao A lista com as submetas
	 * @return String com o resultado da concatenação
	 */
	private String concatenarNumeroDescricaoSubmetas(List<SubmetaMedicaoBD> submetasMedicao) {

		List<Long> idSubmetasVrpl = submetasMedicao.stream().map(SubmetaMedicaoBD::getIdSubmetaVrpl).distinct().sorted()
				.collect(toList());

		return vrplConsumer.getListaSubmetasPorId(idSubmetasVrpl).stream()
				.map(submeta -> "- " + submeta.getNrSubmetaAnalise() + " - " + submeta.getDescricao())
				.collect(joining("\n"));
	}

	/**
	 * Envia uma medição para o Convenente.
	 * 
	 * @param medicaoDTO
	 */
	public MedicaoDTO enviarConvenente(MedicaoDTO medicaoDTO) {

		MedicaoBD medicaoEnviadaConvenente = consultarMedicaoBD(medicaoDTO.getId());
		ContratoBD contratoMedicao = contratoBC.consultarContratoAssociadoMedicao(medicaoDTO.getId());
		ContratoSiconvDTO contrato = contratoBC.consultarContratoPorId(contratoMedicao.getContratoFk());

		validarMedicaoPermiteEnvioConvenente(medicaoEnviadaConvenente);

		medicaoDTO.setSequencial(medicaoEnviadaConvenente.getNrSequencial());
		validarDadosMedicaoEnvio(medicaoDTO, contrato);

		validarSubmetasEmpresa(medicaoEnviadaConvenente, contratoMedicao);

		jdbi.useTransaction(handle -> {

			if (medicaoEnviadaConvenente.getSituacao().equals(SituacaoMedicaoEnum.CE)) {
				List<MedicaoBD> medicaoEmElaboracao = handle.attach(MedicaoDAO.class).consultarMedicaoporSituacao(
						medicaoEnviadaConvenente.getIdContratoMedicao(), SituacaoMedicaoEnum.EM);
				desbloquearMedicaoEmElaboracaoAposComplementacao(handle, medicaoEmElaboracao, medicaoDTO.getDataFim());
			}

			// Na complementação sem alteração de valores as datas também não podem ser alteradas
			if (isNotFalse(medicaoEnviadaConvenente.getPermiteComplementacaoValor())) {
				alterarDataInicioObra(handle, medicaoDTO, contratoMedicao);
				medicaoEnviadaConvenente.setDtInicio(medicaoDTO.getDataInicio());
				medicaoEnviadaConvenente.setDtFim(medicaoDTO.getDataFim());
			}

			medicaoEnviadaConvenente.setSituacao(SituacaoMedicaoEnum.EC);
			medicaoEnviadaConvenente.setVersao(medicaoDTO.getVersao());

			alterarMedicao(handle, medicaoEnviadaConvenente, true);
			alterarMedicoesAcumuladas(handle, medicaoEnviadaConvenente);
		});

		return getMedicaoDAO().obterMedicao(medicaoDTO.getId());
	}

	private void validarMedicaoPermiteEnvioConvenente(MedicaoBD medicao) {

		if (medicao.getIdMedicaoAgrupadora() != null || (medicao.getSituacao() != SituacaoMedicaoEnum.EM
				&& medicao.getSituacao() != SituacaoMedicaoEnum.CE)) {
			throw new MedicaoRestException(MessageKey.ERRO_ENVIO_CONVENENTE_MEDICAO_NAO_PERMITIDA);
		}

		validarMedicaoBloqueada(medicao);
	}

	private void validarDadosMedicaoEnvio(MedicaoDTO medicaoDTO, ContratoSiconvDTO contrato) {

		validarDadosMedicao(medicaoDTO, contrato);
		validarDataFimObrigatoria(medicaoDTO);
	}

	private void validarSubmetasEmpresa(MedicaoBD medicao, ContratoBD contratoMedicao) {

		List<SubmetaMedicaoBD> listaSubmetasMedicao = getSubmetaDAO().buscarListaSubmetasporMedicao(medicao.getId());

		if (listaSubmetasMedicao.isEmpty()) {
			throw new MedicaoRestException(MessageKey.ERRO_MEDICAO_SEM_SUBMETAS);
		}

		if (!contratoMedicao.isInAcompanhamentoEventos() && medicao.getSituacao() == CE) {
			listaSubmetasMedicao.addAll(getSubmetaDAO().listarSubmetasMedicoesAcumuladas(medicao.getId()));
		}

		List<SubmetaMedicaoBD> submetasNaoAssinadas = listaSubmetasMedicao.stream()
				.filter(submeta -> submeta.getSituacaoEmpresa() == SituacaoSubmetaEnum.RAS).collect(toList());

		if (!submetasNaoAssinadas.isEmpty()) {

			MessageKey msgKey = medicao.getSituacao() == CE
					? MessageKey.ERRO_SUBMETAS_NAO_ASSINADAS_ENVIO_COMPLEMENTACAO
					: MessageKey.ERRO_SUBMETAS_NAO_ASSINADAS;

			throw new MedicaoRestException(msgKey, concatenarNumeroDescricaoSubmetas(submetasNaoAssinadas));
		}
	}

	private void desbloquearMedicaoEmElaboracaoAposComplementacao(Handle handle, List<MedicaoBD> medicaoEmElaboracaoOpt, LocalDate dataFimMedicao) {

		medicaoEmElaboracaoOpt
				.forEach(medicaoEmElaboracao -> {
					medicaoEmElaboracao.setDtInicio(dataFimMedicao.plusDays(1L));
					medicaoEmElaboracao.setBloqueada(false);
					alterarMedicao(handle, medicaoEmElaboracao, false);
				});
	}

	public void cancelarEnvioConvenente(Long idMedicao) {

		MedicaoBD medicao = consultarMedicaoBD(idMedicao);

		if (!permiteCancelarEnvioConvenente(idMedicao)) {
			throw new MedicaoRestException(new Message(MessageKey.ERRO_CANCELAR_ENVIO_SITUACAO_NAO_PREVISTA));
		}

		historicoBC.recuperarPenultimoHistoricoPorMedicaoContrato(medicao.getIdContratoMedicao(), medicao.getNrSequencial())
				.filter(penultimoHistorico -> penultimoHistorico.getSituacao().equals(SituacaoMedicaoEnum.CE))
				.ifPresentOrElse(penultimoHistorico -> medicao.setSituacao(penultimoHistorico.getSituacao()), () -> medicao.setSituacao(SituacaoMedicaoEnum.EM));

		jdbi.useTransaction(handle -> {
			alterarMedicao(handle, medicao, true);
			alterarMedicoesAcumuladas(handle, medicao);
		});
	}

	private boolean permiteCancelarEnvioConvenente(Long idMedicao) {
		return getMedicaoDAO().permiteCancelarEnvioConvenente(idMedicao);
	}

	public void excluirMedicao(Long idMedicao) {

		MedicaoBD medicao = consultarMedicaoBD(idMedicao);
		ContratoBD contratoMedicao = getContratoDAO().consultarContrato(medicao.getIdContratoMedicao());

		if (!isUltimaMedicao(contratoMedicao, medicao) || !usuarioTemPermissaoExcluir(medicao)) {
			throw new MedicaoRestException(MessageKey.ERRO_EXCLUIR_MEDICAO);
		}

		List<MedicaoBD> acumuladas = getMedicaoDAO().listarMedicoesAcumuladas(medicao.getId());

		jdbi.useTransaction(handle -> {

			acumuladas.forEach(medicaoAcumulada -> excluirDadosMedicao(handle, medicaoAcumulada));

			excluirDadosMedicao(handle, medicao);

			int qtdMedicoesAposExclusao = handle.attach(MedicaoDAO.class)
					.consultarQtdeMedicoesPorContrato(contratoMedicao.getContratoFk());

			if (qtdMedicoesAposExclusao == 0) {
				documentoComplementarBC.desbloquearDocumentosComplementares(handle, contratoMedicao.getId());
			}
		});
	}

	private boolean isUltimaMedicao(ContratoBD contratoMedicao, MedicaoBD medicao) {

		MedicaoBD ultimaMedicao = getMedicaoDAO().consultarUltimaMedicao(contratoMedicao.getContratoFk());

		return medicao.getId().equals(ultimaMedicao.getId());
	}

	private boolean usuarioTemPermissaoExcluir(MedicaoBD medicao) {

		return usuarioTemPermissaoAdministrador()
				|| usuarioTemPermissaoConvenente() && medicao.getSituacao().permiteManutencaoConvenente()
				|| usuarioTemPermissaoEmpresa() && medicao.getSituacao().permiteManutencaoEmpresa();
	}

	private void excluirDadosMedicao(Handle handle, MedicaoBD medicao) {

		handle.attach(AnexoDAO.class).excluirAnexoPorIdMedicao(medicao.getId());
		handle.attach(ObservacaoDAO.class).excluirObservacaoPorIdMedicao(medicao.getId());
		handle.attach(SubmetaDAO.class).excluirSubmetaPorIdMedicao(medicao.getId());
		handle.attach(ItemMedicaoDAO.class).limparItemMedicaoPorIdMedicao(medicao.getId());
		handle.attach(ItemMedicaoDAO.class).excluirItemMedicaoValorBM(medicao.getId());
		handle.attach(MedicaoDAO.class).excluirMedicaoPorId(medicao.getId());

		// Medicao em elaboracao so gera historico quando teve cancelamento de envio
		if (medicao.getSituacao() != EM || isMedicaoCanceladaEnvioConvenente(medicao)) {
			historicoBC.inserir(new HistoricoMedicaoBD(medicao.getIdContratoMedicao(), medicao.getNrSequencial(),
					SituacaoMedicaoEnum.EXC));
		}
	}

	private boolean isMedicaoCanceladaEnvioConvenente(MedicaoBD medicaoEmElaboracao) {
		// Verifica se a medicao foi enviada para o convenente e depois cancelada
		// consultando o ultimo registro de historico para a medicao
		return historicoBC
				.recuperarUltimoHistoricoPorMedicaoContrato(medicaoEmElaboracao.getIdContratoMedicao(),
						medicaoEmElaboracao.getNrSequencial())
				.filter(historico -> historico.getSituacao() == EM).isPresent();
	}

	/**
	 * Inicia o Ateste de uma Medição pelo Convenente.
	 * 
	 * @param idMedicao
	 */
	public MedicaoDTO iniciarAteste(Long idMedicao) {

		MedicaoBD medicao = consultarMedicaoBD(idMedicao);

		validarIniciarAteste(medicao);

		List<MedicaoBD> listaMedicoesAnteriores = getMedicaoDAO().listarMedicoesAnterioresPorSituacao(
				medicao.getIdContratoMedicao(), medicao.getNrSequencial(), List.of(EC, AT, ECC, CC));

		validarMedicaoEmComplementacaoConvenente(listaMedicoesAnteriores);

		ContratoBD contrato = getContratoDAO().consultarContrato(medicao.getIdContratoMedicao());

		jdbi.useTransaction(handle -> {

			// Bloqueia as observações da empresa
			observacaoBC.bloquearObservacao(handle, idMedicao, Profile.EMPRESA);

			// (Agrupamento) - Verifica se há outras medições anteriores a serem tratadas.
			listaMedicoesAnteriores.forEach(medicaoFilha -> {

				medicaoFilha.setIdMedicaoAgrupadora(medicao.getId());

				if (medicaoFilha.getSituacao().equals(SituacaoMedicaoEnum.AT)) {

					// Move observações não bloqueadas do convenente da medição agrupada para a
					// medição agrupadora
					observacaoBC.moverObservacaoNaoBloqueadaMedicaoAgrupadaParaMedicaoAtual(handle, medicaoFilha);

					// Descarta marcações das submetas e dos itens medição
					submetaBc.apagarMarcacoesConvenenteSubmetasMedicao(handle, medicaoFilha, contrato);

					// Aplica alteracao do idMedicaoAgrupadora na medicão filha
					alterarMedicao(handle, medicaoFilha, false);

				} else {

					// Bloqueia as observações inseridas pela empresa na medição filha
					observacaoBC.bloquearObservacaoMedicaoFilha(handle, medicaoFilha, Profile.EMPRESA);

					medicaoFilha.setSituacao(SituacaoMedicaoEnum.AT);

					// Aplica a alteração da situação e idMedicaoAgrupadora na medição filha
					alterarMedicao(handle, medicaoFilha, true);
				}
			});

			// Altera a situação da medição original
			medicao.setSituacao(SituacaoMedicaoEnum.AT);
			alterarMedicao(handle, medicao, true);
		});

		return getMedicaoDAO().obterMedicao(idMedicao);
	}

	/**
	 * Verifica se a medição está em situação válida para iniciar ateste
	 * e se não se trata de uma medição filha (agrupada, ou seja idMedicaoAgrupadora != null).
	 * 
	 * @param medicao
	 */
	private void validarIniciarAteste(MedicaoBD medicao) {
		if (!medicao.getSituacao().equals(SituacaoMedicaoEnum.EC)) {
			throw new MedicaoRestException(MessageKey.ERRO_SITUACAO_INVALIDA_PARA_INICIAR_ATESTE);
		}
		
		if(medicao.getIdMedicaoAgrupadora() != null) {
			throw new MedicaoRestException(
					new Message(MessageKey.ERRO_INICIAR_ATESTE_MEDICAO_AGRUPADA));
		}
	}

	/**
	 * Verifica se o contrato possui alguma medição Enviada para Complementação do
	 * Convenente ou Em Complementação pelo Convenente.
	 * 
	 * @param listaMedicoesAnteriores
	 */
	private void validarMedicaoEmComplementacaoConvenente(List<MedicaoBD> listaMedicoesAnteriores) {

		if (listaMedicoesAnteriores.stream()
				.anyMatch(medicao -> 
						List.of(ECC, CC, EC, AT).contains(medicao.getSituacao()) &&
						medicao.getIdMedicaoAgrupadora() == null &&
						medicao.getPermiteComplementacaoValor() != null)) {
			throw new MedicaoRestException(MessageKey.ERRO_CONTRATO_POSSUI_MEDICAO_COMPL_CONV_OU_ENVIADA_COMPL_CONV);
		}
	}

	/**
	 * Realiza o ateste de uma medição individualmente ou de forma acumulada (quando
	 * a medição informada é uma agrupadora).
	 * 
	 * @param idMedicao O identificador da medição que será atestada
	 * @return Um objeto {@link MedicaoDTO} com os dados da medição após atualização
	 * @throws NullPointerException se o idMedicao é {@code null}
	 * @throws MedicaoRestException se a medição não existir ou não possuir os
	 *                              parâmetros necessários que permitam a operação
	 *                              de atestar
	 */
	public MedicaoDTO atestar(Long idMedicao) {

		MedicaoBD medicao = consultarMedicaoBD(idMedicao);

		if ((medicao.getSituacao() != SituacaoMedicaoEnum.AT && medicao.getSituacao() != SituacaoMedicaoEnum.CC) || medicao.getIdMedicaoAgrupadora() != null) {
			throw new MedicaoRestException(MessageKey.ERRO_ATESTE_MEDICAO_NAO_PERMITIDA);
		}

		validarMedicaoBloqueada(medicao);

		validarSubmetasConvenente(medicao);

		jdbi.useTransaction(handle -> {

			if (medicao.getSituacao().equals(SituacaoMedicaoEnum.CC)) {
				desbloquearMedicaoEmAtesteAposComplementacao(medicao, handle);
			}
			
			documentoComplementarBC.bloquearDocumentosComplementares(handle, medicao.getIdContratoMedicao());

			alterarMedicaoEAcumuladasComHistorico(medicao, SituacaoMedicaoEnum.ATD, handle);
		});

		return getMedicaoDAO().obterMedicao(idMedicao);
	}

	private void desbloquearMedicaoEmAtesteAposComplementacao(MedicaoBD medicao, Handle handle) {
		MedicaoDAO medicaoDao = handle.attach(MedicaoDAO.class);
		medicaoDao.consultarMedicaoporSituacao(medicao.getIdContratoMedicao(), SituacaoMedicaoEnum.AT)
			.forEach(medicaoDao::desbloquearMedicao);
	}
	
	public void cancelarAceite(Long idMedicao) {

		MedicaoBD medicao = consultarMedicaoBD(idMedicao);

		validarCancelarAceite(medicao);

		HistoricoMedicaoBD penultimoHistorico = recuperaPenultimoHistorico(medicao);

		jdbi.useTransaction(handle -> 
			alterarMedicaoEAcumuladasComHistorico(medicao, penultimoHistorico.getSituacao(), handle)
		);
	}

	private void validarCancelarAceite(MedicaoBD medicao) {
		
		if(medicao.getIdMedicaoAgrupadora() != null) {
			throw new MedicaoRestException(
					new Message(MessageKey.ERRO_CANCELAR_ACEITE_MEDICAO_AGRUPADA));
		}
		
		if(isNotTrue(permiteCancelarAceite(medicao.getId()))) {
			throw new MedicaoRestException(
					new Message(MessageKey.ERRO_CANCELAR_ACEITE_SITUACAO_NAO_PREVISTA));
		}
	}

	private Boolean permiteCancelarAceite(Long idMedicao) {
		return getMedicaoDAO().permiteCancelarAceite(idMedicao);
	}
	
	/**
	 * Realiza o aceite de uma medição individualmente ou de forma acumulada (quando
	 * a medição informada é uma agrupadora).
	 * 
	 * @param idMedicao O identificador da medição que será atestada
	 * @return Um objeto {@link MedicaoDTO} com os dados da medição após atualização
	 * @throws NullPointerException se o idMedicao é {@code null}
	 * @throws MedicaoRestException se a medição não existir ou não possuir os
	 *                              parâmetros necessários que permitam a operação
	 *                              de atestar
	 */
	public MedicaoDTO aceitar(VistoriaExtraDTO vistoriaExtraDTO, Long idMedicao) {

		//Valida se está "Em Análise pelo Concedente/Mandatária ou se é uma Agrupada 
		MedicaoBD medicao = consultarMedicaoBD(idMedicao);
		if (medicao.getSituacao() != SituacaoMedicaoEnum.AC || medicao.getIdMedicaoAgrupadora() != null) {
			throw new MedicaoRestException(MessageKey.ERRO_ACEITE_MEDICAO_NAO_PERMITIDA);
		}

		//Valida se as submetas foram assinadas pelo concedente
		validarSubmetasConcedenteMandataria(medicao);
		
		//Validação do Salvar Medição Concedente Mandataria
 		validaAlterarConcedenteMandataria(vistoriaExtraDTO, medicao, true);

		jdbi.useTransaction(handle -> {
			
			medicao.setSituacao(SituacaoMedicaoEnum.ACT);
			
		    if(!vistoriaExtraDTO.isVistoriaExtra()) {
				medicao.setSolicitanteVistoriaExtra(null);
			}
			
			alterarMedicao(handle, medicao, true);			
			alterarMedicoesAcumuladas(handle, medicao);
		});

		return getMedicaoDAO().obterMedicao(idMedicao);
	}

	private void validarSubmetasConvenente(MedicaoBD medicao) {

		List<SubmetaMedicaoBD> listaSubmetasMedicao = getSubmetaDAO().buscarListaSubmetasporMedicao(medicao.getId());

		List<SubmetaMedicaoBD> listaSubmetasMedicoesAcumuladas = getSubmetaDAO()
				.listarSubmetasMedicoesAcumuladas(medicao.getId());

		validarSubmetasNaoAssinadasConvenente(listaSubmetasMedicao);

		validarSubmetasRascunhoConvenente(listaSubmetasMedicao);
		
		ContratoBD contratoBD = getContratoDAO().consultarContrato(medicao.getIdContratoMedicao());

		if (!contratoBD.isInAcompanhamentoEventos()) {
			validarSubmetasMedicoesAcumuladasRascunhoConvenente(listaSubmetasMedicoesAcumuladas, medicao);
		}

		validarSubmetasMedicoesAcumuladasNaoAssinadasConvenente(listaSubmetasMedicao, listaSubmetasMedicoesAcumuladas);

		validarDocumentoComplementar(medicao, listaSubmetasMedicao);
	}
	
	
	private void validarSubmetasConcedenteMandataria(MedicaoBD medicao) {

		List<SubmetaMedicaoBD> listaSubmetasMedicao = getSubmetaDAO().buscarListaSubmetasporMedicao(medicao.getId());

		validarSubmetasNaoAssinadasConcedenteMandataria(listaSubmetasMedicao);

		validarSubmetasRascunhoConcedenteMandataria(listaSubmetasMedicao);

		validarSubmetasMedicoesAcumuladasNaoAssinadasConcedenteMandataria(medicao, listaSubmetasMedicao);
		
	}

	
	private void validarDocumentoComplementar(MedicaoBD medicao, List<SubmetaMedicaoBD> listaSubmetasMedicao) {
		
		ContratoBD contrato = getContratoDAO().consultarContrato(medicao.getIdContratoMedicao());
		List<DocumentoComplementarDTO> listaDocComplementar = documentoComplementarBC.listarDocumentosComplementares(contrato.getContratoFk());

		if (!listaSubmetasMedicao.isEmpty()) {
			
			// Primeiro verifica no Medição se todas as Submetas da @medicao, possui um Documento Complementar válido.
			boolean todasSubmetasPossuemDocComplementarValido = listaSubmetasMedicao.stream().allMatch(submeta -> 
				validarDocComplementar (submeta,listaDocComplementar)
			);
			
			if (!todasSubmetasPossuemDocComplementarValido) {
				// Se não encontrou no Medição, Documento Complementar válido para todas as Submetas da @medicao então busca no Projeto básico.
				
				//Obtém a lista de Documentos Complementares do Projeto Básico para as Submetas da @medicao.
				List<DocumentoComplementarDTO> listaDocComplementarFaseAnterior = this.listarDocComlpementarFasesAnteriores (medicao);
				
				todasSubmetasPossuemDocComplementarValido = listaSubmetasMedicao.stream().allMatch(submeta -> 
					validarDocComplementar (submeta,listaDocComplementarFaseAnterior)
				);
				
				if  (!todasSubmetasPossuemDocComplementarValido) {
					throw new MedicaoRestException(MessageKey.ERRO_DOC_COMPLEMENTAR_INVALIDO_INEXISTENTE);
				}
			}
			
		} else {
			throw new MedicaoRestException(MessageKey.ERRO_SUBMETAS_ASSINADAS_EMPRESA_NAO_ASSINADAS_CONVENENTE);
		}
		
	}
	
	/**
	 * Verifica se a SubmetaMedicaoBD informada possui algum Documento Complementar válido.
	 * 
	 * @param submeta
	 * @param listaDocComplementar
	 * @return
	 */
	private boolean validarDocComplementar(SubmetaMedicaoBD submeta, List<DocumentoComplementarDTO> listaDocComplementar) {
		
		// Itera na lista de Documentos Complementares 
		return listaDocComplementar.stream().anyMatch( doc -> 
			// Verifica se a SubmetaMedicaoBD informada possui algum Documento Complementar informado para ela. 
			submetaPossuiDocComplementar (submeta,doc).filter(subVrpl -> 
			
				// Verifica se o documento complementar é válido quanto ao Tipo e vigência.
				this.possuiDocComplementarValido (doc)
			).isPresent()
		);
		
	}

	/**
	 * Verifica se a Submeta está informada no Documento Complementar.  
	 * 
	 * @param submeta
	 * @param submetas
	 * @return
	 */
	private Optional<SubmetaVrplDTO> submetaPossuiDocComplementar(SubmetaMedicaoBD submeta, DocumentoComplementarDTO doc) {
		
		return doc.getSubmetas().stream().filter( sub -> submeta.getIdSubmetaVrpl().equals(sub.getId())).findAny();
	}

	private Boolean possuiDocComplementarValido(DocumentoComplementarDTO doc) {
		
		LocalDate hoje = LocalDate.now();
		
		if (doc.getTipoDocumento().equals(MAM) && 
				   ((doc.getTipoManifestoAmbiental().equals(LIN) && 
					TemporalUtil.validarNoIntervalo(hoje, doc.getDtEmissao(), doc.getDtValidade(), TemporalUtil.Intervalo.FECHADO )) ||
					(doc.getTipoManifestoAmbiental().equals(OUT) && 
							doc.getEqLicencaInstalacao() &&
							TemporalUtil.validarNoIntervalo(hoje, doc.getDtEmissao(), doc.getDtValidade(), TemporalUtil.Intervalo.FECHADO)) ||
					(doc.getTipoManifestoAmbiental().equals(DIS) &&
							TemporalUtil.validarNoIntervalo(hoje, doc.getDtEmissao(), doc.getDtValidade(), TemporalUtil.Intervalo.ABERTO)))
			){
				return TRUE;
			}			
		return FALSE;
	}
	
	
	/**
	 * Consulta no Projeto básico a lista de Documentos Complementares cadastrados da @medicao   
	 * 
	 * @param medicao
	 * @return
	 */
    public List<DocumentoComplementarDTO> listarDocComlpementarFasesAnteriores(MedicaoBD medicao) {

    	List<Long> listaIdSubmetasContrato = getItemMedicaoDAO().consultarSubmetasContrato(medicao.getIdContratoMedicao());
    	
    	return this.projetoBasicoConsumer.consultarDocumentosComplementaresProjetoBasico(listaIdSubmetasContrato, Arrays.asList(DIS, LIN));
    }

	private void validarSubmetasNaoAssinadasConvenente(List<SubmetaMedicaoBD> listaSubmetasMedicao) {

		List<SubmetaMedicaoBD> submetasNaoAssinadas = listaSubmetasMedicao.stream()
				.filter(submeta -> submeta.getSituacaoEmpresa() == SituacaoSubmetaEnum.ASS
						&& submeta.getSituacaoConvenente() != SituacaoSubmetaEnum.ASS)
				.collect(toList());

		if (!submetasNaoAssinadas.isEmpty()) {
			throw new MedicaoRestException(
					new Message(MessageKey.ERRO_SUBMETAS_ASSINADAS_EMPRESA_NAO_ASSINADAS_CONVENENTE,
							new String[] { concatenarNumeroDescricaoSubmetas(submetasNaoAssinadas) }));
		}
	}
	
	private void validarSubmetasNaoAssinadasConcedenteMandataria(List<SubmetaMedicaoBD> listaSubmetasMedicao) {

		List<SubmetaMedicaoBD> submetasNaoAssinadas = listaSubmetasMedicao.stream()
				.filter(submeta -> submeta.getSituacaoConvenente() == SituacaoSubmetaEnum.ASS
						&& submeta.getSituacaoConcedente() != SituacaoSubmetaEnum.ASS)
				.collect(toList());

		if (!submetasNaoAssinadas.isEmpty()) {
			throw new MedicaoRestException(
					new Message(MessageKey.ERRO_SUBMETAS_ASSINADAS_CONVENENTE_NAO_ASSINADAS_CONCEDENTE_MANDATARIA,
							new String[] { concatenarNumeroDescricaoSubmetas(submetasNaoAssinadas) }));
		}
	}	

	private void validarSubmetasMedicoesAcumuladasNaoAssinadasConvenente(List<SubmetaMedicaoBD> listaSubmetasMedicao,
			List<SubmetaMedicaoBD> listaSubmetasMedicoesAcumuladas) {

		Set<Long> idSubmetasAssinadasConvenente = Stream
				.concat(listaSubmetasMedicao.stream(), listaSubmetasMedicoesAcumuladas.stream())
				.filter(submeta -> submeta.getSituacaoConvenente() == SituacaoSubmetaEnum.ASS)
				.map(SubmetaMedicaoBD::getIdSubmetaVrpl).collect(toSet());

		List<SubmetaMedicaoBD> submetasMedicoesAcumuladasNaoAssinadas = listaSubmetasMedicoesAcumuladas.stream()
				.filter(submeta -> submeta.getSituacaoEmpresa() == SituacaoSubmetaEnum.ASS
						&& !idSubmetasAssinadasConvenente.contains(submeta.getIdSubmetaVrpl()))
				.collect(toList());

		if (!submetasMedicoesAcumuladasNaoAssinadas.isEmpty()) {
			throw new MedicaoRestException(new Message(
					MessageKey.ERRO_SUBMETAS_ACUMULADAS_ASSINADAS_EMPRESA_NAO_ASSINADAS_CONVENENTE,
					new String[] { concatenarNumeroDescricaoSubmetas(submetasMedicoesAcumuladasNaoAssinadas) }));
		}
	}

	private void validarSubmetasMedicoesAcumuladasNaoAssinadasConcedenteMandataria(MedicaoBD medicao,
                                                                                   List<SubmetaMedicaoBD> listaSubmetasMedicao) {

		Set<Long> idSubmetasAssinadasConcedente = listaSubmetasMedicao.stream()
				.filter(submeta -> submeta.getSituacaoConcedente() == SituacaoSubmetaEnum.ASS)
				.map(SubmetaMedicaoBD::getIdSubmetaVrpl).collect(toSet());

		List<SubmetaMedicaoBD> submetasMedicoesAcumuladasNaoAssinadas = getSubmetaDAO()
				.listarSubmetasMedicoesAcumuladas(medicao.getId()).stream()
				.filter(submeta -> submeta.getSituacaoConvenente() == SituacaoSubmetaEnum.ASS
						&& !idSubmetasAssinadasConcedente.contains(submeta.getIdSubmetaVrpl()))
				.collect(toList());

		if (!submetasMedicoesAcumuladasNaoAssinadas.isEmpty()) {
			throw new MedicaoRestException(new Message(
					MessageKey.ERRO_SUBMETAS_ACUMULADAS_ASSINADAS_CONVENENTE_NAO_ASSINADAS_CONCEDENTE_MANDATARIA,
					new String[] { concatenarNumeroDescricaoSubmetas(submetasMedicoesAcumuladasNaoAssinadas) }));
		}
	}		
	
	private void validarSubmetasRascunhoConvenente(List<SubmetaMedicaoBD> listaSubmetasMedicao) {

		List<SubmetaMedicaoBD> submetasRascunho = listaSubmetasMedicao.stream()
				.filter(submeta -> submeta.getSituacaoEmpresa() == null
						&& submeta.getSituacaoConvenente() == SituacaoSubmetaEnum.RAS)
				.collect(toList());

		if (!submetasRascunho.isEmpty()) {
			throw new MedicaoRestException(new Message(MessageKey.ERRO_SUBMETAS_RASCUNHO_CONVENENTE,
					new String[] { concatenarNumeroDescricaoSubmetas(submetasRascunho) }));
		}
	}

	private void validarSubmetasRascunhoConcedenteMandataria(List<SubmetaMedicaoBD> listaSubmetasMedicao) {

		List<SubmetaMedicaoBD> submetasRascunho = listaSubmetasMedicao.stream()
				.filter(submeta -> submeta.getSituacaoConvenente() == null
						&& submeta.getSituacaoConcedente() == SituacaoSubmetaEnum.RAS)
				.collect(toList());

		if (!submetasRascunho.isEmpty()) {
			throw new MedicaoRestException(new Message(MessageKey.ERRO_SUBMETAS_RASCUNHO_CONCEDENTE_MADATARIA,
					new String[] { concatenarNumeroDescricaoSubmetas(submetasRascunho) }));
		}
	}
	
	private void validarSubmetasMedicoesAcumuladasRascunhoConvenente(List<SubmetaMedicaoBD> listaSubmetasMedicoesAcumuladas, MedicaoBD medicao) {

		List<SubmetaMedicaoBD> submetasMedicoesAcumuladasRascunho = listaSubmetasMedicoesAcumuladas.stream()
				.filter(submeta -> submeta.getSituacaoConvenente() == SituacaoSubmetaEnum.RAS).collect(toList());

		if (!submetasMedicoesAcumuladasRascunho.isEmpty()) {
			
			boolean complementacaoConvenentePermiteAlterarValor = medicao.getSituacao() == SituacaoMedicaoEnum.CC && medicao.getPermiteComplementacaoValor();
			MessageKey messageKey;

			if (complementacaoConvenentePermiteAlterarValor) {
				messageKey = MessageKey.ERRO_SUBMETAS_ACUMULADAS_RASCUNHO_CONVENENTE;
			} else {
				messageKey = MessageKey.ERRO_SUBMETAS_RASCUNHO_CONVENENTE;
			}
			throw new MedicaoRestException(new Message(messageKey,
					new String[] { concatenarNumeroDescricaoSubmetas(submetasMedicoesAcumuladasRascunho) }));
		}
	}
	
	
	/**
	 * Aplica as alterações de uma medição no banco de dados e efetua um novo
	 * registro de histórico (opcional).
	 * 
	 * @param handle    O {@link Handle} que controla a transação
	 * @param medicao   A medição com os dados que serão alterados no banco
	 * @param historico <code>true</code> cria um registro de histórico,
	 *                  <code>false</code> não gera histórico
	 */
	private void alterarMedicao(Handle handle, MedicaoBD medicao, boolean historico) {

		handle.attach(MedicaoDAO.class).alterar(medicao);

		if (historico) {
			historicoBC.inserir(new HistoricoMedicaoBD(medicao.getIdContratoMedicao(), medicao.getNrSequencial(),
					medicao.getSituacao()));
		}
	}

	/**
	 * Altera a <b>situação</b> e o indicador <b>permiteComplementacaoValor</b> das
	 * medições filhas (acumuladas) conforme a medição agrupadora. Também gera
	 * <b>histórico</b> para cada medição alterada.
	 * 
	 * @param handle            O {@link Handle} que controla a transação
	 * @param medicaoAgrupadora A medição agrupadora já com a nova situação
	 */
	private void alterarMedicoesAcumuladas(Handle handle, MedicaoBD medicaoAgrupadora) {

		List<MedicaoBD> listaMedicoesAcumuladas = handle.attach(MedicaoDAO.class)
				.listarMedicoesAcumuladas(medicaoAgrupadora.getId());

		listaMedicoesAcumuladas.forEach(medicaoAcumulada -> {
			medicaoAcumulada.setSituacao(medicaoAgrupadora.getSituacao());
			medicaoAcumulada.setPermiteComplementacaoValor(medicaoAgrupadora.getPermiteComplementacaoValor());
			alterarMedicao(handle, medicaoAcumulada, true);
		});
	}

	/**
	 * Verifica se uma medição possui os requisitos necessários para ser solicitada
	 * uma complementação.
	 * 
	 * @param idMedicao O identificador da medição que será verificada
	 * @return {@code true} se a medição permitir complementação, e {@code false}
	 *         caso contrário
	 * @throws NullPointerException se o idMedicao é {@code null}
	 */
	public boolean verificarMedicaoPermiteComplementacao(Long idMedicao) {
		
		boolean retornoPermiteComplementacao = false;

		if(securityContext.isUserInProfile(PROPONENTE_CONVENENTE)) {
			retornoPermiteComplementacao = verificarMedicaoPermiteComplementacaoEmpresa(consultarMedicaoBD(idMedicao));
		}
		
		if(securityContext.isUserInProfile(CONCEDENTE) || securityContext.isUserInProfile(MANDATARIA)) {
			retornoPermiteComplementacao = verificarMedicaoPermiteComplementacaoConvenente(consultarMedicaoBD(idMedicao));
		}
		
		return retornoPermiteComplementacao;
	}

	private boolean verificarMedicaoPermiteComplementacaoEmpresa(MedicaoBD medicao) {

		boolean permite = false;

		if ((medicao.getSituacao().equals(AT) || medicao.getSituacao().equals(CC))
				&& medicao.getIdMedicaoAgrupadora() == null && !medicao.isBloqueada()) {

			Map<Long, SituacaoMedicaoEnum> situacoesMedicoesContrato = getMedicaoDAO()
					.listarSituacoesMedicoes(medicao.getIdContratoMedicao());

			permite = !existeMedicaoPosteriorEmEdicaoConvenente(medicao.getId(), situacoesMedicoesContrato)
					&& !existeSituacaoComplementacaoEmpresa(situacoesMedicoesContrato);
		}

		return permite;
	}
	
	private boolean verificarMedicaoPermiteComplementacaoConvenente(MedicaoBD medicao) {

		boolean permite = false;

		if (medicao.getSituacao().equals(SituacaoMedicaoEnum.AC) && medicao.getIdMedicaoAgrupadora() == null) {

			Map<Long, SituacaoMedicaoEnum> situacoesMedicoesContrato = getMedicaoDAO()
					.listarSituacoesMedicoes(medicao.getIdContratoMedicao());

			permite = !existeMedicaoPosteriorEnviadaConcedenteMandataria(medicao.getId(), situacoesMedicoesContrato)
					&& !existeSituacaoComplementacaoConvenente(situacoesMedicoesContrato);
		}

		return permite;
	}

	private boolean existeMedicaoPosteriorEmEdicaoConvenente(Long idMedicao,
			Map<Long, SituacaoMedicaoEnum> situacoesMedicoesContrato) {

            return situacoesMedicoesContrato.entrySet().stream()
				.anyMatch(entry -> entry.getKey().longValue() > idMedicao.longValue()
						&& (entry.getValue().equals(EC)||entry.getValue().equals(AT)));
	}
	
	
	private boolean existeMedicaoPosteriorEnviadaConcedenteMandataria(Long idMedicao,
			Map<Long, SituacaoMedicaoEnum> situacoesMedicoesContrato) {

		return situacoesMedicoesContrato.entrySet().stream()
				.anyMatch(entry -> entry.getKey().longValue() > idMedicao.longValue()
						&& entry.getValue().equals(ATD));
	}
	

	private boolean existeSituacaoComplementacaoEmpresa(Map<Long, SituacaoMedicaoEnum> situacoesMedicoesContrato) {

		return situacoesMedicoesContrato.containsValue(CE)
				|| situacoesMedicoesContrato.containsValue(ECE);
	}
	
	private boolean existeSituacaoComplementacaoConvenente(Map<Long, SituacaoMedicaoEnum> situacoesMedicoesContrato) {

		return situacoesMedicoesContrato.containsValue(SituacaoMedicaoEnum.CC)
				|| situacoesMedicoesContrato.containsValue(SituacaoMedicaoEnum.ECC);
	}

	private boolean existeSituacaoEmElaboracao(Map<Long, SituacaoMedicaoEnum> situacoesMedicoesContrato) {

		return situacoesMedicoesContrato.containsValue(SituacaoMedicaoEnum.EM);
	}

	/**
	 * Realiza a solicitação de complementação de uma medição individualmente ou de
	 * forma acumulada (quando a medição informada é uma agrupadora).
	 * 
	 * @param idMedicao O identificador da medição que será solicitada
	 *                  complementação.
	 * @return Um objeto {@link MedicaoDTO} com os dados da medição após atualização
	 * @throws NullPointerException se o idMedicao é {@code null}
	 * @throws MedicaoRestException se a medição não existir ou não possuir os
	 *                              parâmetros necessários que permitam a operação
	 *                              de solicitar complementação
	 */
	public MedicaoDTO solicitarComplementacaoEmpresa(Long idMedicao) {
		
		MedicaoBD medicao = consultarMedicaoBD(idMedicao);

		if ((securityContext.isUserInProfile(PROPONENTE_CONVENENTE)
				&& !verificarMedicaoPermiteComplementacaoEmpresa(medicao))) {
			throw new MedicaoRestException(MessageKey.ERRO_COMPLEMENTACAO_EMPRESA_NAO_PERMITIDA);
		}

		if (!observacaoBC.existeObservacaoCadastradaPorUsuarioDesbloqueadaNaMedicao(idMedicao, this.getCpfUsuario())) {
			throw new MedicaoRestException(MessageKey.ERRO_NECESSARIO_CADASTRAR_PELO_MENOS_UMA_OBSERVACAO);
		}

		ContratoBD contrato = getContratoDAO().consultarContrato(medicao.getIdContratoMedicao());

		jdbi.useTransaction(handle -> {
			if (isNotFalse(medicao.getPermiteComplementacaoValor())) {
				submetaBc.apagarMarcacoesConvenenteSubmetasMedicao(handle, medicao, contrato);

				List<MedicaoBD> listaMedicoesAcumuladas = handle.attach(MedicaoDAO.class)
						.listarMedicoesAcumuladas(medicao.getId());

				listaMedicoesAcumuladas.forEach(medicaoFilha ->
				// Descarta marcações das submetas e dos itens medição
				submetaBc.apagarMarcacoesConvenenteSubmetasMedicao(handle, medicaoFilha, contrato));
			}

			alterarMedicaoEAcumuladasComHistorico(medicao, SituacaoMedicaoEnum.ECE, handle);
			bloqueiaMedicaoNaSituacao(medicao, SituacaoMedicaoEnum.EM, handle);
		});

		return getMedicaoDAO().obterMedicao(idMedicao);
	}

	/**
	 * Realiza a solicitação de complementação de uma medição individualmente ou de
	 * forma acumulada (quando a medição informada é uma agrupadora).
	 * 
	 * @param idMedicao O identificador da medição que será solicitada
	 *                  complementação.
	 * @return Um objeto {@link MedicaoDTO} com os dados da medição após atualização
	 * @throws NullPointerException se o idMedicao é {@code null}
	 * @throws MedicaoRestException se a medição não existir ou não possuir os
	 *                              parâmetros necessários que permitam a operação
	 *                              de solicitar complementação
	 */
	public MedicaoDTO solicitarComplementacaoConvenente(VistoriaExtraDTO vistoriaExtraDTO, Long idMedicao) {
		MedicaoBD medicao = consultarMedicaoBD(idMedicao);

		if (((securityContext.isUserInProfile(CONCEDENTE) || securityContext.isUserInProfile(MANDATARIA))
				&& !verificarMedicaoPermiteComplementacaoConvenente(medicao))) {
			throw new MedicaoRestException(MessageKey.ERRO_COMPLEMENTACAO_MEDICAO_NAO_PERMITIDA);
		}

		if (!usuarioTemPermissaoAdministrador() && !observacaoBC
				.existeObservacaoCadastradaPorUsuarioDesbloqueadaNaMedicao(idMedicao, this.getCpfUsuario())) {
			throw new MedicaoRestException(MessageKey.ERRO_NECESSARIO_CADASTRAR_PELO_MENOS_UMA_OBSERVACAO);
		}

		ContratoBD contrato = getContratoDAO().consultarContrato(medicao.getIdContratoMedicao());

		jdbi.useTransaction(handle -> {
			//Validação do Salvar Medição Concedente Mandataria
	 		validaAlterarConcedenteMandataria(vistoriaExtraDTO, medicao, false);
	 		
	 		if(!vistoriaExtraDTO.isVistoriaExtra()) {
				medicao.setSolicitanteVistoriaExtra(null);
			}
	 		
			if (usuarioTemPermissaoAdministrador()) {
				submetaBc.apagarMarcacoesConcedenteSubmetasMedicao(handle, medicao, contrato);
				medicao.setPermiteComplementacaoValor(true);
			} else {
				medicao.setPermiteComplementacaoValor(false);
			}

			alterarMedicaoEAcumuladasComHistorico(medicao, SituacaoMedicaoEnum.ECC, handle);
			bloqueiaMedicaoNaSituacao(medicao, SituacaoMedicaoEnum.AT, handle);			
		});

		return getMedicaoDAO().obterMedicao(idMedicao);
	}
	
	private void bloqueiaMedicaoNaSituacao(MedicaoBD medicao, SituacaoMedicaoEnum situacao, Handle handle) {
		MedicaoDAO medicaoDao = handle.attach(MedicaoDAO.class);
		medicaoDao.consultarMedicaoporSituacao(medicao.getIdContratoMedicao(), situacao)
		.forEach(medicaoDao::bloquearMedicao);
	}

	/**
	 * Inicia a Complementação de uma Medição pela Empresa ou Pelo Convenente.
	 * 
	 * EMPRESA:
	 * - Altera a situação da mediação, e acumuladas (filhas), de "Enviada para
	 * Complementação da Empresa" (ECE) para "Em Complementação pela Empresa" (CE).
	 * 
	 * - Bloqueia as Observações incluídas pelo convenente na medição em questão.
	 * 
	 * CONVENENTE:
	 * - Altera a situação da medição, e acumuladas se houver (filhas), de "Enviada para
	 * Complementação do Convenente" (ECC) para "Em Complementação pelo Convenente" (CC).
	 * 
	 * - Bloqueia as Observações incluídas pelo concedente/mandatária na medição em questão.
	 * 
	 * @param idMedicao
	 */
	public MedicaoDTO iniciarComplementacao(Long idMedicao) {

		MedicaoBD medicao = consultarMedicaoBD(idMedicao);

		if(securityContext.isUserInProfile(EMPRESA)) {
			validarIniciarComplementacaoEmpresa(medicao);

			jdbi.useTransaction(handle -> {
				alterarMedicaoEAcumuladasComHistorico(medicao, SituacaoMedicaoEnum.CE, handle);
				observacaoBC.bloquearObservacao(handle, idMedicao, Profile.PROPONENTE_CONVENENTE);
			}); 
		} else if(securityContext.isUserInProfile(PROPONENTE_CONVENENTE)) {
			validarIniciarComplementacaoConvenente(medicao);

			jdbi.useTransaction(handle -> {
				alterarMedicaoEAcumuladasComHistorico(medicao, SituacaoMedicaoEnum.CC, handle);
				observacaoBC.bloquearObservacao(handle, idMedicao, Profile.CONCEDENTE);
				observacaoBC.bloquearObservacao(handle, idMedicao, Profile.MANDATARIA);
			});
		}

		return getMedicaoDAO().obterMedicao(idMedicao);
	}

	private void alterarMedicaoEAcumuladasComHistorico(MedicaoBD medicao, SituacaoMedicaoEnum novaSituacao, Handle handle) {
		medicao.setSituacao(novaSituacao);
		alterarMedicao(handle, medicao, true);
		alterarMedicoesAcumuladas(handle, medicao);
	}

	private void validarIniciarComplementacaoEmpresa(MedicaoBD medicao) {

		if (!medicao.getSituacao().equals(SituacaoMedicaoEnum.ECE)) {
			throw new MedicaoRestException(MessageKey.ERRO_SITUACAO_INVALIDA_PARA_INICIAR_COMPLEMENTACAO_EMPRESA);
		}

		validarComplementacaoMedicaoAcumulada(medicao);
	}
	
	private void validarIniciarComplementacaoConvenente(MedicaoBD medicao) {

		if (!medicao.getSituacao().equals(SituacaoMedicaoEnum.ECC)) {
			throw new MedicaoRestException(MessageKey.ERRO_SITUACAO_INVALIDA_PARA_INICIAR_COMPLEMENTACAO_CONVENENTE);
		}

		validarComplementacaoMedicaoAcumulada(medicao);
	}
	
	private void validarComplementacaoMedicaoAcumulada(MedicaoBD medicao) {
		if (medicao.getIdMedicaoAgrupadora() != null) {
			throw new MedicaoRestException(MessageKey.ERRO_COMPLEMENTACAO_MEDICAO_ACUMULADA);
		}
	}

	private MedicaoBD consultarMedicaoBD(Long idMedicao) {

		Objects.requireNonNull(idMedicao, "Parâmetro idMedicao não pode ser nulo");

		MedicaoBD medicao = getMedicaoDAO().consultarMedicao(idMedicao);

		if (medicao == null) {
			throw new MedicaoRestException(MessageKey.ERRO_MEDICAO_NAO_ENCONTRADA, Status.NOT_FOUND.getStatusCode());
		}

		return medicao;
	}

	public void cancelarEnvioConcedente(Long idMedicao) {

		MedicaoBD medicao = consultarMedicaoBD(idMedicao);

		if (!permiteCancelarEnvioConcedente(idMedicao)) {
			throw new MedicaoRestException(
					new Message(MessageKey.ERRO_CANCELAR_ENVIO_CONCEDENTE_SITUACAO_NAO_PREVISTA));
		}

		HistoricoMedicaoBD penultimoHistorico = recuperaPenultimoHistorico(medicao);

		jdbi.useTransaction(handle -> 
					alterarMedicaoEAcumuladasComHistorico(medicao, penultimoHistorico.getSituacao(), handle)
					);
	}

	private boolean permiteCancelarEnvioConcedente(Long idMedicao) {
		return getMedicaoDAO().permiteCancelarEnvioConcedente(idMedicao);
	}

	/**
	 * Cancelar envio de medição para Complementacao da Empresa ou Convenente
	 * @param idMedicao
	 */
	public void cancelarEnvioParaComplementacao(Long idMedicao) {
		
		List<SituacaoMedicaoEnum> situacoesEmEdicao = new ArrayList<>();
		SituacaoMedicaoEnum situacaoEmComplementacao = SituacaoMedicaoEnum.ECE;
		SituacaoMedicaoEnum situacaoMedicaoBloqueada = SituacaoMedicaoEnum.EM;
		
		MessageKey messageKey = MessageKey.ERRO_MEDICAO_NAO_PODE_SER_ALTERADA;
		
		if(securityContext.isUserInProfile(PROPONENTE_CONVENENTE)) {
			situacoesEmEdicao.add(SituacaoMedicaoEnum.AT);
			situacoesEmEdicao.add(SituacaoMedicaoEnum.CC);
			situacoesEmEdicao.add(SituacaoMedicaoEnum.ECC);
			
			messageKey = MessageKey.ERRO_CANCELAR_ENVIO_PARA_COMPLEMENTACAO_EMPRESA_SITUACAO_NAO_PREVISTA;
			
		} else if(securityContext.isUserInProfile(CONCEDENTE) || securityContext.isUserInProfile(MANDATARIA)){				
			situacoesEmEdicao.add(SituacaoMedicaoEnum.AC);
			situacaoEmComplementacao = SituacaoMedicaoEnum.ECC;
			situacaoMedicaoBloqueada = SituacaoMedicaoEnum.AT;
			
			messageKey = MessageKey.ERRO_CANCELAR_ENVIO_PARA_COMPLEMENTACAO_CONVENENTE_SITUACAO_NAO_PREVISTA;
		}
		
		MedicaoBD medicao = consultarMedicaoBD(idMedicao);
		
		if (!permiteCancelarEnvioParaComplementacao(medicao,situacaoEmComplementacao, situacoesEmEdicao)) {
			throw new MedicaoRestException(messageKey);
		}

		HistoricoMedicaoBD penultimoHistorico = recuperaPenultimoHistorico(medicao);

		atualizaMedicaoEnvioParaComplementacaoCancelado(medicao, situacaoMedicaoBloqueada, penultimoHistorico);
	}

	private void atualizaMedicaoEnvioParaComplementacaoCancelado(MedicaoBD medicao,
			SituacaoMedicaoEnum situacaoMedicaoBloqueada, HistoricoMedicaoBD penultimoHistorico) {
		
		jdbi.useTransaction(handle -> {
			
			if (medicao.getSituacao().equals(SituacaoMedicaoEnum.ECC)) {
				medicao.setPermiteComplementacaoValor(null);
			}
			
			alterarMedicaoEAcumuladasComHistorico(medicao, penultimoHistorico.getSituacao(), handle);

			MedicaoDAO medicaoDAO = handle.attach(MedicaoDAO.class);
			medicaoDAO.consultarMedicaoporSituacao(medicao.getIdContratoMedicao(), situacaoMedicaoBloqueada)
					.forEach(medicaoDAO::desbloquearMedicao);
		});
	}

	private HistoricoMedicaoBD recuperaPenultimoHistorico(MedicaoBD medicao) {
		return historicoBC
				.recuperarPenultimoHistoricoPorMedicaoContrato(medicao.getIdContratoMedicao(), medicao.getNrSequencial())
				.orElseThrow(() -> new MedicaoRestException(MessageKey.ERRO_PENULTIMO_HISTORICO_MEDICAO_NAO_ENCONTRADO, Status.NOT_FOUND.getStatusCode()));
	}

	private boolean permiteCancelarEnvioParaComplementacao(MedicaoBD medicao, SituacaoMedicaoEnum situacaoEmComplementacao, List<SituacaoMedicaoEnum> situacoes) {
		return medicao.getSituacao().equals(situacaoEmComplementacao) && medicao.getIdMedicaoAgrupadora() == null
				&& !getMedicaoDAO().existeMedicao(medicao.idContratoMedicao, situacoes);
	}
	
	/**
	 * Inicia o Análise de uma Medição pelo Concedente/Mandatária.
	 * 
	 * @param idMedicao
	 */
	public MedicaoDTO iniciarAnalise(Long idMedicao) {

		MedicaoBD medicao = consultarMedicaoBD(idMedicao);
		
		validarIniciarAnalise(medicao);

		List<MedicaoBD> listaMedicoesAnteriores = getMedicaoDAO().listarMedicoesAnterioresPorSituacao(
				medicao.getIdContratoMedicao(), medicao.getNrSequencial(), List.of(SituacaoMedicaoEnum.AC,
						SituacaoMedicaoEnum.ATD));

		ContratoBD contrato = getContratoDAO().consultarContrato(medicao.getIdContratoMedicao());

		jdbi.useTransaction(handle -> {

			// Bloqueia as observações do Convenente
			observacaoBC.bloquearObservacao(handle, idMedicao, Profile.PROPONENTE_CONVENENTE);

			// (Agrupamento) - Verifica se há outras medições anteriores a serem tratadas.
			listaMedicoesAnteriores.forEach(medicaoFilha -> {

				medicaoFilha.setIdMedicaoAgrupadora(medicao.getId());

				if (medicaoFilha.getSituacao().equals(SituacaoMedicaoEnum.AC)) {

					// Move observações não bloqueadas do concedente/mandataria da medição agrupada para a
					// medição agrupadora
					observacaoBC.moverObservacaoMedicaoAgrupadaParaMedicaoAtualConcedenteMandataria(handle, medicaoFilha);

					// Descarta marcações das submetas e dos itens medição 
					submetaBc.apagarMarcacoesConcedenteSubmetasMedicao(handle, medicaoFilha, contrato);

					// Descarta possíveis informações de vistoria extra
					medicaoFilha.setDataVistoriaExtra(null);
					medicaoFilha.setVistoriaExtra(FALSE);
					medicaoFilha.setSolicitanteVistoriaExtra(null);
					
					// Aplica alteracao do idMedicaoAgrupadora na medicão filha
					alterarMedicao(handle, medicaoFilha, false);

				} else {

					// Bloqueia as observações inseridas pelo convenente na medição filha
					observacaoBC.bloquearObservacaoMedicaoFilha(handle, medicaoFilha, Profile.PROPONENTE_CONVENENTE);

					// Descarta marcações das submetas e dos itens medição 
					submetaBc.apagarMarcacoesConcedenteSubmetasMedicao(handle, medicaoFilha, contrato);

					medicaoFilha.setSituacao(SituacaoMedicaoEnum.AC);
					
					// Descarta possíveis informações de vistoria extra
					medicaoFilha.setDataVistoriaExtra(null);
					medicaoFilha.setVistoriaExtra(FALSE);
					medicaoFilha.setSolicitanteVistoriaExtra(null);

					// Limpa indicador permite complementação com alteração na medição filha
					medicaoFilha.setPermiteComplementacaoValor(null);

					// Aplica a alteração da situação e idMedicaoAgrupadora na medição filha
					alterarMedicao(handle, medicaoFilha, true);
				}
			});

			// Altera a situação da medição original
			medicao.setSituacao(SituacaoMedicaoEnum.AC);

			// Limpa indicador permite complementação com alteração
			medicao.setPermiteComplementacaoValor(null);

			alterarMedicao(handle, medicao, true);
		});
		
		return getMedicaoDAO().obterMedicao(idMedicao);
	}

	/**
	 * Verifica se a medição está:
	 * - em situação válida para iniciar análise, e
	 * - se não se trata de uma medição filha (agrupada, ou seja idMedicaoAgrupadora != null), e
	 * - se trata de um contrato acompanhando por eventos
	 * 
	 * @param medicao
	 */
	private void validarIniciarAnalise(MedicaoBD medicao) {
		
		if (!medicao.getSituacao().equals(SituacaoMedicaoEnum.ATD)) {
			throw new MedicaoRestException(MessageKey.ERRO_SITUACAO_INVALIDA_PARA_INICIAR_ANALISE);
		}
		
		if(medicao.getIdMedicaoAgrupadora() != null) {
			throw new MedicaoRestException(
					new Message(MessageKey.ERRO_INICIAR_ANALISE_MEDICAO_AGRUPADA));
		}
	}
	
	
	/**
	 * Obtem as Medições filhas(se houver) da Medição informada. 
	 * 
	 * 	Se @param submetasPreenchidas = true
	 *  	carrega a lista de submetas com algum valor previamente informado para o Usuario logado.
	 *  Se @param submetasPreenchidas for false 
	 *  	retorna apenas o MedicaoAgrupadaDTO
	 *  Se @param incluirMedicaoAgrupadora = true
	 *  	inclui na lista de mediçoes agrupadas a própria medição acumuladora. 
	 *  
	 *  @throws MedicaoRestException caso seja informado uma medição que não seja agrupadora será levantado exceção throw
	 * 
	 * @param idMedicaoAgrupadora
	 * @param submetasPreenchidas
	 * @param incluirMedicaoAgrupadora
	 * 
	 * @return
	 */
	public List<MedicaoAgrupadaDTO> listarMedicoesAgrupadas(Long idMedicaoAgrupadora, 
										Boolean submetasPreenchidas){
		
		MedicaoBD medicao = getMedicaoDAO().consultarMedicao(idMedicaoAgrupadora);
		
		if (medicao != null && medicao.getIdMedicaoAgrupadora() == null) {
			return getMedicaoDAO().listarMedicoesAcumuladas(idMedicaoAgrupadora).stream()
					.map(med -> obterSubmetasMarcadas(med, submetasPreenchidas))
					.collect(Collectors.toList());
		} else {
			throw new MedicaoRestException(MessageKey.ERRO_MEDICAO_ACUMULADA);
		}

		
		
	}

	/**
	 * Se @param submetasPreenchidas = true então adiciona apenas as submetas da
	 * Medição Agrupada que permitem manutenção pelo ator solicitante.
	 * 
	 * @param medicaoBD
	 * @param submetasPreenchidas
	 * @return
	 */
	private MedicaoAgrupadaDTO obterSubmetasMarcadas(MedicaoBD medicaoBD, Boolean submetasPreenchidas) {

		MedicaoAgrupadaDTO medicaoAgrupadaDTO = new MedicaoAgrupadaDTO(medicaoBD);

		if (isTrue(submetasPreenchidas) && (usuarioTemPermissaoEmpresa() || usuarioTemPermissaoConvenente())) {

			Predicate<SubmetaMedicaoDTO> filtro = usuarioTemPermissaoEmpresa()
					? SubmetaMedicaoDTO::isPermiteMarcacaoEmpresa
					: SubmetaMedicaoDTO::isPermiteMarcacaoConvenente;

			List<SubmetaMedicaoDTO> listaSubmetasPreenchidas = submetaBc
					.recuperarListaSubmetasPorMedicao(medicaoBD.getId())
					.stream()
					.filter(filtro)
					.collect(toList());

			medicaoAgrupadaDTO.setListaSubmetasPreenchidas(listaSubmetasPreenchidas);
		}

		return medicaoAgrupadaDTO;
	}

	private MedicaoDAO getMedicaoDAO() {
		return jdbi.onDemand(MedicaoDAO.class);
	}

	private SubmetaDAO getSubmetaDAO() {
		return jdbi.onDemand(SubmetaDAO.class);
	}
	
	private ContratoDAO getContratoDAO() {
		return jdbi.onDemand(ContratoDAO.class);
	}
	
	private ItemMedicaoDAO getItemMedicaoDAO() {
		return jdbi.onDemand(ItemMedicaoDAO.class);
	}

}