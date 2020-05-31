import {Moment} from 'moment';
import {DataTableInput} from 'app/shared/model/datatable/datatable-input';

export interface IInvoiceSearchRequest {
  fromDate?: Moment ;
  toDate?: Moment ;
  clientId?: number;
  customerId?: string;
  accountId?: number;
  input?: DataTableInput;
}

export class InvoiceSearchRequest implements IInvoiceSearchRequest {
  constructor(
    fromDate?: Moment ,
    toDate?: Moment ,
    clientId?: number,
    customerId?: string,
    accountId?: number,
    input?: DataTableInput) {
  }
}
