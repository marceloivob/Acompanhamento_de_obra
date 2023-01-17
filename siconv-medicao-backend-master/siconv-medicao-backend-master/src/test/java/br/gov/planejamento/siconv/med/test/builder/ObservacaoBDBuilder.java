package br.gov.planejamento.siconv.med.test.builder;

import java.util.List;

import br.gov.planejamento.siconv.med.medicao.entity.database.AnexoBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.ObservacaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.dto.PerfilEnum;

public class ObservacaoBDBuilder {
	
	private ObservacaoBD observacao;
	
	public ObservacaoBDBuilder() {
	}
	
	public static ObservacaoBDBuilder newObservacaoBuilder() {
		ObservacaoBDBuilder obs = new ObservacaoBDBuilder();
    	obs.observacao = new ObservacaoBD();
		return obs;
	}
	
	public ObservacaoBD create() {
		return this.observacao;
	}
	
	
	public ObservacaoBDBuilder setTxObservacao(String txObservacao) {
		this.observacao.setTxObservacao(txObservacao);
		return this;
	}
	
	public ObservacaoBDBuilder setNrCpfResponsavel(String nrCpfResponsavel) {
		this.observacao.setNrCpfResponsavel(nrCpfResponsavel);
		return this;
	}
	
	public ObservacaoBDBuilder setInPerfilResponsavel(PerfilEnum inPerfilResponsavel) {
		this.observacao.setInPerfilResponsavel(inPerfilResponsavel.toString());
		return this;
	}
	
	public ObservacaoBDBuilder setAnexos(List<AnexoBD> anexos) {
		this.observacao.setAnexos(anexos);
		return this;
	}	
}
