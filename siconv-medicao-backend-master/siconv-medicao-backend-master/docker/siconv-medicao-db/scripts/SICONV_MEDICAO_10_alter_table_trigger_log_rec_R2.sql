/*
* ======================================== DADOS DO SCRIPT =================================================
* AUTOR: Fabiano Augusto Silva
* OBJETIVO: Alteracao das tabelas e triggers de controle de auditoria (i.e. log_rec) do SICONV MEDICAO
*           para a release 2
* PRE-REQUISITOS: Modelo da release 1 previamente aplicado
*
*
* ======================= HISTÓRICO DE ALTERAÇÕES ==========================================================
* DATA       | AUTOR                	| MOTIVO
* ----------------------------------------------------------------------------------------------------------
* 28/08/2020 | Fabiano Augusto Silva  	| Alteração da tabela med_anexo
*                                   	| para inclusão do campo nr_cpf_inativo.
*                                   	|
*                                   	|
*/

-------------------------------  Tabela: med_anexo_log_rec  -------------------------------

ALTER TABLE siconv.med_anexo_log_rec ADD nr_cpf_inativo varchar(11) NULL;

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
        in_inativo,
        nr_cpf_inativo
       ) VALUES (
        nextval('siconv.med_anexo_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        NEW.adt_data_hora,
        NEW.adt_operacao,
        NEW.co_ceph,
        NEW.id,
        NEW.nm_arquivo,
        NEW.observacao_fk,
        NEW.in_inativo,
	NEW.nr_cpf_inativo
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
        in_inativo,
	nr_cpf_inativo
       ) VALUES (
        nextval('siconv.med_anexo_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        LOCALTIMESTAMP,
        TG_OP,
        OLD.co_ceph,
        OLD.id,
        OLD.nm_arquivo,
        OLD.observacao_fk,
        OLD.in_inativo,
        OLD.nr_cpf_inativo        
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $function$
;

ALTER FUNCTION siconv.med_anexo_trigger() OWNER TO owner_siconv_p;
