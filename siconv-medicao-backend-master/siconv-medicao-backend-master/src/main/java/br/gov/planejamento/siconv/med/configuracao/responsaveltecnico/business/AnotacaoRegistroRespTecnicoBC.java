package br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.business;

import static com.google.common.collect.Sets.difference;
import static java.util.Collections.emptyList;
import static java.util.Collections.sort;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.groups.ConvertGroup;

import org.jdbi.v3.core.Jdbi;

import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao.AnotacaoRegistroRespTecnicoDAO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao.AnotacaoRegistroRtSubmetaDAO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.dao.ContratoResponsavelTecnicoDAO;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.database.AnotacaoRegistroRtSubmetaBD;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.database.ContratoResponsavelTecnicoBD;
import br.gov.planejamento.siconv.med.configuracao.responsaveltecnico.entity.dto.AnotacaoRegistroRespTecnicoDTO;
import br.gov.planejamento.siconv.med.contrato.dao.ContratoDAO;
import br.gov.planejamento.siconv.med.contrato.entity.ContratoBD;
import br.gov.planejamento.siconv.med.infra.exception.MedicaoRestException;
import br.gov.planejamento.siconv.med.infra.message.MessageKey;
import br.gov.planejamento.siconv.med.infra.validation.InsertGroup;
import br.gov.planejamento.siconv.med.infra.validation.UpdateGroup;
import br.gov.planejamento.siconv.med.integration.ceph.CephActions;
import br.gov.planejamento.siconv.med.integration.dto.UsuarioDTO;
import br.gov.planejamento.siconv.med.medicao.business.SubmetaBC;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;

@ApplicationScoped
public class AnotacaoRegistroRespTecnicoBC {

    @Inject
    private Jdbi jdbi;
    
    @Inject
    private CephActions cephActions;

    @Inject
    private ResponsavelTecnicoBC responsavelTecnicoBC;
    
    @Inject
    private SubmetaBC submetaBC;

    public List<AnotacaoRegistroRespTecnicoDTO> listarAnotacoes(Long idContratoSiconv) {

        List<AnotacaoRegistroRespTecnicoDTO> lista = emptyList();

        ContratoBD contratoMedicao = getContratoDAO().consultarContratoPorContratoFK(idContratoSiconv);

        if (contratoMedicao != null) {
            Map<Long, SubmetaVrplDTO> submetasContrato = consultarSubmetasContrato(contratoMedicao);
            lista = getAnotacaoDAO().listarAnotacaoRegistroRT(contratoMedicao.getId());
            lista.forEach(art -> decorateAnotacaoDTO(art, contratoMedicao, submetasContrato));
        }

        return lista;
    }

    private AnotacaoRegistroRespTecnicoDTO decorateAnotacaoDTO(AnotacaoRegistroRespTecnicoDTO art,
            ContratoBD contratoMedicao, Map<Long, SubmetaVrplDTO> submetasContrato) {

        UsuarioDTO usuario = responsavelTecnicoBC.consultarUsuario(art.getResponsavelTecnico().getCpf(), art.getTipo(),
                contratoMedicao.getContratoFk(), false);

        if (usuario != null) {
            art.getResponsavelTecnico().setNome(usuario.getNome());
        }

        art.getSubmetas().replaceAll(submeta -> submetasContrato.getOrDefault(submeta.getId(), submeta));
        sort(art.getSubmetas(), SubmetaVrplDTO.ORDENACAO_PADRAO);

        art.setUrl(cephActions.getPresignedUrl(art.getCoCeph()));

        art.setPossuiSubmetaAssinada(existeSubmetaAssinadaPeloResponsavelAnotacao(art));

        art.setIdContratoSiconv(contratoMedicao.getContratoFk());

        return art;
    }

    public AnotacaoRegistroRespTecnicoDTO consultarAnotacao(Long idAnotacao) {

        AnotacaoRegistroRespTecnicoDTO art = consultarAnotacaoDTO(idAnotacao);

        ContratoResponsavelTecnicoBD crt = consultarContratoResponsavelTecnico(art);

        ContratoBD contratoMedicao = getContratoDAO().consultarContrato(crt.getContrato());
        Map<Long, SubmetaVrplDTO> submetasContrato = consultarSubmetasContrato(contratoMedicao);

        return decorateAnotacaoDTO(art, contratoMedicao, submetasContrato);
    }

