import {Component, OnInit, ViewChild} from '@angular/core';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { HttpResponse, HttpErrorResponse, HttpParams} from '@angular/common/http';
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
import {IRefund, Refund} from 'app/shared/model/refund.model';
import {RequestStatus} from 'app/shared/model/enumerations/request-status.model';
import {Content} from '@angular/compiler/src/render3/r3_ast';

@Component({
  selector: 'app-payment-test-payfort',
  templateUrl: './customer-test-payfort.component.html',
  styleUrls: ['./customer-test-payfort.component.scss']
})
export class CustomerTestPayfortComponent implements OnInit {
  isSaving: boolean;

  invoices: IInvoice[];

  submitFormCC: String;

  paymentmethods: IPaymentMethod[];

  invoiceSelected: any;

  invoiceOption: any;

  operationStatus: boolean;

  busy = false;

  editForm = this.fb.group({
    service_command: ['', [Validators.required]],
    access_code: ['', [Validators.required]],
    merchant_identifier: ['', [Validators.required]],
    merchant_reference: ['', [Validators.required]],
    language: ['', [Validators.required]],
    signature: ['', [Validators.required]],
    return_url: ['', [Validators.required]],
    card_number: ['', [Validators.required]],
    expiry_date: ['', [Validators.required]],
    card_security_code: ['', [Validators.required]],
    card_holder_name: ['', [Validators.required]]
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
        if (params['QSC'] === '1007') {
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
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.operation = 'payment';
    this.isSaving = true;

    const body = new HttpParams()
      .set('service_command', this.editForm.get('service_command').value)
      .set('access_code', this.editForm.get('access_code').value)
      .set('merchant_identifier', this.editForm.get('merchant_identifier').value)
      .set('merchant_reference', this.editForm.get('merchant_reference').value)
      .set('language', this.editForm.get('language').value)
      .set('signature', this.editForm.get('signature').value)
      .set('return_url', this.editForm.get('return_url').value)
      .set('card_number', this.editForm.get('card_number').value)
      .set('expiry_date', this.editForm.get('expiry_date').value)
      .set('card_security_code', this.editForm.get('card_security_code').value)
      .set('card_holder_name', this.editForm.get('card_holder_name').value);


    this.subscribeToSaveResponse(this.paymentService.payfortTokenization(body.toString()));
  }

  protected subscribeToSaveResponse(result: Observable<Object>) {
    result.subscribe((res) => this.onSaveSuccess(res), (err) => this.onSaveError(err));
  }

  protected onSaveSuccess(resp) {
    this.isSaving = false;
    // window.location.href = res.body.link;
    console.log('----Purchase response:' + JSON.stringify(resp));
    if (resp['3ds_url'] != null) {
      window.location.href = resp['3ds_url'];
      return;
    } else if (resp.satatus  === '14') {
      this.operationStatus = true;
    } else {
      this.operationStatus = false;
    }
  }

  protected onSaveError(err) {
    this.isSaving = false;
    console.log('----Tokenization error:' + err);
  }
  protected onError(errorMessage: string) {
    this.operationStatus = false;
    this.jhiAlertService.error(errorMessage, null, null);
  }

  onChangeInvoice(val, type) {
    // console.log(val);
    if (type === 'payment') {
      // console.log(JSON.stringify(this.invoiceSelected));
      // get signature
      this.busy = true;
      this.paymentService.initPayfortPayment(this.invoiceSelected.accountId)
        .subscribe((res: any) => {
          console.log('----Initiate resp:' + JSON.stringify(res));
            this.editForm.patchValue({
              service_command: res.body.service_command,
              access_code: res.body.access_code,
              merchant_identifier: res.body.merchant_identifier,
              merchant_reference: res.body.merchant_reference,
              language: res.body.language,
              signature: res.body.signature,
              return_url: res.body.return_url
            });
            this.busy = false;
        }
        , (res: HttpErrorResponse) => {
            this.busy = false;
            console.log('----Initiate resp error :' + res.message);
          });
      // this.editForm.patchValue({'amount' : (this.invoiceSelected != null) ? this.invoiceSelected.amount : 0});
      const amountField = document.getElementById('field_amount');
      amountField.setAttribute('value', this.invoiceSelected != null ? this.invoiceSelected.amount : 0);
    }
  }
}
