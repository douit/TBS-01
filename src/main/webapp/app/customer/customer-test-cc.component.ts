import {Component, OnInit, ViewChild} from '@angular/core';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import {FormBuilder, FormGroupDirective, Validators} from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import * as moment from 'moment';
import { DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';
import { JhiAlertService } from 'ng-jhipster';
import { IPayment, Payment } from 'app/shared/model/payment.model';
import { PaymentService } from '../payment/payment.service';
import { IInvoice } from 'app/shared/model/invoice.model';
import { InvoiceService } from 'app/invoice/invoice.service';
import { IPaymentMethod } from 'app/shared/model/payment-method.model';
import { PaymentMethodService } from 'app/payment-method/payment-method.service';
import {IRefund, Refund} from "app/shared/model/refund.model";

@Component({
  selector: 'app-payment-test-cc',
  templateUrl: './customer-test-cc.component.html'
})
export class CustomerTestCcComponent implements OnInit {
  isSaving: boolean;

  invoices: IInvoice[];

  invoicesPaid: IInvoice[];

  submitFormCC: String;

  // postURL = 'https://srstaging.stspayone.com/SmartRoutePaymentWeb/SRPayMsgHandler';

  paymentmethods: IPaymentMethod[];

  invoiceSelected: any;

  invoiceRefundSelected: any;

  invoiceOption: any;

  operationStatus: boolean;

  editForm = this.fb.group({
    invoiceId: [],
    amount: []
  });

  editForm2 = this.fb.group({
    invoiceId: [],
    amount: []
  });

  operation = 'payment';
  @ViewChild('ccForm', {static: true})
  ccForm: FormGroupDirective;

  constructor(
    protected jhiAlertService: JhiAlertService,
    protected paymentService: PaymentService,
    protected invoiceService: InvoiceService,
    protected paymentMethodService: PaymentMethodService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {
    const that = this;
    this.activatedRoute.queryParams.subscribe(params => {
      if (params['QSC']) {
        if (params['QSC'] == '1007') {
          that.operationStatus = true;
        } else {
          that.operationStatus = false;
        }
      }
    });
  }

  ngOnInit() {
    this.isSaving = false;
    this.invoiceService
      .queryByPaymentStatus('PENDING')
      .pipe(
        filter((mayBeOk: HttpResponse<IInvoice[]>) => mayBeOk.ok),
        map((response: HttpResponse<IInvoice[]>) => response.body)
      )
      .subscribe((res: IInvoice[]) => (this.invoices = res), (res: HttpErrorResponse) => this.onError(res.message));

    this.invoiceService
      .queryByPaymentStatus('PAID')
      .pipe(
        filter((mayBeOk: HttpResponse<IInvoice[]>) => mayBeOk.ok),
        map((response: HttpResponse<IInvoice[]>) => response.body)
      )
      .subscribe((res: IInvoice[]) => (this.invoicesPaid = res), (res: HttpErrorResponse) => this.onError(res.message));
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.operation = 'payment';
    this.isSaving = true;
    const payment = this.createFromForm(this.editForm);
    this.subscribeToSaveResponse(this.paymentService.createCcPayment(payment));
  }

  private createFromForm(editForm): IPayment {
    return {
      ...new Refund(),
      invoiceId: editForm.get(['invoiceId']).value.id,
      amount: editForm.get(['amount']).value
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IPayment>>) {
    result.subscribe((res) => this.onSaveSuccess(res), (err) => this.onSaveError(err));
  }

  protected onSaveSuccess(res) {
    this.isSaving = false;
    window.location.href = res.body.redirectUrl;
  }

  refund() {
    this.operation = 'refund';
    this.isSaving = true;
    const refund = this.createFromForm(this.editForm2);
    this.subscribeToRefundResponse(this.paymentService.createCcRefund(refund));
  }

  protected subscribeToRefundResponse(result: Observable<HttpResponse<IRefund>>) {
    result.subscribe((res) => this.onRefundSuccess(res), (err) => this.onSaveError(err));
  }

  protected onRefundSuccess(res) {
    this.isSaving = false;
    if (res.requestStatus == 'SUCCEEDED') {
      this.operationStatus = true;
    } else {
      this.operationStatus = false;
    }
  }

  protected onSaveError(err) {
    this.isSaving = false;
  }
  protected onError(errorMessage: string) {
    this.operationStatus = false;
    this.jhiAlertService.error(errorMessage, null, null);
  }

  trackInvoiceById(index: number, item: IInvoice) {
    return item.id;
  }

  trackPaymentMethodById(index: number, item: IPaymentMethod) {
    return item.id;
  }

  onChangeInvoice(val, type) {
    console.log(val);
    if (type === 'payment') {
      console.log(JSON.stringify(this.invoiceSelected));
      this.editForm.patchValue({'amount' : (this.invoiceSelected != null) ? this.invoiceSelected.amount : 0});
    } else {
      console.log(JSON.stringify(this.invoiceRefundSelected));
      this.editForm2.patchValue({'amount' : (this.invoiceRefundSelected != null) ? this.invoiceRefundSelected.amount : 0});
    }
  }
}
