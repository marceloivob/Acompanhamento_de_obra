package br.gov.planejamento.siconv.med.medicao.dao;

import java.util.List;
import java.util.Optional;

import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindFields;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import br.gov.planejamento.siconv.med.medicao.entity.database.ItemMedicaoBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.ItemMedicaoBMBD;
import br.gov.planejamento.siconv.med.medicao.entity.database.ItemMedicaoBMValorBD;

public interface ItemMedicaoDAO {

	// Consulta Item Medição
	@SqlQuery("select * from med_item_medicao" + 					 
				" where med_item_medicao.vrpl_evento_fk = Cast(:idVrplEvento as int8)" +  
				" and med_item_medicao.vrpl_frente_obra_fk = Cast(:idVrplFrenteObra as int8)" +
				" and med_item_medicao.vrpl_submeta_fk = Cast(:idVrplSubmeta as int8)")
	@RegisterFieldMapper(ItemMedicaoBD.class)
	ItemMedicaoBD consultarItemMedicao(@Bind("idVrplEvento") Long idVrplEvento,
			@Bind("idVrplFrenteObra") Long idVrplFrenteObra, @Bind("idVrplSubmeta") Long idVrplSubmeta); 	

	@SqlQuery("select * from med_item_medicao_bm" + 					 
				" where med_item_medicao_bm.vrpl_servico_fk = :idVrplServico" +  
				" and med_item_medicao_bm.vrpl_frente_obra_fk = :idVrplFrenteObra" +
				" and med_item_medicao_bm.vrpl_submeta_fk = :idVrplSubmeta")
	@RegisterFieldMapper(ItemMedicaoBMBD.class)
	Optional<ItemMedicaoBMBD> consultarItemMedicaoBM(@Bind("idVrplServico") Long idVrplServico,
			@Bind("idVrplFrenteObra") Long idVrplFrenteObra, @Bind("idVrplSubmeta") Long idVrplSubmeta);

	@SqlQuery("SELECT * FROM med_item_medicao_bm"
			+ " WHERE med_item_medicao_bm.vrpl_submeta_fk = :idVrplSubmeta"
			+ "   AND med_contrato_fk = :idContratoMedicao")
	@RegisterFieldMapper(ItemMedicaoBMBD.class)
	List<ItemMedicaoBMBD> listarItemMedicaoBM(@Bind("idContratoMedicao") Long idContratoMedicao,
			@Bind("idVrplSubmeta") Long idVrplSubmeta);

	@SqlQuery("select * from med_item_medicao_bm_vl"
			+ " where med_item_medicao_bm_vl.med_item_medicao_bm_fk = :idItemMedicaoBM"
			+ " and med_item_medicao_bm_vl.med_medicao_fk = :idMedicao")
	@RegisterFieldMapper(ItemMedicaoBMValorBD.class)
	Optional<ItemMedicaoBMValorBD> consultarItemMedicaoBMValor(@Bind("idItemMedicaoBM") Long idItemMedicaoBM,
			@Bind("idMedicao") Long idMedicao);

	@SqlQuery("SELECT med_item_medicao_bm_vl.* FROM med_item_medicao_bm_vl"
			+ " INNER JOIN med_item_medicao_bm ON med_item_medicao_bm_vl.med_item_medicao_bm_fk = med_item_medicao_bm.id"
			+ " WHERE med_item_medicao_bm_vl.med_medicao_fk = :idMedicao"
			+ "   AND med_item_medicao_bm.vrpl_submeta_fk = :idVrplSubmeta")
	@RegisterFieldMapper(ItemMedicaoBMValorBD.class)
	List<ItemMedicaoBMValorBD> listarItemMedicaoBMValor(@Bind("idMedicao") Long idMedicao, @Bind("idVrplSubmeta") Long idVrplSubmeta);

