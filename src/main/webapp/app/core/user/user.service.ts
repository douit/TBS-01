import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';

import {SERVER_API_URL} from '../../app.constants';
import {createRequestOption} from '../../shared/util/request-util';
import {IUser} from './user.model';
import {DataTableInput} from 'app/shared/model/datatable/datatable-input';
import {Pageable} from 'app/shared/model/pageable';
import {_tbs} from 'app/shared/util/tbs-utility';

@Injectable({ providedIn: 'root' })
export class UserService {
  public resourceUrl = SERVER_API_URL + 'api/users';

  constructor(private http: HttpClient) {}

  getList(datatableInput: DataTableInput): Observable<Pageable<IUser>> {
    return this.http.get<Pageable<IUser>>(`${this.resourceUrl}/datatable?${_tbs.serializeDataTableRequest(datatableInput)}`);
  }

  create(user: IUser): Observable<HttpResponse<IUser>> {
    return this.http.post<IUser>(this.resourceUrl, user, { observe: 'response' });
  }

  update(user: IUser): Observable<HttpResponse<IUser>> {
    return this.http.put<IUser>(this.resourceUrl, user, { observe: 'response' });
  }

  find(login: string): Observable<HttpResponse<IUser>> {
    return this.http.get<IUser>(`${this.resourceUrl}/${login}`, { observe: 'response' });
  }

  query(req?: any): Observable<HttpResponse<IUser[]>> {
    const options = createRequestOption(req);
    return this.http.get<IUser[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(login: string): Observable<HttpResponse<any>> {
    return this.http.delete(`${this.resourceUrl}/${login}`, { observe: 'response' });
  }

  authorities(): Observable<string[]> {
    return this.http.get<string[]>(SERVER_API_URL + 'api/users/authorities');
  }
}
