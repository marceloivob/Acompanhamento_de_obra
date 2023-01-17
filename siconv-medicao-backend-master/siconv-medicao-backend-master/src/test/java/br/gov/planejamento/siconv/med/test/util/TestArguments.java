package br.gov.planejamento.siconv.med.test.util;

import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.CONCEDENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.EMPRESA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.GUEST;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.MANDATARIA;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.PROPONENTE_CONVENENTE;
import static br.gov.planejamento.siconv.med.infra.security.domain.Profile.USUARIO_SICONV;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.AC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ACT;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.AT;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ATD;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.CC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.CE;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.EC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ECC;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.ECE;
import static br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum.EM;
import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import br.gov.planejamento.siconv.med.infra.security.domain.Profile;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;

public class TestArguments {

    private TestArguments() {
    }

    public static Stream<Arguments> listaParametrosTodasSituacoes() {
        return Stream.of(SituacaoMedicaoEnum.values()).map(Arguments::of);
    }

    public static Stream<Arguments> listaParametrosTodosProfiles() {
        return Stream.of(Profile.values()).map(Arguments::of);
    }

    public static Stream<Arguments> listaParametrosTodosProfilesExcetoEmpresa() {
        return Stream.of(Profile.values()).filter(not(isEqual(EMPRESA))).map(Arguments::of);
    }

    public static List<Arguments> listaParametrosOutroProfileComSituacaoNaoPermiteManutencaoEmpresa() {
        List<Profile> profiles = List.of(PROPONENTE_CONVENENTE, CONCEDENTE, MANDATARIA, GUEST, USUARIO_SICONV);
        List<SituacaoMedicaoEnum> situacoes = List.of(EC, AT, ATD, ECE, AC, ACT, ECC, CC);
        return combinarParametros(profiles, situacoes);
    }

    public static List<Arguments> listaParametrosOutroProfileComSituacaoPermiteManutencaoEmpresa() {
        List<Profile> profiles = List.of(PROPONENTE_CONVENENTE, CONCEDENTE, MANDATARIA, GUEST, USUARIO_SICONV);
        List<SituacaoMedicaoEnum> situacoes = List.of(EM, CE);
        return combinarParametros(profiles, situacoes);
    }

    public static List<Arguments> listaParametrosOutroProfileComSituacaoNaoPermiteManutencaoConvenente() {
        List<Profile> profiles = List.of(EMPRESA, CONCEDENTE, MANDATARIA, GUEST, USUARIO_SICONV);
        List<SituacaoMedicaoEnum> situacoes = List.of(EM, EC, ATD, CE, ECE, AC, ACT, ECC);
        return combinarParametros(profiles, situacoes);
    }

    public static List<Arguments> listaParametrosOutroProfileComSituacaoPermiteManutencaoConvenente() {
        List<Profile> profiles = List.of(EMPRESA, CONCEDENTE, MANDATARIA, GUEST, USUARIO_SICONV);
        List<SituacaoMedicaoEnum> situacoes = List.of(AT, CC);
        return combinarParametros(profiles, situacoes);
    }

    public static List<Arguments> listaParametrosOutroProfileComSituacaoPermitePublicacaoConcedente() {
        List<Profile> profiles = List.of(EMPRESA, PROPONENTE_CONVENENTE, GUEST, USUARIO_SICONV);
        List<SituacaoMedicaoEnum> situacoes = List.of(ACT);
        return combinarParametros(profiles, situacoes);
    }

    public static List<Arguments> listaParametrosOutroProfileComSituacaoNaoPermitePublicacaoConcedente() {
        List<Profile> profiles = List.of(EMPRESA, PROPONENTE_CONVENENTE, GUEST, USUARIO_SICONV);
        List<SituacaoMedicaoEnum> situacoes = List.of(EM, EC, AT, ATD, ECE, CE, AC, ECC, CC);
        return combinarParametros(profiles, situacoes);
    }

    private static List<Arguments> combinarParametros(List<Profile> profiles, List<SituacaoMedicaoEnum> situacoes) {
        List<Arguments> parametros = new ArrayList<Arguments>();
        profiles.forEach(profile -> situacoes.forEach(situacao -> parametros.add(Arguments.of(profile, situacao))));
        return parametros;
    }
}
