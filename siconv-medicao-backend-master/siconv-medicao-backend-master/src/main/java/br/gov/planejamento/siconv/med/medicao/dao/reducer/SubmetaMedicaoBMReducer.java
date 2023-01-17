package br.gov.planejamento.siconv.med.medicao.dao.reducer;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.jdbi.v3.core.result.RowReducer;
import org.jdbi.v3.core.result.RowView;

import br.gov.planejamento.siconv.med.medicao.entity.SituacaoSubmetaEnum;
import br.gov.planejamento.siconv.med.medicao.entity.dto.FrenteObraVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.PerfilEnum;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoVrplDTO.ValorServicoBM;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;

public class SubmetaMedicaoBMReducer implements RowReducer<Map<Long, SubmetaMedicaoDTO>, SubmetaMedicaoDTO> {

	private Map<Long, SubmetaMedicaoDTO> container = new LinkedHashMap<>();
	private Map<Long, FrenteObraVrplDTO> frentesObra = new HashMap<>();
	private Map<String, ServicoVrplDTO> servicos = new HashMap<>();

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

		Long idServico = rowView.getColumn("serv_id", Long.class);
		ServicoVrplDTO servico = servicos.computeIfAbsent(idFrenteDeObraVRPLDTO + "|" + idServico, id -> {
			ServicoVrplDTO itemServ = rowView.getRow(ServicoVrplDTO.class);

			frenteObra.addServicos(itemServ);

			return itemServ;
		});

		Long idMedicaoValorServico = rowView.getColumn("svl_med_id", Long.class);
		if (idMedicaoValorServico != null) {
			BigDecimal qtdEmpresa = rowView.getColumn("svl_qtd_empresa", BigDecimal.class);
			BigDecimal qtdConvenente = rowView.getColumn("svl_qtd_convenente", BigDecimal.class);
			BigDecimal qtdConcedente = rowView.getColumn("svl_qtd_concedente", BigDecimal.class);

			servico.getValoresPorIdMedicao().put(idMedicaoValorServico,
					new ValorServicoBM(qtdEmpresa, qtdConvenente, qtdConcedente));
		}
	}

	@Override
	public Stream<SubmetaMedicaoDTO> stream(Map<Long, SubmetaMedicaoDTO> container) {
		return container.values().stream();
	}

}
