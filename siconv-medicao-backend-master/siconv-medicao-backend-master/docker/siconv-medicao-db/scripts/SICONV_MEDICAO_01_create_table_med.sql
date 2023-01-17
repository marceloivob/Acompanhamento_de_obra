
/*********************************************************************************************************************************************
 * SERVICO FEDERAL DE PROCESSAMENTO DE DADOS - SERPRO
 * Superintendencia de Desenvolvimento - SUPDE
 * Polo de Desenvolvimento em Recife - DGGOV/DGGO4
 *
 * =================================== DADOS DO SCRIPT =======================================================================================
 * DATA DE CRIACAO: 09-07-2019
 * AUTOR: MARIANA DE SA BRANDAO ROCHA DE OLIVEIRA
 * OBJETIVO: Criação de novas tabelas do Medição para atender a iteração 8 
 * PRE-REQUISITOS: N/A
 *
 * ================================= Historico de Execucoes ==================================================================================
 * DATA         | EXECUTOR          | TAREFA        | IP SERVIDOR   | BANCO DE DADOS                     | MOTIVO
 * -------------------------------------------------------------------------------------------------------------------------------------------
 * 10-07-2019   | Erison Galvão     | XXXXXXXXX     | 10.32.64.166  | siconv_mandatarias_desenv          |
 * 23-07-2019   | Samuel M. Oliveira| XXXXXXXXX     | 10.32.64.166  | siconv_mandatarias_desenv          | Criação da tabela 
 *                                                                    med_anotacao_registro_rt_submeta e
 *                                                                    associação com 
 *                                                                    med_anotacao_registro_rt_submeta e 
 *                                                                    alteração do nome campo tipo das
 *                                                                    tabelas med_contrato_resp_tecnico e
 *                                                                     med_anotacao_registro_rt
 * 30-07-2019   | Erison Galvão     | XXXXXXXXX     | 10.32.64.166  | siconv_mandatarias_desenv          | Criação da coluna telefone na tabela 
 *                                                    med_responsavel_tecnico
 * 01-08-2019   | Erison Galvão     | XXXXXXXXX     | 10.32.64.166  | siconv_mandatarias_desenv          | nr_art_rrt p/ character varying(50) 
 *                                                    
 * 09-08-2019   | Antulio Oliveira  | XXXXXXXXX     | 10.32.64.166  | siconv_mandatarias_desenv          | alteração do tipo do campo dt_inclusao
 *                                                                                                        de "date" para "timestamp" 
 *                                                                                                        tabela med-contrato_resp_tecnico  
 * 14-08-2019   | Antulio Oliveira  | XXXXXXXXX     | 10.31.0.134  | dbdes_mandatarias_desenv           | Inclusao de colunas de auditoria e
 *                                                                                                        versao (concorrencia) 
 * 22-11-2019   | Antulio Oliveira  | XXXXXXXXX     | 10.139.34.42 | dbdes_mandatarias_desenv           | Inclusao da tabela med_historico_medicao 
 *                                                  ! porta : 5432 !                                    !                                                                                                            
  **********************************************************************************************************************************************/

-------------------------------  Drop Modelo  -------------------------------

-- Drop tables

-- DROP TABLE siconv.med_anexo;
-- DROP TABLE siconv.med_observacao;
-- DROP TABLE siconv.med_submeta_medicao;
-- DROP TABLE siconv.med_item_medicao;
-- DROP TABLE siconv.med_medicao;
-- DROP TABLE siconv.med_anotacao_registro_rt_submeta;
-- DROP TABLE siconv.med_anotacao_registro_rt;
-- DROP TABLE siconv.med_contrato_resp_tecnico;
-- DROP TABLE siconv.med_contrato_rt_social_submeta;
-- DROP TABLE siconv.med_contrato_resp_tecnico_social;
-- DROP TABLE siconv.med_registro_profissional;
-- DROP TABLE siconv.med_responsavel_tecnico;
-- DROP TABLE siconv.med_doc_complementar_submeta;
-- DROP TABLE siconv.med_doc_complementar;
-- DROP TABLE siconv.med_historico_medicao;
-- DROP TABLE siconv.med_contrato;

-- Drop sequences

-- DROP SEQUENCE siconv.med_anexo_id_seq;
-- DROP SEQUENCE siconv.med_observacao_id_seq;
-- DROP SEQUENCE siconv.med_submeta_medicao_id_seq;
-- DROP SEQUENCE siconv.med_item_medicao_id_seq;
-- DROP SEQUENCE siconv.med_medicao_id_seq;
-- DROP SEQUENCE siconv.med_anotacao_registro_rt_submeta_id_seq;
-- DROP SEQUENCE siconv.med_anotacao_registro_rt_id_seq;
-- DROP SEQUENCE siconv.med_contrato_resp_tecnico_id_seq;
-- DROP SEQUENCE siconv.med_contrato_rt_social_submeta_id_seq;
-- DROP SEQUENCE siconv.med_contrato_resp_tecnico_social_id_seq;
-- DROP SEQUENCE siconv.med_registro_profissional_id_seq;
-- DROP SEQUENCE siconv.med_responsavel_tecnico_id_seq;
-- DROP SEQUENCE siconv.med_doc_complementar_submeta_id_seq;
-- DROP SEQUENCE siconv.med_doc_complementar_id_seq;
-- DROP SEQUENCE siconv.med_historico_medicao_id_seq;
-- DROP SEQUENCE siconv.med_contrato_id_seq;

-------------------------------  Tabela: med_contrato  -------------------------------

-- sequence 'med_contrato_id_seq'
create sequence siconv.med_contrato_id_seq;

CREATE TABLE siconv.med_contrato (
                id bigint NOT NULL DEFAULT nextval('med_contrato_id_seq'::regclass),
                dt_inicio_obra date NULL,
                cnpj_fornecedor character varying(255) NOT NULL,
                in_social boolean NOT NULL,
                contrato_fk bigint NOT NULL,
                proposta_fk bigint NOT NULL,
                versao bigint NOT NULL DEFAULT 0,
                adt_login character varying NOT NULL,
                adt_data_hora timestamp NOT NULL,
                adt_operacao character varying(6) NOT NULL,
                CONSTRAINT med_contrato_pk PRIMARY KEY (id),
                CONSTRAINT ck_med_contrato_adt_operacao CHECK (adt_operacao::text = ANY (ARRAY['INSERT'::character varying::text, 'UPDATE'::character varying::text, 'DELETE'::character varying::text]))
                
);

