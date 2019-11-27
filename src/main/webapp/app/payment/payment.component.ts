import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {HttpErrorResponse} from '@angular/common/http';
import {Subscription} from 'rxjs';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import {finalize} from 'rxjs/operators';
import {JhiAlertService, JhiEventManager} from 'ng-jhipster';

import {IPayment} from 'app/shared/model/payment.model';
import {AccountService} from 'app/core/auth/account.service';
import {PaymentService} from './payment.service';
import {DatatableComponent} from '@swimlane/ngx-datatable';
import {IItem} from 'app/shared/model/item.model';
import {Datatable} from 'app/shared/model/datatable/datatable';
import {DatatableColumn} from 'app/shared/model/datatable/datatable-column';
import {PageQueryParams} from 'app/shared/model/page-query-params';
import {TranslateService} from '@ngx-translate/core';
import {ActivatedRoute, Router} from '@angular/router';
import {PaymentStatus, PaymentMethod} from 'app/shared/constants';
import {_tbs} from 'app/shared/util/tbs-utility';

@Component({
  selector: 'app-payment',
  templateUrl: './payment.component.html'
})
export class PaymentComponent implements OnInit {
  payments: IPayment[];
  currentAccount: any;
  eventSubscriber: Subscription;

  // new datatable
  busy = false;
  datatable = new Datatable<IItem>();
  PaymentStatus = PaymentStatus;
  PaymentMethod = PaymentMethod;

  // Datatable Reference
  @ViewChild('table', {static: true}) table: DatatableComponent;

  // Datatable Templates Reference
  @ViewChild('headerTemplate', {static: false}) headerTemplate;
  @ViewChild('rowTemplate', {static: false}) rowTemplate;
  @ViewChild('nameRowTemplate', {static: false}) nameRowTemplate;
  @ViewChild('statusRowTemplate', {static: true}) statusRowTemplate;
  @ViewChild('paymentMethodRowTemplate', {static: true}) paymentMethodRowTemplate;
  @ViewChild('modifiedDateTemplate', {static: true}) modifiedDateTemplate;
  @ViewChild('invoiceRowTemplate', {static: true}) invoiceRowTemplate;
  @ViewChild('actionsRowTemplate', {static: true}) actionsRowTemplate;

  constructor(
    protected paymentService: PaymentService,
    protected jhiAlertService: JhiAlertService,
    protected eventManager: JhiEventManager,
    protected accountService: AccountService,
    protected activatedRoute: ActivatedRoute,
    private translateService: TranslateService,
    private router: Router
  ) {}

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
        name: this.translateService.instant('tbsApp.payment.amount'),
        prop: 'amount',
        headerTemplate: this.headerTemplate,
        cellTemplate: this.rowTemplate
      }),
      new DatatableColumn({
        name: this.translateService.instant('tbsApp.payment.status'),
        prop: 'status',
        headerTemplate: this.headerTemplate,
        cellTemplate: this.statusRowTemplate
      }),
      new DatatableColumn({
        name: this.translateService.instant('global.lastModifiedDate'),
        prop: 'lastModifiedDate',
        headerTemplate: this.headerTemplate,
        cellTemplate: this.modifiedDateTemplate
      }),
      new DatatableColumn({
        name: this.translateService.instant('tbsApp.payment.invoice'),
        prop: 'invoice.id',
        headerTemplate: this.headerTemplate,
        cellTemplate: this.invoiceRowTemplate
      }),
      new DatatableColumn({
        name: this.translateService.instant('tbsApp.payment.paymentMethod'),
        prop: 'paymentMethod',
        sortable: false,
        searchable: false,
        headerTemplate: this.headerTemplate,
        cellTemplate: this.paymentMethodRowTemplate,
      })/*,
      new DatatableColumn({
        name: this.translateService.instant('global.datatable.actions'),
        sortable: false,
        searchable: false,
        headerTemplate: this.headerTemplate,
        cellTemplate: this.actionsRowTemplate
      })*/
    ]);
  }

  loadData() {
    this.busy = true;
    this.paymentService.getList(this.datatable.getDataTableInput())
      .pipe(
        finalize(() => this.busy = false)
      )
      .subscribe(
        (res) => {

          /*for (const payment of res.data) {
            payment.pay = Image.resolve(driver.personalPhoto);
          }*/

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

    this.router.navigate(['/payment'], { queryParams: pageQueryParams });
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
    this.router.navigate(['/payment/' + row.id + '/edit']);
  }

  view(row: any) {
    this.router.navigate(['/payment/' + row.id + '/view']);
  }

  traceObject(obj) {
    JSON.stringify(obj);
  }

}
