/*
* ================================= DADOS DO SCRIPT ==================================================
* AUTOR: Ana Cristina Nunes Soares
* OBJETIVO: Criacao das tabelas med_paralisacao e med_anexo_paralisacao
*           referente a evolucao de Paralisacao.
* DATA CRIACAO: 23/11/2022
* PRE-REQUISITOS: Modelo atual da release 6 previamente aplicado
*/


-------------------------------  Início: med_paralisacao  -------------------------------


-------------------------------  Sequence: med_paralisacao  -------------------------------
create sequence siconv.med_paralisacao_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;


-------------------------------  Tabela: med_paralisacao  -------------------------------
CREATE TABLE siconv.med_paralisacao (
                id bigint NOT NULL DEFAULT nextval('med_paralisacao_id_seq'::regclass),
                med_contrato_fk bigint NOT NULL,
                dt_inicio date NOT NULL,
                dt_fim date NULL,
                tx_observacao character varying (1000) NOT NULL,
                in_responsavel varchar(3) NOT NULL,
                in_indicativo integer NOT NULL,
                in_motivo integer NOT NULL,                
                versao bigint NOT NULL DEFAULT 0,
                adt_login character varying NOT NULL,
                adt_data_hora timestamp NOT NULL,
                adt_operacao character varying(6) NOT NULL,
                CONSTRAINT med_paralisacao_pk PRIMARY KEY (id),
                CONSTRAINT ck_med_paralisacao_adt_operacao CHECK (adt_operacao::text = ANY (ARRAY['INSERT'::character varying::text, 'UPDATE'::character varying::text, 'DELETE'::character varying::text])),
                CONSTRAINT fkc_med_paralisacao_contrato_fk FOREIGN KEY (med_contrato_fk) REFERENCES med_contrato(id)
);

-------------------------------  Owner: med_paralisacao  -------------------------------
ALTER SEQUENCE siconv.med_paralisacao_id_seq OWNER TO owner_siconv_p;
ALTER TABLE siconv.med_paralisacao OWNER TO owner_siconv_p;



-------------------------------  Comentários: med_paralisacao  -------------------------------
COMMENT ON TABLE med_paralisacao IS 'Tabela de Cadastro de Paralisação';
COMMENT ON COLUMN med_paralisacao.id IS 'ID da Tabela de Paralisação';
COMMENT ON COLUMN med_paralisacao.med_contrato_fk IS 'FK da Tabela de Contratos';
COMMENT ON COLUMN med_paralisacao.dt_inicio IS 'Data de início da paralisação';
COMMENT ON COLUMN med_paralisacao.dt_fim IS 'Data de fim da paralisação';
COMMENT ON COLUMN med_paralisacao.tx_observacao IS 'Texto de observação';
COMMENT ON COLUMN med_paralisacao.in_responsavel IS 'EMP - Empresa; CVE - Convenente; CCE - Concedente; MAN - Mandatária; ORG - Órgão de controle; JUD - Judiciário; OUT - Outros';
COMMENT ON COLUMN med_paralisacao.in_indicativo IS '1 - Decisão judicial ou de órgão de controle interno ou externo; 2 - Declaração de empresa executora; 3 - Declaração de órgão ou entidade da administração pública federal; 4 - Sem apresentação de boletim de medição por período igual ou superior a 90 dias; 5 - Outros';
COMMENT ON COLUMN med_paralisacao.in_motivo IS '1 - Ação judicial; 2 - Alto reajuste dos valores de material; 3 - Ausência de recursos orçamentário/financeiro; 4 - Baixa governança sobre o objeto/localidade de recurso de emenda parlamentar; 5 - Carência no mercado local de materiais; 6 - Constantes necessidade de realinhamentos de preços; 7 - Demora na liberação de recursos pela união, acarretando aumento no valor do bem
e desistência do fornecedor; 8 - Desistência ou abandono pela empresa com justificativa; 9 - Desistência ou abandono pela empresa sem justificativa; 10 - Desvio de finalidade (do objeto); 11 - Dificuldades técnicas da organização executora; 12 - Excesso de burocracia (mandatária); 13 - Execução em desacordo com o projeto; 14 - Falta de recursos de contrapartida; 15 - Falta de titularidade e/ou desapropriação; 16 - Falta equipe técnica nos municípios para operacionalizar os instrumentos; 17 - Inadimplência da empresa executora; 18 - Localização geográfica de difícil acesso para entrega de materiais e serviços; 19 - Não obtenção de licenças, autorizações ou outros instrumentos equivalentes; 20 - Índices pluviométricos elevados em decorrência de chuvas; 21 - Perda de prazo / prorrogação de vigência; 22 - Problemas na garantia contratual; 23 - Problemas técnicos de execução; 24 - Projeto mal elaborado com impacto no licitatório; 25 - Rescisão contratual; 26 - Revisão de projeto básico; 27 - Revisão de projeto executivo.';
COMMENT ON COLUMN med_paralisacao.versao IS 'Versão usada para controlar a concorrência';
COMMENT ON COLUMN med_paralisacao.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN med_paralisacao.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN med_paralisacao.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';


