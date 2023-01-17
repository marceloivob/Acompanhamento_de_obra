import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import moment from 'moment';

function isEmptyInputValue(value: any): boolean {
  return value == null || value.length === 0;
}

export class DateValidators {

  static after(refControlName: string): ValidatorFn {

    return (control: AbstractControl): ValidationErrors | null => {

      if (isEmptyInputValue(control.value) || isEmptyInputValue(control.parent)) {
        return null;
      }

      const refControl = control.parent.get(refControlName);

      if (isEmptyInputValue(refControl.value)) {
        return null;
      }

      const inputDate = moment(control.value);
      const refDate = moment(refControl.value);

      return inputDate.isAfter(refDate) ? null : { invalidMinDate: { minDate: refDate.add(1, 'day').toDate() } };
    };
  }
}
