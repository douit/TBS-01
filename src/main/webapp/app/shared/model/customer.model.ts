import { IdentityType } from 'app/shared/model/enumerations/identity-type.model';

export interface ICustomer {
  id?: number;
  identity?: string;
  identityType?: IdentityType;
  name?: string;
  contactId?: number;
}

export class Customer implements ICustomer {
  constructor(
    public id?: number,
    public identity?: string,
    public identityType?: IdentityType,
    public name?: string,
    public contactId?: number
  ) {}
}
