import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { ITax } from 'app/shared/model/tax.model';

type EntityResponseType = HttpResponse<ITax>;
type EntityArrayResponseType = HttpResponse<ITax[]>;

@Injectable({ providedIn: 'root' })
export class TaxService {
  public resourceUrl = SERVER_API_URL + 'api/taxes';

  constructor(protected http: HttpClient) {}

  create(tax: ITax): Observable<EntityResponseType> {
    return this.http.post<ITax>(this.resourceUrl, tax, { observe: 'response' });
  }

  update(tax: ITax): Observable<EntityResponseType> {
    return this.http.put<ITax>(this.resourceUrl, tax, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<ITax>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<ITax[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
}
