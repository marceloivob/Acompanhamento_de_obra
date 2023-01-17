package br.gov.planejamento.siconv.med.medicao.business.builder;

import static java.util.Collections.sort;
import static java.util.Comparator.comparing;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import br.gov.planejamento.siconv.med.infra.exception.MedicaoRestException;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.integration.vrpl.VrplGRPCConsumer;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Context;
import br.gov.planejamento.siconv.med.medicao.entity.dto.EventoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.FrenteObraVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MacroservicoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;
import io.quarkus.arc.Unremovable;

@Unremovable
@ApplicationScoped
public class PreencherListaFrenteObrasStep extends AbstractSubmetaMedicaoStep {

	
	private VrplGRPCConsumer vrplConsumer;
	
	@Inject
	public PreencherListaFrenteObrasStep(VrplGRPCConsumer vrplConsumer,  SecurityContext securityContext) {
		super(securityContext);
		this.vrplConsumer = vrplConsumer;
	}

	@Override
	public void process(SubmetaMedicaoDTO submetaMedicao, Context builderContext) {
		
		
	   SubmetaVrplDTO submetaVrpl = vrplConsumer.getSubmetaPorId(submetaMedicao.getId())
			   .orElseThrow(this::newSubmetaInexistenteException);
		
		for (FrenteObraVrplDTO frenteObraMedicao : submetaMedicao.getFrentesObra()) {

			this.completarDadosFrenteObra(frenteObraMedicao, submetaVrpl);
		}
	}
	
	
	private void completarDadosFrenteObra(FrenteObraVrplDTO frenteObraMedicao, SubmetaVrplDTO submetaVrpl) {

		FrenteObraVrplDTO frenteObraVrpl = submetaVrpl.getFrentesObras().stream().filter(frenteObra -> 
			frenteObraMedicao.getId().equals(frenteObra.getId())
		).iterator().next();

		frenteObraMedicao.setDescricao(frenteObraVrpl.getDescricao());

		this.preencherListaEventosOuMacroServico(frenteObraMedicao, frenteObraVrpl);

	}

	/**
	 * Preenche a lista de Eventos se o Contrato for acompanhado por Eventos 
	 * OU
	 * Preenche a lista de MacroServicos se o Contrato NÃO for acompanhado por Eventos
	 * 
	 * @param frenteObraMedicao
	 * @param frenteObraVrpl
	 * @param medicaoAtual
	 * @param submetaMedicao
	 */
	private void preencherListaEventosOuMacroServico(FrenteObraVrplDTO frenteObraMedicao, 
			FrenteObraVrplDTO frenteObraVrpl) {

		// Contrato controlado por Evento 
		frenteObraVrpl.getEventos().forEach(eventoVrpl -> {

			for (EventoVrplDTO eventoMedicao : frenteObraMedicao.getEventos()) {
				
				if (frenteObraMedicao.getId().equals(frenteObraVrpl.getId()) &&
						eventoMedicao.getId().equals(eventoVrpl.getId())) {
					
					eventoMedicao.setDescricao(eventoVrpl.getDescricao());
					eventoMedicao.setValor(eventoVrpl.getValor());
					eventoMedicao.setServicos(eventoVrpl.getServicos());
				}
			}
			
		});
		
		//	Contrato NÃO controlado por Evento
		frenteObraVrpl.getMacroServicosView().forEach(macroServicoVRPL -> {
			
			MacroservicoVrplDTO macroServicoMedicao = new MacroservicoVrplDTO();
			macroServicoMedicao.setDescricao(macroServicoVRPL.getDescricao());
			macroServicoMedicao.setId(macroServicoVRPL.getId());
			macroServicoMedicao.setNumero(macroServicoVRPL.getNumero());
			
			macroServicoVRPL.getServicos().forEach(servicoVRPL -> frenteObraMedicao.getServicos().
				stream().
				filter(servico -> servico.getId().equals(servicoVRPL.getId())).
				findFirst().ifPresent(servicoMedicao -> {
					servicoMedicao.setNumero(servicoVRPL.getNumero());
					servicoMedicao.setDescricao(servicoVRPL.getDescricao());
					servicoMedicao.setSgUnidade(servicoVRPL.getSgUnidade());
					macroServicoMedicao.addServicos(servicoMedicao);
				})
			);
			
			sort(macroServicoMedicao.getServicos(), comparing(ServicoVrplDTO::getNumero));
			frenteObraMedicao.addMacroservicosView(macroServicoMedicao);
		});
		
		sort(frenteObraMedicao.getMacroServicosView(), comparing(MacroservicoVrplDTO::getNumero));
	}
	
	private MedicaoRestException newSubmetaInexistenteException() {
        return new MedicaoRestException(MessageKey.ERRO_SUBMETA_INEXISTENTE,
                Status.NOT_FOUND.getStatusCode());
	}


}
