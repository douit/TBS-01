import { extend } from 'lodash';

interface IDatatableColumn {
    name?: string;
    prop?: string;
    resizeable?: boolean;
    sortable?: boolean;
    searchable?: boolean;
    visible?: boolean;
    headerTemplate?: any;
    cellTemplate?: any;
    flexGrow?: number;
}

export class DatatableColumn implements IDatatableColumn {
    name: string;
    prop = '';
    resizeable = false;
    sortable = true;
    searchable = true;
    visible = true;
    headerTemplate: any;
    cellTemplate: any;
    flexGrow = 1;

    constructor(datatableColumn?: IDatatableColumn) {
        if (datatableColumn) {
            extend(this, datatableColumn);
        }
    }
}
