import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import * as moment from 'moment';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import {map} from 'rxjs/operators';

import {SERVER_API_URL} from 'app/app.constants';
import {createRequestOption} from 'app/shared/util/request-util';
import {IPayment} from 'app/shared/model/payment.model';
import {DataTableInput} from 'app/shared/model/datatable/datatable-input';
import {_tbs} from 'app/shared/util/tbs-utility';
import {Pageable} from 'app/shared/model/pageable';
import {IRefund} from "app/shared/model/refund.model";
import {IInvoiceSearchRequest} from "app/shared/model/invoice-serach-request";
import {IInvoice} from "app/shared/model/invoice.model";
import {PaymentSearchRequest} from "app/shared/model/payment-serach-request";
import {environment} from '../../environments/environment';

type EntityResponseType = HttpResponse<IPayment>;
type EntityArrayResponseType = HttpResponse<IPayment[]>;

@Injectable({ providedIn: 'root' })
export class PaymentService {
  public resourceUrl = SERVER_API_URL + 'api/payments';
  public resourceUrlCreditCard = '/billing/newPayment';
  public resourceUrlRefund = '/billing/refunds';

  constructor(protected http: HttpClient) {}

  getList(datatableInput: DataTableInput): Observable<Pageable<IPayment>> {
    return this.http.get<Pageable<IPayment>>(`${this.resourceUrl}/datatable?${_tbs.serializeDataTableRequest(datatableInput)}`);
  }
  getPaymentBySearch(paymentSearchRequest: PaymentSearchRequest): Observable<Pageable<IPayment>> {
    return this.http.post<Pageable<IPayment>>(`${this.resourceUrl}/search`, paymentSearchRequest);
  }
  create(payment: IPayment): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(payment);
    return this.http
      .post<IPayment>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  createCcPayment(accountId): Observable<EntityResponseType> {
    return this.http
      /*.post<IPayment>(this.resourceUrlCreditCard, copy, { observe: 'response' })*/
      .get<IPayment>(this.resourceUrlCreditCard + '/' + accountId + '/CREDIT_CARD', { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  createCcRefund(refund: IRefund): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(refund);
    return this.http
      .post<IPayment>(this.resourceUrlRefund, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(payment: IPayment): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(payment);
    return this.http
      .put<IPayment>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<IPayment>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<IPayment[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  // get signature
  /*getSignature(accountId: number): Observable<any> {
    return this.http
      .get(`${this.resourceUrl}/payfort-signature/${accountId}`, { responseType: 'text'})
     //.pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)))
    ;
  }*/
  initPayfortPayment(invoiceNumber: number): Observable<any> {
    return this.http
      .get<any>(`${this.resourceUrl}/payfort-initiate/${invoiceNumber}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)))
      ;
  }

  payfortTokenization(form: any) {
    return this.http.post(environment.payFortPaymentPage , form,
      {
        headers: new HttpHeaders()
          .set('Content-Type', 'application/x-www-form-urlencoded')
/*          .set('authority', 'sbcheckout.payfort.com')
          .set('path', '/FortAPI/paymentPage')
          .set('origin', 'http://localhost:9000')
          .set('referer', 'http://localhost:9000/#/customer/test-payfort')*/
          // .set('Access-Control-Allow-Origin', 'http://localhost')
          /*.set('Access-Control-Allow-Origin', '*')
          .set('Access-Control-Allow-Methods', 'GET, POST, PATCH, PUT, DELETE, OPTIONS')*/
         // .set('Access-Control-Allow-Headers', '*')
         /*.set('Access-Control-Allow-Headers', 'x-requested-with, Content-Type, origin, authorization, accept, client-security-token')
         .set('Access-Control-Expose-Headers', 'Content-Length, X-JSON')*/
      }
    )// .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)))
      ;
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  protected convertDateFromClient(payment: IPayment): IPayment {
    const copy: IPayment = Object.assign({}, payment, {
      expirationDate: payment.expirationDate != null && payment.expirationDate.isValid() ? payment.expirationDate.toJSON() : null
    });
    return copy;
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.expirationDate = res.body.expirationDate != null ? moment(res.body.expirationDate) : null;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((payment: IPayment) => {
        payment.expirationDate = payment.expirationDate != null ? moment(payment.expirationDate) : null;
      });
    }
    return res;
  }
}