ALTER TABLE med_contrato OWNER TO owner_siconv_p;

COMMENT ON TABLE med_contrato
IS 'Tabela de Contrato de Licitação com Medição';
COMMENT ON COLUMN med_contrato.id IS 'ID de um Contrato';
COMMENT ON COLUMN med_contrato.dt_inicio_obra IS 'Data de Início da Obra do Contrato';
COMMENT ON COLUMN med_contrato.cnpj_fornecedor IS 'CNJP do Fornecedor';
COMMENT ON COLUMN med_contrato.in_social IS 'Indica se o contrato trata submetas do tipo Social';
COMMENT ON COLUMN med_contrato.contrato_fk IS 'FK da tabela Contrato do módulo Siconv';
COMMENT ON COLUMN med_contrato.proposta_fk IS 'FK da tabela Proposta do módulo SICONV';
COMMENT ON COLUMN med_contrato.versao IS 'Versão usada para controlar a concorrência';
COMMENT ON COLUMN med_contrato.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN med_contrato.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN med_contrato.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';



------------------------------- Tabela: med_medicao -----------------------------

-- sequence 'med_medicao_id_seq'
create sequence siconv.med_medicao_id_seq;

CREATE TABLE siconv.med_medicao (
    id bigint NOT NULL DEFAULT nextval('med_medicao_id_seq'::regclass),
    nr_sequencial smallint NOT NULL,
    dt_inicio date NOT NULL,
    dt_fim date NULL,
    in_situacao varchar(3) NOT NULL,
    med_contrato_fk bigint NOT NULL,
    versao bigint NOT NULL DEFAULT 0,
    adt_login character varying NOT NULL,
    adt_data_hora timestamp NOT NULL,
    adt_operacao character varying(6) NOT NULL,
    CONSTRAINT med_medicao_pk PRIMARY KEY (id),
    CONSTRAINT fkc_med_medicao_contrato_fk FOREIGN KEY (med_contrato_fk) REFERENCES med_contrato(id),
    CONSTRAINT ck_med_medicao_adt_operacao CHECK (adt_operacao::text = ANY (ARRAY['INSERT'::character varying::text, 'UPDATE'::character varying::text, 'DELETE'::character varying::text]))
);

ALTER TABLE med_medicao OWNER TO owner_siconv_p;
COMMENT ON TABLE med_medicao
  IS 'Tabela de Medição de um Contrato de Licitação';
COMMENT ON COLUMN med_medicao.id IS 'Id da Medição';
COMMENT ON COLUMN med_medicao.nr_sequencial IS 'Número Sequencial da Medição';
COMMENT ON COLUMN med_medicao.dt_inicio IS 'Data Inicial da Medição';
COMMENT ON COLUMN med_medicao.dt_fim IS 'Data Final da Medição';
COMMENT ON COLUMN med_medicao.in_situacao IS 'Indicador da Situação da Medição';
COMMENT ON COLUMN med_medicao.med_contrato_fk IS 'FK da tabela Contrato do módulo Medição';
COMMENT ON COLUMN med_medicao.versao IS 'Versão usada para controlar a concorrência';
COMMENT ON COLUMN med_medicao.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN med_medicao.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN med_medicao.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';


------------------------------- Tabela: med_observacao -----------------------------

-- sequence 'med_observacao_id_seq'
create sequence siconv.med_observacao_id_seq;

CREATE TABLE siconv.med_observacao (
    id bigint NOT NULL DEFAULT nextval('med_observacao_id_seq'::regclass),
    dt_registro timestamp NOT NULL,
    in_perfil_responsavel varchar(3) NOT NULL,
    nr_cpf_responsavel bpchar(11) NOT NULL,
    tx_observacao character varying (1000) NOT NULL,
    medicao_fk bigint NOT NULL,
    versao bigint NOT NULL DEFAULT 0,
    adt_login character varying NOT NULL,
    adt_data_hora timestamp NOT NULL,
    adt_operacao character varying(6) NOT NULL,
    CONSTRAINT med_observacao_pk PRIMARY KEY (id),
    CONSTRAINT fkc_med_observacao_medicao_fk FOREIGN KEY (medicao_fk) REFERENCES med_medicao(id)
);

ALTER TABLE med_observacao OWNER TO owner_siconv_p;
COMMENT ON TABLE med_observacao
  IS 'Tabela de Observações das Medições de um Contrato de Licitação';
COMMENT ON COLUMN med_observacao.id IS 'Id da Observação';
COMMENT ON COLUMN med_observacao.dt_registro IS 'Data/Hora de registro da Observação';
COMMENT ON COLUMN med_observacao.in_perfil_responsavel IS 'Perfil do usuário responsável pela Observação';
COMMENT ON COLUMN med_observacao.nr_cpf_responsavel IS 'CPF do usuário responsável pela Observação ';
COMMENT ON COLUMN med_observacao.tx_observacao IS 'Texto da Observação';
COMMENT ON COLUMN med_observacao.medicao_fk IS 'FK da tabela Medição';
COMMENT ON COLUMN med_observacao.versao IS 'Versão usada para controlar a concorrência';
COMMENT ON COLUMN med_observacao.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN med_observacao.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN med_observacao.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';


------------------------------- Tabela: med_anexo -----------------------------

-- sequence 'med_anexo_id_seq'
create sequence siconv.med_anexo_id_seq;

CREATE TABLE siconv.med_anexo (
    id bigint NOT NULL DEFAULT nextval('med_anexo_id_seq'::regclass),
    nm_arquivo varchar(100) NOT NULL,
    co_ceph varchar(1024) NOT NULL,
    observacao_fk bigint NOT NULL,
    versao bigint NOT NULL DEFAULT 0,
    adt_login character varying NOT NULL,
    adt_data_hora timestamp NOT NULL,
    adt_operacao character varying(6) NOT NULL,
    CONSTRAINT med_anexo_pk PRIMARY KEY (id),
    CONSTRAINT fkc_med_anexo_observacao_fk FOREIGN KEY (observacao_fk) REFERENCES med_observacao(id)
);

