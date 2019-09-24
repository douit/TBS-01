import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IPaymentMethod } from 'app/shared/model/payment-method.model';

type EntityResponseType = HttpResponse<IPaymentMethod>;
type EntityArrayResponseType = HttpResponse<IPaymentMethod[]>;

@Injectable({ providedIn: 'root' })
export class PaymentMethodService {
  public resourceUrl = SERVER_API_URL + 'api/payment-methods';

  constructor(protected http: HttpClient) {}

  create(paymentMethod: IPaymentMethod): Observable<EntityResponseType> {
    return this.http.post<IPaymentMethod>(this.resourceUrl, paymentMethod, { observe: 'response' });
  }

  update(paymentMethod: IPaymentMethod): Observable<EntityResponseType> {
    return this.http.put<IPaymentMethod>(this.resourceUrl, paymentMethod, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IPaymentMethod>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IPaymentMethod[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
}
