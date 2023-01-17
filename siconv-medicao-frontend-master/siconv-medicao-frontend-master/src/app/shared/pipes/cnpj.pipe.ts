import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'cnpj'
})
export class CnpjPipe implements PipeTransform {

  transform(value: any): any {
    if (typeof value !== 'string') {
        return value;
    }

    if (value && value.length === 14) {
        return `${value.substr(0, 2)}.${value.substr(2, 3)}.${value.substr(5, 3)}/${value.substr(8, 4)}-${value.substr(12, 2)}`;
    }

    return value;
}

}
