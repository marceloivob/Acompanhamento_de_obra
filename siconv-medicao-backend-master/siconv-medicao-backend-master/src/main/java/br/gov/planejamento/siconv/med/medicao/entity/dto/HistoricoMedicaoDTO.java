package br.gov.planejamento.siconv.med.medicao.entity.dto;

import static br.gov.planejamento.siconv.med.infra.security.domain.SensitiveDataType.CPF;

import java.time.Instant;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import br.gov.planejamento.siconv.med.infra.security.annotation.SensitiveData;
import br.gov.planejamento.siconv.med.medicao.entity.SituacaoMedicaoEnum;
import lombok.Data;

@Data
public class HistoricoMedicaoDTO {

    @SensitiveData(type = CPF)
    private String nrCpfResponsavel;

    @ColumnName("perfil")
    private PerfilEnum inPerfilResponsavel;

    private Short nrSequencial;

    @ColumnName("situacao")
    private SituacaoMedicaoEnum inSituacao;

    private Instant dataHora;

    private String nomeResponsavel;

}
