/*
* ================================= DADOS DO SCRIPT ==================================================
* AUTOR: Ana Cristina Nunes Soares
* OBJETIVO: Criacao de constraint uniquekey para coluna contrato_fk na tabela med_contrato
* DATA CRIACAO: 29/12/2022
* PRE-REQUISITOS: Modelo atual da release 6 previamente aplicado
*/

ALTER TABLE siconv.med_contrato
ADD CONSTRAINT uk_med_contrato_contrato_fk UNIQUE (contrato_fk);


