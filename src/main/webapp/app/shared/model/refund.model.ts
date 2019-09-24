import { PaymentStatus } from 'app/shared/model/enumerations/payment-status.model';

export interface IRefund {
  id?: number;
  amount?: number;
  status?: PaymentStatus;
  refundId?: string;
}

export class Refund implements IRefund {
  constructor(public id?: number, public amount?: number, public status?: PaymentStatus, public refundId?: string) {}
}
