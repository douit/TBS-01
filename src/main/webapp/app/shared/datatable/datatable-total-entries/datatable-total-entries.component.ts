import { Component, Input } from '@angular/core';

@Component({
    selector: 'app-datatable-total-entries',
    templateUrl: './datatable-total-entries.component.html',
    styleUrls: ['./datatable-total-entries.component.scss']
})
export class DatatableTotalEntriesComponent {
    @Input() rowCount;
    @Input() offset;
    @Input() pageSize;
    @Input() curPage;
}
