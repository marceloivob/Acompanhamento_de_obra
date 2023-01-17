/*
* ================================== DADOS DO SCRIPT ================================================
* AUTOR: Samuel M. de Oliveira
* OBJETIVO: Criação da coluna de acompanhamento de contrato por evento e criação das tabelas de 
* Item medição BM e Item medição BM Valor 
* DATA CRIACAO: 26/08/2021
* PRE-REQUISITOS: Modelo atual da release 3 previamente aplicado
*
* ============================ HISTÓRICO DE ALTERAÇÕES ==============================================
* DATA       | AUTOR          | MOTIVO
* ----------------------------------------------------------------------------------------------
* 27/09/2021 | Gustavo Diniz  | Ajuste colunas qt_empresa, qt_convenente e qt_concedente para NULLABLE
*
*/

-------------------------------  Início: med_contrato  -------------------------------


-------------------------------  Tabela: med_contrato  -------------------------------
ALTER TABLE siconv.med_contrato ADD in_acompanhamento_eventos bool NULL;

COMMENT ON COLUMN siconv.med_contrato.in_acompanhamento_eventos IS 'Indica se o contrato é acompanhado por Eventos';


ALTER TABLE siconv.med_contrato DISABLE TRIGGER tg_med_contrato;

UPDATE siconv.med_contrato
SET in_acompanhamento_eventos = true;

ALTER TABLE siconv.med_contrato ENABLE TRIGGER tg_med_contrato;

ALTER TABLE siconv.med_contrato ALTER COLUMN in_acompanhamento_eventos SET NOT NULL;


-------------------------------  Tabela log_rec: med_contrato_log_rec  -------------------------------
ALTER TABLE siconv.med_contrato_log_rec ADD in_acompanhamento_eventos bool NULL;

UPDATE siconv.med_contrato_log_rec 
SET in_acompanhamento_eventos = true;

ALTER TABLE siconv.med_contrato_log_rec ALTER COLUMN in_acompanhamento_eventos SET NOT NULL;


COMMENT ON COLUMN siconv.med_contrato_log_rec.in_acompanhamento_eventos IS 'Indica se o contrato é acompanhado por Eventos';

-------------------------------  trigger: med_contrato -------------------------------
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
        proposta_fk,
        in_acompanhamento_eventos
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
        NEW.proposta_fk,
        NEW.in_acompanhamento_eventos
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
        proposta_fk,
        in_acompanhamento_eventos
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
        OLD.proposta_fk,
        OLD.in_acompanhamento_eventos
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $function$
;

ALTER FUNCTION siconv.med_contrato_trigger() OWNER TO owner_siconv_p;
-------------------------------  Fim: med_contrato  -------------------------------





-------------------------------  Início: med_item_medicao_bm  -------------------------------


-------------------------------  Sequence: med_item_medicao_bm  -------------------------------
create sequence siconv.med_item_medicao_bm_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;


-------------------------------  Tabela: med_item_medicao_bm  -------------------------------
CREATE TABLE siconv.med_item_medicao_bm (
                id bigint NOT NULL DEFAULT nextval('med_item_medicao_bm_id_seq'::regclass),
                med_contrato_fk bigint NOT NULL,
                vrpl_frente_obra_fk bigint NOT NULL,
    			vrpl_submeta_fk bigint NOT NULL,
    			vrpl_servico_fk bigint NOT NULL,
    			qt_total_servico numeric(17,2) NOT NULL DEFAULT 0,
    			vl_preco_unitario_licitado numeric(17,2) NOT NULL DEFAULT 0,
                adt_login character varying NOT NULL,
                adt_data_hora timestamp NOT NULL,
                adt_operacao character varying(6) NOT NULL,
                CONSTRAINT med_item_medicao_bm_pk PRIMARY KEY (id),
                CONSTRAINT fkc_med_item_medicao_bm_contrato_fk FOREIGN KEY (med_contrato_fk) REFERENCES med_contrato(id),
                CONSTRAINT ck_med_item_medicao_bm_adt_operacao CHECK (adt_operacao::text = ANY (ARRAY['INSERT'::character varying::text, 'UPDATE'::character varying::text, 'DELETE'::character varying::text])),
                CONSTRAINT uk_med_item_medicao_bm_vrpl_submeta_frente_obra_servico UNIQUE (vrpl_frente_obra_fk, vrpl_submeta_fk, vrpl_servico_fk)
                
);

