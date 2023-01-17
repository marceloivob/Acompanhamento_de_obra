package br.gov.planejamento.siconv.med.medicao.entity.database;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ObservacaoBD {
	private Long id;

	private Long versao;

	private Instant dtRegistro;

	private String inPerfilResponsavel;

	private String nrCpfResponsavel;

	private String txObservacao;

	private Long medicaoFk;

	private boolean inBloqueio;

	private List<AnexoBD> anexos = new ArrayList<>();
}
