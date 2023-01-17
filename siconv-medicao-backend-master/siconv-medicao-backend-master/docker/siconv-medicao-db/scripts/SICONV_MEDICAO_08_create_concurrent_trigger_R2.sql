/*
* ======================================== DADOS DO SCRIPT =================================================
* AUTOR: Gustavo Ferraz Diniz
* OBJETIVO: Criacao de trigger para tratar concorrencia nas operacoes de update das tabelas de negocio
* PRE-REQUISITOS: Modelo da release 2 previamente aplicado
*/

-- Funcao de gatilho generica para tratar concorrencia nas operacoes de update 
create or replace function siconv.med_fn_generic_concurrent_trigger() returns trigger 
language plpgsql as $function$ 
declare
   _version_ integer;
   _sql_ text;
   _where_ text;
   _table_pks_ record;
   _pk_count_ int;
   _table_name_ text := 'siconv.' || TG_TABLE_NAME;
begin
   _pk_count_ := 1;
   _where_ := '';
   for _table_pks_ in (
      select a.attname as column_name from pg_index i
      join pg_attribute a on a.attrelid = i.indrelid
      and a.attnum = ANY(i.indkey)
      where i.indrelid = _table_name_::regclass
      and i.indisprimary)
   loop
      if (_pk_count_ > 1) then
          _where_ := _where_ || ' and ';
      end if;
      _where_ := _where_ || _table_pks_.column_name || ' = ($1).' || _table_pks_.column_name;
      _pk_count_ := _pk_count_ + 1;
   end loop;

   _sql_ := 'select versao from ' || _table_name_ || ' where ' || _where_;
   execute _sql_ into _version_ using old;

   if (_version_ is null) then
      _version_ := 0;
   end if;

   if (TG_OP = 'UPDATE') then
      if (_version_ + 1) <> NEW.versao then
         raise exception 'Versao % invalida na tabela %. Valor atual -> %', (_version_ + 1), _table_name_, NEW.versao using ERRCODE = '23501';
      end if;
      return new;
   end if;
   return null;
end;
$function$
;

ALTER FUNCTION siconv.med_fn_generic_concurrent_trigger() OWNER TO owner_siconv_p;

-- Tabela med_doc_complementar
create trigger tg_concurrent_med_doc_complementar
before update on siconv.med_doc_complementar
for each row execute procedure siconv.med_fn_generic_concurrent_trigger();

-- Tabela med_medicao
create trigger tg_concurrent_med_medicao
before update on siconv.med_medicao
for each row execute procedure siconv.med_fn_generic_concurrent_trigger();

-- Tabela med_observacao
create trigger tg_concurrent_med_observacao
before update on siconv.med_observacao
for each row execute procedure siconv.med_fn_generic_concurrent_trigger();

-- Tabela med_submeta_medicao
create trigger tg_concurrent_med_submeta_medicao
before update on siconv.med_submeta_medicao
for each row execute procedure siconv.med_fn_generic_concurrent_trigger();

-- Tabela med_contrato_resp_tecnico_social
create trigger tg_concurrent_med_contrato_resp_tecnico_social
before update on siconv.med_contrato_resp_tecnico_social
for each row execute procedure siconv.med_fn_generic_concurrent_trigger();

-- Tabela med_responsavel_tecnico
create trigger tg_concurrent_med_responsavel_tecnico
before update on siconv.med_responsavel_tecnico
for each row execute procedure siconv.med_fn_generic_concurrent_trigger();

-- Tabela med_registro_profissional
create trigger tg_concurrent_med_registro_profissional
before update on siconv.med_registro_profissional
for each row execute procedure siconv.med_fn_generic_concurrent_trigger();

-- Tabela med_contrato_resp_tecnico
create trigger tg_concurrent_med_contrato_resp_tecnico
before update on siconv.med_contrato_resp_tecnico
for each row execute procedure siconv.med_fn_generic_concurrent_trigger();

-- Tabela med_anotacao_registro_rt
create trigger tg_concurrent_med_anotacao_registro_rt
before update on siconv.med_anotacao_registro_rt
for each row execute procedure siconv.med_fn_generic_concurrent_trigger();

