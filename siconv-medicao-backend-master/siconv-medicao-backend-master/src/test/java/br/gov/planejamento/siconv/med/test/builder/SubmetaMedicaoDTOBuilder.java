package br.gov.planejamento.siconv.med.test.builder;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import br.gov.planejamento.siconv.med.medicao.entity.SituacaoSubmetaEnum;
import br.gov.planejamento.siconv.med.medicao.entity.dto.EventoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.FrenteObraVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MacroservicoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.PerfilEnum;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;

public class SubmetaMedicaoDTOBuilder {

	private SubmetaMedicaoDTO submeta;
	
	public SubmetaMedicaoDTOBuilder() {
	}

    public SubmetaMedicaoDTOBuilder(Long idSubmetaVrpl, Long idFrenteObra, Long idServico) {

        ServicoVrplDTO servicoBase = new ServicoVrplDTO();
        servicoBase.setNumero(1);
        servicoBase.setId(idServico);

        FrenteObraVrplDTO frenteObraBase = new FrenteObraVrplDTO();
        frenteObraBase.setId(idFrenteObra);
        frenteObraBase.setServicos(List.of(servicoBase));

        submeta = new SubmetaMedicaoDTO();
        submeta.setId(idSubmetaVrpl);
        submeta.setFrentesObra(List.of(frenteObraBase));
    }

	public static SubmetaMedicaoDTOBuilder newSubmetaMedicaoBuilder() {
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = new SubmetaMedicaoDTOBuilder();
		submetaMedicaoBuilder.submeta = new SubmetaMedicaoDTO();
		return submetaMedicaoBuilder;
	}

	
	public static SubmetaMedicaoDTOBuilder newSubmetaMedicaoBuilder_ComServico(BigDecimal qtdPlanejadaServico ) {
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = new SubmetaMedicaoDTOBuilder();
		submetaMedicaoBuilder.submeta = new SubmetaMedicaoDTO();
		
		submetaMedicaoBuilder.addMacroServicosComServicoSemMedicao (qtdPlanejadaServico);
		
		return submetaMedicaoBuilder;
	}
	
	
	public SubmetaMedicaoDTO create() {
		return this.submeta;
	}

	
	private void addMacroServicosComServicoSemMedicao(BigDecimal qtdPlanejadaServico) {
		
		FrenteObraVrplDTO frenteObra = new FrenteObraVrplDTO();
			
		MacroservicoVrplDTO macroServico = new MacroservicoVrplDTO();
		macroServico.setNumero(1);
				
		ServicoVrplDTO servico = new ServicoVrplDTO();
		servico.setNumero(1);
		servico.setId(1L);
		servico.setQtd(qtdPlanejadaServico);
		
		macroServico.addServicos(servico);
				
		frenteObra.setId(1L);
		frenteObra.addServicos(servico);
		frenteObra.addMacroservicosView(macroServico);
		
		submeta.addFrentesObra(frenteObra);
		
	}

	public void addQuantidadeInsuficienteParaServicoEmpresa() {
		 
		ServicoVrplDTO servico = submeta.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		
		servico.setQtdRealizadoEmpresa(BigDecimal.valueOf(3.0));
		servico.setQtdAcumuladoEmpresa(BigDecimal.valueOf(11.0));
		
	}

	public void addQuantidadeTotalParaServicoEmpresa() {
		 
		ServicoVrplDTO servico = submeta.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		
		servico.setQtdRealizadoEmpresa(BigDecimal.valueOf(3.32));
		servico.setQtdAcumuladoEmpresa(BigDecimal.valueOf(15.32));
		
	}

	public void addQuantidadeTotalMasSemMedicaoParaServicoCorrenteEmpresa() {
		 
		ServicoVrplDTO servico = submeta.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		
		servico.setQtdAcumuladoEmpresa(BigDecimal.valueOf(15.32));
		
	}
	
	public void addQuantidadeInsuficienteParaServicoConvenente() {
		ServicoVrplDTO servico = submeta.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		
		servico.setQtdRealizadoConvenente(BigDecimal.valueOf(3.0));
		servico.setQtdAcumuladoConvenente(BigDecimal.valueOf(9.0));
	}

