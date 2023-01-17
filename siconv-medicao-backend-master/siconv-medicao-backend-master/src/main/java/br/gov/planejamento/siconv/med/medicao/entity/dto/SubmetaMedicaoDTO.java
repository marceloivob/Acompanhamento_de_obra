package br.gov.planejamento.siconv.med.medicao.entity.dto;

import static br.gov.planejamento.siconv.med.infra.security.domain.SensitiveDataType.CPF;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import br.gov.planejamento.siconv.med.medicao.entity.SituacaoSubmetaEnum;
import br.gov.planejamento.siconv.med.infra.security.annotation.SensitiveData;
import lombok.Data;

@Data
public class SubmetaMedicaoDTO {

    private Long id;
    
    private Integer nrMeta;
    private Integer nrSubmeta;
    
    private String descricao;
    private BigDecimal valor;

    private BigDecimal valorRealizadoEmpresa;
    private BigDecimal percentualRealizadoEmpresa;
    private BigDecimal valorRealizadoAcumuladoEmpresa;
    private BigDecimal percentualRealizadoAcumuladoEmpresa;

    private BigDecimal valorRealizadoConvenente;
    private BigDecimal percentualRealizadoConvenente;
    private BigDecimal valorRealizadoAcumuladoConvenente;
    private BigDecimal percentualRealizadoAcumuladoConvenente;

    private BigDecimal valorRealizadoConcedente;
    private BigDecimal percentualRealizadoConcedente;
    private BigDecimal valorRealizadoAcumuladoConcedente;
    private BigDecimal percentualRealizadoAcumuladoConcedente;

    @JsonInclude(value = Include.NON_EMPTY)
    private List<FrenteObraVrplDTO> frentesObra = new ArrayList<>();

    @JsonInclude(value = Include.NON_NULL)
    private List<Assinatura> assinaturas = new ArrayList<>();

    private SituacaoSubmetaEnum situacaoEmpresa;

    private SituacaoSubmetaEnum situacaoConvenente;
    
    private SituacaoSubmetaEnum situacaoConcedente;
    
    private boolean permiteMarcacaoEmpresa;
    
    private boolean permiteMarcacaoConvenente;
    
    private boolean permiteMarcacaoConcedente;

    @ColumnName("versao")
    private Long versao;

    private Long idContratoSiconv;

    public FrenteObraVrplDTO addFrentesObra(FrenteObraVrplDTO frenteObraVrplDTO) {
        int pos = this.frentesObra.indexOf(frenteObraVrplDTO);
        if (pos == -1) {
            this.frentesObra.add(frenteObraVrplDTO);
            return frenteObraVrplDTO;
        }

        return this.frentesObra.get(pos);
    }

    @Data
    public class Assinatura {
        private Responsavel responsavel = new Responsavel();
        private Timestamp data;
    }

    @Data
    public class Responsavel {
        @SensitiveData(type = CPF)
        private String nrCpf;
        private String nome;
        private String perfil;
        private String nrCrea;
    }

    public void setAssinatura(String nrCpf, Timestamp data) {
    	Assinatura assinatura = new Assinatura();
    	assinatura.getResponsavel().setNrCpf(nrCpf);
        assinatura.setData(data);
        if(!this.getAssinaturas().contains(assinatura)) {
        	this.getAssinaturas().add(assinatura);
        }
    }

    public void setAssinatura(String nrCpf, Timestamp data, String perfil) {
    	Assinatura assinatura = new Assinatura();
    	assinatura.getResponsavel().setNrCpf(nrCpf);
    	assinatura.getResponsavel().setPerfil(perfil);
        assinatura.setData(data);
        if(!this.getAssinaturas().contains(assinatura)) {
        	this.getAssinaturas().add(assinatura);
        }
    }

}
