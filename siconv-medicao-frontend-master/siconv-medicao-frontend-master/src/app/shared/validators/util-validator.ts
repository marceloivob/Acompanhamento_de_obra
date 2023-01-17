import { AbstractControl, ValidationErrors, FormControl, ValidatorFn } from '@angular/forms';
import * as moment from 'moment';

export function validateTelefone(control: AbstractControl) {
  let error = false;

  if (control.value) {
    // Se o tamanho da String que representa o Telefone for menor que 13 está errado
    // pois deve ser considerado caracteres de máscara "(" , ")" , "-"
    if (control.value.length < 14 ||
      control.value.length > 15) {
      error = true;
    } else {
      // divide a string em duas parte separadas por " - "
      const partes = control.value.split('-');

      // Se não houver duas partes da string do telefone é porque não há hífen separando o telefone,
      // então está errado.
      // OU Se a primeira parte tiver menos de 8 caracteres está errado deve levar em conta o DDD. Ex.: (99)99999 ou (99)9999
      // OU Se a segunda parte tiver menos que 3 caracteres está errado
      if (partes.length !== 2 ||
        partes[0].length < 8 ||
        partes[1].length < 4) {
        error = true;
      }
    }

    return error ? { 'validateTelefone': { vlInvalido: true } } : null;
  }
}

export function tamanhoArquivoValidator() {
  return (c: AbstractControl): ValidationErrors | null => {
    const tamanhoValido = c.value.size / 1000000 > 10;

    return (tamanhoValido) ? { 'tamanhoArquivoValidator': { vlInvalido: true } } : null;
  };
}

export function tamanhoNomeArquivoValidator() {
  return (c: AbstractControl): ValidationErrors | null => {
    const tamanhoInvalido = c.value.name && c.value.name > 100;

    return (tamanhoInvalido) ? { 'tamanhoNomeArquivoValidator': { vlInvalido: true } } : null;
  };
}

export function tipoArquivoValidator() {
  return (c: AbstractControl): ValidationErrors | null => {
    const tiposValidos: string[] = new Array(
      'application/pdf',
      'application/msword',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      'application/vnd.ms-excel',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      'image/jpeg',
      'image/png',
      'application/vnd.oasis.opendocument.text',
      'application/vnd.oasis.opendocument.spreadsheet'
    );
    const tipos = tiposValidos.indexOf(c.value.type) < 0;

    return (tipos) ? { 'tipoArquivoValidator': { vlInvalido: true } } : null;
  };
}

export function DateValidator2(format = "dd/MM/YYYY"): any {
  return (control: FormControl): { [key: string]: any } => {
    const val = moment(control.value, format, true);

    if (!val.isValid()) {
      return { invalidDate: true };
    }

    return null;
  };
}

export function DateValidator(): ValidatorFn {
  return (control: AbstractControl): { [key: string]: any } => {
    const date = new Date(control.value);
    const invalidDate = date.getDay === undefined || date.getMonth === undefined || date.getFullYear === undefined;
    return invalidDate ? { 'invalidDate': { value: control.value } } : null;
  };
}
