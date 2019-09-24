export interface ITax {
  id?: number;
  name?: string;
  description?: string;
  rate?: number;
  itemId?: number;
}

export class Tax implements ITax {
  constructor(public id?: number, public name?: string, public description?: string, public rate?: number, public itemId?: number) {}
}
