package br.gov.planejamento.siconv.med.medicao.dao;

import java.util.List;

import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.statement.UseRowReducer;

import br.gov.planejamento.siconv.med.medicao.dao.reducer.SubmetaMedicaoBMReducer;
import br.gov.planejamento.siconv.med.medicao.dao.reducer.SubmetaMedicaoReducer;
import br.gov.planejamento.siconv.med.medicao.entity.database.SubmetaMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.dto.EventoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.FrenteObraVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ResponsavelTecnicoFiscalizacaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO.Assinatura;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO.Responsavel;

public interface SubmetaDAO {

	@SqlQuery("select med_item_medicao.vrpl_submeta_fk as subm_id, "
			+ "med_submeta_medicao.versao as subm_versao, "
			+ "med_submeta_medicao.in_situacao_empresa as sit_in_situacao_emp, "  
			+ "med_submeta_medicao.nr_cpf_resp_empresa as resp_nr_cpf_emp, "  
			+ "med_submeta_medicao.dt_assinatura_empresa as assin_data_emp,  "  
			+ "med_submeta_medicao.in_situacao_convenente as sit_in_situacao_conv, "  
			+ "med_submeta_medicao.nr_cpf_resp_convenente as resp_nr_cpf_conv, "  
			+ "med_submeta_medicao.dt_assinatura_convenente as assin_data_conv, " 
			+ "med_submeta_medicao.in_situacao_concedente as sit_in_situacao_conc, "  
			+ "med_submeta_medicao.nr_cpf_resp_concedente as resp_nr_cpf_conc, "  
			+ "med_submeta_medicao.dt_assinatura_concedente as assin_data_conc, "
			+ "med_submeta_medicao.in_perfil_resp_concedente as resp_perfil_conc, "
			+ "med_item_medicao.vrpl_frente_obra_fk as fo_id, "
			+ "med_item_medicao.vrpl_evento_fk as even_id, "
			+ "med_item_medicao.medicao_fk_empresa as even_id_medicao_empresa, "
			+ "med_item_medicao.medicao_fk_convenente as even_id_medicao_convenente, "
			+ "med_item_medicao.medicao_fk_concedente as even_id_medicao_concedente, "
			+ "med_item_medicao.vl_total_servicos as even_valor " 
			+ "from med_item_medicao "
			+ "join med_contrato on (med_contrato.id = med_item_medicao.med_contrato_fk) "
			+ "left join med_medicao on (med_contrato.id = med_medicao.med_contrato_fk) "
			+ "left join med_submeta_medicao on (med_medicao.id = med_submeta_medicao.medicao_fk "
			+ "and med_item_medicao.vrpl_submeta_fk = med_submeta_medicao.vrpl_submeta_fk) "
			+ "where med_medicao.med_contrato_fk =  Cast(:idContratoMedicao as int8) and "
			+ "med_medicao.id = Cast(:idMedicao as int8) " 
			+ "order by subm_id, fo_id, even_id ")
	@RegisterFieldMapper(value = SubmetaMedicaoDTO.class, prefix = "subm")
	@RegisterFieldMapper(value = Assinatura.class, prefix = "assin")
	@RegisterFieldMapper(value = Responsavel.class, prefix = "resp")
	@RegisterFieldMapper(value = FrenteObraVrplDTO.class, prefix = "fo")
	@RegisterFieldMapper(value = EventoVrplDTO.class, prefix = "even")
	@UseRowReducer(SubmetaMedicaoReducer.class)
	List<SubmetaMedicaoDTO> listarSubmetasMedicao(@Bind("idContratoMedicao") Long idContratoMedicao,
			@Bind("idMedicao") Long idMedicao);
	
