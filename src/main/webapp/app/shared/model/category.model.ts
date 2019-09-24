export interface ICategory {
  id?: number;
  code?: string;
  name?: string;
  description?: string;
}

export class Category implements ICategory {
  constructor(public id?: number, public code?: string, public name?: string, public description?: string) {}
}
