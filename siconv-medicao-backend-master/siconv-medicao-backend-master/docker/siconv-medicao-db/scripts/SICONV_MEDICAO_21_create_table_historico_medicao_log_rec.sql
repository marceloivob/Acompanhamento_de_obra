/*
* ================================== DADOS DO SCRIPT ================================================
* AUTOR: Erison Galvão
* OBJETIVO: Criação da tabela historico_medicao_log_rec, trigger med_historico_medicao
* e add colunas de auditoria em historico_medicao
* DATA CRIACAO: 03/03/2021
* PRE-REQUISITOS: Modelo atual da release 4 previamente aplicado
*
* ============================ HISTÓRICO DE ALTERAÇÕES ==============================================
* DATA       | AUTOR          | MOTIVO
* ----------------------------------------------------------------------------------------------
* 14/03/2022  Ana Cristina  Ajuste do script para preenchimento dos campos de auditoria da tabela med_historico_medicao.
*
*/


-------------------------------  Início: med_historico_medicao  -------------------------------
ALTER TABLE siconv.med_historico_medicao
 	ADD	adt_login character varying,
    ADD	adt_data_hora timestamp,
    ADD	adt_operacao character varying(6),
	ADD	CONSTRAINT ck_med_historico_medicao_adt_operacao CHECK (adt_operacao::text = ANY (ARRAY['INSERT'::character varying::text, 'UPDATE'::character varying::text, 'DELETE'::character varying::text]))
;

COMMENT ON COLUMN siconv.med_historico_medicao.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN siconv.med_historico_medicao.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN siconv.med_historico_medicao.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';

UPDATE SICONV.MED_HISTORICO_MEDICAO 
  SET ADT_LOGIN = NR_CPF_RESPONSAVEL, 
    ADT_DATA_HORA = LOCALTIMESTAMP, 
    ADT_OPERACAO = 'INSERT';

ALTER TABLE siconv.med_historico_medicao ALTER COLUMN adt_login SET NOT NULL;
ALTER TABLE siconv.med_historico_medicao ALTER COLUMN adt_data_hora SET NOT NULL;
ALTER TABLE siconv.med_historico_medicao ALTER COLUMN adt_operacao SET NOT NULL;    

-------------------------------  Fim: med_historico_medicao  -------------------------------






-------------------------------  Início: med_historico_medicao_log_rec  -------------------------------


-------------------------------  Sequence: med_historico_medicao_log_rec  -------------------------------
CREATE SEQUENCE siconv.med_historico_medicao_log_rec_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;


-------------------------------  Tabela: med_historico_medicao_log_rec  -------------------------------
CREATE TABLE siconv.med_historico_medicao_log_rec
(
  id bigint NOT NULL DEFAULT nextval('siconv.med_historico_medicao_log_rec_seq'::regclass), -- Coluna de id.
  entity_id bigint NOT NULL, 
  medcontratolog bigint NOT NULL, 
  nr_cpf_responsavel bpchar(11) NOT NULL,
  in_perfil_responsavel character varying (3) NOT NULL,  
  nr_sequencial smallint NOT NULL,
  in_situacao varchar(3) NOT NULL, 
  dt_registro timestamp NOT NULL, 
  adt_login character varying(60),
  adt_data_hora timestamp without time zone,
  adt_operacao character varying(6),
  CONSTRAINT med_historico_medicao_log_rec_pk PRIMARY KEY (id),
  CONSTRAINT ck_med_historico_medicao_adt_operacao CHECK (adt_operacao::text = ANY (ARRAY['INSERT'::character varying::text, 'UPDATE'::character varying::text, 'DELETE'::character varying::text]))
);

-------------------------------  Owner: med_historico_medicao_log_rec  -------------------------------
ALTER SEQUENCE siconv.med_historico_medicao_log_rec_seq OWNER TO owner_siconv_p;
ALTER TABLE siconv.med_historico_medicao_log_rec OWNER TO owner_siconv_p;


