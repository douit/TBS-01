import {Moment} from 'moment';
import {PaymentStatus} from 'app/shared/constants';
import {DataTableInput} from 'app/shared/model/datatable/datatable-input';

export interface IPaymentSearchRequest {
  fromDate?: Moment ;
  toDate?: Moment ;
  paymentStatus?: PaymentStatus;
  clientId?: number;
  customerId?: number;
  accountId?: number;
  input?: DataTableInput;
}

export class PaymentSearchRequest implements IPaymentSearchRequest {
  constructor(
    fromDate?: Moment ,
  toDate?: Moment ,
  paymentStatus?: PaymentStatus,
  clientId?: number,
  customerId?: number,
    accountId?: number,
    input?: DataTableInput
) {}
}
