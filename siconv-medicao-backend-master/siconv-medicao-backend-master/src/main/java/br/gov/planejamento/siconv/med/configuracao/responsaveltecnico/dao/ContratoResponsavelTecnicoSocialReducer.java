package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.jdbi.v3.core.result.RowReducer;
import org.jdbi.v3.core.result.RowView;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.ContratoResponsavelTecnicoSocialDTO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.ContratoResponsavelTecnicoSocialDTO.Orgao;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.ResponsavelTecnicoDTO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.TipoResponsavelTecnicoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;

public class ContratoResponsavelTecnicoSocialReducer 
	implements RowReducer<Map<Long, ContratoResponsavelTecnicoSocialDTO>, ContratoResponsavelTecnicoSocialDTO> {
	
	private Map<Long, ContratoResponsavelTecnicoSocialDTO> container = new LinkedHashMap<>();
	private Map<Long, SubmetaVrplDTO> submetas = new HashMap<>();
	private Map<Long, ResponsavelTecnicoDTO> responsavelTecnico = new HashMap<>();

	@Override
	public Map<Long, ContratoResponsavelTecnicoSocialDTO> container() {
		return this.container;
	}

	@Override
	public void accumulate(Map<Long, ContratoResponsavelTecnicoSocialDTO> container, RowView rowView) {
		
		ContratoResponsavelTecnicoSocialDTO contratoRTSocial = container.computeIfAbsent(rowView.getColumn("rtsocial_id", Long.class),
				id -> rowView.getRow(ContratoResponsavelTecnicoSocialDTO.class));
		
		Long idResponsavel = rowView.getColumn("rt_id", Long.class);
		if (idResponsavel != null) {
			
			ResponsavelTecnicoDTO responsavel = responsavelTecnico.computeIfAbsent(idResponsavel,
					id -> rowView.getRow(ResponsavelTecnicoDTO.class));
			
			contratoRTSocial.setResponsavelTecnico(responsavel);

		}

		if (contratoRTSocial.getTipo() == TipoResponsavelTecnicoEnum.FIS) {
		    contratoRTSocial.setOrgao(rowView.getRow(Orgao.class));
		}

		Long idSubmeta = rowView.getColumn("sub_id", Long.class);
		SubmetaVrplDTO submeta = submetas.computeIfAbsent(idSubmeta, id -> rowView.getRow(SubmetaVrplDTO.class));
		contratoRTSocial.addSubmetas(submeta);
	}

	@Override
	public Stream<ContratoResponsavelTecnicoSocialDTO> stream(
			Map<Long, ContratoResponsavelTecnicoSocialDTO> container) {
		return container.values().stream();
	}

}