	@SqlUpdate("INSERT INTO med_item_medicao_bm_vl (med_item_medicao_bm_fk, med_medicao_fk, qt_empresa, qt_convenente, qt_concedente, adt_login, adt_data_hora, adt_operacao) "
			+ " VALUES ( :idItemMedicaoBM, :idMedicao, :qtEmpresa, :qtConvenente, :qtConcedente, current_setting('med.cpf_usuario'), LOCALTIMESTAMP, 'INSERT' )")
    @RegisterFieldMapper(ItemMedicaoBMValorBD.class)
	@GetGeneratedKeys
	ItemMedicaoBMValorBD inserirItemMedicaoBMValor(@BindFields ItemMedicaoBMValorBD itemMedicaoBMValorBD);

	@SqlUpdate("UPDATE med_item_medicao_bm_vl"
			+ " SET qt_empresa = :qtEmpresa,"
			+ "     qt_convenente = :qtConvenente,"
			+ "     qt_concedente = :qtConcedente,"
			+ "     adt_login = current_setting('med.cpf_usuario'),"
			+ "     adt_data_hora = LOCALTIMESTAMP,"
			+ "     adt_operacao = 'UPDATE'"
			+ " WHERE med_item_medicao_bm_fk = :idItemMedicaoBM"
			+ "   AND med_medicao_fk = :idMedicao")
    @RegisterFieldMapper(ItemMedicaoBMValorBD.class)
	@GetGeneratedKeys
	ItemMedicaoBMValorBD atualizarItemMedicaoBMValor(@BindFields ItemMedicaoBMValorBD itemMedicaoBMValorBD);

	@SqlUpdate("DELETE FROM med_item_medicao_bm_vl WHERE id = :idItemMedicaoBMValor")
	void excluirItemMedicaoBMValor(@BindFields ItemMedicaoBMValorBD itemMedicaoBMValorBD);

	@SqlUpdate("INSERT INTO med_item_medicao (vrpl_evento_fk, vrpl_frente_obra_fk, vrpl_submeta_fk, vl_total_servicos,med_contrato_fk, adt_login, adt_data_hora, adt_operacao) "
			+ " VALUES ( :idEventoVrpl, :idFrenteObraVrpl, :idSubmetaVrpl, :vlTotalServicos, :idContratoMedicao, current_setting('med.cpf_usuario'), LOCALTIMESTAMP, 'INSERT' )")
    @RegisterFieldMapper(ItemMedicaoBD.class)
	@GetGeneratedKeys
	ItemMedicaoBD inserir(@BindFields ItemMedicaoBD itemMedicaoBD);
	
	@SqlUpdate("INSERT INTO med_item_medicao_bm (med_contrato_fk, vrpl_submeta_fk, vrpl_frente_obra_fk, vrpl_servico_fk, qt_total_servico, vl_preco_unitario_licitado, adt_login, adt_data_hora, adt_operacao) "
			+ " VALUES ( :idContratoMedicao, :idSubmetaVrpl, :idFrenteObraVrpl, :idServicoVrpl , :qtTotalServico, :vlPrecoUnitarioLicitado, current_setting('med.cpf_usuario'), LOCALTIMESTAMP, 'INSERT' )")
    @RegisterFieldMapper(ItemMedicaoBMBD.class)
	@GetGeneratedKeys
	ItemMedicaoBMBD inserirItemBM(@BindFields ItemMedicaoBMBD itemMedicaoBMBD);
	
	@SqlQuery("select med_item_medicao_bm_vl.* "
			+ "from med_item_medicao_bm, med_item_medicao_bm_vl "
			+ "where med_item_medicao_bm.vrpl_submeta_fk = Cast(:idVrplSubmeta as int8) "
		    + " and med_item_medicao_bm_vl.med_item_medicao_bm_fk  = med_item_medicao_bm.id "
		    + " and med_item_medicao_bm_vl.med_medicao_fk = Cast(:idMedicao as int8) ")
	@RegisterFieldMapper(ItemMedicaoBMValorBD.class)
	List<ItemMedicaoBMValorBD> listarItensMedicaoBMValor(@Bind("idVrplSubmeta") Long idVrplSubmeta, @Bind("idMedicao") Long idMedicao);
	
