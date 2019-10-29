/**
 * Enums
 */

export enum LoadingButtonStyles {
    ZOOM_IN = 'zoom-in'
}

/**
 * Classes
 */

export class DatatableOptions {
    static PAGE_SIZE = 10;
    static DEBOUNCE_TIME = 1000; // milliseconds
    static SEARCH_DELAY = 5000;
    static FOOTER_HEIGHT = 65;
    static AUTOCOMPLETE_PAGE_SIZE = 25;
}

export class LoadingOptions {
    static BUTTON = {
        STYLE: LoadingButtonStyles.ZOOM_IN
    };
}
