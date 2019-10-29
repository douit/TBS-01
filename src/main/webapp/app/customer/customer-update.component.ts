import { Component, OnInit } from '@angular/core';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { JhiAlertService } from 'ng-jhipster';
import { ICustomer, Customer } from 'app/shared/model/customer.model';
import { CustomerService } from './customer.service';
import { IContact } from 'app/shared/model/contact.model';
import { ContactService } from 'app/contact/contact.service';

@Component({
  selector: 'jhi-customer-update',
  templateUrl: './customer-update.component.html'
})
export class CustomerUpdateComponent implements OnInit {
  isSaving: boolean;

  contacts: IContact[];

  editForm = this.fb.group({
    id: [],
    identity: [],
    identityType: [],
    name: [],
    contactId: []
  });

  constructor(
    protected jhiAlertService: JhiAlertService,
    protected customerService: CustomerService,
    protected contactService: ContactService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ customer }) => {
      this.updateForm(customer);
    });
    this.contactService
      .query({ filter: 'customer-is-null' })
      .pipe(
        filter((mayBeOk: HttpResponse<IContact[]>) => mayBeOk.ok),
        map((response: HttpResponse<IContact[]>) => response.body)
      )
      .subscribe(
        (res: IContact[]) => {
          if (!this.editForm.get('contactId').value) {
            this.contacts = res;
          } else {
            this.contactService
              .find(this.editForm.get('contactId').value)
              .pipe(
                filter((subResMayBeOk: HttpResponse<IContact>) => subResMayBeOk.ok),
                map((subResponse: HttpResponse<IContact>) => subResponse.body)
              )
              .subscribe(
                (subRes: IContact) => (this.contacts = [subRes].concat(res)),
                (subRes: HttpErrorResponse) => this.onError(subRes.message)
              );
          }
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  updateForm(customer: ICustomer) {
    this.editForm.patchValue({
      id: customer.id,
      identity: customer.identity,
      identityType: customer.identityType,
      name: customer.name,
      contactId: customer.contactId
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const customer = this.createFromForm();
    if (customer.id !== undefined) {
      this.subscribeToSaveResponse(this.customerService.update(customer));
    } else {
      this.subscribeToSaveResponse(this.customerService.create(customer));
    }
  }

  private createFromForm(): ICustomer {
    return {
      ...new Customer(),
      id: this.editForm.get(['id']).value,
      identity: this.editForm.get(['identity']).value,
      identityType: this.editForm.get(['identityType']).value,
      name: this.editForm.get(['name']).value,
      contactId: this.editForm.get(['contactId']).value
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ICustomer>>) {
    result.subscribe(() => this.onSaveSuccess(), () => this.onSaveError());
  }

  protected onSaveSuccess() {
    this.isSaving = false;
    this.previousState();
  }

  protected onSaveError() {
    this.isSaving = false;
  }
  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }

  trackContactById(index: number, item: IContact) {
    return item.id;
  }
}
