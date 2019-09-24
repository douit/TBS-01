import { Component, OnInit } from '@angular/core';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { JhiAlertService } from 'ng-jhipster';
import { IInvoiceItem, InvoiceItem } from 'app/shared/model/invoice-item.model';
import { InvoiceItemService } from './invoice-item.service';
import { IInvoice } from 'app/shared/model/invoice.model';
import { InvoiceService } from 'app/entities/invoice/invoice.service';
import { IDiscount } from 'app/shared/model/discount.model';
import { DiscountService } from 'app/entities/discount/discount.service';
import { IItem } from 'app/shared/model/item.model';
import { ItemService } from 'app/entities/item/item.service';

@Component({
  selector: 'jhi-invoice-item-update',
  templateUrl: './invoice-item-update.component.html'
})
export class InvoiceItemUpdateComponent implements OnInit {
  isSaving: boolean;

  invoices: IInvoice[];

  discounts: IDiscount[];

  items: IItem[];

  editForm = this.fb.group({
    id: [],
    name: [],
    description: [],
    amount: [],
    quantity: [],
    taxName: [],
    taxRate: [],
    invoiceId: [],
    discountId: [],
    itemId: []
  });

  constructor(
    protected jhiAlertService: JhiAlertService,
    protected invoiceItemService: InvoiceItemService,
    protected invoiceService: InvoiceService,
    protected discountService: DiscountService,
    protected itemService: ItemService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ invoiceItem }) => {
      this.updateForm(invoiceItem);
    });
    this.invoiceService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<IInvoice[]>) => mayBeOk.ok),
        map((response: HttpResponse<IInvoice[]>) => response.body)
      )
      .subscribe((res: IInvoice[]) => (this.invoices = res), (res: HttpErrorResponse) => this.onError(res.message));
    this.discountService
      .query({ filter: 'invoiceitem-is-null' })
      .pipe(
        filter((mayBeOk: HttpResponse<IDiscount[]>) => mayBeOk.ok),
        map((response: HttpResponse<IDiscount[]>) => response.body)
      )
      .subscribe(
        (res: IDiscount[]) => {
          if (!this.editForm.get('discountId').value) {
            this.discounts = res;
          } else {
            this.discountService
              .find(this.editForm.get('discountId').value)
              .pipe(
                filter((subResMayBeOk: HttpResponse<IDiscount>) => subResMayBeOk.ok),
                map((subResponse: HttpResponse<IDiscount>) => subResponse.body)
              )
              .subscribe(
                (subRes: IDiscount) => (this.discounts = [subRes].concat(res)),
                (subRes: HttpErrorResponse) => this.onError(subRes.message)
              );
          }
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
    this.itemService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<IItem[]>) => mayBeOk.ok),
        map((response: HttpResponse<IItem[]>) => response.body)
      )
      .subscribe((res: IItem[]) => (this.items = res), (res: HttpErrorResponse) => this.onError(res.message));
  }

  updateForm(invoiceItem: IInvoiceItem) {
    this.editForm.patchValue({
      id: invoiceItem.id,
      name: invoiceItem.name,
      description: invoiceItem.description,
      amount: invoiceItem.amount,
      quantity: invoiceItem.quantity,
      taxName: invoiceItem.taxName,
      taxRate: invoiceItem.taxRate,
      invoiceId: invoiceItem.invoiceId,
      discountId: invoiceItem.discountId,
      itemId: invoiceItem.itemId
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const invoiceItem = this.createFromForm();
    if (invoiceItem.id !== undefined) {
      this.subscribeToSaveResponse(this.invoiceItemService.update(invoiceItem));
    } else {
      this.subscribeToSaveResponse(this.invoiceItemService.create(invoiceItem));
    }
  }

  private createFromForm(): IInvoiceItem {
    return {
      ...new InvoiceItem(),
      id: this.editForm.get(['id']).value,
      name: this.editForm.get(['name']).value,
      description: this.editForm.get(['description']).value,
      amount: this.editForm.get(['amount']).value,
      quantity: this.editForm.get(['quantity']).value,
      taxName: this.editForm.get(['taxName']).value,
      taxRate: this.editForm.get(['taxRate']).value,
      invoiceId: this.editForm.get(['invoiceId']).value,
      discountId: this.editForm.get(['discountId']).value,
      itemId: this.editForm.get(['itemId']).value
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IInvoiceItem>>) {
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

  trackInvoiceById(index: number, item: IInvoice) {
    return item.id;
  }

  trackDiscountById(index: number, item: IDiscount) {
    return item.id;
  }

  trackItemById(index: number, item: IItem) {
    return item.id;
  }
}