	@SqlQuery(" select med_item_medicao_bm.vrpl_submeta_fk as subm_id," + 
			" med_submeta_medicao.versao as subm_versao," + 
			" med_submeta_medicao.in_situacao_empresa as sit_in_situacao_emp, " + 
			" med_submeta_medicao.nr_cpf_resp_empresa as resp_nr_cpf_emp, " + 
			" med_submeta_medicao.dt_assinatura_empresa as assin_data_emp, " + 
			" med_submeta_medicao.in_situacao_convenente as sit_in_situacao_conv, " + 
			" med_submeta_medicao.nr_cpf_resp_convenente as resp_nr_cpf_conv, " + 
			" med_submeta_medicao.dt_assinatura_convenente as assin_data_conv," + 
			" med_submeta_medicao.in_situacao_concedente as sit_in_situacao_conc,  " + 
			" med_submeta_medicao.nr_cpf_resp_concedente as resp_nr_cpf_conc,  " + 
			" med_submeta_medicao.dt_assinatura_concedente as assin_data_conc," + 
			" med_submeta_medicao.in_perfil_resp_concedente as resp_perfil_conc," +  
			" med_item_medicao_bm.vrpl_frente_obra_fk as fo_id," + 
			" med_item_medicao_bm.vrpl_servico_fk as serv_id," + 
			" med_item_medicao_bm.qt_total_servico as serv_qtd," + 
			" med_item_medicao_bm.vl_preco_unitario_licitado as serv_preco, " +
			" med_item_medicao_bm_vl.med_medicao_fk as svl_med_id, " +
			" med_item_medicao_bm_vl.qt_empresa as svl_qtd_empresa," + 
			" med_item_medicao_bm_vl.qt_convenente as svl_qtd_convenente, " + 
			" med_item_medicao_bm_vl.qt_concedente as svl_qtd_concedente " + 
			" FROM med_item_medicao_bm " +
			" LEFT JOIN med_submeta_medicao ON ( med_item_medicao_bm.vrpl_submeta_fk = med_submeta_medicao.vrpl_submeta_fk" +
			"                                    AND med_submeta_medicao.medicao_fk = :idMedicao )" +
			" LEFT JOIN med_item_medicao_bm_vl ON (med_item_medicao_bm.id = med_item_medicao_bm_vl.med_item_medicao_bm_fk)" +
			" WHERE med_item_medicao_bm.med_contrato_fk = :idContratoMedicao" +
			" order by subm_id, fo_id, serv_id ")
	@RegisterFieldMapper(value = SubmetaMedicaoDTO.class, prefix = "subm")
	@RegisterFieldMapper(value = Assinatura.class, prefix = "assin")
	@RegisterFieldMapper(value = Responsavel.class, prefix = "resp")
	@RegisterFieldMapper(value = FrenteObraVrplDTO.class, prefix = "fo")
	@RegisterFieldMapper(value = ServicoVrplDTO.class, prefix = "serv")
	@UseRowReducer(SubmetaMedicaoBMReducer.class)
	List<SubmetaMedicaoDTO> listarSubmetasMedicaoBM(@Bind("idContratoMedicao") Long idContratoMedicao,
			@Bind("idMedicao") Long idMedicao);

	// Consulta do Módulo Medição: Consulta SubMeta Medição
	@SqlQuery("select * from med_submeta_medicao"
			+ " where med_submeta_medicao.vrpl_submeta_fk = Cast(:idSubmetaVrpl as int8)"
			+ " and med_submeta_medicao.medicao_fk = Cast(:idMedicao as int8)")
	@RegisterFieldMapper(SubmetaMedicaoBD.class)
	SubmetaMedicaoBD consultarSubmetaMedicao(@Bind("idMedicao") Long idMedicao,
			@Bind("idSubmetaVrpl") Long idSubmetaVrpl);

	// Consulta submeta por Id Medicao
	@SqlQuery("select * from med_submeta_medicao"			
			+ " where med_submeta_medicao.medicao_fk = Cast(:idMedicao as int8)")
	@RegisterFieldMapper(SubmetaMedicaoBD.class)
	List<SubmetaMedicaoBD> buscarListaSubmetasporMedicao(@Bind("idMedicao") Long idMedicao);

	@SqlQuery(" SELECT sub.*"
			+ " FROM med_submeta_medicao sub"
			+ "   JOIN med_medicao med ON med.id = sub.medicao_fk"
			+ " WHERE med.medicao_fk_agrupadora = :idMedicaoAgrupadora")
	@RegisterFieldMapper(SubmetaMedicaoBD.class)
	List<SubmetaMedicaoBD> listarSubmetasMedicoesAcumuladas(@Bind("idMedicaoAgrupadora") Long idMedicaoAgrupadora);

	// Insere Submeta
	@SqlUpdate("INSERT INTO siconv.med_submeta_medicao "
			+ "(in_situacao_empresa, nr_cpf_resp_empresa, dt_assinatura_empresa, vrpl_submeta_fk, medicao_fk, versao, adt_login, adt_data_hora, adt_operacao)"
			+ " VALUES (:situacaoEmpresa, :nrCpfResponsavelAssinaturaEmpresa, :dtAssinaturaEmpresa, :idSubmetaVrpl, :idMedicao, 1, current_setting('med.cpf_usuario'), LOCALTIMESTAMP, 'INSERT')")
	@RegisterFieldMapper(SubmetaMedicaoBD.class)
	@GetGeneratedKeys
	Long inserir(@BindBean SubmetaMedicaoBD submeta);
	
