import {Injectable} from '@angular/core';
import {SERVER_API_URL} from 'app/app.constants';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {IStatistics, Statistics} from 'app/shared/model/statistics.model';
import {IChartStatistics} from 'app/shared/model/chart-statistics.model';
import {IStatisticsRequest} from "app/shared/model/chart-statistics-request.model";

@Injectable({ providedIn: 'root' })


export class DashboardService {
  public statisticsUrl = SERVER_API_URL + 'api/statistics';
  public chartStatisticsUrl = SERVER_API_URL + 'api/chartStatistics';

  constructor(protected http: HttpClient) {}

  getStatistics(statisticsRequest:IStatisticsRequest): Observable<IStatistics> {
    return this.http.post<IStatistics>(this.statisticsUrl,statisticsRequest);
  }

  getChartStatistics(chartStatisticsRequest: IChartStatistics): Observable<IChartStatistics[]> {
    return  this.http.post<IChartStatistics[]>(this.chartStatisticsUrl, chartStatisticsRequest);

  }
}