    public Long incluirAnotacao(Long idContratoSiconv,
            @Valid @ConvertGroup(to = InsertGroup.class) AnotacaoRegistroRespTecnicoDTO anotacaoInput) {

        validarInclusao(idContratoSiconv, anotacaoInput);

        return jdbi.inTransaction(handle -> {

            AnotacaoRegistroRespTecnicoDAO anotacaoDAO = handle.attach(AnotacaoRegistroRespTecnicoDAO.class);
            AnotacaoRegistroRtSubmetaDAO anotacaoSubmetaDAO = handle.attach(AnotacaoRegistroRtSubmetaDAO.class);

            anotacaoInput.setCoCeph(cephActions.uploadFile(anotacaoInput.getArquivo(), anotacaoInput.getNmArquivo()));

            Long idAnotacaoInserida = anotacaoDAO.inserirAnotacao(anotacaoInput.converterParaBD());

            Set<Long> idSubmetasAnotacaoInput = anotacaoInput.getIdSubmetas();

            anotacaoSubmetaDAO.inserirAnotacaoRegistoSubmeta(idSubmetasAnotacaoInput.stream()
                    .map(idSubmeta -> new AnotacaoRegistroRtSubmetaBD(idSubmeta, idAnotacaoInserida))
                    .collect(toList()));

            return idAnotacaoInserida;
        });
    }

    private void validarInclusao(Long idContratoSiconv, AnotacaoRegistroRespTecnicoDTO anotacaoInput) {
    	
        ContratoResponsavelTecnicoBD crtInput = consultarContratoResponsavelTecnico(anotacaoInput);

        ContratoBD contratoMedicao = getContratoDAO().consultarContrato(crtInput.getContrato());
        
    	validarSeContratoDiferenteDeSocial(contratoMedicao);

        if (!contratoMedicao.getContratoFk().equals(idContratoSiconv)) {
            throw new MedicaoRestException(MessageKey.ERRO_CTEF_ARRT_NAO_VINCULADO_AO_CONTRATO);
        }

        validarSubmetasAnotacaoVinculadasContrato(anotacaoInput, contratoMedicao);

        // Verifica se o Tipo do RT da Anotação é o mesmo tipo do RT associado
        if (!anotacaoInput.getTipo().equals(crtInput.getTipo())) {
        	throw new MedicaoRestException(MessageKey.ERRO_ART_TIPO_DIFERENTE_RT_ASSOCIADO);
        }
    }

	private void validarSeContratoDiferenteDeSocial(ContratoBD contratoMedicao) {
		if (contratoMedicao.isInSocial()) {
			throw new MedicaoRestException(MessageKey.ERRO_EDITAR_RT_ART_RRT_CONTRATO_SOCIAL);
		}
	}
   
    private void validarSubmetasAnotacaoVinculadasContrato(AnotacaoRegistroRespTecnicoDTO art,
            ContratoBD contratoMedicao) {

        Set<Long> idSubmetasContrato = consultarSubmetasContrato(contratoMedicao).keySet();
        Set<Long> idSubmetasAnotacao = art.getIdSubmetas();

        if (!idSubmetasContrato.containsAll(idSubmetasAnotacao)) {
            throw new MedicaoRestException(MessageKey.ERRO_ART_RRT_SUBMETA_NAO_VINCULADA_CONTRATO);
        }
    }

    public void alterarAnotacao(
            @Valid @ConvertGroup(to = UpdateGroup.class) AnotacaoRegistroRespTecnicoDTO anotacaoInput) {

        AnotacaoRegistroRespTecnicoDTO anotacaoBase = consultarAnotacaoDTO(anotacaoInput.getId());

        validarAlteracao(anotacaoInput, anotacaoBase);

        jdbi.useTransaction(handle -> {

            AnotacaoRegistroRespTecnicoDAO anotacaoDAO = handle.attach(AnotacaoRegistroRespTecnicoDAO.class);
            AnotacaoRegistroRtSubmetaDAO anotacaoSubmetaDAO = handle.attach(AnotacaoRegistroRtSubmetaDAO.class);

            if (!isEmpty(anotacaoInput.getNmArquivo())) {
                anotacaoInput
                        .setCoCeph(cephActions.uploadFile(anotacaoInput.getArquivo(), anotacaoInput.getNmArquivo()));

            } else {
                anotacaoInput.setNmArquivo(anotacaoBase.getNmArquivo());
                anotacaoInput.setCoCeph(anotacaoBase.getCoCeph());
            }

            Set<Long> idSubmetasAnotacaoInput = anotacaoInput.getIdSubmetas();
            Set<Long> idSubmetasAnotacaoBase = anotacaoBase.getIdSubmetas();

            anotacaoSubmetaDAO.inserirAnotacaoRegistoSubmeta(difference(idSubmetasAnotacaoInput, idSubmetasAnotacaoBase)
                    .stream().map(idSubmeta -> new AnotacaoRegistroRtSubmetaBD(idSubmeta, anotacaoInput.getId()))
                    .collect(toList()));

            anotacaoSubmetaDAO.deletar(difference(idSubmetasAnotacaoBase, idSubmetasAnotacaoInput).stream()
                    .map(idSubmeta -> new AnotacaoRegistroRtSubmetaBD(idSubmeta, anotacaoBase.getId()))
                    .collect(toList()));
            
            anotacaoDAO.alterar(anotacaoInput.converterParaBD());
        });
    }

