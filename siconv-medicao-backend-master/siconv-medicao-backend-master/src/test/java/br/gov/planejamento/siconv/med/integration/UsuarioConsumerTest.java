package br.gov.planejamento.siconv.med.integration;

import static br.gov.planejamento.siconv.med.test.builder.ContratoSiconvBuilder.newContratoDTOBuilder;
import static br.gov.planejamento.siconv.med.test.builder.UsuarioDTOBuilder.newUsuarioDTOBuilder;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.TipoResponsavelTecnicoEnum;
import br.gov.planejamento.siconv.med.contrato.entity.dto.ContratoSiconvDTO;
import br.gov.planejamento.siconv.med.integration.dto.UsuarioDTO;
import br.gov.planejamento.siconv.med.integration.maisbrasil.MaisBrasilGRPCConsumer;
import br.gov.planejamento.siconv.med.integration.siconv.SiconvGRPCConsumer;
import br.gov.planejamento.siconv.med.medicao.entity.dto.PerfilEnum;

class UsuarioConsumerTest {
	
	@InjectMocks
	private UsuarioConsumer usuarioConsumer;

	@Mock
	private MaisBrasilGRPCConsumer maisBrasilConsumer;
	
	@Mock
	private SiconvGRPCConsumer siconvConsumer;
		
	@SuppressWarnings("unchecked")
	final HashMap<String, UsuarioDTO> usuarioHash = mock(HashMap.class);
	
	@BeforeEach
	void setup() throws Exception {

		MockitoAnnotations.initMocks(this);
	}

	@Test
	void testGetNomeUsuario_cpfVazio() {
		
		String nomeUsuario = usuarioConsumer.getNomeUsuario(EMPTY, PerfilEnum.EMP, true);
		
		assertEquals(EMPTY, nomeUsuario);
	}
	
	@Test
	void testGetNomeUsuario_perfilNulo() {
		String cpf = "11111111111";
		
		String nomeUsuario = usuarioConsumer.getNomeUsuario(cpf, null, true);
		
		assertEquals(EMPTY, nomeUsuario);
	}
	
	@Test
	void testGetNomeUsuario_perfilConvenente() {
		String cpf = "11111111111";
		String nome = "convenente";
		
		UsuarioDTO usuario = newUsuarioDTOBuilder().setCPF(cpf).setNome(nome).create();
		
		when(siconvConsumer.consultarUsuarioPorCpf(cpf)).thenReturn(usuario);
		
		String nomeUsuario = usuarioConsumer.getNomeUsuario(cpf, PerfilEnum.CVE, true);
		
		assertEquals(nome, nomeUsuario);
	}
	
	@Test
	void testGetNomeUsuario_perfilEmpresa() {
		String cpf = "11111111111";
		String nome = "empresa";
		
		UsuarioDTO usuario = newUsuarioDTOBuilder().setCPF(cpf).setNome(nome).create();
		
		when(maisBrasilConsumer.consultaUsuarioMaisBrasil(cpf)).thenReturn(usuario);
		
		String nomeUsuario = usuarioConsumer.getNomeUsuario(cpf, PerfilEnum.EMP, true);
		
		assertEquals(nome, nomeUsuario);
	}
	
	@Test
	void testGetNomeUsuario_perfilEmpresa_comCache() {
		String cpf = "11111111111";
		String nome = "empresa";
		
		UsuarioDTO usuario = newUsuarioDTOBuilder().setCPF(cpf).setNome(nome).create();
		
		when(usuarioHash.containsKey(cpf)).thenReturn(true);
		when(usuarioHash.get(cpf)).thenReturn(usuario);
		
		String nomeUsuario = usuarioConsumer.getNomeUsuario(cpf, PerfilEnum.EMP, true);
		
		assertEquals(nome, nomeUsuario);
	}
	
