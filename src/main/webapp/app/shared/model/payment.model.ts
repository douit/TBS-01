import { Moment } from 'moment';
import { PaymentStatus } from 'app/shared/model/enumerations/payment-status.model';
import {IPaymentMethod} from 'app/shared/model/payment-method.model';

export interface IPayment {
  id?: number;
  amount?: number;
  status?: PaymentStatus;
  expirationDate?: Moment;
  invoiceId?: number;
  paymentMethod?: IPaymentMethod;
  redirectUrl?: string;
  transactionId?: string;
  lastModifiedDate?: Date;
}

export class Payment implements IPayment {
  constructor(
    public id?: number,
    public amount?: number,
    public status?: PaymentStatus,
    public expirationDate?: Moment,
    public invoiceId?: number,
    public paymentMethod?: IPaymentMethod,
    public redirectUrl?: string,
    public transactionId?: string,
    public lastModifiedDate?: Date
  ) {}
}
