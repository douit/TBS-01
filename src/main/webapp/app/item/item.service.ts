import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IItem } from 'app/shared/model/item.model';

import { Pageable } from 'app/shared/model/pageable';
import { _tbs } from 'app/shared/util/tbs-utility';
import { DataTableInput } from 'app/shared/model/datatable/datatable-input';

type EntityResponseType = HttpResponse<IItem>;
type EntityArrayResponseType = HttpResponse<IItem[]>;

@Injectable({ providedIn: 'root' })
export class ItemService {
  // public resourceClientUrl = SERVER_API_URL + 'billing/items';
  public resourceApiUrl = SERVER_API_URL + 'api/items';

  constructor(protected http: HttpClient) {}

  getList(datatableInput: DataTableInput): Observable<Pageable<IItem>> {
    return this.http.get<Pageable<IItem>>(`${this.resourceApiUrl}/datatable?${_tbs.serializeDataTableRequest(datatableInput)}`);
  }

  create(item: IItem): Observable<EntityResponseType> {
    return this.http.post<IItem>(this.resourceApiUrl, item, { observe: 'response' });
  }

  update(item: IItem): Observable<EntityResponseType> {
    return this.http.put<IItem>(this.resourceApiUrl, item, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IItem>(`${this.resourceApiUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IItem[]>(this.resourceApiUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceApiUrl}/${id}`, { observe: 'response' });
  }

  getItemAudit(id: number) {
    return this.http.get<any>(`${this.resourceApiUrl}/audit/${id}`);
  }
}
