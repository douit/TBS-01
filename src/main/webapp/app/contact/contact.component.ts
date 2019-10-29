import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { filter, map } from 'rxjs/operators';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';

import { IContact } from 'app/shared/model/contact.model';
import { AccountService } from 'app/core/auth/account.service';
import { ContactService } from './contact.service';

@Component({
  selector: 'jhi-contact',
  templateUrl: './contact.component.html'
})
export class ContactComponent implements OnInit, OnDestroy {
  contacts: IContact[];
  currentAccount: any;
  eventSubscriber: Subscription;

  constructor(
    protected contactService: ContactService,
    protected jhiAlertService: JhiAlertService,
    protected eventManager: JhiEventManager,
    protected accountService: AccountService
  ) {}

  loadAll() {
    this.contactService
      .query()
      .pipe(
        filter((res: HttpResponse<IContact[]>) => res.ok),
        map((res: HttpResponse<IContact[]>) => res.body)
      )
      .subscribe(
        (res: IContact[]) => {
          this.contacts = res;
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  ngOnInit() {
    this.loadAll();
    this.accountService.identity().then(account => {
      this.currentAccount = account;
    });
    this.registerChangeInContacts();
  }

  ngOnDestroy() {
    this.eventManager.destroy(this.eventSubscriber);
  }

  trackId(index: number, item: IContact) {
    return item.id;
  }

  registerChangeInContacts() {
    this.eventSubscriber = this.eventManager.subscribe('contactListModification', response => this.loadAll());
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }
}
