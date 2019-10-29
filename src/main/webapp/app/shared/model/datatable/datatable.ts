import { DatatableComponent } from '@swimlane/ngx-datatable';
import { DatatableOptions } from 'app/shared/constants';
import { DatatableColumn } from 'app/shared/model/datatable/datatable-column';
import { DataTableInput as DatatableInput } from 'app/shared/model/datatable/datatable-input';
import { DatatableInputColumn } from 'app/shared/model/datatable/datatable-input-column';
import { DatatableInputOrder } from 'app/shared/model/datatable/datatable-input-order';
import { DatatableSort } from 'app/shared/model/datatable/datatable-sort';
import { PageQueryParams } from 'app/shared/model/page-query-params';
import { Pageable } from 'app/shared/model/pageable';
import { toNumber } from 'lodash';

export class Datatable<T> extends Pageable<T> {
    private table: DatatableComponent;
    private start = 0;
    private _visibleColumns: Array<DatatableColumn>;
    private _columns: Array<DatatableColumn>;

    get visibleColumns(): Array<DatatableColumn> {
        return this._visibleColumns;
    }

    private _page = 1;
    get page(): number {
        return this._page;
    }

    private _sort: DatatableSort;
    get sort(): DatatableSort {
        return this._sort;
    }

    get footerHeight(): number {
        return DatatableOptions.FOOTER_HEIGHT;
    }

    length = DatatableOptions.PAGE_SIZE;
    search = '';

    update(pageable: Pageable<T>) {
        this.data = pageable.data;
        this.draw = pageable.draw;
        this.recordsFiltered = pageable.recordsFiltered;
        this.recordsTotal = pageable.recordsTotal;
    }

    addItem(item) {
        // If `data` length reaches size limit
        if (this.data.length >= this.length) {
            this.data.pop();
        }

        this.data.unshift(item);
    }

    getItemIndex(item) {
        return this.data.indexOf(item);
    }

    updateItemAtIndex(item, index: number) {
        let result = false;

        if (index > -1) {
            this.data[index] = item;

            result = true;
        }

        return result;
    }

    removeItemAtIndex(index) {
        this.data.splice(index, 1);
    }

    removeItem(item) {
        const index = this.data.indexOf(item);
        const indexExists = index > -1;

        if (indexExists) {
            this.removeItemAtIndex(index);
        }

        return indexExists;
    }

    removeList(items: Array<T>) {
        for (const item of items) {
            this.removeItem(item);
        }
    }

    changePageNumber(offset: number) {
        this._page = offset + 1;
        this.start = offset * this.length;

        if (this.table.offset !== offset) {
            this.table.offset = offset;
        }
    }

    resetPageNumber() {
        this.table.offset = 0;

        this.changePageNumber(this.table.offset);
    }

    // resetPageOffset(table: DatatableComponent) {
    //     if (table) {
    //         table.offset = 0;
    //         this.changePage(0);
    //     }
    // }

    setTable(table: DatatableComponent) {
        this.table = table;
    }

    private updateVisibleColumns() {
        this._visibleColumns = this._columns.filter(c => c.visible);
    }

    setColumns(columns: Array<DatatableColumn>) {
        this._columns = columns;

        this.updateVisibleColumns();
    }

    setSort(datatableSort: DatatableSort) {
        this._sort = datatableSort;

        this.table.sorts = [datatableSort];
    }

    fillPageQueryParams(pageQueryParams: PageQueryParams) {
        this.search = pageQueryParams.search || '';

        if (pageQueryParams.search) {
            this.search = pageQueryParams.search;
        }

        if (pageQueryParams.page) {
            const pageOffset = toNumber(pageQueryParams.page) - 1;
            this.changePageNumber(pageOffset);
        }

        if (pageQueryParams.length) {
            this.length = toNumber(pageQueryParams.length);
        }

        if (pageQueryParams.sortBy && pageQueryParams.sortDir) {
            this.setSort({
                prop: pageQueryParams.sortBy,
                dir: pageQueryParams.sortDir
            });
        }
    }

    getDataTableInput() {
        const datatableInput = new DatatableInput();

        datatableInput.draw = 0;
        datatableInput.start = this.start;
        datatableInput.length = this.length;
        datatableInput.search.value = this.search;

        let index = 0;
        let sortColumnIndex = null;

        for (const column of this._columns) {
            // Datatable Input Column
            const datatableInputColumn = new DatatableInputColumn();
            const prop = column.prop.toString();

            if (prop) {
                datatableInputColumn.data = prop;
                datatableInputColumn.orderable = column.sortable;
                datatableInputColumn.searchable = column.searchable;

                datatableInput.columns.push(datatableInputColumn);

                if (this.sort && this.sort.prop === prop) {
                    sortColumnIndex = index;
                }

                // Increment index ONLY when `datatableInputColumn` is pushed in `datatableInput.columns` to get accurate index for sorting
                index++;
            }

        }

        // Sorting
        if (this.sort && sortColumnIndex !== null) {

            // Datatable Input Order
            const datatableInputOrder = new DatatableInputOrder();
            datatableInputOrder.column = sortColumnIndex;
            datatableInputOrder.dir = this.sort.dir;
            datatableInput.order.push(datatableInputOrder);
        } else {

            // Remove `order` property if their value is not set
            // delete datatableInput.order;
          datatableInput.order = [{column: 0, dir: 'desc'}];
        }

        return datatableInput;
    }
}
