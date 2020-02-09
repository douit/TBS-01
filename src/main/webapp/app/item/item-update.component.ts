import {Component, OnInit} from '@angular/core';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import {HttpErrorResponse, HttpResponse} from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import {FormBuilder, FormControl, FormGroup, FormGroupDirective, NgForm, Validators} from '@angular/forms';
import {ActivatedRoute} from '@angular/router';
import {Observable} from 'rxjs';
import {filter, map} from 'rxjs/operators';
import {JhiAlertService} from 'ng-jhipster';
import {IItem, Item} from 'app/shared/model/item.model';
import {ItemService} from './item.service';
import {ICategory} from 'app/shared/model/category.model';
import {CategoryService} from 'app/category/category.service';
import {IClient} from 'app/shared/model/client.model';
import {ClientService} from 'app/client/client.service';
import {ErrorStateMatcher} from '@angular/material/core';
import {IDropdownSettings} from 'ng-multiselect-dropdown';
import {TaxService} from 'app/item/tax.service';

export class MyErrorStateMatcher implements ErrorStateMatcher {
  isErrorState(control: FormControl | null, form: FormGroupDirective | NgForm | null): boolean {
    const isSubmitted = form && form.submitted;
    return !!(control && control.invalid && (control.dirty || control.touched || isSubmitted));
  }
}

@Component({
  selector: 'app-item-update',
  templateUrl: './item-update.component.html'
})
export class ItemUpdateComponent implements OnInit {
  isSaving: boolean;
  selectedCategory: ICategory;
  selectedClient: IClient;
  // allTaxes: ITax[];
  // selectedTaxes: ITax[] = [];

  // selectedTax: ITax;
  validSourceType = false;
  categories: ICategory[];
  type: FormGroup;
  validUrlType = false;
  validDestinationType = false;
  clients: IClient[];
  validTextType = false;
  validEmailType = false;

  validEmailRegister = false;
  validConfirmPasswordRegister = false;
  validPasswordRegister = false;
  isFlexiblePrice: boolean = true;

  validEmailLogin = false;
  validPasswordLogin = false;
  validNumberType = false;
  register: FormGroup;
  // taxesList = [];
  dropdownList = [];
  // selectedItems = [];
  dropdownSettings: IDropdownSettings = {};
  editForm = this.fb.group({
    id: [],
    name: ['', Validators.required],
    description: [],
    defaultQuantity: [],
    price:[''],
    category: ['', Validators.required],
    client: ['', Validators.required],
    taxes: [],
    code: ['', Validators.required],
    flexiblePrice: [true]

  });
  priceControl = this.editForm.get('price');
  isEmptyPrice: boolean = true;

