import { Pipe, PipeTransform } from '@angular/core';
import { UF } from '../model/uf.model';

@Pipe({
  name: 'siglaUF'
})
export class SilgaUFPipe implements PipeTransform {
  transform(ufs: UF[]): string[] {
    return ufs.map(uf => uf.sigla);
  }
}