ALTER TABLE med_anexo OWNER TO owner_siconv_p;
COMMENT ON TABLE med_anexo
  IS 'Tabela de Anexos das Observações das Medições de um Contrato de Licitação';
COMMENT ON COLUMN med_anexo.id IS 'Id do Anexo';
COMMENT ON COLUMN med_anexo.nm_arquivo IS 'Nome do arquivo anexado';
COMMENT ON COLUMN med_anexo.co_ceph IS 'Código da chave utilizada no Ceph para identificar o arquivo';
COMMENT ON COLUMN med_anexo.observacao_fk IS 'FK da tabela Observação do módulo Medição';
COMMENT ON COLUMN med_anexo.versao IS 'Versão usada para controlar a concorrência';
COMMENT ON COLUMN med_anexo.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN med_anexo.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN med_anexo.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';



------------------------------- Tabela: med_submeta_medicao -----------------------------

-- sequence 'med_submeta_medicao_id_seq'
create sequence siconv.med_submeta_medicao_id_seq;

CREATE TABLE siconv.med_submeta_medicao (
    id bigint NOT NULL DEFAULT nextval('med_submeta_medicao_id_seq'::regclass),
    in_situacao varchar(3) NOT NULL,
    nr_cpf_resp_assinatura bpchar(11) NULL,
    dt_assinatura timestamp NULL,
    vrpl_submeta_fk bigint NOT NULL,
    medicao_fk bigint NOT NULL,
    versao bigint NOT NULL DEFAULT 0,
    adt_login character varying NOT NULL,
    adt_data_hora timestamp NOT NULL,
    adt_operacao character varying(6) NOT NULL,
    CONSTRAINT med_submeta_medicao_pk PRIMARY KEY (id),
    CONSTRAINT fkc_submeta_medicao_medicao_fk FOREIGN KEY (medicao_fk) REFERENCES med_medicao(id)
);

ALTER TABLE med_submeta_medicao OWNER TO owner_siconv_p;
COMMENT ON TABLE med_submeta_medicao
  IS 'Tabela Submetas das Medições de um Contrato de Licitação';
COMMENT ON COLUMN med_submeta_medicao.id IS 'Id da Submeta da Medição';
COMMENT ON COLUMN med_submeta_medicao.in_situacao IS 'Situação da Submeta da Medição';
COMMENT ON COLUMN med_submeta_medicao.nr_cpf_resp_assinatura IS 'CPF do responsável pela assinatura da Submeta da Medição';
COMMENT ON COLUMN med_submeta_medicao.dt_assinatura IS 'Data da assinatura da Submeta da Medição';
COMMENT ON COLUMN med_submeta_medicao.vrpl_submeta_fk IS 'FK da tabela Submeta do módulo VRPL';
COMMENT ON COLUMN med_submeta_medicao.medicao_fk IS 'FK da tabela Medição';
COMMENT ON COLUMN med_submeta_medicao.versao IS 'Versão usada para controlar a concorrência';
COMMENT ON COLUMN med_submeta_medicao.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN med_submeta_medicao.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN med_submeta_medicao.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';


------------------------------- Tabela: med_item_medicao -----------------------------

-- sequence 'med_item_medicao_id_seq'
create sequence siconv.med_item_medicao_id_seq;

CREATE TABLE siconv.med_item_medicao (
    id bigint NOT NULL DEFAULT nextval('med_item_medicao_id_seq'::regclass),
    vrpl_evento_fk bigint NOT NULL,
    vrpl_frente_obra_fk bigint NOT NULL,
    vrpl_submeta_fk bigint NOT NULL,
    vl_total_servicos numeric(17,2) NOT NULL DEFAULT 0,
    medicao_fk_empresa bigint NULL,
    medicao_fk_concedente bigint NULL,
    medicao_fk_convenente bigint NULL,
    med_contrato_fk bigint NOT NULL,
    versao bigint NOT NULL DEFAULT 0,
    adt_login character varying NOT NULL,
    adt_data_hora timestamp NOT NULL,
    adt_operacao character varying(6) NOT NULL,
    CONSTRAINT med_item_medicao_pk PRIMARY KEY (id),
    CONSTRAINT fkc_med_item_medicao_contrato_fk FOREIGN KEY (med_contrato_fk) REFERENCES med_contrato(id),
    CONSTRAINT fkc_med_item_medicao_medicao_fk_emp FOREIGN KEY (medicao_fk_empresa) REFERENCES med_medicao(id),
    CONSTRAINT fkc_med_item_medicao_medicao_fk_conc FOREIGN KEY (medicao_fk_concedente) REFERENCES med_medicao(id),
    CONSTRAINT fkc_med_item_medicao_medicao_fk_conv FOREIGN KEY (medicao_fk_convenente) REFERENCES med_medicao(id)
);

    
ALTER TABLE med_item_medicao OWNER TO owner_siconv_p;

COMMENT ON TABLE med_item_medicao
  IS 'Tabela de Itens da Submeta das Medições de um Contrato de Licitação';
COMMENT ON COLUMN med_item_medicao.id IS 'Id do Item';
COMMENT ON COLUMN med_item_medicao.med_contrato_fk IS 'FK da tabela Contrato do módulo Medição';
COMMENT ON COLUMN med_item_medicao.vrpl_submeta_fk IS 'FK da tabela Submeta do módulo VRPL';
COMMENT ON COLUMN med_item_medicao.vrpl_evento_fk IS 'FK da tabela Evento do módulo VRPL';
COMMENT ON COLUMN med_item_medicao.vrpl_frente_obra_fk IS 'FK da tabela Frente de Obra do módulo VRPL';
COMMENT ON COLUMN med_item_medicao.medicao_fk_empresa IS 'FK da medição onde a Empresa marcou o Evento/Frente de Obra como concluído';
COMMENT ON COLUMN med_item_medicao.medicao_fk_concedente IS 'FK da medição onde o Concedente marcou o Evento/Frente de Obra como concluído';
COMMENT ON COLUMN med_item_medicao.medicao_fk_convenente IS 'FK da medição onde o Convenente marcou o Evento/Frente de Obra como concluído';
COMMENT ON COLUMN med_item_medicao.vl_total_servicos IS 'Somatório do Valor dos Serviços envolvidos no Evento/Frente de Obra';
COMMENT ON COLUMN med_item_medicao.versao IS 'Versão usada para controlar a concorrência';
COMMENT ON COLUMN med_item_medicao.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN med_item_medicao.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN med_item_medicao.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';


