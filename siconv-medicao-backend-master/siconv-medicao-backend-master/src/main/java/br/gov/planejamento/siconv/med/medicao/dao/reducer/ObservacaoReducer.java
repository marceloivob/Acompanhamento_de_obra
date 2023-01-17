package br.gov.planejamento.siconv.med.medicao.dao.reducer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.jdbi.v3.core.result.RowReducer;
import org.jdbi.v3.core.result.RowView;

import br.gov.planejamento.siconv.med.medicao.entity.dto.AnexoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ObservacaoDTO;

public class ObservacaoReducer implements RowReducer<Map<Long, ObservacaoDTO>, ObservacaoDTO> {

	private Map<Long, ObservacaoDTO> container = new LinkedHashMap<>();
	private Map<Long, AnexoDTO> anexos = new HashMap<>();

	@Override
	public Map<Long, ObservacaoDTO> container() {
		return this.container;
	}

	@Override
	public void accumulate(Map<Long, ObservacaoDTO> observacao, RowView rowView) {
		ObservacaoDTO observacaoDTO = container.computeIfAbsent(rowView.getColumn("observ_id", Long.class),
				id -> rowView.getRow(ObservacaoDTO.class));

		Long idAnexoDTO = rowView.getColumn("anexo_id", Long.class);
		
		if (idAnexoDTO != null) {
			anexos.computeIfAbsent(idAnexoDTO, id -> {
				AnexoDTO itemAnexo = rowView.getRow(AnexoDTO.class);
	
				observacaoDTO.addAnexos(itemAnexo);
	
				return itemAnexo;
			});
		}

	}

	@Override
	public Stream<ObservacaoDTO> stream(Map<Long, ObservacaoDTO> container) {
		return container.values().stream();
	}

}
