package br.gov.planejamento.siconv.med.medicao.entity.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
public class MacroservicoVrplDTO {

	private Long id;
	
	private Integer numero;
	
	private String descricao;	
	
	@JsonInclude(value = Include.NON_EMPTY)
	private List<ServicoVrplDTO> servicos = new ArrayList<>();
	
	public ServicoVrplDTO addServicos(ServicoVrplDTO servicoVrplDTO) {
        int pos = this.servicos.indexOf(servicoVrplDTO);
        if(pos == -1) {
            this.servicos.add(servicoVrplDTO);
            return servicoVrplDTO;
        }

        return this.servicos.get(pos);
    }

}
