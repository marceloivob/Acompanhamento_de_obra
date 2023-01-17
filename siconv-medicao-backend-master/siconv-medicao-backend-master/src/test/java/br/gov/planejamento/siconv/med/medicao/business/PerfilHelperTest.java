package br.gov.planejamento.siconv.med.medicao.business;

import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.EMPRESA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.GUEST;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.MANDATARIA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.PROPONENTE_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.USUARIO_SICONV;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.FISCAL_ACOMPANHAMENTO;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.FISCAL_CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Role.TECNICO_TERCEIRO;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import br.gov.planejamento.siconv.med.infra.security.domain.Role;
import br.gov.planejamento.siconv.med.medicao.business.PerfilHelper;
import br.gov.planejamento.siconv.med.medicao.entity.dto.PerfilEnum;
import br.gov.planejamento.siconv.med.test.extension.MockUsuario;
import br.gov.planejamento.siconv.med.test.extension.SecurityBaseTest;

class PerfilHelperTest extends SecurityBaseTest {

	@InjectMocks
	private PerfilHelper perfilHelper;

	@BeforeEach
	void setup() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@MockUsuario(profile = EMPRESA)
	@Test
	void testPerfilEmpresa() {
		assertPerfilUsuarioLogado(PerfilEnum.EMP);
	}

	@MockUsuario(profile = PROPONENTE_CONVENENTE)
	@Test
	void testPerfilConvenente() {
		assertPerfilUsuarioLogado(PerfilEnum.CVE);
	}

	@MockUsuario(profile = MANDATARIA)
	@Test
	void testPerfilMandataria() {
		assertPerfilUsuarioLogado(PerfilEnum.MAN);
	}

	@MockUsuario(profile = CONCEDENTE, roles = FISCAL_ACOMPANHAMENTO)
	@Test
	void testPerfilFiscalAcompanhamento() {
		assertPerfilUsuarioLogado(PerfilEnum.FSA);
	}

	@MockUsuario(profile = CONCEDENTE, roles = TECNICO_TERCEIRO)
	@Test
	void testPerfilTecnicoTerceiro() {
		assertPerfilUsuarioLogado(PerfilEnum.TTE);
	}

	@MockUsuario(profile = CONCEDENTE, roles = FISCAL_CONCEDENTE)
	@Test
	void testPerfilConcedente() {
		assertPerfilUsuarioLogado(PerfilEnum.CCE);
	}

	@MockUsuario(profile = CONCEDENTE, roles = { FISCAL_CONCEDENTE, FISCAL_ACOMPANHAMENTO })
	@Test
	void testPerfilConcedenteComPapelFiscal() {
		assertPerfilUsuarioLogado(PerfilEnum.CCE);
	}

	@MockUsuario(profile = CONCEDENTE, roles = { FISCAL_CONCEDENTE, TECNICO_TERCEIRO })
	@Test
	void testPerfilConcedenteComPapelTecnico() {
		assertPerfilUsuarioLogado(PerfilEnum.CCE);
	}

	@MockUsuario(profile = GUEST)
	@Test
	void testPerfilNuloProfileGuest() {
		assertPerfilUsuarioLogado(null);
	}

	@MockUsuario(profile = USUARIO_SICONV)
	@Test
	void testPerfilNuloProfileUsuarioSiconv() {
		assertPerfilUsuarioLogado(null);
	}

	@MockUsuario(profile = CONCEDENTE, roles = { Role.ADMINISTRADOR_SISTEMA })
	@Test
	void testPerfilAdministradorProprioOrgao() {
		assertPerfilUsuarioLogado(PerfilEnum.ADM);
	}

	@MockUsuario(profile = CONCEDENTE, roles = { Role.ADMINISTRADOR_SISTEMA_ORGAO_EXTERNO })
	@Test
	void testPerfilAdministradorOutroOrgao() {
		assertPerfilUsuarioLogado(PerfilEnum.ADM);
	}

	@MockUsuario(profile = CONCEDENTE, roles = { Role.FISCAL_CONCEDENTE, Role.ADMINISTRADOR_SISTEMA })
	@Test
	void testPerfilConcedenteComPerfilAdministrador() {
		assertPerfilUsuarioLogado(PerfilEnum.CCE);
	}

	private void assertPerfilUsuarioLogado(PerfilEnum perfilEsperado) {
		assertEquals(perfilEsperado, perfilHelper.getPerfilUsuarioLogado());
	}
}
