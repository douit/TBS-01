import { Moment } from 'moment';
import { IInvoiceItem } from 'app/shared/model/invoice-item.model';
import { IPayment } from 'app/shared/model/payment.model';
import { InvoiceStatus } from 'app/shared/model/enumerations/invoice-status.model';

export interface IInvoice {
  id?: number;
  status?: InvoiceStatus;
  number?: number;
  note?: string;
  dueDate?: Moment;
  subtotal?: number;
  amount?: number;
  discountId?: number;
  customerId?: number;
  invoiceItems?: IInvoiceItem[];
  payments?: IPayment[];
  clientId?: number;
}

export class Invoice implements IInvoice {
  constructor(
    public id?: number,
    public status?: InvoiceStatus,
    public number?: number,
    public note?: string,
    public dueDate?: Moment,
    public subtotal?: number,
    public amount?: number,
    public discountId?: number,
    public customerId?: number,
    public invoiceItems?: IInvoiceItem[],
    public payments?: IPayment[],
    public clientId?: number
  ) {}
}
