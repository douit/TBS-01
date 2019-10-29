export interface IPaymentMethod {
  id?: number;
  name?: string;
  code?: string;
}

export class PaymentMethod implements IPaymentMethod {
  constructor(public id?: number, public name?: string, public code?: string) {}
}
