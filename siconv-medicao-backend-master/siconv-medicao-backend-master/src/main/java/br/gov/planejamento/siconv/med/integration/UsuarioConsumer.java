package br.gov.planejamento.siconv.med.integration;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.HashMap;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.TipoResponsavelTecnicoEnum;
import br.gov.planejamento.siconv.med.contrato.entity.dto.ContratoSiconvDTO;
import br.gov.planejamento.siconv.med.integration.dto.UsuarioDTO;
import br.gov.planejamento.siconv.med.integration.maisbrasil.MaisBrasilGRPCConsumer;
import br.gov.planejamento.siconv.med.integration.siconv.SiconvGRPCConsumer;
import br.gov.planejamento.siconv.med.medicao.entity.dto.PerfilEnum;

@RequestScoped
public class UsuarioConsumer {

	private HashMap<String, UsuarioDTO> usuarioHash = new HashMap<>();
	
	@Inject
	private MaisBrasilGRPCConsumer maisBrasilConsumer;

	@Inject
	private SiconvGRPCConsumer siconvConsumer;
	
	public String getNomeUsuario(String cpf, PerfilEnum perfilUsuario, boolean cache) {
		
		UsuarioDTO usuario = null;
		
		if (cache && usuarioHash.containsKey(cpf)) {
            usuario = usuarioHash.get(cpf);
        } else {
		
			if (perfilUsuario != null && perfilUsuario.equals(PerfilEnum.EMP)) {
				usuario = maisBrasilConsumer.consultaUsuarioMaisBrasil(cpf);
			} else {
				usuario = siconvConsumer.consultarUsuarioPorCpf(cpf);
			}
			usuarioHash.put(cpf, usuario);
        }
		return usuario != null ? usuario.getNome() : EMPTY;
	}
	
	public String getNomeUsuarioPorTipoRT(String cpf, 
			TipoResponsavelTecnicoEnum tipoResponsavelTecnicoEnum, boolean cache) {

		UsuarioDTO usuario = null;
		
		if (cache && usuarioHash.containsKey(cpf)) {
            usuario = usuarioHash.get(cpf);
        } else {
		
			switch (tipoResponsavelTecnicoEnum) {
			case EXE:
				usuario = maisBrasilConsumer.consultaUsuarioMaisBrasil(cpf);
				break;
	
			case FIS:
				usuario = siconvConsumer.consultarUsuarioPorCpf(cpf);
				break;
				
			case ANS:
				usuario = siconvConsumer.consultarUsuarioPorCpf(cpf);
				break;
				
			default:
				break;
			}
			
			usuarioHash.put(cpf, usuario);
        }
		
		return usuario != null ? usuario.getNome() : EMPTY;
	}
	
	public String getNomeUsuarioSiconv(String cpf, boolean cache) {
		return this.getNomeUsuario(cpf, null, cache);
	}
	
	public UsuarioDTO getUsuario(String cpf, 
			TipoResponsavelTecnicoEnum tipoRT, ContratoSiconvDTO contrato,
			boolean cache) {
		
		UsuarioDTO usuario = new UsuarioDTO();
		
		if (cache && usuarioHash.containsKey(cpf)) {
            usuario = usuarioHash.get(cpf);
        
		} else {
        	switch (tipoRT) {
			case EXE:
				usuario =  maisBrasilConsumer.consultaUsuarioMaisBrasil(cpf,contrato.getCnpj());
				break;
	
			case FIS:
				usuario =  siconvConsumer.consultarUsuarioMembroConvenio(cpf,
						contrato.getNumeroConvenioRepasse(),
						contrato.getAnoConvenioRepasse());
				break;
				
			default:
				break;
			}
        	usuarioHash.put(cpf, usuario);
        }
		
		return usuario;
	}
}
