import {Component, OnInit, OnDestroy, ViewChild} from '@angular/core';
import {HttpErrorResponse, HttpResponse} from '@angular/common/http';
import { Subscription } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import { ActivatedRoute, Router } from '@angular/router';
import { JhiEventManager, JhiParseLinks, JhiAlertService } from 'ng-jhipster';

import { ITEMS_PER_PAGE } from 'app/shared/constants/pagination.constants';
import { AccountService } from 'app/core/auth/account.service';
import { UserService } from 'app/core/user/user.service';
import {IUser, User} from 'app/core/user/user.model';
import { UserMgmtDeleteDialogComponent } from './user-management-delete-dialog.component';
import {PageQueryParams} from 'app/shared/model/page-query-params';
import {DatatableColumn} from 'app/shared/model/datatable/datatable-column';
import {TranslateService} from '@ngx-translate/core';
import {Datatable} from 'app/shared/model/datatable/datatable';
import {DatatableComponent} from '@swimlane/ngx-datatable';
import {finalize} from 'rxjs/operators';
import {NotificationComponent} from 'app/shared/notification/notification.component';

@Component({
  selector: 'app-user-mgmt',
  templateUrl: './user-management.component.html'
})
export class UserMgmtComponent implements OnInit, OnDestroy {
  currentAccount: any;
  users: User[];
  error: any;
  success: any;
  userListSubscription: Subscription;
  routeData: Subscription;
  links: any;
  totalItems: any;
  itemsPerPage: any;
  page: any;
  predicate: any;
  previousPage: any;
  reverse: any;

  // new datatable
  busy = false;
  datatable = new Datatable<IUser>();
  // Datatable Reference
  @ViewChild('table', {static: true}) table: DatatableComponent;

  // Datatable Templates Reference
  @ViewChild('headerTemplate', {static: false}) headerTemplate;
  @ViewChild('rowTemplate', {static: false}) rowTemplate;
  @ViewChild('actionsRowTemplate', {static: true}) actionsRowTemplate;
  @ViewChild('editRowTemplate', {static: true}) editRowTemplate;

  @ViewChild('userActivationTemplate', {static: true}) userActivationTemplate;
  @ViewChild('modifiedDateTemplate', {static: true}) modifiedDateTemplate;
  @ViewChild('createdDateTemplate', {static: true}) createdDateTemplate;

  constructor(
    private userService: UserService,
    private alertService: JhiAlertService,
    private accountService: AccountService,
    private parseLinks: JhiParseLinks,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private eventManager: JhiEventManager,
    private modalService: NgbModal,
    private translateService: TranslateService,
    private notification: NotificationComponent,
  ) {
    this.itemsPerPage = ITEMS_PER_PAGE;
    this.routeData = this.activatedRoute.data.subscribe(data => {
      this.page = data.pagingParams.page;
      this.previousPage = data.pagingParams.page;
      this.reverse = data.pagingParams.ascending;
      this.predicate = data.pagingParams.predicate;
    });
  }

  ngOnInit() {

    this.accountService.identity().then(account => {
      this.currentAccount = account;
      // this.loadAll();
      this.activatedRoute.queryParams
        .subscribe((pageQueryParams: PageQueryParams) => {
          this.datatable.fillPageQueryParams(pageQueryParams);
          this.loadData();
        });
      this.registerChangeInUsers();
    });

    this.initDatatable();
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
        name: this.translateService.instant('userManagement.login'),
        prop: 'login',
        headerTemplate: this.headerTemplate,
        cellTemplate: this.rowTemplate
      }),
      new DatatableColumn({
        name: this.translateService.instant('userManagement.email'),
        prop: 'email',
        headerTemplate: this.headerTemplate,
        cellTemplate: this.rowTemplate
      }),
      new DatatableColumn({
        // name: this.translateService.instant('userManagement.email'),
        sortable: false,
        searchable: false,
        headerTemplate: this.headerTemplate,
        cellTemplate: this.userActivationTemplate
      }),
      new DatatableColumn({
        name: this.translateService.instant('userManagement.langKey'),
        prop: 'langKey',
        headerTemplate: this.headerTemplate,
        cellTemplate: this.rowTemplate
      }),
      new DatatableColumn({
        name: this.translateService.instant('userManagement.createdDate'),
        prop: 'createdDate',
        headerTemplate: this.headerTemplate,
        cellTemplate: this.createdDateTemplate
      }),
      new DatatableColumn({
        name: this.translateService.instant('userManagement.lastModifiedBy'),
        prop: 'lastModifiedBy',
        headerTemplate: this.headerTemplate,
        cellTemplate: this.rowTemplate
      }),
      new DatatableColumn({
        name: this.translateService.instant('userManagement.lastModifiedDate'),
        prop: 'lastModifiedDate',
        headerTemplate: this.headerTemplate,
        cellTemplate: this.modifiedDateTemplate
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
    this.userService.getList(this.datatable.getDataTableInput())
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

  ngOnDestroy() {
    this.routeData.unsubscribe();
    if (this.userListSubscription) {
      this.eventManager.destroy(this.userListSubscription);
    }
  }

  registerChangeInUsers() {
    this.userListSubscription = this.eventManager.subscribe('userListModification', response => this.loadData());
  }

  setActive(user, isActivated) {
    user.activated = isActivated;

    this.userService.update(user).subscribe(response => {
      if (response.status === 200) {
        this.error = null;
        this.success = 'OK';
        // this.notification.showNotification('success', 'unable to read user information');
        this.loadData();
      } else {
        this.success = null;
        this.error = 'ERROR';
        // this.notification.showNotification('danger', 'unable to read user information');
      }
    });
  }

  filter(reset: boolean) {
    if (reset) {
      this.datatable.resetPageNumber();
    }

    const pageQueryParams = new PageQueryParams();

    pageQueryParams.fillDatatable(this.datatable);

    this.router.navigate(['/admin/user-management'], {queryParams: pageQueryParams});
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

  deleteUser(user: User) {
    const modalRef = this.modalService.open(UserMgmtDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.user = user;
    modalRef.result.then(
      result => {
        // Left blank intentionally, nothing to do here
      },
      reason => {
        // Left blank intentionally, nothing to do here
      }
    );
  }

  private onSuccess(data, headers) {
    this.links = this.parseLinks.parse(headers.get('link'));
    this.totalItems = headers.get('X-Total-Count');
    this.users = data;
  }

  private onError(error) {
    this.alertService.error(error.error, error.message, null);
  }
}
