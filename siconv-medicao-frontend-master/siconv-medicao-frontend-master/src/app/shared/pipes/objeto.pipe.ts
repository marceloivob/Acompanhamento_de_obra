import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'objeto'
})
export class ObjetoPipe implements PipeTransform {

  transform(value: any): any {
    if (value.length > 100) {
        return `${value.substr(0, 100)} ...`;
    }

    return value;
}
  
}