	// Insere Submeta Convenente
	@SqlUpdate("INSERT INTO siconv.med_submeta_medicao "
			+ "(in_situacao_convenente, nr_cpf_resp_convenente, dt_assinatura_convenente, vrpl_submeta_fk, medicao_fk, versao, adt_login, adt_data_hora, adt_operacao)"
			+ " VALUES (:situacaoConvenente, :nrCpfResponsavelAssinaturaConvenente, :dtAssinaturaConvenente, :idSubmetaVrpl, :idMedicao, 1, current_setting('med.cpf_usuario'), LOCALTIMESTAMP, 'INSERT')")
	@RegisterFieldMapper(SubmetaMedicaoBD.class)
	@GetGeneratedKeys
	Long inserirSubmetaConvenente(@BindBean SubmetaMedicaoBD submeta);

	
	// Insere Submeta Concedente Mandatária
	@SqlUpdate("INSERT INTO siconv.med_submeta_medicao "
			+ "(in_situacao_concedente, nr_cpf_resp_concedente, dt_assinatura_concedente, in_perfil_resp_concedente, vrpl_submeta_fk, medicao_fk, versao, adt_login, adt_data_hora, adt_operacao)"
			+ " VALUES (:situacaoConcedente, :nrCpfResponsavelAssinaturaConcedente, :dtAssinaturaConcedente, :inPerfilRespConcedente, :idSubmetaVrpl, :idMedicao, 1, current_setting('med.cpf_usuario'), LOCALTIMESTAMP, 'INSERT')")
	@RegisterFieldMapper(SubmetaMedicaoBD.class)
	@GetGeneratedKeys
	Long inserirSubmetaConcedenteMandataria(@BindBean SubmetaMedicaoBD submeta);
	
	
	// Atualiza Submeta da Empresa
	@SqlUpdate("UPDATE siconv.med_submeta_medicao"
			+ " SET in_situacao_empresa = :situacaoEmpresa, nr_cpf_resp_empresa = :nrCpfResponsavelAssinaturaEmpresa, dt_assinatura_empresa = :dtAssinaturaEmpresa, versao = :versao + 1, adt_login = current_setting('med.cpf_usuario'), adt_data_hora = LOCALTIMESTAMP, adt_operacao = 'UPDATE'" 
			+ " WHERE medicao_fk = :idMedicao AND vrpl_submeta_fk = :idSubmetaVrpl")
	@RegisterFieldMapper(SubmetaMedicaoBD.class)
	@GetGeneratedKeys
	Long atualizarAssinaturaEmpresa(@BindBean SubmetaMedicaoBD submeta);

	// Atualiza assinatura da Submeta para o perfil Convenente
	@SqlUpdate("UPDATE siconv.med_submeta_medicao "
			+ " SET in_situacao_convenente = :situacaoConvenente, nr_cpf_resp_convenente = :nrCpfResponsavelAssinaturaConvenente, dt_assinatura_convenente = :dtAssinaturaConvenente, versao = :versao + 1, adt_login = current_setting('med.cpf_usuario'), adt_data_hora = LOCALTIMESTAMP, adt_operacao = 'UPDATE' " 
			+ " WHERE medicao_fk = :idMedicao AND vrpl_submeta_fk = :idSubmetaVrpl")
	@RegisterFieldMapper(SubmetaMedicaoBD.class)
	@GetGeneratedKeys
	Long atualizarAssinaturaConvenente(@BindBean SubmetaMedicaoBD submeta);
	
