package br.gov.planejamento.siconv.med.acompanhamento.business;

import static br.gov.planejamento.siconv.med.acompanhamento.entity.dto.ContratoLoteDTO.Tipo.CONTRATO;
import static br.gov.planejamento.siconv.med.acompanhamento.entity.dto.ContratoLoteDTO.Tipo.LOTE;
import static br.gov.planejamento.siconv.med.infra.util.MathUtil.nullSafeAdd;
import static java.util.Collections.sort;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.gov.planejamento.siconv.med.acompanhamento.entity.dto.ContratoLoteDTO;
import br.gov.planejamento.siconv.med.acompanhamento.entity.dto.ListagemContratoLoteDTO;
import br.gov.planejamento.siconv.med.acompanhamento.entity.dto.SubmetaContratoLoteDTO;
import br.gov.planejamento.siconv.med.acompanhamento.entity.dto.TipoInstrumentoDTO;
import br.gov.planejamento.siconv.med.contrato.business.ContratosBC;
import br.gov.planejamento.siconv.med.contrato.entity.ModalidadeEnum;
import br.gov.planejamento.siconv.med.contrato.entity.dto.AndamentoContratoDTO;
import br.gov.planejamento.siconv.med.infra.exception.MedicaoRestException;
import br.gov.planejamento.siconv.med.infra.exception.SeverityEnum;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.infra.util.UrlConsultaTipoInstrumentoBuilder;
import br.gov.planejamento.siconv.med.integration.contratos.ContratosGrpcConsumer;
import br.gov.planejamento.siconv.med.integration.siconv.SiconvGRPCConsumer;
import br.gov.planejamento.siconv.med.integration.vrpl.VrplGRPCConsumer;
import br.gov.planejamento.siconv.med.medicao.business.SubmetaBC;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import br.gov.serpro.siconv.contratos.grpc.dto.ContratoDTO;
import br.gov.serpro.siconv.contratos.grpc.dto.PropostaDTO;
import br.gov.serpro.siconv.contratos.grpc.dto.SubmetaDTO;
import br.gov.serpro.vrpl.grpc.Lote;
import br.gov.serpro.vrpl.grpc.Lote.Submeta;
import br.gov.serpro.vrpl.grpc.PropostaLote;

@ApplicationScoped
public class AcompanhamentoObraBC {

	@Inject
	private VrplGRPCConsumer vrplConsumer;

	@Inject
	private ContratosGrpcConsumer contratosConsumer;

    @Inject
	private SiconvGRPCConsumer siconvConsumer;
	
	@Inject
	private ContratosBC contratoBC;

	@Inject
	private SubmetaBC submetaBC;

	@Inject
	private UrlConsultaTipoInstrumentoBuilder urlBuilder;

	/**
	 * Consulta os contratos e/ou lotes para a tela inicial do convenente.
	 * 
	 * @param idProposta
	 */
	public ListagemContratoLoteDTO listarContratosLotes(Long idProposta) {

		Optional<PropostaLote> propostaVrpl = vrplConsumer.consultarSubmetasPropostaVRPLAceita(idProposta);

		if (propostaVrpl.isPresent()) {

			Optional<PropostaDTO> propostaContrato = contratosConsumer.consultarTipoInstrumentoConvenente(idProposta);
			ListagemContratoLoteDTO listagem = new ListagemContratoLoteDTO();
			
			preencherDadosTipoInstrumento(listagem, propostaVrpl.get());
			preencherDadosContratosLotes(listagem, propostaVrpl.get(), propostaContrato);
			preencherDadosMedicoes(listagem);
			calcularTotais(listagem);

			sort(listagem.getContratosLotes(),
					comparing(ContratoLoteDTO::getTipo).thenComparing(ContratoLoteDTO::getNumero));

			return listagem;
		} else {
			throw consultarPropostaSiconv (idProposta).get();
		}

	}

	
	/**
	 * Esse metodo é executado sempre quando NÃO houver pelo menos uma VRPL Aceita para a Proposta.
	 * 
	 * @param idProposta
	 */
	private Supplier<MedicaoRestException> consultarPropostaSiconv(Long idProposta) {
		
		Optional<Boolean> isVRPLResponsavel = siconvConsumer.isVRPLResponsavelAceiteProcessoExecucao(idProposta);
		 
		// A proposta EXISTE no SICONV
		if (isVRPLResponsavel.isPresent() ) {
			//e É mantida pelo VRPL, porém ainda não está na base de dados do VRPL.
			if (isVRPLResponsavel.get().booleanValue()) {
				return () -> new MedicaoRestException(MessageKey.ERRO_PROPOSTA_INEXISTENTE_VRPL, SeverityEnum.CRITICAL);
			} else {//e NÃO É mantida pelo VRPL.
				return () -> new MedicaoRestException(MessageKey.ERRO_PROPOSTA_NAO_MANTIDA_VRPL, SeverityEnum.CRITICAL);
			}
		} else {
			return () -> new MedicaoRestException (MessageKey.ERRO_PROPOSTA_INEXISTENTE_SICONV, SeverityEnum.CRITICAL);
		}
		
	}

