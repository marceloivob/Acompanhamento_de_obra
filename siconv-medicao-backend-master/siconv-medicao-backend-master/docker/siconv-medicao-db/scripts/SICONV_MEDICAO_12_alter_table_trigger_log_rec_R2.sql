/*
* ======================================== DADOS DO SCRIPT =================================================
* AUTOR: Erison
* OBJETIVO: Alteracao das tabelas e triggers de controle de auditoria (i.e. log_rec) do SICONV MEDICAO
*           para a release 2
* PRE-REQUISITOS: Modelo da release 1 previamente aplicado
*
*
* ======================= HISTÓRICO DE ALTERAÇÕES ==========================================================
* DATA       | AUTOR                	| MOTIVO
* ----------------------------------------------------------------------------------------------------------
* 08/09/2020 | Erison Galvão		  	| Alteração da med_doc_complementar
*                                   	| renomeando a coluna nm_licenca para tx_descricao_outros
* 
*/

-------------------------------  Tabela: med_doc_complementar_log_rec  -------------------------------

ALTER TABLE siconv.med_doc_complementar_log_rec RENAME COLUMN nm_licenca TO tx_descricao_outros;

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
        tx_descricao_outros,
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
        NEW.tx_descricao_outros,
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
        tx_descricao_outros,
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
        OLD.tx_descricao_outros,
        OLD.in_eq_lic_inst
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $function$
;

ALTER FUNCTION siconv.med_doc_complementar_trigger() OWNER TO owner_siconv_p;
