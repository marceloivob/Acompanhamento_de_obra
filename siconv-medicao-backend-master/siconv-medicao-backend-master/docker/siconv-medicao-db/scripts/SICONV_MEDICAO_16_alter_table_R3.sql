/*
* ======================= DADOS DO SCRIPT ======================================================
* AUTOR: Fabiano
* OBJETIVO: Alteração da tabela med_submeta_medicao para a release 3
* PRE-REQUISITOS: Modelo da release 1 e 2 previamente aplicado
*
*
* ======================= HISTÓRICO DE ALTERAÇÕES ==============================================
* DATA       | AUTOR                	| MOTIVO
* ----------------------------------------------------------------------------------------------
* 31/05/2021 | Fabiano Augusto       	| Alteração da tabela med_submeta_medicao
*                                   	| add coluna para exibição de perfil mandataria na assinatura da submeta
* 
*/

-------------------------------  Tabela: med_submeta_medicao  -------------------------------
ALTER TABLE siconv.med_submeta_medicao ADD in_perfil_mandataria bool NULL;