-------------------------------  Tabela: med_responsavel_tecnico  -------------------------------

-- sequence 'med_responsavel_tecnico_id_seq'
create sequence siconv.med_responsavel_tecnico_id_seq;

CREATE TABLE siconv.med_responsavel_tecnico (
                id bigint NOT NULL DEFAULT nextval('med_responsavel_tecnico_id_seq'::regclass),
                nr_cpf bpchar(11) NOT NULL,
                telefone character varying(1024) NOT NULL,
                versao bigint NOT NULL DEFAULT 0,
                adt_login character varying NOT NULL,
                adt_data_hora timestamp NOT NULL,
                adt_operacao character varying(6) NOT NULL,
                CONSTRAINT med_responsavel_tecnico_pk PRIMARY KEY (id),
                CONSTRAINT med_responsavel_tecnico_nr_cpf_ukey UNIQUE (nr_cpf)
);

ALTER TABLE med_responsavel_tecnico OWNER TO owner_siconv_p;

COMMENT ON TABLE med_responsavel_tecnico
IS 'Tabela de Responsável Técnico';
COMMENT ON COLUMN med_responsavel_tecnico.id IS 'ID de um Responsável Técnico';
COMMENT ON COLUMN med_responsavel_tecnico.nr_cpf IS 'Número do CPF do Responsável Técnico';
COMMENT ON COLUMN med_responsavel_tecnico.telefone IS 'Telefone de contato do Responsável Técnico cadastrado no Medição.';
COMMENT ON COLUMN med_responsavel_tecnico.versao IS 'Versão usada para controlar a concorrência';
COMMENT ON COLUMN med_responsavel_tecnico.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN med_responsavel_tecnico.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN med_responsavel_tecnico.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';



-------------------------------  Tabela: med_registro_profissional  -------------------------------

-- sequence 'med_registro_profissional_id_seq'
create sequence siconv.med_registro_profissional_id_seq;

CREATE TABLE siconv.med_registro_profissional (
        id bigint NOT NULL DEFAULT nextval('med_registro_profissional_id_seq'::regclass),
        atividade varchar(3) NOT NULL,
        crea_cau varchar(50) NOT NULL,
        uf varchar(2) NOT NULL,
        med_responsavel_tecnico_fk bigint NOT NULL,
        versao bigint NOT NULL DEFAULT 0,
        adt_login character varying NOT NULL,
        adt_data_hora timestamp NOT NULL,
        adt_operacao character varying(6) NOT NULL,
        CONSTRAINT med_registro_profissional_pk PRIMARY KEY (id),
        CONSTRAINT fkc_med_responsavel_tecnico_fk FOREIGN KEY (med_responsavel_tecnico_fk) REFERENCES med_responsavel_tecnico(id)
);


ALTER TABLE med_registro_profissional OWNER TO owner_siconv_p;

COMMENT ON TABLE med_registro_profissional
IS 'Tabela de Registro Profissional de um Responsável Técnico';
COMMENT ON COLUMN med_registro_profissional.id IS 'ID de um Registro Profissional';
COMMENT ON COLUMN med_registro_profissional.atividade IS 'Atividade (Engenharia ou Arquitetura)';
COMMENT ON COLUMN med_registro_profissional.crea_cau IS 'Número do CREA(Engenharia) ou CAU(Arquitetura)';
COMMENT ON COLUMN med_registro_profissional.uf IS 'UF do registro profissional (CREA)';
COMMENT ON COLUMN med_registro_profissional.med_responsavel_tecnico_fk IS 'FK do Responsável Técnico';
COMMENT ON COLUMN med_registro_profissional.versao IS 'Versão usada para controlar a concorrência';
COMMENT ON COLUMN med_registro_profissional.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN med_registro_profissional.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN med_registro_profissional.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';


-------------------------------  Tabela: med_contrato_resp_tecnico  -------------------------------

-- sequence 'med_contrato_resp_tecnico_id_seq'
create sequence siconv.med_contrato_resp_tecnico_id_seq;

CREATE TABLE siconv.med_contrato_resp_tecnico (
        id bigint NOT NULL DEFAULT nextval('med_contrato_resp_tecnico_id_seq'::regclass),
        med_contrato_fk bigint NOT NULL,
    med_registro_profissional_fk bigint NOT NULL,
        dt_inclusao timestamp NOT NULL,
        in_tipo varchar(3) NOT NULL,
        versao bigint NOT NULL DEFAULT 0,
        adt_login character varying NOT NULL,
        adt_data_hora timestamp NOT NULL,
        adt_operacao character varying(6) NOT NULL,
        CONSTRAINT med_contrato_resp_tecnico_pk PRIMARY KEY (id),
        CONSTRAINT fkc_med_contrato_fk FOREIGN KEY (med_contrato_fk) REFERENCES med_contrato(id),
        CONSTRAINT fkc_med_registro_profissional_fk FOREIGN KEY (med_registro_profissional_fk) REFERENCES med_registro_profissional(id)
);

ALTER TABLE med_contrato_resp_tecnico OWNER TO owner_siconv_p;

