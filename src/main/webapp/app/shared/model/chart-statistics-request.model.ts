import {TypeStatistics} from "app/shared/model/enumerations/type-statistics.model";
import {Moment} from "moment";


export interface IChartStatisticsRequest {
  date?:Moment ;
  type?: TypeStatistics;
}

export class ChartStatisticsRequest implements IChartStatisticsRequest {
  constructor(
    date?:Moment ,
  type?: TypeStatistics
  ) {}
}
