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
  public resourceUrl = SERVER_API_URL + 'billing/items';

  constructor(protected http: HttpClient) {}

  getList(datatableInput: DataTableInput): Observable<Pageable<IItem>> {
    return this.http.get<Pageable<IItem>>(`${this.resourceUrl}/datatable?${_tbs.serializeDataTableRequest(datatableInput)}`);
  }

  create(item: IItem): Observable<EntityResponseType> {
    return this.http.post<IItem>(this.resourceUrl, item, { observe: 'response' });
  }

  update(item: IItem): Observable<EntityResponseType> {
    return this.http.put<IItem>(this.resourceUrl, item, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IItem>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IItem[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
}
