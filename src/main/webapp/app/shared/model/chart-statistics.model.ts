import {TypeStatistics} from "app/shared/model/enumerations/type-statistics.model";

export interface IChartStatistics {
  totalInvoice?: number;
  totalPaid?: number;
  month?: number;
  day?: number;
  type?: TypeStatistics;
  duration?:Date ;


}

export class ChartStatistics implements IChartStatistics {
  constructor(
    totalInvoice?: number,
  totalPaid?: number,
  month?: number,
  day?: number,
  type?: TypeStatistics,
  duration?:Date
  ) {}
}
