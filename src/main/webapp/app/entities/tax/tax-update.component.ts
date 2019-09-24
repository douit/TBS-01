import { Component, OnInit } from '@angular/core';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { JhiAlertService } from 'ng-jhipster';
import { ITax, Tax } from 'app/shared/model/tax.model';
import { TaxService } from './tax.service';
import { IItem } from 'app/shared/model/item.model';
import { ItemService } from 'app/entities/item/item.service';

@Component({
  selector: 'jhi-tax-update',
  templateUrl: './tax-update.component.html'
})
export class TaxUpdateComponent implements OnInit {
  isSaving: boolean;

  items: IItem[];

  editForm = this.fb.group({
    id: [],
    name: [],
    description: [],
    rate: [],
    itemId: []
  });

  constructor(
    protected jhiAlertService: JhiAlertService,
    protected taxService: TaxService,
    protected itemService: ItemService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ tax }) => {
      this.updateForm(tax);
    });
    this.itemService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<IItem[]>) => mayBeOk.ok),
        map((response: HttpResponse<IItem[]>) => response.body)
      )
      .subscribe((res: IItem[]) => (this.items = res), (res: HttpErrorResponse) => this.onError(res.message));
  }

  updateForm(tax: ITax) {
    this.editForm.patchValue({
      id: tax.id,
      name: tax.name,
      description: tax.description,
      rate: tax.rate,
      itemId: tax.itemId
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const tax = this.createFromForm();
    if (tax.id !== undefined) {
      this.subscribeToSaveResponse(this.taxService.update(tax));
    } else {
      this.subscribeToSaveResponse(this.taxService.create(tax));
    }
  }

  private createFromForm(): ITax {
    return {
      ...new Tax(),
      id: this.editForm.get(['id']).value,
      name: this.editForm.get(['name']).value,
      description: this.editForm.get(['description']).value,
      rate: this.editForm.get(['rate']).value,
      itemId: this.editForm.get(['itemId']).value
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ITax>>) {
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

  trackItemById(index: number, item: IItem) {
    return item.id;
  }
}
