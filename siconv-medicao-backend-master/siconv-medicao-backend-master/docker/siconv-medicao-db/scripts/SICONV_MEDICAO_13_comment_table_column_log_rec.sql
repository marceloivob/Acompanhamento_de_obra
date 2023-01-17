COMMENT ON TABLE siconv.med_anexo_log_rec IS 'Tabela que registra o log de Anexos das Observações das Medições de um Contrato de Licitação';

COMMENT ON COLUMN siconv.med_anexo_log_rec.id IS 'Identificador único do Registro de Log da tabela Anexo';
COMMENT ON COLUMN siconv.med_anexo_log_rec.entity_id IS 'Identificador único do Anexo';
COMMENT ON COLUMN siconv.med_anexo_log_rec.nm_arquivo IS 'Nome do arquivo anexado';
COMMENT ON COLUMN siconv.med_anexo_log_rec.co_ceph IS 'Código da chave utilizada no Ceph para identificar o arquivo';
COMMENT ON COLUMN siconv.med_anexo_log_rec.observacaolog IS 'FK da tabela Observação do módulo Medição';
COMMENT ON COLUMN siconv.med_anexo_log_rec.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN siconv.med_anexo_log_rec.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN siconv.med_anexo_log_rec.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';
COMMENT ON COLUMN siconv.med_anexo_log_rec.in_inativo IS 'Indicador se o anexo da observação está inativo';
COMMENT ON COLUMN siconv.med_anexo_log_rec.nr_cpf_inativo IS 'Número do CPF do usuário que inativou o anexo.';


COMMENT ON TABLE siconv.med_anotacao_registro_rt_log_rec IS 'Tabela que registra o log de Anotação ou Registro de Responsbilidade Técnica';

COMMENT ON COLUMN siconv.med_anotacao_registro_rt_log_rec.id IS 'Identificador único do Registro de Log da tabela de Anotação ou Registro de Responsbilidade Técnica';
COMMENT ON COLUMN siconv.med_anotacao_registro_rt_log_rec.entity_id IS 'Identificador único de uma Anotação ou Registro de Responsbilidade Técnica';
COMMENT ON COLUMN siconv.med_anotacao_registro_rt_log_rec.nr_art_rrt IS 'Número de uma Anotação ou Registro de Responsbilidade Técnica';
COMMENT ON COLUMN siconv.med_anotacao_registro_rt_log_rec.dt_emissao IS 'Data da Emissão da Anotação ou Registro de Responsbilidade Técnica';
COMMENT ON COLUMN siconv.med_anotacao_registro_rt_log_rec.in_tipo IS 'Tipo da Anotação ou Registro de Responsbilidade Técnica (Execução ou Fiscalização)';
COMMENT ON COLUMN siconv.med_anotacao_registro_rt_log_rec.dt_inativacao IS 'Data da Inativação da Anotação ou Registro de Responsbilidade Técnica';
COMMENT ON COLUMN siconv.med_anotacao_registro_rt_log_rec.nm_arquivo IS 'Nome do Arquivo Anexo da Anotação ou Registro de Responsbilidade Técnica';
COMMENT ON COLUMN siconv.med_anotacao_registro_rt_log_rec.co_ceph IS 'Código da chave utilizada no Ceph para identificar o arquivo';
COMMENT ON COLUMN siconv.med_anotacao_registro_rt_log_rec.medcontratoresptecnicolog IS 'FK da tabela de Vinculo entre o Contrato, Responsável Técnico e Registro Profissional';
COMMENT ON COLUMN siconv.med_anotacao_registro_rt_log_rec.versao IS 'Versão usada para controlar a concorrência';
COMMENT ON COLUMN siconv.med_anotacao_registro_rt_log_rec.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN siconv.med_anotacao_registro_rt_log_rec.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN siconv.med_anotacao_registro_rt_log_rec.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';


COMMENT ON TABLE siconv.med_anotacao_registro_rt_submeta_log_rec IS 'Tabela que registra o log de Submetas de Anotação ou Registro de Responsbilidade Técnica';

