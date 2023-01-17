/*
* ================================= DADOS DO SCRIPT ==================================================
* AUTOR: Gustavo Diniz
* OBJETIVO: Alteracao da tabela med_medicao para adicionar coluna opcional in_complementacao_valor [BOOLEAN],
*           referente a evolucao do modelo para a Release 6 do Medicao.
* DATA CRIACAO: 16/03/2022
* PRE-REQUISITOS: Modelo atual da release 5 previamente aplicado
*/

-------------------------------  Tabela: med_medicao  -------------------------------
ALTER TABLE siconv.med_medicao ADD in_complementacao_valor bool NULL;

COMMENT ON COLUMN siconv.med_medicao.in_complementacao_valor IS 'Indica se a medição é proveniente de um processo de complementação que permite alteração de valores ou não';

-------------------------------  Tabela log_rec: med_medicao_log_rec  -------------------------------
ALTER TABLE siconv.med_medicao_log_rec ADD in_complementacao_valor bool NULL;

COMMENT ON COLUMN siconv.med_medicao_log_rec.in_complementacao_valor IS 'Indica se a medição é proveniente de um processo de complementação que permite alteração de valores ou não';

-------------------------------  Trigger: med_medicao_trigger -------------------------------

CREATE OR REPLACE FUNCTION siconv.med_medicao_trigger()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
   DECLARE wl_id integer;
   BEGIN
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
        in_solicitante_vistoria,
        in_complementacao_valor
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
        NEW.in_solicitante_vistoria,
        NEW.in_complementacao_valor
       );
       RETURN NEW;
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
        in_solicitante_vistoria,
        in_complementacao_valor
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
        OLD.in_solicitante_vistoria,
        OLD.in_complementacao_valor
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $function$
;

ALTER FUNCTION siconv.med_medicao_trigger() OWNER TO owner_siconv_p;
