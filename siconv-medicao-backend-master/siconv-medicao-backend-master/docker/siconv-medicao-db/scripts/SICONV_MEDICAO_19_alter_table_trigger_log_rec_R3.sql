/*
* ================================== DADOS DO SCRIPT ================================================
* AUTOR: Gustavo Diniz
* OBJETIVO: Alteracao da tabela e trigger de controle de auditoria (i.e. log_rec) da tabela de 
*           negocio med_submeta_medicao, apos remocao da coluna in_perfil_mandataria [BOOLEAN] 
*           e adicao da coluna in_perfil_resp_concedente [VARCHAR 3].
* DATA CRIACAO: 10/08/2021
* PRE-REQUISITOS: Modelo atual da release 3 previamente aplicado
*/

-------------------------------  Tabela: med_submeta_medicao_log_rec  -------------------------------
ALTER TABLE siconv.med_submeta_medicao_log_rec ADD in_perfil_resp_concedente varchar(3) NULL;

COMMENT ON COLUMN siconv.med_submeta_medicao_log_rec.in_perfil_resp_concedente IS 'Perfil do usuário responsável pela assinatura concedente';

UPDATE siconv.med_submeta_medicao_log_rec SET in_perfil_resp_concedente = 'CCE'
WHERE nr_cpf_resp_concedente IS NOT NULL AND in_perfil_mandataria = FALSE;

UPDATE siconv.med_submeta_medicao_log_rec SET in_perfil_resp_concedente = 'MAN'
WHERE nr_cpf_resp_concedente IS NOT NULL AND in_perfil_mandataria = TRUE;

ALTER TABLE siconv.med_submeta_medicao_log_rec DROP COLUMN in_perfil_mandataria;

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
        in_perfil_resp_concedente
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
        NEW.in_perfil_resp_concedente
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
        in_perfil_resp_concedente
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
        OLD.in_perfil_resp_concedente
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $function$
;

ALTER FUNCTION siconv.med_submeta_medicao_trigger() OWNER TO owner_siconv_p;


