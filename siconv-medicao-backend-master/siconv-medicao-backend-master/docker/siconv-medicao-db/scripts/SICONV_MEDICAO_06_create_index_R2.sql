/*
* ======================================== DADOS DO SCRIPT =================================================
* AUTOR: Gustavo Diniz
* OBJETIVO: Criacao de indices para as tabelas de negocio
* PRE-REQUISITOS: Modelo da release 2 previamente aplicado
*/

-- Tabela med_contrato
CREATE INDEX idx_med_contrato_contrato_fk ON
siconv.med_contrato (contrato_fk);

-- Tabela med_historico_medicao
CREATE INDEX idx_med_historico_medicao_med_contrato_fk ON
siconv.med_historico_medicao (med_contrato_fk);

-- Tabela med_medicao
CREATE INDEX idx_med_medicao_medicao_fk_agrupadora ON
siconv.med_medicao (medicao_fk_agrupadora);

CREATE INDEX idx_med_medicao_med_contrato_fk ON
siconv.med_medicao (med_contrato_fk);

-- Tabela med_item_medicao
CREATE INDEX idx_med_item_medicao_medicao_fk_empresa ON
siconv.med_item_medicao (medicao_fk_empresa);

CREATE INDEX idx_med_item_medicao_medicao_fk_concedente ON
siconv.med_item_medicao (medicao_fk_concedente);

CREATE INDEX idx_med_item_medicao_medicao_fk_convenente ON
siconv.med_item_medicao (medicao_fk_convenente);

CREATE INDEX idx_med_item_medicao_med_contrato_fk ON
siconv.med_item_medicao (med_contrato_fk);

CREATE INDEX idx_med_item_medicao_vrpl_submeta_fk ON
siconv.med_item_medicao (vrpl_submeta_fk);

-- Tabela med_submeta_medicao
CREATE INDEX idx_med_submeta_medicao_vrpl_submeta_fk ON
siconv.med_submeta_medicao (vrpl_submeta_fk);

CREATE INDEX idx_med_submeta_medicao_medicao_fk ON
siconv.med_submeta_medicao (medicao_fk);

-- Tabela med_observacao
CREATE INDEX idx_med_observacao_medicao_fk ON
siconv.med_observacao (medicao_fk);

-- Tabela med_anexo
CREATE INDEX idx_med_anexo_observacao_fk ON
siconv.med_anexo (observacao_fk);

-- Tabela med_registro_profissional
CREATE INDEX idx_med_registro_profissional_med_responsavel_tecnico_fk ON
siconv.med_registro_profissional (med_responsavel_tecnico_fk);

-- Tabela med_contrato_resp_tecnico
CREATE INDEX idx_med_contrato_resp_tecnico_med_contrato_fk ON
siconv.med_contrato_resp_tecnico (med_contrato_fk);

CREATE INDEX idx_med_contrato_resp_tecnico_med_registro_profissional_fk ON
siconv.med_contrato_resp_tecnico (med_registro_profissional_fk);

-- Tabela med_anotacao_registro_rt
CREATE INDEX idx_med_anotacao_registro_rt_med_contrato_resp_tecnico_fk ON
siconv.med_anotacao_registro_rt (med_contrato_resp_tecnico_fk);

-- Tabela med_anotacao_registro_rt_submeta
CREATE INDEX idx_med_anotacao_reg_rt_submeta_med_anotacao_registro_rt_fk ON
siconv.med_anotacao_registro_rt_submeta (med_anotacao_registro_rt_fk);

-- Tabela med_contrato_resp_tecnico_social
CREATE INDEX idx_med_contrato_resp_tecnico_social_med_contrato_fk ON
siconv.med_contrato_resp_tecnico_social (med_contrato_fk);

CREATE INDEX idx_med_contrato_resp_tecnico_social_med_responsavel_tecnico_fk ON
siconv.med_contrato_resp_tecnico_social (med_responsavel_tecnico_fk);

-- Tabela med_contrato_rt_social_submeta
CREATE INDEX idx_med_contrato_rt_social_sub_med_contrato_resp_tec_social_fk ON
siconv.med_contrato_rt_social_submeta (med_contrato_resp_tecnico_social_fk);

-- Tabela med_doc_complementar
CREATE INDEX idx_med_doc_complementar_med_contrato_fk ON
siconv.med_doc_complementar (med_contrato_fk);

-- Tabela med_doc_complementar_submeta
CREATE INDEX idx_med_doc_complementar_submeta_med_doc_complementar_fk ON
siconv.med_doc_complementar_submeta (med_doc_complementar_fk);


