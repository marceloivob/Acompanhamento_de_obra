import {AbstractControl, ValidationErrors} from '@angular/forms';


/**
 * Valida se a Data Final da Medição é posterior a Data Início da Medição
 *
 * @ param dataContrato
 */
export function dataFinalMedicaoPosteriorDataInicioMedicaoValidator(c: AbstractControl): ValidationErrors | null {
  let formGroup = c.parent;
  let dataInicio: Date = formGroup.value['dtInicioMedicao'];
  let dataFim: Date = formGroup.value['dtFimMedicao'];

  return dataInicio && dataFim && dataFim < dataInicio
    ? { dataFinalMedicaoPosteriorDataInicioMedicaoValidator: { vlInvalido: true } }
    : null;
}


export function dataVistoriaAnteriorDataInicioObraValidator(dataInicioObra: Date) {
  return (c: AbstractControl): ValidationErrors|null => {
    const formGroup = c.parent;
    let dataVistoriaExtra: Date = formGroup.value['dtVistoria'];
    return  dataVistoriaExtra && dataVistoriaExtra < dataInicioObra ? { 'dataVistoriaAnteriorDataInicioObraValidator': {vlInvalido : true }} : null;
  };
}

export function dataVistoriaMedicaoRequiredValidator(c: AbstractControl): ValidationErrors | null {
  let formGroup = c.parent;
  let dtVistoriaObrigatoria: boolean = formGroup.value['dtVistoriaObrigatoria'];
  return dtVistoriaObrigatoria && (c.value == null || c.value.length === 0) ? { required: true } : null;
}


/**
 * Valida se a Data de Inicio da Obra não é futura
 *
 * @ param dataInicioObra
 */
export function dataInicioObraDataAtualValidator(dataInicioObra: AbstractControl) {
  return (c: AbstractControl): ValidationErrors | null => {
    const dataAtual = new Date();

    return dataInicioObra && dataInicioObra.value && dataInicioObra.value.getTime() > dataAtual.getTime() ? { 'dataInicioObraDataAtualValidator': { vlInvalido: true } } : null;
  };
}


export function dataFinalMedicaoRequiredValidator(c: AbstractControl): ValidationErrors | null {
  let formGroup = c.parent;
  let dtFimObrigatoria: boolean = formGroup.value['dtFimObrigatoria'];
  return dtFimObrigatoria && (c.value == null || c.value.length === 0) ? { required: true } : null;
}

/**
 * Valida se a data de fim da Obra é posterior a Data de Fim de Vigência do Contrato
 *
 * @ param dataContrato
 *
export function dataFimObraDataContratoValidator(dataContrato: Date) {
  return (c: AbstractControl): ValidationErrors|null => {
    const formGroup = c.parent;
    const dataFimMedicao: Date = formGroup.value['dtFimMedicao'];

    return  dataFimMedicao && dataFimMedicao.getTime() > dataContrato.getTime() ? { 'dataFimObraDataContratoValidator': {vlInvalido : true }} : null;
  };
}
*/
