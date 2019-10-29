import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { filter, map } from 'rxjs/operators';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';

import { IPaymentMethod } from 'app/shared/model/payment-method.model';
import { AccountService } from 'app/core/auth/account.service';
import { PaymentMethodService } from './payment-method.service';

@Component({
  selector: 'jhi-payment-method',
  templateUrl: './payment-method.component.html'
})
export class PaymentMethodComponent implements OnInit, OnDestroy {
  paymentMethods: IPaymentMethod[];
  currentAccount: any;
  eventSubscriber: Subscription;

  constructor(
    protected paymentMethodService: PaymentMethodService,
    protected jhiAlertService: JhiAlertService,
    protected eventManager: JhiEventManager,
    protected accountService: AccountService
  ) {}

  loadAll() {
    this.paymentMethodService
      .query()
      .pipe(
        filter((res: HttpResponse<IPaymentMethod[]>) => res.ok),
        map((res: HttpResponse<IPaymentMethod[]>) => res.body)
      )
      .subscribe(
        (res: IPaymentMethod[]) => {
          this.paymentMethods = res;
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  ngOnInit() {
    this.loadAll();
    this.accountService.identity().then(account => {
      this.currentAccount = account;
    });
    this.registerChangeInPaymentMethods();
  }

  ngOnDestroy() {
    this.eventManager.destroy(this.eventSubscriber);
  }

  trackId(index: number, item: IPaymentMethod) {
    return item.id;
  }

  registerChangeInPaymentMethods() {
    this.eventSubscriber = this.eventManager.subscribe('paymentMethodListModification', response => this.loadAll());
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }
}