-------------------------------  Owner: med_item_medicao_bm  -------------------------------
ALTER SEQUENCE siconv.med_item_medicao_bm_id_seq OWNER TO owner_siconv_p;
ALTER TABLE siconv.med_item_medicao_bm OWNER TO owner_siconv_p;



-------------------------------  Comentários: med_item_medicao_bm  -------------------------------
COMMENT ON TABLE med_item_medicao_bm IS 'Tabela de Item de Medição do BM';
COMMENT ON COLUMN med_item_medicao_bm.id IS 'ID da Tabela de Item de Medição do BM';
COMMENT ON COLUMN med_item_medicao_bm.med_contrato_fk IS 'FK da Tabela de Contratos';
COMMENT ON COLUMN med_item_medicao_bm.vrpl_frente_obra_fk IS 'FK da tabela Frente de Obra no VRPL';
COMMENT ON COLUMN med_item_medicao_bm.vrpl_submeta_fk IS 'FK da tabela Submeta no VRPL';
COMMENT ON COLUMN med_item_medicao_bm.vrpl_servico_fk IS 'FK da tabela Serviço no VRPL';
COMMENT ON COLUMN med_item_medicao_bm.qt_total_servico IS 'Qtde total do Serviço por Frente de Obra no VRPL';
COMMENT ON COLUMN med_item_medicao_bm.vl_preco_unitario_licitado IS 'Custo unitário do Serviço no VRPL';
COMMENT ON COLUMN med_item_medicao_bm.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN med_item_medicao_bm.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN med_item_medicao_bm.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';


-------------------------------  Grants: med_item_medicao_bm  -------------------------------
grant select,usage                               on sequence med_item_medicao_bm_id_seq                to usr_siconv_p;
grant all                                        on table med_item_medicao_bm                          to owner_siconv_p;
grant select,insert, update, delete, references  on table med_item_medicao_bm                          to usr_siconv_p;


-------------------------------  Indice: med_item_medicao_bm  -------------------------------
CREATE INDEX idx_med_item_medicao_bm_med_contrato_fk ON
siconv.med_item_medicao_bm (med_contrato_fk);

CREATE INDEX idx_med_item_medicao_bm_vrpl_submeta_fk ON
siconv.med_item_medicao_bm (vrpl_submeta_fk);

CREATE INDEX idx_med_item_medicao_bm_vrpl_frente_obra_fk ON
siconv.med_item_medicao_bm (vrpl_frente_obra_fk);

CREATE INDEX idx_med_item_medicao_bm_vrpl_servico_fk ON
siconv.med_item_medicao_bm (vrpl_servico_fk);

-------------------------------  Fim: med_item_medicao_bm  -------------------------------



-------------------------------  Início: med_item_medicao_bm_log_rec  -------------------------------


-------------------------------  Sequence: med_item_medicao_bm_log_rec  -------------------------------
CREATE SEQUENCE siconv.med_item_medicao_bm_log_rec_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;


-------------------------------  Tabela: med_item_medicao_bm_log_rec  -------------------------------
CREATE TABLE siconv.med_item_medicao_bm_log_rec
(
  id bigint NOT NULL DEFAULT nextval('siconv.med_item_medicao_bm_log_rec_seq'::regclass), -- Coluna de id.
  entity_id bigint NOT NULL, 
  medcontratolog bigint NOT NULL, 
  qt_total_servico numeric(17,2) NOT NULL,
  vl_preco_unitario_licitado numeric(17,2) NOT NULL,  
  vrpl_frente_obra_fk bigint NOT NULL,
  vrpl_submeta_fk bigint NOT NULL, 
  vrpl_servico_fk bigint NOT NULL, 
  adt_login character varying(60),
  adt_data_hora timestamp without time zone,
  adt_operacao character varying(6),
  CONSTRAINT med_item_medicao_bm_log_rec_pk PRIMARY KEY (id),
  CONSTRAINT ck_med_item_medicao_bm_adt_operacao CHECK (adt_operacao::text = ANY (ARRAY['INSERT'::character varying::text, 'UPDATE'::character varying::text, 'DELETE'::character varying::text]))
);

