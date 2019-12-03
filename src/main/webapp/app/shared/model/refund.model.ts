import {RequestStatus} from 'app/shared/model/enumerations/request-status.model';

export interface IRefund {
  id?: number;
  amount?: number;
  requestStatus?: RequestStatus;
  invoiceId?: number;
}

export class Refund implements IRefund {
  constructor(
    public id?: number,
    public amount?: number,
    public requestStatus?: RequestStatus,
    public invoiceId?: number

  ) {}
}
