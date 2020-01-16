import {Observable} from 'rxjs/Observable';
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';


@Injectable()
export class ReportService {
    private apiUrl = '/api/report/';
    private apiUrlDP = '/api/v1/deliveryProvider';

    constructor(private http: HttpClient) {
    }

    requestPaymentReport(report: any): Observable<any> {
        return this.http.post(this.apiUrl + 'payment', report);
    }

}
