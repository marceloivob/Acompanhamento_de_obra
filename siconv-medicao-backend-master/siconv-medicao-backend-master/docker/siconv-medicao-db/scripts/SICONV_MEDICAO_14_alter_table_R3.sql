/*
* ======================= DADOS DO SCRIPT ======================================================
* AUTOR: Mariana
* OBJETIVO: Alteração da tabela med_submeta_medicao para a release 3
* PRE-REQUISITOS: Modelo da release 1 e 2 previamente aplicado
*
*
* ======================= HISTÓRICO DE ALTERAÇÕES ==============================================
* DATA       | AUTOR                	| MOTIVO
* ----------------------------------------------------------------------------------------------
* 05/03/2021 | Mariana Oliveira  	| Alteração da tabela med_submeta_medicao
*                                   	| add colunas para aceite da medição
* 08/03/2021 | Mariana Oliveira  	| Alteração da tabela med_medicao
*                                   	| nos campos referentes à vistoria extra 
*                                   	| (in_vistoria_extra, dt_vistoria_extra,
*                                   	| in_solicitante_vistoria)
* 
*/

-------------------------------  Tabela: med_submeta_medicao  -------------------------------
ALTER TABLE siconv.med_submeta_medicao ADD dt_assinatura_concedente timestamp NULL;
ALTER TABLE siconv.med_submeta_medicao ADD nr_cpf_resp_concedente bpchar(11) NULL;
ALTER TABLE siconv.med_submeta_medicao ADD in_situacao_concedente varchar(3) NULL;

COMMENT ON COLUMN siconv.med_submeta_medicao.dt_assinatura_concedente IS 'Data da assinatura da Submeta da Medição';
COMMENT ON COLUMN siconv.med_submeta_medicao.nr_cpf_resp_concedente IS 'CPF do responsável pela assinatura da Submeta da Medição';
COMMENT ON COLUMN siconv.med_submeta_medicao.in_situacao_concedente IS 'Situação da assinatura da Submeta da Medição';

-------------------------------  Tabela: med_medicao  -------------------------------
ALTER TABLE siconv.med_medicao ADD in_vistoria_extra bool NULL;
ALTER TABLE siconv.med_medicao ADD dt_vistoria_extra timestamp NULL;
ALTER TABLE siconv.med_medicao ADD in_solicitante_vistoria varchar(3) NULL;

COMMENT ON COLUMN siconv.med_medicao.in_vistoria_extra IS 'Indicador de Vistoria Extra';
COMMENT ON COLUMN siconv.med_medicao.dt_vistoria_extra IS 'Data da Vistoria Extra';
COMMENT ON COLUMN siconv.med_medicao.in_solicitante_vistoria IS 'Solicitante da Vistoria Extra';

