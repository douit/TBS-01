import { DatatableInputSearch } from './datatable-input-search';

export class DatatableInputColumn {
    data: string;
    name = '';
    searchable = true;
    orderable = true;
    search = new DatatableInputSearch();

    constructor(data?: string) {
        if (typeof data === 'string') {
            this.data = data;
        }
    }
}