-------------------------------  Grants: med_paralisacao  -------------------------------
grant select,usage                               on sequence med_paralisacao_id_seq                to usr_siconv_p;
grant all                                        on table med_paralisacao                          to owner_siconv_p;
grant select,insert, update, delete, references  on table med_paralisacao                          to usr_siconv_p;


-------------------------------  Indice: med_paralisacao  -------------------------------
CREATE INDEX idx_med_paralisacao_med_contrato_fk ON
siconv.med_paralisacao (med_contrato_fk);

------------------------------- Trigger de concorrência -------------------------------
create trigger tg_concurrent_med_paralisacao
before update on siconv.med_paralisacao
for each row execute procedure siconv.med_fn_generic_concurrent_trigger();

-------------------------------  Fim: med_paralisacao  -------------------------------






-------------------------------  Início: med_anexo_paralisacao  -------------------------------


-------------------------------  Sequence: med_anexo_paralisacao  -------------------------------
create sequence siconv.med_anexo_paralisacao_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;


-------------------------------  Tabela: med_anexo_paralisacao  -------------------------------
CREATE TABLE siconv.med_anexo_paralisacao (
                id bigint NOT NULL DEFAULT nextval('med_anexo_paralisacao_id_seq'::regclass),
                paralisacao_fk bigint NOT NULL,
                nm_arquivo varchar(100) NOT NULL,
                co_ceph varchar(1024) NOT NULL,
                adt_login character varying NOT NULL,
                adt_data_hora timestamp NOT NULL,
                adt_operacao character varying(6) NOT NULL,
                CONSTRAINT med_anexo_paralisacao_pk PRIMARY KEY (id),
                CONSTRAINT ck_med_anexo_paralisacao_adt_operacao CHECK (adt_operacao::text = ANY (ARRAY['INSERT'::character varying::text, 'UPDATE'::character varying::text, 'DELETE'::character varying::text])),
                CONSTRAINT fkc_med_anexo_paralisacao_paralisacao_fk FOREIGN KEY (paralisacao_fk) REFERENCES med_paralisacao(id)                
);

-------------------------------  Owner: med_anexo_paralisacao  -------------------------------
ALTER SEQUENCE siconv.med_anexo_paralisacao_id_seq OWNER TO owner_siconv_p;
ALTER TABLE siconv.med_anexo_paralisacao OWNER TO owner_siconv_p;



-------------------------------  Comentários: med_anexo_paralisacao  -------------------------------
COMMENT ON TABLE med_anexo_paralisacao IS 'Tabela de Cadastro de Anexo de Paralisação';
COMMENT ON COLUMN med_anexo_paralisacao.id IS 'ID da Tabela de Anexo de Paralisação';
COMMENT ON COLUMN med_anexo_paralisacao.paralisacao_fk IS 'FK da Tabela de Paralisação';
COMMENT ON COLUMN med_anexo_paralisacao.nm_arquivo IS 'Nome do arquivo anexado';
COMMENT ON COLUMN med_anexo_paralisacao.co_ceph IS 'Código da chave utilizada no Ceph para identificar o arquivo';
COMMENT ON COLUMN med_anexo_paralisacao.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN med_anexo_paralisacao.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN med_anexo_paralisacao.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';


-------------------------------  Grants: med_anexo_paralisacao  -------------------------------
grant select,usage                               on sequence med_anexo_paralisacao_id_seq                to usr_siconv_p;
grant all                                        on table med_anexo_paralisacao                          to owner_siconv_p;
grant select,insert, update, delete, references  on table med_anexo_paralisacao                          to usr_siconv_p;


-------------------------------  Indice: med_anexo_paralisacao  -------------------------------
CREATE INDEX idx_med_paralisacao_med_paralisacao_fk ON
siconv.med_anexo_paralisacao (paralisacao_fk);

