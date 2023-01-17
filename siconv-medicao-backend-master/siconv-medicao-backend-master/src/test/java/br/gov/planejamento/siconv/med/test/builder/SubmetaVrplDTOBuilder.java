package br.gov.planejamento.siconv.med.test.builder;

import java.math.BigDecimal;

import br.gov.planejamento.siconv.med.medicao.entity.dto.EventoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.FrenteObraVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MacroservicoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;

public class SubmetaVrplDTOBuilder {

	private SubmetaVrplDTO submeta;
	
	public SubmetaVrplDTOBuilder() {
	}
	
	public static SubmetaVrplDTOBuilder newSubmetaMedicaoBuilder() {
		SubmetaVrplDTOBuilder submetaMedicaoBuilder = new SubmetaVrplDTOBuilder();
		submetaMedicaoBuilder.submeta = new SubmetaVrplDTO();
		return submetaMedicaoBuilder;
	}

	
	public static SubmetaVrplDTOBuilder newSubmetaMedicaoBuilder_ComServico(BigDecimal qtdPlanejadaServico ) {
		SubmetaVrplDTOBuilder submetaMedicaoBuilder = new SubmetaVrplDTOBuilder();
		submetaMedicaoBuilder.submeta = new SubmetaVrplDTO();
		
		submetaMedicaoBuilder.addMacroServicos(qtdPlanejadaServico);
		
		return submetaMedicaoBuilder;
	}
	
	
	public SubmetaVrplDTO create() {
		return this.submeta;
	}

	
	private void addMacroServicos(BigDecimal qtdPlanejadaServico) {
		
		FrenteObraVrplDTO frenteObra = new FrenteObraVrplDTO();
			
		MacroservicoVrplDTO macroServico = new MacroservicoVrplDTO();
		macroServico.setId(1L);
		macroServico.setNumero(1);
		macroServico.setDescricao("Macrosserviço");
				
		ServicoVrplDTO servico = new ServicoVrplDTO();
		servico.setId(1L);
		servico.setNumero(1);
		servico.setDescricao("Serviço");
		
		macroServico.addServicos(servico);
				
		frenteObra.addMacroservicosView(macroServico);
		
		this.submeta.addFrentesObras(frenteObra);
		
	}
	
	public static SubmetaVrplDTOBuilder newSubmetaMedicaoBuilder_ComEvento(Long idMedicao, Boolean permiteMarcacao) {
		SubmetaVrplDTOBuilder submetaMedicaoBuilder = new SubmetaVrplDTOBuilder();
		submetaMedicaoBuilder.submeta = new SubmetaVrplDTO();
		
		submetaMedicaoBuilder.addFrenteObraComEventos(idMedicao);
		
		return submetaMedicaoBuilder;
	}

	private void addFrenteObraComEventos(Long idMedicao) {
		
		FrenteObraVrplDTO frenteObra = new FrenteObraVrplDTO();
							
		EventoVrplDTO evento = new EventoVrplDTO();
		evento.setId(1L);
		evento.setDescricao("Evento");
		evento.setIdMedicaoEmpresa(idMedicao);
		evento.setNrSeqMedicaoEmpresa(idMedicao);
		
		ServicoVrplDTO servico = new ServicoVrplDTO();
		servico.setDescricao("Serviço");
		evento.addServicos(servico);
				
		frenteObra.addEventos(evento);
		
		this.submeta.addFrentesObras(frenteObra);
		
	}
	
	public void addDadosFrenteObra() {
		FrenteObraVrplDTO frenteObra = this.submeta.getFrentesObras().get(0);
		frenteObra.setId(1L);
		frenteObra.setDescricao("Frente de Obra");
	}
	
}
