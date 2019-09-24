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
import { IPayment, Payment } from 'app/shared/model/payment.model';
import { PaymentService } from './payment.service';
import { IInvoice } from 'app/shared/model/invoice.model';
import { InvoiceService } from 'app/entities/invoice/invoice.service';
import { IPaymentMethod } from 'app/shared/model/payment-method.model';
import { PaymentMethodService } from 'app/entities/payment-method/payment-method.service';

@Component({
  selector: 'jhi-payment-update',
  templateUrl: './payment-update.component.html'
})
export class PaymentUpdateComponent implements OnInit {
  isSaving: boolean;

  invoices: IInvoice[];

  paymentmethods: IPaymentMethod[];

  editForm = this.fb.group({
    id: [],
    amount: [],
    status: [],
    expirationDate: [],
    invoiceId: [],
    paymentMethodId: []
  });

  constructor(
    protected jhiAlertService: JhiAlertService,
    protected paymentService: PaymentService,
    protected invoiceService: InvoiceService,
    protected paymentMethodService: PaymentMethodService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ payment }) => {
      this.updateForm(payment);
    });
    this.invoiceService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<IInvoice[]>) => mayBeOk.ok),
        map((response: HttpResponse<IInvoice[]>) => response.body)
      )
      .subscribe((res: IInvoice[]) => (this.invoices = res), (res: HttpErrorResponse) => this.onError(res.message));
    this.paymentMethodService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<IPaymentMethod[]>) => mayBeOk.ok),
        map((response: HttpResponse<IPaymentMethod[]>) => response.body)
      )
      .subscribe((res: IPaymentMethod[]) => (this.paymentmethods = res), (res: HttpErrorResponse) => this.onError(res.message));
  }

  updateForm(payment: IPayment) {
    this.editForm.patchValue({
      id: payment.id,
      amount: payment.amount,
      status: payment.status,
      expirationDate: payment.expirationDate != null ? payment.expirationDate.format(DATE_TIME_FORMAT) : null,
      invoiceId: payment.invoiceId,
      paymentMethodId: payment.paymentMethodId
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const payment = this.createFromForm();
    if (payment.id !== undefined) {
      this.subscribeToSaveResponse(this.paymentService.update(payment));
    } else {
      this.subscribeToSaveResponse(this.paymentService.create(payment));
    }
  }

  private createFromForm(): IPayment {
    return {
      ...new Payment(),
      id: this.editForm.get(['id']).value,
      amount: this.editForm.get(['amount']).value,
      status: this.editForm.get(['status']).value,
      expirationDate:
        this.editForm.get(['expirationDate']).value != null
          ? moment(this.editForm.get(['expirationDate']).value, DATE_TIME_FORMAT)
          : undefined,
      invoiceId: this.editForm.get(['invoiceId']).value,
      paymentMethodId: this.editForm.get(['paymentMethodId']).value
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IPayment>>) {
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

  trackInvoiceById(index: number, item: IInvoice) {
    return item.id;
  }

  trackPaymentMethodById(index: number, item: IPaymentMethod) {
    return item.id;
  }
}
