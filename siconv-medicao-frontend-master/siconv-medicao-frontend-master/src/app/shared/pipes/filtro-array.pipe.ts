import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'filtrar',
})
export class FiltroArrayPipe implements PipeTransform {
  transform(itens: any[], predicado: (item?: any) => boolean): any {
    return predicado ? itens.filter(predicado) : itens;
  }
}
