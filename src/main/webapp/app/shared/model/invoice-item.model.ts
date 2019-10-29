export interface IInvoiceItem {
  id?: number;
  name?: string;
  description?: string;
  amount?: number;
  quantity?: number;
  taxName?: string;
  taxRate?: number;
  invoiceId?: number;
  discountId?: number;
  itemId?: number;
}

export class InvoiceItem implements IInvoiceItem {
  constructor(
    public id?: number,
    public name?: string,
    public description?: string,
    public amount?: number,
    public quantity?: number,
    public taxName?: string,
    public taxRate?: number,
    public invoiceId?: number,
    public discountId?: number,
    public itemId?: number
  ) {}
}
