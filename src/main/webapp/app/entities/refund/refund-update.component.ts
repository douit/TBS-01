import { Component, OnInit } from '@angular/core';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { IRefund, Refund } from 'app/shared/model/refund.model';
import { RefundService } from './refund.service';

@Component({
  selector: 'jhi-refund-update',
  templateUrl: './refund-update.component.html'
})
export class RefundUpdateComponent implements OnInit {
  isSaving: boolean;

  editForm = this.fb.group({
    id: [],
    amount: [],
    status: [],
    refundId: []
  });

  constructor(protected refundService: RefundService, protected activatedRoute: ActivatedRoute, private fb: FormBuilder) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ refund }) => {
      this.updateForm(refund);
    });
  }

  updateForm(refund: IRefund) {
    this.editForm.patchValue({
      id: refund.id,
      amount: refund.amount,
      status: refund.status,
      refundId: refund.refundId
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const refund = this.createFromForm();
    if (refund.id !== undefined) {
      this.subscribeToSaveResponse(this.refundService.update(refund));
    } else {
      this.subscribeToSaveResponse(this.refundService.create(refund));
    }
  }

  private createFromForm(): IRefund {
    return {
      ...new Refund(),
      id: this.editForm.get(['id']).value,
      amount: this.editForm.get(['amount']).value,
      status: this.editForm.get(['status']).value,
      refundId: this.editForm.get(['refundId']).value
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IRefund>>) {
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
