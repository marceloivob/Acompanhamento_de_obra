import { Pipe, PipeTransform } from '@angular/core';
@Pipe({
  name: 'tipoDocumento'
})
export class TipoDocumentoPipe implements PipeTransform {
  transform(tipoDocumento: any[]): string[] {
    return tipoDocumento.map(tipo => tipo.descricao);
  }
}