-------------------------------  Fim: med_anexo_paralisacao  -------------------------------





-------------------------------  Início: med_paralisacao_log_rec  -------------------------------

-------------------------------  Sequence: siconv.med_paralisacao_log_rec_seq -------------------------------


CREATE SEQUENCE siconv.med_paralisacao_log_rec_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;



------------------------------- Tabela: siconv.med_paralisacao_log_rec ------------------------------- 


CREATE TABLE siconv.med_paralisacao_log_rec
(
  id bigint NOT NULL DEFAULT nextval('siconv.med_paralisacao_log_rec_seq'::regclass),
  entity_id bigint NOT NULL, 
  med_contrato_fk bigint NOT NULL,
  dt_inicio date NOT NULL,
  dt_fim date NULL,
  tx_observacao character varying (1000) NOT NULL,
  in_responsavel varchar(3) NOT NULL,
  in_indicativo integer NOT NULL,
  in_motivo integer NOT NULL,
  versao bigint,
  adt_login character varying(60),
  adt_data_hora timestamp without time zone,
  adt_operacao character varying(6),
  CONSTRAINT med_paralisacao_log_rec_pkey PRIMARY KEY (id),
  CONSTRAINT ck_med_paralisacao_log_rec_adt_operacao CHECK (adt_operacao::text = ANY (ARRAY['INSERT'::character varying::text, 'UPDATE'::character varying::text, 'DELETE'::character varying::text]))
)
WITH (
  OIDS=FALSE
);

-------------------------------  Comentários: med_paralisacao_log_rec  -------------------------------
COMMENT ON TABLE med_paralisacao_log_rec IS 'Tabela que registra o log das Paralisações de um Contrato de Licitação';
COMMENT ON COLUMN med_paralisacao_log_rec.id IS 'Identificador único do Registro de Log da tabela Paralisação';
COMMENT ON COLUMN med_paralisacao_log_rec.entity_id IS 'Identificador único da Paralisação';
COMMENT ON COLUMN med_paralisacao_log_rec.med_contrato_fk IS 'FK da Tabela de Contratos';
COMMENT ON COLUMN med_paralisacao_log_rec.dt_inicio IS 'Data de início da paralisação';
COMMENT ON COLUMN med_paralisacao_log_rec.dt_fim IS 'Data de fim da paralisação';
COMMENT ON COLUMN med_paralisacao_log_rec.tx_observacao IS 'Texto de observação';
COMMENT ON COLUMN med_paralisacao_log_rec.in_responsavel IS 'EMP - Empresa; CVE - Convenente; CCE - Concedente; MAN - Mandatária; ORG - Órgão de controle; JUD - Judiciário; OUT - Outros';
COMMENT ON COLUMN med_paralisacao_log_rec.in_indicativo IS '1 - Decisão judicial ou de órgão de controle interno ou externo; 2 - Declaração de empresa executora; 3 - Declaração de órgão ou entidade da administração pública federal; 4 - Sem apresentação de boletim de medição por período igual ou superior a 90 dias; 5 - Outros';
COMMENT ON COLUMN med_paralisacao_log_rec.in_motivo IS '1 - Ação judicial; 2 - Alto reajuste dos valores de material; 3 - Ausência de recursos orçamentário/financeiro; 4 - Baixa governança sobre o objeto/localidade de recurso de emenda parlamentar; 5 - Carência no mercado local de materiais; 6 - Constantes necessidade de realinhamentos de preços; 7 - Demora na liberação de recursos pela união, acarretando aumento no valor do bem
e desistência do fornecedor; 8 - Desistência ou abandono pela empresa com justificativa; 9 - Desistência ou abandono pela empresa sem justificativa; 10 - Desvio de finalidade (do objeto); 11 - Dificuldades técnicas da organização executora; 12 - Excesso de burocracia (mandatária); 13 - Execução em desacordo com o projeto; 14 - Falta de recursos de contrapartida; 15 - Falta de titularidade e/ou desapropriação; 16 - Falta equipe técnica nos municípios para operacionalizar os instrumentos; 17 - Inadimplência da empresa executora; 18 - Localização geográfica de difícil acesso para entrega de materiais e serviços; 19 - Não obtenção de licenças, autorizações ou outros instrumentos equivalentes; 20 - Índices pluviométricos elevados em decorrência de chuvas; 21 - Perda de prazo / prorrogação de vigência; 22 - Problemas na garantia contratual; 23 - Problemas técnicos de execução; 24 - Projeto mal elaborado com impacto no licitatório; 25 - Rescisão contratual; 26 - Revisão de projeto básico; 27 - Revisão de projeto executivo.';
COMMENT ON COLUMN med_paralisacao_log_rec.versao IS 'Versão usada para controlar a concorrência';
COMMENT ON COLUMN med_paralisacao_log_rec.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN med_paralisacao_log_rec.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN med_paralisacao_log_rec.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';

