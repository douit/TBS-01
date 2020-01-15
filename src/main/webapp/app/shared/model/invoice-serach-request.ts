import { ITax } from 'app/shared/model/tax.model';
import {Moment} from "moment";
import {TypeStatistics} from "app/shared/model/enumerations/type-statistics.model";
import {PaymentStatus} from "app/shared/constants";
import {Pageable} from "app/shared/model/pageable";
import {IInvoice} from "app/shared/model/invoice.model";
import {DataTableInput} from "app/shared/model/datatable/datatable-input";

export interface IInvoiceSearchRequest {
  fromDate?:Moment ;
  toDate?:Moment ;
  clientId?:number;
  customerId?:string;
  input?: DataTableInput;
}

export class InvoiceSearchRequest implements IInvoiceSearchRequest {
  constructor(
    fromDate?:Moment ,
  toDate?:Moment ,
  clientId?:number,
  customerId?:string,
    input?:DataTableInput
) {}
}