	// Atualiza o Item Medição com ID da Medição da empresa
	@SqlUpdate("UPDATE med_item_medicao"
			+ " SET medicao_fk_empresa = :idMedicaoEmpresa, adt_login = current_setting('med.cpf_usuario'), adt_data_hora = LOCALTIMESTAMP, adt_operacao = 'UPDATE' "
			+ " WHERE vrpl_submeta_fk = :idVrplSubmeta AND vrpl_frente_obra_fk = :idVrplFrenteObra AND vrpl_evento_fk = :idVrplEvento")
	@RegisterFieldMapper(ItemMedicaoBD.class)
	@GetGeneratedKeys
	ItemMedicaoBD atualizarIndicadorExecutadoEmpresa(@Bind("idMedicaoEmpresa") Long idMedicaoEmpresa, @Bind("idVrplSubmeta") Long idVrplSubmeta,
			@Bind("idVrplFrenteObra") Long idVrplFrenteObra, @Bind("idVrplEvento") Long idVrplEvento);
	
	// Atualiza o Item Medição com ID da Medição da empresa
	@SqlUpdate("UPDATE med_item_medicao"
			+ " SET medicao_fk_empresa = :idMedicaoEmpresa, adt_login = current_setting('med.cpf_usuario'), adt_data_hora = LOCALTIMESTAMP, adt_operacao = 'UPDATE' "
			+ " WHERE vrpl_submeta_fk = :idSubmetaVrpl AND vrpl_frente_obra_fk = :idFrenteObraVrpl AND vrpl_evento_fk = :idEventoVrpl")
	@RegisterFieldMapper(ItemMedicaoBD.class)
	@GetGeneratedKeys
	ItemMedicaoBD atualizarIndicadorExecutadoEmpresa(@BindFields ItemMedicaoBD itemMedicaoBD);
	
	// Atualiza o Item Medição com ID da Medição do Convenente
	@SqlUpdate("UPDATE med_item_medicao"
			+ " SET medicao_fk_convenente = :idMedicaoConvenente, adt_login = current_setting('med.cpf_usuario'), adt_data_hora = LOCALTIMESTAMP, adt_operacao = 'UPDATE' "
			+ " WHERE vrpl_submeta_fk = :idSubmetaVrpl AND vrpl_frente_obra_fk = :idFrenteObraVrpl AND vrpl_evento_fk = :idEventoVrpl")
	@RegisterFieldMapper(ItemMedicaoBD.class)
	@GetGeneratedKeys
	ItemMedicaoBD atualizarIndicadorAtestadoConvenente(@BindFields ItemMedicaoBD itemMedicaoBD);

	@SqlUpdate("DELETE FROM med_item_medicao_bm_vl WHERE id = :idItemMedicaoValorBM")	
	void excluirItemMedicaoBMEmpresa(@Bind("idItemMedicaoValorBM") Long idItemMedicaoValorBM);		

	@SqlUpdate("DELETE FROM med_item_medicao_bm_vl WHERE med_medicao_fk = :idMedicao")	
	void excluirItemMedicaoValorBM(@Bind("idMedicao") Long idMedicao);

	@SqlUpdate("UPDATE med_item_medicao SET medicao_fk_empresa = null, adt_login = current_setting('med.cpf_usuario'), adt_data_hora = LOCALTIMESTAMP, adt_operacao = 'UPDATE' WHERE medicao_fk_empresa = :idMedicao AND vrpl_submeta_fk = :idSubmetaVrpl")
	void limparMedicaoEmpresa(@Bind("idMedicao") Long idMedicao, @Bind("idSubmetaVrpl") Long idSubmetaVrpl);

	@SqlUpdate("UPDATE siconv.med_item_medicao"
			+ " SET medicao_fk_empresa = null,"
			+ "     medicao_fk_convenente = null,"
			+ "     medicao_fk_concedente = null,"
			+ "     adt_login = current_setting('med.cpf_usuario'),"
			+ "     adt_data_hora = LOCALTIMESTAMP,"
			+ "     adt_operacao = 'UPDATE'"
			+ " WHERE medicao_fk_empresa = :idMedicao")
	void limparItemMedicaoPorIdMedicao(@Bind("idMedicao") Long idMedicao);

