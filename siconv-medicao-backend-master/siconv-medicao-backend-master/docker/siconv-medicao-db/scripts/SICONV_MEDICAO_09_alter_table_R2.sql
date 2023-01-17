/*
* ======================= DADOS DO SCRIPT ======================================================
* AUTOR: Fabiano
* OBJETIVO: Alteracao da tabela de negocio do SICONV MEDICAO para a release 2
* PRE-REQUISITOS: Modelo da release 1 previamente aplicado
*
*
* ======================= HISTÓRICO DE ALTERAÇÕES ==============================================
* DATA       | AUTOR                	| MOTIVO
* ----------------------------------------------------------------------------------------------
* 28/08/2020 | Fabiano Augusto Silva  	| Alteração da tabela med_anexo
*                                   	| a fim de incluir informação de responsável por inativaçao
* 
*/

-------------------------------  Tabela: med_anexo  -------------------------------
ALTER TABLE siconv.med_anexo ADD nr_cpf_inativo varchar(11) null;

COMMENT ON COLUMN siconv.med_anexo.nr_cpf_inativo IS 'Número do CPF do usuário que inativou o anexo.';