COMMENT ON TABLE med_contrato_resp_tecnico
IS 'Tabela de Vinculo entre o Contrato, Responsável Técnico e Registro Profissional';
COMMENT ON COLUMN med_contrato_resp_tecnico.id IS 'ID de um Vinculo entre o Contrato, Responsável Técnico e Registro Profissional';
COMMENT ON COLUMN med_contrato_resp_tecnico.med_contrato_fk IS 'FK da tabela Contrato de Licitação com Medição ';
COMMENT ON COLUMN med_contrato_resp_tecnico.med_registro_profissional_fk IS 'FK da tabela de Registro Profissional de um Responsável Técnico';
COMMENT ON COLUMN med_contrato_resp_tecnico.dt_inclusao IS 'Data de registro do Vinculo entre o Contrato, Responsável Técnico e Registro Profissional';
COMMENT ON COLUMN med_contrato_resp_tecnico.in_tipo IS 'Tipo do Vinculo entre o Contrato, Responsável Técnico e Registro Profisional (Execução ou Fiscalização)';
COMMENT ON COLUMN med_contrato_resp_tecnico.versao IS 'Versão usada para controlar a concorrência';
COMMENT ON COLUMN med_contrato_resp_tecnico.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN med_contrato_resp_tecnico.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN med_contrato_resp_tecnico.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';


-------------------------------  Tabela: med_anotacao_registro_rt  -------------------------------

-- sequence 'med_anotacao_registro_rt_id_seq'
create sequence siconv.med_anotacao_registro_rt_id_seq;

CREATE TABLE siconv.med_anotacao_registro_rt (
    id bigint NOT NULL DEFAULT nextval('med_anotacao_registro_rt_id_seq'::regclass),
    nr_art_rrt character varying(50) NOT NULL,
    dt_emissao date NOT NULL,
    in_tipo varchar(3) NOT NULL,
    dt_inativacao date NULL,
    nm_arquivo varchar(100) NOT NULL,
    co_ceph varchar(1024) NOT NULL,
    med_contrato_resp_tecnico_fk bigint NOT NULL,
    versao bigint NOT NULL DEFAULT 0,
    adt_login character varying NOT NULL,
    adt_data_hora timestamp NOT NULL,
    adt_operacao character varying(6) NOT NULL,
    CONSTRAINT med_anotacao_registro_rt_pk PRIMARY KEY (id),
    CONSTRAINT fkc_med_contrato_resp_tecnico_fk FOREIGN KEY (med_contrato_resp_tecnico_fk) REFERENCES med_contrato_resp_tecnico(id)
);

ALTER TABLE med_anotacao_registro_rt OWNER TO owner_siconv_p;

COMMENT ON TABLE med_anotacao_registro_rt
IS 'Tabela de Anotação ou Registro de Responsbilidade Técnica';
COMMENT ON COLUMN med_anotacao_registro_rt.id IS 'ID de uma Anotação ou Registro de Responsbilidade Técnica';
COMMENT ON COLUMN med_anotacao_registro_rt.nr_art_rrt IS 'Número de uma Anotação ou Registro de Responsbilidade Técnica';
COMMENT ON COLUMN med_anotacao_registro_rt.dt_emissao IS 'Data da Emissão da Anotação ou Registro de Responsbilidade Técnica';
COMMENT ON COLUMN med_anotacao_registro_rt.in_tipo IS 'Tipo da Anotação ou Registro de Responsbilidade Técnica (Execução ou Fiscalização)';
COMMENT ON COLUMN med_anotacao_registro_rt.dt_inativacao IS 'Data da Inativação da Anotação ou Registro de Responsbilidade Técnica';
COMMENT ON COLUMN med_anotacao_registro_rt.nm_arquivo IS 'Nome do Arquivo Anexo da Anotação ou Registro de Responsbilidade Técnica';
COMMENT ON COLUMN med_anotacao_registro_rt.co_ceph IS 'Código da chave utilizada no Ceph para identificar o arquivo';
COMMENT ON COLUMN med_anotacao_registro_rt.med_contrato_resp_tecnico_fk IS 'FK da tabela de Vinculo entre o Contrato, Responsável Técnico e Registro Profissional';
COMMENT ON COLUMN med_anotacao_registro_rt.versao IS 'Versão usada para controlar a concorrência';
COMMENT ON COLUMN med_anotacao_registro_rt.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN med_anotacao_registro_rt.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN med_anotacao_registro_rt.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';


-------------------------------  Tabela: med_anotacao_registro_rt_submeta  -------------------------------
create sequence siconv.med_anotacao_registro_rt_submeta_id_seq;

CREATE TABLE siconv.med_anotacao_registro_rt_submeta (
        id bigint NOT NULL DEFAULT nextval('med_anotacao_registro_rt_submeta_id_seq'::regclass),
    vrpl_submeta_fk bigint NOT NULL,
    med_anotacao_registro_rt_fk bigint NOT NULL,
    versao bigint NOT NULL DEFAULT 0,
    adt_login character varying NOT NULL,
    adt_data_hora timestamp NOT NULL,
    adt_operacao character varying(6) NOT NULL,
    CONSTRAINT med_anotacao_registro_rt_submeta_pk PRIMARY KEY (id),
    CONSTRAINT fkc_med_anotacao_registro_rt_submeta_fk FOREIGN KEY (med_anotacao_registro_rt_fk) REFERENCES med_anotacao_registro_rt(id)
);

ALTER TABLE med_anotacao_registro_rt_submeta OWNER TO owner_siconv_p;

COMMENT ON TABLE med_anotacao_registro_rt_submeta IS 'Tabela de Submetas de Anotação ou Registro de Responsbilidade Técnica';
COMMENT ON COLUMN med_anotacao_registro_rt_submeta.id IS 'ID de uma Submeta da Anotação ou Registro de Responsbilidade Técnica';
COMMENT ON COLUMN med_anotacao_registro_rt_submeta.vrpl_submeta_fk IS 'FK da tabela Submeta do módulo VRPL';
COMMENT ON COLUMN med_anotacao_registro_rt_submeta.med_anotacao_registro_rt_fk IS 'FK da tabela de Anotação de Registro de Responsável Técnico';
COMMENT ON COLUMN med_anotacao_registro_rt_submeta.versao IS 'Versão usada para controlar a concorrência';
COMMENT ON COLUMN med_anotacao_registro_rt_submeta.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN med_anotacao_registro_rt_submeta.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN med_anotacao_registro_rt_submeta.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';