-------------------------------  Owner: med_item_medicao_bm_log_rec  -------------------------------
ALTER SEQUENCE siconv.med_item_medicao_bm_log_rec_seq OWNER TO owner_siconv_p;
ALTER TABLE siconv.med_item_medicao_bm_log_rec OWNER TO owner_siconv_p;


-------------------------------  Comentários: med_item_medicao_bm_log_rec  -------------------------------
COMMENT ON TABLE siconv.med_item_medicao_bm_log_rec IS 'Tabela que registra o log de Itens da Submeta das Medições de um BM';
COMMENT ON COLUMN siconv.med_item_medicao_bm_log_rec.id IS 'Identificador único de um Registro de Log na tabela de Item de Medicao de um BM';
COMMENT ON COLUMN siconv.med_item_medicao_bm_log_rec.entity_id IS 'Identificador único do Item de Medicao de um BM';
COMMENT ON COLUMN siconv.med_item_medicao_bm_log_rec.medcontratolog IS 'FK da tabela Contrato do módulo Medição';
COMMENT ON COLUMN siconv.med_item_medicao_bm_log_rec.qt_total_servico IS 'Qtde total do Serviço por Frente de Obra no VRPL';
COMMENT ON COLUMN siconv.med_item_medicao_bm_log_rec.vl_preco_unitario_licitado IS 'Custo unitário do Serviço no VRPL';
COMMENT ON COLUMN siconv.med_item_medicao_bm_log_rec.vrpl_frente_obra_fk IS 'FK da tabela Frente de Obra do módulo VRPL';
COMMENT ON COLUMN siconv.med_item_medicao_bm_log_rec.vrpl_submeta_fk IS 'FK da tabela Submeta do módulo VRPL';
COMMENT ON COLUMN siconv.med_item_medicao_bm_log_rec.vrpl_servico_fk IS 'FK da tabela Serviço do módulo VRPL';
COMMENT ON COLUMN siconv.med_item_medicao_bm_log_rec.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN siconv.med_item_medicao_bm_log_rec.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN siconv.med_item_medicao_bm_log_rec.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';

 -------------------------------  Grants: med_item_medicao_bm_log_rec  -------------------------------
GRANT ALL ON SEQUENCE siconv.med_item_medicao_bm_log_rec_seq TO owner_siconv_p;
grant select,usage on siconv.med_item_medicao_bm_log_rec_seq to usr_siconv_p;

GRANT ALL ON TABLE siconv.med_item_medicao_bm_log_rec TO owner_siconv_p;
grant select, insert, update, delete, references on siconv.med_item_medicao_bm_log_rec to usr_siconv_p;

-------------------------------  Fim: med_item_medicao_bm_log_rec  ------------------------------- 
 





-------------------------------  Início: med_item_medicao_bm_vl  -------------------------------


-------------------------------  Sequence: med_item_medicao_bm_vl  -------------------------------
create sequence siconv.med_item_medicao_bm_vl_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;


-------------------------------  Tabela: med_item_medicao_bm_vl  -------------------------------
CREATE TABLE siconv.med_item_medicao_bm_vl (
                id bigint NOT NULL DEFAULT nextval('med_item_medicao_bm_vl_id_seq'::regclass),
                med_item_medicao_bm_fk bigint NOT NULL,
                med_medicao_fk bigint NOT NULL,
    			qt_empresa numeric(17,2) NULL,
    			qt_convenente numeric(17,2) NULL,
    			qt_concedente numeric(17,2) NULL,
                adt_login character varying NOT NULL,
                adt_data_hora timestamp NOT NULL,
                adt_operacao character varying(6) NOT NULL,
                CONSTRAINT med_item_medicao_bm_vl_pk PRIMARY KEY (id),
                CONSTRAINT fkc_med_item_medicao_bm_fk FOREIGN KEY (med_item_medicao_bm_fk) REFERENCES med_item_medicao_bm(id),
                CONSTRAINT fkc_med_medicao_fk FOREIGN KEY (med_medicao_fk) REFERENCES med_medicao(id),
                CONSTRAINT ck_med_item_medicao_bm_vl_adt_operacao CHECK (adt_operacao::text = ANY (ARRAY['INSERT'::character varying::text, 'UPDATE'::character varying::text, 'DELETE'::character varying::text])),
                CONSTRAINT uk_item_medicao_bm_vl_item_medicao_bm_fk_medicao_fk UNIQUE (med_item_medicao_bm_fk, med_medicao_fk)
);


