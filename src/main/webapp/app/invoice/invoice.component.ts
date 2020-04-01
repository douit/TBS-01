import {Component, OnInit, ViewChild} from '@angular/core';
import {HttpErrorResponse} from '@angular/common/http';
import {ActivatedRoute, Router} from '@angular/router';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import {finalize} from 'rxjs/operators';
import {JhiAlertService, JhiEventManager} from 'ng-jhipster';

import {IInvoice} from 'app/shared/model/invoice.model';
import {AccountService} from 'app/core/auth/account.service';
import {InvoiceService} from './invoice.service';
import {DatatableColumn} from 'app/shared/model/datatable/datatable-column';
import {PageQueryParams} from 'app/shared/model/page-query-params';
import {DatatableComponent} from '@swimlane/ngx-datatable';
import {Datatable} from 'app/shared/model/datatable/datatable';
import {TranslateService} from '@ngx-translate/core';
import {_tbs} from 'app/shared/util/tbs-utility';
import {InvoiceStatus, PaymentStatus} from 'app/shared/constants';
import {NgbCalendar, NgbDate, NgbDateParserFormatter} from '@ng-bootstrap/ng-bootstrap';
import {NgbDateStruct} from '@ng-bootstrap/ng-bootstrap/datepicker/ngb-date-struct';
import {IClient} from 'app/shared/model/client.model';
import * as moment from 'moment';
import {IInvoiceSearchRequest} from 'app/shared/model/invoice-serach-request';
import {ClientService} from 'app/client/client.service';
import {IInvoiceItem} from 'app/shared/model/invoice-item.model';

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
  invoiceItems: IInvoiceItem[];
  invoiceView: IInvoice;
  selectedInvoice: IInvoice;
  PaymentStatus = PaymentStatus;

  @ViewChild('nameRowTemplate', {static: false}) nameRowTemplate;
  @ViewChild('paymentStatusRowTemplate', {static: true}) paymentStatusRowTemplate;
  @ViewChild('issueDateTemplate', {static: true}) issueDateTemplate;

  // new datatable
  busy = false;
  datatable = new Datatable<IInvoice>();


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
    public formatter: NgbDateParserFormatter,
    private calendar: NgbCalendar,
    protected clientService: ClientService
  ) {
    const current = new Date();
    this.maxDate = {
      year: current.getFullYear(),
      month: current.getMonth() + 1,
      day: current.getDate()
    };
    const previous = new Date();
    previous.setMonth(previous.getMonth() - 1);
    this.startDate = {
      year: previous.getFullYear(),
      month: previous.getMonth() + 1,
      day: previous.getDate()
    };
  }

  hoveredDate: NgbDate;
  fromDate: NgbDate;
  toDate: NgbDate;
  maxDate: NgbDateStruct;
  startDate: NgbDateStruct;
  selectedClient: IClient;
  clients: IClient[];
  customerId: string;
  paymentStatus: any;
  isSearchOpr = false;
  taxRate = 0;

   invoiceSearch: IInvoiceSearchRequest = {
    fromDate : null,
    toDate : null,
    clientId : null,
    customerId : null,
    // paymentStatus:this.paymentStatusSelected.value,
  };
  onDateSelection(date: NgbDate, datepicker) {
    if (!this.fromDate && !this.toDate) {
      this.fromDate = date;
    } else if (this.fromDate && !this.toDate && date.after(this.fromDate)) {
      this.toDate = date;
      datepicker.close();
    } else {
      this.toDate = null;
      this.fromDate = date;
    }
  }


  isHovered(date: NgbDate) {
    return this.fromDate && !this.toDate && this.hoveredDate && date.after(this.fromDate) && date.before(this.hoveredDate);
  }

  isInside(date: NgbDate) {
    return date.after(this.fromDate) && date.before(this.toDate);
  }

  isRange(date: NgbDate) {
    return date.equals(this.fromDate) || date.equals(this.toDate) || this.isInside(date) || this.isHovered(date);
  }

  validateInput(currentValue: NgbDate, input: string): NgbDate {
    const parsed = this.formatter.parse(input);
    return parsed && this.calendar.isValid(NgbDate.from(parsed)) ? NgbDate.from(parsed) : currentValue;
  }

  trackClientById(index: number, item: IClient) {
    return item.id;
  }

  onClickFilter() {
    let toDate = null;
    let fromDate = null;
    let clientId = null;
    if (this.fromDate != null) {
      fromDate = this.formatDate(this.fromDate);
    }
    if (this.toDate != null) {
      toDate = this.formatDate(this.toDate).add(1, 'days');
    }
    if (this.selectedClient != null) {
      clientId = this.selectedClient.id;
    }
    this.invoiceSearch.fromDate = fromDate;
    this.invoiceSearch.toDate = toDate;
    this.invoiceSearch.clientId = clientId;
    this.invoiceSearch.customerId = this.customerId;

    // if(!this.isSearchOpr){
      this.initDatatable();
      this.activatedRoute.queryParams
        .subscribe((pageQueryParams: PageQueryParams) => {
          this.datatable.fillPageQueryParams(pageQueryParams);
          this.invoiceSearch.input = this.datatable.getDataTableInput();
          this.invoiceService.getInvoiceBySearch(this.invoiceSearch)
            .subscribe(
              (res) => {
                this.datatable.update(res);
              },
              (res: HttpErrorResponse) => this.onError(res.message)
            );
        });
    // }else
      this.filter(this.isSearchOpr);
    this.isSearchOpr = true;


    // this.invoiceService.getInvoiceBySearch(invoiceSearch)
    //   .pipe(
    //     finalize(() => this.busy = false)
    //   )
    //   .subscribe(
    //     (res) => {
    //       this.datatable.update(res);
    //     },
    //     (res: HttpErrorResponse) => this.onError(res.message)
    //   );




    // this.activatedRoute.queryParams
    //   .subscribe(pageQueryParams => {
    //     this.invoiceService.getInvoiceBySearch(invoiceSearch)
    //       .subscribe(
    //         (res) => {
    //           this.filter(false);
    //           this.datatable.update(res);
    //         },
    //         (res: HttpErrorResponse) => this.onError(res.message)
    //       );
    //   });

    // this.initDatatable();
    // this.activatedRoute.queryParams
    //   .subscribe((pageQueryParams: PageQueryParams) => {
    //     this.datatable.fillPageQueryParams(pageQueryParams);
    //     this.loadData();
    //
    //   });

  }

  sumTaxRate(invoiceItems: IInvoiceItem[]) {
    this.taxRate = 0;
    for (const item of invoiceItems) {
      this.taxRate += item.taxRate;
    }
  }
  ngOnInit() {
    const paymentStatusMapping = [
      { value: PaymentStatus.PAID, type: 'PAID' },
      { value: PaymentStatus.PENDING, type: 'PENDING' },
      { value: PaymentStatus.REFUNDED, type: 'REFUNDED' },
      { value: PaymentStatus.UNPAID, type: 'UNPAID' }
    ];

    this.paymentStatus = paymentStatusMapping;
    this.clientService.getClientByRole()
      .subscribe(
        res => {
          this.clients = res.body ;
        }, res => {
          console.log('An error has occurred when get clientByRole');
        }
      );
    this.initDatatable();
    this.onClickFilter();

    // this.activatedRoute.queryParams
    //   .subscribe((pageQueryParams: PageQueryParams) => {
    //     this.datatable.fillPageQueryParams(pageQueryParams);
    //     this.loadData();
    //   });

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
        name: this.translateService.instant('tbsApp.invoice.paymentStatus'),
        prop: 'paymentStatus',
        headerTemplate: this.headerTemplate,
        cellTemplate: this.paymentStatusRowTemplate
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
  // loadData(invoiceSearch: IInvoiceSearchRequest) {
  //   this.invoiceService.getInvoiceBySearch(invoiceSearch)
  //     .subscribe(
  //       (res) => {
  //         this.datatable.update(res);
  //       },
  //       (res: HttpErrorResponse) => this.onError(res.message)
  //     );
  // }
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

    this.router.navigate(['/invoice'], {queryParams: pageQueryParams});
  }

  search() {
    this.filter(true);
  }

  clearSearch() {
    this.datatable.search = '';
    this.customerId = '';
    this.selectedClient = null;
    this.selectedClient = null;
    this.fromDate = null;
    this.toDate = null;
    this.ngOnInit();

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

  auditInvoiceView(row: any) {
    console.log('Audit invoice: ' + row.accountId);
    this.selectedInvoice = row;
    this.busy = true;
    const that = this;
    this.invoiceService.getInvoiceAudit(row.accountId).subscribe(
      data => {
        that.busy = false;
        data.forEach(log => {
          this.auditInvoice = data;
        });
      },
      err => {
        // that.notification.showNotification('danger', 'Trip audit could not be retrieved')
      }
    );
  }

  viewInvoice(row: any) {
    console.log('Invoice view: ' + row.id);
    this.selectedInvoice = row;
    // this.busy = true;
    // const that = this;
    // this.invoiceService.find(row.id).subscribe(
    //   data => {
    //     that.busy = false;
        this.invoiceView = row;
        this.invoiceItems = row.invoiceItems;
    //   },
    //   err => {
    //     // that.notification.showNotification('danger', 'Trip audit could not be retrieved')
    //   }
    // );
  }

  exportInvoice(id: number) {
    this.busy = true;
    this.invoiceService.exportInvoice(id).subscribe(
      response => {
        this.busy = false;
        if (response.bytes != null) {
          const file = new Blob([ response.bytes ]);
          const array = new Uint8Array(response.bytes.length);
          for (let i = 0; i < response.bytes.length; i++) {
            array[i] = response.bytes.charCodeAt(i);
          }
          const blob = this.b64toBlob(response.bytes, 'application/octet-stream', 512);
          this.downloadAction(window.URL.createObjectURL(blob), response.name);
        } else {
           this.jhiAlertService.error('report.error.reportDownload', null, null);
        }
      },
      err => {
        console.log('An error has occurred when get clientByRole');
      }
    );
  }

  downloadAction(href, fileName) {
    const a = document.createElement('a');
    a.href = href;
    a.download = fileName;
    document.body.appendChild(a);
    a.click();
  }

  b64toBlob(b64Data, contentType, sliceSize) {
    contentType = contentType || '';
    sliceSize = sliceSize || 512;

    const byteCharacters = atob(b64Data);
    const byteArrays = [];

    for (let offset = 0; offset < byteCharacters.length; offset += sliceSize) {
      const slice = byteCharacters.slice(offset, offset	+ sliceSize);
      const byteNumbers = new Array(slice.length);
      for (let i = 0; i < slice.length; i++) {
        byteNumbers[i] = slice.charCodeAt(i);
      }
      const byteArray = new Uint8Array(byteNumbers);
      byteArrays.push(byteArray);
    }

    const blob = new Blob(byteArrays, {
      type : contentType
    });
    return blob;
  }

  formatDate(date: NgbDate) {
    // NgbDates use 1 for Jan, Moement uses 0, must substract 1 month for proper date conversion
    const ngbObj = JSON.parse(JSON.stringify(date));
    // const jsDate = new Date(date.year, date.month - 1, date.day);
    if (ngbObj) {
      // ngbObj.month--;
      return moment(ngbObj.year + '-' + ngbObj.month + '-' + ngbObj.day, 'YYYY-MM-DD');
    }
  }

  humanizeEnumString(data: any) {
    return _tbs.humanizeEnumString(data);
  }


}