-------------------------------  Tabela: med_contrato_resp_tecnico_social  -------------------------------

-- sequence 'med_contrato_resp_tecnico_social_id_seq'
create sequence siconv.med_contrato_resp_tecnico_social_id_seq;

CREATE TABLE siconv.med_contrato_resp_tecnico_social (
        id bigint NOT NULL DEFAULT nextval('med_contrato_resp_tecnico_social_id_seq'::regclass),
        med_contrato_fk bigint NOT NULL,
        med_responsavel_tecnico_fk bigint NOT NULL,
        in_tipo varchar(3) NOT NULL,
        in_atividade varchar(3) NOT NULL,
        nm_arquivo_curriculo varchar(100) NOT NULL,
        co_ceph_curriculo varchar(1024) NOT NULL,
        nm_formacao varchar(100) NOT NULL,
        nm_registro_profissional varchar(100) NULL,
        nm_orgao_responsavel varchar(100) NULL,
        nr_telefone_orgao character varying(15) NULL,
        tx_email_orgao varchar(100) NULL,     
        dt_inclusao timestamp NOT NULL,
        dt_inativacao timestamp NULL,
        versao bigint NOT NULL DEFAULT 0,
        adt_login character varying NOT NULL,
        adt_data_hora timestamp NOT NULL,
        adt_operacao character varying(6) NOT NULL,
        CONSTRAINT med_contrato_resp_tecnico_social_pk PRIMARY KEY (id),
        CONSTRAINT fkc_med_contrato_fk FOREIGN KEY (med_contrato_fk) REFERENCES med_contrato(id),
        CONSTRAINT fkc_med_responsavel_tecnico_fk FOREIGN KEY (med_responsavel_tecnico_fk) REFERENCES med_responsavel_tecnico(id)
);

ALTER TABLE med_contrato_resp_tecnico_social OWNER TO owner_siconv_p;

COMMENT ON TABLE med_contrato_resp_tecnico_social IS 'Tabela com os dados do responsável técnico social e seu registro profissional em determinado contrato';
COMMENT ON COLUMN med_contrato_resp_tecnico_social.id IS 'ID do contrato responsável técnico social';
COMMENT ON COLUMN med_contrato_resp_tecnico_social.med_contrato_fk IS 'FK da tabela med_contrato';
COMMENT ON COLUMN med_contrato_resp_tecnico_social.med_responsavel_tecnico_fk IS 'FK da tabela med_responsavel_tecnico';
COMMENT ON COLUMN med_contrato_resp_tecnico_social.in_tipo IS 'Tipo do Responsável Técnico, referente à sua atuação no contexto do acompanhamento da obra/serviço (EXE: Execução, FIS: Fiscalização)';
COMMENT ON COLUMN med_contrato_resp_tecnico_social.in_atividade IS 'Indicador de Atividade: SOC (Trabalho Social)';
COMMENT ON COLUMN med_contrato_resp_tecnico_social.nm_arquivo_curriculo IS 'Nome do arquivo do currículo do responsável técnico social';
COMMENT ON COLUMN med_contrato_resp_tecnico_social.co_ceph_curriculo IS 'Código Ceph do arquivo do currículo do responsável técnico social';
COMMENT ON COLUMN med_contrato_resp_tecnico_social.nm_formacao IS 'Nome da formação profissional do Responsável Técnico Social';
COMMENT ON COLUMN med_contrato_resp_tecnico_social.nm_registro_profissional IS 'Registro Profissional do Responsável Técnico no respectivo
conselho.';
COMMENT ON COLUMN med_contrato_resp_tecnico_social.nm_orgao_responsavel IS 'Nome do órgão do responsável técnico.';
COMMENT ON COLUMN med_contrato_resp_tecnico_social.nr_telefone_orgao IS 'Telefone do órgão do responsável técnico.';
COMMENT ON COLUMN med_contrato_resp_tecnico_social.tx_email_orgao IS 'E-mail do órgão do responsável técnico.';
COMMENT ON COLUMN med_contrato_resp_tecnico_social.dt_inclusao IS 'Data da inclusão do responsável técnico social';
COMMENT ON COLUMN med_contrato_resp_tecnico_social.dt_inativacao IS 'Data da inativação do responsável técnico social';
COMMENT ON COLUMN med_contrato_resp_tecnico_social.versao IS 'Versão usada para controlar a concorrência';
COMMENT ON COLUMN med_contrato_resp_tecnico_social.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN med_contrato_resp_tecnico_social.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN med_contrato_resp_tecnico_social.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';


-------------------------------  Tabela: med_contrato_rt_social_submeta  -------------------------------
create sequence siconv.med_contrato_rt_social_submeta_id_seq;

CREATE TABLE siconv.med_contrato_rt_social_submeta (
    id bigint NOT NULL DEFAULT nextval('med_contrato_rt_social_submeta_id_seq'::regclass),  
    vrpl_submeta_fk bigint NOT NULL,
    med_contrato_resp_tecnico_social_fk bigint NOT NULL,
    versao bigint NOT NULL DEFAULT 0,
    adt_login character varying NOT NULL,
    adt_data_hora timestamp NOT NULL,
    adt_operacao character varying(6) NOT NULL,
    CONSTRAINT med_contrato_rt_social_submeta_pk PRIMARY KEY (id),
    CONSTRAINT fkc_med_contrato_resp_tecnico_social_fk FOREIGN KEY (med_contrato_resp_tecnico_social_fk) REFERENCES med_contrato_resp_tecnico_social(id)
);

ALTER TABLE med_contrato_rt_social_submeta OWNER TO owner_siconv_p;

