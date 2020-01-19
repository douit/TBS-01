import {Observable} from 'rxjs/Observable';
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {_tbs} from 'app/shared/util/tbs-utility';
import {DataTableInput} from 'app/shared/model/datatable/datatable-input';
import {Pageable} from 'app/shared/model/pageable';
import {IReport} from 'app/shared/model/report.model';


@Injectable({ providedIn: 'root' })
export class ReportService {
    private apiUrl = '/api/report';

    constructor(private http: HttpClient) {
    }

    getPaymentReportList(datatableInput: DataTableInput): Observable<Pageable<IReport>> {
      return this.http.get<Pageable<IReport>>(`${this.apiUrl}/payment/datatable?${_tbs.serializeDataTableRequest(datatableInput)}`);
    }

    requestPaymentReport(report: any): Observable<any> {
        return this.http.post(this.apiUrl + '/payment', report);
    }

}
