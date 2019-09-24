import { Component, OnInit } from '@angular/core';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { IPaymentMethod, PaymentMethod } from 'app/shared/model/payment-method.model';
import { PaymentMethodService } from './payment-method.service';

@Component({
  selector: 'jhi-payment-method-update',
  templateUrl: './payment-method-update.component.html'
})
export class PaymentMethodUpdateComponent implements OnInit {
  isSaving: boolean;

  editForm = this.fb.group({
    id: [],
    name: [],
    code: []
  });

  constructor(protected paymentMethodService: PaymentMethodService, protected activatedRoute: ActivatedRoute, private fb: FormBuilder) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ paymentMethod }) => {
      this.updateForm(paymentMethod);
    });
  }

  updateForm(paymentMethod: IPaymentMethod) {
    this.editForm.patchValue({
      id: paymentMethod.id,
      name: paymentMethod.name,
      code: paymentMethod.code
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const paymentMethod = this.createFromForm();
    if (paymentMethod.id !== undefined) {
      this.subscribeToSaveResponse(this.paymentMethodService.update(paymentMethod));
    } else {
      this.subscribeToSaveResponse(this.paymentMethodService.create(paymentMethod));
    }
  }

  private createFromForm(): IPaymentMethod {
    return {
      ...new PaymentMethod(),
      id: this.editForm.get(['id']).value,
      name: this.editForm.get(['name']).value,
      code: this.editForm.get(['code']).value
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IPaymentMethod>>) {
    result.subscribe(() => this.onSaveSuccess(), () => this.onSaveError());
  }

  protected onSaveSuccess() {
    this.isSaving = false;
    this.previousState();
  }

  protected onSaveError() {
    this.isSaving = false;
  }
}
