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
* 05/03/2021 | Mariana Oliveira  		| Alteração da tabela med_submeta_medicao
*                                  		| no campo referente à indicação de assinatura da mandataria na submeta 
*                                  		| (in_perfil_mandataria)
* 
*/
-------------------------------  Tabela: med_submeta_medicao_log_rec  -------------------------------
ALTER TABLE siconv.med_submeta_medicao_log_rec ADD in_perfil_mandataria bool NULL;

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
        in_situacao_concedente,
        in_perfil_mandataria
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
        NEW.in_situacao_concedente,
        NEW.in_perfil_mandataria
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
        in_situacao_concedente,
        in_perfil_mandataria
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
        OLD.in_situacao_concedente,
        OLD.in_perfil_mandataria
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $function$
;
