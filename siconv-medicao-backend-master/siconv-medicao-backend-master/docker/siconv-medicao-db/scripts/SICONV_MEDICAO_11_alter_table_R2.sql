/*
* ======================= DADOS DO SCRIPT ======================================================
* AUTOR: Erison
* OBJETIVO: Alteracao da tabela med_doc_complementar para a release 2
* PRE-REQUISITOS: Modelo da release 1 previamente aplicado
*
*
* ======================= HISTÓRICO DE ALTERAÇÕES ==============================================
* DATA       | AUTOR                	| MOTIVO
* ----------------------------------------------------------------------------------------------
* 08/09/2020 | Erison Galvao		  	| Alteração da tabela med_doc_complementar
*                                   	| renomeando a coluna nm_licenca para tx_descricao_outros
* 
*/

-------------------------------  Tabela: med_doc_complementar  -------------------------------
ALTER TABLE siconv.med_doc_complementar RENAME COLUMN nm_licenca TO tx_descricao_outros;

COMMENT ON COLUMN siconv.med_doc_complementar.tx_descricao_outros IS 'Descrição quando Tipo Manifesto Ambiental igual a Outros';

