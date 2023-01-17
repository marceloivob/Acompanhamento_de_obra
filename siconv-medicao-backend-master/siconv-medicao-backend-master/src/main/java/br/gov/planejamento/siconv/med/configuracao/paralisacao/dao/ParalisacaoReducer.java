package br.gov.planejamento.siconv.med.configuracao.paralisacao.dao;

import java.util.HashMap;
import java.util.Map;

import org.jdbi.v3.core.result.LinkedHashMapRowReducer;
import org.jdbi.v3.core.result.RowView;

import br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.dto.AnexoParalisacaoDTO;
import br.gov.planejamento.siconv.med.configuracao.paralisacao.entity.dto.ParalisacaoDTO;

public class ParalisacaoReducer implements LinkedHashMapRowReducer<Long, ParalisacaoDTO> {

	private Map<Long, AnexoParalisacaoDTO> anexos = new HashMap<>();

	@Override
	public void accumulate(Map<Long, ParalisacaoDTO> container, RowView rowView) {

		ParalisacaoDTO paralisacaoDTO = container.computeIfAbsent(rowView.getColumn("paralisa_id", Long.class),
				id -> rowView.getRow(ParalisacaoDTO.class));

		Long idAnexoDTO = rowView.getColumn("anexo_id", Long.class);

		if (idAnexoDTO != null) {
			anexos.computeIfAbsent(idAnexoDTO, id -> {
				AnexoParalisacaoDTO itemAnexo = rowView.getRow(AnexoParalisacaoDTO.class);
				paralisacaoDTO.addAnexos(itemAnexo);
				return itemAnexo;
			});
		}
	}
}
