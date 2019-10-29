import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { filter, map } from 'rxjs/operators';
import {JhiEventManager, JhiAlertService, JhiTranslateDirective} from 'ng-jhipster';

import { IItem } from 'app/shared/model/item.model';
import { AccountService } from 'app/core/auth/account.service';
import { ItemService } from './item.service';
import { DatatableComponent } from '@swimlane/ngx-datatable';
import { Datatable } from '../shared/model/datatable/datatable';
import {ActivatedRoute, Router} from '@angular/router';
import { DatatableColumn } from 'app/shared/model/datatable/datatable-column';
import { PageQueryParams } from 'app/shared/model/page-query-params';
import {Pageable} from 'app/shared/model/pageable';
import {finalize } from 'rxjs/operators';
import {DataTableInput} from "app/shared/model/datatable/datatable-input";
import {DatatableSort} from "app/shared/model/datatable/datatable-sort";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'jhi-item',
  templateUrl: './item.component.html'
})
export class ItemComponent implements OnInit {
  /*items: IItem[];
  currentAccount: any;*/
  eventSubscriber: Subscription;

  // new
  busy = false;
  datatable = new Datatable<IItem>();
  // Datatable Reference
  @ViewChild('table', {static: true}) table: DatatableComponent;

  // Datatable Templates Reference
  @ViewChild('headerTemplate', {static: false}) headerTemplate;
  @ViewChild('rowTemplate', {static: false}) rowTemplate;
  @ViewChild('actionsRowTemplate', {static: true}) actionsRowTemplate;

  constructor(
    protected itemService: ItemService,
    protected jhiAlertService: JhiAlertService,
    protected eventManager: JhiEventManager,
    protected accountService: AccountService,
    private activatedRoute: ActivatedRoute,
    private translateService: TranslateService,
    private router: Router
  ) {}


  ngOnInit() {
    this.initDatatable();

    this.activatedRoute.queryParams
      .subscribe((pageQueryParams: PageQueryParams) => {
        this.datatable.fillPageQueryParams(pageQueryParams);

        this.loadData();
      });
  }

  initDatatable() {
    this.datatable.setTable(this.table);
    this.datatable.setColumns([
      new DatatableColumn({
        name: this.translateService.instant('global.datatable.id'),
        prop: 'id',
        headerTemplate: this.headerTemplate,
        cellTemplate: this.rowTemplate,
      }),
      new DatatableColumn({
        name: this.translateService.instant('global.datatable.name'),
        prop: 'name',
        headerTemplate: this.headerTemplate,
        cellTemplate: this.rowTemplate
      }),
      new DatatableColumn({
        name: this.translateService.instant('tbsApp.item.price'),
        prop: 'price',
        headerTemplate: this.headerTemplate,
        cellTemplate: this.rowTemplate
      }),
      new DatatableColumn({
        name: this.translateService.instant('global.datatable.actions'),
        sortable: false,
        searchable: false,
        headerTemplate: this.headerTemplate,
        cellTemplate: this.actionsRowTemplate
      })
    ]);
  }

  loadData() {
    this.busy = true;
    this.itemService.getList(this.datatable.getDataTableInput())
      /*.finally(() => this.busy = false)
      .subscribe((response) => {
        this.datatable.update(response);
      });*/
      .pipe(
        finalize(() => this.busy = false)
      )
      .subscribe(
        (res) => {
          this.datatable.update(res);
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  filter(reset: boolean) {
    if (reset) {
      this.datatable.resetPageNumber();
    }

    const pageQueryParams = new PageQueryParams();

    pageQueryParams.fillDatatable(this.datatable);

    this.router.navigate(['/item'], { queryParams: pageQueryParams });
  }

  search() {
    this.filter(true);
  }

  clearSearch() {
    this.datatable.search = '';

    this.search();
  }

  onPageChanged(data) {
    this.datatable.changePageNumber(data.offset);

    this.filter(false);
  }

  onPageSizeChanged() {
    this.filter(true);
  }

  onSortChanged(data) {
    this.datatable.setSort(data.sorts[0]);

    this.filter(true);
  }

  /*loadAll() {
    this.itemService
      .query()
      .pipe(
        filter((res: HttpResponse<IItem[]>) => res.ok),
        map((res: HttpResponse<IItem[]>) => res.body)
      )
      .subscribe(
        (res: IItem[]) => {
          this.items = res;
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  ngOnInit() {
    this.loadAll();
    this.accountService.identity().then(account => {
      this.currentAccount = account;
    });
    this.registerChangeInItems();
  }
*/
/*  ngOnDestroy() {
    this.eventManager.destroy(this.eventSubscriber);
  }*/

/*  trackId(index: number, item: IItem) {
    return item.id;
  }

  registerChangeInItems() {
    this.eventSubscriber = this.eventManager.subscribe('itemListModification', response => this.loadAll());
  }*/

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }

  edit(row:any) {
    // [routerLink]="['/item', item.id, 'edit']"
    this.router.navigate(['item/' + row.id + '/edit']);
  }

  view(row:any) {
    // [routerLink]="['/item', item.id, 'edit']"
    this.router.navigate(['item/' + row.id + '/view']);
  }
}
