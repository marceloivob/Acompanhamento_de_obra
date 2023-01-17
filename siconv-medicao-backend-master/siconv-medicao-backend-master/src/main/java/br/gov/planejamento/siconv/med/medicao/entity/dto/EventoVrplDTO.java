package br.gov.planejamento.siconv.med.medicao.entity.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
public class EventoVrplDTO {

	private Long id;
	
	private String descricao;
	
	private BigDecimal valor;
	
	private Long idMedicaoEmpresa;
	
	private Long idMedicaoConvenente;
	
	private Long idMedicaoConcedente;

	private Long nrSeqMedicaoEmpresa;
	
	private Long nrSeqMedicaoConvenente;
	
	private Long nrSeqMedicaoConcedente;
	
	private Boolean indRealizado;
	
	private Boolean permiteMarcacao = Boolean.FALSE;
	
	
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
