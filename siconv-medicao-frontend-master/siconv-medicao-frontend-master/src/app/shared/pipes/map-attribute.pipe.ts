import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'mapAttribute',
})
export class MapAttributePipe implements PipeTransform {
  transform(itens: any[], attribute: string): any {
    return itens.map(item => item[attribute]);
  }
}
