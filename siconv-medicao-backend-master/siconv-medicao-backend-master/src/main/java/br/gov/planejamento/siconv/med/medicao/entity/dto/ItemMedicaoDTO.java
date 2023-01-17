package br.gov.planejamento.siconv.med.medicao.entity.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ItemMedicaoDTO {

	private Long idItemMedicao;
	private Long idSubmeta;
	private Long idFrenteObra;
	private String nmFrenteObra;
	private Long idMedicaoEmpresa;
	private Long idMedicaoConvenente;
	private Long idMedicaoConcedente;
	private Long idContratoMedicao;
	private BigDecimal vlTotalServicos;
	private Long versao;
	private List<EventoVrplDTO> listaEvento = new ArrayList<>();
		
	public EventoVrplDTO addEventos(EventoVrplDTO eventoVrplDTO) {
        int pos = this.listaEvento.indexOf(eventoVrplDTO);
        if(pos == -1) {
            this.listaEvento.add(eventoVrplDTO);
            return eventoVrplDTO;
        }

        return this.listaEvento.get(pos);
    }
}