-------------------------------  Grants: med_paralisacao_log_rec  -------------------------------
GRANT ALL ON SEQUENCE siconv.med_paralisacao_log_rec_seq TO owner_siconv_p;
grant select,usage on siconv.med_paralisacao_log_rec_seq to usr_siconv_p;

GRANT ALL ON TABLE siconv.med_paralisacao_log_rec TO owner_siconv_p;
grant select, insert, update, delete, references on siconv.med_paralisacao_log_rec to usr_siconv_p;


------------------------------- Função: med_paralisacao_trigger -------------------------------
 CREATE OR REPLACE FUNCTION siconv.med_paralisacao_trigger()
   RETURNS trigger AS 
 $BODY$
   DECLARE wl_id integer;
   BEGIN
   -- Aqui temos um bloco IF que confirmará o tipo de operação.
   IF (TG_OP = 'INSERT') or (TG_OP = 'UPDATE') THEN
       INSERT INTO siconv.med_paralisacao_log_rec (
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        entity_id,
        versao,
        med_contrato_fk,
        dt_inicio,
        dt_fim,
        tx_observacao,
        in_responsavel,
        in_indicativo,
        in_motivo
       ) VALUES (
        nextval('siconv.med_paralisacao_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        NEW.adt_data_hora,
        NEW.adt_operacao,
        NEW.id,
        NEW.versao,
        NEW.med_contrato_fk,
        NEW.dt_inicio,
        NEW.dt_fim,
        NEW.tx_observacao,
        NEW.in_responsavel,
        NEW.in_indicativo,
        NEW.in_motivo
       );
       RETURN NEW;
   -- Aqui temos um bloco IF que confirmará o tipo de operação DELETE.
   ELSIF (TG_OP = 'DELETE') THEN
       INSERT INTO siconv.med_paralisacao_log_rec (
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        entity_id,
        versao,
        med_contrato_fk,
        dt_inicio,
        dt_fim,
        tx_observacao,
        in_responsavel,
        in_indicativo,
        in_motivo
       ) VALUES (
        nextval('siconv.med_paralisacao_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        LOCALTIMESTAMP,
        TG_OP,
        OLD.id,
        OLD.versao,
        OLD.med_contrato_fk,
        OLD.dt_inicio,
        OLD.dt_fim,
        OLD.tx_observacao,
        OLD.in_responsavel,
        OLD.in_indicativo,
        OLD.in_motivo
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $BODY$
   LANGUAGE plpgsql VOLATILE
   COST 100;
 
 
 ------------------------------- Trigger: siconv.tg_med_paralisacao on siconv.med_paralisacao
 CREATE TRIGGER tg_med_paralisacao
   AFTER INSERT OR UPDATE OR DELETE
   ON siconv.med_paralisacao
   FOR EACH ROW
   EXECUTE PROCEDURE siconv.med_paralisacao_trigger(); 

-------------------------------  Fim: med_paralisacao_log_rec  -------------------------------



-------------------------------  Início: med_anexo_paralisacao_log_rec  -------------------------------

-------------------------------  Sequence: siconv.med_anexo_paralisacao_log_rec_seq -------------------------------


CREATE SEQUENCE siconv.med_anexo_paralisacao_log_rec_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;



------------------------------- Tabela: siconv.med_anexo_paralisacao_log_rec ------------------------------- 


CREATE TABLE siconv.med_anexo_paralisacao_log_rec
(
  id bigint NOT NULL DEFAULT nextval('siconv.med_anexo_paralisacao_log_rec_seq'::regclass),
  co_ceph character varying (1024) NOT NULL, 
  entity_id bigint NOT NULL, 
  nm_arquivo character varying (100) NOT NULL,
  paralisacao_fk bigint NOT NULL,
  adt_login character varying(60),
  adt_data_hora timestamp without time zone,
  adt_operacao character varying(6),
  CONSTRAINT med_anexo_paralisacao_log_rec_pkey PRIMARY KEY (id),
  CONSTRAINT ck_med_anexo_paralisacao_adt_operacao CHECK (adt_operacao::text = ANY (ARRAY['INSERT'::character varying::text, 'UPDATE'::character varying::text, 'DELETE'::character varying::text]))
)
WITH (
  OIDS=FALSE
);

-------------------------------  Comentários: med_anexo_paralisacao_log_rec  -------------------------------
COMMENT ON TABLE med_anexo_paralisacao_log_rec IS 'Tabela que registra o log de Anexos das Paralisações de um Contrato de Licitação';
COMMENT ON COLUMN med_anexo_paralisacao_log_rec.id IS 'Identificador único do Registro de Log da tabela Anexo Paralisação';
COMMENT ON COLUMN med_anexo_paralisacao_log_rec.entity_id IS 'Identificador único do Anexo Paralisação';
COMMENT ON COLUMN med_anexo_paralisacao_log_rec.co_ceph IS 'Código da chave utilizada no Ceph para identificar o arquivo';
COMMENT ON COLUMN med_anexo_paralisacao_log_rec.nm_arquivo IS 'Nome do arquivo anexado';
COMMENT ON COLUMN med_anexo_paralisacao_log_rec.paralisacao_fk IS 'FK da Tabela de Paralisação';
COMMENT ON COLUMN med_anexo_paralisacao_log_rec.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN med_anexo_paralisacao_log_rec.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN med_anexo_paralisacao_log_rec.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';

-------------------------------  Grants: med_anexo_paralisacao_log_rec  -------------------------------
GRANT ALL ON SEQUENCE siconv.med_anexo_paralisacao_log_rec_seq TO owner_siconv_p;
grant select,usage on siconv.med_anexo_paralisacao_log_rec_seq to usr_siconv_p;

GRANT ALL ON TABLE siconv.med_anexo_paralisacao_log_rec TO owner_siconv_p;
grant select, insert, update, delete, references on siconv.med_anexo_paralisacao_log_rec to usr_siconv_p;

------------------------------- Função: med_anexo_paralisacao_trigger -------------------------------
 CREATE OR REPLACE FUNCTION siconv.med_anexo_paralisacao_trigger()
   RETURNS trigger AS 
 $BODY$
   DECLARE wl_id integer;
   BEGIN
   -- Aqui temos um bloco IF que confirmará o tipo de operação.
   IF (TG_OP = 'INSERT') or (TG_OP = 'UPDATE') THEN
       INSERT INTO siconv.med_anexo_paralisacao_log_rec (
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        co_ceph,
        entity_id,
        nm_arquivo,
        paralisacao_fk
       ) VALUES (
        nextval('siconv.med_anexo_paralisacao_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        NEW.adt_data_hora,
        NEW.adt_operacao,
        NEW.co_ceph,
        NEW.id,
        NEW.nm_arquivo,
        NEW.paralisacao_fk
       );
       RETURN NEW;
   -- Aqui temos um bloco IF que confirmará o tipo de operação DELETE.
   ELSIF (TG_OP = 'DELETE') THEN
       INSERT INTO siconv.med_anexo_paralisacao_log_rec (
        id,
        adt_login,
        adt_data_hora,
        adt_operacao,
        co_ceph,
        entity_id,
        nm_arquivo,
        paralisacao_fk
       ) VALUES (
        nextval('siconv.med_anexo_paralisacao_log_rec_seq'),
        current_setting('med.cpf_usuario'),
        LOCALTIMESTAMP,
        TG_OP,
        OLD.co_ceph,
        OLD.id,
        OLD.nm_arquivo,
        OLD.paralisacao_fk
       );
       RETURN OLD;
   END IF;
   RETURN NULL;
   END;
   $BODY$
   LANGUAGE plpgsql VOLATILE
   COST 100;
 
 
 ------------------------------- Trigger: siconv.tg_med_anexo_paralisacao on siconv.med_anexo_paralisacao
 CREATE TRIGGER tg_med_anexo_paralisacao
   AFTER INSERT OR UPDATE OR DELETE
   ON siconv.med_anexo_paralisacao
   FOR EACH ROW
   EXECUTE PROCEDURE siconv.med_anexo_paralisacao_trigger(); 

-------------------------------  Fim: med_anexo_paralisacao_log_rec  -------------------------------