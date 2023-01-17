/*
* ================================= DADOS DO SCRIPT ==================================================
* AUTOR: Gustavo Diniz
* OBJETIVO: Alteracao da tabela med_submeta_medicao para remover coluna in_perfil_mandataria [BOOLEAN]
*           e adicionar nova coluna in_perfil_resp_concedente [VARCHAR 3].
* DATA CRIACAO: 10/08/2021
* PRE-REQUISITOS: Modelo atual da release 3 previamente aplicado
*/

-------------------------------  Tabela: med_submeta_medicao  -------------------------------
ALTER TABLE siconv.med_submeta_medicao ADD in_perfil_resp_concedente varchar(3) NULL;

COMMENT ON COLUMN siconv.med_submeta_medicao.in_perfil_resp_concedente IS 'Perfil do usuário responsável pela assinatura concedente';

ALTER TABLE siconv.med_submeta_medicao DISABLE TRIGGER tg_med_submeta_medicao;
ALTER TABLE siconv.med_submeta_medicao DISABLE TRIGGER tg_concurrent_med_submeta_medicao;

UPDATE siconv.med_submeta_medicao SET in_perfil_resp_concedente = 'CCE'
WHERE nr_cpf_resp_concedente IS NOT NULL AND in_perfil_mandataria = FALSE;

UPDATE siconv.med_submeta_medicao SET in_perfil_resp_concedente = 'MAN'
WHERE nr_cpf_resp_concedente IS NOT NULL AND in_perfil_mandataria = TRUE;

ALTER TABLE siconv.med_submeta_medicao ENABLE TRIGGER tg_med_submeta_medicao;
ALTER TABLE siconv.med_submeta_medicao ENABLE TRIGGER tg_concurrent_med_submeta_medicao;

ALTER TABLE siconv.med_submeta_medicao DROP COLUMN in_perfil_mandataria;

