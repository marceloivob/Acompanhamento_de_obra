package br.gov.planejamento.siconv.med.medicao.business;

import static br.gov.planejamento.siconv.med.infra.message.MessageKey.ERRO_ACUMULADO_SERVICO_CONVENENTE_MAIOR_ACUMULADO_SERVICO_EMPRESA;
import static br.gov.planejamento.siconv.med.infra.message.MessageKey.ERRO_ACUMULADO_SERVICO_CONVENENTE_MEDICAO_POSTERIOR_MAIOR_ACUMULADO_SERVICO_EMPRESA;
import static br.gov.planejamento.siconv.med.infra.message.MessageKey.ERRO_ITEM_MEDICAO_NAO_PERMITE_MUDANCA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Permission.ASSINAR_SUBMETA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.EMPRESA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.MANDATARIA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.PROPONENTE_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.FISCAL_ACOMPANHAMENTO;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.FISCAL_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.TECNICO_TERCEIRO;
import static br.gov.planejamento.siconv.med.infra.util.MathUtil.is;
import static br.gov.planejamento.siconv.med.infra.util.MathUtil.isEqualToZero;
import static br.gov.planejamento.siconv.med.infra.util.MathUtil.nullSafeAdd;
import static br.gov.planejamento.siconv.med.infra.util.MathUtil.zeroIfNull;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.AC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.AT;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.CC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.CE;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.EM;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoSubmetaEnum.RAS;
import static java.util.Arrays.asList;
import static java.util.Collections.sort;
import static java.util.Comparator.comparing;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collector;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.core.Response.Status;

import org.jdbi.v3.core.Handle;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.TipoResponsavelTecnicoEnum;
import br.gov.planejamento.siconv.med.contrato.dao.ContratoDAO;
import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.infra.database.DAOFactory;
import br.gov.planejamento.siconv.med.infra.exception.MedicaoRestException;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.infra.security.domain.Profile;
import br.gov.planejamento.siconv.med.infra.util.LazySupplier;
import br.gov.planejamento.siconv.med.integration.contratos.ContratosGrpcConsumer;
import br.gov.planejamento.siconv.med.medicao.business.builder.AssinaturaSubmetaStep;
import br.gov.planejamento.siconv.med.medicao.business.builder.CalculoValoresServicoStep;
import br.gov.planejamento.siconv.med.medicao.business.builder.CalculoValoresSubmetaPorEventoStep;
import br.gov.planejamento.siconv.med.medicao.business.builder.CalculoValoresSubmetaPorServicoStep;
import br.gov.planejamento.siconv.med.medicao.business.builder.DadosGeraisSubmetaStep;
import br.gov.planejamento.siconv.med.medicao.business.builder.FiltroCamposDesnecessariosListagemSubmetaStep;
import br.gov.planejamento.siconv.med.medicao.business.builder.FiltroMarcacoesEventosSubmetaStep;
import br.gov.planejamento.siconv.med.medicao.business.builder.FiltroSituacoesSubmetaStep;
import br.gov.planejamento.siconv.med.medicao.business.builder.FiltroValoresServicosSubmetaStep;
import br.gov.planejamento.siconv.med.medicao.business.builder.IndicadoresSubmetaEventoStep;
import br.gov.planejamento.siconv.med.medicao.business.builder.IndicadoresSubmetaServicoStep;
import br.gov.planejamento.siconv.med.medicao.business.builder.PreencherListaFrenteObrasStep;
import br.gov.planejamento.siconv.med.medicao.business.builder.PrepararEventoStep;
import br.gov.planejamento.siconv.med.medicao.business.builder.PrepararServicoStep;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder;
import br.gov.planejamento.siconv.med.medicao.dao.ItemMedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.MedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.dao.SubmetaDAO;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoSubmetaEnum;
import br.gov.planejamento.siconv.med.medicao.entity.database.ItemMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.ItemMedicaoBMBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.ItemMedicaoBMValorBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.MedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.SubmetaMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.dto.EventoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.FrenteObraVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoVrplDTO.ValorServicoBM;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ValoresSubmetaDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.submetaservicosalvar.EventoSubmetaSalvarDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.submetaservicosalvar.FrenteObraSubmetaSalvarDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.submetaservicosalvar.ServicoSubmetaSalvarDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.submetaservicosalvar.SubmetaSalvarDTO;

@ApplicationScoped
public class SubmetaBC {

	@Inject
	private DAOFactory dao;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private ContratosGrpcConsumer contratosConsumer;

    @Inject
    private PerfilHelper perfilHelper;

    @Inject
    private SubmetaMedicaoBuilder submetaBuilder;

    /**
     * Recuperar lista de submetas relacionadas ao contrato/licitação e medição em
     * questão
     * 
     * @param idMedicao
     * @return
     */
    public List<SubmetaMedicaoDTO> recuperarListaSubmetasPorMedicao(Long idMedicao) {

        MedicaoDTO medicao = dao.get(MedicaoDAO.class).obterMedicao(idMedicao);

        ContratoBD contratoBD = dao.get(ContratoDAO.class).consultarContrato(medicao.getIdContrato());

        List<SubmetaMedicaoDTO> submetasMedicao = listarSubmetasMedicao(contratoBD, medicao);

        submetaBuilder.of(contratoBD, medicao)
                .add(DadosGeraisSubmetaStep.class)
                .add(FiltroSituacoesSubmetaStep.class)

                .when(contratoBD.isInAcompanhamentoEventos())
                        .add(FiltroMarcacoesEventosSubmetaStep.class)
                        .add(CalculoValoresSubmetaPorEventoStep.class)
                        .add(IndicadoresSubmetaEventoStep.class)

                .orElse().add(FiltroValoresServicosSubmetaStep.class)
                         .add(CalculoValoresServicoStep.class)
                         .add(CalculoValoresSubmetaPorServicoStep.class)
                         .add(IndicadoresSubmetaServicoStep.class)

                .anyway().add(FiltroCamposDesnecessariosListagemSubmetaStep.class)

                .build(submetasMedicao);
        
        sort(submetasMedicao, 
        		comparing(SubmetaMedicaoDTO::getNrMeta)
        		.thenComparing(SubmetaMedicaoDTO::getNrSubmeta));
        
        return submetasMedicao;
    }

    private List<SubmetaMedicaoDTO> listarSubmetasMedicao(ContratoBD contrato, MedicaoDTO medicao) {

        if (contrato.isInAcompanhamentoEventos()) {
            return dao.get(SubmetaDAO.class).listarSubmetasMedicao(contrato.getId(), medicao.getId());

        } else {
            return dao.get(SubmetaDAO.class).listarSubmetasMedicaoBM(contrato.getId(), medicao.getId());
        }
    }

    public void totalizarValoresSubmetasPorMedicao(List<MedicaoDTO> medicoes) {

        if (!medicoes.isEmpty()) {

            Long idContratoMedicao = medicoes.get(0).getIdContrato();

            ContratoBD contratoBD = dao.get(ContratoDAO.class).consultarContrato(idContratoMedicao);

            medicoes.forEach(medicao -> {

                List<SubmetaMedicaoDTO> submetasMedicao = listarSubmetasMedicao(contratoBD, medicao);

                submetaBuilder.of(contratoBD, medicao)
                        .add(DadosGeraisSubmetaStep.class)
                        .add(FiltroSituacoesSubmetaStep.class)

                        .when(contratoBD.isInAcompanhamentoEventos())
                                .add(FiltroMarcacoesEventosSubmetaStep.class)
                                .add(CalculoValoresSubmetaPorEventoStep.class)

                        .orElse().add(FiltroValoresServicosSubmetaStep.class)
                                 .add(CalculoValoresServicoStep.class)
                                 .add(CalculoValoresSubmetaPorServicoStep.class)

                        .build(submetasMedicao);

                medicao.setValoresTotalSubmetas(totalizarValoresSubmetas(submetasMedicao));
            });
        }
    }

	public ValoresSubmetaDTO totalizarValoresSubmetas(List<SubmetaMedicaoDTO> submetas) {

		BiFunction<ValoresSubmetaDTO, SubmetaMedicaoDTO, ValoresSubmetaDTO> reducer = (acumulador, submeta) -> {

			acumulador.setValorSubmeta(nullSafeAdd(acumulador.getValorSubmeta(), submeta.getValor()));

			acumulador.setValorRealizadoEmpresa(
					nullSafeAdd(acumulador.getValorRealizadoEmpresa(), submeta.getValorRealizadoEmpresa()));

			acumulador.setValorRealizadoAcumuladoEmpresa(nullSafeAdd(acumulador.getValorRealizadoAcumuladoEmpresa(),
					submeta.getValorRealizadoAcumuladoEmpresa()));

			acumulador.setValorRealizadoConvenente(
					nullSafeAdd(acumulador.getValorRealizadoConvenente(), submeta.getValorRealizadoConvenente()));

			acumulador.setValorRealizadoAcumuladoConvenente(nullSafeAdd(
					acumulador.getValorRealizadoAcumuladoConvenente(), submeta.getValorRealizadoAcumuladoConvenente()));

			acumulador.setValorRealizadoConcedente(
					nullSafeAdd(acumulador.getValorRealizadoConcedente(), submeta.getValorRealizadoConcedente()));

			acumulador.setValorRealizadoAcumuladoConcedente(nullSafeAdd(
					acumulador.getValorRealizadoAcumuladoConcedente(), submeta.getValorRealizadoAcumuladoConcedente()));

			return acumulador;
		};

		return submetas.stream().reduce(new ValoresSubmetaDTO(), reducer, (t, s) -> t);
	}

