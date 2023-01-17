package br.gov.planejamento.siconv.med.medicao.business.builder;

import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.EMPRESA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.PROPONENTE_CONVENENTE;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.gov.planejamento.siconv.med.infra.database.DAOFactory;
import br.gov.planejamento.siconv.med.infra.exception.MedicaoRestException;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.infra.security.domain.Profile;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Context;
import br.gov.planejamento.siconv.med.medicao.dao.MedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.EventoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.FrenteObraVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import io.quarkus.arc.Unremovable;

@Unremovable
@ApplicationScoped
public class PrepararEventoStep extends AbstractSubmetaMedicaoStep {

	private DAOFactory dao;
	
	@Inject
	public PrepararEventoStep(SecurityContext securityContext, DAOFactory dao) {
		super(securityContext);
		this.dao = dao;
	}

	@Override
	public void process(SubmetaMedicaoDTO submetaMedicao, Context builderContext) {
		
		 MedicaoDTO medicaoAtual = builderContext.getMedicao();
		
		for (FrenteObraVrplDTO frenteObraMedicao : submetaMedicao.getFrentesObra()) {

			for (EventoVrplDTO eventoMedicao : frenteObraMedicao.getEventos()) {
	
				if (securityContext.isUserInProfile(EMPRESA)) {
					preencherEventoPerfilEmpresa(medicaoAtual, eventoMedicao, submetaMedicao);
				} else if (securityContext.isUserInProfile(PROPONENTE_CONVENENTE)) {
					preencherEventoPerfilConvenente(medicaoAtual, eventoMedicao, submetaMedicao);
				} else if (securityContext.isUserInProfile(CONCEDENTE) || securityContext.isUserInProfile(Profile.MANDATARIA)) {
					preencherEventoPerfilConcedente(medicaoAtual, eventoMedicao, submetaMedicao);
				} else {
					// preenche dados acesso livre e outros perfis
					preencherEvento(medicaoAtual, eventoMedicao);
				}
			}
		}
	}

	
	private void preencherEvento(MedicaoDTO medicaoAtual, EventoVrplDTO eventoMedicao) {
		
		if (securityContext.isUserInProfile(CONCEDENTE) ||
				securityContext.isUserInProfile(Profile.MANDATARIA) ||
				securityContext.isUserInProfile(PROPONENTE_CONVENENTE) ||
				securityContext.isUserInProfile(EMPRESA)) {
			throw new MedicaoRestException(MessageKey.ERRO_OPERACAO_NAO_PERMITIDA);
		}

		preencheDadosEmpresaParaEventoPerfilConvenente(medicaoAtual, eventoMedicao);

		// Preenche a coluna do Convenente
		if (eventoMedicao.getIdMedicaoConvenente() != null) {
			MedicaoDTO medicaoEventoAtestado = dao.get(MedicaoDAO.class)
					.obterMedicao(eventoMedicao.getIdMedicaoConvenente());
		
			if (medicaoEventoAtestado != null && eventoMedicao.getIdMedicaoConvenente() <= medicaoAtual.getId() 
						&& medicaoEventoAtestado.getSituacao().permitePublicacaoConvenente()) {
					eventoMedicao.setNrSeqMedicaoConvenente(medicaoEventoAtestado.getSequencial().longValue());
					eventoMedicao.setIdMedicaoConvenente(eventoMedicao.getIdMedicaoConvenente());
			}
		} 
		
		// Preenche a coluna do Concedente
		if (eventoMedicao.getIdMedicaoConcedente() != null) {
			MedicaoDTO medicaoEventoAtestado = dao.get(MedicaoDAO.class)
					.obterMedicao(eventoMedicao.getIdMedicaoConcedente());
		
			if (medicaoEventoAtestado != null && eventoMedicao.getIdMedicaoConcedente() <= medicaoAtual.getId() 
						&& medicaoEventoAtestado.getSituacao().permitePublicacaoConcedente()) {
					eventoMedicao.setNrSeqMedicaoConcedente(medicaoEventoAtestado.getSequencial().longValue());
					eventoMedicao.setIdMedicaoConcedente(eventoMedicao.getIdMedicaoConcedente());
			}
		} 
	}
	
	
	private void preencherEventoPerfilEmpresa(MedicaoDTO medicaoAtual, EventoVrplDTO eventoMedicao, SubmetaMedicaoDTO submetaMedicao) {

		if (!securityContext.isUserInProfile(EMPRESA)) {
			throw new MedicaoRestException(MessageKey.ERRO_FUNCIONALIDADE_DISPONIVEL_APENAS_PARA_EMPRESA);
		}
		
		MedicaoDTO medicaoEventoExecutado = null;
		// Preenche a coluna da Empresa
		if (eventoMedicao.getIdMedicaoEmpresa() != null) {
			medicaoEventoExecutado = dao.get(MedicaoDAO.class)
					.obterMedicao(eventoMedicao.getIdMedicaoEmpresa());
			
			if (medicaoEventoExecutado != null) {
				eventoMedicao.setIndRealizado(true);
				eventoMedicao.setNrSeqMedicaoEmpresa(medicaoEventoExecutado.getSequencial().longValue());
				eventoMedicao.setIdMedicaoEmpresa(eventoMedicao.getIdMedicaoEmpresa());
				
			}
		} else {
			// Caso o Evento não tenha sido marcado pela EMPRESA e tenha a Situação da Submeta para a EMPRESA diferente de Nulo 
			// então é porque o Evento NÃO foi realizado pela EMPRESA.			
			if (submetaMedicao.getSituacaoEmpresa() != null) {
				eventoMedicao.setIndRealizado(false);
			}
		}
		
		// Preenche a coluna do Convenente
		if (eventoMedicao.getIdMedicaoConvenente() != null) {
			MedicaoDTO medicaoEventoAtestado = dao.get(MedicaoDAO.class)
					.obterMedicao(eventoMedicao.getIdMedicaoConvenente());
			
			// Caso o Evento tenha já tenha sido medido pelo Convenente na medição atual ou anterior e a medição
			// a medição NÃO esteja em edição pelo Convenente ou seja já esteja publicada.
			if (medicaoEventoAtestado != null && eventoMedicao.getIdMedicaoConvenente() <= medicaoAtual.getId()
				&& medicaoEventoAtestado.getSituacao().permitePublicacaoConvenente()) {
				eventoMedicao.setNrSeqMedicaoConvenente(medicaoEventoAtestado.getSequencial().longValue());
				eventoMedicao.setIdMedicaoConvenente(eventoMedicao.getIdMedicaoConvenente());
			}
		}
		
		MedicaoDTO medicaoEventoAnalisado = null;
		// Preenche a coluna do Concedente
		if (eventoMedicao.getIdMedicaoConcedente() != null) {
			medicaoEventoAnalisado = dao.get(MedicaoDAO.class)
					.obterMedicao(eventoMedicao.getIdMedicaoConcedente());
			
			if (medicaoEventoAnalisado != null && eventoMedicao.getIdMedicaoConcedente() <= medicaoAtual.getId()
					&& medicaoEventoAnalisado.getSituacao().permitePublicacaoConcedente()) {
				eventoMedicao.setIndRealizado(true);
				eventoMedicao.setNrSeqMedicaoConcedente(medicaoEventoAnalisado.getSequencial().longValue());
				eventoMedicao.setIdMedicaoConcedente(eventoMedicao.getIdMedicaoConcedente());
			}
			
		} 
		
		eventoMedicao.setPermiteMarcacao(this.permiteMarcacaoEvento(medicaoAtual, medicaoEventoExecutado, submetaMedicao, eventoMedicao));
		
	}
	
	
	private void preencherEventoPerfilConvenente(MedicaoDTO medicaoAtual, EventoVrplDTO eventoMedicao, SubmetaMedicaoDTO submetaMedicao) {
		
		if (!securityContext.isUserInProfile(PROPONENTE_CONVENENTE)) {
			throw new MedicaoRestException(MessageKey.ERRO_FUNCIONALIDADE_DISPONIVEL_APENAS_PARA_CONVENENTE);
		}

		// Preenche a coluna da Empresa
		preencheDadosEmpresaParaEventoPerfilConvenente(medicaoAtual, eventoMedicao);
		
		MedicaoDTO medicaoEventoAtestado = null;
		// Preenche a coluna do Convenente
		if (eventoMedicao.getIdMedicaoConvenente() != null) {
			medicaoEventoAtestado = dao.get(MedicaoDAO.class)
					.obterMedicao(eventoMedicao.getIdMedicaoConvenente());
			
			if (medicaoEventoAtestado != null && eventoMedicao.getIdMedicaoConvenente() <= medicaoAtual.getId()
					&& (medicaoEventoAtestado.getSituacao().permitePublicacaoConvenente() || medicaoEventoAtestado.getSituacao().permiteManutencaoConvenente())) {
				eventoMedicao.setIndRealizado(true);
				eventoMedicao.setNrSeqMedicaoConvenente(medicaoEventoAtestado.getSequencial().longValue());
				eventoMedicao.setIdMedicaoConvenente(eventoMedicao.getIdMedicaoConvenente());
			}
			
		} else {
			// Caso o Evento não tenha sido marcado pelo CONVENENTE, tenha sido Marcado pela EMPRESA e tenha a Situação da Submeta 
			// para o CONVENENTE diferente de Nulo é porque houve uma glosa no Evento então o Evento NÃO foi realizado pelo CONVENENTE.	
			if (submetaMedicao.getSituacaoConvenente() != null && eventoMedicao.getIdMedicaoEmpresa() != null) {
				eventoMedicao.setIndRealizado(false);
			}
		}
		
		MedicaoDTO medicaoEventoAnalisado = null;
		// Preenche a coluna do Concedente
		if (eventoMedicao.getIdMedicaoConcedente() != null) {
			medicaoEventoAnalisado = dao.get(MedicaoDAO.class)
					.obterMedicao(eventoMedicao.getIdMedicaoConcedente());
			
			if (medicaoEventoAnalisado != null && eventoMedicao.getIdMedicaoConcedente() <= medicaoAtual.getId()
					&& medicaoEventoAnalisado.getSituacao().permitePublicacaoConcedente()) {
				eventoMedicao.setNrSeqMedicaoConcedente(medicaoEventoAnalisado.getSequencial().longValue());
				eventoMedicao.setIdMedicaoConcedente(eventoMedicao.getIdMedicaoConcedente());
			}
			
		} 
		
		eventoMedicao.setPermiteMarcacao(this.permiteMarcacaoEvento(medicaoAtual, medicaoEventoAtestado, submetaMedicao, eventoMedicao));
	}
	
	
	private void preencherEventoPerfilConcedente(MedicaoDTO medicaoAtual, EventoVrplDTO eventoMedicao, SubmetaMedicaoDTO submetaMedicao) {
		
		if (!securityContext.isUserInProfile(CONCEDENTE) && !securityContext.isUserInProfile(Profile.MANDATARIA)) {
			throw new MedicaoRestException(MessageKey.ERRO_FUNCIONALIDADE_DISPONIVEL_APENAS_PARA_CONCEDENTE_MANDATARIA);
		}

		preencheDadosEmpresaParaEventoPerfilConvenente(medicaoAtual, eventoMedicao);
		
		preencheDadosConvenenteParaEventoPerfilConvenente(medicaoAtual, eventoMedicao);
		
		MedicaoDTO medicaoEventoAnalisado = preencheDadosConcedenteParaEventoPerfilConvenente(medicaoAtual, eventoMedicao, submetaMedicao);
		
		eventoMedicao.setPermiteMarcacao(this.permiteMarcacaoEvento(medicaoAtual, medicaoEventoAnalisado, submetaMedicao, eventoMedicao));
	}
	
