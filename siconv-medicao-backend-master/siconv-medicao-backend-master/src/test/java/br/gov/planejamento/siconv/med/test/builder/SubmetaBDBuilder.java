package br.gov.planejamento.siconv.med.test.builder;

import br.gov.planejamento.siconv.med.medicao.entity.SituacaoSubmetaEnum;
import br.gov.planejamento.siconv.med.medicao.entity.database.SubmetaMedicaoBD;

public class SubmetaBDBuilder {

	private SubmetaMedicaoBD submeta;
	
	public SubmetaBDBuilder() {
	}
	
	public static SubmetaBDBuilder newSubmetaBuilder() {
		SubmetaBDBuilder submetaBuilder = new SubmetaBDBuilder();
		submetaBuilder.submeta = new SubmetaMedicaoBD();
		return submetaBuilder;
	}
	
	public SubmetaMedicaoBD create() {
		return this.submeta;
	}
	
	public SubmetaBDBuilder setIdSubmetaMedicao(Long id) {
		this.submeta.idSubmetaMedicao = id;
		return this;
	}
	
	public SubmetaBDBuilder setIdSubmetaVrpl(Long id) {
		this.submeta.idSubmetaVrpl = id;
		return this;
	}
	
	public SubmetaBDBuilder setIdMedicao(Long id) {
		this.submeta.idMedicao = id;
		return this;
	}
	
	public SubmetaBDBuilder setSituacaoEmpresa(SituacaoSubmetaEnum situacao) {
		this.submeta.situacaoEmpresa = situacao;
		return this;
	}
	
	public SubmetaBDBuilder setSituacaoConvenente(SituacaoSubmetaEnum situacao) {
		this.submeta.situacaoConvenente = situacao;
		return this;
	}
	
	public SubmetaBDBuilder setSituacaoConcedente(SituacaoSubmetaEnum situacao) {
		this.submeta.situacaoConcedente = situacao;
		return this;
	}
}