COMMENT ON COLUMN siconv.med_anotacao_registro_rt_submeta_log_rec.id IS 'Identificador único do Registro de Log na tabela para uma Submeta da Anotação ou Registro de Responsbilidade Técnica';
COMMENT ON COLUMN siconv.med_anotacao_registro_rt_submeta_log_rec.entity_id IS 'Identificador único de uma Submeta da Anotação ou Registro de Responsbilidade Técnica';
COMMENT ON COLUMN siconv.med_anotacao_registro_rt_submeta_log_rec.vrpl_submeta_fk IS 'FK da tabela Submeta do módulo VRPL';
COMMENT ON COLUMN siconv.med_anotacao_registro_rt_submeta_log_rec.medanotacaoregistrortlog IS 'FK da tabela de Anotação de Registro de Responsável Técnico';
COMMENT ON COLUMN siconv.med_anotacao_registro_rt_submeta_log_rec.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN siconv.med_anotacao_registro_rt_submeta_log_rec.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN siconv.med_anotacao_registro_rt_submeta_log_rec.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';


COMMENT ON TABLE siconv.med_contrato_log_rec IS 'Tabela que registra o log de Contrato de Licitação com Medição';

COMMENT ON COLUMN siconv.med_contrato_log_rec.id IS 'Identificador único de um do Registro de Log na tabela de Contrato';
COMMENT ON COLUMN siconv.med_contrato_log_rec.entity_id IS 'Identificador único de um Contrato';
COMMENT ON COLUMN siconv.med_contrato_log_rec.dt_inicio_obra IS 'Data de Início da Obra do Contrato';
COMMENT ON COLUMN siconv.med_contrato_log_rec.cnpj_fornecedor IS 'CNJP do Fornecedor';
COMMENT ON COLUMN siconv.med_contrato_log_rec.in_social IS 'Indica se o contrato trata submetas do tipo Social';
COMMENT ON COLUMN siconv.med_contrato_log_rec.contrato_fk IS 'FK da tabela Contrato do módulo Siconv';
COMMENT ON COLUMN siconv.med_contrato_log_rec.proposta_fk IS 'FK da tabela Proposta do módulo SICONV';
COMMENT ON COLUMN siconv.med_contrato_log_rec.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN siconv.med_contrato_log_rec.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN siconv.med_contrato_log_rec.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';


COMMENT ON TABLE siconv.med_contrato_resp_tecnico_log_rec IS 'Tabela que registra o log de Vinculo entre o Contrato, Responsável Técnico e Registro Profissional';

COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_log_rec.id IS 'Identificador único de um do Registro de Log na tabela de Vinculo entre o Contrato, Responsável Técnico e Registro Profissional';
COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_log_rec.entity_id IS 'Identificador único de um Vinculo entre o Contrato, Responsável Técnico e Registro Profissional';
COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_log_rec.medcontratolog IS 'FK da tabela Contrato de Licitação com Medição ';
COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_log_rec.medregistroprofissionallog IS 'FK da tabela de Registro Profissional de um Responsável Técnico';
COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_log_rec.dt_inclusao IS 'Data de registro do Vinculo entre o Contrato, Responsável Técnico e Registro Profissional';
COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_log_rec.in_tipo IS 'Tipo do Vinculo entre o Contrato, Responsável Técnico e Registro Profisional (Execução ou Fiscalização)';
COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_log_rec.versao IS 'Versão usada para controlar a concorrência';
COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_log_rec.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_log_rec.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_log_rec.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';


COMMENT ON TABLE siconv.med_contrato_resp_tecnico_social_log_rec IS 'Tabela que registra o log com os dados do responsável técnico social e seu registro profissional em determinado contrato';

COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_social_log_rec.id IS 'Identificador único de um do Registro de Log na tabela de Vinculo do Contrato e  Responsável Técnico social';
COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_social_log_rec.entity_id IS 'Identificador único do Vinculo do Contrato e  Responsável Técnico social';
COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_social_log_rec.medcontratolog IS 'FK da tabela med_contrato';
COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_social_log_rec.medresponsaveltecnicolog IS 'FK da tabela med_responsavel_tecnico_log_rec';
COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_social_log_rec.in_tipo IS 'Tipo do Responsável Técnico, referente à sua atuação no contexto do acompanhamento da obra/serviço (EXE: Execução, FIS: Fiscalização)';
COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_social_log_rec.in_atividade IS 'Indicador de Atividade: SOC (Trabalho Social)';
COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_social_log_rec.nm_arquivo_curriculo IS 'Nome do arquivo do currículo do responsável técnico social';
COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_social_log_rec.co_ceph_curriculo IS 'Código Ceph do arquivo do currículo do responsável técnico social';
COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_social_log_rec.nm_formacao IS 'Nome da formação profissional do Responsável Técnico Social';
COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_social_log_rec.nm_registro_profissional IS 'Registro Profissional do Responsável Técnico no respectivo
conselho.';
COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_social_log_rec.nm_orgao_responsavel IS 'Nome do órgão do responsável técnico.';
COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_social_log_rec.nr_telefone_orgao IS 'Telefone do órgão do responsável técnico.';
COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_social_log_rec.tx_email_orgao IS 'E-mail do órgão do responsável técnico.';
COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_social_log_rec.dt_inclusao IS 'Data da inclusão do responsável técnico social';
COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_social_log_rec.dt_inativacao IS 'Data da inativação do responsável técnico social';
COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_social_log_rec.versao IS 'Versão usada para controlar a concorrência';
COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_social_log_rec.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_social_log_rec.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN siconv.med_contrato_resp_tecnico_social_log_rec.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';


COMMENT ON TABLE siconv.med_contrato_rt_social_submeta_log_rec IS 'Tabela que registra o log de Submetas do Contrato Responsável Técnico Social';

COMMENT ON COLUMN siconv.med_contrato_rt_social_submeta_log_rec.id IS 'Identificador único de um do Registro de Log de uma Submeta associada ao contrato de responsável técnico social';
COMMENT ON COLUMN siconv.med_contrato_rt_social_submeta_log_rec.entity_id IS 'Identificador único de uma Submeta associada ao contrato de responsável técnico social';
COMMENT ON COLUMN siconv.med_contrato_rt_social_submeta_log_rec.vrpl_submeta_fk IS 'FK da tabela Submeta do módulo VRPL';
COMMENT ON COLUMN siconv.med_contrato_rt_social_submeta_log_rec.medcontratoresptecnicosociallog IS 'FK da tabela de Contrato Responsável Técnico Social';
COMMENT ON COLUMN siconv.med_contrato_rt_social_submeta_log_rec.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN siconv.med_contrato_rt_social_submeta_log_rec.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN siconv.med_contrato_rt_social_submeta_log_rec.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';


COMMENT ON TABLE siconv.med_doc_complementar_log_rec IS 'Tabela que registra o log de Documento Complementar';

COMMENT ON COLUMN siconv.med_doc_complementar_log_rec.id IS 'Identificador único de um do Registro de Log na tabela Documento Complementar';
COMMENT ON COLUMN siconv.med_doc_complementar_log_rec.entity_id IS 'Identificador único de um Documento Complementar';
COMMENT ON COLUMN siconv.med_doc_complementar_log_rec.in_tipo_documento IS 'Tipo do Documento (Autorização/Declaração/Manifesto Ambiental/Ordem de Serviço/Outorga/Outros)';
COMMENT ON COLUMN siconv.med_doc_complementar_log_rec.in_tipo_manifesto IS 'Tipo do Manifesto (Dispensa/Licença Prévia/Licença de Instalação/Licença de Operacão/Protocolo)';
COMMENT ON COLUMN siconv.med_doc_complementar_log_rec.dt_emissao IS 'Data de Emissão do Documento Complementar';
COMMENT ON COLUMN siconv.med_doc_complementar_log_rec.dt_validade IS 'Data de Validade do Documento Complementar';
COMMENT ON COLUMN siconv.med_doc_complementar_log_rec.nr_documento IS 'Número do Documento Complementar';
COMMENT ON COLUMN siconv.med_doc_complementar_log_rec.tx_descricao IS 'Descrição do Documento Complementar';
COMMENT ON COLUMN siconv.med_doc_complementar_log_rec.nm_orgao_emissor IS 'Orgão Emissor do Documento Complementar';
COMMENT ON COLUMN siconv.med_doc_complementar_log_rec.nm_arquivo IS 'Nome do Arquivo Anexo da Anotação ou Registro de Responsbilidade Técnica';
COMMENT ON COLUMN siconv.med_doc_complementar_log_rec.co_ceph IS 'Código da chave utilizada no Ceph para identificar o arquivo';
COMMENT ON COLUMN siconv.med_doc_complementar_log_rec.medcontratolog IS 'FK da tabela de Contrato do Medição';
COMMENT ON COLUMN siconv.med_doc_complementar_log_rec.versao IS 'Versão usada para controlar a concorrência';
COMMENT ON COLUMN siconv.med_doc_complementar_log_rec.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN siconv.med_doc_complementar_log_rec.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN siconv.med_doc_complementar_log_rec.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';
COMMENT ON COLUMN siconv.med_doc_complementar_log_rec.in_bloqueio IS 'Indicador se o registro está bloqueado para edição';
COMMENT ON COLUMN siconv.med_doc_complementar_log_rec.tx_descricao_outros IS 'Descrição quando Tipo Manifesto Ambiental igual a Outros';
COMMENT ON COLUMN siconv.med_doc_complementar_log_rec.in_eq_lic_inst IS 'Indicador de Licença equivalente a Licença de Instalação';


