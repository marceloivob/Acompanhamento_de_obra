package br.gov.planejamento.siconv.med.infra.database;



import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import br.gov.planejamento.siconv.med.infra.database.SqlFormatter;

class SQLFormatterTest {


	private final String STR_INPUT = "SELECT siconv.acffo_doc_complementar.id	as idDocumentoComplementar,	siconv.acffo_doc_complementar.tx_orgao_emissor	as nomeOrgaoEmissor,	siconv.acffo_doc_complementar.dt_emissao as dataEmissao, siconv.acffo_doc_complementar.dt_valida_ate as dataValidade, siconv.acffo_doc_complementar.nr_documento as numeroDocumento, siconv.acffo_tp_manif_ambiental.tx_nome as tipoManifestoAmbiental,	siconv.acffo_tp_doc_complementar.tx_nome as tipoDocumentoComplementar, siconv.acffo.id	as idAcffo, siconv.acffo.prop_fk as propFk,	siconv.acffo_meta.id as idMeta, siconv.acffo_meta.nr_meta as numeroMeta, siconv.acffo_meta.tx_descricao	 as descricaoMeta,	siconv.acffo_submeta.id	as idSubmeta, siconv.acffo_submeta.nr_submeta as numeroSubmeta, siconv.acffo_submeta.nr_lote as numeroLote,\n" + 
			"	siconv.acffo_submeta.tx_descricao as descricaoSubmeta FROM siconv.acffo_doc_complementar JOIN siconv.acffo ON ( siconv.acffo_doc_complementar.acffo_fk = siconv.acffo.id) JOIN siconv.acffo_tp_doc_complementar ON (siconv.acffo_doc_complementar.tp_doc_complementar_fk = siconv.acffo_tp_doc_complementar.id) JOIN siconv.acffo_tp_manif_ambiental 	ON (siconv.acffo_doc_complementar.tp_manif_ambiental_fk	= siconv.acffo_tp_manif_ambiental.id) JOIN siconv.acffo_doc_complementar_meta ON (siconv.acffo_doc_complementar_meta.doc_complementar_fk 	= siconv.acffo_doc_complementar.id) JOIN siconv.acffo_meta ON (siconv.acffo_doc_complementar_meta.meta_fk = siconv.acffo_meta.id) JOIN siconv.acffo_submeta ON (siconv.acffo_submeta.meta_fk = siconv.acffo_meta.id) WHERE siconv.acffo_submeta.id IN (030429, 30431, 30432, 30433) AND ( (siconv.acffo.prop_fk, siconv.acffo.versao_nr) IN\n" + 
			"		(SELECT siconv.acffo.prop_fk, max(siconv.acffo.versao_nr) FROM siconv.acffo JOIN siconv.acffo_qci ON (siconv.acffo_qci.acffo_fk	= siconv.acffo.id) JOIN siconv.acffo_meta ON (siconv.acffo_meta.qci_fk	= siconv.acffo_qci.id) JOIN siconv.acffo_submeta ON (siconv.acffo_submeta.meta_fk = siconv.acffo_meta.id) WHERE siconv.acffo_submeta.id IN (030429, 30431, 30432, 30433) GROUP BY siconv.acffo.prop_fk)) --AND siconv.acffo_tp_manif_ambiental.tx_nome 	in ('DISPENSA', 'LICENCA_INSTALACAO') ORDER BY siconv.acffo.prop_fk, siconv.acffo_meta.nr_meta, siconv.acffo_submeta.nr_submeta;";

