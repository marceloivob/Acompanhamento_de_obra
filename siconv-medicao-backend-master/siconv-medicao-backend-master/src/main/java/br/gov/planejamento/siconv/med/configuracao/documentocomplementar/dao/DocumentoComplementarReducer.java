package br.gov.planejamento.siconv.med.configuracao.documentocomplementar.dao;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.jdbi.v3.core.result.RowReducer;
import org.jdbi.v3.core.result.RowView;

import br.gov.planejamento.siconv.med.configuracao.documentocomplementar.entity.dto.DocumentoComplementarDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;


public class DocumentoComplementarReducer 
             implements RowReducer<Map<Long, DocumentoComplementarDTO>, DocumentoComplementarDTO> {
	private Map<Long, DocumentoComplementarDTO>      container = new LinkedHashMap<>();
	private Map<Long, SubmetaVrplDTO>                submetas  = new HashMap<>();

	@Override
	public Map<Long,DocumentoComplementarDTO> container() {
		return this.container;
	}

	@Override
	public void accumulate(Map<Long, DocumentoComplementarDTO> dc, RowView rowView) {
		DocumentoComplementarDTO dcDTO = container.computeIfAbsent(rowView.getColumn("dc_id", Long.class),
				id -> rowView.getRow(DocumentoComplementarDTO.class));
	                  Long   idSubmeta = rowView.getColumn("subm_id", Long.class);
	          
		SubmetaVrplDTO submeta = submetas.computeIfAbsent(idSubmeta, id -> rowView.getRow(SubmetaVrplDTO.class));

		if(submeta != null && submeta.getId() != null) {
			dcDTO.addSubmetas(submeta);
		}
		
	}

	@Override
	public Stream<DocumentoComplementarDTO> stream(Map<Long, DocumentoComplementarDTO> container) {
		return container.values().stream();
	}
	

}
