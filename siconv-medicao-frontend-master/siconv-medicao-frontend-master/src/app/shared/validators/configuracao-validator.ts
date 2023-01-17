import {AbstractControl, ValidationErrors, FormControl} from '@angular/forms';


/**
 * Valida se a Data de Emissao da ART não é futura
 *
 * @ param dataEmissao
 */
export function dataEmissaoArtRrtValidator(dataEmissao: Date) {
  return (c: AbstractControl): ValidationErrors|null => {
    const dataAtual = new Date (c.value).getTime();

    return  dataAtual > dataEmissao.getTime() ? { 'dataEmissaoArtRrtValidator': {vlInvalido : true }} : null;
  };
}

export function dataEmissaoDocComplValidator() {
  const dtAtual = new Date();

  return (c: AbstractControl): ValidationErrors|null => {
    const dtEmissao = new Date (c.value).getTime();

    return  (dtEmissao && dtAtual.getTime() < dtEmissao) ? { 'dataEmissaoDocComplValidator': {vlInvalido : true }} : null;
  };
}

export function dataValidadeDocComplValidator(dataEmissao: FormControl) {
  return (c: AbstractControl): ValidationErrors|null => {
    const dtValidade = c.value;
    const dtEmissao = new Date(dataEmissao.value).getTime();
    return  (dtValidade && dtEmissao && dtValidade < dtEmissao) ?
                    { 'dataValidadeDocComplValidator': {vlInvalido : true }} : null;
  };
}

export function validaCampoDataDocComplValidator(isCampoObrigatorio: boolean) {
  return (c: AbstractControl): ValidationErrors|null => {
    const dtValidade = c.value;
    if ((!isCampoObrigatorio) && (dtValidade > 0)) {
        let padraoData = /^[0-9]{2}\/[0-9]{2}\/[0-9]{4}$/;
        return (!padraoData.test(dtValidade)) ? { 'validaCampoDataDocComplValidator': {vlInvalido : true }} :  null;
    }
  };
}

export function verificaValorCampoValidator() {
  return (c: AbstractControl): ValidationErrors|null => {
    const valorCampo = c.value ? (c.value).toString().trim() : '';

    return  (valorCampo === '') ? { 'verificaValorCampoValidator': {vlInvalido : true }} : null;
  };
}

