/*
* ======================================== DADOS DO SCRIPT =================================================
* AUTOR: Gustavo Ferraz Diniz
* OBJETIVO: Alteracao das tabelas e triggers de controle de auditoria (i.e. log_rec) do SICONV MEDICAO
*           para a release 2
* PRE-REQUISITOS: Modelo da release 1 previamente aplicado
*
*
* ======================= HISTÓRICO DE ALTERAÇÕES ==========================================================
* DATA       | AUTOR                	| MOTIVO
* ----------------------------------------------------------------------------------------------------------
* 25/05/2020 | Ana Cristina Soares  	| Alteração da tabela med_submeta_medicao
*                                   	| nos campos referentes às assinaturas da empresa
*                                   	| e do convenente na submeta 
*                                   	| (in_situacao_*, nr_cpf_responsavel_*, dt_assinatura_*)
* 
* 22/06/2020 | Gustavo Ferraz Diniz 	| Alteração da tabela med_medicao para adicionar
*                                   	| o campo in_bloqueio
*
* 19/08/2020 | Samuel Meira de Oliveira | Alteração da tabela med_documento_complementar para adicionar
*                                   	| os campos nm_licenca e in_eq_lic_inst
* 
*/

-------------------------------  Tabela: med_observacao_log_rec  -------------------------------

ALTER TABLE siconv.med_observacao_log_rec ADD in_bloqueio boolean NULL;

