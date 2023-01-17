package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto;

import static br.gov.planejamento.siconv.med.infra.security.domain.SensitiveDataType.CPF;
import static br.gov.planejamento.siconv.med.infra.security.domain.SensitiveDataType.EMAIL;
import static br.gov.planejamento.siconv.med.infra.security.domain.SensitiveDataType.TELEFONE;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.database.ResponsavelTecnicoBD;
import br.gov.planejamento.siconv.med.infra.security.annotation.SensitiveData;
import lombok.Data;

@Data
@JsonIgnoreProperties ("contratoFk")
public class ResponsavelTecnicoDTO {

	@ColumnName("id") // tabela interna - med_responsavel_tecnico
	private Long id;

	@SensitiveData(type = CPF)
	@ColumnName("nr_cpf") // tabela interna - med_responsavel_tecnico
	private String cpf;

	@ColumnName("nm_responsavel") // tabela externa - vrpl_responsavel_tecnico
	private String nome;
	
	@ColumnName ("med_contrato_fk")
	private Long contratoFk;
	
	@SensitiveData(type = EMAIL)
	private String email;
	
	@SensitiveData(type = TELEFONE)
	private String telefone; 

    private Long versao;
    
    private Long idContratoSiconv;
    
	@JsonInclude(value = Include.NON_EMPTY)
	private List<RegistroProfissionalDTO> registrosProfissional = new ArrayList<>();
	
	public RegistroProfissionalDTO addRegistros(RegistroProfissionalDTO registroProfissionalDTO) {
		int pos = this.registrosProfissional.indexOf(registroProfissionalDTO);
		if (pos == -1) {
			this.registrosProfissional.add(registroProfissionalDTO);
			return registroProfissionalDTO;
		}

		return this.registrosProfissional.get(pos);
	}

	
	public ResponsavelTecnicoBD converterParaBD() {

		ResponsavelTecnicoBD responsavelTecnicoBD = new ResponsavelTecnicoBD();

		responsavelTecnicoBD.setId(this.id);
		responsavelTecnicoBD.setCpf(this.cpf);
		responsavelTecnicoBD.setTelefone(this.telefone);
		responsavelTecnicoBD.setVersao(this.versao);

		return responsavelTecnicoBD;

	}

	/**
	 * Recupera o Registro Profissional que está no Contrato, caso o contrato não esteja vinculado a nenhum Registro Profissional
	 * retorna null.
	 * 
	 * @param contratoFk
	 * @return
	 */
	public RegistroProfissionalDTO obterRegistroProfissional (Long contratoFk) {
		
		RegistroProfissionalDTO registro = null;
		
		for (RegistroProfissionalDTO element : this.getRegistrosProfissional()) {

			Stream<ContratoResponsavelTecnicoDTO> ret = element.getContratos().stream().filter(contr -> 
				contr.contratoFk != null && contr.contratoFk.equals(contratoFk)
			);
			
			if (ret.count() == 1) {
				registro = element;
			}
		}
		
		return registro;
	}
}