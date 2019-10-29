export class Pageable<T> {
    data = new Array<T>();
    draw: number;
    recordsFiltered: number;
    recordsTotal: number;
}