-------------------------------  Comentários: med_historico_medicao_log_rec  -------------------------------
COMMENT ON TABLE siconv.med_historico_medicao_log_rec IS 'Tabela que registra o log do historico de medições de um contrato de execução';
COMMENT ON COLUMN siconv.med_historico_medicao_log_rec.id IS 'Identificador único de um Registro de Log na tabela de Histórico de Medições';
COMMENT ON COLUMN siconv.med_historico_medicao_log_rec.entity_id IS 'Identificador único do Histórico de Medições';
COMMENT ON COLUMN siconv.med_historico_medicao_log_rec.medcontratolog IS 'FK da tabela Contrato do módulo Medição';
COMMENT ON COLUMN siconv.med_historico_medicao_log_rec.nr_cpf_responsavel IS 'CPF do usuário responsável pela medicao';
COMMENT ON COLUMN siconv.med_historico_medicao_log_rec.in_perfil_responsavel IS 'Perfil do usuário responsável pela medicao';
COMMENT ON COLUMN siconv.med_historico_medicao_log_rec.nr_sequencial IS 'Número Sequencial da Medição';
COMMENT ON COLUMN siconv.med_historico_medicao_log_rec.in_situacao IS 'Indicador da Situação da Medição';
COMMENT ON COLUMN siconv.med_historico_medicao_log_rec.dt_registro IS 'Data/Hora de criação do registro';
COMMENT ON COLUMN siconv.med_historico_medicao_log_rec.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN siconv.med_historico_medicao_log_rec.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN siconv.med_historico_medicao_log_rec.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';

 -------------------------------  Grants: med_historico_medicao_log_rec  -------------------------------
GRANT ALL ON SEQUENCE siconv.med_historico_medicao_log_rec_seq TO owner_siconv_p;
grant select,usage on siconv.med_historico_medicao_log_rec_seq to usr_siconv_p;

GRANT ALL ON TABLE siconv.med_historico_medicao_log_rec TO owner_siconv_p;
grant select, insert, update, delete, references on siconv.med_historico_medicao_log_rec to usr_siconv_p;

-------------------------------  Fim: med_historico_medicao_log_rec  ------------------------------- 
 
 
-------------------------------  Início Trigger: med_historico_medicao_trigger  -------------------------------
CREATE OR REPLACE FUNCTION siconv.med_historico_medicao_trigger()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
   DECLARE wl_id integer;
   BEGIN
   -- Aqui temos um bloco IF que confirmará o tipo de operação.
   IF (TG_OP = 'INSERT') or (TG_OP = 'UPDATE') THEN
       INSERT INTO siconv.med_historico_medicao_log_rec (
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        entity_id,
        medcontratolog,
		nr_cpf_responsavel,
		in_perfil_responsavel,
        nr_sequencial,
        in_situacao,
		dt_registro
       ) VALUES (
        nextval('siconv.med_historico_medicao_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        NEW.adt_data_hora,
        NEW.adt_operacao,
        NEW.id,
        NEW.med_contrato_fk,
        NEW.nr_cpf_responsavel,
        NEW.in_perfil_responsavel,
        NEW.nr_sequencial,
        NEW.in_situacao,
        NEW.dt_registro
       );
       RETURN NEW;
   -- Aqui temos um bloco IF que confirmará o tipo de operação DELETE.
   ELSIF (TG_OP = 'DELETE') THEN
       INSERT INTO siconv.med_historico_medicao_log_rec (
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        entity_id,
        medcontratolog,
        nr_cpf_responsavel,
        in_perfil_responsavel,
        nr_sequencial,
        in_situacao,
        dt_registro
       ) VALUES (
        nextval('siconv.med_historico_medicao_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        LOCALTIMESTAMP,
        TG_OP,
        OLD.id,
        OLD.med_contrato_fk,
        OLD.nr_cpf_responsavel,
        OLD.in_perfil_responsavel,
        OLD.nr_sequencial,
        OLD.in_situacao,
        OLD.dt_registro
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $function$
;

-------------------------------  Owner Trigger: med_historico_medicao_trigger  -------------------------------
ALTER FUNCTION siconv.med_historico_medicao_trigger() OWNER TO owner_siconv_p;   


-------------------------------  Trigger: tg_med_historico_medicao  ----------------------------------------
 CREATE TRIGGER tg_med_historico_medicao
   AFTER INSERT OR UPDATE OR DELETE
   ON siconv.med_historico_medicao
   FOR EACH ROW
   EXECUTE PROCEDURE siconv.med_historico_medicao_trigger(); 

-------------------------------  Fim Trigger: med_item_historico_trigger  -------------------------------  

  



