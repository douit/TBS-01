import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { Observable, of } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { Customer } from 'app/shared/model/customer.model';
import { CustomerService } from './customer.service';
import { CustomerComponent } from './customer.component';
import { CustomerDetailComponent } from './customer-detail.component';
import { CustomerUpdateComponent } from './customer-update.component';
import { CustomerDeletePopupComponent } from './customer-delete-dialog.component';
import { ICustomer } from 'app/shared/model/customer.model';
import {PaymentResolve} from "app/payment/payment.route";
import {CustomerTestCcComponent} from "app/customer/customer-test-cc.component";
import {CustomerTestPayfortComponent} from 'app/customer/customer-test-payfort.component';

@Injectable({ providedIn: 'root' })
export class CustomerResolve implements Resolve<ICustomer> {
  constructor(private service: CustomerService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<ICustomer> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        filter((response: HttpResponse<Customer>) => response.ok),
        map((customer: HttpResponse<Customer>) => customer.body)
      );
    }
    return of(new Customer());
  }
}

export const customerRoute: Routes = [
  {
    path: '',
    component: CustomerComponent,
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'tbsApp.customer.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/view',
    component: CustomerDetailComponent,
    resolve: {
      customer: CustomerResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'tbsApp.customer.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'new',
    component: CustomerUpdateComponent,
    resolve: {
      customer: CustomerResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'tbsApp.customer.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/edit',
    component: CustomerUpdateComponent,
    resolve: {
      customer: CustomerResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'tbsApp.customer.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'test-cc',
    component: CustomerTestCcComponent,
    resolve: {
      payment: PaymentResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'tbsApp.payment.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'test-payfort',
    component: CustomerTestPayfortComponent,
    resolve: {
      payment: PaymentResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'tbsApp.payment.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const customerPopupRoute: Routes = [
  {
    path: ':id/delete',
    component: CustomerDeletePopupComponent,
    resolve: {
      customer: CustomerResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'tbsApp.customer.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