	@Test
	void testGetNomeUsuario_perfilEmpresa_semCache() {
		String cpf = "11111111111";
		String nome = "empresa";
		
		UsuarioDTO usuario = newUsuarioDTOBuilder().setCPF(cpf).setNome(nome).create();
		
		when(usuarioHash.containsKey(cpf)).thenReturn(true);
		when(maisBrasilConsumer.consultaUsuarioMaisBrasil(cpf)).thenReturn(usuario);
		
		String nomeUsuario = usuarioConsumer.getNomeUsuario(cpf, PerfilEnum.EMP, false);
		
		assertEquals(nome, nomeUsuario);
	}
	
	@Test
	void testGetNomeUsuarioSiconv_cpfVazio() {
		String nomeUsuario = usuarioConsumer.getNomeUsuarioSiconv(EMPTY, true);
		assertEquals(EMPTY, nomeUsuario);
	}
	
	@Test
	void testGetNomeUsuarioSiconv() {
		String cpf = "11111111111";
		String nome = "siconv";
		
		UsuarioDTO usuario = newUsuarioDTOBuilder().setCPF(cpf).setNome(nome).create();
		
		when(siconvConsumer.consultarUsuarioPorCpf(cpf)).thenReturn(usuario);
		
		String nomeUsuario = usuarioConsumer.getNomeUsuarioSiconv(cpf, true);
		
		assertEquals(nome, nomeUsuario);
	}
	
	@Test
	void testGetNomeUsuarioPorTipoRT_RT_cpfVazio() {
		String nomeUsuario = usuarioConsumer.getNomeUsuarioPorTipoRT(EMPTY, TipoResponsavelTecnicoEnum.EXE, true);
		assertEquals(EMPTY, nomeUsuario);
	}

	@Test
	void testGetNomeUsuarioPorTipoRT_RT_EXE() {
		
		String cpf = "11111111111";
		String nome = "execucao";
		
		UsuarioDTO usuario = newUsuarioDTOBuilder().setCPF(cpf).setNome(nome).create();
		
		when(maisBrasilConsumer.consultaUsuarioMaisBrasil(cpf)).thenReturn(usuario);
		
		String nomeUsuario = usuarioConsumer.getNomeUsuarioPorTipoRT(cpf, TipoResponsavelTecnicoEnum.EXE, true);
		
		assertEquals(nome, nomeUsuario);
	}
	
	@Test
	void testGetNomeUsuarioPorTipoRT_RT_EXE_semCache() {
		
		String cpf = "11111111111";
		String nome = "execucao";
		
		UsuarioDTO usuario = newUsuarioDTOBuilder().setCPF(cpf).setNome(nome).create();
		
		when(usuarioHash.containsKey(cpf)).thenReturn(true);
		when(maisBrasilConsumer.consultaUsuarioMaisBrasil(cpf)).thenReturn(usuario);
		
		String nomeUsuario = usuarioConsumer.getNomeUsuarioPorTipoRT(cpf, TipoResponsavelTecnicoEnum.EXE, false);
		
		assertEquals(nome, nomeUsuario);
	}
	
	@Test
	void testGetNomeUsuarioPorTipoRT_RT_EXE_comCache() {
		
		String cpf = "11111111111";
		String nome = "execucao";
		
		UsuarioDTO usuario = newUsuarioDTOBuilder().setCPF(cpf).setNome(nome).create();
		
		when(usuarioHash.containsKey(cpf)).thenReturn(true);
		when(usuarioHash.get(cpf)).thenReturn(usuario);
		
		String nomeUsuario = usuarioConsumer.getNomeUsuarioPorTipoRT(cpf, TipoResponsavelTecnicoEnum.EXE, true);
		
		assertEquals(nome, nomeUsuario);
	}
	