	public void addQuantidadeInsuficienteParaServicoConcedente() {
		ServicoVrplDTO servico = submeta.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		
		servico.setQtdRealizadoConcedente(BigDecimal.valueOf(3.0));
		servico.setQtdAcumuladoConcedente(BigDecimal.valueOf(9.0));
	}
	
	public void addQuantidadeTotalParaServicoConvenente() {
		ServicoVrplDTO servico = submeta.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		
		servico.setQtdRealizadoConvenente(BigDecimal.valueOf(3.32));
		servico.setQtdAcumuladoConvenente(BigDecimal.valueOf(15.32));
	}
	
	public void addQuantidadeTotalParaServicoConcedente() {
		ServicoVrplDTO servico = submeta.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		
		servico.setQtdRealizadoConcedente(BigDecimal.valueOf(3.32));
		servico.setQtdAcumuladoConcedente(BigDecimal.valueOf(15.32));
	}
	
	public void addQuantidadeTotalMasSemMedicaoParaServicoCorrenteConvenente() {
		ServicoVrplDTO servico = submeta.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		servico.setQtdAcumuladoConvenente(BigDecimal.valueOf(15.32));
	}
	
	public void addQuantidadeTotalMasSemMedicaoParaServicoCorrenteConcedente() {
		ServicoVrplDTO servico = submeta.getFrentesObra().get(0).getMacroServicosView().get(0).getServicos().get(0);
		servico.setQtdAcumuladoConcedente(BigDecimal.valueOf(15.32));
	}
	
	public static SubmetaMedicaoDTOBuilder newSubmetaMedicaoBuilder_ComEvento(Long idMedicao) {
		SubmetaMedicaoDTOBuilder submetaMedicaoBuilder = new SubmetaMedicaoDTOBuilder();
		submetaMedicaoBuilder.submeta = new SubmetaMedicaoDTO();
		
		submetaMedicaoBuilder.addFrenteObraComEventos(idMedicao);
		
		return submetaMedicaoBuilder;
	}
	
	private void addFrenteObraComEventos(Long idMedicao) {
		
		FrenteObraVrplDTO frenteObra = new FrenteObraVrplDTO();
							
		EventoVrplDTO evento = new EventoVrplDTO();
		evento.setId(1L);
		evento.setNrSeqMedicaoEmpresa(idMedicao);
		evento.setNrSeqMedicaoConvenente(idMedicao);
				
		frenteObra.setId(1L);
		frenteObra.addEventos(evento);
		
		submeta.addFrentesObra(frenteObra);
		
	}
	
	public SubmetaMedicaoDTOBuilder permiteMarcacaoConcedente(boolean permiteMarcacaoConcedente) {
        submeta.setPermiteMarcacaoConcedente(permiteMarcacaoConcedente);
        return this;
    }
	
	public SubmetaMedicaoDTOBuilder setEventoMedidoConcedente(Long idMedicao) {
        submeta.getFrentesObra().get(0).getEventos().get(0).setIdMedicaoConcedente(idMedicao);
        return this;
    }
	
	public SubmetaMedicaoDTOBuilder setEventoMedidoConvenente(Long idMedicao) {
        submeta.getFrentesObra().get(0).getEventos().get(0).setIdMedicaoConvenente(idMedicao);
        return this;
    }
	
	public SubmetaMedicaoDTOBuilder setSituacaoSubmetaConvenente(SituacaoSubmetaEnum situacaoConvenente) {
        submeta.setSituacaoConvenente(situacaoConvenente);
        return this;
    }
    public SubmetaMedicaoDTOBuilder setSituacaoSubmetaConcedente(SituacaoSubmetaEnum situacaoConcedente) {
        submeta.setSituacaoConcedente(situacaoConcedente);
        return this;
    }
	
	public SubmetaMedicaoDTOBuilder permiteMarcacaoConvenente(boolean permiteMarcacaoConvenente) {
        submeta.setPermiteMarcacaoConvenente(permiteMarcacaoConvenente);
        return this;
    }
	