    /**
     * Recuperar uma submeta e seus itens relacionados da medição em questão
     * 
     * @param idMedicao, idSubmeta
     * @return
     */
    public SubmetaMedicaoDTO recuperarSubmetaPorMedicao(Long idMedicao, Long idSubmetaVrpl) {

        MedicaoDTO medicao = dao.get(MedicaoDAO.class).obterMedicao(idMedicao);

        ContratoBD contrato = dao.get(ContratoDAO.class).consultarContrato(medicao.getIdContrato());

        SubmetaMedicaoDTO submetaMedicao = listarSubmetasMedicao(contrato, medicao).stream()
                .filter(submeta -> submeta.getId().equals(idSubmetaVrpl)).findFirst()
                .orElseThrow(() -> new MedicaoRestException(MessageKey.ERRO_SUBMETA_INEXISTENTE,
                        Status.NOT_FOUND.getStatusCode()));

        submetaBuilder.of(contrato, medicao)
                .add(DadosGeraisSubmetaStep.class)
                .add(FiltroSituacoesSubmetaStep.class)
                .add(AssinaturaSubmetaStep.class)
                .add(PreencherListaFrenteObrasStep.class)

                .when(contrato.isInAcompanhamentoEventos())
                        .add(FiltroMarcacoesEventosSubmetaStep.class)
                        .add(CalculoValoresSubmetaPorEventoStep.class)
                        .add(IndicadoresSubmetaEventoStep.class)
                        .add(PrepararEventoStep.class)

                 .orElse().add(FiltroValoresServicosSubmetaStep.class)
                          .add(CalculoValoresServicoStep.class)
                          .add(CalculoValoresSubmetaPorServicoStep.class)
                          .add(IndicadoresSubmetaServicoStep.class)
                          .add(PrepararServicoStep.class)

                .build(submetaMedicao);

        return submetaMedicao;
    }

	/*
	 * Verifica se o usuário logado é perfil empresa e pode realizar marcação na submeta
	 */
	private boolean permiteEdicaoSubmetaPelaEmpresa(SubmetaMedicaoDTO submetaMedicao) {
		return securityContext.isUserInProfile(EMPRESA) && submetaMedicao.isPermiteMarcacaoEmpresa();
	}

	/*
	 * Verifica se o usuário logado é perfil convenente e pode realizar marcação na submeta
	 */
	private boolean permiteEdicaoSubmetaPeloConvenente(SubmetaMedicaoDTO submetaMedicao) {
		return securityContext.isUserInProfile(PROPONENTE_CONVENENTE) && submetaMedicao.isPermiteMarcacaoConvenente();
	}
	
	/*
	 * Verifica se o usuário logado é perfil convenente e pode realizar marcação na submeta
	 */
	private boolean permiteEdicaoSubmetaPeloConcedente(SubmetaMedicaoDTO submetaMedicao) {
		return  (securityContext.isUserInProfile(CONCEDENTE) || securityContext.isUserInProfile(Profile.MANDATARIA))
				&& 
				submetaMedicao.isPermiteMarcacaoConcedente();
	}


	/**
	 * Verifica se o Evento foi Marcado em uma Medição que está em Elaboração e é posterior a Medição Atual 
	 * que está sendo mantida. 
	 * 
	 * @param idMedicaoEmpresa
	 * @return
	 */
	private boolean medicaoPosteriorEmElaboracao(Long idMedicaoEventoMarcadoEmpresa, MedicaoBD medicaoAtual) {
		
		// Consulta para saber situação da Medição
		if (idMedicaoEventoMarcadoEmpresa != null && medicaoAtual != null) {
			MedicaoBD medicaoEventoMarcadoEmpresa = dao.get(MedicaoDAO.class).consultarMedicao(idMedicaoEventoMarcadoEmpresa);
			
			// Se a medição marcada no evento da Empresa for Em Elaboração e for posterior a Medição Atual 
			if (medicaoEventoMarcadoEmpresa.getSituacao().equals(SituacaoMedicaoEnum.EM) &&
					idMedicaoEventoMarcadoEmpresa > medicaoAtual.getId()) {
				return Boolean.TRUE;
			}
		}
		
		return Boolean.FALSE;
	}

	/**
	 * Verifica se o Evento foi Marcado em uma Medição que está Em Ateste e é posterior a Medição Atual 
	 * que está sendo mantida. 
	 * 
	 * @param idMedicaoConvenente
	 * @return
	 */
	private boolean medicaoPosteriorEmAteste(Long idMedicaoEventoMarcadoConvenente, MedicaoBD medicaoAtual) {
		
		// Consulta para saber situação da Medição
		if (idMedicaoEventoMarcadoConvenente != null && medicaoAtual != null) {
			MedicaoBD medicaoEventoMarcadoConvenente = dao.get(MedicaoDAO.class).consultarMedicao(idMedicaoEventoMarcadoConvenente);
			
			// Se a medição marcada no evento do Convenente for Em Ateste e for posterior a Medição Atual 
			if (medicaoEventoMarcadoConvenente.getSituacao().equals(SituacaoMedicaoEnum.AT) &&
					idMedicaoEventoMarcadoConvenente > medicaoAtual.getId()) {
				return Boolean.TRUE;
			}
		}
		
		return Boolean.FALSE;
	}
	
	/**
	 * Utiliza a submetaMedicao obtida no método recuperarSubmeta que monta uma SubmetaMedicao com seus Eventos e valida
	 * se os mesmos permiteMarcacao ou não.
	 * 
	 * @param submetaMedicao
	 * @param frenteObraJson
	 * @param eventoJson
	 * @return
	 */
	private boolean permiteMarcacaoEvento(SubmetaMedicaoDTO submetaMedicao, FrenteObraSubmetaSalvarDTO frenteObraJson,
			EventoSubmetaSalvarDTO eventoJson) {
		
		for (FrenteObraVrplDTO fo : submetaMedicao.getFrentesObra()) {
			for (EventoVrplDTO ev : fo.getEventos()) {
				if (frenteObraJson.getId().equals(fo.getId()) && eventoJson.getId().equals(ev.getId())) {
					return ev.getPermiteMarcacao().booleanValue();
				}
			}
		}
			
		return Boolean.FALSE;
	}

	/*
	 * Seta situação Rascunho e limpa dados da assinatura, caso a submeta tenha sido
	 * assinada anteriormente
	 */
	private void limparAssinaturaEmpresa(SubmetaMedicaoBD submetaMedicao) {
		submetaMedicao.setNrCpfResponsavelAssinaturaEmpresa(null);
		submetaMedicao.setDtAssinaturaEmpresa(null);
		submetaMedicao.setSituacaoEmpresa(SituacaoSubmetaEnum.RAS);
	}

	/*
	 * Seta situação Rascunho e limpa dados da assinatura, caso a submeta tenha sido
	 * assinada anteriormente
	 */
	private void limparAssinaturaConvenente(SubmetaMedicaoBD submetaMedicao) {
		submetaMedicao.setNrCpfResponsavelAssinaturaConvenente(null);
		submetaMedicao.setDtAssinaturaConvenente(null);
		submetaMedicao.setSituacaoConvenente(SituacaoSubmetaEnum.RAS);
	}

	/*
	 * Seta situação Rascunho e limpa dados da assinatura, caso a submeta tenha sido
	 * assinada anteriormente
	 */
	private void limparAssinaturaConcedenteMandataria(SubmetaMedicaoBD submetaMedicao) {
		submetaMedicao.setNrCpfResponsavelAssinaturaConcedente(null);
		submetaMedicao.setDtAssinaturaConcedente(null);
		submetaMedicao.setSituacaoConcedente(SituacaoSubmetaEnum.RAS);
		submetaMedicao.setInPerfilRespConcedente(null);
	}

	
	private SubmetaMedicaoBD preparaInclusaoSubmetaMedicaoRascunho(MedicaoBD medicaoAtual, Long idSubmetaVrpl) {
		SubmetaMedicaoBD submetaMedicao = new SubmetaMedicaoBD();
		submetaMedicao.setIdSubmetaVrpl(idSubmetaVrpl);
		submetaMedicao.setIdMedicao(medicaoAtual.getId());
		if (securityContext.isUserInProfile(EMPRESA)) {
			limparAssinaturaEmpresa(submetaMedicao);
		}
		if (securityContext.isUserInProfile(PROPONENTE_CONVENENTE)) {
			limparAssinaturaConvenente(submetaMedicao);
		}
		if (securityContext.isUserInProfile(CONCEDENTE) || securityContext.isUserInProfile(MANDATARIA) ) {
			limparAssinaturaConcedenteMandataria(submetaMedicao);
		}
		
		return submetaMedicao;
	}

	/**
	 * Verifica se a submeta é 'assinável', ou seja se o CPF é de um Responsável
	 * Técnico <b>Execução</b> ao qual a Submeta está vinculada
	 * 
	 * @param idContratoSiconv    - id do Contrato SICONV.
	 * @param idMedicao - id da Medicao atual
	 * @param idSubmetaVrpl - id da Submeta no Vrpl.
	 * @param nrCpfUsuario  - número do CPF do usuário.
	 * @return true se o CPF é o do Responsável Técnico ao qual a Submeta está
	 *         vinculada, senão false.
	 */
	public boolean isSubmetaAssinavelPeloCpf(Long idContratoSiconv, Long idMedicao, Long idSubmetaVrpl, String nrCpfUsuario) {

		ContratoBD contratoMedicao = dao.get(ContratoDAO.class).consultarContratoPorContratoFK(idContratoSiconv);
		
		if (contratoMedicao != null) {
			
			MedicaoBD medicao = dao.get(MedicaoDAO.class).consultarMedicao(idMedicao);

			if (medicao != null) {
				if ((securityContext.isUserInProfile(EMPRESA) && medicao.getSituacao().permiteManutencaoEmpresa()) || 
						(securityContext.isUserInProfile(PROPONENTE_CONVENENTE) && medicao.getSituacao().permiteManutencaoConvenente())
						) {
					return isSubmetaAssinavelEmpresaOuConvenente(idContratoSiconv, idSubmetaVrpl, nrCpfUsuario, contratoMedicao);
				} else {
					return isSubmetaAssinavelConcedenteOuMandataria(medicao);
				} 
			} else {
				throw new MedicaoRestException(MessageKey.ERRO_MEDICAO_NAO_ENCONTRADA);				
			}
		} else {
			throw new MedicaoRestException(MessageKey.CONTRATO_INEXISTENTE);
		}

	}

