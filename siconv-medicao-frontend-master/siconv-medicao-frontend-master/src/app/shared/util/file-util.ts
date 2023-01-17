import { AbstractControl } from '@angular/forms';

export class FileUtil {

  public static validarTamanhoArquivo(file: File, tamanhoMaximo: number, formControl?: AbstractControl): boolean {
    let isValido = true;

    if (file.size / 1000000 > tamanhoMaximo) {
      isValido = false;
      formControl.setErrors ({ 'tamanhoArquivoValidator': true });
      formControl.markAsTouched({ onlySelf: true });
    }
    return isValido;
  }

  public static validarTamanhoNomeArquivo(file: File, formControl?: AbstractControl): boolean {
    let isValido = true;

    if (file.name && file.name.length > 100) {
      isValido = false;
      formControl.setErrors ({ 'tamanhoNomeArquivoValidator': true });
      formControl.markAsTouched({ onlySelf: true });
    }
    return isValido;
  }

  public static validarExtensaoArquivo(file: File, formControl?: AbstractControl): boolean {
    let isValido = true;
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

    if (tiposValidos.indexOf(file.type) < 0) {
      isValido = false;
      formControl.setErrors ({ 'tipoArquivoValidator': true });
      formControl.markAsTouched({ onlySelf: true });
    }
    return isValido;
  }

  public static validarArquivo(file: File, tamanhoMaximo: number, formControl?: AbstractControl): boolean {
    return this.validarExtensaoArquivo(file, formControl)
      && this.validarTamanhoArquivo(file, tamanhoMaximo, formControl)
      && this.validarTamanhoNomeArquivo(file, formControl);
  }
}