	// Atualiza assinatura da Submeta para o perfil Concedente
	@SqlUpdate("UPDATE siconv.med_submeta_medicao "
			+ " SET in_situacao_concedente = :situacaoConcedente, nr_cpf_resp_concedente = :nrCpfResponsavelAssinaturaConcedente, dt_assinatura_concedente = :dtAssinaturaConcedente, in_perfil_resp_concedente = :inPerfilRespConcedente, versao = :versao + 1, adt_login = current_setting('med.cpf_usuario'), adt_data_hora = LOCALTIMESTAMP, adt_operacao = 'UPDATE' " 
			+ " WHERE medicao_fk = :idMedicao AND vrpl_submeta_fk = :idSubmetaVrpl")
	@RegisterFieldMapper(SubmetaMedicaoBD.class)
	@GetGeneratedKeys
	Long atualizarAssinaturaConcedente(@BindBean SubmetaMedicaoBD submeta);

	
	// Verifica se o usuário logado pode assinar a submeta
	@SqlQuery("SELECT count(art_rt.id) > 0 " + 
			" FROM " + 
			"   siconv.med_contrato contrato, " + 
			"   siconv.med_contrato_resp_tecnico contrato_rt, " + 
			"   siconv.med_registro_profissional reg_prof, " + 
			"   siconv.med_responsavel_tecnico rt, " + 
			"   siconv.med_anotacao_registro_rt art_rt, " + 
			"   siconv.med_anotacao_registro_rt_submeta art_rt_submeta " + 
			" WHERE " + 
			"   contrato.contrato_fk = :idContrato and " + 
			"   contrato_rt.med_contrato_fk = contrato.id and " + 
			"   contrato_rt.med_registro_profissional_fk = reg_prof.id and " + 
			"   reg_prof.med_responsavel_tecnico_fk = rt.id and " + 
			"   rt.nr_cpf = :nrCpfUsuario and " + 
			"   contrato_rt.in_tipo = art_rt.in_tipo and " +
			"   art_rt.med_contrato_resp_tecnico_fk = contrato_rt.id and " + 
			"   art_rt_submeta.med_anotacao_registro_rt_fk = art_rt.id and " + 
			"   art_rt.dt_inativacao is null and " + 
			"   art_rt_submeta.vrpl_submeta_fk = :idSubmetaVrpl and " + 
			"   contrato_rt.in_tipo = :tipo ")
	Boolean isSubmetaContratoArqEngAssinavelPeloCpf(@Bind("idContrato") Long idContrato, @Bind("idSubmetaVrpl") Long idSubmetaVrpl,
			@Bind("nrCpfUsuario") String nrCpfUsuari, @Bind("tipo") String tipo);

	 @SqlQuery("SELECT respTec.id AS idResponsavelTecnico, contrRespTec.in_tipo AS tipo, respTec.nr_cpf AS nrCpfResponsavelTecnico,"
	 		+ " regProf.crea_cau AS cdResponsavelTecnico, regProf.uf AS uf "
	 		+ " FROM siconv.med_anotacao_registro_rt anotacaoRt,"
	        + " siconv.med_contrato_resp_tecnico contrRespTec,"
			+ " siconv.med_registro_profissional regProf,"
	        + " siconv.med_responsavel_tecnico respTec,"
	        + " siconv.med_anotacao_registro_rt_submeta submetaRt"				
			+ " WHERE submetaRt.med_anotacao_registro_rt_fk = anotacaoRt.id"
			+ " AND anotacaoRt.med_contrato_resp_tecnico_fk = contrRespTec.id"
	        + " AND contrRespTec.med_registro_profissional_fk = regProf.id"
			+ " AND regProf.med_responsavel_tecnico_fk = respTec.id"
	        + " AND respTec.nr_cpf = :idResponsavelTecnico"
	        + " AND submetaRt.vrpl_submeta_fk = :idSubmetaVrpl")
	@RegisterFieldMapper(ResponsavelTecnicoFiscalizacaoDTO.class)
	ResponsavelTecnicoFiscalizacaoDTO consultarDadosResponsavelTecnicoArqEng(@Bind("idResponsavelTecnico") String idResponsavelTecnico, @Bind("idSubmetaVrpl") Long idSubmetaVrpl);
		
		
	@SqlUpdate("DELETE FROM siconv.med_submeta_medicao WHERE medicao_fk = :idMedicao AND vrpl_submeta_fk = :idSubmetaVrpl")
	void excluirSubmetaMedicao(@Bind("idMedicao") Long idMedicao, @Bind("idSubmetaVrpl") Long idSubmetaVrpl);

	@SqlUpdate("UPDATE siconv.med_submeta_medicao " +
			" SET in_situacao_convenente = null, nr_cpf_resp_convenente = null, dt_assinatura_convenente = null, versao = :versao + 1, adt_login = current_setting('med.cpf_usuario'), adt_data_hora = LOCALTIMESTAMP, adt_operacao = 'UPDATE'" + 
			" WHERE medicao_fk = :idMedicao AND vrpl_submeta_fk = :idSubmetaVrpl")
	void limparSubmetaMedicaoConvenente(@BindBean SubmetaMedicaoBD submeta);
	
	@SqlUpdate("UPDATE siconv.med_submeta_medicao " +
			" SET in_situacao_concedente = null, nr_cpf_resp_concedente = null, dt_assinatura_concedente = null, in_perfil_resp_concedente = null, versao = :versao + 1, adt_login = current_setting('med.cpf_usuario'), adt_data_hora = LOCALTIMESTAMP, adt_operacao = 'UPDATE'" + 
			" WHERE medicao_fk = :idMedicao AND vrpl_submeta_fk = :idSubmetaVrpl")
	void limparSubmetaMedicaoConcedente(@BindBean SubmetaMedicaoBD submeta);	
	