    public SubmetaMedicaoDTOBuilder permiteMarcacaoEmpresa(boolean permiteMarcacaoEmpresa) {
        submeta.setPermiteMarcacaoEmpresa(permiteMarcacaoEmpresa);
        return this;
    }
    
    public SubmetaMedicaoDTOBuilder setEventoMedidoEmpresa(Long idMedicao) {
        submeta.getFrentesObra().get(0).getEventos().get(0).setIdMedicaoEmpresa(idMedicao);
        return this;
    }

    public SubmetaMedicaoDTOBuilder setSituacaoSubmetaEmpresa(SituacaoSubmetaEnum situacaoEmpresa) {
        submeta.setSituacaoEmpresa(situacaoEmpresa);
        return this;
    }
    
    public SubmetaMedicaoDTOBuilder servicoPermiteMedicao(boolean permiteMedicao) {
        submeta.getFrentesObra().get(0).getServicos().get(0).setPermiteMedicao(permiteMedicao);
        return this;
    }

    public SubmetaMedicaoDTOBuilder servicoQtdPlanejado(BigDecimal qtd) {
        submeta.getFrentesObra().get(0).getServicos().get(0).setQtd(qtd);
        return this;
    }

    public SubmetaMedicaoDTOBuilder servicoQtdAcumuladoEmpresa(BigDecimal qtd) {
        submeta.getFrentesObra().get(0).getServicos().get(0).setQtdAcumuladoEmpresa(qtd);
        return this;
    }

    public SubmetaMedicaoDTOBuilder servicoQtdRealizadoEmpresa(BigDecimal qtd) {
        submeta.getFrentesObra().get(0).getServicos().get(0).setQtdRealizadoEmpresa(qtd);
        return this;
    }

    public SubmetaMedicaoDTOBuilder servicos(List<ServicoVrplDTO> servicos) {
        submeta.getFrentesObra().get(0).setServicos(servicos);
        return this;
    }
    
    public SubmetaMedicaoDTOBuilder servicoQtdAcumuladoConvenente(BigDecimal qtd) {
        submeta.getFrentesObra().get(0).getServicos().get(0).setQtdAcumuladoConvenente(qtd);
        return this;
    }

    public SubmetaMedicaoDTOBuilder servicoQtdRealizadoConvenente(BigDecimal qtd) {
        submeta.getFrentesObra().get(0).getServicos().get(0).setQtdRealizadoConvenente(qtd);
        return this;
    }

    public SubmetaMedicaoDTOBuilder servicoQtdAcumuladoConcedente(BigDecimal qtd) {
        submeta.getFrentesObra().get(0).getServicos().get(0).setQtdAcumuladoConcedente(qtd);
        return this;
    }

    public SubmetaMedicaoDTOBuilder servicoQtdRealizadoConcedente(BigDecimal qtd) {
        submeta.getFrentesObra().get(0).getServicos().get(0).setQtdRealizadoConcedente(qtd);
        return this;
    }

    public SubmetaMedicaoDTOBuilder assinarEmpresa(String cpf) {
        submeta.setSituacaoEmpresa(SituacaoSubmetaEnum.ASS);
        submeta.setAssinatura(cpf, Timestamp.from(Instant.now()), PerfilEnum.EMP.getDescricao());
        return this;
    }

    public SubmetaMedicaoDTOBuilder assinarConvenente(String cpf) {
        submeta.setSituacaoConvenente(SituacaoSubmetaEnum.ASS);
        submeta.setAssinatura(cpf, Timestamp.from(Instant.now()), PerfilEnum.CVE.getDescricao());
        return this;
    }

    public SubmetaMedicaoDTOBuilder assinarConcedente(String cpf) {
        submeta.setSituacaoConcedente(SituacaoSubmetaEnum.ASS);
        submeta.setAssinatura(cpf, Timestamp.from(Instant.now()), PerfilEnum.CCE.getDescricao());
        return this;
    }
}