	/**
	 * Preenche os dados do tipo de instrumento no objeto
	 * {@link ListagemContratoLoteDTO} conforme os dados da proposta retornados pelo
	 * módulo VRPL.
	 * 
	 * @param listagem
	 * @param propostaVrpl
	 */
	private void preencherDadosTipoInstrumento(ListagemContratoLoteDTO listagem, PropostaLote propostaVrpl) {

		TipoInstrumentoDTO tipoInstrumento = new TipoInstrumentoDTO();

		tipoInstrumento.setModalidade(ModalidadeEnum.fromCodigo(propostaVrpl.getModalidade(), propostaVrpl.getPossuiInstituicaoMandataria()));
		tipoInstrumento.setNomeObjetoContratoRepasse(propostaVrpl.getNomeObjeto());
		tipoInstrumento.setLocalidade(propostaVrpl.getUf());
		tipoInstrumento.setAnoConvenioRepasse(propostaVrpl.getAnoConvenio());
		tipoInstrumento.setNumeroConvenioRepasse(propostaVrpl.getNumeroConvenio());
		tipoInstrumento.setUrlSiconvMedicao(
				urlBuilder.getUrl(tipoInstrumento.getNumeroConvenioRepasse(), tipoInstrumento.getAnoConvenioRepasse()));
		tipoInstrumento.setNomeConvenente(propostaVrpl.getNomeProponente());

		listagem.setTipoInstrumento(tipoInstrumento);
	}

	/**
	 * Preenche os dados dos contratos e/ou lotes no objeto
	 * {@link ListagemContratoLoteDTO} conforme os dados das propostas retornados
	 * pelos serviços dos módulos VRPL e CONTRATO.
	 * 
	 * @param listagem
	 * @param propostaVrpl
	 * @param propostaContrato
	 */
	private void preencherDadosContratosLotes(ListagemContratoLoteDTO listagem, PropostaLote propostaVrpl,
			Optional<PropostaDTO> propostaContrato) {

		//CONTRATOS
		propostaContrato.ifPresent(proposta -> proposta.getContratos().stream()
				.map(this::converterContratoLote)
				.collect(toCollection(listagem::getContratosLotes)));

		//VRPL
		propostaVrpl.getLotesList().stream().filter(loteVrpl -> !lotePertenceContrato(loteVrpl, propostaContrato))
				.map(this::converterContratoLote).collect(toCollection(listagem::getContratosLotes));
	}
	
	
	/**
	 * Converte um objeto do tipo {@link ContratoDTO}, retornado pelo serviço do
	 * módulo CONTRATO, para um objeto do tipo {@link ContratoLoteDTO}.
	 * 
	 * @param contrato
	 * @return
	 */
	private ContratoLoteDTO converterContratoLote(ContratoDTO contrato) {

		ContratoLoteDTO contratoLote = new ContratoLoteDTO();

		contratoLote.setTipo(CONTRATO);
		contratoLote.setId(contrato.getId());
		contratoLote.setNumero(contrato.getNumero());
		contratoLote.setAptoIniciar(contrato.isAptIniciar());
		contratoLote.setAcompEventos(contrato.isAcompEventos());

		contrato.getLotes().stream().flatMap(lote -> lote.getSubmetas().stream())
				.map(this::converterSubmetaContratoLote)
				.sorted(SubmetaContratoLoteDTO.ORDENACAO_PADRAO)
				.collect(toCollection(contratoLote::getSubmetas));

		return contratoLote;
	}