	private final String STR_RESULT = "\n"
			+ "    SELECT\n" + 
			"        siconv.acffo_doc_complementar.id as idDocumentoComplementar,\n" + 
			"        siconv.acffo_doc_complementar.tx_orgao_emissor as nomeOrgaoEmissor,\n" + 
			"        siconv.acffo_doc_complementar.dt_emissao as dataEmissao,\n" + 
			"        siconv.acffo_doc_complementar.dt_valida_ate as dataValidade,\n" + 
			"        siconv.acffo_doc_complementar.nr_documento as numeroDocumento,\n" + 
			"        siconv.acffo_tp_manif_ambiental.tx_nome as tipoManifestoAmbiental,\n" + 
			"        siconv.acffo_tp_doc_complementar.tx_nome as tipoDocumentoComplementar,\n" + 
			"        siconv.acffo.id as idAcffo,\n" + 
			"        siconv.acffo.prop_fk as propFk,\n" + 
			"        siconv.acffo_meta.id as idMeta,\n" + 
			"        siconv.acffo_meta.nr_meta as numeroMeta,\n" + 
			"        siconv.acffo_meta.tx_descricao  as descricaoMeta,\n" + 
			"        siconv.acffo_submeta.id as idSubmeta,\n" + 
			"        siconv.acffo_submeta.nr_submeta as numeroSubmeta,\n" + 
			"        siconv.acffo_submeta.nr_lote as numeroLote,\n" + 
			"        siconv.acffo_submeta.tx_descricao as descricaoSubmeta \n" + 
			"    FROM\n" + 
			"        siconv.acffo_doc_complementar \n" + 
			"    JOIN\n" + 
			"        siconv.acffo \n" + 
			"            ON (\n" + 
			"                siconv.acffo_doc_complementar.acffo_fk = siconv.acffo.id\n" + 
			"            ) \n" + 
			"    JOIN\n" + 
			"        siconv.acffo_tp_doc_complementar \n" + 
			"            ON (\n" + 
			"                siconv.acffo_doc_complementar.tp_doc_complementar_fk = siconv.acffo_tp_doc_complementar.id\n" + 
			"            ) \n" + 
			"    JOIN\n" + 
			"        siconv.acffo_tp_manif_ambiental  \n" + 
			"            ON (\n" + 
			"                siconv.acffo_doc_complementar.tp_manif_ambiental_fk = siconv.acffo_tp_manif_ambiental.id\n" + 
			"            ) \n" + 
			"    JOIN\n" + 
			"        siconv.acffo_doc_complementar_meta \n" + 
			"            ON (\n" + 
			"                siconv.acffo_doc_complementar_meta.doc_complementar_fk  = siconv.acffo_doc_complementar.id\n" + 
			"            ) \n" + 
			"    JOIN\n" + 
			"        siconv.acffo_meta \n" + 
			"            ON (\n" + 
			"                siconv.acffo_doc_complementar_meta.meta_fk = siconv.acffo_meta.id\n" + 
			"            ) \n" + 
			"    JOIN\n" + 
			"        siconv.acffo_submeta \n" + 
			"            ON (\n" + 
			"                siconv.acffo_submeta.meta_fk = siconv.acffo_meta.id\n" + 
			"            ) \n" + 
			"    WHERE\n" + 
			"        siconv.acffo_submeta.id IN (\n" + 
			"            030429, 30431, 30432, 30433\n" + 
			"        ) \n" + 
			"        AND (\n" + 
			"            (\n" + 
			"                siconv.acffo.prop_fk, siconv.acffo.versao_nr\n" + 
			"            ) IN   (\n" + 
			"                SELECT\n" + 
			"                    siconv.acffo.prop_fk,\n" + 
			"                    max(siconv.acffo.versao_nr) \n" + 
			"                FROM\n" + 
			"                    siconv.acffo \n" + 
			"                JOIN\n" + 
			"                    siconv.acffo_qci \n" + 
			"                        ON (\n" + 
			"                            siconv.acffo_qci.acffo_fk = siconv.acffo.id\n" + 
			"                        ) \n" + 
			"                JOIN\n" + 
			"                    siconv.acffo_meta \n" + 
			"                        ON (\n" + 
			"                            siconv.acffo_meta.qci_fk = siconv.acffo_qci.id\n" + 
			"                        ) \n" + 
			"                JOIN\n" + 
			"                    siconv.acffo_submeta \n" + 
			"                        ON (\n" + 
			"                            siconv.acffo_submeta.meta_fk = siconv.acffo_meta.id\n" + 
			"                        ) \n" + 
			"                WHERE\n" + 
			"                    siconv.acffo_submeta.id IN (\n" + 
			"                        030429, 30431, 30432, 30433\n" + 
			"                    ) \n" + 
			"                GROUP BY\n" + 
			"                    siconv.acffo.prop_fk\n" + 
			"            )\n" + 
			"        ) --\n" + 
			"        AND siconv.acffo_tp_manif_ambiental.tx_nome  in (\n" + 
			"            'DISPENSA', 'LICENCA_INSTALACAO'\n" + 
			"        ) \n" + 
			"    ORDER BY\n" + 
			"        siconv.acffo.prop_fk,\n" + 
			"        siconv.acffo_meta.nr_meta,\n" + 
			"        siconv.acffo_submeta.nr_submeta;";
	
	
	@InjectMocks
	private SqlFormatter formatter;
	
	
	@BeforeEach
	void setup() throws Exception {

		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	void testFormat() {
		
		String sqlFormatado = formatter.format(STR_INPUT);
		
		assertEquals(STR_RESULT, sqlFormatado);
		
	}
	
}
