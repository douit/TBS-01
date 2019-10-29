import { DatatableInputColumn } from 'app/shared/model/datatable/datatable-input-column';
import { DatatableInputOrder } from 'app/shared/model/datatable/datatable-input-order';
import { DatatableInputSearch } from 'app/shared/model/datatable/datatable-input-search';

export class DataTableInput {
    draw: number;
    columns = new Array<DatatableInputColumn>();
    order = new Array<DatatableInputOrder>();
    start: number;
    length: number;
    search = new DatatableInputSearch();
}