COMMENT ON TABLE med_contrato_rt_social_submeta IS 'Tabela de Submetas do Contrato Responsável Técnico Social';
COMMENT ON COLUMN med_contrato_rt_social_submeta.id IS 'ID de uma Submeta associada ao contrato de responsável técnico social';
COMMENT ON COLUMN med_contrato_rt_social_submeta.vrpl_submeta_fk IS 'FK da tabela Submeta do módulo VRPL';
COMMENT ON COLUMN med_contrato_rt_social_submeta.med_contrato_resp_tecnico_social_fk IS 'FK da tabela de Contrato Responsável Técnico Social';
COMMENT ON COLUMN med_contrato_rt_social_submeta.versao IS 'Versão usada para controlar a concorrência';
COMMENT ON COLUMN med_contrato_rt_social_submeta.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN med_contrato_rt_social_submeta.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN med_contrato_rt_social_submeta.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';

-------------------------------  Tabela: med_doc_complementar  -------------------------------

-- sequence 'med_doc_complementar_id_seq'
create sequence siconv.med_doc_complementar_id_seq;

CREATE TABLE siconv.med_doc_complementar (
    id bigint NOT NULL DEFAULT nextval('med_doc_complementar_id_seq'::regclass),
    in_tipo_documento varchar(3) NOT NULL,
    in_tipo_manifesto varchar(3) NULL,
    dt_emissao date NULL,
    dt_validade date NULL,
    nr_documento varchar(40) NULL,
    tx_descricao varchar(100) NULL,
    nm_orgao_emissor varchar(100) NULL,
    nm_arquivo varchar(100) NOT NULL,
    co_ceph varchar(1024) NOT NULL,
    med_contrato_fk bigint NOT NULL,
    versao bigint NOT NULL DEFAULT 0,
    adt_login character varying NOT NULL,
    adt_data_hora timestamp NOT NULL,
    adt_operacao character varying(6) NOT NULL,
    CONSTRAINT med_doc_complementar_pk PRIMARY KEY (id),
    CONSTRAINT fkc_med_contrato_fk FOREIGN KEY (med_contrato_fk) REFERENCES med_contrato(id)
);

ALTER TABLE med_doc_complementar OWNER TO owner_siconv_p;

COMMENT ON TABLE med_doc_complementar
IS 'Tabela de Documento Complementar';
COMMENT ON COLUMN med_doc_complementar.id IS 'ID de um Documento Complementar';
COMMENT ON COLUMN med_doc_complementar.in_tipo_documento IS 'Tipo do Documento (Autorização/Declaração/Manifesto Ambiental/Ordem de Serviço/Outorga/Outros)';
COMMENT ON COLUMN med_doc_complementar.in_tipo_manifesto IS 'Tipo do Manifesto (Dispensa/Licença Prévia/Licença de Instalação/Licença de Operacão/Protocolo)';
COMMENT ON COLUMN med_doc_complementar.dt_emissao IS 'Data de Emissão do Documento Complementar';
COMMENT ON COLUMN med_doc_complementar.dt_validade IS 'Data de Validade do Documento Complementar';
COMMENT ON COLUMN med_doc_complementar.nr_documento IS 'Número do Documento Complementar';
COMMENT ON COLUMN med_doc_complementar.tx_descricao IS 'Descrição do Documento Complementar';
COMMENT ON COLUMN med_doc_complementar.nm_orgao_emissor IS 'Orgão Emissor do Documento Complementar';
COMMENT ON COLUMN med_doc_complementar.nm_arquivo IS 'Nome do Arquivo Anexo da Anotação ou Registro de Responsbilidade Técnica';
COMMENT ON COLUMN med_doc_complementar.co_ceph IS 'Código da chave utilizada no Ceph para identificar o arquivo';
COMMENT ON COLUMN med_doc_complementar.med_contrato_fk IS 'FK da tabela de Contrato do Medição';
COMMENT ON COLUMN med_doc_complementar.versao IS 'Versão usada para controlar a concorrência';
COMMENT ON COLUMN med_doc_complementar.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN med_doc_complementar.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN med_doc_complementar.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';


-------------------------------  Tabela: med_doc_complementar_submeta  -------------------------------
create sequence siconv.med_doc_complementar_submeta_id_seq;

CREATE TABLE siconv.med_doc_complementar_submeta (
    id bigint NOT NULL DEFAULT nextval('med_doc_complementar_submeta_id_seq'::regclass),
    vrpl_submeta_fk bigint NOT NULL,
    med_doc_complementar_fk bigint NOT NULL,
    versao bigint NOT NULL DEFAULT 0,
    adt_login character varying NOT NULL,
    adt_data_hora timestamp NOT NULL,
    adt_operacao character varying(6) NOT NULL,
    CONSTRAINT med_doc_complementar_submeta_pk PRIMARY KEY (id),
    CONSTRAINT fkc_med_doc_complementar_fk FOREIGN KEY (med_doc_complementar_fk) REFERENCES med_doc_complementar(id)
);

ALTER TABLE med_doc_complementar_submeta OWNER TO owner_siconv_p;

COMMENT ON TABLE med_doc_complementar_submeta IS 'Tabela de Submetas do Documento Complementar';
COMMENT ON COLUMN med_doc_complementar_submeta.id IS 'ID de uma Submeta do Documento Complementar';
COMMENT ON COLUMN med_doc_complementar_submeta.vrpl_submeta_fk IS 'FK da tabela Submeta do módulo VRPL';
COMMENT ON COLUMN med_doc_complementar_submeta.med_doc_complementar_fk IS 'FK da tabela de Documento Complementar';
COMMENT ON COLUMN med_doc_complementar_submeta.versao IS 'Versão usada para controlar a concorrência';
COMMENT ON COLUMN med_doc_complementar_submeta.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN med_doc_complementar_submeta.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN med_doc_complementar_submeta.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';

------------------------------- Tabela: med_historico_medicao -----------------------------

-- sequence 'med_historico_medicao_id_seq'
create sequence siconv.med_historico_medicao_id_seq;

CREATE TABLE siconv.med_historico_medicao (
   
    id                    bigint               NOT NULL DEFAULT nextval('med_historico_medicao_id_seq'::regclass),
    med_contrato_fk       bigint               NOT NULL,
    nr_cpf_responsavel    bpchar(11)           NOT NULL,
    in_perfil_responsavel varchar(3)           NOT NULL,
    nr_sequencial         smallint             NOT NULL,
    in_situacao           varchar(3)           NOT NULL,
    versao                bigint               NOT NULL DEFAULT 0,
    adt_login             character varying    NOT NULL,
    adt_data_hora         timestamp            NOT NULL,
    adt_operacao          character varying(6) NOT NULL,
   
    CONSTRAINT med_historico_medicao_pk        PRIMARY KEY (id),
    CONSTRAINT fkc_med__historico_medicao_fk   FOREIGN KEY (med_contrato_fk) REFERENCES med_contrato(id)
);

