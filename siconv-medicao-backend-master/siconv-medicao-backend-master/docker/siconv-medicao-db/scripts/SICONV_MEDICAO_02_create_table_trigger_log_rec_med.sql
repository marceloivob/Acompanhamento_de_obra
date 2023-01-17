/*********************************************************************************************************
 * SERVICO FEDERAL DE PROCESSAMENTO DE DADOS - SERPRO
 * SUPERINTENDENCIA DE DIGITALIZACAO DE GOVERNO - SUPDG
 * POLO DE DESENVOLVIMENTO EM RECIFE - DGGOV/DGGO4
 * 
 * ========================================= DADOS DO SCRIPT =============================================
 * DATA DE CRIACAO: 23-08-2019
 * OBJETIVO: Criacao das tabelas e triggers para controle de auditoria (i.e. log_rec) do MEDICAO
 * PRE-REQUISITOS: Tabelas de negocio do Siconv Medicao criadas
 * 
 * ======================================= Historico de Execucoes ========================================
 * DATA | EXECUTOR | TAREFA | IP SERVIDOR | BANCO DE DADOS
 * -------------------------------------------------------------------------------------------------------
 * 23-08-2019 | GUSTAVO DINIZ | XXXXXX | 10.139.34.42 | siconv_mandatarias_desenv
 *********************************************************************************************************/

-------------------------------  Drop log_rec -------------------------------

-- Drop triggers

-- DROP TRIGGER tg_med_anotacao_registro_rt ON siconv.med_anotacao_registro_rt;
-- DROP TRIGGER tg_med_contrato ON siconv.med_contrato;
-- DROP TRIGGER tg_med_submeta_medicao ON siconv.med_submeta_medicao;
-- DROP TRIGGER tg_med_responsavel_tecnico ON siconv.med_responsavel_tecnico;
-- DROP TRIGGER tg_med_item_medicao ON siconv.med_item_medicao;
-- DROP TRIGGER tg_med_anotacao_registro_rt_submeta ON siconv.med_anotacao_registro_rt_submeta;
-- DROP TRIGGER tg_med_anexo ON siconv.med_anexo;
-- DROP TRIGGER tg_med_contrato_resp_tecnico ON siconv.med_contrato_resp_tecnico;
-- DROP TRIGGER tg_med_registro_profissional ON siconv.med_registro_profissional;
-- DROP TRIGGER tg_med_medicao ON siconv.med_medicao;
-- DROP TRIGGER tg_med_observacao ON siconv.med_observacao;
-- DROP TRIGGER tg_med_doc_complementar ON siconv.med_doc_complementar;
-- DROP TRIGGER tg_med_doc_complementar_submeta ON siconv.med_doc_complementar_submeta;
-- DROP TRIGGER tg_med_contrato_resp_tecnico_social ON siconv.med_contrato_resp_tecnico_social;
-- DROP TRIGGER tg_med_contrato_rt_social_submeta ON siconv.med_contrato_rt_social_submeta;

-- Drop functions

-- DROP FUNCTION siconv.med_anotacao_registro_rt_trigger();
-- DROP FUNCTION siconv.med_contrato_trigger();
-- DROP FUNCTION siconv.med_submeta_medicao_trigger();
-- DROP FUNCTION siconv.med_responsavel_tecnico_trigger();
-- DROP FUNCTION siconv.med_item_medicao_trigger();
-- DROP FUNCTION siconv.med_anotacao_registro_rt_submeta_trigger();
-- DROP FUNCTION siconv.med_anexo_trigger();
-- DROP FUNCTION siconv.med_contrato_resp_tecnico_trigger();
-- DROP FUNCTION siconv.med_registro_profissional_trigger();
-- DROP FUNCTION siconv.med_medicao_trigger();
-- DROP FUNCTION siconv.med_observacao_trigger();
-- DROP FUNCTION siconv.med_doc_complementar_trigger();
-- DROP FUNCTION siconv.med_doc_complementar_submeta_trigger();
-- DROP FUNCTION siconv.med_contrato_resp_tecnico_social_trigger();
-- DROP FUNCTION siconv.med_contrato_rt_social_submeta_trigger();

-- Drop  tables

-- DROP TABLE siconv.med_anotacao_registro_rt_log_rec;
-- DROP TABLE siconv.med_contrato_log_rec;
-- DROP TABLE siconv.med_submeta_medicao_log_rec;
-- DROP TABLE siconv.med_responsavel_tecnico_log_rec;
-- DROP TABLE siconv.med_item_medicao_log_rec;
-- DROP TABLE siconv.med_anotacao_registro_rt_submeta_log_rec;
-- DROP TABLE siconv.med_anexo_log_rec;
-- DROP TABLE siconv.med_contrato_resp_tecnico_log_rec;
-- DROP TABLE siconv.med_registro_profissional_log_rec;
-- DROP TABLE siconv.med_medicao_log_rec;
-- DROP TABLE siconv.med_observacao_log_rec;
-- DROP TABLE siconv.med_doc_complementar_submeta_log_rec;
-- DROP TABLE siconv.med_doc_complementar_log_rec;
-- DROP TABLE siconv.med_contrato_rt_social_submeta_log_rec;
-- DROP TABLE siconv.med_contrato_resp_tecnico_social_log_rec;


-- Drop sequences

-- DROP SEQUENCE siconv.med_anotacao_registro_rt_log_rec_seq;
-- DROP SEQUENCE siconv.med_contrato_log_rec_seq;
-- DROP SEQUENCE siconv.med_submeta_medicao_log_rec_seq;
-- DROP SEQUENCE siconv.med_responsavel_tecnico_log_rec_seq;
-- DROP SEQUENCE siconv.med_item_medicao_log_rec_seq;
-- DROP SEQUENCE siconv.med_anotacao_registro_rt_submeta_log_rec_seq;
-- DROP SEQUENCE siconv.med_anexo_log_rec_seq;
-- DROP SEQUENCE siconv.med_contrato_resp_tecnico_log_rec_seq;
-- DROP SEQUENCE siconv.med_registro_profissional_log_rec_seq;
-- DROP SEQUENCE siconv.med_medicao_log_rec_seq;
-- DROP SEQUENCE siconv.med_observacao_log_rec_seq;
-- DROP SEQUENCE siconv.med_doc_complementar_submeta_log_rec_seq;
-- DROP SEQUENCE siconv.med_doc_complementar_log_rec_seq;
-- DROP SEQUENCE siconv.med_contrato_resp_tecnico_social_log_rec_seq;
-- DROP SEQUENCE siconv.med_contrato_rt_social_submeta_log_rec_seq;


-- Sequence: siconv.med_anotacao_registro_rt_log_rec_seq



CREATE SEQUENCE siconv.med_anotacao_registro_rt_log_rec_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;




-- Table: siconv.med_anotacao_registro_rt_log_rec


CREATE TABLE siconv.med_anotacao_registro_rt_log_rec
(
  id bigint NOT NULL DEFAULT nextval('siconv.med_anotacao_registro_rt_log_rec_seq'::regclass), -- Coluna de id.
  co_ceph character varying (1024) NOT NULL, 
  dt_emissao date NOT NULL, 
  dt_inativacao date, 
  entity_id bigint NOT NULL, 
  in_tipo character varying (3) NOT NULL, 
  medcontratoresptecnicolog bigint NOT NULL, 
  nm_arquivo character varying (100) NOT NULL, 
  nr_art_rrt character varying (50) NOT NULL, 
  versao bigint,
  adt_login character varying(60),
  adt_data_hora timestamp without time zone,
  adt_operacao character varying(6),
  CONSTRAINT med_anotacao_registro_rt_log_rec_pkey PRIMARY KEY (id),
  CONSTRAINT ck_med_anotacao_registro_rt_adt_operacao CHECK (adt_operacao::text = ANY (ARRAY['INSERT'::character varying::text, 'UPDATE'::character varying::text, 'DELETE'::character varying::text]))
)
WITH (
  OIDS=FALSE
);
COMMENT ON TABLE siconv.med_anotacao_registro_rt_log_rec
  IS 'Tabela que representa ????';