	private MedicaoDTO preencheDadosConvenenteParaEventoPerfilConvenente(MedicaoDTO medicaoAtual,
			EventoVrplDTO eventoMedicao) {
		MedicaoDTO medicaoEventoAtestado = null;
		// Preenche a coluna do Convenente
		if (eventoMedicao.getIdMedicaoConvenente() != null) {
			medicaoEventoAtestado = dao.get(MedicaoDAO.class)
					.obterMedicao(eventoMedicao.getIdMedicaoConvenente());
			
			if (medicaoEventoAtestado != null && eventoMedicao.getIdMedicaoConvenente() <= medicaoAtual.getId()
					&& (medicaoEventoAtestado.getSituacao().permitePublicacaoConvenente() || medicaoEventoAtestado.getSituacao().permiteManutencaoConvenente())) {
				eventoMedicao.setNrSeqMedicaoConvenente(medicaoEventoAtestado.getSequencial().longValue());
				eventoMedicao.setIdMedicaoConvenente(eventoMedicao.getIdMedicaoConvenente());
			}	
		}
		return medicaoEventoAtestado;
	}
	
	
	
	private void preencheDadosEmpresaParaEventoPerfilConvenente(MedicaoDTO medicaoAtual, EventoVrplDTO eventoMedicao) {
		if (eventoMedicao.getIdMedicaoEmpresa() != null) {
			MedicaoDTO medicaoEventoExecutado = dao.get(MedicaoDAO.class)
					.obterMedicao(eventoMedicao.getIdMedicaoEmpresa());
				
			if (medicaoEventoExecutado != null && eventoMedicao.getIdMedicaoEmpresa() <= medicaoAtual.getId()
					&& medicaoEventoExecutado.getSituacao().permitePublicacaoEmpresa()) {
				
				eventoMedicao.setNrSeqMedicaoEmpresa(medicaoEventoExecutado.getSequencial().longValue());
				eventoMedicao.setIdMedicaoEmpresa(eventoMedicao.getIdMedicaoEmpresa());
			}
		}
	}
	
	
	private MedicaoDTO preencheDadosConcedenteParaEventoPerfilConvenente(MedicaoDTO medicaoAtual, EventoVrplDTO eventoMedicao,
			SubmetaMedicaoDTO submetaMedicao) {
		MedicaoDTO medicaoEventoAnalisado = null;
		// Preenche a coluna do Concedente
		if (eventoMedicao.getIdMedicaoConcedente() != null) {
			medicaoEventoAnalisado = dao.get(MedicaoDAO.class)
					.obterMedicao(eventoMedicao.getIdMedicaoConcedente());
			
			if (medicaoEventoAnalisado != null && eventoMedicao.getIdMedicaoConcedente() <= medicaoAtual.getId()
					&& (medicaoEventoAnalisado.getSituacao().permitePublicacaoConcedente() || medicaoEventoAnalisado.getSituacao().permiteManutencaoConcedente())) {
				eventoMedicao.setIndRealizado(true);
				eventoMedicao.setNrSeqMedicaoConcedente(medicaoEventoAnalisado.getSequencial().longValue());
				eventoMedicao.setIdMedicaoConcedente(eventoMedicao.getIdMedicaoConcedente());
			}
			
		} else {
			// Caso o Evento não tenha sido marcado pelo CONVENENTE, tenha sido Marcado pela EMPRESA e tenha a Situação da Submeta 
			// para o CONVENENTE diferente de Nulo é porque houve uma glosa no Evento então o Evento NÃO foi realizado pelo CONVENENTE.	
			if (submetaMedicao.getSituacaoConcedente() != null && eventoMedicao.getIdMedicaoEmpresa() != null) {
				eventoMedicao.setIndRealizado(false);
			}
		}
		
		return medicaoEventoAnalisado;
	}
	
	
	/**
	 * Verifica de Acordo com o Perfil logado e a Medição se o Evento pode ser Medido
	 * 
	 * @param medicaoAtual
	 * @param medicaoEventoExecutado
	 * @return
	 */
	private Boolean permiteMarcacaoEvento(MedicaoDTO medicaoAtual, MedicaoDTO medicaoEventoRealizado, SubmetaMedicaoDTO submetaMedicao, EventoVrplDTO eventoMedicao) {
		
		Boolean permiteManutencaoEvento = null;
		
		if (securityContext.isUserInProfile(EMPRESA)) {
			permiteManutencaoEvento = submetaMedicao.isPermiteMarcacaoEmpresa();
		} else if (securityContext.isUserInProfile(PROPONENTE_CONVENENTE)) {
			permiteManutencaoEvento = eventoMedicao.getIdMedicaoEmpresa() != null && submetaMedicao.isPermiteMarcacaoConvenente();
		} else if (securityContext.isUserInProfile(CONCEDENTE) || securityContext.isUserInProfile(Profile.MANDATARIA)) {
			permiteManutencaoEvento = eventoMedicao.getIdMedicaoConvenente() != null && submetaMedicao.isPermiteMarcacaoConcedente();
		} else {
			permiteManutencaoEvento = Boolean.FALSE;
		}
		
		// Se o Evento ainda não foi Medido então o atributo medicaoEventoRealizado é nulo, nesse caso será 
		// verificada a possibilidade do Evento ser medido apenas pela Situação da Medição Atual.
		if (medicaoEventoRealizado == null) {
			return permiteManutencaoEvento;
		} else {
			return validaPermiteMarcacaoEvento(medicaoAtual, medicaoEventoRealizado, permiteManutencaoEvento);
		}
		
	}
	
	private Boolean validaPermiteMarcacaoEvento(MedicaoDTO medicaoAtual, MedicaoDTO medicaoEventoRealizado,
			Boolean permiteManutencaoEvento) {
		// Verifica se a Medição pode ser medida Usuario Logado.  
		if (permiteManutencaoEvento.booleanValue()) {
			// Se o Evento já foi medido (medicaoEventoRealizado), verifica se a Medição que mediu é 
			// a própria Medição Atual ou uma filha
			
			return (medicaoAtual.getSequencial().equals(medicaoEventoRealizado.getSequencial()) ||
				(medicaoEventoRealizado.getIdMedicaoAgrupadora() != null && medicaoEventoRealizado.getIdMedicaoAgrupadora().equals(medicaoAtual.getId())));
		}
		
		return false;
	}

}