-------------------------------  Owner: med_item_medicao_bm_vl  -------------------------------
ALTER SEQUENCE siconv.med_item_medicao_bm_vl_id_seq OWNER TO owner_siconv_p;
ALTER TABLE siconv.med_item_medicao_bm_vl OWNER TO owner_siconv_p;



-------------------------------  Comentários: med_item_medicao_bm_vl  -------------------------------
COMMENT ON TABLE med_item_medicao_bm_vl IS 'Tabela de Item de Medição do BM que registra as qtdes informadas por perfil';
COMMENT ON COLUMN med_item_medicao_bm_vl.id IS 'ID da Tabela de valores dos Itens de Medição do BM';
COMMENT ON COLUMN med_item_medicao_bm_vl.med_item_medicao_bm_fk IS 'FK da Tabela de Item de Medicao do BM';
COMMENT ON COLUMN med_item_medicao_bm_vl.med_medicao_fk IS 'FK da Tabela de Medicao';
COMMENT ON COLUMN med_item_medicao_bm_vl.qt_empresa IS 'Qtde informada na Medicao pela Empresa';
COMMENT ON COLUMN med_item_medicao_bm_vl.qt_convenente IS 'Qtde informada na Medicao pelo Convenente';
COMMENT ON COLUMN med_item_medicao_bm_vl.qt_concedente IS 'Qtde informada na Medicao pelo Concedente';
COMMENT ON COLUMN med_item_medicao_bm_vl.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN med_item_medicao_bm_vl.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN med_item_medicao_bm_vl.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';


-------------------------------  Grants: med_item_medicao_bm_vl  -------------------------------
grant select,usage                               on sequence med_item_medicao_bm_vl_id_seq                to usr_siconv_p;
grant all                                        on table med_item_medicao_bm_vl                          to owner_siconv_p;
grant select,insert, update, delete, references  on table med_item_medicao_bm_vl                          to usr_siconv_p;


-------------------------------  Indice: med_item_medicao_bm_vl  -------------------------------
CREATE INDEX idx_med_item_medicao_bm_vl_med_item_medicao_bm_fk ON
siconv.med_item_medicao_bm_vl (med_item_medicao_bm_fk);

CREATE INDEX idx_med_item_medicao_bm_vl_med_medicao_fk ON
siconv.med_item_medicao_bm_vl (med_medicao_fk);

-------------------------------  Fim: med_item_medicao_bm  -------------------------------



-------------------------------  Início: med_item_medicao_bm_vl_log_rec  -------------------------------


-------------------------------  Sequence: med_item_medicao_bm_log_rec  -------------------------------
CREATE SEQUENCE siconv.med_item_medicao_bm_vl_log_rec_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;


 -------------------------------  Tabela: med_item_medicao_bm_vl_log_rec  -------------------------------
CREATE TABLE siconv.med_item_medicao_bm_vl_log_rec
(
  id bigint NOT NULL DEFAULT nextval('siconv.med_item_medicao_bm_log_rec_seq'::regclass), -- Coluna de id.
  entity_id bigint NOT NULL, 
  meditemmedicaobmlog bigint NOT NULL, 
  medmedicaolog bigint NOT NULL,
  qt_empresa numeric(17,2) NULL,
  qt_convenente numeric(17,2) NULL,  
  qt_concedente numeric(17,2) NULL,
  adt_login character varying(60),
  adt_data_hora timestamp without time zone,
  adt_operacao character varying(6),
  CONSTRAINT med_item_medicao_bm_vl_log_rec_pk PRIMARY KEY (id),
  CONSTRAINT ck_med_item_medicao_bm_vl_adt_operacao CHECK (adt_operacao::text = ANY (ARRAY['INSERT'::character varying::text, 'UPDATE'::character varying::text, 'DELETE'::character varying::text]))
);

