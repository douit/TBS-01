import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { filter, map } from 'rxjs/operators';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';

import { IDiscount } from 'app/shared/model/discount.model';
import { AccountService } from 'app/core/auth/account.service';
import { DiscountService } from './discount.service';

@Component({
  selector: 'jhi-discount',
  templateUrl: './discount.component.html'
})
export class DiscountComponent implements OnInit, OnDestroy {
  discounts: IDiscount[];
  currentAccount: any;
  eventSubscriber: Subscription;

  constructor(
    protected discountService: DiscountService,
    protected jhiAlertService: JhiAlertService,
    protected eventManager: JhiEventManager,
    protected accountService: AccountService
  ) {}

  loadAll() {
    this.discountService
      .query()
      .pipe(
        filter((res: HttpResponse<IDiscount[]>) => res.ok),
        map((res: HttpResponse<IDiscount[]>) => res.body)
      )
      .subscribe(
        (res: IDiscount[]) => {
          this.discounts = res;
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  ngOnInit() {
    this.loadAll();
    this.accountService.identity().then(account => {
      this.currentAccount = account;
    });
    this.registerChangeInDiscounts();
  }

  ngOnDestroy() {
    this.eventManager.destroy(this.eventSubscriber);
  }

  trackId(index: number, item: IDiscount) {
    return item.id;
  }

  registerChangeInDiscounts() {
    this.eventSubscriber = this.eventManager.subscribe('discountListModification', response => this.loadAll());
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }
}
