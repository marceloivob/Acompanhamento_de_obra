package br.gov.planejamento.siconv.med.test.builder;

import java.time.LocalDate;

import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import br.gov.planejamento.siconv.med.medicao.entity.database.MedicaoBD;

public class MedicaoBuilder {
	
	private MedicaoBD medicao;
	
	public MedicaoBuilder() {
	}
	
	public static MedicaoBuilder newMedicaoBuilder() {
		MedicaoBuilder medicaoBuilder = new MedicaoBuilder();
		medicaoBuilder.medicao = new MedicaoBD();
		return medicaoBuilder;
	}
	
	public MedicaoBD create() {
		return this.medicao;
	}
	
	public MedicaoBuilder setId(Long id) {
		this.medicao.id = id;
		return this;
	}
	
	public MedicaoBuilder setNrSequencial(Short nrSequencial) {
		this.medicao.nrSequencial = nrSequencial;
		return this;
	}
	
	public MedicaoBuilder setAgrupadora(Long idAgrupadora) {
		this.medicao.idMedicaoAgrupadora = idAgrupadora;
		return this;
	}
	
	public MedicaoBuilder setMedContrato(Long idContatoMedicao) {
		this.medicao.idContratoMedicao = idContatoMedicao;
		return this;
	}
	
	public MedicaoBuilder comSituacao(SituacaoMedicaoEnum situacao) {
		this.medicao.situacao = situacao;
		return this;
	}
	
	public MedicaoBuilder setBloqueada(boolean bloqueada) {
		this.medicao.setBloqueada(bloqueada);
		return this;
	}
	
	public MedicaoBuilder setDtVistoriaExtra(LocalDate data) {
		this.medicao.setDataVistoriaExtra(data);
		return this;
	}
	
	public MedicaoBuilder setDtFimMedicao(LocalDate data) {
		this.medicao.setDtFim(data);
		return this;
	}
	
	public MedicaoBuilder setDtInicioMedicao(LocalDate data) {
		this.medicao.setDtInicio(data);
		return this;
	}
	
	public MedicaoBuilder setPermiteComplementacaoValor(boolean permiteComplementacaoValor) {
		this.medicao.setPermiteComplementacaoValor(permiteComplementacaoValor);
		return this;
	}
	
}