    /**
     * Verifica se o contrato eh assinavel pelo Concedente ou pela Mandataria. Se o
     * usuário logado não for nem Concedente nem Mandataria retorna Falso.
     * 
     * @param medicao
     * @return
     */
    private boolean isSubmetaAssinavelConcedenteOuMandataria(MedicaoBD medicao) {

        return medicao.getSituacao().permiteManutencaoConcedente() && (
                securityContext.hasRoleInProfile(CONCEDENTE, asList(FISCAL_ACOMPANHAMENTO, TECNICO_TERCEIRO)) ||
                securityContext.hasRoleInProfile(MANDATARIA, asList(AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA)));
    }

	
	private Boolean isSubmetaAssinavelEmpresaOuConvenente(Long idContrato, Long idSubmetaVrpl, String nrCpfUsuario,
			ContratoBD contratoMedicao) {

		String tipo = "";
		
		if (securityContext.isUserInProfile(EMPRESA)) {
			if (!securityContext.hasPermissionInProfile(EMPRESA, asList(ASSINAR_SUBMETA))) {
				return false;
			}
			tipo = TipoResponsavelTecnicoEnum.EXE.getCodigo();
		} else if (securityContext.isUserInProfile(PROPONENTE_CONVENENTE)) {
			if (!securityContext.hasRoleInProfile(PROPONENTE_CONVENENTE, asList(FISCAL_CONVENENTE))) {
				return false;
			}
			tipo = TipoResponsavelTecnicoEnum.FIS.getCodigo();
		}

		if (contratoMedicao.isInSocial()) {
			return dao.get(SubmetaDAO.class).isSubmetaContratoSocialAssinavelPeloCpf(idContrato, idSubmetaVrpl,
					nrCpfUsuario, tipo);
		} else {
			return dao.get(SubmetaDAO.class).isSubmetaContratoArqEngAssinavelPeloCpf(idContrato, idSubmetaVrpl,
					nrCpfUsuario, tipo);
		}
	}

	/**
	 * Assina a submeta
	 * 
	 * @param idContrato       - id do Contrato.
	 * @param idMedicao        - id da Medição.
	 * @param idSubmetaVrpl    - id da Submeta no Vrpl.
	 * @param SubmetaSalvarDTO
	 * 
	 */
	public void assinarSubmeta(Long idContrato, Long idMedicao, Long idSubmetaVrpl, SubmetaSalvarDTO submetaSalvarDTO,
			String cpfUsuarioLogado) {

		if (isSubmetaAssinavelPeloCpf(idContrato, idMedicao, idSubmetaVrpl, cpfUsuarioLogado)) {
			salvarSubmeta(idMedicao, idSubmetaVrpl, submetaSalvarDTO, true);
		} else {
			throw new MedicaoRestException(MessageKey.ERRO_USUARIO_NAO_RESPONSAVEL_SUBMETA);
		}

	}

	public void excluirRascunhoSubmeta(Long idMedicao, Long idSubmetaVrpl) {

		final SubmetaMedicaoBD submetaBD = dao.get(SubmetaDAO.class).consultarSubmetaMedicao(idMedicao, idSubmetaVrpl);
		MedicaoBD medicaoBD = dao.get(MedicaoDAO.class).consultarMedicao(idMedicao);
        
		// verifica se submeta existe
		if (submetaBD != null) {

			ContratoBD contrato = dao.get(ContratoDAO.class).consultarContrato(medicaoBD.getIdContratoMedicao());

			this.validarManutencaoMedicao(contrato, medicaoBD);

			// Verifica se a medição existe e está em situação de alteração que permita
			// exclusão de rascunho
			// Perfil Empresa e situação da medição "Em Elaboração" ou
			// Perfil Convenente e situação da medição "Em Ateste" ou
			// Perfil Concedente ou Mandatária e situação da medição "Em Análise"
			if ((securityContext.isUserInProfile(EMPRESA) && medicaoBD.getSituacao().equals(EM))
				|| 
				(securityContext.isUserInProfile(PROPONENTE_CONVENENTE)	&& medicaoBD.getSituacao().equals(AT))
				|| 
				((securityContext.isUserInProfile(CONCEDENTE) || securityContext.isUserInProfile(MANDATARIA)) && medicaoBD.getSituacao().equals(AC))) {

				if(contrato.isInAcompanhamentoEventos())
					executarExclusaoRascunhoSubmeta(idMedicao, idSubmetaVrpl, submetaBD);
				else {
					executarExclusaoRascunhoSubmetaBM(idMedicao, idSubmetaVrpl, submetaBD);
				}
					
			} else {
				throw new MedicaoRestException(MessageKey.ERRO_MEDICAO_NAO_PODE_SER_ALTERADA);
			}

		} else {
			throw new MedicaoRestException(MessageKey.ERRO_SUBMETA_INEXISTENTE);
		}

	}

	private void executarExclusaoRascunhoSubmeta(Long idMedicao, Long idSubmetaVrpl, final SubmetaMedicaoBD submetaBD) {
		dao.getJdbi().useTransaction(transaction -> {

			// Se Empresa, verifica se situação da submeta é Rascunho
			if (securityContext.isUserInProfile(EMPRESA) &&
					submetaBD.getSituacaoEmpresa() != null &&
					submetaBD.getSituacaoEmpresa().equals(RAS)) {

					excluirRascunhoEmpresa(idMedicao, idSubmetaVrpl, transaction);

				// Se Convenente, verifica se situação da submeta é Rascunho
			} else if (securityContext.isUserInProfile(PROPONENTE_CONVENENTE)
					&& submetaBD.getSituacaoConvenente() != null
					&& submetaBD.getSituacaoConvenente().equals(RAS)) {

					excluirRascunhoConvenente(idMedicao, idSubmetaVrpl, submetaBD, transaction);

				// Se Concedente ou Mandatária verifica se situação da submeta é Rascunho
			} else if ((securityContext.isUserInProfile(CONCEDENTE) || securityContext.isUserInProfile(MANDATARIA)) 
					&& submetaBD.getSituacaoConcedente() != null
					&& submetaBD.getSituacaoConcedente().equals(RAS)) {
				
					excluirRascunhoConcedente(idMedicao, idSubmetaVrpl, submetaBD, transaction);

				// Se a situação da submeta não é rascunho
			} else {
				throw new MedicaoRestException(MessageKey.ERRO_MEDICAO_SITUACAO_DIFERENTE_RASCUNHO);
			}
			
		});
	}
	private void excluirRascunhoConcedente(Long idMedicao, Long idSubmetaVrpl, final SubmetaMedicaoBD submetaBD,
			Handle transaction) {
		// se não há registro de medição pela empresa e convenente na submeta, exclui a submeta
		if(submetaBD.situacaoEmpresa == null && submetaBD.situacaoConvenente == null ) {
			transaction.attach(SubmetaDAO.class).excluirSubmetaMedicao(idMedicao, idSubmetaVrpl);
		} else {
		// se existe registro de medição pela empresa e/ou existe registro de medição 
		// pelo Convenente na submeta, então exclui rascunho de concedente: o sistema limpa os dados 
		// informados pelo concedente atualizando a submeta, e limpa a medicao
			transaction.attach(SubmetaDAO.class).limparSubmetaMedicaoConcedente(submetaBD);
		}

		transaction.attach(ItemMedicaoDAO.class).limparMedicaoConcedente(idMedicao, idSubmetaVrpl);
	}

	private void excluirRascunhoConvenente(Long idMedicao, Long idSubmetaVrpl, final SubmetaMedicaoBD submetaBD,
			Handle transaction) {
		// se não há registro de medição pela empresa na submeta, exclui a submeta
		if(submetaBD.situacaoEmpresa == null) {
			transaction.attach(SubmetaDAO.class).excluirSubmetaMedicao(idMedicao, idSubmetaVrpl);
		} else {
		// se existe registro de medição pela empresa na submeta, então
		// excluir rascunho de convenente: o sistema limpa os dados informados pelo
		// convenente atualizando a submeta, e limpa a medicao
			transaction.attach(SubmetaDAO.class).limparSubmetaMedicaoConvenente(submetaBD);
		}

		transaction.attach(ItemMedicaoDAO.class).limparMedicaoConvenente(idMedicao, idSubmetaVrpl);
	}

	private void excluirRascunhoEmpresa(Long idMedicao, Long idSubmetaVrpl, Handle transaction) {
		transaction.attach(SubmetaDAO.class).excluirSubmetaMedicao(idMedicao, idSubmetaVrpl);

		transaction.attach(ItemMedicaoDAO.class).limparMedicaoEmpresa(idMedicao, idSubmetaVrpl);
	}

	private void executarExclusaoRascunhoSubmetaBM(Long idMedicao, Long idSubmetaVrpl, final SubmetaMedicaoBD submetaBD) {
		dao.getJdbi().useTransaction(transaction -> {

			// Se Empresa, verifica se situação da submeta é Rascunho
			if (securityContext.isUserInProfile(EMPRESA) &&
					submetaBD.getSituacaoEmpresa() != null &&
					submetaBD.getSituacaoEmpresa().equals(RAS)) {
				    
					excluirRascunhoEmpresaBM(idMedicao, idSubmetaVrpl, transaction);

				// Se Convenente, verifica se situação da submeta é Rascunho
			} else if (securityContext.isUserInProfile(PROPONENTE_CONVENENTE)
					&& submetaBD.getSituacaoConvenente() != null
					&& submetaBD.getSituacaoConvenente().equals(RAS)) {

				excluirRascunhoConvenenteBM(idMedicao, idSubmetaVrpl, submetaBD, transaction);

				// Se Concedente ou Mandatária verifica se situação da submeta é Rascunho
			} else if ((securityContext.isUserInProfile(CONCEDENTE) || securityContext.isUserInProfile(MANDATARIA)) 
					&& submetaBD.getSituacaoConcedente() != null
					&& submetaBD.getSituacaoConcedente().equals(RAS)) {
				
				excluirRascunhoConcedenteBM(idMedicao, idSubmetaVrpl, submetaBD, transaction);

				// Se a situação da submeta não é rascunho
			} else {
				throw new MedicaoRestException(MessageKey.ERRO_MEDICAO_SITUACAO_DIFERENTE_RASCUNHO);
			}
			
		});
	}
	
	private void excluirRascunhoEmpresaBM(Long idMedicao, Long idSubmetaVrpl, Handle transaction) {

		transaction.attach(SubmetaDAO.class).excluirSubmetaMedicao(idMedicao, idSubmetaVrpl);
		
		List<ItemMedicaoBMValorBD> listaItensMedicaoBMValorBD = transaction.attach(ItemMedicaoDAO.class).listarItensMedicaoBMValor(idSubmetaVrpl, idMedicao);
		
		listaItensMedicaoBMValorBD.forEach(itemMedicaoBMValor ->
			
			transaction.attach(ItemMedicaoDAO.class).excluirItemMedicaoBMEmpresa(itemMedicaoBMValor.getIdItemMedicaoBMValor())
			
		);
	}
	
