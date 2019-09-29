/**
 * utility class contain
 * most of the reusable methods
 */
import {LatLng, LatLngLiteral} from "@agm/core";
import * as moment from "moment";
import {format} from "libphonenumber-js";

export class _swift{

    static TripStatus = {
        DELIVERED: 'DELIVERED',
        TO_PICKUP: 'TO_PICKUP',
        NEAR_PICKUP: 'NEAR_PICKUP',
        AT_PICKUP: 'AT_PICKUP',
        TO_DESTINATION: 'TO_DESTINATION',
        NEAR_DESTINATION: 'NEAR_DESTINATION',
        AT_DESTINATION: 'AT_DESTINATION',
        CANCELLED: 'CANCELLED'
    };

    static DispatchStatus = {
        NEW_ROUND: 'NEW_ROUND',
        DISPATCHED: 'DISPATCHED',
        UNDISPATCHED: 'UNDISPATCHED',
        MANUAL: 'MANUAL',
        FORCED: 'FORCED',
        CANCELLED: 'CANCELLED'
    };

    /**
     *
     * @param {Array<T>} o
     * @returns {Array<T>}
     */
    static copyArray<T>(o:Array<T>):Array<T>{
        return _swift.mapThenCopy(o,item=> item);
    }


    /**
     *
     * @param {Array<T>} o
     * @param {Mapper<T, R>} mapper
     * @returns {Array<R>}
     */
    static mapThenCopy<T,R>(o:Array<T>,mapper:(item:T) => R):Array<R>{
        let copy = new Array<R>(o.length);
        for(var i=0 ; i < o.length ;i++){
            copy[i] = mapper(o[i]);
        }
        return copy;
    }


    /**
     *
     * @param {Array<LatLng>} coordinates
     * @returns {Array<LatLng>}
     */
    static copyCoordinates(coordinates:Array<LatLng>):Array<LatLngLiteral>{
        return _swift.mapThenCopy(coordinates,sourceCoordinates => {
            return {
                lat:sourceCoordinates.lat(),
                lng:sourceCoordinates.lng()
            };
        });
    }

    /**
     *
     * @param a
     * @param b
     * @returns {boolean}
     */
    static arraysEqual(a:Array<any>, b:Array<any>,sortFunc?:(a:any,b:any)=>number):boolean {
        if (a === b) return true;
        if (a == null || b == null || !a || !b) return false;
        if (a.length != b.length) return false;

        if(sortFunc){
            a = a.sort(sortFunc);
            b = b.sort(sortFunc);
        }

        for (var i = 0; i < a.length; ++i) {
            if (a[i] !== b[i]) return false;
        }
        return true;
    }

    /**
     *
     * @param {string} value
     * @returns {string}
     */
    static humanizeEnumString(value: string): string {
        return value.toString().toLowerCase()
            .replace(/[_-]/g, ' ')
            .replace(/(?:^|\s)\S/g, function(a) {
                return a.toUpperCase();
            });
    }

    /**
     *
     * @param value
     * @returns {string}
     */
    static formatCurrency(value: any): string {
        value = value.replace(/\,/g, '');
        return new Intl.NumberFormat('en-US', {style: 'currency', currency: 'SAR'}).format(value);
    }

    static formatBigDecimal(value: any): string {
        value = value.replace(/\,/g, '');
        return new Intl.NumberFormat('en-US').format(value);
    }

    /**
     *
     * @param {string} value
     * @returns {string}
     */
    static formatPhoneNumber(value: string): string {
        return format('+' + value, 'International');
    }

    /**
     *
     * @param value
     * @returns {string}
     */
    static formatDate(value: any): string {
        return moment(value).format('MMMM Do YY, h:mm:ss a');
    }

    /**
     *
     * @param value
     * @param {string} format
     * @returns {string}
     */
    static formatDateUsing(value: any, format:string): string {
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
}