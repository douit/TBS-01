import { Moment } from 'moment';
import { PaymentStatus } from 'app/shared/model/enumerations/payment-status.model';

export interface IPayment {
  id?: number;
  amount?: number;
  status?: PaymentStatus;
  expirationDate?: Moment;
  invoiceId?: number;
  paymentMethodId?: number;
}

export class Payment implements IPayment {
  constructor(
    public id?: number,
    public amount?: number,
    public status?: PaymentStatus,
    public expirationDate?: Moment,
    public invoiceId?: number,
    public paymentMethodId?: number
  ) {}
}