-------------------------------  Owner: med_item_medicao_bm_vl_log_rec  -------------------------------
ALTER SEQUENCE siconv.med_item_medicao_bm_vl_log_rec_seq OWNER TO owner_siconv_p;
ALTER TABLE siconv.med_item_medicao_bm_vl_log_rec OWNER TO owner_siconv_p;
 

-------------------------------  Comentários: med_item_medicao_bm_vl_log_rec  -------------------------------
COMMENT ON TABLE siconv.med_item_medicao_bm_vl_log_rec IS 'Tabela que registra o log dos valores informados para os Itens de Medicao por perfil no BM';
COMMENT ON COLUMN siconv.med_item_medicao_bm_vl_log_rec.id IS 'Identificador único de um Registro de Log na tabela valores dos Itens de Medicoes de um BM';
COMMENT ON COLUMN siconv.med_item_medicao_bm_vl_log_rec.entity_id IS 'Identificador único do valor do Item de Medicao de um BM';
COMMENT ON COLUMN siconv.med_item_medicao_bm_vl_log_rec.meditemmedicaobmlog IS 'FK do Item de Medicao de um BM';
COMMENT ON COLUMN siconv.med_item_medicao_bm_vl_log_rec.medmedicaolog IS 'FK da Medicao de um BM';
COMMENT ON COLUMN siconv.med_item_medicao_bm_vl_log_rec.qt_empresa IS 'Qtde informada na Medicao pela Empresa';
COMMENT ON COLUMN siconv.med_item_medicao_bm_vl_log_rec.qt_convenente IS 'Qtde informada na Medicao pelo Convenente';
COMMENT ON COLUMN siconv.med_item_medicao_bm_vl_log_rec.qt_concedente IS 'Qtde informada na Medicao pelo Concedente';
COMMENT ON COLUMN siconv.med_item_medicao_bm_vl_log_rec.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN siconv.med_item_medicao_bm_vl_log_rec.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN siconv.med_item_medicao_bm_vl_log_rec.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';


 -------------------------------  Grants: med_item_medicao_bm_vl_log_rec  -------------------------------
GRANT ALL ON SEQUENCE siconv.med_item_medicao_bm_vl_log_rec_seq TO owner_siconv_p;
grant select,usage on siconv.med_item_medicao_bm_vl_log_rec_seq to usr_siconv_p;

GRANT ALL ON TABLE siconv.med_item_medicao_bm_vl_log_rec TO owner_siconv_p;
grant select, insert, update, delete, references on siconv.med_item_medicao_bm_vl_log_rec to usr_siconv_p;

