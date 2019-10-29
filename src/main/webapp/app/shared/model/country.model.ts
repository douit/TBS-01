export interface ICountry {
  id?: number;
  name?: string;
  nameAr?: string;
}

export class Country implements ICountry {
  constructor(public id?: number, public name?: string, public nameAr?: string) {}
}
