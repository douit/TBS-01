export interface IContact {
  id?: number;
  email?: string;
  phone?: string;
  address?: string;
  street?: string;
  city?: string;
  countryId?: number;
}

export class Contact implements IContact {
  constructor(
    public id?: number,
    public email?: string,
    public phone?: string,
    public address?: string,
    public street?: string,
    public city?: string,
    public countryId?: number
  ) {}
}
