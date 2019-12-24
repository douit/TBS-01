import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {HttpErrorResponse} from '@angular/common/http';
import {ActivatedRoute, Router} from '@angular/router';
import {Subscription} from 'rxjs';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import {finalize} from 'rxjs/operators';
import {JhiAlertService, JhiEventManager} from 'ng-jhipster';

import {IInvoice} from 'app/shared/model/invoice.model';
import {AccountService} from 'app/core/auth/account.service';
import {InvoiceService} from './invoice.service';
import {DatatableColumn} from 'app/shared/model/datatable/datatable-column';
import {PageQueryParams} from 'app/shared/model/page-query-params';
import {DatatableComponent} from '@swimlane/ngx-datatable';
import {IItem} from 'app/shared/model/item.model';
import {Datatable} from 'app/shared/model/datatable/datatable';
import {TranslateService} from '@ngx-translate/core';
import {_tbs} from 'app/shared/util/tbs-utility';
import {InvoiceStatus} from 'app/shared/constants';

@Component({
  selector: 'app-invoice',
  templateUrl: './invoice.component.html'
})
export class InvoiceComponent implements OnInit {
  currentAccount: any;
  invoices: IInvoice[];
  error: any;
  success: any;
  page: any;
  reverse: any;
  InvoiceStatus = InvoiceStatus;
  auditInvoice: any[];

  @ViewChild('nameRowTemplate', {static: false}) nameRowTemplate;
  @ViewChild('statusRowTemplate', {static: true}) statusRowTemplate;
  @ViewChild('issueDateTemplate', {static: true}) issueDateTemplate;

  // new datatable
  busy = false;
  datatable = new Datatable<IItem>();
  // Datatable Reference
  @ViewChild('table', {static: true}) table: DatatableComponent;

  // Datatable Templates Reference
  @ViewChild('headerTemplate', {static: false}) headerTemplate;
  @ViewChild('rowTemplate', {static: false}) rowTemplate;
  @ViewChild('actionsRowTemplate', {static: true}) actionsRowTemplate;

  constructor(
    protected invoiceService: InvoiceService,
    protected jhiAlertService: JhiAlertService,
    protected accountService: AccountService,
    protected activatedRoute: ActivatedRoute,
    protected router: Router,
    protected eventManager: JhiEventManager,
    private translateService: TranslateService,
  ) {
  }

  ngOnInit() {
    this.initDatatable();

    this.activatedRoute.queryParams
      .subscribe((pageQueryParams: PageQueryParams) => {
        this.datatable.fillPageQueryParams(pageQueryParams);

        this.loadData();
      });
  }

  initDatatable() {
    this.datatable.setTable(this.table);
    this.datatable.setColumns([
      new DatatableColumn({
        name: this.translateService.instant('global.datatable.id'),
        prop: 'id',
        headerTemplate: this.headerTemplate,
        cellTemplate: this.rowTemplate,
      }),
      new DatatableColumn({
        name: this.translateService.instant('tbsApp.invoice.accountId'),
        prop: 'accountId',
        headerTemplate: this.headerTemplate,
        cellTemplate: this.rowTemplate,
      }),
      new DatatableColumn({
        name: this.translateService.instant('tbsApp.invoice.status'),
        prop: 'status',
        headerTemplate: this.headerTemplate,
        cellTemplate: this.statusRowTemplate
      }),
      new DatatableColumn({
        name: this.translateService.instant('tbsApp.invoice.issueDate'),
        prop: 'createdDate',
        headerTemplate: this.headerTemplate,
        cellTemplate: this.issueDateTemplate
      }),
      new DatatableColumn({
        name: this.translateService.instant('tbsApp.invoice.amount'),
        prop: 'amount',
        headerTemplate: this.headerTemplate,
        cellTemplate: this.rowTemplate
      }),
      new DatatableColumn({
        name: this.translateService.instant('tbsApp.invoice.customer'),
        prop: 'customer.identity',
        headerTemplate: this.headerTemplate,
        cellTemplate: this.rowTemplate
      }),
      new DatatableColumn({
        name: this.translateService.instant('tbsApp.invoice.client'),
        prop: 'client.name',
        headerTemplate: this.headerTemplate,
        cellTemplate: this.rowTemplate
      }),
      new DatatableColumn({
        name: this.translateService.instant('global.datatable.actions'),
        sortable: false,
        searchable: false,
        headerTemplate: this.headerTemplate,
        cellTemplate: this.actionsRowTemplate
      })
    ]);
  }