    private void validarAlteracao(AnotacaoRegistroRespTecnicoDTO anotacaoInput,
            AnotacaoRegistroRespTecnicoDTO anotacaoBase) {
    	
        if(anotacaoBase.getDataInativacao() != null ) {
        	throw new MedicaoRestException(MessageKey.ERRO_EDITAR_ART_RRT_INATIVO);
        }
        
    	if (existeSubmetaAssinadaPeloResponsavelAnotacao(anotacaoBase)) {
            throw new MedicaoRestException(MessageKey.ERRO_EDITAR_ART_RRT_SUBMETA_ASSINADA);
        }

        ContratoResponsavelTecnicoBD crtInput = consultarContratoResponsavelTecnico(anotacaoInput);
        ContratoResponsavelTecnicoBD crtBase = consultarContratoResponsavelTecnico(anotacaoBase);

        if (!crtBase.getContrato().equals(crtInput.getContrato())) {
            throw new MedicaoRestException(MessageKey.ERRO_EDITAR_ART_RRT_CONTRATO_DIFERENTE_ANTERIOR);
        }

        ContratoBD contratoMedicao = getContratoDAO().consultarContrato(crtInput.getContrato());
        validarSubmetasAnotacaoVinculadasContrato(anotacaoInput, contratoMedicao);
        
    	validarSeContratoDiferenteDeSocial(contratoMedicao);
        
        // Verifica se o Tipo do RT da Anotação é o mesmo tipo do RT associado
        if (!anotacaoInput.getTipo().equals(crtInput.getTipo())) {
        	throw new MedicaoRestException(MessageKey.ERRO_ART_TIPO_DIFERENTE_RT_ASSOCIADO);
        }
    }

    public void excluirAnotacao(Long idAnotacao) {

        AnotacaoRegistroRespTecnicoDTO art = consultarAnotacaoDTO(idAnotacao);

        validarExclusao(art);

        jdbi.useTransaction(handle -> {

            AnotacaoRegistroRespTecnicoDAO anotacaoDAO = handle.attach(AnotacaoRegistroRespTecnicoDAO.class);
            AnotacaoRegistroRtSubmetaDAO anotacaoSubmetaDAO = handle.attach(AnotacaoRegistroRtSubmetaDAO.class);

            anotacaoSubmetaDAO.deletarPorIdAnotacao(art.getId());
            anotacaoDAO.deletar(art.getId());
        });
    }

    private void validarExclusao(AnotacaoRegistroRespTecnicoDTO art) {

        if (existeSubmetaAssinadaPeloResponsavelAnotacao(art)) {
            throw new MedicaoRestException(MessageKey.ERRO_EXCLUIR_ART_RRT_SUBMETA_ASSINADA);
        }
    }

    public void inativarAnotacao(Long idAnotacao) {

        AnotacaoRegistroRespTecnicoDTO art = consultarAnotacaoDTO(idAnotacao);

        validarInativacao(art);

        getAnotacaoDAO().inativar(art.converterParaBD());
    }

    private void validarInativacao(AnotacaoRegistroRespTecnicoDTO art) {

        if (art.getDataInativacao() != null) {
            throw new MedicaoRestException(MessageKey.ERRO_INATIVAR_ART_RRT_JA_INATIVO);
        }
    }

    private boolean existeSubmetaAssinadaPeloResponsavelAnotacao(AnotacaoRegistroRespTecnicoDTO art) {
        return getAnotacaoDAO().existeSubmetaAssinadaPeloResponsavelAnotacao(art.getId());
    }

    private AnotacaoRegistroRespTecnicoDTO consultarAnotacaoDTO(Long idAnotacao) {
        return getAnotacaoDAO().consultarAnotacaoDTO(idAnotacao)
                .orElseThrow(() -> new MedicaoRestException(MessageKey.ERRO_ART_RRT_INEXISTENTE));
    }

    private ContratoResponsavelTecnicoBD consultarContratoResponsavelTecnico(AnotacaoRegistroRespTecnicoDTO art) {
        return getContratoResponsavelTecnicoDAO().consultar(art.getIdMedContratoRespTec())
                .orElseThrow(() -> new MedicaoRestException(MessageKey.CONTRATO_RESP_TEC_INEXISTENTE));
    }

    private Map<Long, SubmetaVrplDTO> consultarSubmetasContrato(ContratoBD contratoMedicao) {
        return submetaBC.listarSubmetasPorContrato(contratoMedicao.getContratoFk()).stream()
                .collect(toMap(SubmetaVrplDTO::getId, Function.identity()));
    }

    private AnotacaoRegistroRespTecnicoDAO getAnotacaoDAO() {
        return jdbi.onDemand(AnotacaoRegistroRespTecnicoDAO.class);
    }

    private ContratoDAO getContratoDAO() {
        return jdbi.onDemand(ContratoDAO.class);
    }

    private ContratoResponsavelTecnicoDAO getContratoResponsavelTecnicoDAO() {
        return jdbi.onDemand(ContratoResponsavelTecnicoDAO.class);
    }
    
    public boolean existeAnotacaoComRTAtiva(Long idContrato, String tipo) {
        return getAnotacaoDAO().existeAnotacaoComRTAtiva(idContrato, tipo);
    }
}