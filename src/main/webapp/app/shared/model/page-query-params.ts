import { Datatable } from 'app/shared/model/datatable/datatable';

export class PageQueryParams {
    search: string;
    page: number | string;
    length: number | string;
    sortBy: string;
    sortDir: string;

    fillDatatable(datatable: Datatable<any>) {
        if (datatable.search) {
            this.search = datatable.search;
        }

        if (datatable.page) {
            this.page = datatable.page;
        }

        if (datatable.page) {
            this.length = datatable.length;
        }

        if (datatable.sort) {
            this.sortBy = datatable.sort.prop;
            this.sortDir = datatable.sort.dir;
        }
    }
}