COMMENT ON COLUMN siconv.med_anotacao_registro_rt_log_rec.id IS 'Coluna de id.';


 CREATE OR REPLACE FUNCTION siconv.med_anotacao_registro_rt_trigger()
   RETURNS trigger AS 
 $BODY$
   DECLARE wl_id integer;
   BEGIN
   -- Aqui temos um bloco IF que confirmará o tipo de operação.
   IF (TG_OP = 'INSERT') or (TG_OP = 'UPDATE') THEN
       INSERT INTO siconv.med_anotacao_registro_rt_log_rec (
        versao,
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        co_ceph,
        dt_emissao,
        dt_inativacao,
        entity_id,
        in_tipo,
        medcontratoresptecnicolog,
        nm_arquivo,
        nr_art_rrt
       ) VALUES (
        NEW.versao,
        nextval('siconv.med_anotacao_registro_rt_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        NEW.adt_data_hora,
        NEW.adt_operacao,
        NEW.co_ceph,
        NEW.dt_emissao,
        NEW.dt_inativacao,
        NEW.id,
        NEW.in_tipo,
        NEW.med_contrato_resp_tecnico_fk,
        NEW.nm_arquivo,
        NEW.nr_art_rrt
       );
       RETURN NEW;
   -- Aqui temos um bloco IF que confirmará o tipo de operação DELETE.
   ELSIF (TG_OP = 'DELETE') THEN
       INSERT INTO siconv.med_anotacao_registro_rt_log_rec (
        versao,
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        co_ceph,
        dt_emissao,
        dt_inativacao,
        entity_id,
        in_tipo,
        medcontratoresptecnicolog,
        nm_arquivo,
        nr_art_rrt
       ) VALUES (
        OLD.versao,
        nextval('siconv.med_anotacao_registro_rt_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        LOCALTIMESTAMP,
        TG_OP,
        OLD.co_ceph,
        OLD.dt_emissao,
        OLD.dt_inativacao,
        OLD.id,
        OLD.in_tipo,
        OLD.med_contrato_resp_tecnico_fk,
        OLD.nm_arquivo,
        OLD.nr_art_rrt
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $BODY$
   LANGUAGE plpgsql VOLATILE
   COST 100;
 
 
 -- Trigger: siconv.tg_med_anotacao_registro_rt on siconv.med_anotacao_registro_rt
 CREATE TRIGGER tg_med_anotacao_registro_rt
   AFTER INSERT OR UPDATE OR DELETE
   ON siconv.med_anotacao_registro_rt
   FOR EACH ROW
   EXECUTE PROCEDURE siconv.med_anotacao_registro_rt_trigger(); 


-- Sequence: siconv.med_contrato_log_rec_seq



CREATE SEQUENCE siconv.med_contrato_log_rec_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;




-- Table: siconv.med_contrato_log_rec


CREATE TABLE siconv.med_contrato_log_rec
(
  id bigint NOT NULL DEFAULT nextval('siconv.med_contrato_log_rec_seq'::regclass), -- Coluna de id.
  cnpj_fornecedor character varying (255), 
  contrato_fk bigint NOT NULL, 
  dt_inicio_obra date, 
  entity_id bigint NOT NULL, 
  in_social boolean NOT NULL, 
  proposta_fk bigint NOT NULL, 
  versao bigint,
  adt_login character varying(60),
  adt_data_hora timestamp without time zone,
  adt_operacao character varying(6),
  CONSTRAINT med_contrato_log_rec_pkey PRIMARY KEY (id),
  CONSTRAINT ck_med_contrato_adt_operacao CHECK (adt_operacao::text = ANY (ARRAY['INSERT'::character varying::text, 'UPDATE'::character varying::text, 'DELETE'::character varying::text]))
)
WITH (
  OIDS=FALSE
);
COMMENT ON TABLE siconv.med_contrato_log_rec
  IS 'Tabela que representa ????';
COMMENT ON COLUMN siconv.med_contrato_log_rec.id IS 'Coluna de id.';


 CREATE OR REPLACE FUNCTION siconv.med_contrato_trigger()
   RETURNS trigger AS 
 $BODY$
   DECLARE wl_id integer;
   BEGIN
   -- Aqui temos um bloco IF que confirmará o tipo de operação.
   IF (TG_OP = 'INSERT') or (TG_OP = 'UPDATE') THEN
       INSERT INTO siconv.med_contrato_log_rec (
        versao,
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
        NEW.versao,
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
        versao,
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
        OLD.versao,
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
   $BODY$
   LANGUAGE plpgsql VOLATILE
   COST 100;


 -- Trigger: siconv.tg_med_contrato on siconv.med_contrato
 CREATE TRIGGER tg_med_contrato
   AFTER INSERT OR UPDATE OR DELETE
   ON siconv.med_contrato
   FOR EACH ROW
   EXECUTE PROCEDURE siconv.med_contrato_trigger(); 


-- Sequence: siconv.med_submeta_medicao_log_rec_seq



CREATE SEQUENCE siconv.med_submeta_medicao_log_rec_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;




-- Table: siconv.med_submeta_medicao_log_rec


CREATE TABLE siconv.med_submeta_medicao_log_rec
(
  id bigint NOT NULL DEFAULT nextval('siconv.med_submeta_medicao_log_rec_seq'::regclass), -- Coluna de id.
  dt_assinatura timestamp, 
  entity_id bigint NOT NULL, 
  in_situacao character varying (3) NOT NULL, 
  medicaolog bigint NOT NULL, 
  nr_cpf_resp_assinatura character varying (11), 
  vrpl_submeta_fk bigint NOT NULL, 
  versao bigint,
  adt_login character varying(60),
  adt_data_hora timestamp without time zone,
  adt_operacao character varying(6),
  CONSTRAINT med_submeta_medicao_log_rec_pkey PRIMARY KEY (id),
  CONSTRAINT ck_med_submeta_medicao_adt_operacao CHECK (adt_operacao::text = ANY (ARRAY['INSERT'::character varying::text, 'UPDATE'::character varying::text, 'DELETE'::character varying::text]))
)
WITH (
  OIDS=FALSE
);
COMMENT ON TABLE siconv.med_submeta_medicao_log_rec
  IS 'Tabela que representa ????';
COMMENT ON COLUMN siconv.med_submeta_medicao_log_rec.id IS 'Coluna de id.';


 CREATE OR REPLACE FUNCTION siconv.med_submeta_medicao_trigger()
   RETURNS trigger AS 
 $BODY$
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
        dt_assinatura,
        entity_id,
        in_situacao,
        medicaolog,
        nr_cpf_resp_assinatura,
        vrpl_submeta_fk
       ) VALUES (
        NEW.versao,
        nextval('siconv.med_submeta_medicao_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        NEW.adt_data_hora,
        NEW.adt_operacao,
        NEW.dt_assinatura,
        NEW.id,
        NEW.in_situacao,
        NEW.medicao_fk,
        NEW.nr_cpf_resp_assinatura,
        NEW.vrpl_submeta_fk
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
        dt_assinatura,
        entity_id,
        in_situacao,
        medicaolog,
        nr_cpf_resp_assinatura,
        vrpl_submeta_fk
       ) VALUES (
        OLD.versao,
        nextval('siconv.med_submeta_medicao_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        LOCALTIMESTAMP,
        TG_OP,
        OLD.dt_assinatura,
        OLD.id,
        OLD.in_situacao,
        OLD.medicao_fk,
        OLD.nr_cpf_resp_assinatura,
        OLD.vrpl_submeta_fk
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $BODY$
   LANGUAGE plpgsql VOLATILE
   COST 100;
 
 
 -- Trigger: siconv.tg_med_submeta_medicao on siconv.med_submeta_medicao
 CREATE TRIGGER tg_med_submeta_medicao
   AFTER INSERT OR UPDATE OR DELETE
   ON siconv.med_submeta_medicao
   FOR EACH ROW
   EXECUTE PROCEDURE siconv.med_submeta_medicao_trigger(); 


-- Sequence: siconv.med_responsavel_tecnico_log_rec_seq



CREATE SEQUENCE siconv.med_responsavel_tecnico_log_rec_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;




-- Table: siconv.med_responsavel_tecnico_log_rec


CREATE TABLE siconv.med_responsavel_tecnico_log_rec
(
  id bigint NOT NULL DEFAULT nextval('siconv.med_responsavel_tecnico_log_rec_seq'::regclass), -- Coluna de id.
  entity_id bigint NOT NULL, 
  nr_cpf character varying (11) NOT NULL, 
  telefone character varying (1024) NOT NULL, 
  versao bigint,
  adt_login character varying(60),
  adt_data_hora timestamp without time zone,
  adt_operacao character varying(6),
  CONSTRAINT med_responsavel_tecnico_log_rec_pkey PRIMARY KEY (id),
  CONSTRAINT ck_med_responsavel_tecnico_adt_operacao CHECK (adt_operacao::text = ANY (ARRAY['INSERT'::character varying::text, 'UPDATE'::character varying::text, 'DELETE'::character varying::text]))
)
WITH (
  OIDS=FALSE
);
COMMENT ON TABLE siconv.med_responsavel_tecnico_log_rec
  IS 'Tabela que representa ????';
COMMENT ON COLUMN siconv.med_responsavel_tecnico_log_rec.id IS 'Coluna de id.';


 CREATE OR REPLACE FUNCTION siconv.med_responsavel_tecnico_trigger()
   RETURNS trigger AS 
 $BODY$
   DECLARE wl_id integer;
   BEGIN
   -- Aqui temos um bloco IF que confirmará o tipo de operação.
   IF (TG_OP = 'INSERT') or (TG_OP = 'UPDATE') THEN
       INSERT INTO siconv.med_responsavel_tecnico_log_rec (
        versao,
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        entity_id,
        nr_cpf,
        telefone
       ) VALUES (
        NEW.versao,
        nextval('siconv.med_responsavel_tecnico_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        NEW.adt_data_hora,
        NEW.adt_operacao,
        NEW.id,
        NEW.nr_cpf,
        NEW.telefone
       );
       RETURN NEW;
   -- Aqui temos um bloco IF que confirmará o tipo de operação DELETE.
   ELSIF (TG_OP = 'DELETE') THEN
       INSERT INTO siconv.med_responsavel_tecnico_log_rec (
        versao,
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        entity_id,
        nr_cpf,
        telefone
       ) VALUES (
        OLD.versao,
        nextval('siconv.med_responsavel_tecnico_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        LOCALTIMESTAMP,
        TG_OP,
        OLD.id,
        OLD.nr_cpf,
        OLD.telefone
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $BODY$
   LANGUAGE plpgsql VOLATILE
   COST 100;
 
 
 -- Trigger: siconv.tg_med_responsavel_tecnico on siconv.med_responsavel_tecnico
 CREATE TRIGGER tg_med_responsavel_tecnico
   AFTER INSERT OR UPDATE OR DELETE
   ON siconv.med_responsavel_tecnico
   FOR EACH ROW
   EXECUTE PROCEDURE siconv.med_responsavel_tecnico_trigger(); 


-- Sequence: siconv.med_item_medicao_log_rec_seq



CREATE SEQUENCE siconv.med_item_medicao_log_rec_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;




-- Table: siconv.med_item_medicao_log_rec


CREATE TABLE siconv.med_item_medicao_log_rec
(
  id bigint NOT NULL DEFAULT nextval('siconv.med_item_medicao_log_rec_seq'::regclass), -- Coluna de id.
  entity_id bigint NOT NULL, 
  medcontratolog bigint NOT NULL, 
  medicaologconcedente bigint, 
  medicaologconvenente bigint, 
  medicaologempresa bigint, 
  vl_total_servicos NUMERIC(17,2) NOT NULL, 
  vrpl_evento_fk bigint NOT NULL, 
  vrpl_frente_obra_fk bigint NOT NULL, 
  vrpl_submeta_fk bigint NOT NULL, 
  versao bigint,
  adt_login character varying(60),
  adt_data_hora timestamp without time zone,
  adt_operacao character varying(6),
  CONSTRAINT med_item_medicao_log_rec_pkey PRIMARY KEY (id),
  CONSTRAINT ck_med_item_medicao_adt_operacao CHECK (adt_operacao::text = ANY (ARRAY['INSERT'::character varying::text, 'UPDATE'::character varying::text, 'DELETE'::character varying::text]))
)
WITH (
  OIDS=FALSE
);
COMMENT ON TABLE siconv.med_item_medicao_log_rec
  IS 'Tabela que representa ????';
COMMENT ON COLUMN siconv.med_item_medicao_log_rec.id IS 'Coluna de id.';


 CREATE OR REPLACE FUNCTION siconv.med_item_medicao_trigger()
   RETURNS trigger AS 
 $BODY$
   DECLARE wl_id integer;
   BEGIN
   -- Aqui temos um bloco IF que confirmará o tipo de operação.
   IF (TG_OP = 'INSERT') or (TG_OP = 'UPDATE') THEN
       INSERT INTO siconv.med_item_medicao_log_rec (
        versao,
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
        NEW.versao,
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
        versao,
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
        OLD.versao,
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
   $BODY$
   LANGUAGE plpgsql VOLATILE
   COST 100;
 
 
 -- Trigger: siconv.tg_med_item_medicao on siconv.med_item_medicao
 CREATE TRIGGER tg_med_item_medicao
   AFTER INSERT OR UPDATE OR DELETE
   ON siconv.med_item_medicao
   FOR EACH ROW
   EXECUTE PROCEDURE siconv.med_item_medicao_trigger(); 


-- Sequence: siconv.med_anotacao_registro_rt_submeta_log_rec_seq



CREATE SEQUENCE siconv.med_anotacao_registro_rt_submeta_log_rec_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;




-- Table: siconv.med_anotacao_registro_rt_submeta_log_rec


CREATE TABLE siconv.med_anotacao_registro_rt_submeta_log_rec
(
  id bigint NOT NULL DEFAULT nextval('siconv.med_anotacao_registro_rt_submeta_log_rec_seq'::regclass), -- Coluna de id.
  entity_id bigint NOT NULL, 
  medanotacaoregistrortlog bigint NOT NULL, 
  vrpl_submeta_fk bigint NOT NULL, 
  versao bigint,
  adt_login character varying(60),
  adt_data_hora timestamp without time zone,
  adt_operacao character varying(6),
  CONSTRAINT med_anotacao_registro_rt_submeta_log_rec_pkey PRIMARY KEY (id),
  CONSTRAINT ck_med_anotacao_registro_rt_submeta_adt_operacao CHECK (adt_operacao::text = ANY (ARRAY['INSERT'::character varying::text, 'UPDATE'::character varying::text, 'DELETE'::character varying::text]))
)
WITH (
  OIDS=FALSE
);
COMMENT ON TABLE siconv.med_anotacao_registro_rt_submeta_log_rec
  IS 'Tabela que representa ????';
COMMENT ON COLUMN siconv.med_anotacao_registro_rt_submeta_log_rec.id IS 'Coluna de id.';


 CREATE OR REPLACE FUNCTION siconv.med_anotacao_registro_rt_submeta_trigger()
   RETURNS trigger AS 
 $BODY$
   DECLARE wl_id integer;
   BEGIN
   -- Aqui temos um bloco IF que confirmará o tipo de operação.
   IF (TG_OP = 'INSERT') or (TG_OP = 'UPDATE') THEN
       INSERT INTO siconv.med_anotacao_registro_rt_submeta_log_rec (
        versao,
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        entity_id,
        medanotacaoregistrortlog,
        vrpl_submeta_fk
       ) VALUES (
        NEW.versao,
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
        versao,
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        entity_id,
        medanotacaoregistrortlog,
        vrpl_submeta_fk
       ) VALUES (
        OLD.versao,
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
   $BODY$
   LANGUAGE plpgsql VOLATILE
   COST 100;
 
 
 -- Trigger: siconv.tg_med_anotacao_registro_rt_submeta on siconv.med_anotacao_registro_rt_submeta
 CREATE TRIGGER tg_med_anotacao_registro_rt_submeta
   AFTER INSERT OR UPDATE OR DELETE
   ON siconv.med_anotacao_registro_rt_submeta
   FOR EACH ROW
   EXECUTE PROCEDURE siconv.med_anotacao_registro_rt_submeta_trigger(); 


-- Sequence: siconv.med_anexo_log_rec_seq



CREATE SEQUENCE siconv.med_anexo_log_rec_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;




-- Table: siconv.med_anexo_log_rec


CREATE TABLE siconv.med_anexo_log_rec
(
  id bigint NOT NULL DEFAULT nextval('siconv.med_anexo_log_rec_seq'::regclass), -- Coluna de id.
  co_ceph character varying (1024) NOT NULL, 
  entity_id bigint NOT NULL, 
  nm_arquivo character varying (100) NOT NULL, 
  observacaolog bigint NOT NULL, 
  versao bigint,
  adt_login character varying(60),
  adt_data_hora timestamp without time zone,
  adt_operacao character varying(6),
  CONSTRAINT med_anexo_log_rec_pkey PRIMARY KEY (id),
  CONSTRAINT ck_med_anexo_adt_operacao CHECK (adt_operacao::text = ANY (ARRAY['INSERT'::character varying::text, 'UPDATE'::character varying::text, 'DELETE'::character varying::text]))
)
WITH (
  OIDS=FALSE
);
COMMENT ON TABLE siconv.med_anexo_log_rec
  IS 'Tabela que representa ????';
COMMENT ON COLUMN siconv.med_anexo_log_rec.id IS 'Coluna de id.';


 CREATE OR REPLACE FUNCTION siconv.med_anexo_trigger()
   RETURNS trigger AS 
 $BODY$
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
        observacaolog
       ) VALUES (
        NEW.versao,
        nextval('siconv.med_anexo_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        NEW.adt_data_hora,
        NEW.adt_operacao,
        NEW.co_ceph,
        NEW.id,
        NEW.nm_arquivo,
        NEW.observacao_fk
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
        observacaolog
       ) VALUES (
        OLD.versao,
        nextval('siconv.med_anexo_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        LOCALTIMESTAMP,
        TG_OP,
        OLD.co_ceph,
        OLD.id,
        OLD.nm_arquivo,
        OLD.observacao_fk
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $BODY$
   LANGUAGE plpgsql VOLATILE
   COST 100;
 
 
 -- Trigger: siconv.tg_med_anexo on siconv.med_anexo
 CREATE TRIGGER tg_med_anexo
   AFTER INSERT OR UPDATE OR DELETE
   ON siconv.med_anexo
   FOR EACH ROW
   EXECUTE PROCEDURE siconv.med_anexo_trigger(); 


-- Sequence: siconv.med_contrato_resp_tecnico_log_rec_seq



CREATE SEQUENCE siconv.med_contrato_resp_tecnico_log_rec_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;




-- Table: siconv.med_contrato_resp_tecnico_log_rec


CREATE TABLE siconv.med_contrato_resp_tecnico_log_rec
(
  id bigint NOT NULL DEFAULT nextval('siconv.med_contrato_resp_tecnico_log_rec_seq'::regclass), -- Coluna de id.
  dt_inclusao timestamp NOT NULL, 
  entity_id bigint NOT NULL, 
  in_tipo character varying (3) NOT NULL, 
  medcontratolog bigint NOT NULL, 
  medregistroprofissionallog bigint NOT NULL, 
  versao bigint,
  adt_login character varying(60),
  adt_data_hora timestamp without time zone,
  adt_operacao character varying(6),
  CONSTRAINT med_contrato_resp_tecnico_log_rec_pkey PRIMARY KEY (id),
  CONSTRAINT ck_med_contrato_resp_tecnico_adt_operacao CHECK (adt_operacao::text = ANY (ARRAY['INSERT'::character varying::text, 'UPDATE'::character varying::text, 'DELETE'::character varying::text]))
)
WITH (
  OIDS=FALSE
);
COMMENT ON TABLE siconv.med_contrato_resp_tecnico_log_rec
  IS 'Tabela que representa ????';
COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_log_rec.id IS 'Coluna de id.';


 CREATE OR REPLACE FUNCTION siconv.med_contrato_resp_tecnico_trigger()
   RETURNS trigger AS 
 $BODY$
   DECLARE wl_id integer;
   BEGIN
   -- Aqui temos um bloco IF que confirmará o tipo de operação.
   IF (TG_OP = 'INSERT') or (TG_OP = 'UPDATE') THEN
       INSERT INTO siconv.med_contrato_resp_tecnico_log_rec (
        versao,
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        dt_inclusao,
        entity_id,
        in_tipo,
        medcontratolog,
        medregistroprofissionallog
       ) VALUES (
        NEW.versao,
        nextval('siconv.med_contrato_resp_tecnico_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        NEW.adt_data_hora,
        NEW.adt_operacao,
        NEW.dt_inclusao,
        NEW.id,
        NEW.in_tipo,
        NEW.med_contrato_fk,
        NEW.med_registro_profissional_fk
       );
       RETURN NEW;
   -- Aqui temos um bloco IF que confirmará o tipo de operação DELETE.
   ELSIF (TG_OP = 'DELETE') THEN
       INSERT INTO siconv.med_contrato_resp_tecnico_log_rec (
        versao,
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        dt_inclusao,
        entity_id,
        in_tipo,
        medcontratolog,
        medregistroprofissionallog
       ) VALUES (
        OLD.versao,
        nextval('siconv.med_contrato_resp_tecnico_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        LOCALTIMESTAMP,
        TG_OP,
        OLD.dt_inclusao,
        OLD.id,
        OLD.in_tipo,
        OLD.med_contrato_fk,
        OLD.med_registro_profissional_fk
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $BODY$
   LANGUAGE plpgsql VOLATILE
   COST 100;
 
 
 -- Trigger: siconv.tg_med_contrato_resp_tecnico on siconv.med_contrato_resp_tecnico
 CREATE TRIGGER tg_med_contrato_resp_tecnico
   AFTER INSERT OR UPDATE OR DELETE
   ON siconv.med_contrato_resp_tecnico
   FOR EACH ROW
   EXECUTE PROCEDURE siconv.med_contrato_resp_tecnico_trigger(); 


-- Sequence: siconv.med_registro_profissional_log_rec_seq



CREATE SEQUENCE siconv.med_registro_profissional_log_rec_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;




-- Table: siconv.med_registro_profissional_log_rec


CREATE TABLE siconv.med_registro_profissional_log_rec
(
  id bigint NOT NULL DEFAULT nextval('siconv.med_registro_profissional_log_rec_seq'::regclass), -- Coluna de id.
  atividade character varying (3) NOT NULL, 
  crea_cau character varying (50) NOT NULL, 
  entity_id bigint NOT NULL, 
  medresponsaveltecnicolog bigint NOT NULL, 
  uf character varying (2) NOT NULL, 
  versao bigint,
  adt_login character varying(60),
  adt_data_hora timestamp without time zone,
  adt_operacao character varying(6),
  CONSTRAINT med_registro_profissional_log_rec_pkey PRIMARY KEY (id),
  CONSTRAINT ck_med_registro_profissional_adt_operacao CHECK (adt_operacao::text = ANY (ARRAY['INSERT'::character varying::text, 'UPDATE'::character varying::text, 'DELETE'::character varying::text]))
)
WITH (
  OIDS=FALSE
);
COMMENT ON TABLE siconv.med_registro_profissional_log_rec
  IS 'Tabela que representa ????';
COMMENT ON COLUMN siconv.med_registro_profissional_log_rec.id IS 'Coluna de id.';


 CREATE OR REPLACE FUNCTION siconv.med_registro_profissional_trigger()
   RETURNS trigger AS 
 $BODY$
   DECLARE wl_id integer;
   BEGIN
   -- Aqui temos um bloco IF que confirmará o tipo de operação.
   IF (TG_OP = 'INSERT') or (TG_OP = 'UPDATE') THEN
       INSERT INTO siconv.med_registro_profissional_log_rec (
        versao,
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        atividade,
        crea_cau,
        entity_id,
        medresponsaveltecnicolog,
        uf
       ) VALUES (
        NEW.versao,
        nextval('siconv.med_registro_profissional_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        NEW.adt_data_hora,
        NEW.adt_operacao,
        NEW.atividade,
        NEW.crea_cau,
        NEW.id,
        NEW.med_responsavel_tecnico_fk,
        NEW.uf
       );
       RETURN NEW;
   -- Aqui temos um bloco IF que confirmará o tipo de operação DELETE.
   ELSIF (TG_OP = 'DELETE') THEN
       INSERT INTO siconv.med_registro_profissional_log_rec (
        versao,
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        atividade,
        crea_cau,
        entity_id,
        medresponsaveltecnicolog,
        uf
       ) VALUES (
        OLD.versao,
        nextval('siconv.med_registro_profissional_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        LOCALTIMESTAMP,
        TG_OP,
        OLD.atividade,
        OLD.crea_cau,
        OLD.id,
        OLD.med_responsavel_tecnico_fk,
        OLD.uf
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $BODY$
   LANGUAGE plpgsql VOLATILE
   COST 100;
 
 
 -- Trigger: siconv.tg_med_registro_profissional on siconv.med_registro_profissional
 CREATE TRIGGER tg_med_registro_profissional
   AFTER INSERT OR UPDATE OR DELETE
   ON siconv.med_registro_profissional
   FOR EACH ROW
   EXECUTE PROCEDURE siconv.med_registro_profissional_trigger(); 


-- Sequence: siconv.med_medicao_log_rec_seq



CREATE SEQUENCE siconv.med_medicao_log_rec_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;




-- Table: siconv.med_medicao_log_rec


CREATE TABLE siconv.med_medicao_log_rec
(
  id bigint NOT NULL DEFAULT nextval('siconv.med_medicao_log_rec_seq'::regclass), -- Coluna de id.
  dt_fim date, 
  dt_inicio date NOT NULL, 
  entity_id bigint NOT NULL, 
  in_situacao character varying (3) NOT NULL, 
  medcontratolog bigint NOT NULL, 
  nr_sequencial smallint NOT NULL, 
  versao bigint,
  adt_login character varying(60),
  adt_data_hora timestamp without time zone,
  adt_operacao character varying(6),
  CONSTRAINT med_medicao_log_rec_pkey PRIMARY KEY (id),
  CONSTRAINT ck_med_medicao_adt_operacao CHECK (adt_operacao::text = ANY (ARRAY['INSERT'::character varying::text, 'UPDATE'::character varying::text, 'DELETE'::character varying::text]))
)
WITH (
  OIDS=FALSE
);
COMMENT ON TABLE siconv.med_medicao_log_rec
  IS 'Tabela que representa ????';
COMMENT ON COLUMN siconv.med_medicao_log_rec.id IS 'Coluna de id.';


 CREATE OR REPLACE FUNCTION siconv.med_medicao_trigger()
   RETURNS trigger AS 
 $BODY$
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
        nr_sequencial
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
        NEW.nr_sequencial
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
        nr_sequencial
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
        OLD.nr_sequencial
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $BODY$
   LANGUAGE plpgsql VOLATILE
   COST 100;
 
 
 -- Trigger: siconv.tg_med_medicao on siconv.med_medicao
 CREATE TRIGGER tg_med_medicao
   AFTER INSERT OR UPDATE OR DELETE
   ON siconv.med_medicao
   FOR EACH ROW
   EXECUTE PROCEDURE siconv.med_medicao_trigger(); 


-- Sequence: siconv.med_observacao_log_rec_seq



CREATE SEQUENCE siconv.med_observacao_log_rec_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;




-- Table: siconv.med_observacao_log_rec


CREATE TABLE siconv.med_observacao_log_rec
(
  id bigint NOT NULL DEFAULT nextval('siconv.med_observacao_log_rec_seq'::regclass), -- Coluna de id.
  dt_registro timestamp NOT NULL, 
  entity_id bigint NOT NULL, 
  in_perfil_responsavel character varying (3) NOT NULL, 
  medicaolog bigint NOT NULL, 
  nr_cpf_responsavel character varying (11) NOT NULL, 
  tx_observacao character varying (1000) NOT NULL, 
  versao bigint,
  adt_login character varying(60),
  adt_data_hora timestamp without time zone,
  adt_operacao character varying(6),
  CONSTRAINT med_observacao_log_rec_pkey PRIMARY KEY (id),
  CONSTRAINT ck_med_observacao_adt_operacao CHECK (adt_operacao::text = ANY (ARRAY['INSERT'::character varying::text, 'UPDATE'::character varying::text, 'DELETE'::character varying::text]))
)
WITH (
  OIDS=FALSE
);
COMMENT ON TABLE siconv.med_observacao_log_rec
  IS 'Tabela que representa ????';
COMMENT ON COLUMN siconv.med_observacao_log_rec.id IS 'Coluna de id.';


 CREATE OR REPLACE FUNCTION siconv.med_observacao_trigger()
   RETURNS trigger AS 
 $BODY$
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
        tx_observacao
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
        NEW.tx_observacao
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
        tx_observacao
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
        OLD.tx_observacao
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $BODY$
   LANGUAGE plpgsql VOLATILE
   COST 100;
 
 
 -- Trigger: siconv.tg_med_observacao on siconv.med_observacao
 CREATE TRIGGER tg_med_observacao
   AFTER INSERT OR UPDATE OR DELETE
   ON siconv.med_observacao
   FOR EACH ROW
   EXECUTE PROCEDURE siconv.med_observacao_trigger();



-- Sequence: siconv.med_doc_complementar_log_rec_seq

CREATE SEQUENCE siconv.med_doc_complementar_log_rec_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;


-- Table: siconv.med_doc_complementar_log_rec

-- DROP TABLE siconv.med_doc_complementar_log_rec;

CREATE TABLE siconv.med_doc_complementar_log_rec
(
  id bigint NOT NULL DEFAULT nextval('siconv.med_doc_complementar_log_rec_seq'::regclass), -- Coluna de id.
  co_ceph character varying (1024) NOT NULL, 
  dt_emissao date, 
  dt_validade date, 
  entity_id bigint NOT NULL, 
  in_tipo_documento character varying (3) NOT NULL, 
  in_tipo_manifesto character varying (3), 
  medcontratolog bigint NOT NULL, 
  nm_arquivo character varying (100) NOT NULL, 
  nm_orgao_emissor character varying (100), 
  nr_documento character varying (40), 
  tx_descricao character varying (100), 
  versao bigint,
  adt_login character varying(60),
  adt_data_hora timestamp without time zone,
  adt_operacao character varying(6),
  CONSTRAINT med_doc_complementar_log_rec_pkey PRIMARY KEY (id),
  CONSTRAINT ck_med_doc_complementar_adt_operacao CHECK (adt_operacao::text = ANY (ARRAY['INSERT'::character varying::text, 'UPDATE'::character varying::text, 'DELETE'::character varying::text]))
)
WITH (
  OIDS=FALSE
);
COMMENT ON TABLE siconv.med_doc_complementar_log_rec
  IS 'Tabela que representa ????';
COMMENT ON COLUMN siconv.med_doc_complementar_log_rec.id IS 'Coluna de id.';


 CREATE OR REPLACE FUNCTION siconv.med_doc_complementar_trigger()
   RETURNS trigger AS 
 $BODY$
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
        tx_descricao
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
        NEW.tx_descricao
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
        tx_descricao
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
        OLD.tx_descricao
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $BODY$
   LANGUAGE plpgsql VOLATILE
   COST 100;
 
 
 -- Trigger: siconv.tg_med_doc_complementar on siconv.med_doc_complementar
 CREATE TRIGGER tg_med_doc_complementar
   AFTER INSERT OR UPDATE OR DELETE
   ON siconv.med_doc_complementar
   FOR EACH ROW
   EXECUTE PROCEDURE siconv.med_doc_complementar_trigger(); 



-- Sequence: siconv.med_doc_complementar_submeta_log_rec_seq

CREATE SEQUENCE siconv.med_doc_complementar_submeta_log_rec_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;




-- Table: siconv.med_doc_complementar_submeta_log_rec


CREATE TABLE siconv.med_doc_complementar_submeta_log_rec
(
  id bigint NOT NULL DEFAULT nextval('siconv.med_doc_complementar_submeta_log_rec_seq'::regclass), -- Coluna de id.
  entity_id bigint NOT NULL, 
  meddoccomplementarlog bigint NOT NULL, 
  vrpl_submeta_fk bigint NOT NULL, 
  versao bigint,
  adt_login character varying(60),
  adt_data_hora timestamp without time zone,
  adt_operacao character varying(6),
  CONSTRAINT med_doc_complementar_submeta_log_rec_pkey PRIMARY KEY (id),
  CONSTRAINT ck_med_doc_complementar_submeta_adt_operacao CHECK (adt_operacao::text = ANY (ARRAY['INSERT'::character varying::text, 'UPDATE'::character varying::text, 'DELETE'::character varying::text]))
)
WITH (
  OIDS=FALSE
);
COMMENT ON TABLE siconv.med_doc_complementar_submeta_log_rec
  IS 'Tabela que representa ????';
COMMENT ON COLUMN siconv.med_doc_complementar_submeta_log_rec.id IS 'Coluna de id.';

 
 CREATE OR REPLACE FUNCTION siconv.med_doc_complementar_submeta_trigger()
   RETURNS trigger AS 
 $BODY$
   DECLARE wl_id integer;
   BEGIN
   -- Aqui temos um bloco IF que confirmará o tipo de operação.
   IF (TG_OP = 'INSERT') or (TG_OP = 'UPDATE') THEN
       INSERT INTO siconv.med_doc_complementar_submeta_log_rec (
        versao,
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        entity_id,
        meddoccomplementarlog,
        vrpl_submeta_fk
       ) VALUES (
        NEW.versao,
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
        versao,
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        entity_id,
        meddoccomplementarlog,
        vrpl_submeta_fk
       ) VALUES (
        OLD.versao,
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
   $BODY$
   LANGUAGE plpgsql VOLATILE
   COST 100;
 
 
 -- Trigger: siconv.tg_med_doc_complementar_submeta on siconv.med_doc_complementar_submeta
 CREATE TRIGGER tg_med_doc_complementar_submeta
   AFTER INSERT OR UPDATE OR DELETE
   ON siconv.med_doc_complementar_submeta
   FOR EACH ROW
   EXECUTE PROCEDURE siconv.med_doc_complementar_submeta_trigger(); 
   



-- Sequence: siconv.med_contrato_resp_tecnico_social_log_rec_seq

CREATE SEQUENCE siconv.med_contrato_resp_tecnico_social_log_rec_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;


-- Table: siconv.med_contrato_resp_tecnico_social_log_rec

CREATE TABLE siconv.med_contrato_resp_tecnico_social_log_rec
(
  id bigint NOT NULL DEFAULT nextval('siconv.med_contrato_resp_tecnico_social_log_rec_seq'::regclass), -- Coluna de id.
  co_ceph_curriculo character varying (1024) NOT NULL, 
  dt_inativacao timestamp, 
  dt_inclusao timestamp NOT NULL, 
  entity_id bigint NOT NULL, 
  in_atividade character varying (3) NOT NULL, 
  in_tipo character varying (3) NOT NULL, 
  medcontratolog bigint NOT NULL, 
  medresponsaveltecnicolog bigint NOT NULL, 
  nm_arquivo_curriculo character varying (100) NOT NULL, 
  nm_formacao character varying (100) NOT NULL, 
  nm_orgao_responsavel character varying (100), 
  nm_registro_profissional character varying (100), 
  nr_telefone_orgao character varying (15), 
  tx_email_orgao character varying (100), 
  versao bigint,
  adt_login character varying(60),
  adt_data_hora timestamp without time zone,
  adt_operacao character varying(6),
  CONSTRAINT med_contrato_resp_tecnico_social_log_rec_pkey PRIMARY KEY (id),
  CONSTRAINT ck_med_contrato_resp_tecnico_social_adt_operacao CHECK (adt_operacao::text = ANY (ARRAY['INSERT'::character varying::text, 'UPDATE'::character varying::text, 'DELETE'::character varying::text]))
)
WITH (
  OIDS=FALSE
);
COMMENT ON TABLE siconv.med_contrato_resp_tecnico_social_log_rec
  IS 'Tabela que representa ????';
COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_social_log_rec.id IS 'Coluna de id.';


 CREATE OR REPLACE FUNCTION siconv.med_contrato_resp_tecnico_social_trigger()
   RETURNS trigger AS 
 $BODY$
   DECLARE wl_id integer;
   BEGIN
   -- Aqui temos um bloco IF que confirmará o tipo de operação.
   IF (TG_OP = 'INSERT') or (TG_OP = 'UPDATE') THEN
       INSERT INTO siconv.med_contrato_resp_tecnico_social_log_rec (
        versao,
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        co_ceph_curriculo,
        dt_inativacao,
        dt_inclusao,
        entity_id,
        in_atividade,
        in_tipo,
        medcontratolog,
        medresponsaveltecnicolog,
        nm_arquivo_curriculo,
        nm_formacao,
        nm_orgao_responsavel,
        nm_registro_profissional,
        nr_telefone_orgao,
        tx_email_orgao
       ) VALUES (
        NEW.versao,
        nextval('siconv.med_contrato_resp_tecnico_social_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        NEW.adt_data_hora,
        NEW.adt_operacao,
        NEW.co_ceph_curriculo,
        NEW.dt_inativacao,
        NEW.dt_inclusao,
        NEW.id,
        NEW.in_atividade,
        NEW.in_tipo,
        NEW.med_contrato_fk,
        NEW.med_responsavel_tecnico_fk,
        NEW.nm_arquivo_curriculo,
        NEW.nm_formacao,
        NEW.nm_orgao_responsavel,
        NEW.nm_registro_profissional,
        NEW.nr_telefone_orgao,
        NEW.tx_email_orgao
       );
       RETURN NEW;
   -- Aqui temos um bloco IF que confirmará o tipo de operação DELETE.
   ELSIF (TG_OP = 'DELETE') THEN
       INSERT INTO siconv.med_contrato_resp_tecnico_social_log_rec (
        versao,
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        co_ceph_curriculo,
        dt_inativacao,
        dt_inclusao,
        entity_id,
        in_atividade,
        in_tipo,
        medcontratolog,
        medresponsaveltecnicolog,
        nm_arquivo_curriculo,
        nm_formacao,
        nm_orgao_responsavel,
        nm_registro_profissional,
        nr_telefone_orgao,
        tx_email_orgao
       ) VALUES (
        OLD.versao,
        nextval('siconv.med_contrato_resp_tecnico_social_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        LOCALTIMESTAMP,
        TG_OP,
        OLD.co_ceph_curriculo,
        OLD.dt_inativacao,
        OLD.dt_inclusao,
        OLD.id,
        OLD.in_atividade,
        OLD.in_tipo,
        OLD.med_contrato_fk,
        OLD.med_responsavel_tecnico_fk,
        OLD.nm_arquivo_curriculo,
        OLD.nm_formacao,
        OLD.nm_orgao_responsavel,
        OLD.nm_registro_profissional,
        OLD.nr_telefone_orgao,
        OLD.tx_email_orgao
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $BODY$
   LANGUAGE plpgsql VOLATILE
   COST 100;
 
 
 -- Trigger: siconv.tg_med_contrato_resp_tecnico_social on siconv.med_contrato_resp_tecnico_social
 CREATE TRIGGER tg_med_contrato_resp_tecnico_social
   AFTER INSERT OR UPDATE OR DELETE
   ON siconv.med_contrato_resp_tecnico_social
   FOR EACH ROW
   EXECUTE PROCEDURE siconv.med_contrato_resp_tecnico_social_trigger(); 




-- Sequence: siconv.med_contrato_rt_social_submeta_log_rec_seq

CREATE SEQUENCE siconv.med_contrato_rt_social_submeta_log_rec_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;


-- Table: siconv.med_contrato_rt_social_submeta_log_rec

CREATE TABLE siconv.med_contrato_rt_social_submeta_log_rec
(
  id bigint NOT NULL DEFAULT nextval('siconv.med_contrato_rt_social_submeta_log_rec_seq'::regclass), -- Coluna de id.
  entity_id bigint NOT NULL, 
  medcontratoresptecnicosociallog bigint NOT NULL, 
  vrpl_submeta_fk bigint NOT NULL, 
  versao bigint,
  adt_login character varying(60),
  adt_data_hora timestamp without time zone,
  adt_operacao character varying(6),
  CONSTRAINT med_contrato_rt_social_submeta_log_rec_pkey PRIMARY KEY (id),
  CONSTRAINT ck_med_contrato_rt_social_submeta_adt_operacao CHECK (adt_operacao::text = ANY (ARRAY['INSERT'::character varying::text, 'UPDATE'::character varying::text, 'DELETE'::character varying::text]))
)
WITH (
  OIDS=FALSE
);
COMMENT ON TABLE siconv.med_contrato_rt_social_submeta_log_rec
  IS 'Tabela que representa ????';
COMMENT ON COLUMN siconv.med_contrato_rt_social_submeta_log_rec.id IS 'Coluna de id.';


 CREATE OR REPLACE FUNCTION siconv.med_contrato_rt_social_submeta_trigger()
   RETURNS trigger AS 
 $BODY$
   DECLARE wl_id integer;
   BEGIN
   -- Aqui temos um bloco IF que confirmará o tipo de operação.
   IF (TG_OP = 'INSERT') or (TG_OP = 'UPDATE') THEN
       INSERT INTO siconv.med_contrato_rt_social_submeta_log_rec (
        versao,
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        entity_id,
        medcontratoresptecnicosociallog,
        vrpl_submeta_fk
       ) VALUES (
        NEW.versao,
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
        versao,
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        entity_id,
        medcontratoresptecnicosociallog,
        vrpl_submeta_fk
       ) VALUES (
        OLD.versao,
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
   $BODY$
   LANGUAGE plpgsql VOLATILE
   COST 100;
 
 
 -- Trigger: siconv.tg_med_contrato_rt_social_submeta on siconv.med_contrato_rt_social_submeta
 CREATE TRIGGER tg_med_contrato_rt_social_submeta
   AFTER INSERT OR UPDATE OR DELETE
   ON siconv.med_contrato_rt_social_submeta
   FOR EACH ROW
   EXECUTE PROCEDURE siconv.med_contrato_rt_social_submeta_trigger(); 





ALTER TABLE siconv.med_anotacao_registro_rt_log_rec_seq OWNER TO owner_siconv_p;
ALTER TABLE siconv.med_anotacao_registro_rt_log_rec OWNER TO owner_siconv_p;  
ALTER FUNCTION siconv.med_anotacao_registro_rt_trigger() OWNER TO owner_siconv_p;   

ALTER TABLE siconv.med_contrato_log_rec_seq OWNER TO owner_siconv_p;
ALTER TABLE siconv.med_contrato_log_rec OWNER TO owner_siconv_p;
ALTER FUNCTION siconv.med_contrato_trigger() OWNER TO owner_siconv_p;   

ALTER TABLE siconv.med_submeta_medicao_log_rec_seq OWNER TO owner_siconv_p;
ALTER TABLE siconv.med_submeta_medicao_log_rec OWNER TO owner_siconv_p; 
ALTER FUNCTION siconv.med_submeta_medicao_trigger() OWNER TO owner_siconv_p;   

ALTER TABLE siconv.med_responsavel_tecnico_log_rec_seq OWNER TO owner_siconv_p;
ALTER TABLE siconv.med_responsavel_tecnico_log_rec OWNER TO owner_siconv_p;  
ALTER FUNCTION siconv.med_responsavel_tecnico_trigger() OWNER TO owner_siconv_p;   

ALTER TABLE siconv.med_item_medicao_log_rec_seq OWNER TO owner_siconv_p;
ALTER TABLE siconv.med_item_medicao_log_rec OWNER TO owner_siconv_p;
ALTER FUNCTION siconv.med_item_medicao_trigger() OWNER TO owner_siconv_p;   

ALTER TABLE siconv.med_anotacao_registro_rt_submeta_log_rec_seq OWNER TO owner_siconv_p;
ALTER TABLE siconv.med_anotacao_registro_rt_submeta_log_rec OWNER TO owner_siconv_p;
ALTER FUNCTION siconv.med_anotacao_registro_rt_submeta_trigger() OWNER TO owner_siconv_p;   

ALTER TABLE siconv.med_anexo_log_rec_seq OWNER TO owner_siconv_p;
ALTER TABLE siconv.med_anexo_log_rec OWNER TO owner_siconv_p;
ALTER FUNCTION siconv.med_anexo_trigger() OWNER TO owner_siconv_p;   

ALTER TABLE siconv.med_contrato_resp_tecnico_log_rec_seq OWNER TO owner_siconv_p;
ALTER TABLE siconv.med_contrato_resp_tecnico_log_rec OWNER TO owner_siconv_p;  
ALTER FUNCTION siconv.med_contrato_resp_tecnico_trigger() OWNER TO owner_siconv_p;   

ALTER TABLE siconv.med_registro_profissional_log_rec_seq OWNER TO owner_siconv_p;
ALTER TABLE siconv.med_registro_profissional_log_rec OWNER TO owner_siconv_p;  
ALTER FUNCTION siconv.med_registro_profissional_trigger() OWNER TO owner_siconv_p;   

ALTER TABLE siconv.med_medicao_log_rec_seq OWNER TO owner_siconv_p;
ALTER TABLE siconv.med_medicao_log_rec OWNER TO owner_siconv_p;  
ALTER FUNCTION siconv.med_medicao_trigger() OWNER TO owner_siconv_p;   

ALTER TABLE siconv.med_observacao_log_rec_seq OWNER TO owner_siconv_p;
ALTER TABLE siconv.med_observacao_log_rec OWNER TO owner_siconv_p;   
ALTER FUNCTION siconv.med_observacao_trigger() OWNER TO owner_siconv_p;  

ALTER TABLE siconv.med_doc_complementar_submeta_log_rec_seq OWNER TO owner_siconv_p;
ALTER TABLE siconv.med_doc_complementar_submeta_log_rec OWNER TO owner_siconv_p;
ALTER FUNCTION siconv.med_doc_complementar_submeta_trigger() OWNER TO owner_siconv_p;   

ALTER TABLE siconv.med_doc_complementar_log_rec_seq OWNER TO owner_siconv_p;
ALTER TABLE siconv.med_doc_complementar_log_rec OWNER TO owner_siconv_p;
ALTER FUNCTION siconv.med_doc_complementar_trigger() OWNER TO owner_siconv_p;   

ALTER TABLE siconv.med_contrato_resp_tecnico_social_log_rec_seq OWNER TO owner_siconv_p;
ALTER TABLE siconv.med_contrato_resp_tecnico_social_log_rec OWNER TO owner_siconv_p;
ALTER FUNCTION siconv.med_contrato_resp_tecnico_social_trigger() OWNER TO owner_siconv_p;

ALTER TABLE siconv.med_contrato_rt_social_submeta_log_rec_seq OWNER TO owner_siconv_p;
ALTER TABLE siconv.med_contrato_rt_social_submeta_log_rec OWNER TO owner_siconv_p;
ALTER FUNCTION siconv.med_contrato_rt_social_submeta_trigger() OWNER TO owner_siconv_p;   


GRANT ALL ON SEQUENCE siconv.med_anotacao_registro_rt_log_rec_seq TO owner_siconv_p;
grant select,usage on siconv.med_anotacao_registro_rt_log_rec_seq to usr_siconv_p;
GRANT ALL ON TABLE siconv.med_anotacao_registro_rt_log_rec TO owner_siconv_p;
grant select,insert, update, delete, references on siconv.med_anotacao_registro_rt_log_rec to usr_siconv_p;

GRANT ALL ON SEQUENCE siconv.med_contrato_log_rec_seq TO owner_siconv_p;
grant select,usage on siconv.med_contrato_log_rec_seq to usr_siconv_p;
GRANT ALL ON TABLE siconv.med_contrato_log_rec TO owner_siconv_p;
grant select,insert, update, delete, references on siconv.med_contrato_log_rec to usr_siconv_p;

GRANT ALL ON SEQUENCE siconv.med_submeta_medicao_log_rec_seq TO owner_siconv_p;
grant select,usage on siconv.med_submeta_medicao_log_rec_seq to usr_siconv_p;
GRANT ALL ON TABLE siconv.med_submeta_medicao_log_rec TO owner_siconv_p;
grant select,insert, update, delete, references on siconv.med_submeta_medicao_log_rec to usr_siconv_p;

GRANT ALL ON SEQUENCE siconv.med_responsavel_tecnico_log_rec_seq TO owner_siconv_p;
grant select,usage on siconv.med_responsavel_tecnico_log_rec_seq to usr_siconv_p;
GRANT ALL ON TABLE siconv.med_responsavel_tecnico_log_rec TO owner_siconv_p;
grant select,insert, update, delete, references on siconv.med_responsavel_tecnico_log_rec to usr_siconv_p;

GRANT ALL ON SEQUENCE siconv.med_item_medicao_log_rec_seq TO owner_siconv_p;
grant select,usage on siconv.med_item_medicao_log_rec_seq to usr_siconv_p;
GRANT ALL ON TABLE siconv.med_item_medicao_log_rec TO owner_siconv_p;
grant select,insert, update, delete, references on siconv.med_item_medicao_log_rec to usr_siconv_p;

GRANT ALL ON SEQUENCE siconv.med_anotacao_registro_rt_submeta_log_rec_seq TO owner_siconv_p;
grant select,usage on siconv.med_anotacao_registro_rt_submeta_log_rec_seq to usr_siconv_p;
GRANT ALL ON TABLE siconv.med_anotacao_registro_rt_submeta_log_rec TO owner_siconv_p;
grant select,insert, update, delete, references on siconv.med_anotacao_registro_rt_submeta_log_rec to usr_siconv_p;

GRANT ALL ON SEQUENCE siconv.med_anexo_log_rec_seq TO owner_siconv_p;
grant select,usage on siconv.med_anexo_log_rec_seq to usr_siconv_p;
GRANT ALL ON TABLE siconv.med_anexo_log_rec TO owner_siconv_p;
grant select,insert, update, delete, references on siconv.med_anexo_log_rec to usr_siconv_p;

GRANT ALL ON SEQUENCE siconv.med_contrato_resp_tecnico_log_rec_seq TO owner_siconv_p;
grant select,usage on siconv.med_contrato_resp_tecnico_log_rec_seq to usr_siconv_p;
GRANT ALL ON TABLE siconv.med_contrato_resp_tecnico_log_rec TO owner_siconv_p;
grant select,insert, update, delete, references on siconv.med_contrato_resp_tecnico_log_rec to usr_siconv_p;

GRANT ALL ON SEQUENCE siconv.med_registro_profissional_log_rec_seq TO owner_siconv_p;
grant select,usage on siconv.med_registro_profissional_log_rec_seq to usr_siconv_p;
GRANT ALL ON TABLE siconv.med_registro_profissional_log_rec TO owner_siconv_p;
grant select,insert, update, delete, references on siconv.med_registro_profissional_log_rec to usr_siconv_p;

GRANT ALL ON SEQUENCE siconv.med_medicao_log_rec_seq TO owner_siconv_p;
grant select,usage on siconv.med_medicao_log_rec_seq to usr_siconv_p;
GRANT ALL ON TABLE siconv.med_medicao_log_rec TO owner_siconv_p;
grant select,insert, update, delete, references on siconv.med_medicao_log_rec to usr_siconv_p;

GRANT ALL ON SEQUENCE siconv.med_observacao_log_rec_seq TO owner_siconv_p;
grant select,usage on siconv.med_observacao_log_rec_seq to usr_siconv_p;
GRANT ALL ON TABLE siconv.med_observacao_log_rec TO owner_siconv_p;
grant select,insert, update, delete, references on siconv.med_observacao_log_rec to usr_siconv_p;

GRANT ALL ON SEQUENCE siconv.med_doc_complementar_submeta_log_rec_seq TO owner_siconv_p;
grant select,usage on siconv.med_doc_complementar_submeta_log_rec_seq to usr_siconv_p;
GRANT ALL ON TABLE siconv.med_doc_complementar_submeta_log_rec TO owner_siconv_p;
grant select,insert, update, delete, references on siconv.med_doc_complementar_submeta_log_rec to usr_siconv_p;

GRANT ALL ON SEQUENCE siconv.med_doc_complementar_log_rec_seq TO owner_siconv_p;
grant select,usage on siconv.med_doc_complementar_log_rec_seq to usr_siconv_p;
GRANT ALL ON TABLE siconv.med_doc_complementar_log_rec TO owner_siconv_p;
grant select,insert, update, delete, references on siconv.med_doc_complementar_log_rec to usr_siconv_p;

GRANT ALL ON SEQUENCE siconv.med_contrato_resp_tecnico_social_log_rec_seq TO owner_siconv_p;
grant select,usage on siconv.med_contrato_resp_tecnico_social_log_rec_seq to usr_siconv_p;
GRANT ALL ON TABLE siconv.med_contrato_resp_tecnico_social_log_rec TO owner_siconv_p;
grant select,insert, update, delete, references on siconv.med_contrato_resp_tecnico_social_log_rec to usr_siconv_p;

GRANT ALL ON SEQUENCE siconv.med_contrato_rt_social_submeta_log_rec_seq TO owner_siconv_p;
grant select,usage on siconv.med_contrato_rt_social_submeta_log_rec_seq to usr_siconv_p;
GRANT ALL ON TABLE siconv.med_contrato_rt_social_submeta_log_rec TO owner_siconv_p;
grant select,insert, update, delete, references on siconv.med_contrato_rt_social_submeta_log_rec to usr_siconv_p;