	private void excluirRascunhoConvenenteBM(Long idMedicao, Long idSubmetaVrpl,  final SubmetaMedicaoBD submetaBD, Handle transaction) {

		// se não há registro de medição pela empresa na submeta, exclui a submeta
		if(submetaBD.situacaoEmpresa == null) {
			transaction.attach(SubmetaDAO.class).excluirSubmetaMedicao(idMedicao, idSubmetaVrpl);
		} else {
			// se existe registro de medição pela empresa na submeta, então
			// excluir rascunho de convenente: o sistema limpa os dados informados pelo
			// convenente atualizando a submeta, e limpa a medicao
			transaction.attach(SubmetaDAO.class).limparSubmetaMedicaoConvenente(submetaBD);
		}
		
		List<ItemMedicaoBMValorBD> listaItensMedicaoBMValorBD = transaction.attach(ItemMedicaoDAO.class).listarItensMedicaoBMValor(idSubmetaVrpl, idMedicao);
		
		listaItensMedicaoBMValorBD.forEach(itemMedicaoBMValor -> {
			
			// se o registro não possui qtdEmpresa preenchido, o registro deve ser excluído
			if (itemMedicaoBMValor.getQtEmpresa() == null) {
				transaction.attach(ItemMedicaoDAO.class).excluirItemMedicaoBMValor(itemMedicaoBMValor);
			} else {
				transaction.attach(ItemMedicaoDAO.class).limparMedicaoConvenenteBM(itemMedicaoBMValor.getIdItemMedicaoBMValor());
			}
		});
	}
	
	private void excluirRascunhoConcedenteBM(Long idMedicao, Long idSubmetaVrpl,  final SubmetaMedicaoBD submetaBD, Handle transaction) {

		// se não há registro de medição pela empresa e nem pelo convenente na submeta, exclui a submeta
		if(submetaBD.situacaoEmpresa == null && submetaBD.situacaoConvenente == null) {
			transaction.attach(SubmetaDAO.class).excluirSubmetaMedicao(idMedicao, idSubmetaVrpl);
		} else {
			// se existe registro de medição pelo convenente na submeta, então
			// excluir rascunho do concedente: o sistema limpa os dados informados pelo
			// concedente atualizando a submeta
			transaction.attach(SubmetaDAO.class).limparSubmetaMedicaoConcedente(submetaBD);
		}
		
		List<ItemMedicaoBMValorBD> listaItensMedicaoBMValorBD = transaction.attach(ItemMedicaoDAO.class).listarItensMedicaoBMValor(idSubmetaVrpl, idMedicao);
		
		listaItensMedicaoBMValorBD.forEach(itemMedicaoBMValor -> {
			
			// se o registro não possui getQtEmpresa e nem qtdConvenente preenchidos, o registro deve ser excluído
			if (itemMedicaoBMValor.getQtEmpresa() == null && itemMedicaoBMValor.getQtConvenente() == null) {
				transaction.attach(ItemMedicaoDAO.class).excluirItemMedicaoBMValor(itemMedicaoBMValor);
			} else {
				transaction.attach(ItemMedicaoDAO.class).limparMedicaoConcedenteBM(itemMedicaoBMValor.getIdItemMedicaoBMValor());
			}
		});
	}
	
	public DAOFactory getDao() {
		return dao;
	}

	public void setDao(DAOFactory dao) {
		this.dao = dao;
	}

	public List<SubmetaVrplDTO> listarSubmetasPorContrato(Long idContrato) {

		if (idContrato != null) {

			return contratosConsumer.listarSubmetasPorContratoId(idContrato);
		}

		return new ArrayList<>();
	}

    /**
     * Apaga da medição informada os possíveis dados preenchidos nas submetas pelo
     * convenente: marcações (PLE) ou valores (BM).
     * 
     * @param transaction
     * @param medicao
     * @param contrato
     */
    public void apagarMarcacoesConvenenteSubmetasMedicao(Handle transaction, MedicaoBD medicao, ContratoBD contrato) {

        List<SubmetaMedicaoBD> listaSubmetasMedicao = transaction.attach(SubmetaDAO.class)
                .buscarListaSubmetasporMedicao(medicao.getId());

        if (contrato.isInAcompanhamentoEventos()) {
            listaSubmetasMedicao.forEach(submeta -> excluirRascunhoConvenente(medicao.getId(),
                    submeta.getIdSubmetaVrpl(), submeta, transaction));

        } else {
            listaSubmetasMedicao.forEach(submeta -> excluirRascunhoConvenenteBM(medicao.getId(),
                    submeta.getIdSubmetaVrpl(), submeta, transaction));
        }
    }

	/**
	 * 1 - Caso a Submeta tenha Assinatura do Convenente, então apaga (atualiza para null na Submeta) os dados do Concedente.
	 *  
	 * 2 - Caso a Submeta NÃO tenha Assinatura do Convenente, então o registro da submeta é deletado do Banco 
	 * 
	 * @param transaction
	 * @param medicao
	 */
	public void apagarMarcacoesConcedenteSubmetasMedicao(Handle transaction, MedicaoBD medicao, ContratoBD contrato) {

		List<SubmetaMedicaoBD> listaSubmetasMedicao = transaction.attach(SubmetaDAO.class)
				.buscarListaSubmetasporMedicao(medicao.getId());

		if (contrato.isInAcompanhamentoEventos()) {
			listaSubmetasMedicao.forEach(submeta -> excluirRascunhoConcedente(medicao.getId(),
					submeta.getIdSubmetaVrpl(), submeta, transaction));

		} else {
			listaSubmetasMedicao.forEach(submeta -> excluirRascunhoConcedenteBM(medicao.getId(),
					submeta.getIdSubmetaVrpl(), submeta, transaction));

		}
	}

	/**
	 * Inclui ou atualiza a Submeta Medição na situação Rascunho. Atualiza indicador
	 * Executado Empresa do Item Medição (submetaVrpl, FrenteObra, Evento), conforme
	 * situação IndExecutadoEmpresa no corpo do JSON e regras possíveis de
	 * alteração: - Seta informação da Medição no Indicador Executado empresa: se
	 * EVENTO veio marcado como "CONCLUIDO" e EVENTO não estava CONCLUÍDO em nenhuma
	 * medição anterior; - Remove informação do Indicador Executado empresa: se o
	 * EVENTO veio marcado como "NÃO" CONCLUÍDO e se trata da mesma medição, cuja
	 * Situação = "Em Elaboração" ou "Enviada para Complementação Convenente", onde
	 * é possível alterar o evento de Concluído=SIM para Concluído=NAO
	 * 
	 * @param idMedicao
	 * @param idSubmetaVrpl        Id da Submeta do Vrpl
	 * @param listaFrentesObraJson lista de Frente de Obra com o Evento no formato
	 * @param cpfUsuarioLogado
	 * @param assinar
	 * 
	 */
	public void salvarSubmeta(Long idMedicao, Long idSubmetaVrpl, @Valid SubmetaSalvarDTO listaFrentesObraJson,
			Boolean assinar) {

		MedicaoBD medicaoAtual = dao.get(MedicaoDAO.class).consultarMedicao(idMedicao);

		ContratoBD contrato = dao.get(ContratoDAO.class).consultarContrato(medicaoAtual.getIdContratoMedicao());

		validarManutencaoMedicao(contrato, medicaoAtual);

		SubmetaMedicaoDTO submetaMedicao = recuperarSubmetaPorMedicao(idMedicao, idSubmetaVrpl);

		if (!permiteEdicaoSubmetaPelaEmpresa(submetaMedicao) && !permiteEdicaoSubmetaPeloConvenente(submetaMedicao)
				&& !permiteEdicaoSubmetaPeloConcedente(submetaMedicao)) {
			throw new MedicaoRestException(MessageKey.ERRO_MEDICAO_NAO_PODE_SER_ALTERADA);
		}

		dao.getJdbi().useTransaction(transaction -> {

			if (contrato.isInAcompanhamentoEventos()) {
				atualizarIndicadorPorEvento(medicaoAtual, listaFrentesObraJson, submetaMedicao, idSubmetaVrpl,
						transaction);
			} else {
				atualizarValoresServicos(medicaoAtual, submetaMedicao, listaFrentesObraJson, transaction);
			}

			salvarSubmetaMedicao(idSubmetaVrpl, listaFrentesObraJson, assinar, medicaoAtual, transaction);
		});
	}

	private void salvarSubmetaMedicao(Long idSubmetaVrpl, SubmetaSalvarDTO listaFrentesObraJson, Boolean assinar,
			MedicaoBD medicaoAtual, Handle transaction) {
		// Se perfil EMPRESA
		if (securityContext.isUserInProfile(EMPRESA)) {

			salvarSubmetaMedicaoEmpresa(medicaoAtual, idSubmetaVrpl, listaFrentesObraJson, 
					assinar, transaction);

			// Se perfil CONVENENTE
		} else if (securityContext.isUserInProfile(PROPONENTE_CONVENENTE)) {

			salvarSubmetaMedicaoConvenente(medicaoAtual, idSubmetaVrpl, listaFrentesObraJson, 
					assinar, transaction);
			
		} else if (securityContext.isUserInProfile(CONCEDENTE) || securityContext.isUserInProfile(MANDATARIA)) {

			salvarSubmetaMedicaoConcedenteMandataria(medicaoAtual, idSubmetaVrpl, listaFrentesObraJson, 
					assinar, transaction);

		}
	}
	
	/**
	 * Valida os critérios que permitem uma Medição ser Mantida
	 * 
	 * @param medicaoAtual
	 */
	private void validarManutencaoMedicao(ContratoBD contrato, MedicaoBD medicaoAtual) {
		
		if (medicaoAtual != null) {
			// Se a Medição foi Agrupada não pode ser mantida, logo levanta exceção.
			if (medicaoAtual.getIdMedicaoAgrupadora() != null
					&& (contrato.isInAcompanhamentoEventos() || (medicaoAtual.getSituacao() != CE && medicaoAtual.getSituacao() != CC))) {
				throw new MedicaoRestException(MessageKey.ERRO_MANTER_SUBMETA_MEDICAO_AGRUPADA);
			}
			
			// Se a Medição estiver Bloqueada não pode ser mantida, logo levanta exceção.
			if (medicaoAtual.isBloqueada()) {
				throw new MedicaoRestException(MessageKey.ERRO_MEDICAO_BLOQUEADA);
			}
		}
	}