CREATE OR REPLACE FUNCTION siconv.med_observacao_trigger()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
   DECLARE wl_id integer;
   BEGIN
   -- Aqui temos um bloco IF que confirmará o tipo de operação.
   IF (TG_OP = 'INSERT') or (TG_OP = 'UPDATE') THEN
       INSERT INTO siconv.med_observacao_log_rec (
        versao,
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        dt_registro,
        entity_id,
        in_perfil_responsavel,
        medicaolog,
        nr_cpf_responsavel,
        tx_observacao,
        in_bloqueio
       ) VALUES (
        NEW.versao,
        nextval('siconv.med_observacao_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        NEW.adt_data_hora,
        NEW.adt_operacao,
        NEW.dt_registro,
        NEW.id,
        NEW.in_perfil_responsavel,
        NEW.medicao_fk,
        NEW.nr_cpf_responsavel,
        NEW.tx_observacao,
        NEW.in_bloqueio
       );
       RETURN NEW;
   -- Aqui temos um bloco IF que confirmará o tipo de operação DELETE.
   ELSIF (TG_OP = 'DELETE') THEN
       INSERT INTO siconv.med_observacao_log_rec (
        versao,
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        dt_registro,
        entity_id,
        in_perfil_responsavel,
        medicaolog,
        nr_cpf_responsavel,
        tx_observacao,
        in_bloqueio
       ) VALUES (
        OLD.versao,
        nextval('siconv.med_observacao_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        LOCALTIMESTAMP,
        TG_OP,
        OLD.dt_registro,
        OLD.id,
        OLD.in_perfil_responsavel,
        OLD.medicao_fk,
        OLD.nr_cpf_responsavel,
        OLD.tx_observacao,
        OLD.in_bloqueio
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $function$
;

-------------------------------  Tabela: med_anexo_log_rec  -------------------------------

ALTER TABLE siconv.med_anexo_log_rec ADD in_inativo boolean NULL;

CREATE OR REPLACE FUNCTION siconv.med_anexo_trigger()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
   DECLARE wl_id integer;
   BEGIN
   -- Aqui temos um bloco IF que confirmará o tipo de operação.
   IF (TG_OP = 'INSERT') or (TG_OP = 'UPDATE') THEN
       INSERT INTO siconv.med_anexo_log_rec (
        versao,
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
        NEW.versao,
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
        versao,
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
        OLD.versao,
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

-------------------------------  Tabela: med_submeta_medicao_log_rec  -------------------------------

ALTER TABLE siconv.med_submeta_medicao_log_rec RENAME COLUMN in_situacao TO in_situacao_assinatura;
ALTER TABLE siconv.med_submeta_medicao_log_rec ALTER COLUMN in_situacao_assinatura DROP NOT NULL;
ALTER TABLE siconv.med_submeta_medicao_log_rec ADD dt_ateste timestamp NULL;
ALTER TABLE siconv.med_submeta_medicao_log_rec ADD nr_cpf_resp_ateste bpchar(11) NULL;
ALTER TABLE siconv.med_submeta_medicao_log_rec ADD in_situacao_ateste varchar(3) NULL;

ALTER TABLE siconv.med_submeta_medicao_log_rec RENAME COLUMN in_situacao_assinatura TO in_situacao_empresa;
ALTER TABLE siconv.med_submeta_medicao_log_rec RENAME COLUMN nr_cpf_resp_assinatura TO nr_cpf_resp_empresa;
ALTER TABLE siconv.med_submeta_medicao_log_rec RENAME COLUMN dt_assinatura TO dt_assinatura_empresa;
ALTER TABLE siconv.med_submeta_medicao_log_rec RENAME COLUMN in_situacao_ateste TO in_situacao_convenente;
ALTER TABLE siconv.med_submeta_medicao_log_rec RENAME COLUMN nr_cpf_resp_ateste TO nr_cpf_resp_convenente;
ALTER TABLE siconv.med_submeta_medicao_log_rec RENAME COLUMN dt_ateste TO dt_assinatura_convenente;

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
        in_situacao_convenente
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
        NEW.in_situacao_convenente
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
        in_situacao_convenente
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
        OLD.in_situacao_convenente
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $function$
;

-------------------------------  Tabela: med_medicao_log_rec  -------------------------------

ALTER TABLE siconv.med_medicao_log_rec ADD medicaologagrupadora int8 NULL;
ALTER TABLE siconv.med_medicao_log_rec ADD in_bloqueio boolean NULL;

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
        in_bloqueio
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
        NEW.in_bloqueio
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
        in_bloqueio
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
        OLD.in_bloqueio
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $function$
;

-------------------------------  Tabela: med_doc_complementar_log_rec  -------------------------------

ALTER TABLE siconv.med_doc_complementar_log_rec ADD in_bloqueio boolean NULL;
ALTER TABLE siconv.med_doc_complementar_log_rec ADD nm_licenca varchar(200) NULL;
ALTER TABLE siconv.med_doc_complementar_log_rec ADD in_eq_lic_inst boolean NOT NULL DEFAULT false;

CREATE OR REPLACE FUNCTION siconv.med_doc_complementar_trigger()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
   DECLARE wl_id integer;
   BEGIN
   -- Aqui temos um bloco IF que confirmará o tipo de operação.
   IF (TG_OP = 'INSERT') or (TG_OP = 'UPDATE') THEN
       INSERT INTO siconv.med_doc_complementar_log_rec (
        versao,
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        co_ceph,
        dt_emissao,
        dt_validade,
        entity_id,
        in_tipo_documento,
        in_tipo_manifesto,
        medcontratolog,
        nm_arquivo,
        nm_orgao_emissor,
        nr_documento,
        tx_descricao,
        in_bloqueio,
        nm_licenca,
        in_eq_lic_inst
       ) VALUES (
        NEW.versao,
        nextval('siconv.med_doc_complementar_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        NEW.adt_data_hora,
        NEW.adt_operacao,
        NEW.co_ceph,
        NEW.dt_emissao,
        NEW.dt_validade,
        NEW.id,
        NEW.in_tipo_documento,
        NEW.in_tipo_manifesto,
        NEW.med_contrato_fk,
        NEW.nm_arquivo,
        NEW.nm_orgao_emissor,
        NEW.nr_documento,
        NEW.tx_descricao,
        NEW.in_bloqueio,
        NEW.nm_licenca,
        NEW.in_eq_lic_inst
       );
       RETURN NEW;
   -- Aqui temos um bloco IF que confirmará o tipo de operação DELETE.
   ELSIF (TG_OP = 'DELETE') THEN
       INSERT INTO siconv.med_doc_complementar_log_rec (
        versao,
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        co_ceph,
        dt_emissao,
        dt_validade,
        entity_id,
        in_tipo_documento,
        in_tipo_manifesto,
        medcontratolog,
        nm_arquivo,
        nm_orgao_emissor,
        nr_documento,
        tx_descricao,
        in_bloqueio,
        nm_licenca,
        in_eq_lic_inst
       ) VALUES (
        OLD.versao,
        nextval('siconv.med_doc_complementar_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        LOCALTIMESTAMP,
        TG_OP,
        OLD.co_ceph,
        OLD.dt_emissao,
        OLD.dt_validade,
        OLD.id,
        OLD.in_tipo_documento,
        OLD.in_tipo_manifesto,
        OLD.med_contrato_fk,
        OLD.nm_arquivo,
        OLD.nm_orgao_emissor,
        OLD.nr_documento,
        OLD.tx_descricao,
        OLD.in_bloqueio,
        OLD.nm_licenca,
        OLD.in_eq_lic_inst
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $function$
;

ALTER FUNCTION siconv.med_observacao_trigger() OWNER TO owner_siconv_p;
ALTER FUNCTION siconv.med_anexo_trigger() OWNER TO owner_siconv_p;
ALTER FUNCTION siconv.med_submeta_medicao_trigger() OWNER TO owner_siconv_p;
ALTER FUNCTION siconv.med_medicao_trigger() OWNER TO owner_siconv_p;   
ALTER FUNCTION siconv.med_doc_complementar_trigger() OWNER TO owner_siconv_p;
