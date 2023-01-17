import { Pipe, PipeTransform } from '@angular/core';
import { Anexo } from '../model/anexo.modelo';

@Pipe({
  name: 'filtroAnexos'
})
export class FiltroAnexosPipe implements PipeTransform {

  transform(value: Anexo[], args?: any): any {
    if (args) {
     return value;
    }
    return value.map(anexo => anexo.inInativo === false);
  }
}