	/*
	 * Inclui ou Altera no banco uma Submeta Medição na Situação Rascunho
	 */
	private void salvarSubmetaMedicaoEmpresa(MedicaoBD medicaoAtual, Long idSubmetaVrpl, SubmetaSalvarDTO listaFrentesObraJson, 
			Boolean assinar, Handle transaction) {

		String cpfUsuarioLogado = securityContext.getUser().getCpf();
		
		// Verifica se já existe a submeta da tabela Submeta Medicao
		SubmetaMedicaoBD submetaMedicaoBD = getDao().get(SubmetaDAO.class).consultarSubmetaMedicao(medicaoAtual.getId(), idSubmetaVrpl);
		
		if (submetaMedicaoBD != null) {
			submetaMedicaoBD.setVersao(listaFrentesObraJson.getVersao());
			
			if (submetaMedicaoBD.getIdSubmetaMedicao() != null && assinar) {
				submetaMedicaoBD.setSituacaoEmpresa(SituacaoSubmetaEnum.ASS);
				submetaMedicaoBD.setNrCpfResponsavelAssinaturaEmpresa(cpfUsuarioLogado);
				submetaMedicaoBD.setDtAssinaturaEmpresa(Instant.now());
			} else {
				// Só atualiza a submeta Medição se tiver como 'Assinada' para remover a
				// assinatura. Estando como Rascunho, não precisa atualizar pois os campos
				// atualizados são o cpf e data de assinatura!
				limparAssinaturaEmpresa(submetaMedicaoBD);
			}

			transaction.attach(SubmetaDAO.class).atualizarAssinaturaEmpresa(submetaMedicaoBD);
		} else {// Inclui Submeta

			submetaMedicaoBD = preparaInclusaoSubmetaMedicaoRascunho(medicaoAtual, idSubmetaVrpl);

			if (assinar.booleanValue()) {
				submetaMedicaoBD.setSituacaoEmpresa(SituacaoSubmetaEnum.ASS);
				submetaMedicaoBD.setNrCpfResponsavelAssinaturaEmpresa(cpfUsuarioLogado);
				submetaMedicaoBD.setDtAssinaturaEmpresa(Instant.now());
			}

			transaction.attach(SubmetaDAO.class).inserir(submetaMedicaoBD);
		}
		
	}
	
	/*
	 * Altera no banco uma Submeta Medição na Situação Rascunho, para o perfil
	 * Convenente
	 */
	private void salvarSubmetaMedicaoConvenente(MedicaoBD medicaoAtual, Long idSubmetaVrpl, SubmetaSalvarDTO listaFrentesObraJson, 
			Boolean assinar, Handle transaction) {

		String cpfUsuarioLogado = securityContext.getUser().getCpf();
		
		// Verifica se já existe a submeta da tabela Submeta Medicao
		SubmetaMedicaoBD submetaMedicaoBD = getDao().get(SubmetaDAO.class).consultarSubmetaMedicao(medicaoAtual.getId(), idSubmetaVrpl);
		
		if (submetaMedicaoBD != null) {
			submetaMedicaoBD.setVersao(listaFrentesObraJson.getVersao());
	
			if (submetaMedicaoBD.getIdSubmetaMedicao() != null && assinar) {
				submetaMedicaoBD.setSituacaoConvenente(SituacaoSubmetaEnum.ASS);
				submetaMedicaoBD.setNrCpfResponsavelAssinaturaConvenente(cpfUsuarioLogado);
				submetaMedicaoBD.setDtAssinaturaConvenente(Instant.now());
			} else {
				// Só atualiza a submeta Medição se tiver como 'Assinada' para remover a
				// assinatura. Estando como Rascunho, não precisa atualizar pois os campos
				// atualizados são o cpf e data de assinatura!
				limparAssinaturaConvenente(submetaMedicaoBD);
			}

			transaction.attach(SubmetaDAO.class).atualizarAssinaturaConvenente(submetaMedicaoBD);

		} else {
			// Submeta não existe para medição sendo atestada
			// Inclui Submeta
			submetaMedicaoBD = preparaInclusaoSubmetaMedicaoRascunho(medicaoAtual, idSubmetaVrpl);

			if (assinar.booleanValue()) {
				submetaMedicaoBD.setSituacaoConvenente(SituacaoSubmetaEnum.ASS);
				submetaMedicaoBD.setNrCpfResponsavelAssinaturaConvenente(cpfUsuarioLogado);
				submetaMedicaoBD.setDtAssinaturaConvenente(Instant.now());
			}
			
			transaction.attach(SubmetaDAO.class).inserirSubmetaConvenente(submetaMedicaoBD);
		}
	}
	
	
	/*
	 * 
	 * Altera no banco uma Submeta Medição na Situação Rascunho, para o perfil Concedente e Mandatária 
	 * 
	 */
	private void salvarSubmetaMedicaoConcedenteMandataria(MedicaoBD medicaoAtual, Long idSubmetaVrpl, SubmetaSalvarDTO listaFrentesObraJson, 
			Boolean assinar, Handle transaction) {

		String cpfUsuarioLogado = securityContext.getUser().getCpf();
		
		// Verifica se já existe a submeta da tabela Submeta Medicao
		SubmetaMedicaoBD submetaMedicaoBD = getDao().get(SubmetaDAO.class).consultarSubmetaMedicao(medicaoAtual.getId(), idSubmetaVrpl);
		
		if (submetaMedicaoBD != null) {
			submetaMedicaoBD.setVersao(listaFrentesObraJson.getVersao());
	
			if (submetaMedicaoBD.getIdSubmetaMedicao() != null && assinar) {
				submetaMedicaoBD.setSituacaoConcedente(SituacaoSubmetaEnum.ASS);
				submetaMedicaoBD.setNrCpfResponsavelAssinaturaConcedente(cpfUsuarioLogado);
				submetaMedicaoBD.setDtAssinaturaConcedente(Instant.now());
				submetaMedicaoBD.setInPerfilRespConcedente(perfilHelper.getPerfilUsuarioLogado().getCodigo());
			} else {
				// Só atualiza a submeta Medição se tiver como 'Assinada' para remover a
				// assinatura. Estando como Rascunho, não precisa atualizar pois os campos
				// atualizados são o cpf e data de assinatura!
				limparAssinaturaConcedenteMandataria(submetaMedicaoBD);
			}

			transaction.attach(SubmetaDAO.class).atualizarAssinaturaConcedente(submetaMedicaoBD);

		} else {
			// Submeta não existe para medição sendo analisada
			// Inclui Submeta
			submetaMedicaoBD = preparaInclusaoSubmetaMedicaoRascunho(medicaoAtual, idSubmetaVrpl);

			if (assinar.booleanValue()) {
				submetaMedicaoBD.setSituacaoConcedente(SituacaoSubmetaEnum.ASS);
				submetaMedicaoBD.setNrCpfResponsavelAssinaturaConcedente(cpfUsuarioLogado);
				submetaMedicaoBD.setDtAssinaturaConcedente(Instant.now());
				submetaMedicaoBD.setInPerfilRespConcedente(perfilHelper.getPerfilUsuarioLogado().getCodigo());
			}
			
			transaction.attach(SubmetaDAO.class).inserirSubmetaConcedenteMandataria(submetaMedicaoBD);
		}
	}

	/*
	 * Atualiza a informação do Indicador
	 * 
	 * Se perfil EMPRESA, atualiza indicador executado Empresa setando com o
	 * IdMediçao ou remove (seta null) se foi alterado de TRUE para FALSE na mesma
	 * medição cuja situação deve estar "Em Elaboração" ou
	 * "Em Complementação pela Empresa"
	 * 
	 * Se perfil CONVENENTE, atualiza indicador atestado Convenente sentando com o
	 * idMedicao ou remove (seta null) se foi alterado de TRUE para FALSE na mesma
	 * medição cuja situação deve estar "Em Ateste" ou
	 * "Em Complementação pelo Convenente"
	 * 
	 * Se perfil CONCEDENTE ou MANDATARIA, atualiza indicador analisado Concedente 
	 * sentando com o idMedicao ou remove (seta null) se foi alterado de TRUE para 
	 * FALSE na mesma medição cuja situação deve estar "Em Analise"
	 * 
	 */
	private void atualizarIndicadorPorEvento(MedicaoBD medicaoAtual, SubmetaSalvarDTO listaFrentesObraJson,
			SubmetaMedicaoDTO submetaMedicaoDTO, Long idSubmetaVrpl, Handle transaction) {

		// Percorre as frentes de obras e os eventos de cada frente do parametro Json
		for (FrenteObraSubmetaSalvarDTO frenteObraJson : listaFrentesObraJson.getFrentesObra()) {

			// Na estrutura do Json, pode ter mais de 1 evento por frente de obra
			for (EventoSubmetaSalvarDTO eventoJson : frenteObraJson.getEventos()) {

				ItemMedicaoBD itemMedicaoBD = transaction.attach(ItemMedicaoDAO.class)
						.consultarItemMedicao(eventoJson.getId(), frenteObraJson.getId(), submetaMedicaoDTO.getId());

				if (itemMedicaoBD != null) {

					if (securityContext.isUserInProfile(EMPRESA)) {
						this.atualizaIndicadorFrenteObraEventoEmpresa(eventoJson, frenteObraJson, itemMedicaoBD,
								medicaoAtual, submetaMedicaoDTO, idSubmetaVrpl, transaction);
					} else if (securityContext.isUserInProfile(PROPONENTE_CONVENENTE)) {
						this.atualizaIndicadorFrenteObraEventoConvenente(eventoJson, frenteObraJson, itemMedicaoBD,
							medicaoAtual, submetaMedicaoDTO, idSubmetaVrpl, transaction);
					} else if (securityContext.isUserInProfile(CONCEDENTE) || securityContext.isUserInProfile(MANDATARIA)) {
						this.atualizaIndicadorFrenteObraEventoConcedenteMandataria(eventoJson, frenteObraJson, itemMedicaoBD,
								medicaoAtual, submetaMedicaoDTO, transaction);
					}
				} else {
					throw new MedicaoRestException(MessageKey.ITEM_MEDICAO_INEXISTENTE);
				} // FIM Do IF para testar se o Item Medição Existe

			} // FIM iteração EventoJson

		} // FIM iteração frenteObraBanco

	}

	
	private void atualizaIndicadorFrenteObraEventoConcedenteMandataria(EventoSubmetaSalvarDTO eventoJson,
			FrenteObraSubmetaSalvarDTO frenteObraJson, ItemMedicaoBD itemMedicaoBD, MedicaoBD medicaoAtual,
			SubmetaMedicaoDTO submetaMedicaoDTO, Handle transaction) {
		
		
		if (!securityContext.isUserInProfile(CONCEDENTE) && !securityContext.isUserInProfile(MANDATARIA)) {
			throw new MedicaoRestException(MessageKey.ERRO_FUNCIONALIDADE_DISPONIVEL_APENAS_PARA_CONCEDENTE_MANDATARIA);
		}		
		
		// Compara os Eventos do JSON com o Evento do Item Medição BD.
		if (eventoJson.getId().longValue() == itemMedicaoBD.getIdEventoVrpl()) {
			boolean eventoAlterado = atualizaEventoConcedenteMandataria(eventoJson, itemMedicaoBD, medicaoAtual);
			
			if (eventoAlterado) { 
				if (permiteMarcacaoEvento(submetaMedicaoDTO, frenteObraJson, eventoJson)) {
					itemMedicaoBD.setIdSubmetaVrpl(submetaMedicaoDTO.getId());
					itemMedicaoBD.setIdFrenteObraVrpl(frenteObraJson.getId());
					itemMedicaoBD.setIdEventoVrpl(eventoJson.getId());
	
					transaction.attach(ItemMedicaoDAO.class).atualizarIndicadorAnalisadoConcedenteMandataria(itemMedicaoBD);
				} else {
					throw new MedicaoRestException(MessageKey.ERRO_ITEM_MEDICAO_NAO_PERMITE_MUDANCA);
				}
			}
		}
		
	}

