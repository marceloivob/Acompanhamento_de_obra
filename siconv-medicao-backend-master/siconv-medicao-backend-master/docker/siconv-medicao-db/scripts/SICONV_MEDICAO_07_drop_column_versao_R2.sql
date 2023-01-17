/*
* ======================================== DADOS DO SCRIPT =================================================
* AUTOR: Gustavo Ferraz Diniz
* OBJETIVO: Remover coluna versao das tabelas med_doc_complementar_submeta, med_contrato, med_item_medicao,
*           med_contrato_rt_social_submeta, med_anotacao_registro_rt_submeta e suas respectivas tabelas de
*           auditoria (log_rec).
* PRE-REQUISITOS: Modelo da release 2 previamente aplicado
*/

-------------------------------------------------------------------------------------
-- Tabelas med_doc_complementar_submeta e med_doc_complementar_submeta_log_rec
-------------------------------------------------------------------------------------

ALTER TABLE siconv.med_doc_complementar_submeta DROP COLUMN versao;
ALTER TABLE siconv.med_doc_complementar_submeta_log_rec DROP COLUMN versao;

CREATE OR REPLACE FUNCTION siconv.med_doc_complementar_submeta_trigger()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
   DECLARE wl_id integer;
   BEGIN
   -- Aqui temos um bloco IF que confirmará o tipo de operação.
   IF (TG_OP = 'INSERT') or (TG_OP = 'UPDATE') THEN
       INSERT INTO siconv.med_doc_complementar_submeta_log_rec (
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        entity_id,
        meddoccomplementarlog,
        vrpl_submeta_fk
       ) VALUES (
        nextval('siconv.med_doc_complementar_submeta_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        NEW.adt_data_hora,
        NEW.adt_operacao,
        NEW.id,
        NEW.med_doc_complementar_fk,
        NEW.vrpl_submeta_fk
       );
       RETURN NEW;
   -- Aqui temos um bloco IF que confirmará o tipo de operação DELETE.
   ELSIF (TG_OP = 'DELETE') THEN
       INSERT INTO siconv.med_doc_complementar_submeta_log_rec (
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        entity_id,
        meddoccomplementarlog,
        vrpl_submeta_fk
       ) VALUES (
        nextval('siconv.med_doc_complementar_submeta_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        LOCALTIMESTAMP,
        TG_OP,
        OLD.id,
        OLD.med_doc_complementar_fk,
        OLD.vrpl_submeta_fk
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $function$
;

-------------------------------------------------------------------------------------
-- Tabelas med_contrato e med_contrato_log_rec
-------------------------------------------------------------------------------------

ALTER TABLE siconv.med_contrato DROP COLUMN versao;
ALTER TABLE siconv.med_contrato_log_rec DROP COLUMN versao;

CREATE OR REPLACE FUNCTION siconv.med_contrato_trigger()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
   DECLARE wl_id integer;
   BEGIN
   -- Aqui temos um bloco IF que confirmará o tipo de operação.
   IF (TG_OP = 'INSERT') or (TG_OP = 'UPDATE') THEN
       INSERT INTO siconv.med_contrato_log_rec (
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        cnpj_fornecedor,
        contrato_fk,
        dt_inicio_obra,
        entity_id,
        in_social,
        proposta_fk
       ) VALUES (
        nextval('siconv.med_contrato_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        NEW.adt_data_hora,
        NEW.adt_operacao,
        NEW.cnpj_fornecedor,
        NEW.contrato_fk,
        NEW.dt_inicio_obra,
        NEW.id,
        NEW.in_social,
        NEW.proposta_fk
       );
       RETURN NEW;
   -- Aqui temos um bloco IF que confirmará o tipo de operação DELETE.
   ELSIF (TG_OP = 'DELETE') THEN
       INSERT INTO siconv.med_contrato_log_rec (
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        cnpj_fornecedor,
        contrato_fk,
        dt_inicio_obra,
        entity_id,
        in_social,
        proposta_fk
       ) VALUES (
        nextval('siconv.med_contrato_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        LOCALTIMESTAMP,
        TG_OP,
        OLD.cnpj_fornecedor,
        OLD.contrato_fk,
        OLD.dt_inicio_obra,
        OLD.id,
        OLD.in_social,
        OLD.proposta_fk
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $function$
;

-------------------------------------------------------------------------------------
-- Tabelas med_item_medicao e med_item_medicao_log_rec
-------------------------------------------------------------------------------------

ALTER TABLE siconv.med_item_medicao DROP COLUMN versao;
ALTER TABLE siconv.med_item_medicao_log_rec DROP COLUMN versao;

CREATE OR REPLACE FUNCTION siconv.med_item_medicao_trigger()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
   DECLARE wl_id integer;
   BEGIN
   -- Aqui temos um bloco IF que confirmará o tipo de operação.
   IF (TG_OP = 'INSERT') or (TG_OP = 'UPDATE') THEN
       INSERT INTO siconv.med_item_medicao_log_rec (
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        entity_id,
        medcontratolog,
        medicaologconcedente,
        medicaologconvenente,
        medicaologempresa,
        vl_total_servicos,
        vrpl_evento_fk,
        vrpl_frente_obra_fk,
        vrpl_submeta_fk
       ) VALUES (
        nextval('siconv.med_item_medicao_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        NEW.adt_data_hora,
        NEW.adt_operacao,
        NEW.id,
        NEW.med_contrato_fk,
        NEW.medicao_fk_concedente,
        NEW.medicao_fk_convenente,
        NEW.medicao_fk_empresa,
        NEW.vl_total_servicos,
        NEW.vrpl_evento_fk,
        NEW.vrpl_frente_obra_fk,
        NEW.vrpl_submeta_fk
       );
       RETURN NEW;
   -- Aqui temos um bloco IF que confirmará o tipo de operação DELETE.
   ELSIF (TG_OP = 'DELETE') THEN
       INSERT INTO siconv.med_item_medicao_log_rec (
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        entity_id,
        medcontratolog,
        medicaologconcedente,
        medicaologconvenente,
        medicaologempresa,
        vl_total_servicos,
        vrpl_evento_fk,
        vrpl_frente_obra_fk,
        vrpl_submeta_fk
       ) VALUES (
        nextval('siconv.med_item_medicao_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        LOCALTIMESTAMP,
        TG_OP,
        OLD.id,
        OLD.med_contrato_fk,
        OLD.medicao_fk_concedente,
        OLD.medicao_fk_convenente,
        OLD.medicao_fk_empresa,
        OLD.vl_total_servicos,
        OLD.vrpl_evento_fk,
        OLD.vrpl_frente_obra_fk,
        OLD.vrpl_submeta_fk
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $function$
;

-------------------------------------------------------------------------------------
-- Tabelas med_contrato_rt_social_submeta e med_contrato_rt_social_submeta_log_rec
-------------------------------------------------------------------------------------

ALTER TABLE siconv.med_contrato_rt_social_submeta DROP COLUMN versao;
ALTER TABLE siconv.med_contrato_rt_social_submeta_log_rec DROP COLUMN versao;

CREATE OR REPLACE FUNCTION siconv.med_contrato_rt_social_submeta_trigger()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
   DECLARE wl_id integer;
   BEGIN
   -- Aqui temos um bloco IF que confirmará o tipo de operação.
   IF (TG_OP = 'INSERT') or (TG_OP = 'UPDATE') THEN
       INSERT INTO siconv.med_contrato_rt_social_submeta_log_rec (
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        entity_id,
        medcontratoresptecnicosociallog,
        vrpl_submeta_fk
       ) VALUES (
        nextval('siconv.med_contrato_rt_social_submeta_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        NEW.adt_data_hora,
        NEW.adt_operacao,
        NEW.id,
        NEW.med_contrato_resp_tecnico_social_fk,
        NEW.vrpl_submeta_fk
       );
       RETURN NEW;
   -- Aqui temos um bloco IF que confirmará o tipo de operação DELETE.
   ELSIF (TG_OP = 'DELETE') THEN
       INSERT INTO siconv.med_contrato_rt_social_submeta_log_rec (
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        entity_id,
        medcontratoresptecnicosociallog,
        vrpl_submeta_fk
       ) VALUES (
        nextval('siconv.med_contrato_rt_social_submeta_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        LOCALTIMESTAMP,
        TG_OP,
        OLD.id,
        OLD.med_contrato_resp_tecnico_social_fk,
        OLD.vrpl_submeta_fk
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $function$
;

-------------------------------------------------------------------------------------
-- Tabelas med_anotacao_registro_rt_submeta e med_anotacao_registro_rt_submeta_log_rec
-------------------------------------------------------------------------------------

ALTER TABLE siconv.med_anotacao_registro_rt_submeta DROP COLUMN versao;
ALTER TABLE siconv.med_anotacao_registro_rt_submeta_log_rec DROP COLUMN versao;

CREATE OR REPLACE FUNCTION siconv.med_anotacao_registro_rt_submeta_trigger()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
   DECLARE wl_id integer;
   BEGIN
   -- Aqui temos um bloco IF que confirmará o tipo de operação.
   IF (TG_OP = 'INSERT') or (TG_OP = 'UPDATE') THEN
       INSERT INTO siconv.med_anotacao_registro_rt_submeta_log_rec (
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        entity_id,
        medanotacaoregistrortlog,
        vrpl_submeta_fk
       ) VALUES (
        nextval('siconv.med_anotacao_registro_rt_submeta_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        NEW.adt_data_hora,
        NEW.adt_operacao,
        NEW.id,
        NEW.med_anotacao_registro_rt_fk,
        NEW.vrpl_submeta_fk
       );
       RETURN NEW;
   -- Aqui temos um bloco IF que confirmará o tipo de operação DELETE.
   ELSIF (TG_OP = 'DELETE') THEN
       INSERT INTO siconv.med_anotacao_registro_rt_submeta_log_rec (
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        entity_id,
        medanotacaoregistrortlog,
        vrpl_submeta_fk
       ) VALUES (
        nextval('siconv.med_anotacao_registro_rt_submeta_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        LOCALTIMESTAMP,
        TG_OP,
        OLD.id,
        OLD.med_anotacao_registro_rt_fk,
        OLD.vrpl_submeta_fk
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $function$
;

-------------------------------------------------------------------------------------
-- Tabelas med_anexo e med_anexo_log_rec
-------------------------------------------------------------------------------------

ALTER TABLE siconv.med_anexo DROP COLUMN versao;
ALTER TABLE siconv.med_anexo_log_rec DROP COLUMN versao;

CREATE OR REPLACE FUNCTION siconv.med_anexo_trigger()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
   DECLARE wl_id integer;
   BEGIN
   -- Aqui temos um bloco IF que confirmará o tipo de operação.
   IF (TG_OP = 'INSERT') or (TG_OP = 'UPDATE') THEN
       INSERT INTO siconv.med_anexo_log_rec (
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        co_ceph,
        entity_id,
        nm_arquivo,
        observacaolog,
        in_inativo
       ) VALUES (
        nextval('siconv.med_anexo_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        NEW.adt_data_hora,
        NEW.adt_operacao,
        NEW.co_ceph,
        NEW.id,
        NEW.nm_arquivo,
        NEW.observacao_fk,
        NEW.in_inativo
       );
       RETURN NEW;
   -- Aqui temos um bloco IF que confirmará o tipo de operação DELETE.
   ELSIF (TG_OP = 'DELETE') THEN
       INSERT INTO siconv.med_anexo_log_rec (
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        co_ceph,
        entity_id,
        nm_arquivo,
        observacaolog,
        in_inativo
       ) VALUES (
        nextval('siconv.med_anexo_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        LOCALTIMESTAMP,
        TG_OP,
        OLD.co_ceph,
        OLD.id,
        OLD.nm_arquivo,
        OLD.observacao_fk,
        OLD.in_inativo
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $function$
;

ALTER FUNCTION siconv.med_doc_complementar_submeta_trigger() OWNER TO owner_siconv_p;
ALTER FUNCTION siconv.med_contrato_trigger() OWNER TO owner_siconv_p;
ALTER FUNCTION siconv.med_item_medicao_trigger() OWNER TO owner_siconv_p;
ALTER FUNCTION siconv.med_contrato_rt_social_submeta_trigger() OWNER TO owner_siconv_p;
ALTER FUNCTION siconv.med_anotacao_registro_rt_submeta_trigger() OWNER TO owner_siconv_p;
ALTER FUNCTION siconv.med_anexo_trigger() OWNER TO owner_siconv_p;
