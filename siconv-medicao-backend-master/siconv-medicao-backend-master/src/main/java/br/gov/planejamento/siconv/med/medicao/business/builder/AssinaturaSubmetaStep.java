package br.gov.planejamento.siconv.med.medicao.business.builder;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.TipoResponsavelTecnicoEnum;
import br.gov.planejamento.siconv.med.infra.database.DAOFactory;
import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.integration.UsuarioConsumer;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Context;
import br.gov.planejamento.siconv.med.medicao.dao.SubmetaDAO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ResponsavelTecnicoFiscalizacaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO.Assinatura;
import io.quarkus.arc.Unremovable;

@Unremovable
@ApplicationScoped
public class AssinaturaSubmetaStep extends AbstractSubmetaMedicaoStep {

    private DAOFactory dao;

    private UsuarioConsumer usuarioConsumer;

    @Inject
    public AssinaturaSubmetaStep(SecurityContext securityContext, DAOFactory dao, UsuarioConsumer usuarioConsumer) {
        super(securityContext);
        this.dao = dao;
        this.usuarioConsumer = usuarioConsumer;
    }

    @Override
    public void process(SubmetaMedicaoDTO submetaMedicao, Context builderContext) {

        List<Assinatura> listaAssinaturaIt = new ArrayList<>();
        listaAssinaturaIt.addAll(submetaMedicao.getAssinaturas());

        for (Assinatura assinatura : listaAssinaturaIt) {

            ResponsavelTecnicoFiscalizacaoDTO responsavelAssinatura = null;

            if (assinatura.getResponsavel().getNrCpf() != null) {
                responsavelAssinatura = recuperarDadosResponsavelTecnicoSubmeta(assinatura.getResponsavel().getNrCpf(),
                        submetaMedicao.getId(), builderContext.getContrato().isInSocial());

                avaliaAssinatura(submetaMedicao, assinatura, responsavelAssinatura, builderContext);
            }
        }
    }

    private void avaliaAssinatura(SubmetaMedicaoDTO submetaMedicao, Assinatura assinatura,
            ResponsavelTecnicoFiscalizacaoDTO responsavelAssinatura, Context builderContext) {

        MedicaoDTO medicao = builderContext.getMedicao();

        if (responsavelAssinatura != null) {
            assinatura.getResponsavel().setNrCrea(responsavelAssinatura.getCreaCau());
            assinatura.getResponsavel().setPerfil(responsavelAssinatura.getPerfil());

            // Se a Assinatura pode ser exibida então recupera o nome de quem Assinou
            if (isExibirAssinatura(medicao.getId(), assinatura.getResponsavel().getPerfil(), builderContext)) {
                assinatura.getResponsavel().setNome(usuarioConsumer.getNomeUsuarioPorTipoRT(
                        assinatura.getResponsavel().getNrCpf(), responsavelAssinatura.getTipo(), Boolean.TRUE));
            } else {// Caso a Assinatura não possa ser exibida ela será removida da Lista de
                    // Assinaturas da Submeta
                submetaMedicao.getAssinaturas().remove(assinatura);
            }

        } else {
            if (isExibirAssinatura(medicao.getId(), "Concedente/Mandatária", builderContext)) {

                assinatura.getResponsavel().setNome(usuarioConsumer.getNomeUsuarioPorTipoRT(
                        assinatura.getResponsavel().getNrCpf(), TipoResponsavelTecnicoEnum.ANS, Boolean.TRUE));
            } else {// Caso a Assinatura não possa ser exibida ela será removida da Lista de
                    // Assinaturas da Submeta
                submetaMedicao.getAssinaturas().remove(assinatura);
            }
        }
    }

    /**
     * Recuperar dados do responsavel pela assinatura da submeta no Vrpl
     * 
     * @param idLicitacaoVrpl, nrCpfResp
     * @return
     */
    private ResponsavelTecnicoFiscalizacaoDTO recuperarDadosResponsavelTecnicoSubmeta(
            String cpfResponsavelTecnicoSubmeta, Long idSubmetaVrpl, boolean inSocial) {

        ResponsavelTecnicoFiscalizacaoDTO retorno;

        if (inSocial) {
            retorno = dao.get(SubmetaDAO.class).consultarDadosResponsavelTecnicoSocial(cpfResponsavelTecnicoSubmeta,
                    idSubmetaVrpl);
        } else {
            retorno = dao.get(SubmetaDAO.class).consultarDadosResponsavelTecnicoArqEng(cpfResponsavelTecnicoSubmeta,
                    idSubmetaVrpl);
        }
        return retorno;

    }

    /**
     * Verifica se deve exibir assinatura de acordo com 3 Critérios
     * 
     * 1 - Usuário Logado 2 - Perfil de quem assinou 3 - Situação da Medição
     * 
     * @param situacaoMedicao
     * @param perfilAssinatura
     * @return
     */
    private boolean isExibirAssinatura(Long idMedicao, String perfilAssinatura, Context builderContext) {

        return perfilAssinatura.equals("Empresa") && permiteVisualizarDadosEmpresa(idMedicao, builderContext)
                || perfilAssinatura.equals("Convenente") && permiteVisualizarDadosConvenente(idMedicao, builderContext)
                || perfilAssinatura.equals("Concedente/Mandatária")
                        && permiteVisualizarDadosConcedente(idMedicao, builderContext);
    }

}