	/**
	 * Converte um objeto do tipo {@link Lote}, retornado pelo serviço do módulo
	 * VRPL, para um objeto do tipo {@link ContratoLoteDTO}.
	 * 
	 * @param loteVrpl
	 * @return
	 */
	private ContratoLoteDTO converterContratoLote(Lote loteVrpl) {

		ContratoLoteDTO contratoLote = new ContratoLoteDTO();

		contratoLote.setTipo(LOTE);
		contratoLote.setNumero(String.valueOf(loteVrpl.getNumero()));
		contratoLote.setAptoIniciar(false);
		contratoLote.setAcompEventos(false);

		loteVrpl.getSubmetasList().stream()
				.map(this::converterSubmetaContratoLote)
				.sorted(SubmetaContratoLoteDTO.ORDENACAO_PADRAO)
				.collect(toCollection(contratoLote::getSubmetas));

		return contratoLote;
	}

	/**
	 * Converte um objeto do tipo {@link SubmetaDTO}, retornado pelo serviço do
	 * módulo CONTRATO, para um objeto do tipo {@link SubmetaContratoLoteDTO}.
	 * 
	 * @param submetaContrato
	 * @return
	 */
	private SubmetaContratoLoteDTO converterSubmetaContratoLote(SubmetaDTO submetaContrato) {

		SubmetaContratoLoteDTO submetaContratoLote = new SubmetaContratoLoteDTO();

		submetaContratoLote.setId(submetaContrato.getId_vrpl());
		submetaContratoLote.setNumero(submetaContrato.getNumero());
		submetaContratoLote.setDescricao(submetaContrato.getDescricao());
		submetaContratoLote.setValorSubmeta(submetaContrato.getValorTotal());
		submetaContratoLote.setSituacao(submetaContrato.getSituacao());
		submetaContratoLote.setRegimeExecucao(submetaContrato.getRegimeExecucao());

		return submetaContratoLote;
	}

	/**
	 * Converte um objeto do tipo {@link Submeta}, retornado pelo serviço do módulo
	 * VRPL, para um objeto do tipo {@link SubmetaContratoLoteDTO}.
	 * 
	 * @param submetaVrpl
	 * @return
	 */
	private SubmetaContratoLoteDTO converterSubmetaContratoLote(Submeta submetaVrpl) {

		SubmetaContratoLoteDTO submetaContratoLote = new SubmetaContratoLoteDTO();

		submetaContratoLote.setId(submetaVrpl.getId());
		submetaContratoLote.setNumero(submetaVrpl.getNumero());
		submetaContratoLote.setDescricao(submetaVrpl.getDescricao());
		submetaContratoLote.setValorSubmeta(new BigDecimal(submetaVrpl.getValorTotal()));
		submetaContratoLote.setSituacao(submetaVrpl.getSituacao());
		submetaContratoLote.setRegimeExecucao(submetaVrpl.getRegimeExecucao());

		return submetaContratoLote;
	}

	/**
	 * Verifica se um determinado lote retornado pelo serviço do módulo VRPL está
	 * incluso em algum contrato da proposta retornada pelo serviço do módulo
	 * CONTRATO. A comparação é realizada pelo número do lote.
	 * 
	 * @param loteVrpl
	 * @param propostaContrato
	 * @return
	 */
	private boolean lotePertenceContrato(Lote loteVrpl, Optional<PropostaDTO> propostaContrato) {

		return propostaContrato.isPresent() && propostaContrato.get().getContratos().stream()
				.flatMap(contrato -> contrato.getLotes().stream())
				.anyMatch(loteContrato -> loteContrato.getNumero().equals(String.valueOf(loteVrpl.getNumero())));
	}

