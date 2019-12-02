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

@Component({
  selector: 'app-payment-test-cc',
  templateUrl: './customer-test-cc.component.html'
})
export class CustomerTestCcComponent implements OnInit {
  isSaving: boolean;

  invoices: IInvoice[];

  submitFormCC: String;

  // postURL = 'https://srstaging.stspayone.com/SmartRoutePaymentWeb/SRPayMsgHandler';

  paymentmethods: IPaymentMethod[];

  invoiceSelected: any;

  invoiceOption: any;

  paymentStatus: boolean;

  editForm = this.fb.group({
    invoiceId: [],
    amount: []
  });

  /*ccForm = this.fb.group({
  });*/
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
          that.paymentStatus = true;
        } else {
          that.paymentStatus = false;
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
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const payment = this.createFromForm();
    this.subscribeToSaveResponse(this.paymentService.createCcPayment(payment));
  }

  /*save2(form: any, e: any): void {
    // Note that I added 'e' and calling the event target's .submit()
      e.target.submit();
    }*/

  private createFromForm(): IPayment {
    return {
      ...new Payment(),
      invoiceId: this.editForm.get(['invoiceId']).value.id,
      amount: this.editForm.get(['amount']).value
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IPayment>>) {
    result.subscribe((res) => this.onSaveSuccess(res), (err) => this.onSaveError(err));
  }

  protected onSaveSuccess(res) {
    this.isSaving = false;
    window.location.href = res.body.redirectUrl;
  }

  protected onSaveError(err) {
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

  onChangeInvoice(val) {
    console.log(val);
    console.log(JSON.stringify(this.invoiceSelected));
    this.editForm.patchValue({'amount' : (this.invoiceSelected != null) ? this.invoiceSelected.amount : 0});
  }
}