	/**
	 * Marca ou Desmarca o Item de Medição de uma determinada Medição (@param
	 * idMedicao) de uma @param submetaMedicaoDTO acordo com as regras.
	 * 
	 * @param eventoJson
	 * @param frenteObraJson
	 * @param itemMedicaoBD
	 * @param transaction
	 * @param idMedicao
	 * @param submetaMedicao
	 * @return
	 */
	private void atualizaIndicadorFrenteObraEventoEmpresa(EventoSubmetaSalvarDTO eventoJson,
			FrenteObraSubmetaSalvarDTO frenteObraJson, ItemMedicaoBD itemMedicaoBD, MedicaoBD medicaoAtual,
			SubmetaMedicaoDTO submetaMedicao, Long idSubmetaVrpl, Handle transaction) {

		// Compara os Eventos do JSON com o Evento do Item Medição.
		if (eventoJson.getId().longValue() == itemMedicaoBD.getIdEventoVrpl()) {
			boolean eventoAlterado = atualizaEventoEmpresa(eventoJson, itemMedicaoBD, medicaoAtual, idSubmetaVrpl, transaction);
			
			// SE O EVENTO foi alterado na tela, avalia a permissão de Alteração do Evento, com base no Perfil
			if (eventoAlterado) {
				if (permiteMarcacaoEvento(submetaMedicao, frenteObraJson, eventoJson)) {
					itemMedicaoBD.setIdSubmetaVrpl(submetaMedicao.getId());
					itemMedicaoBD.setIdFrenteObraVrpl(frenteObraJson.getId());
					itemMedicaoBD.setIdEventoVrpl(eventoJson.getId());
	
					transaction.attach(ItemMedicaoDAO.class).atualizarIndicadorExecutadoEmpresa(itemMedicaoBD);
				} else {
					throw new MedicaoRestException(MessageKey.ERRO_ITEM_MEDICAO_NAO_PERMITE_MUDANCA);
				}
			} 
		}
	} // FIM Compara os Eventos

	
	/**
	 * Marca ou desmarca item de medição de uma determinada medição pelo Convenente.
	 * 
	 * @param eventoJson
	 * @param frenteObraJson
	 * @param itemMedicaoBD
	 * @param transaction
	 * @param idMedicao
	 * @param submetaMedicao
	 */
	private void atualizaIndicadorFrenteObraEventoConvenente(EventoSubmetaSalvarDTO eventoJson,
			FrenteObraSubmetaSalvarDTO frenteObraJson, ItemMedicaoBD itemMedicaoBD,MedicaoBD medicaoAtual,
			SubmetaMedicaoDTO submetaMedicao, Long idSubmetaVrpl, Handle transaction) {

		if (!securityContext.isUserInProfile(PROPONENTE_CONVENENTE)) {
			throw new MedicaoRestException(MessageKey.ERRO_FUNCIONALIDADE_DISPONIVEL_APENAS_PARA_CONVENENTE);
		}		
		
		// Compara os Eventos do JSON com o Evento do Item Medição BD.
		if (eventoJson.getId().longValue() == itemMedicaoBD.getIdEventoVrpl()) {
			boolean eventoAlterado = atualizaEventoConvenente(eventoJson, itemMedicaoBD, medicaoAtual, idSubmetaVrpl, transaction);
			
			if (eventoAlterado) { 
				if (permiteMarcacaoEvento(submetaMedicao, frenteObraJson, eventoJson)) {
					itemMedicaoBD.setIdSubmetaVrpl(submetaMedicao.getId());
					itemMedicaoBD.setIdFrenteObraVrpl(frenteObraJson.getId());
					itemMedicaoBD.setIdEventoVrpl(eventoJson.getId());
	
					transaction.attach(ItemMedicaoDAO.class).atualizarIndicadorAtestadoConvenente(itemMedicaoBD);
				} else {
					throw new MedicaoRestException(MessageKey.ERRO_ITEM_MEDICAO_NAO_PERMITE_MUDANCA);
				}
			}
		}
	}


	private boolean atualizaEventoEmpresa(EventoSubmetaSalvarDTO eventoJson, ItemMedicaoBD itemMedicaoBD,
			MedicaoBD medicaoAtual, Long idSubmetaVrpl, Handle transaction) {
		
		boolean eventoAlterado = Boolean.FALSE;
		
		// Se o indicador de realizado é diferente de NULO
		if (eventoJson.getIndRealizado() != null) {

			//Se o EVENTO veio como "TRUE"
			if (eventoJson.getIndRealizado().booleanValue()) {
				eventoAlterado = verificaAtualizaEventoEmpresa(itemMedicaoBD, medicaoAtual, idSubmetaVrpl, transaction,
						eventoAlterado);
				
				//Atualiza o id da medicaoAtual no Item.
				if (eventoAlterado) {
					itemMedicaoBD.setIdMedicaoEmpresa(medicaoAtual.getId());
				}
			} // Se o EVENTO veio como "FALSE" 
			else {
				//E se estava MARCADO no banco
				if (itemMedicaoBD.getIdMedicaoEmpresa() != null && itemMedicaoBD.getIdMedicaoEmpresa() <= medicaoAtual.getId()) {
					// Remove informação do Indicador Executado Empresa, setando como null
					itemMedicaoBD.setIdMedicaoEmpresa(null);
					eventoAlterado = Boolean.TRUE;
				}
			}
		}
		
		return eventoAlterado;
	}

	private boolean verificaAtualizaEventoEmpresa(ItemMedicaoBD itemMedicaoBD, MedicaoBD medicaoAtual,
			Long idSubmetaVrpl, Handle transaction, boolean eventoAlterado) {
		//SE o Item NÃO está MARCADO no banco => RETORNA TRUE
		if (itemMedicaoBD.getIdMedicaoEmpresa() == null) {
			eventoAlterado = Boolean.TRUE;
		} // OU, se está marcado em Medição que está Em Elaboração e é posterior a Medição Atual. 
		else if (medicaoPosteriorEmElaboracao(itemMedicaoBD.getIdMedicaoEmpresa(), medicaoAtual)) {
			
			// Obtem a Submeta marcada na Medição Posterior e Em Elaboração 
			SubmetaMedicaoBD submetaPosteriorMedicaoBD = getDao().get(SubmetaDAO.class).consultarSubmetaMedicao(itemMedicaoBD.getIdMedicaoEmpresa(), idSubmetaVrpl);
			
			//Limpa a assinatura da Submeta
			if (submetaPosteriorMedicaoBD != null) {
				limparAssinaturaEmpresa(submetaPosteriorMedicaoBD);
				transaction.attach(SubmetaDAO.class).atualizarAssinaturaEmpresa(submetaPosteriorMedicaoBD);
			}
			
			eventoAlterado = Boolean.TRUE;
		}
		return eventoAlterado;
	}

	private boolean atualizaEventoConvenente(EventoSubmetaSalvarDTO eventoJson, ItemMedicaoBD itemMedicaoBD,
			MedicaoBD medicaoAtual, Long idSubmetaVrpl, Handle transaction) {
		boolean eventoAlterado = Boolean.FALSE;
		
		// Se EVENTO veio como "TRUE" e EVENTO NÃO estava marcado no banco => MARCAÇÃO DO EVENTO
		if (eventoJson.getIndRealizado() != null) {
			if (eventoJson.getIndRealizado().booleanValue()) {
				eventoAlterado = verificaAtualizaEventoConvenente(itemMedicaoBD, medicaoAtual, idSubmetaVrpl,
						transaction, eventoAlterado);
				
				//Atualiza o id da medicaoAtual no Item.
				if (eventoAlterado) {
					itemMedicaoBD.setIdMedicaoConvenente(medicaoAtual.getId());
				}
				
			} // Se EVENTO veio como "FALSE" e EVENTO estava marcado no banco => DESMARCAÇÃO DO EVENTO 
			else {
				if (itemMedicaoBD.getIdMedicaoConvenente() != null && itemMedicaoBD.getIdMedicaoConvenente() <= medicaoAtual.getId()) {
					// Remove informação do Indicador Executado Convenente, setando como null
					itemMedicaoBD.setIdMedicaoConvenente(null);
					eventoAlterado = Boolean.TRUE;
				}
			}
		}
		
		return eventoAlterado;
	}