COMMENT ON TABLE siconv.med_doc_complementar_submeta_log_rec IS 'Tabela que registra o log de Submetas do Documento Complementar';

COMMENT ON COLUMN siconv.med_doc_complementar_submeta_log_rec.id IS 'Identificador único de um do Registro de Log na tabela Submeta do Documento Complementar';
COMMENT ON COLUMN siconv.med_doc_complementar_submeta_log_rec.entity_id IS 'Identificador único de uma Submeta do Documento Complementar';
COMMENT ON COLUMN siconv.med_doc_complementar_submeta_log_rec.vrpl_submeta_fk IS 'FK da tabela Submeta do módulo VRPL';
COMMENT ON COLUMN siconv.med_doc_complementar_submeta_log_rec.meddoccomplementarlog IS 'FK da tabela de Documento Complementar';
COMMENT ON COLUMN siconv.med_doc_complementar_submeta_log_rec.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN siconv.med_doc_complementar_submeta_log_rec.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN siconv.med_doc_complementar_submeta_log_rec.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';


COMMENT ON TABLE siconv.med_item_medicao_log_rec IS 'Tabela que registra o log de Itens da Submeta das Medições de um Contrato de Licitação';

COMMENT ON COLUMN siconv.med_item_medicao_log_rec.id IS 'Identificador único de um do Registro de Log na tabela de Itens da Submeta das Medições de um Contrato de Licitação';
COMMENT ON COLUMN siconv.med_item_medicao_log_rec.entity_id IS 'Identificador único do Item da Submeta das Medições de um Contrato de Licitação';
COMMENT ON COLUMN siconv.med_item_medicao_log_rec.vrpl_evento_fk IS 'FK da tabela Evento do módulo VRPL';
COMMENT ON COLUMN siconv.med_item_medicao_log_rec.vrpl_frente_obra_fk IS 'FK da tabela Frente de Obra do módulo VRPL';
COMMENT ON COLUMN siconv.med_item_medicao_log_rec.vrpl_submeta_fk IS 'FK da tabela Submeta do módulo VRPL';
COMMENT ON COLUMN siconv.med_item_medicao_log_rec.vl_total_servicos IS 'Somatório do Valor dos Serviços envolvidos no Evento/Frente de Obra';
COMMENT ON COLUMN siconv.med_item_medicao_log_rec.medicaologempresa IS 'FK da medição onde a Empresa marcou o Evento/Frente de Obra como concluído';
COMMENT ON COLUMN siconv.med_item_medicao_log_rec.medicaologconcedente IS 'FK da medição onde o Concedente marcou o Evento/Frente de Obra como concluído';
COMMENT ON COLUMN siconv.med_item_medicao_log_rec.medicaologconvenente IS 'FK da medição onde o Convenente marcou o Evento/Frente de Obra como concluído';
COMMENT ON COLUMN siconv.med_item_medicao_log_rec.medcontratolog IS 'FK da tabela Contrato do módulo Medição';
COMMENT ON COLUMN siconv.med_item_medicao_log_rec.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN siconv.med_item_medicao_log_rec.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN siconv.med_item_medicao_log_rec.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';