  constructor(
    protected jhiAlertService: JhiAlertService,
    protected itemService: ItemService,
    protected taxService: TaxService,
    protected categoryService: CategoryService,
    protected clientService: ClientService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {
  }

  isFieldValid(form: FormGroup, field: string) {
    return !form.get(field).valid && form.get(field).touched;
  }

  ngOnInit() {
    // if (this.isFlexiblePrice) {
    //   this.priceControl.setValidators([Validators.required]);
    // }
    // let taxss = {item_id: 1, item_text: 'KSA Tax' }
    // this.dropdownList.push(taxss)
    this.taxService.getTaxes().subscribe(res => {
      this.dropdownList = res.body;
      /* res.body.forEach(tax => {
         this.dropdownList.push(tax
           // item_id: tax.id, item_text: tax.code }
           );
       });*/
      // this.dropdownList = this.taxesList;

    });

    this.dropdownSettings = {
      singleSelection: false,
      idField: 'id',
      textField: 'code',
      selectAllText: 'Select All',
      unSelectAllText: 'UnSelect All',
      itemsShowLimit: 3,
      allowSearchFilter: true
    };
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({item}) => {
      this.updateForm(item);
    });
    this.categoryService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<ICategory[]>) => mayBeOk.ok),
        map((response: HttpResponse<ICategory[]>) => response.body)
      )
      .subscribe((res: ICategory[]) => (this.categories = res), (res: HttpErrorResponse) => this.onError(res.message));
    /*this.clientService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<IClient[]>) => mayBeOk.ok),
        map((response: HttpResponse<IClient[]>) => response.body)
      )
      .subscribe((res: IClient[]) => (this.clients = res), (res: HttpErrorResponse) => this.onError(res.message));*/
    this.clientService.getClientByRole()
      .subscribe(
        res => {
          this.clients = res.body;
        }, res => {
          console.log('An error has occurred when get clientByRole');
        }
      );
  }

  onItemSelect(item: any) {
    // alert(item.valueOf().item_id)
    console.log(item);
    /*const that = this;
    this.taxService.getTax(item.valueOf().item_id).subscribe(res => {
      that.selectedTax = res.body;
      // this.selectedTax = {
      //   code:res.body.code,
      //   name :res.body.name,
      //   id:res.body.id
      // };

      this.selectedTaxes.push(this.selectedTax);
      this.selectedItems.push(item);
      // alert(this.selectedTaxes.length);

    });*/
  }

  onSelectAll(items: any) {
    // console.log(items);
  }

  onItemDeSelect(item: any) {

    /*for (let i = 1; i <= this.selectedItems.length; i++) {
      const tax = this.selectedItems.pop();
      this.selectedTaxes.pop();
      if (tax.valueOf().item_id != item.valueOf().item_id) {
        this.selectedItems.push(tax);
        this.taxService.getTax(item.valueOf().item_id).subscribe(res => {
          this.selectedTaxes.push(res.body);
        });
      } else {
        // alert(this.selectedTaxes.length)
      }
    }*/
  }

  updateForm(item: IItem) {
    this.selectedCategory = item.category;
    this.selectedClient = item.client;
    this.isFlexiblePrice = item.flexiblePrice;
    if(!this.isFlexiblePrice && item.id){
      this.isEmptyPrice= false;
    }

    this.editForm.patchValue({
      id: item.id,
      code: item.code,
      name: item.name,
      description: item.description,
      price: item.price,
      defaultQuantity: item.defaultQuantity,
      category: item.category,
      client: item.client,
      taxes: item.taxes,
      flexiblePrice: item.flexiblePrice
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const item = this.createFromForm();
    item.flexiblePrice = this.isFlexiblePrice;
    if (item.id !== undefined) {
      this.subscribeToSaveResponse(this.itemService.update(item));
    } else {
      this.subscribeToSaveResponse(this.itemService.create(item));
    }
  }


  private createFromForm(): IItem {
    return {
      ...new Item(),
      id: this.editForm.get(['id']).value,
      code: this.editForm.get(['code']).value,
      name: this.editForm.get(['name']).value,
      description: this.editForm.get(['description']).value,
      price: this.editForm.get(['price']).value,
      defaultQuantity: this.editForm.get(['defaultQuantity']).value,
      category: this.editForm.get(['category']).value,
      client: this.editForm.get(['client']).value,
      taxes: /*this.selectedTaxes*/this.editForm.get(['taxes']).value,
      flexiblePrice: this.editForm.get(['flexiblePrice']).value

    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IItem>>) {
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

  trackCategoryById(index: number, item: ICategory) {
    return item.id;
  }

  trackClientById(index: number, item: IClient) {
    return item.id;
  }

  sourceValidationType(e) {
    if (e) {
      this.validSourceType = true;
    } else {
      this.validSourceType = false;
    }
  }

  displayFieldCss(form: FormGroup, field: string) {
    return {
      'has-error': this.isFieldValid(form, field),
      'has-feedback': this.isFieldValid(form, field)
    };
  }

  confirmDestinationValidationType(e) {
    if (this.type.controls['password'].value === e) {
      this.validDestinationType = true;
    } else {
      this.validDestinationType = false;
    }
  }

  onType() {
    if (this.type.valid) {
    } else {
      this.validateAllFormFields(this.type);
    }
  }

  validateAllFormFields(formGroup: FormGroup) {
    Object.keys(formGroup.controls).forEach(field => {
      const control = formGroup.get(field);
      if (control instanceof FormControl) {
        control.markAsTouched({onlySelf: true});
      } else if (control instanceof FormGroup) {
        this.validateAllFormFields(control);
      }
    });
  }

  urlValidationType(e) {
    try {
      new URL(e);
      this.validUrlType = true;
    } catch (_) {
      this.validUrlType = false;
    }
  }

  textValidationType(e) {
    if (e) {
      this.validTextType = true;
    } else {
      this.validTextType = false;
    }
  }

  emailValidationRegister(e) {
    let re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    if (re.test(String(e).toLowerCase())) {
      this.validEmailRegister = true;
    } else {
      this.validEmailRegister = false;
    }
  }

  passwordValidationRegister(e) {
    if (e.length > 5) {
      this.validPasswordRegister = true;
    } else {
      this.validPasswordRegister = false;
    }
  }

  confirmPasswordValidationRegister(e) {
    if (this.register.controls['password'].value === e) {
      this.validConfirmPasswordRegister = true;
    } else {
      this.validConfirmPasswordRegister = false;
    }
  }

  emailValidationLogin(e) {
    let re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    if (re.test(String(e).toLowerCase())) {
      this.validEmailLogin = true;
    } else {
      this.validEmailLogin = false;
    }
  }

  passwordValidationLogin(e) {
    if (e.length > 5) {
      this.validPasswordLogin = true;
    } else {
      this.validPasswordLogin = false;
    }
  }


  emailValidationType(e) {
    let re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    if (re.test(String(e).toLowerCase())) {
      this.validEmailType = true;
    } else {
      this.validEmailType = false;
    }
  }

  numberValidationType(e) {
    if (e) {
      this.validNumberType = true;
    } else {
      this.validNumberType = false;
    }
  }

  compareObjects(selectClient: IClient, client: IClient): boolean {
    return selectClient.name === client.name && selectClient.id === client.id;
  }

  FlexiblePriceCheck(): boolean {
    if (this.isFlexiblePrice) {
      return this.isFlexiblePrice = false;
    } else {
      return this.isFlexiblePrice = true;
    }


  }

  paddingPrice() {

    if (this.editForm.get(['price']).value != 0 &&
      this.editForm.get(['price']).value.toString() != "")
      this.isEmptyPrice = false
    else
      this.isEmptyPrice= true


  }
}
