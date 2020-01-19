import { Moment } from 'moment';
import { IInvoiceItem } from 'app/shared/model/invoice-item.model';
import { IPayment } from 'app/shared/model/payment.model';
import { InvoiceStatus } from 'app/shared/model/enumerations/invoice-status.model';
import {PaymentStatus, ReportStatus} from 'app/shared/constants';

export interface IReport {
  id?: number;
  status?: ReportStatus;
  requestDate?: Moment;
  generatedDate?: Moment;
  expireDate?: Moment;
}

export class Report implements IReport {
  constructor(
    public id?: number,
    public status?: ReportStatus,
    public requestDate?: Moment,
    public generatedDate?: Moment,
    public expireDate?: Moment
  ) {}
}
