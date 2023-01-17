import { Pipe, PipeTransform } from '@angular/core';
@Pipe({
  name: 'tipoManifestoAmbiental'
})
export class TipoManifestoAmbientalPipe implements PipeTransform {
  transform(tipoDocumento: any[]): string[] {
    return tipoDocumento.map(tipo => tipo.descricao);
  }
}