	@Test
	void testGetNomeUsuarioPorTipoRT_RT_FIS() {
		
		String cpf = "11111111111";
		String nome = "fiscalizacao";
		
		UsuarioDTO usuario = newUsuarioDTOBuilder().setCPF(cpf).setNome(nome).create();
		
		when(siconvConsumer.consultarUsuarioPorCpf(cpf)).thenReturn(usuario);
		
		String nomeUsuario = usuarioConsumer.getNomeUsuarioPorTipoRT(cpf, TipoResponsavelTecnicoEnum.FIS, true);
		
		assertEquals(nome, nomeUsuario);
	}

	@Test
	void testGetNomeUsuarioPorTipoRT_RT_ANS() {
		
		String cpf = "11111111111";
		String nome = "analise";
		
		UsuarioDTO usuario = newUsuarioDTOBuilder().setCPF(cpf).setNome(nome).create();
		
		when(siconvConsumer.consultarUsuarioPorCpf(cpf)).thenReturn(usuario);
		
		String nomeUsuario = usuarioConsumer.getNomeUsuarioPorTipoRT(cpf, TipoResponsavelTecnicoEnum.ANS, true);
		
		assertEquals(nome, nomeUsuario);
	}
	
	@Test
	void testGetUsuario_cpfVazio() {
		ContratoSiconvDTO contrato = newContratoDTOBuilder().setId(1L).create();
		UsuarioDTO usuario = usuarioConsumer.getUsuario(EMPTY, TipoResponsavelTecnicoEnum.EXE, contrato, true);
		assertThat(usuario, CoreMatchers.nullValue(UsuarioDTO.class));
	}
	
	@Test
	void testGetUsuario_Execucao() {
		String cpf = "11111111111";
		String cnpj = "2222222222222222";
		
		UsuarioDTO usuario = newUsuarioDTOBuilder().setCPF(cpf).create();
		ContratoSiconvDTO contrato = newContratoDTOBuilder().setId(1L)
				.setCnpj(cnpj).create();
		
		when(usuarioHash.containsKey(cpf)).thenReturn(false);
		when(maisBrasilConsumer.consultaUsuarioMaisBrasil(cpf,cnpj)).thenReturn(usuario);
		
		UsuarioDTO usuarioDTO = usuarioConsumer.getUsuario(cpf, TipoResponsavelTecnicoEnum.EXE, contrato, false);
		
		assertEquals(usuario, usuarioDTO);
	}
	
	@Test
	void testGetUsuario_Execucao_comCache() {
		String cpf = "11111111111";
		String cnpj = "2222222222222222";
		
		UsuarioDTO usuario = newUsuarioDTOBuilder().setCPF(cpf).create();
		ContratoSiconvDTO contrato = newContratoDTOBuilder().setId(1L)
				.setCnpj(cnpj).create();
		
		when(usuarioHash.containsKey(cpf)).thenReturn(true);
		when(usuarioHash.get(cpf)).thenReturn(usuario);
		
		UsuarioDTO usuarioDTO = usuarioConsumer.getUsuario(cpf, TipoResponsavelTecnicoEnum.EXE, contrato, true);
		
		assertEquals(usuario, usuarioDTO);
	}
	
	@Test
	void testGetUsuario_Fiscalizacao() {
		String cpf = "11111111111";
		Integer numeroConvenio = 1;
		Integer anoConvenio = 2020;
		
		UsuarioDTO usuario = newUsuarioDTOBuilder().setCPF(cpf).create();
		
		ContratoSiconvDTO contrato = newContratoDTOBuilder().setId(1L).setAnoConvenioRepasse(anoConvenio)
				.setNumeroConvenioRepasse(numeroConvenio).create();
		
		when(usuarioHash.containsKey(cpf)).thenReturn(false);
		when(siconvConsumer.consultarUsuarioMembroConvenio(cpf,numeroConvenio,anoConvenio)).thenReturn(usuario);
		
		UsuarioDTO usuarioDTO = usuarioConsumer.getUsuario(cpf, TipoResponsavelTecnicoEnum.FIS, contrato, true);
		
		assertEquals(usuario, usuarioDTO);
	}
}