COMMENT ON TABLE siconv.med_medicao_log_rec IS 'Tabela que registra o log de Medição de um Contrato de Licitação';

COMMENT ON COLUMN siconv.med_medicao_log_rec.id IS 'Identificador único de um do Registro de Log na tabela da Medição';
COMMENT ON COLUMN siconv.med_medicao_log_rec.entity_id IS 'Identificador único da Medição';
COMMENT ON COLUMN siconv.med_medicao_log_rec.nr_sequencial IS 'Número Sequencial da Medição';
COMMENT ON COLUMN siconv.med_medicao_log_rec.dt_inicio IS 'Data Inicial da Medição';
COMMENT ON COLUMN siconv.med_medicao_log_rec.dt_fim IS 'Data Final da Medição';
COMMENT ON COLUMN siconv.med_medicao_log_rec.in_situacao IS 'Indicador da Situação da Medição';
COMMENT ON COLUMN siconv.med_medicao_log_rec.medcontratolog IS 'FK da tabela Contrato do módulo Medição';
COMMENT ON COLUMN siconv.med_medicao_log_rec.versao IS 'Versão usada para controlar a concorrência';
COMMENT ON COLUMN siconv.med_medicao_log_rec.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN siconv.med_medicao_log_rec.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN siconv.med_medicao_log_rec.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';
COMMENT ON COLUMN siconv.med_medicao_log_rec.medicaologagrupadora IS 'FK da medição agrupadora utilizada na acumulação';
COMMENT ON COLUMN siconv.med_medicao_log_rec.in_bloqueio IS 'Indicador se o registro está bloqueado para edição';


COMMENT ON TABLE siconv.med_observacao_log_rec IS 'Tabela que registra o log de Observações das Medições de um Contrato de Licitação';

COMMENT ON COLUMN siconv.med_observacao_log_rec.id IS 'Identificador único de um do Registro de Log na tabela da Observação';
COMMENT ON COLUMN siconv.med_observacao_log_rec.entity_id IS 'Identificador único de uma Observação';
COMMENT ON COLUMN siconv.med_observacao_log_rec.dt_registro IS 'Data/Hora de registro da Observação';
COMMENT ON COLUMN siconv.med_observacao_log_rec.in_perfil_responsavel IS 'Perfil do usuário responsável pela Observação';
COMMENT ON COLUMN siconv.med_observacao_log_rec.nr_cpf_responsavel IS 'CPF do usuário responsável pela Observação ';
COMMENT ON COLUMN siconv.med_observacao_log_rec.tx_observacao IS 'Texto da Observação';
COMMENT ON COLUMN siconv.med_observacao_log_rec.medicaolog IS 'FK da tabela Medição';
COMMENT ON COLUMN siconv.med_observacao_log_rec.versao IS 'Versão usada para controlar a concorrência';
COMMENT ON COLUMN siconv.med_observacao_log_rec.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN siconv.med_observacao_log_rec.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN siconv.med_observacao_log_rec.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';
COMMENT ON COLUMN siconv.med_observacao_log_rec.in_bloqueio IS 'Indicador se o registro está bloqueado para edição';


COMMENT ON TABLE siconv.med_registro_profissional_log_rec IS 'Tabela que registra o log de Registro Profissional de um Responsável Técnico';