-------------------------------  Fim: med_item_medicao_bm_vl_log_rec  ------------------------------- 


 
-------------------------------  Início Trigger: med_item_medicao_bm_trigger  -------------------------------
CREATE OR REPLACE FUNCTION siconv.med_item_medicao_bm_trigger()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
   DECLARE wl_id integer;
   BEGIN
   -- Aqui temos um bloco IF que confirmará o tipo de operação.
   IF (TG_OP = 'INSERT') or (TG_OP = 'UPDATE') THEN
       INSERT INTO siconv.med_item_medicao_bm_log_rec (
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        entity_id,
        medcontratolog,
		qt_total_servico,
		vl_preco_unitario_licitado,
        vrpl_frente_obra_fk,
        vrpl_submeta_fk,
		vrpl_servico_fk
       ) VALUES (
        nextval('siconv.med_item_medicao_bm_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        NEW.adt_data_hora,
        NEW.adt_operacao,
        NEW.id,
        NEW.med_contrato_fk,
        NEW.qt_total_servico,
        NEW.vl_preco_unitario_licitado,
        NEW.vrpl_frente_obra_fk,
        NEW.vrpl_submeta_fk,
        NEW.vrpl_servico_fk
       );
       RETURN NEW;
   -- Aqui temos um bloco IF que confirmará o tipo de operação DELETE.
   ELSIF (TG_OP = 'DELETE') THEN
       INSERT INTO siconv.med_item_medicao_bm_log_rec (
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        entity_id,
        medcontratolog,
        qt_total_servico,
        vl_preco_unitario_licitado,
        vrpl_frente_obra_fk,
        vrpl_submeta_fk,
        vrpl_servico_fk
       ) VALUES (
        nextval('siconv.med_item_medicao_bm_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        LOCALTIMESTAMP,
        TG_OP,
        OLD.id,
        OLD.med_contrato_fk,
        OLD.qt_total_servico,
        OLD.vl_preco_unitario_licitado,
        OLD.vrpl_frente_obra_fk,
        OLD.vrpl_submeta_fk,
        OLD.vrpl_servico_fk
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $function$
;

-------------------------------  Owner Trigger: med_item_medicao_bm_trigger  -------------------------------
ALTER FUNCTION siconv.med_item_medicao_bm_trigger() OWNER TO owner_siconv_p;   


-------------------------------  Trigger: tg_med_item_medicao_bm  ----------------------------------------
 CREATE TRIGGER tg_med_item_medicao_bm
   AFTER INSERT OR UPDATE OR DELETE
   ON siconv.med_item_medicao_bm
   FOR EACH ROW
   EXECUTE PROCEDURE siconv.med_item_medicao_bm_trigger(); 

-------------------------------  Fim Trigger: med_item_medicao_bm_trigger  -------------------------------  

  
-------------------------------  Início Trigger: med_item_medicao_bm_vl_trigger  -------------------------------
CREATE OR REPLACE FUNCTION siconv.med_item_medicao_bm_vl_trigger()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
   DECLARE wl_id integer;
   BEGIN
   -- Aqui temos um bloco IF que confirmará o tipo de operação.
   IF (TG_OP = 'INSERT') or (TG_OP = 'UPDATE') THEN
       INSERT INTO siconv.med_item_medicao_bm_vl_log_rec (
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        entity_id,
        meditemmedicaobmlog,
        medmedicaolog,
        qt_empresa,
		qt_convenente,  
		qt_concedente
		) VALUES (
        nextval('siconv.med_item_medicao_bm_vl_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        NEW.adt_data_hora,
        NEW.adt_operacao,
        NEW.id,
        NEW.med_item_medicao_bm_fk,
        NEW.med_medicao_fk,
        NEW.qt_empresa,
        NEW.qt_convenente,
        NEW.qt_concedente
       );
       RETURN NEW;
   -- Aqui temos um bloco IF que confirmará o tipo de operação DELETE.
   ELSIF (TG_OP = 'DELETE') THEN
       INSERT INTO siconv.med_item_medicao_bm_vl_log_rec (
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        entity_id,
        meditemmedicaobmlog,
        medmedicaolog,
        qt_empresa,
		qt_convenente,  
		qt_concedente
       ) VALUES (
        nextval('siconv.med_item_medicao_bm_vl_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        LOCALTIMESTAMP,
        TG_OP,
        OLD.id,
        OLD.med_item_medicao_bm_fk,
        OLD.med_medicao_fk,
        OLD.qt_empresa,
        OLD.qt_convenente,
        OLD.qt_concedente
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $function$
;
  

-------------------------------  Owner Trigger: med_item_medicao_bm_trigger  -------------------------------
ALTER FUNCTION siconv.med_item_medicao_bm_vl_trigger() OWNER TO owner_siconv_p;   


-------------------------------  Trigger: tg_med_item_medicao_bm  ----------------------------------------
 CREATE TRIGGER tg_med_item_medicao_bm_vl
   AFTER INSERT OR UPDATE OR DELETE
   ON siconv.med_item_medicao_bm_vl
   FOR EACH ROW
   EXECUTE PROCEDURE siconv.med_item_medicao_bm_vl_trigger(); 

-------------------------------  Fim Trigger: med_item_medicao_bm_trigger  -------------------------------  
