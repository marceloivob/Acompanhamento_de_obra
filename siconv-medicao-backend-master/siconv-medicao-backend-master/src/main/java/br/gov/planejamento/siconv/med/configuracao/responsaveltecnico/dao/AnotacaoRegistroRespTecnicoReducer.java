package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.jdbi.v3.core.result.RowReducer;
import org.jdbi.v3.core.result.RowView;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.AnotacaoRegistroRespTecnicoDTO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.ResponsavelTecnicoDTO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.TipoResponsavelTecnicoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;

public class AnotacaoRegistroRespTecnicoReducer
		implements RowReducer<Map<Long, AnotacaoRegistroRespTecnicoDTO>, AnotacaoRegistroRespTecnicoDTO> {
	private Map<Long, AnotacaoRegistroRespTecnicoDTO> container = new LinkedHashMap<>();
	private Map<Long, SubmetaVrplDTO> submetas = new HashMap<>();
	private Map<Long, ResponsavelTecnicoDTO> responsavelTecnico = new HashMap<>();

	@Override
	public Map<Long, AnotacaoRegistroRespTecnicoDTO> container() {
		return this.container;
	}

	@Override
	public void accumulate(Map<Long, AnotacaoRegistroRespTecnicoDTO> arrt, RowView rowView) {
		AnotacaoRegistroRespTecnicoDTO arrtDTO = container.computeIfAbsent(rowView.getColumn("arrt_id", Long.class),
				id -> rowView.getRow(AnotacaoRegistroRespTecnicoDTO.class));

		Long idSubmeta = rowView.getColumn("subm_id", Long.class);

		String tipo = rowView.getColumn("arrt_in_tipo", String.class);

		if (tipo != null) {
			TipoResponsavelTecnicoEnum tipoEnum = TipoResponsavelTecnicoEnum.valueOf(tipo);

			arrtDTO.setTipo(tipoEnum);
		}
		
		Long idResponsavel = rowView.getColumn("rt_id", Long.class);
		if (idResponsavel != null) {
			
			ResponsavelTecnicoDTO responsavel = responsavelTecnico.computeIfAbsent(idResponsavel,
					id -> rowView.getRow(ResponsavelTecnicoDTO.class));
			
			//Carrega o Responsavel Tecnico na ARRT
			arrtDTO.setResponsavelTecnico(responsavel);

			//Carrega os dados de Contrato Responsavel Tecnico
			Long idMedContratoRespTec = rowView.getColumn("crt_id", Long.class);					
			if (idMedContratoRespTec != null) {					
						
				//Carrega o Contrato Responsavel Tecnico na ARRT
				arrtDTO.setIdMedContratoRespTec(idMedContratoRespTec);

			}
		}

		SubmetaVrplDTO submeta = submetas.computeIfAbsent(idSubmeta, id -> rowView.getRow(SubmetaVrplDTO.class));

		arrtDTO.addSubmetas(submeta);
		
	}

	@Override
	public Stream<AnotacaoRegistroRespTecnicoDTO> stream(Map<Long, AnotacaoRegistroRespTecnicoDTO> container) {
		return container.values().stream();
	}

}
