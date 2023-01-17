package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.jdbi.v3.core.result.RowReducer;
import org.jdbi.v3.core.result.RowView;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.AtividadeRegistroProfissionalEnum;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.ContratoResponsavelTecnicoDTO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.RegistroProfissionalDTO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.ResponsavelTecnicoDTO;

public class ResponsavelTecnicoReducer implements RowReducer<Map<Long, ResponsavelTecnicoDTO>, ResponsavelTecnicoDTO> {

	private Map<Long, ResponsavelTecnicoDTO> container = new LinkedHashMap<>();
	private Map<Long, RegistroProfissionalDTO> registro = new HashMap<>();
	private Map<Long, ContratoResponsavelTecnicoDTO> contrRespTec = new HashMap<>();

	@Override
	public Map<Long, ResponsavelTecnicoDTO> container() {
		return this.container;
	}

	@Override
	public void accumulate(Map<Long, ResponsavelTecnicoDTO> responsaveltecnico, RowView rowView) {
		ResponsavelTecnicoDTO responsavelTecnicoDTO = container.computeIfAbsent(rowView.getColumn("rt_id", Long.class),
				id -> rowView.getRow(ResponsavelTecnicoDTO.class));

		Long idRegistroProfissionalDTO = rowView.getColumn("rp_id", Long.class);

		if (idRegistroProfissionalDTO != null) {
			RegistroProfissionalDTO registroDTO = registro.computeIfAbsent(idRegistroProfissionalDTO, id -> {
				RegistroProfissionalDTO itemRegistro = rowView.getRow(RegistroProfissionalDTO.class);

				itemRegistro.setAtividade(
						AtividadeRegistroProfissionalEnum.valueOf(itemRegistro.getAtividade()).getDescricao());
				responsavelTecnicoDTO.addRegistros(itemRegistro);

				return itemRegistro;
			});


				Long idContratoResponsavelTecnicoDTO = rowView.getColumn("crt_id", Long.class);

				if (idContratoResponsavelTecnicoDTO != null) {
					ContratoResponsavelTecnicoDTO contratoResponsavelTecnico = contrRespTec.computeIfAbsent(
							idContratoResponsavelTecnicoDTO, id -> rowView.getRow(ContratoResponsavelTecnicoDTO.class));

					registroDTO.getContratos().add(contratoResponsavelTecnico);

				}
			}

	}

	@Override
	public Stream<ResponsavelTecnicoDTO> stream(Map<Long, ResponsavelTecnicoDTO> container) {
		return container.values().stream();
	}

}