COMMENT ON COLUMN siconv.med_registro_profissional_log_rec.id IS 'Identificador único de um do Registro de Log na tabela de um Registro Profissional';
COMMENT ON COLUMN siconv.med_registro_profissional_log_rec.entity_id IS 'Identificador único de um Registro Profissional';
COMMENT ON COLUMN siconv.med_registro_profissional_log_rec.atividade IS 'Atividade (Engenharia ou Arquitetura)';
COMMENT ON COLUMN siconv.med_registro_profissional_log_rec.crea_cau IS 'Número do CREA(Engenharia) ou CAU(Arquitetura)';
COMMENT ON COLUMN siconv.med_registro_profissional_log_rec.uf IS 'UF do registro profissional (CREA)';
COMMENT ON COLUMN siconv.med_registro_profissional_log_rec.medresponsaveltecnicolog IS 'FK do Responsável Técnico';
COMMENT ON COLUMN siconv.med_registro_profissional_log_rec.versao IS 'Versão usada para controlar a concorrência';
COMMENT ON COLUMN siconv.med_registro_profissional_log_rec.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN siconv.med_registro_profissional_log_rec.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN siconv.med_registro_profissional_log_rec.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';


COMMENT ON TABLE siconv.med_responsavel_tecnico_log_rec IS 'Tabela que registra o log de Responsável Técnico';

COMMENT ON COLUMN siconv.med_responsavel_tecnico_log_rec.id IS 'Identificador único de um do Registro de Log na tabela de um Responsável Técnico';
COMMENT ON COLUMN siconv.med_responsavel_tecnico_log_rec.entity_id IS 'Identificador único de um Responsável Técnico';
COMMENT ON COLUMN siconv.med_responsavel_tecnico_log_rec.nr_cpf IS 'Número do CPF do Responsável Técnico';
COMMENT ON COLUMN siconv.med_responsavel_tecnico_log_rec.telefone IS 'Telefone de contato do Responsável Técnico cadastrado no Medição.';
COMMENT ON COLUMN siconv.med_responsavel_tecnico_log_rec.versao IS 'Versão usada para controlar a concorrência';
COMMENT ON COLUMN siconv.med_responsavel_tecnico_log_rec.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN siconv.med_responsavel_tecnico_log_rec.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN siconv.med_responsavel_tecnico_log_rec.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';


COMMENT ON TABLE siconv.med_submeta_medicao_log_rec IS 'Tabela que registra o log de Submetas das Medições de um Contrato de Licitação';

COMMENT ON COLUMN siconv.med_submeta_medicao_log_rec.id IS 'Identificador único de um do Registro de Log na tabela da Submeta da Medição';
COMMENT ON COLUMN siconv.med_submeta_medicao_log_rec.entity_id IS 'Identificador único da Submeta da Medição';
COMMENT ON COLUMN siconv.med_submeta_medicao_log_rec.in_situacao_empresa IS 'Situação da assinatura da Empresa na Submeta da Medição';
COMMENT ON COLUMN siconv.med_submeta_medicao_log_rec.nr_cpf_resp_empresa IS 'CPF do responsável pela assinatura da Empresa na Submeta da Medição';
COMMENT ON COLUMN siconv.med_submeta_medicao_log_rec.dt_assinatura_empresa IS 'Data da assinatura da Empresa na Submeta da Medição';
COMMENT ON COLUMN siconv.med_submeta_medicao_log_rec.vrpl_submeta_fk IS 'FK da tabela Submeta do módulo VRPL';
COMMENT ON COLUMN siconv.med_submeta_medicao_log_rec.medicaolog IS 'FK da tabela Medição';
COMMENT ON COLUMN siconv.med_submeta_medicao_log_rec.versao IS 'Versão usada para controlar a concorrência';
COMMENT ON COLUMN siconv.med_submeta_medicao_log_rec.adt_login IS 'Usuário que alterou o registro';
COMMENT ON COLUMN siconv.med_submeta_medicao_log_rec.adt_data_hora IS 'Data/Hora de modificação do registro';
COMMENT ON COLUMN siconv.med_submeta_medicao_log_rec.adt_operacao IS 'Operacão (INSERT/UPDATE/DELETE) da última ação no registro';
COMMENT ON COLUMN siconv.med_submeta_medicao_log_rec.dt_assinatura_convenente IS 'Data da assinatura do Convenente na Submeta da Medição';
COMMENT ON COLUMN siconv.med_submeta_medicao_log_rec.nr_cpf_resp_convenente IS 'CPF do responsável pela assinatura do Convenente na Submeta da Medição';
COMMENT ON COLUMN siconv.med_submeta_medicao_log_rec.in_situacao_convenente IS 'Situação da assinatura do Convenente na Submeta da Medição';

