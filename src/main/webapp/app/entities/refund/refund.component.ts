import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { filter, map } from 'rxjs/operators';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';

import { IRefund } from 'app/shared/model/refund.model';
import { AccountService } from 'app/core/auth/account.service';
import { RefundService } from './refund.service';

@Component({
  selector: 'jhi-refund',
  templateUrl: './refund.component.html'
})
export class RefundComponent implements OnInit, OnDestroy {
  refunds: IRefund[];
  currentAccount: any;
  eventSubscriber: Subscription;

  constructor(
    protected refundService: RefundService,
    protected jhiAlertService: JhiAlertService,
    protected eventManager: JhiEventManager,
    protected accountService: AccountService
  ) {}

  loadAll() {
    this.refundService
      .query()
      .pipe(
        filter((res: HttpResponse<IRefund[]>) => res.ok),
        map((res: HttpResponse<IRefund[]>) => res.body)
      )
      .subscribe(
        (res: IRefund[]) => {
          this.refunds = res;
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  ngOnInit() {
    this.loadAll();
    this.accountService.identity().then(account => {
      this.currentAccount = account;
    });
    this.registerChangeInRefunds();
  }

  ngOnDestroy() {
    this.eventManager.destroy(this.eventSubscriber);
  }

  trackId(index: number, item: IRefund) {
    return item.id;
  }

  registerChangeInRefunds() {
    this.eventSubscriber = this.eventManager.subscribe('refundListModification', response => this.loadAll());
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }
}