  loadData() {
    this.busy = true;
    this.invoiceService.getList(this.datatable.getDataTableInput())
      .pipe(
        finalize(() => this.busy = false)
      )
      .subscribe(
        (res) => {
          this.datatable.update(res);
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  filter(reset: boolean) {
    if (reset) {
      this.datatable.resetPageNumber();
    }

    const pageQueryParams = new PageQueryParams();

    pageQueryParams.fillDatatable(this.datatable);

    this.router.navigate(['/invoice'], { queryParams: pageQueryParams });
  }

  search() {
    this.filter(true);
  }

  clearSearch() {
    this.datatable.search = '';

    this.search();
  }

  onPageChanged(data) {
    this.datatable.changePageNumber(data.offset);

    this.filter(false);
  }

  onPageSizeChanged() {
    this.filter(true);
  }

  onSortChanged(data) {
    this.datatable.setSort(data.sorts[0]);

    this.filter(true);
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }

  edit(row: any) {
    this.router.navigate(['/invoice/' + row.id + '/edit']);
  }

  view(row: any) {
    this.router.navigate(['/invoice/' + row.id + '/view']);
  }

  auditInvoiceview(row: any) {
    console.log('Audit invoice: ' + row.accountId);
    this.busy = true;
    const that = this;
    this.invoiceService.getTripAudit(row.accountId).subscribe(
      data => {
        that.busy = false
        data.forEach(log => {
          this.auditInvoice = data;
          const rowData = [];
          switch (log.auditEventType) {
            case '':
              rowData.push(`<button class="btn btn-secondary btn-sm btn-success" type="button" style="width: 100%; cursor: default;">`
                + _tbs.humanizeEnumString(log.status) + `</button>`);
              break;
            case '':
              rowData.push(`<button class="btn btn-secondary btn-sm" type="button" style="width: 100%; cursor: default;">`
                + _tbs.humanizeEnumString(log.status) + `</button>`);
              break;
            case '':
              rowData.push(`<button class="btn btn-secondary btn-sm" type="button" style="width: 100%; cursor: default;">`
                + _tbs.humanizeEnumString(log.status) + `</button>`);
              break;
            case '':
              rowData.push(`<button class="btn btn-secondary btn-sm" type="button" style="width: 100%; cursor: default;">`
                + _tbs.humanizeEnumString(log.status) + `</button>`);
              break;
            case '':
              rowData.push(`<button class="btn btn-secondary btn-sm" type="button" style="width: 100%; cursor: default;">`
                + _tbs.humanizeEnumString(log.status) + `</button>`);
              break;
            case '':
              rowData.push(`<button class="btn btn-secondary btn-sm" type="button" style="width: 100%; cursor: default;">`
                + _tbs.humanizeEnumString(log.status) + `</button>`);
              break;
            case '':
              rowData.push(`<button class="btn btn-secondary btn-sm" type="button" style="width: 100%; cursor: default;">`
                + _tbs.humanizeEnumString(log.status) + `</button>`);
              break;
            case '':
              rowData.push(`<button class="btn btn-secondary btn-sm btn-danger" type="button" style="width: 100%; cursor: default";">`
                + _tbs.humanizeEnumString(log.status) + `</button>`);
              break;

            default:

          }
          rowData.push(log.driver.name);
          rowData.push(log.lastModifiedBy);
          rowData.push(_tbs.formatDate(log.modifiedDate));
        });
      },
      err => {
        // that.notification.showNotification('danger', 'Trip audit could not be retrieved')
      }
    );
  }

  formatDate(date: any) {
    return _tbs.formatDate(date);
  }

  humanizeEnumString(data: any) {
    return _tbs.humanizeEnumString(data);
  }

}
