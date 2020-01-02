import {TypeStatistics} from "app/shared/model/enumerations/type-statistics.model";
import {Moment} from "moment";
import {IClient} from "app/shared/model/client.model";


export interface IStatisticsRequest {
  fromDate?:Moment ;
  toDate?:Moment ;
  type?: TypeStatistics;
  clientId?:string;
}

export class StatisticsRequest implements IStatisticsRequest {
  constructor(
    fromDate?:Moment ,
  type?: TypeStatistics,
    toDate?:Moment ,
    clientId?:string
  ) {}
}