	@SqlQuery("select count(item.id) > 0 " 
			+ "from med_item_medicao item "
			+ "join med_contrato cont on (cont.id = item.med_contrato_fk) "
			+ "where cont.contrato_fk = :contratoFk and medicao_fk_empresa is null")
	Boolean isContratoComSubmetaAExecutar(@Bind("contratoFk") Long contratoFk);
	
	@SqlQuery("SELECT COUNT(item.id) > 0 " 
			+ "FROM med_item_medicao_bm item "
			+ "JOIN med_contrato cont ON (cont.id = item.med_contrato_fk) "
			+ "WHERE cont.contrato_fk = :contratoFk "
			+ "AND item.qt_total_servico > (SELECT COALESCE(SUM(item_vl.qt_empresa),0) "
			+ "                             FROM med_item_medicao_bm_vl item_vl "
			+ "                             WHERE item_vl.med_item_medicao_bm_fk = item.id)")
	Boolean isContratoBMComSubmetaAExecutar(@Bind("contratoFk") Long contratoFk);
	
	@SqlQuery("SELECT count(rtsocial.id) > 0 "
			+ "FROM "
			+ "siconv.med_contrato contrato"
			+ " JOIN siconv.med_contrato_resp_tecnico_social as rtsocial ON contrato.id = rtsocial.med_contrato_fK"
			+ " JOIN med_responsavel_tecnico rt ON rtsocial.med_responsavel_tecnico_fk = rt.id "
			+ " JOIN med_contrato_rt_social_submeta submetaRtSocial ON rtsocial.id = submetaRtSocial.med_contrato_resp_tecnico_social_fk "
			+ " WHERE contrato.contrato_fk = :idContrato"
			+ "        AND rtsocial.dt_inativacao is null"
			+ "        AND submetaRtSocial.vrpl_submeta_fk = :idSubmetaVrpl"
			+ "        AND rt.nr_cpf = :nrCpfUsuario"
			+ "        AND rtsocial.in_tipo = :tipo ")
	Boolean isSubmetaContratoSocialAssinavelPeloCpf(@Bind("idContrato") Long idContrato, @Bind("idSubmetaVrpl") Long idSubmetaVrpl,
			@Bind("nrCpfUsuario") String nrCpfUsuario, @Bind("tipo") String tipo);
	
	@SqlQuery("SELECT respTec.id AS idResponsavelTecnico,"
			+ " respTec.nr_cpf AS nrCpfResponsavelTecnico,"
			+ " contRtSocial.in_tipo AS tipo"
			+ " FROM siconv.med_responsavel_tecnico respTec, "
			+ " siconv.med_contrato_resp_tecnico_social contRtSocial, "
			+ " siconv.med_contrato_rt_social_submeta subSocial"
			+ " WHERE"
			+ " respTec.id = contRtSocial.med_responsavel_tecnico_fk "
			+ " AND contRtSocial.id = subSocial.med_contrato_resp_tecnico_social_fk "
			+ " AND respTec.nr_cpf = :cpfResponsavelTecnicoSubmeta "
			+ " AND subSocial.vrpl_submeta_fk = :idSubmetaVrpl")
	@RegisterFieldMapper(ResponsavelTecnicoFiscalizacaoDTO.class)
	ResponsavelTecnicoFiscalizacaoDTO consultarDadosResponsavelTecnicoSocial(@Bind("cpfResponsavelTecnicoSubmeta") String cpfResponsavelTecnicoSubmeta,
			 @Bind("idSubmetaVrpl") Long idSubmetaVrpl);
	
	@SqlUpdate("DELETE FROM siconv.med_submeta_medicao WHERE medicao_fk = :idMedicao")
	void excluirSubmetaPorIdMedicao(@Bind("idMedicao") Long idMedicao);
	
    @SqlQuery(" SELECT sub.*"
    		+ " FROM siconv.med_submeta_medicao sub"
    		+ "   JOIN siconv.med_medicao med ON med.id = sub.medicao_fk"
    		+ " WHERE med.med_contrato_fk = :idContratoMedicao")
    @RegisterFieldMapper(SubmetaMedicaoBD.class)
    List<SubmetaMedicaoBD> consultarSubmetasMedicaoPorContrato(@Bind("idContratoMedicao") Long idContratoMedicao);
}