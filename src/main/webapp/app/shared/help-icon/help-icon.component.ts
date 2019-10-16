import {Component, Input, OnInit} from '@angular/core';

declare var $: any;

@Component({
  selector: 'app-help-icon',
  templateUrl: './help-icon.component.html',
  styleUrls: ['./help-icon.component.scss']
})

export class HelpIconComponent implements OnInit{

  @Input() titleMsg: string;

  ngOnInit() {
    $(function () {
      $('[data-toggle="tooltip"]').tooltip()
    })
  }

}
