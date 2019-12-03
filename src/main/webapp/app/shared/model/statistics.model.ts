import { ITax } from 'app/shared/model/tax.model';

export interface IStatistics {
  totalInvoice?: number;
  totalPaid?: number;
  amountRefund?: number;
  income?: number;

}

export class Statistics implements IStatistics {
  constructor(
    totalInvoice?: number,
  totalPaid?: number,
  amountRefund?: number,
  income?: number
  ) {}
}