	private boolean verificaAtualizaEventoConvenente(ItemMedicaoBD itemMedicaoBD, MedicaoBD medicaoAtual,
			Long idSubmetaVrpl, Handle transaction, boolean eventoAlterado) {
		//SE o Item NÃO está MARCADO no banco => RETORNA TRUE
		if (itemMedicaoBD.getIdMedicaoConvenente() == null) {
			eventoAlterado = Boolean.TRUE;
		}// OU, se está marcado em Medição que está Em Ateste e é posterior a Medição Atual. 
		else if (medicaoPosteriorEmAteste(itemMedicaoBD.getIdMedicaoConvenente(), medicaoAtual)) {
			
			// Obtem a Submeta marcada na Medição Posterior e Em Ateste
			SubmetaMedicaoBD submetaPosteriorMedicaoBD = getDao().get(SubmetaDAO.class).consultarSubmetaMedicao(itemMedicaoBD.getIdMedicaoConvenente(), idSubmetaVrpl);
			
			//Limpa a assinatura da Submeta
			if (submetaPosteriorMedicaoBD != null) {
				limparAssinaturaConvenente(submetaPosteriorMedicaoBD);
				transaction.attach(SubmetaDAO.class).atualizarAssinaturaConvenente(submetaPosteriorMedicaoBD);
			}
			
			eventoAlterado = Boolean.TRUE;
		}
		return eventoAlterado;
	}

	private boolean atualizaEventoConcedenteMandataria(EventoSubmetaSalvarDTO eventoJson, ItemMedicaoBD itemMedicaoBD,
			MedicaoBD medicaoAtual) {
		// Se EVENTO veio como "TRUE" e EVENTO NÃO estava marcado no banco => MARCAÇÃO DO EVENTO
		if (eventoJson.getIndRealizado() != null) {
			if (eventoJson.getIndRealizado().booleanValue()) {
				if (itemMedicaoBD.getIdMedicaoConcedente() == null) {
					// Inclui idMedição no Indicador Executado Convenente
					itemMedicaoBD.setIdMedicaoConcedente(medicaoAtual.getId());
					return Boolean.TRUE;
				}
			} // Se EVENTO veio como "FALSE" e EVENTO estava marcado no banco => DESMARCAÇÃO DO EVENTO 
			else {
				if (itemMedicaoBD.getIdMedicaoConcedente() != null) {
					// Remove informação do Indicador Executado Convenente, setando como null
					itemMedicaoBD.setIdMedicaoConcedente(null);
					return Boolean.TRUE;
				}
			}
		}
		
		return Boolean.FALSE;
			
	}

	private void atualizarValoresServicos(MedicaoBD medicao, SubmetaMedicaoDTO submetaMedicao,
			SubmetaSalvarDTO submetaJson, Handle transaction) {

		List<ItemMedicaoBMBD> listaItemMedicao = transaction.attach(ItemMedicaoDAO.class)
				.listarItemMedicaoBM(medicao.getIdContratoMedicao(), submetaMedicao.getId());

		List<ItemMedicaoBMValorBD> listaItemMedicaoValor = transaction.attach(ItemMedicaoDAO.class)
				.listarItemMedicaoBMValor(medicao.getId(), submetaMedicao.getId());

		Supplier<SubmetaMedicaoBD> submetaMedicaoEmElaboracao = new LazySupplier<>(
				() -> consultarSubmetaMedicaoEmSituacao(medicao.getIdContratoMedicao(), submetaMedicao.getId(),
						EM, transaction));
		
		Supplier<SubmetaMedicaoBD> submetaMedicaoEmAteste = new LazySupplier<>(
				() -> consultarSubmetaMedicaoEmSituacao(medicao.getIdContratoMedicao(), submetaMedicao.getId(),
						AT, transaction));

		submetaJson.forEachFrenteObraServico((frenteObraJson, servicoJson) -> {

			ItemMedicaoBMBD itemMedicao = listaItemMedicao.stream()
					.filter(item -> item.getIdFrenteObraVrpl().equals(frenteObraJson.getId())
							&& item.getIdServicoVrpl().equals(servicoJson.getId()))
					.findFirst().orElseThrow(() -> new MedicaoRestException(MessageKey.ITEM_MEDICAO_INEXISTENTE));

			ItemMedicaoBMValorBD itemMedicaoValor = listaItemMedicaoValor.stream()
					.filter(itemValor -> itemValor.getIdItemMedicaoBM().equals(itemMedicao.getIdItemMedicaoBM()))
					.findFirst().orElse(new ItemMedicaoBMValorBD(itemMedicao.getIdItemMedicaoBM(), medicao.getId()));

			ServicoVrplDTO servicoBase = submetaMedicao.getFrentesObra().stream()
					.filter(frenteObra -> frenteObra.getId().equals(frenteObraJson.getId()))
					.flatMap(frenteObra -> frenteObra.getServicos().stream())
					.filter(servico -> servico.getId().equals(servicoJson.getId())).findFirst().orElseThrow();

			if (securityContext.isUserInProfile(EMPRESA)) {

				boolean houveAlteracao = atualizarValorServicoEmpresa(medicao, servicoJson, servicoBase, itemMedicao,
						itemMedicaoValor, transaction);

				if (houveAlteracao && medicao.getSituacao() == CE) {

					limparValorServicoEmpresaSubmetaEmElaboracao(submetaMedicaoEmElaboracao.get(), itemMedicao,
							transaction);
				}

			} else if (securityContext.isUserInProfile(PROPONENTE_CONVENENTE)) {

				boolean houveAlteracao = atualizarValoresServicosConvenente(medicao, servicoJson, servicoBase, itemMedicao, 
						itemMedicaoValor, transaction);
				
				if (houveAlteracao && medicao.getSituacao() == CC) {

					limparValorServicoConvenenteSubmetaEmAteste(submetaMedicaoEmAteste.get(), itemMedicao,
							transaction);
				}

			} else if (securityContext.isUserInProfile(CONCEDENTE) || securityContext.isUserInProfile(MANDATARIA)) {

				atualizarValoresServicosConcedente(servicoJson, servicoBase, itemMedicao, itemMedicaoValor,
						transaction);
			}
		});
	}

	private boolean atualizarValorServicoEmpresa(MedicaoBD medicao, ServicoSubmetaSalvarDTO servicoJson,
			ServicoVrplDTO servicoBase, ItemMedicaoBMBD itemMedicao, ItemMedicaoBMValorBD itemMedicaoValor,
			Handle transaction) {

		// O valor zero é desconsiderado como quantidade para medição não acumulada
		if (isEqualToZero(servicoJson.getQtdInformada()) && medicao.getIdMedicaoAgrupadora() == null) {
			servicoJson.setQtdInformada(null);
		}

		// Verifica se houve alteração do valor do serviço
		if (!is(servicoBase.getQtdRealizadoEmpresa()).equalTo(servicoJson.getQtdInformada())) {

			validarManutencaoServicoEmpresa(medicao, servicoJson, servicoBase, itemMedicao);

			itemMedicaoValor.setQtEmpresa(servicoJson.getQtdInformada());

			if (itemMedicaoValor.isPersistido()) {
				if (itemMedicaoValor.possuiQuantidadePreenchida()) {
					transaction.attach(ItemMedicaoDAO.class).atualizarItemMedicaoBMValor(itemMedicaoValor);
				} else {
					transaction.attach(ItemMedicaoDAO.class).excluirItemMedicaoBMValor(itemMedicaoValor);
				}
			} else {
				transaction.attach(ItemMedicaoDAO.class).inserirItemMedicaoBMValor(itemMedicaoValor);
			}

			return true;
		}

		return false;
	}

	private void validarManutencaoServicoEmpresa(MedicaoBD medicao, ServicoSubmetaSalvarDTO servicoJson,
			ServicoVrplDTO servicoBase, ItemMedicaoBMBD itemMedicao) {

		if (!servicoBase.isPermiteMedicao()) {
			throw new MedicaoRestException(MessageKey.ERRO_ITEM_MEDICAO_NAO_PERMITE_MUDANCA);
		}

		// Na alteração (complementação) de medição filha a quantidade é obrigatória
		if (servicoJson.getQtdInformada() == null && medicao.getIdMedicaoAgrupadora() != null) {
			throw new MedicaoRestException(MessageKey.ERRO_QTDE_INFORMADA_OBRIGATORIA,
					itemMedicao.getIdServicoVrpl().toString(), itemMedicao.getIdFrenteObraVrpl().toString());
		}

		// Valida valor acumulado até a própria medição
		BigDecimal qtdAcumulado = zeroIfNull(servicoBase.getQtdAcumuladoEmpresa());
		BigDecimal qtdRealizado = zeroIfNull(servicoBase.getQtdRealizadoEmpresa());
		BigDecimal qtdInformada = zeroIfNull(servicoJson.getQtdInformada());

		if (is(qtdAcumulado.subtract(qtdRealizado).add(qtdInformada)).greaterThan(servicoBase.getQtd())) {

			throw new MedicaoRestException(MessageKey.ERRO_ACUMULADO_SERVICO_MAIOR_PLANEJADO,
					itemMedicao.getIdServicoVrpl().toString(), itemMedicao.getIdFrenteObraVrpl().toString());
		}

		// Na alteração (complementação) de medição filha, a validação do valor
		// acumulado considera também as medições posteriores
		if (medicao.getSituacao() == SituacaoMedicaoEnum.CE && medicao.getIdMedicaoAgrupadora() != null) {

			BigDecimal qtdAcumuladoTodasMedicoes = zeroIfNull(servicoBase.getQtdAcumuladoEmpresaTodasMedicoes());

			if (is(qtdAcumuladoTodasMedicoes.subtract(qtdRealizado).add(qtdInformada))
					.greaterThan(servicoBase.getQtd())) {

				throw new MedicaoRestException(MessageKey.ERRO_ACUMULADO_SERVICO_MEDICAO_POSTERIOR_MAIOR_PLANEJADO,
						itemMedicao.getIdServicoVrpl().toString(), itemMedicao.getIdFrenteObraVrpl().toString());
			}
		}
	}

	private SubmetaMedicaoBD consultarSubmetaMedicaoEmSituacao(Long idContratoMedicao, Long idSubmetaVrpl,
			SituacaoMedicaoEnum situacaoMedicao, Handle transaction) {

		return transaction.attach(MedicaoDAO.class)
				.consultarMedicaoporSituacao(idContratoMedicao, situacaoMedicao).stream()
				.map(med -> transaction.attach(SubmetaDAO.class).consultarSubmetaMedicao(med.getId(), idSubmetaVrpl))
				.filter(Objects::nonNull).findFirst().orElse(null);
	}