	/**
	 * Preenche para cada contrato apto a iniciar e acompanhado por eventos do
	 * objeto {@link ListagemContratoLoteDTO} os valores realizados das submetas na
	 * última medição e também alguns indicadores.
	 * 
	 * @param listagem
	 */
	private void preencherDadosMedicoes(ListagemContratoLoteDTO listagem) {

		Stream<ContratoLoteDTO> contratos = listagem.getContratosLotes().stream()
				.filter(contratoLote -> contratoLote.getTipo().equals(CONTRATO) && contratoLote.isAptoIniciar());

		contratos.forEach(contrato -> {

			AndamentoContratoDTO andamentoContrato = contratoBC.consultarAndamentoContrato(contrato.getId());

			if (andamentoContrato.possuiMedicao()) {
				contrato.setNumeroUltimaMedicao(andamentoContrato.getSequencialUltimaMedicao());
				preencherValoresSubmetas(contrato, andamentoContrato.getIdUltimaMedicao());
			}

			contrato.setQtdeDiasSemMedicao(andamentoContrato.getQtdeDiasSemMedicao());
			contrato.setAtrasado(andamentoContrato.isAtrasado());
			contrato.setConfiguradoMedicao(contratoBC.isContratoConfiguradoParaMedicao(contrato.getId()));
			contrato.setParalisado(contratoBC.isContratoParalisado(contrato.getId()));
		});
	}

	/**
	 * Preenche os totais realizados (período e acumulado) das submetas no objeto
	 * {@link ContratoLoteDTO} conforme os totais das submetas da medição informada.
	 * 
	 * @param contratoLote
	 * @param idMedicao
	 */
	private void preencherValoresSubmetas(ContratoLoteDTO contratoLote, Long idMedicao) {

		Map<Long, SubmetaMedicaoDTO> mapSubmetasMedicao = submetaBC.recuperarListaSubmetasPorMedicao(idMedicao).stream()
				.collect(toMap(SubmetaMedicaoDTO::getId, Function.identity()));

		contratoLote.getSubmetas().forEach(submetaContratoLote -> {

			if (mapSubmetasMedicao.containsKey(submetaContratoLote.getId())) {

				SubmetaMedicaoDTO submetaMedicao = mapSubmetasMedicao.get(submetaContratoLote.getId());

				// Realizado período
				submetaContratoLote.setValorRealizadoEmpresa(submetaMedicao.getValorRealizadoEmpresa());
				submetaContratoLote.setValorRealizadoConvenente(submetaMedicao.getValorRealizadoConvenente());
				submetaContratoLote.setValorRealizadoConcedente(submetaMedicao.getValorRealizadoConcedente());

				// Realizado acumulado
				submetaContratoLote
						.setValorRealizadoAcumuladoEmpresa(submetaMedicao.getValorRealizadoAcumuladoEmpresa());
				submetaContratoLote
						.setValorRealizadoAcumuladoConvenente(submetaMedicao.getValorRealizadoAcumuladoConvenente());
				submetaContratoLote
						.setValorRealizadoAcumuladoConcedente(submetaMedicao.getValorRealizadoAcumuladoConcedente());
			}
		});
	}

	/**
	 * Calcula e preenche no objeto {@link ListagemContratoLoteDTO} o valor total
	 * (i.e. somatório dos valores das submetas) e o valor realizado acumulado total
	 * das submetas para cada ator (i.e. empresa, convenente e concedente).
	 * 
	 * @param listagem
	 */
	private void calcularTotais(ListagemContratoLoteDTO listagem) {

		Stream<SubmetaContratoLoteDTO> submetas = listagem.getContratosLotes().stream()
				.flatMap(contratoLote -> contratoLote.getSubmetas().stream());

		submetas.forEach(submetaContratoLote -> {

			listagem.setValorTotalSubmetas(
					nullSafeAdd(listagem.getValorTotalSubmetas(), submetaContratoLote.getValorSubmeta()));

			listagem.setValorTotalEmpresa(nullSafeAdd(listagem.getValorTotalEmpresa(),
					submetaContratoLote.getValorRealizadoAcumuladoEmpresa()));

			listagem.setValorTotalConvenente(nullSafeAdd(listagem.getValorTotalConvenente(),
					submetaContratoLote.getValorRealizadoAcumuladoConvenente()));

			listagem.setValorTotalConcedente(nullSafeAdd(listagem.getValorTotalConcedente(),
					submetaContratoLote.getValorRealizadoAcumuladoConcedente()));
		});
	}
}
