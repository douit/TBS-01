import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IPayment } from 'app/shared/model/payment.model';

@Component({
  selector: 'jhi-payment-detail',
  templateUrl: './payment-detail.component.html'
})
export class PaymentDetailComponent implements OnInit {
  payment: IPayment;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ payment }) => {
      this.payment = payment;
    });
  }

  previousState() {
    window.history.back();
  }
}
