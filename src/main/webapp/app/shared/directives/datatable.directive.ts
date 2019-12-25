import { AfterViewInit, Directive, ElementRef } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Directive({
    selector: '[appNgxDatatable]',
})
export class DatatableDirective implements AfterViewInit {

    constructor(private el: ElementRef, private translateService: TranslateService) {

    }

    ngAfterViewInit() {
        const pager = this.el.nativeElement.querySelector('.datatable-footer .datatable-pager .pager');

        if (pager) {
            const first = pager.children[0];
            const prev = pager.children[1];
            const next = pager.children[pager.children.length - 2];
            const last = pager.children[pager.children.length - 1];

            // Add `page-navigate` class to `first`, `prev`, `next` and `last` buttons
            first.classList.add('page-navigate');
            prev.classList.add('page-navigate');
            next.classList.add('page-navigate');
            last.classList.add('page-navigate');

            // Subscribe to observable return by `stream method`, so when language changes, it updates the text
            this.translateService.stream('global.datatable.first').subscribe(text => {
                first.children[0].innerHTML = text;
            });

            // Subscribe to observable return by `stream method`, so when language changes, it updates the text
            this.translateService.stream('global.datatable.previous').subscribe(text => {
                prev.children[0].innerHTML = text;
            });

            // Subscribe to observable return by `stream method`, so when language changes, it updates the text
            this.translateService.stream('global.datatable.next').subscribe(text => {
                next.children[0].innerHTML = text;
            });

            // Subscribe to observable return by `stream method`, so when language changes, it updates the text
            this.translateService.stream('global.datatable.last').subscribe(text => {
                last.children[0].innerHTML = text;
            });
        }
    }
}
