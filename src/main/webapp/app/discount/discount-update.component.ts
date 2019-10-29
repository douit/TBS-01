import { Component, OnInit } from '@angular/core';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { IDiscount, Discount } from 'app/shared/model/discount.model';
import { DiscountService } from './discount.service';

@Component({
  selector: 'jhi-discount-update',
  templateUrl: './discount-update.component.html'
})
export class DiscountUpdateComponent implements OnInit {
  isSaving: boolean;

  editForm = this.fb.group({
    id: [],
    iPercentage: [],
    value: [],
    type: []
  });

  constructor(protected discountService: DiscountService, protected activatedRoute: ActivatedRoute, private fb: FormBuilder) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ discount }) => {
      this.updateForm(discount);
    });
  }

  updateForm(discount: IDiscount) {
    this.editForm.patchValue({
      id: discount.id,
      iPercentage: discount.iPercentage,
      value: discount.value,
      type: discount.type
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const discount = this.createFromForm();
    if (discount.id !== undefined) {
      this.subscribeToSaveResponse(this.discountService.update(discount));
    } else {
      this.subscribeToSaveResponse(this.discountService.create(discount));
    }
  }

  private createFromForm(): IDiscount {
    return {
      ...new Discount(),
      id: this.editForm.get(['id']).value,
      iPercentage: this.editForm.get(['iPercentage']).value,
      value: this.editForm.get(['value']).value,
      type: this.editForm.get(['type']).value
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IDiscount>>) {
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
