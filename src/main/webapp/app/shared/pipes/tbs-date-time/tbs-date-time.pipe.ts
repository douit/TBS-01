import { Pipe, PipeTransform } from '@angular/core';
import { _tbs } from 'app/shared/util/tbs-utility';

@Pipe({name: 'tbsDateTime' })
export class TbsDateTimePipe implements PipeTransform {

    transform(value: any): any {
        if (!value) {
            return value;
        }

        return _tbs.formatDate(value);
    }
}
