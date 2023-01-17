package br.gov.planejamento.siconv.med.medicao.entity.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
public class FrenteObraVrplDTO {

	private Long id;
	
	private String descricao;
	
	@JsonInclude(value = Include.NON_EMPTY)
	private List<EventoVrplDTO> eventos = new ArrayList<>();
	
	@JsonIgnore
	private List<ServicoVrplDTO> servicos = new ArrayList<>();
	
	@JsonInclude(value = Include.NON_EMPTY)
	private List<MacroservicoVrplDTO> macroServicosView = new ArrayList<>();
	
	public EventoVrplDTO addEventos(EventoVrplDTO eventoVrplDTO) {
        int pos = this.eventos.indexOf(eventoVrplDTO);
        if(pos == -1) {
            this.eventos.add(eventoVrplDTO);
            return eventoVrplDTO;
        }

        return this.eventos.get(pos);
    }
	
	public MacroservicoVrplDTO addMacroservicosView(MacroservicoVrplDTO macroservicoVrplDTO) {
        int pos = this.macroServicosView.indexOf(macroservicoVrplDTO);
        if(pos == -1) {
            this.macroServicosView.add(macroservicoVrplDTO);
            return macroservicoVrplDTO;
        }

        return this.macroServicosView.get(pos);
    }
	
	public ServicoVrplDTO addServicos(ServicoVrplDTO servicoVrplDTO) {
        int pos = this.servicos.indexOf(servicoVrplDTO);
        if(pos == -1) {
            this.servicos.add(servicoVrplDTO);
            return servicoVrplDTO;
        }

        return this.servicos.get(pos);
    }
}