	private void limparValorServicoEmpresaSubmetaEmElaboracao(SubmetaMedicaoBD submetaMedicaoEmElaboracao,
			ItemMedicaoBMBD itemMedicao, Handle transaction) {

		if (submetaMedicaoEmElaboracao != null) {

			Optional<ItemMedicaoBMValorBD> itemValorEmElaboracao = transaction.attach(ItemMedicaoDAO.class)
					.consultarItemMedicaoBMValor(itemMedicao.getIdItemMedicaoBM(),
							submetaMedicaoEmElaboracao.getIdMedicao());

			if (itemValorEmElaboracao.isPresent()) {

				transaction.attach(ItemMedicaoDAO.class).excluirItemMedicaoBMValor(itemValorEmElaboracao.get());

				if (submetaMedicaoEmElaboracao.getSituacaoEmpresa() == SituacaoSubmetaEnum.ASS) {

					limparAssinaturaEmpresa(submetaMedicaoEmElaboracao);
					transaction.attach(SubmetaDAO.class).atualizarAssinaturaEmpresa(submetaMedicaoEmElaboracao);
				}
			}
		}
	}
	
	private void limparValorServicoConvenenteSubmetaEmAteste(SubmetaMedicaoBD submetaMedicaoEmAteste,
			ItemMedicaoBMBD itemMedicao, Handle transaction) {

		if (submetaMedicaoEmAteste != null) {

			Optional<ItemMedicaoBMValorBD> itemValorEmAteste = transaction.attach(ItemMedicaoDAO.class)
					.consultarItemMedicaoBMValor(itemMedicao.getIdItemMedicaoBM(),
							submetaMedicaoEmAteste.getIdMedicao());

			if (itemValorEmAteste.isPresent()) {

				// NÃO exclue necessariamente o itemMedicao
				// 1. Qd tem dado originalmente da empresa, não pode excluir. Nesse caso, o valor do convenente deve ser limpo (alteração)
				// 2. Qd só foi medido pelo convenente (no caso de uma analise acumulada), ou seja, não há dado originalmente da empresa,
				//    nesse caso o valor é vazio para empresa (qtdeEmpresa == null). Então, pode excluir o itemMedicao
				if (itemValorEmAteste.get().getQtEmpresa() == null) {
					transaction.attach(ItemMedicaoDAO.class).excluirItemMedicaoBMValor(itemValorEmAteste.get());
				} else {
					transaction.attach(ItemMedicaoDAO.class).limparMedicaoConvenenteBM(itemValorEmAteste.get().getIdItemMedicaoBMValor());
				}

				if (submetaMedicaoEmAteste.getSituacaoConvenente() == SituacaoSubmetaEnum.ASS) {

					limparAssinaturaConvenente(submetaMedicaoEmAteste);
					transaction.attach(SubmetaDAO.class).atualizarAssinaturaConvenente(submetaMedicaoEmAteste);
				}
			}
		}
	}

	private boolean atualizarValoresServicosConvenente(MedicaoBD medicao, ServicoSubmetaSalvarDTO servicoJson, ServicoVrplDTO servicoBase,
			ItemMedicaoBMBD itemMedicao, ItemMedicaoBMValorBD itemMedicaoValor, Handle transaction) {
		
		if (!is(servicoBase.getQtdRealizadoConvenente()).equalTo(servicoJson.getQtdInformada())) {
			
			validarManutencaoServicoConvenente(medicao, servicoJson, servicoBase, itemMedicao);
			
			// Atualiza base de dados
			itemMedicaoValor.setQtConvenente(servicoJson.getQtdInformada());
			
			if (itemMedicaoValor.isPersistido()) {
				transaction.attach(ItemMedicaoDAO.class).atualizarItemMedicaoBMValor(itemMedicaoValor);
			} else {
				transaction.attach(ItemMedicaoDAO.class).inserirItemMedicaoBMValor(itemMedicaoValor);
			}
			
			return true;
		}
		return false;
	}

	private void validarManutencaoServicoConvenente(MedicaoBD medicao, ServicoSubmetaSalvarDTO servicoJson, ServicoVrplDTO servicoBase,
			ItemMedicaoBMBD itemMedicao) {

		if (servicoJson.getQtdInformada() == null) {
			throw new MedicaoRestException(MessageKey.ERRO_QTDE_INFORMADA_OBRIGATORIA,
					itemMedicao.getIdServicoVrpl().toString(), itemMedicao.getIdFrenteObraVrpl().toString());
		}
		
		if (!servicoBase.isPermiteMedicao()) {
			throw new MedicaoRestException(ERRO_ITEM_MEDICAO_NAO_PERMITE_MUDANCA);
		}
		
		// Valida o valor informado pelo convenente: qtdInformada <= qtdAcumuladoEmpresa - (qdtAcumuladoConvenente - qdtRealizadoConvenente)
		BigDecimal qtdAcumuladoEmpresa = zeroIfNull(servicoBase.getQtdAcumuladoEmpresa());
		BigDecimal qtdAcumuladoConvenente = zeroIfNull(servicoBase.getQtdAcumuladoConvenente());
		BigDecimal qtdRealizadoConvenente = zeroIfNull(servicoBase.getQtdRealizadoConvenente());

		if (is(servicoJson.getQtdInformada()).greaterThan(qtdAcumuladoEmpresa.subtract(qtdAcumuladoConvenente.subtract(qtdRealizadoConvenente)))) {
			throw new MedicaoRestException(ERRO_ACUMULADO_SERVICO_CONVENENTE_MAIOR_ACUMULADO_SERVICO_EMPRESA,
					itemMedicao.getIdServicoVrpl().toString(), itemMedicao.getIdFrenteObraVrpl().toString());
		}
		
		// Validação do valor acumulado do convenente considerando também as medições posteriores (exceto em ateste).
		// Validar se: o acumulado do convenente em cada medição posterior não ultrapassa o acumulado da empresa na medição posterior em questão
		if (medicao.getSituacao() == CC && medicao.getIdMedicaoAgrupadora() != null && 
				is(servicoJson.getQtdInformada()).greaterThan(qtdRealizadoConvenente)) {
		
			BigDecimal fatorDeAlteracaoConvenente = servicoJson.getQtdInformada().subtract(qtdRealizadoConvenente);
			BigDecimal qtdeAcumuladaConvenenteCalculada = qtdAcumuladoConvenente.add(fatorDeAlteracaoConvenente);
			
			ValorServicoBM valorAcumulado = servicoBase.getValoresPorIdMedicao().entrySet().stream()
				.filter(e -> e.getKey()	> medicao.getId())
				.sorted(Entry.comparingByKey())
				.map(Entry::getValue)
				.collect(acumulandoQtdeAteConvenenteMaiorEmpresa(qtdAcumuladoEmpresa,
						qtdeAcumuladaConvenenteCalculada));
			
			if(is(valorAcumulado.getQtdConvenente()).greaterThan(valorAcumulado.getQtdEmpresa())) {
				throw new MedicaoRestException(ERRO_ACUMULADO_SERVICO_CONVENENTE_MEDICAO_POSTERIOR_MAIOR_ACUMULADO_SERVICO_EMPRESA,
						itemMedicao.getIdServicoVrpl().toString(), itemMedicao.getIdFrenteObraVrpl().toString());
			}
		}
	}

	private Collector<ValorServicoBM, ValorServicoBM, ValorServicoBM> acumulandoQtdeAteConvenenteMaiorEmpresa(
			BigDecimal qtdAcumuladoEmpresa, BigDecimal qtdeAcumuladaConvenenteCalculada) {
		return Collector.of(() -> new ValorServicoBM(qtdAcumuladoEmpresa, qtdeAcumuladaConvenenteCalculada, null), 
				(acumulador, valorNaMedicao) -> {
					if( is(acumulador.getQtdConvenente()).lessOrEqualThan(acumulador.getQtdEmpresa()) ) {
						acumulador.setQtdEmpresa(nullSafeAdd(acumulador.getQtdEmpresa(), valorNaMedicao.getQtdEmpresa()));
						acumulador.setQtdConvenente(nullSafeAdd(acumulador.getQtdConvenente(), valorNaMedicao.getQtdConvenente()));
					}
				}, (t, s) -> t);
	}

	private void atualizarValoresServicosConcedente(ServicoSubmetaSalvarDTO servicoJson, ServicoVrplDTO servicoBase,
			ItemMedicaoBMBD itemMedicao, ItemMedicaoBMValorBD itemMedicaoValor, Handle transaction) {

		if (servicoJson.getQtdInformada() == null) {
			throw new MedicaoRestException(MessageKey.ERRO_QTDE_INFORMADA_OBRIGATORIA,
					itemMedicao.getIdServicoVrpl().toString(), itemMedicao.getIdFrenteObraVrpl().toString());
		}

		if (!is(servicoBase.getQtdRealizadoConcedente()).equalTo(servicoJson.getQtdInformada())) {

			if (!servicoBase.isPermiteMedicao()) {
				throw new MedicaoRestException(MessageKey.ERRO_ITEM_MEDICAO_NAO_PERMITE_MUDANCA);
			}

			// Valida o valor informado pelo concedente:
			// qtdInformada <= qtdAcumuladoConvenente - (qdtAcumuladoConcedente - qdtRealizadoConcedente)
			BigDecimal qtdAcumuladoConvenente = zeroIfNull(servicoBase.getQtdAcumuladoConvenente());
			BigDecimal qtdAcumuladoConcedente = zeroIfNull(servicoBase.getQtdAcumuladoConcedente());
			BigDecimal qtdRealizadoConcedente = zeroIfNull(servicoBase.getQtdRealizadoConcedente());
			BigDecimal qtdInformada = servicoJson.getQtdInformada();

			if (is(qtdInformada).greaterThan(
					qtdAcumuladoConvenente.subtract(qtdAcumuladoConcedente.subtract(qtdRealizadoConcedente)))) {
				throw new MedicaoRestException(
						MessageKey.ERRO_ACUMULADO_SERVICO_CONCEDENTE_MAIOR_ACUMULADO_SERVICO_CONVENENTE,
						itemMedicao.getIdServicoVrpl().toString(), itemMedicao.getIdFrenteObraVrpl().toString());
			}

			// Atualiza base de dados
			itemMedicaoValor.setQtConcedente(qtdInformada);

			if (itemMedicaoValor.isPersistido()) {
				transaction.attach(ItemMedicaoDAO.class).atualizarItemMedicaoBMValor(itemMedicaoValor);
			} else {
				transaction.attach(ItemMedicaoDAO.class).inserirItemMedicaoBMValor(itemMedicaoValor);
			}
		}
	}
}
