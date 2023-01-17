/*
* ======================================== DADOS DO SCRIPT =================================================
* AUTOR: Mariana Oliveira
* OBJETIVO: Alteracao das tabelas e triggers de controle de auditoria (i.e. log_rec) do SICONV MEDICAO
*           para a release 3
* PRE-REQUISITOS: Modelo da release 1 e 2 previamente aplicado
*
*
* ======================= HISTÓRICO DE ALTERAÇÕES ==========================================================
* DATA       | AUTOR                	| MOTIVO
* ----------------------------------------------------------------------------------------------------------
* 05/03/2021 | Mariana Oliveira  	| Alteração da tabela med_submeta_medicao
*                                   	| nos campos referentes às assinaturas do concedente na submeta 
*                                   	| (in_situacao_concedente, nr_cpf_responsavel_concedente,
*                                   	| dt_assinatura_concedente)
* 08/03/2021 | Mariana Oliveira  	| Alteração da tabela med_medicao
*                                   	| nos campos referentes à vistoria extra 
*                                   	| (in_vistoria_extra, dt_vistoria_extra,
*                                   	| in_solicitante_vistoria)
* 
*/


-------------------------------  Tabela: med_submeta_medicao_log_rec  -------------------------------

ALTER TABLE siconv.med_submeta_medicao_log_rec ADD dt_assinatura_concedente timestamp NULL;
ALTER TABLE siconv.med_submeta_medicao_log_rec ADD nr_cpf_resp_concedente bpchar(11) NULL;
ALTER TABLE siconv.med_submeta_medicao_log_rec ADD in_situacao_concedente varchar(3) NULL;


CREATE OR REPLACE FUNCTION siconv.med_submeta_medicao_trigger()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
   DECLARE wl_id integer;
   BEGIN
   -- Aqui temos um bloco IF que confirmará o tipo de operação.
   IF (TG_OP = 'INSERT') or (TG_OP = 'UPDATE') THEN
       INSERT INTO siconv.med_submeta_medicao_log_rec (
        versao,
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        dt_assinatura_empresa,
        entity_id,
        in_situacao_empresa,
        medicaolog,
        nr_cpf_resp_empresa,
        vrpl_submeta_fk,
        dt_assinatura_convenente,
        nr_cpf_resp_convenente,
        in_situacao_convenente,
        dt_assinatura_concedente,
        nr_cpf_resp_concedente,
        in_situacao_concedente
       ) VALUES (
        NEW.versao,
        nextval('siconv.med_submeta_medicao_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        NEW.adt_data_hora,
        NEW.adt_operacao,
        NEW.dt_assinatura_empresa,
        NEW.id,
        NEW.in_situacao_empresa,
        NEW.medicao_fk,
        NEW.nr_cpf_resp_empresa,
        NEW.vrpl_submeta_fk,
        NEW.dt_assinatura_convenente,
        NEW.nr_cpf_resp_convenente,
        NEW.in_situacao_convenente,
        NEW.dt_assinatura_concedente,
        NEW.nr_cpf_resp_concedente,
        NEW.in_situacao_concedente
       );
       RETURN NEW;
   -- Aqui temos um bloco IF que confirmará o tipo de operação DELETE.
   ELSIF (TG_OP = 'DELETE') THEN
       INSERT INTO siconv.med_submeta_medicao_log_rec (
        versao,
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        dt_assinatura_empresa,
        entity_id,
        in_situacao_empresa,
        medicaolog,
        nr_cpf_resp_empresa,
        vrpl_submeta_fk,
        dt_assinatura_convenente,
        nr_cpf_resp_convenente,
        in_situacao_convenente,
        dt_assinatura_concedente,
        nr_cpf_resp_concedente,
        in_situacao_concedente
       ) VALUES (
        OLD.versao,
        nextval('siconv.med_submeta_medicao_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        LOCALTIMESTAMP,
        TG_OP,
        OLD.dt_assinatura_empresa,
        OLD.id,
        OLD.in_situacao_empresa,
        OLD.medicao_fk,
        OLD.nr_cpf_resp_empresa,
        OLD.vrpl_submeta_fk,
        OLD.dt_assinatura_convenente,
        OLD.nr_cpf_resp_convenente,
        OLD.in_situacao_convenente,
        OLD.dt_assinatura_concedente,
        OLD.nr_cpf_resp_concedente,
        OLD.in_situacao_concedente
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $function$
;

-------------------------------  Tabela: med_medicao_log_rec  -------------------------------

ALTER TABLE siconv.med_medicao_log_rec ADD in_vistoria_extra bool NULL;
ALTER TABLE siconv.med_medicao_log_rec ADD dt_vistoria_extra timestamp NULL;
ALTER TABLE siconv.med_medicao_log_rec ADD in_solicitante_vistoria varchar(3) NULL;

CREATE OR REPLACE FUNCTION siconv.med_medicao_trigger()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
   DECLARE wl_id integer;
   BEGIN
   -- Aqui temos um bloco IF que confirmará o tipo de operação.
   IF (TG_OP = 'INSERT') or (TG_OP = 'UPDATE') THEN
       INSERT INTO siconv.med_medicao_log_rec (
        versao,
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        dt_fim,
        dt_inicio,
        entity_id,
        in_situacao,
        medcontratolog,
        nr_sequencial,
        medicaologagrupadora,
        in_bloqueio,
	in_vistoria_extra,
	dt_vistoria_extra,
	in_solicitante_vistoria
       ) VALUES (
        NEW.versao,
        nextval('siconv.med_medicao_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        NEW.adt_data_hora,
        NEW.adt_operacao,
        NEW.dt_fim,
        NEW.dt_inicio,
        NEW.id,
        NEW.in_situacao,
        NEW.med_contrato_fk,
        NEW.nr_sequencial,
        NEW.medicao_fk_agrupadora,
        NEW.in_bloqueio,
	NEW.in_vistoria_extra,
	NEW.dt_vistoria_extra,
	NEW.in_solicitante_vistoria
       );
       RETURN NEW;
   -- Aqui temos um bloco IF que confirmará o tipo de operação DELETE.
   ELSIF (TG_OP = 'DELETE') THEN
       INSERT INTO siconv.med_medicao_log_rec (
        versao,
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        dt_fim,
        dt_inicio,
        entity_id,
        in_situacao,
        medcontratolog,
        nr_sequencial,
        medicaologagrupadora,
        in_bloqueio,
	in_vistoria_extra,
	dt_vistoria_extra,
	in_solicitante_vistoria
       ) VALUES (
        OLD.versao,
        nextval('siconv.med_medicao_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        LOCALTIMESTAMP,
        TG_OP,
        OLD.dt_fim,
        OLD.dt_inicio,
        OLD.id,
        OLD.in_situacao,
        OLD.med_contrato_fk,
        OLD.nr_sequencial,
        OLD.medicao_fk_agrupadora,
        OLD.in_bloqueio,
	OLD.in_vistoria_extra,
	OLD.dt_vistoria_extra,
	OLD.in_solicitante_vistoria
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $function$
;

ALTER FUNCTION siconv.med_submeta_medicao_trigger() OWNER TO owner_siconv_p;
ALTER FUNCTION siconv.med_medicao_trigger() OWNER TO owner_siconv_p;  



