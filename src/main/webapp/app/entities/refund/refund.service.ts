import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IRefund } from 'app/shared/model/refund.model';

type EntityResponseType = HttpResponse<IRefund>;
type EntityArrayResponseType = HttpResponse<IRefund[]>;

@Injectable({ providedIn: 'root' })
export class RefundService {
  public resourceUrl = SERVER_API_URL + 'api/refunds';

  constructor(protected http: HttpClient) {}

  create(refund: IRefund): Observable<EntityResponseType> {
    return this.http.post<IRefund>(this.resourceUrl, refund, { observe: 'response' });
  }

  update(refund: IRefund): Observable<EntityResponseType> {
    return this.http.put<IRefund>(this.resourceUrl, refund, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IRefund>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IRefund[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
}
