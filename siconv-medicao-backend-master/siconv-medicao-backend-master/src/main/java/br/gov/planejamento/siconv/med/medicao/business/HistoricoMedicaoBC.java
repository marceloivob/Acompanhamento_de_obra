package br.gov.planejamento.siconv.med.medicao.business;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.gov.planejamento.siconv.med.infra.database.DAOFactory;
import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.integration.UsuarioConsumer;
import br.gov.planejamento.siconv.med.medicao.dao.HistoricoMedicaoDAO;
import br.gov.planejamento.siconv.med.medicao.entity.database.HistoricoMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.dto.HistoricoMedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.PerfilEnum;

@ApplicationScoped
public class HistoricoMedicaoBC {

    @Inject
    private DAOFactory dao;

    @Inject
    private SecurityContext securityContext;

    @Inject
    private UsuarioConsumer usuarioConsumer;

    @Inject
    private PerfilHelper perfilHelper;

    public void inserir(HistoricoMedicaoBD historicoMedicao) {

        historicoMedicao.setNrCpfResponsavel(securityContext.getUser().getCpf());
        historicoMedicao.setInPerfilResponsavel(perfilHelper.getPerfilUsuarioLogado());

        dao.getJdbi()
                .useTransaction(transaction -> transaction.attach(HistoricoMedicaoDAO.class).inserir(historicoMedicao));
    }

    public List<HistoricoMedicaoDTO> buscarHistoricosMedicao(Long idContratoSiconv) {

        List<HistoricoMedicaoDTO> historicos = dao.get(HistoricoMedicaoDAO.class)
                .recuperarHistoricoMedicaoPorContrato(idContratoSiconv);

        historicos.forEach(this::decorate);

        return historicos;
    }

    private void decorate(HistoricoMedicaoDTO historico) {

        if (historico.getInPerfilResponsavel() == PerfilEnum.ADM) {
            historico.setNrCpfResponsavel(null);
            historico.setNomeResponsavel("ADMINISTRADOR DO SISTEMA");

        } else {
            historico.setNomeResponsavel(usuarioConsumer.getNomeUsuario(historico.getNrCpfResponsavel(),
                    historico.getInPerfilResponsavel(), true));
        }
    }

    public Optional<HistoricoMedicaoBD> recuperarUltimoHistoricoPorMedicaoContrato(Long idContrato,
            Short sequencialMedicao) {

        return dao.get(HistoricoMedicaoDAO.class).recuperarUltimoHistoricoPorMedicaoContrato(idContrato,
                sequencialMedicao);
    }

    public Optional<HistoricoMedicaoBD> recuperarPenultimoHistoricoPorMedicaoContrato(Long idContrato,
            Short sequencialMedicao) {

        List<HistoricoMedicaoBD> listaHistoricoMedicao = dao.get(HistoricoMedicaoDAO.class)
                .recuperarHistoricoMedicao(idContrato, sequencialMedicao);

        if (listaHistoricoMedicao == null || listaHistoricoMedicao.size() < 2) {
            return Optional.empty();
        } else {
            return Optional.of(listaHistoricoMedicao.get(listaHistoricoMedicao.size() - 2));
        }

    }

}