	@SqlUpdate("UPDATE med_item_medicao " 
			+ " SET medicao_fk_convenente = null, adt_login = current_setting('med.cpf_usuario'), adt_data_hora = LOCALTIMESTAMP, adt_operacao = 'UPDATE' "
			+ " WHERE medicao_fk_convenente = :idMedicao AND vrpl_submeta_fk = :idSubmetaVrpl")
	void limparMedicaoConvenente(@Bind("idMedicao") Long idMedicao, @Bind("idSubmetaVrpl") Long idSubmetaVrpl);

	@SqlUpdate("UPDATE med_item_medicao " 
			+ " SET medicao_fk_concedente = null, adt_login = current_setting('med.cpf_usuario'), adt_data_hora = LOCALTIMESTAMP, adt_operacao = 'UPDATE' "
			+ " WHERE medicao_fk_concedente = :idMedicao AND vrpl_submeta_fk = :idSubmetaVrpl")
	void limparMedicaoConcedente(@Bind("idMedicao") Long idMedicao, @Bind("idSubmetaVrpl") Long idSubmetaVrpl);
	
	@SqlUpdate("UPDATE med_item_medicao_bm_vl " 
			+ " SET qt_convenente = null, adt_login = current_setting('med.cpf_usuario'), adt_data_hora = LOCALTIMESTAMP, adt_operacao = 'UPDATE' "
			+ " WHERE id = :idItemMedicaoBMValor")
	void limparMedicaoConvenenteBM(@Bind("idItemMedicaoBMValor") Long idItemMedicaoBMValor);

	@SqlUpdate("UPDATE med_item_medicao_bm_vl " 
			+ " SET qt_concedente = null, adt_login = current_setting('med.cpf_usuario'), adt_data_hora = LOCALTIMESTAMP, adt_operacao = 'UPDATE' "
			+ " WHERE id = :idItemMedicaoBMValor")
	void limparMedicaoConcedenteBM(@Bind("idItemMedicaoBMValor") Long idItemMedicaoBMValor);

    @SqlQuery("SELECT vrpl_submeta_fk FROM med_item_medicao WHERE med_contrato_fk = :idContrato "
            + "UNION "
            + "SELECT vrpl_submeta_fk FROM med_item_medicao_bm WHERE med_contrato_fk = :idContrato")
    List<Long> consultarSubmetasContrato(@Bind("idContrato") Long idContrato);

	// Atualiza o Item Medição com ID da Medição do Concedente Mandatária
	@SqlUpdate("UPDATE med_item_medicao"
			+ " SET medicao_fk_concedente = :idMedicaoConcedente, adt_login = current_setting('med.cpf_usuario'), adt_data_hora = LOCALTIMESTAMP, adt_operacao = 'UPDATE' "
			+ " WHERE vrpl_submeta_fk = :idSubmetaVrpl AND vrpl_frente_obra_fk = :idFrenteObraVrpl AND vrpl_evento_fk = :idEventoVrpl")
	@RegisterFieldMapper(ItemMedicaoBD.class)
	@GetGeneratedKeys
	ItemMedicaoBD atualizarIndicadorAnalisadoConcedenteMandataria(@BindFields ItemMedicaoBD itemMedicaoBD);
	
	@SqlQuery("select id from med_item_medicao_bm " + 					 
			  "where med_item_medicao_bm.med_contrato_fk = :idContrato ")
    List<Long> consultarListaItemMedicaoBM(@Bind("idContrato") Long idContrato);
	
	@SqlUpdate("DELETE FROM med_item_medicao_bm WHERE med_contrato_fk = :idContrato")
	void excluirItemMedicaoBMPorContrato(@Bind("idContrato") Long idContrato);
	
	@SqlUpdate("DELETE FROM med_item_medicao WHERE med_contrato_fk = :idContrato")
	void excluirItemMedicaoPLEPorContrato(@Bind("idContrato") Long idContrato);
	
}
