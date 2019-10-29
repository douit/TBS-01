import { ITax } from 'app/shared/model/tax.model';

export interface IItem {
  id?: number;
  name?: string;
  description?: string;
  price?: number;
  defaultQuantity?: number;
  taxes?: ITax[];
  categoryId?: number;
  clientId?: number;
}

export class Item implements IItem {
  constructor(
    public id?: number,
    public name?: string,
    public description?: string,
    public price?: number,
    public defaultQuantity?: number,
    public taxes?: ITax[],
    public categoryId?: number,
    public clientId?: number
  ) {}
}
