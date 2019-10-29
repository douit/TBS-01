import { DiscountType } from 'app/shared/model/enumerations/discount-type.model';

export interface IDiscount {
  id?: number;
  iPercentage?: boolean;
  value?: number;
  type?: DiscountType;
}

export class Discount implements IDiscount {
  constructor(public id?: number, public iPercentage?: boolean, public value?: number, public type?: DiscountType) {
    this.iPercentage = this.iPercentage || false;
  }
}
