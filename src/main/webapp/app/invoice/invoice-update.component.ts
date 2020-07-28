import { Component, OnInit } from '@angular/core';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import * as moment from 'moment';
import { DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';
import { JhiAlertService } from 'ng-jhipster';
import { IInvoice, Invoice } from 'app/shared/model/invoice.model';
import { InvoiceService } from './invoice.service';
import { IDiscount } from 'app/shared/model/discount.model';
import { DiscountService } from 'app/discount/discount.service';
import { ICustomer } from 'app/shared/model/customer.model';
import { CustomerService } from 'app/customer/customer.service';
import { IClient } from 'app/shared/model/client.model';
import { ClientService } from 'app/client/client.service';

@Component({
  selector: 'jhi-invoice-update',
  templateUrl: './invoice-update.component.html'
})
export class InvoiceUpdateComponent implements OnInit {
  isSaving: boolean;

  discounts: IDiscount[];

  customers: ICustomer[];

  clients: IClient[];

  editForm = this.fb.group({
    id: [],
    status: [],
    number: [],
    note: [],
    dueDate: [],
    subtotal: [],
    amount: [],
    discountId: [],
    customerId: [],
    clientId: []
  });

  constructor(
    protected jhiAlertService: JhiAlertService,
    protected invoiceService: InvoiceService,
    protected discountService: DiscountService,
    protected customerService: CustomerService,
    protected clientService: ClientService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ invoice }) => {
      this.updateForm(invoice);
    });
    this.discountService
      .query({ filter: 'invoice-is-null' })
      .pipe(
        filter((mayBeOk: HttpResponse<IDiscount[]>) => mayBeOk.ok),
        map((response: HttpResponse<IDiscount[]>) => response.body)
      )
      .subscribe(
        (res: IDiscount[]) => {
          if (!this.editForm.get('discountId').value) {
            this.discounts = res;
          } else {
            this.discountService
              .find(this.editForm.get('discountId').value)
              .pipe(
                filter((subResMayBeOk: HttpResponse<IDiscount>) => subResMayBeOk.ok),
                map((subResponse: HttpResponse<IDiscount>) => subResponse.body)
              )
              .subscribe(
                (subRes: IDiscount) => (this.discounts = [subRes].concat(res)),
                (subRes: HttpErrorResponse) => this.onError(subRes.message)
              );
          }
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
    this.customerService
      .query({ filter: 'invoice-is-null' })
      .pipe(
        filter((mayBeOk: HttpResponse<ICustomer[]>) => mayBeOk.ok),
        map((response: HttpResponse<ICustomer[]>) => response.body)
      )
      .subscribe(
        (res: ICustomer[]) => {
          if (!this.editForm.get('customerId').value) {
            this.customers = res;
          } else {
            this.customerService
              .find(this.editForm.get('customerId').value)
              .pipe(
                filter((subResMayBeOk: HttpResponse<ICustomer>) => subResMayBeOk.ok),
                map((subResponse: HttpResponse<ICustomer>) => subResponse.body)
              )
              .subscribe(
                (subRes: ICustomer) => (this.customers = [subRes].concat(res)),
                (subRes: HttpErrorResponse) => this.onError(subRes.message)
              );
          }
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
    this.clientService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<IClient[]>) => mayBeOk.ok),
        map((response: HttpResponse<IClient[]>) => response.body)
      )
      .subscribe((res: IClient[]) => (this.clients = res), (res: HttpErrorResponse) => this.onError(res.message));
  }

  updateForm(invoice: IInvoice) {
    this.editForm.patchValue({
      id: invoice.id,
      status: invoice.status,
      number: invoice.number,
      note: invoice.note,
      dueDate: invoice.dueDate != null ? invoice.dueDate.format(DATE_TIME_FORMAT) : null,
      subtotal: invoice.subtotal,
      amount: invoice.amount,
      discountId: invoice.discount.id,
      customerId: invoice.customerId,
      clientId: invoice.clientId
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const invoice = this.createFromForm();
    if (invoice.id !== undefined) {
      this.subscribeToSaveResponse(this.invoiceService.update(invoice));
    } else {
      this.subscribeToSaveResponse(this.invoiceService.create(invoice));
    }
  }

  private createFromForm(): IInvoice {
    return {
      ...new Invoice(),
      id: this.editForm.get(['id']).value,
      status: this.editForm.get(['status']).value,
      number: this.editForm.get(['number']).value,
      note: this.editForm.get(['note']).value,
      dueDate: this.editForm.get(['dueDate']).value != null ? moment(this.editForm.get(['dueDate']).value, DATE_TIME_FORMAT) : undefined,
      subtotal: this.editForm.get(['subtotal']).value,
      amount: this.editForm.get(['amount']).value,
      // discountId: this.editForm.get(['discountId']).value,
      customerId: this.editForm.get(['customerId']).value,
      clientId: this.editForm.get(['clientId']).value
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IInvoice>>) {
    result.subscribe(() => this.onSaveSuccess(), () => this.onSaveError());
  }

  protected onSaveSuccess() {
    this.isSaving = false;
    this.previousState();
  }

  protected onSaveError() {
    this.isSaving = false;
  }
  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }

  trackDiscountById(index: number, item: IDiscount) {
    return item.id;
  }

  trackCustomerById(index: number, item: ICustomer) {
    return item.id;
  }

  trackClientById(index: number, item: IClient) {
    return item.id;
  }
}