ALTER TABLE med_historico_medicao OWNER TO owner_siconv_p;

COMMENT ON TABLE med_historico_medicao IS 'Tabela de historico de medições de um contrato de execução';

COMMENT ON COLUMN med_historico_medicao.id                     IS 'Id do historico da medicao';
COMMENT ON COLUMN med_historico_medicao.med_contrato_fk        IS 'FK da tabela Contrato do módulo Medição';
COMMENT ON COLUMN med_historico_medicao.nr_cpf_responsavel     IS 'CPF do usuário responsável pela medicao';
COMMENT ON COLUMN med_historico_medicao.in_perfil_responsavel  IS 'Perfil do usuário responsável pela medicao';
COMMENT ON COLUMN med_historico_medicao.nr_sequencial          IS 'Número Sequencial da Medição';
COMMENT ON COLUMN med_historico_medicao.in_situacao            IS 'Indicador da Situação da Medição';
COMMENT ON COLUMN med_historico_medicao.versao                 IS 'Versão usada para controlar a concorrência';
COMMENT ON COLUMN med_historico_medicao.adt_login              IS 'Usuário que criou o registro';
COMMENT ON COLUMN med_historico_medicao.adt_data_hora          IS 'Data/Hora de criação do registro';
COMMENT ON COLUMN med_historico_medicao.adt_operacao           IS 'Operacão (INSERT) no registro';


grant all                                        on table med_contrato                              to owner_siconv_p;
grant select,insert, update, delete, references  on table med_contrato                              to usr_siconv_p;
grant select,usage                               on sequence med_contrato_id_seq                    to usr_siconv_p;

grant all                                        on table med_anexo                                 to owner_siconv_p;
grant select,insert, update, delete, references  on table med_anexo                                 to usr_siconv_p;
grant select,usage                               on sequence med_anexo_id_seq                       to usr_siconv_p;

grant all                                        on table med_observacao                            to owner_siconv_p;
grant select,insert, update, delete, references  on table med_observacao                            to usr_siconv_p;
grant select,usage                               on sequence med_observacao_id_seq                  to usr_siconv_p;

grant all                                        on table med_submeta_medicao                       to owner_siconv_p;
grant select,insert, update, delete, references  on table med_submeta_medicao                       to usr_siconv_p;
grant select, usage                              on sequence med_submeta_medicao_id_seq             to usr_siconv_p;

grant all                                        on table med_item_medicao                          to owner_siconv_p;
grant select,insert, update, delete, references  on table med_item_medicao                          to usr_siconv_p;
grant select,usage                               on sequence med_item_medicao_id_seq                to usr_siconv_p;

grant all                                        on table med_medicao                               to owner_siconv_p;
grant select,insert, update, delete, references  on table med_medicao                               to usr_siconv_p;
grant select, usage                              on sequence med_medicao_id_seq                     to usr_siconv_p;

grant all                                        on table med_responsavel_tecnico                   to owner_siconv_p;
grant select,insert, update, delete, references  on table med_responsavel_tecnico                   to usr_siconv_p;
grant select,update, usage                       on sequence med_responsavel_tecnico_id_seq         to usr_siconv_p;

grant all                                        on table med_registro_profissional                 to owner_siconv_p;
grant select,insert, update, delete, references  on table med_registro_profissional                 to usr_siconv_p;
grant select, usage                              on sequence med_registro_profissional_id_seq       to usr_siconv_p;

grant all                                        on table med_contrato_resp_tecnico                 to owner_siconv_p;
grant select,insert, update, delete, references  on table med_contrato_resp_tecnico                 to usr_siconv_p;
grant select, usage                              on sequence med_contrato_resp_tecnico_id_seq       to usr_siconv_p;

grant all                                        on table med_anotacao_registro_rt                  to owner_siconv_p;
grant select,insert, update, delete, references  on table med_anotacao_registro_rt                  to usr_siconv_p;
grant select, usage                              on sequence med_anotacao_registro_rt_id_seq        to usr_siconv_p;

grant all                                        on table med_anotacao_registro_rt_submeta          to owner_siconv_p;
grant select,insert, update, delete, references  on table med_anotacao_registro_rt_submeta          to usr_siconv_p;
grant select, usage                              on sequence med_anotacao_registro_rt_submeta_id_seq    to usr_siconv_p;

grant all                                        on table med_contrato_resp_tecnico_social          to owner_siconv_p;
grant select,insert, update, delete, references  on table med_contrato_resp_tecnico_social          to usr_siconv_p;
grant select, usage                              on sequence med_contrato_resp_tecnico_social_id_seq    to usr_siconv_p;

grant all                                        on table med_contrato_rt_social_submeta          to owner_siconv_p;
grant select,insert, update, delete, references  on table med_contrato_rt_social_submeta          to usr_siconv_p;
grant select, usage                              on sequence med_contrato_rt_social_submeta_id_seq    to usr_siconv_p;

grant all                                        on table med_doc_complementar                  to owner_siconv_p;
grant select,insert, update, delete, references  on table med_doc_complementar                  to usr_siconv_p;
grant select, usage                              on sequence med_doc_complementar_id_seq        to usr_siconv_p;

grant all                                        on table med_doc_complementar_submeta          to owner_siconv_p;
grant select,insert, update, delete, references  on table med_doc_complementar_submeta          to usr_siconv_p;
grant select, usage                              on sequence med_doc_complementar_submeta_id_seq  to usr_siconv_p;

grant all                                        on table med_historico_medicao                  to owner_siconv_p;
grant select,insert, update, delete, references  on table med_historico_medicao                  to usr_siconv_p;
grant select,usage                               on sequence med_historico_medicao_id_seq        to usr_siconv_p;
