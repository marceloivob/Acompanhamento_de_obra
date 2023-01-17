package br.gov.planejamento.siconv.med.medicao.dao.reducer;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.jdbi.v3.core.result.RowReducer;
import org.jdbi.v3.core.result.RowView;

import br.gov.planejamento.siconv.med.medicao.entity.SituacaoSubmetaEnum;
import br.gov.planejamento.siconv.med.medicao.entity.dto.EventoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.FrenteObraVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.PerfilEnum;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;

public class SubmetaMedicaoReducer implements RowReducer<Map<Long, SubmetaMedicaoDTO>, SubmetaMedicaoDTO> {

	private Map<Long, SubmetaMedicaoDTO> container = new LinkedHashMap<>();
	private Map<Long, FrenteObraVrplDTO> frentesObra = new HashMap<>();
	private Map<String, EventoVrplDTO> eventos = new HashMap<>();

	@Override
	public Map<Long, SubmetaMedicaoDTO> container() {
		return this.container;
	}

	@Override
	public void accumulate(Map<Long, SubmetaMedicaoDTO> submeta, RowView rowView) {
		SubmetaMedicaoDTO submetaMedicaoDTO = container.computeIfAbsent(rowView.getColumn("subm_id", Long.class),
				id -> rowView.getRow(SubmetaMedicaoDTO.class));

        String situacaoEmpresa = rowView.getColumn("sit_in_situacao_emp", String.class);

        if (situacaoEmpresa != null) {
            submetaMedicaoDTO.setSituacaoEmpresa(SituacaoSubmetaEnum.valueOf(situacaoEmpresa));
        }
        
        String situacaoConvenente = rowView.getColumn("sit_in_situacao_conv", String.class);

        if (situacaoConvenente != null) {
            submetaMedicaoDTO.setSituacaoConvenente(SituacaoSubmetaEnum.valueOf(situacaoConvenente));
        }   
        
        String situacaoConcedente = rowView.getColumn("sit_in_situacao_conc", String.class);

        if (situacaoConcedente != null) {
            submetaMedicaoDTO.setSituacaoConcedente(SituacaoSubmetaEnum.valueOf(situacaoConcedente));
        }
		String nrCpfEmp = rowView.getColumn("resp_nr_cpf_emp", String.class);
		String dataAssinaturaEmp = rowView.getColumn("assin_data_emp", String.class);
		String nrCpfConv = rowView.getColumn("resp_nr_cpf_conv", String.class);
		String dataAssinaturaConv = rowView.getColumn("assin_data_conv", String.class);
		String nrCpfConc = rowView.getColumn("resp_nr_cpf_conc", String.class);
		String dataAssinaturaConc = rowView.getColumn("assin_data_conc", String.class);
		
		if (nrCpfEmp != null) {
			submetaMedicaoDTO.setAssinatura(nrCpfEmp, Timestamp.valueOf(dataAssinaturaEmp));
		}
		if (nrCpfConv != null) {
			submetaMedicaoDTO.setAssinatura(nrCpfConv, Timestamp.valueOf(dataAssinaturaConv));
		}		
		if (nrCpfConc != null) {
			submetaMedicaoDTO.setAssinatura(nrCpfConc, Timestamp.valueOf(dataAssinaturaConc), 
					rowView.getColumn("resp_perfil_conc", PerfilEnum.class).getDescricao());
		}
		
		Long idFrenteDeObraVRPLDTO = rowView.getColumn("fo_id", Long.class);

		FrenteObraVrplDTO frenteObra = frentesObra.computeIfAbsent(idFrenteDeObraVRPLDTO, id -> {
			FrenteObraVrplDTO itemFO = rowView.getRow(FrenteObraVrplDTO.class);

			submetaMedicaoDTO.addFrentesObra(itemFO);

			return itemFO;
		});

		Long idEvento = rowView.getColumn("even_id", Long.class);
		EventoVrplDTO evento = eventos.computeIfAbsent(idFrenteDeObraVRPLDTO + "|" + idEvento, id -> 
			rowView.getRow(EventoVrplDTO.class)
		);

		frenteObra.addEventos(evento);

	}

	@Override
	public Stream<SubmetaMedicaoDTO> stream(Map<Long, SubmetaMedicaoDTO> container) {
		return container.values().stream();
	}

}
