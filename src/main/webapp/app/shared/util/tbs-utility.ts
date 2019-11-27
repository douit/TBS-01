/**
 * utility class contain
 * most of the reusable methods
 */
import { LatLng, LatLngLiteral } from '@agm/core';
import * as moment from 'moment';
import { format } from 'libphonenumber-js';

export class _tbs {

  static copyArray<T>(o: Array<T>): Array<T> {
        return _tbs.mapThenCopy(o, item => item);
    }

    static mapThenCopy<T, R>(o: Array<T>, mapper: (item: T) => R): Array<R> {
        const copy = new Array<R>(o.length);
        for (let i = 0; i < o.length; i++) {
            copy[i] = mapper(o[i]);
        }
        return copy;
    }


    static copyCoordinates(coordinates: Array<LatLng>): Array<LatLngLiteral> {
        return _tbs.mapThenCopy(coordinates, sourceCoordinates => {
            return {
                lat: sourceCoordinates.lat(),
                lng: sourceCoordinates.lng()
            };
        });
    }

    static arraysEqual(a: Array<any>, b: Array<any>, sortFunc?: (a: any, b: any) => number): boolean {
        if (a === b) { return true; }
        if (a == null || b == null || !a || !b) { return false; }
        if (a.length != b.length) { return false; }

        if (sortFunc) {
            a = a.sort(sortFunc);
            b = b.sort(sortFunc);
        }

        for (let i = 0; i < a.length; ++i) {
            if (a[i] !== b[i]) { return false; }
        }
        return true;
    }

    static humanizeEnumString(value: string): string {
        return value.toString().toLowerCase()
            .replace(/[_-]/g, ' ')
            .replace(/(?:^|\s)\S/g, function (a) {
                return a.toUpperCase();
            });
    }

    static formatCurrency(value: any): string {
        value = value.replace(/\,/g, '');
        return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'SAR' }).format(value);
    }

    static formatBigDecimal(value: any): string {
        value = value.replace(/\,/g, '');
        return new Intl.NumberFormat('en-US').format(value);
    }

    static formatPhoneNumber(value: string): string {
        return format('+' + value, 'International');
    }

    static formatDate(value: any): string {
        return moment(value).format('MMMM Do YY, h:mm:ss a');
    }

    static formatDateUsing(value: any, format: string): string {
        return moment(value).format(format);
    }

    static formatPercentage(value: any): string {
        return '%' + new Intl.NumberFormat('en-US').format(value);
    }

    static relativeTime(date: string) {
        return moment(date, 'MMMM Do YYYY, h:mm:ss a').fromNow();
    }

    static getDuration(startTime: any, endTime: any): string {
        return moment
            .duration(moment(endTime).diff(moment(startTime))).humanize(false);
    }

    static serializeDataTableRequest(object, prefix?) {
        const str = [];

        for (const key in object) {
            if (object.hasOwnProperty(key)) {
                let k = null;

                if (Array.isArray(object)) {
                    k = prefix ? prefix + '[' + key + ']' : key;
                } else {
                    k = prefix ? prefix + '.' + key : key;
                }

                const value = object[key];

                str.push((value !== null && typeof value === 'object') ?
                    this.serializeDataTableRequest(value, k) :
                    encodeURIComponent(k) + '=' + encodeURIComponent(value));
            }
        }
        return str.join('&');
    }
}
