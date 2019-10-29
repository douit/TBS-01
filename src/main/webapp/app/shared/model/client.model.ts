import { DateUnit } from 'app/shared/model/enumerations/date-unit.model';

export interface IClient {
  id?: number;
  clientId?: string;
  clientSecret?: string;
  name?: string;
  logo?: string;
  dueDateUnit?: DateUnit;
  dueDateValue?: number;
  vatNumber?: string;
}

export class Client implements IClient {
  constructor(
    public id?: number,
    public clientId?: string,
    public clientSecret?: string,
    public name?: string,
    public logo?: string,
    public dueDateUnit?: DateUnit,
    public dueDateValue?: number,
    public vatNumber?: string
  ) {}
}
