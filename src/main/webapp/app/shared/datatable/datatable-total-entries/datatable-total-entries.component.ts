import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-datatable-total-entries',
  templateUrl: './datatable-total-entries.component.html',
  styleUrls: ['./datatable-total-entries.component.scss']
})
export class DatatableTotalEntriesComponent implements OnInit {
  @Input() rowCount;
  @Input() offset;
  @Input() pageSize;
  @Input() curPage;

  toCal: number;

  ngOnInit() {
    this.toCal = ((this.curPage * this.pageSize) < this.rowCount) ? (this.curPage * this.pageSize) : this.rowCount;
    console.log('----'+this.toCal); // object here
  }

}
