import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';

import {ITax} from "app/shared/model/tax.model";



@Injectable({ providedIn: 'root' })
export class TaxService {
  public resourceUrl = SERVER_API_URL + 'billing/taxes';

  constructor(protected http: HttpClient) {}

  getTaxes(): Observable<HttpResponse<ITax[]>> {
    return this.http.get<ITax[]>(`${this.resourceUrl}`, { observe: 'response' });
  }

  getTax(id: number): Observable<HttpResponse<ITax>> {
    return this.http.get<ITax>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

}
