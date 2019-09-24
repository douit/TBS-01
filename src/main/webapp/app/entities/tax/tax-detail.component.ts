import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ITax } from 'app/shared/model/tax.model';

@Component({
  selector: 'jhi-tax-detail',
  templateUrl: './tax-detail.component.html'
})
export class TaxDetailComponent implements OnInit {
  tax: ITax;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ tax }) => {
      this.tax = tax;
    });
  }

  previousState() {
    window.history.back();
  }
}
