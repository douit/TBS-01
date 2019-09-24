import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { filter, map } from 'rxjs/operators';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';

import { ITax } from 'app/shared/model/tax.model';
import { AccountService } from 'app/core/auth/account.service';
import { TaxService } from './tax.service';

@Component({
  selector: 'jhi-tax',
  templateUrl: './tax.component.html'
})
export class TaxComponent implements OnInit, OnDestroy {
  taxes: ITax[];
  currentAccount: any;
  eventSubscriber: Subscription;

  constructor(
    protected taxService: TaxService,
    protected jhiAlertService: JhiAlertService,
    protected eventManager: JhiEventManager,
    protected accountService: AccountService
  ) {}

  loadAll() {
    this.taxService
      .query()
      .pipe(
        filter((res: HttpResponse<ITax[]>) => res.ok),
        map((res: HttpResponse<ITax[]>) => res.body)
      )
      .subscribe(
        (res: ITax[]) => {
          this.taxes = res;
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  ngOnInit() {
    this.loadAll();
    this.accountService.identity().then(account => {
      this.currentAccount = account;
    });
    this.registerChangeInTaxes();
  }

  ngOnDestroy() {
    this.eventManager.destroy(this.eventSubscriber);
  }

  trackId(index: number, item: ITax) {
    return item.id;
  }

  registerChangeInTaxes() {
    this.eventSubscriber = this.eventManager.subscribe('taxListModification', response => this.loadAll());
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }
}
