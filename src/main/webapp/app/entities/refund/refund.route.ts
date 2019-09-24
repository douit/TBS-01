import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { Observable, of } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { Refund } from 'app/shared/model/refund.model';
import { RefundService } from './refund.service';
import { RefundComponent } from './refund.component';
import { RefundDetailComponent } from './refund-detail.component';
import { RefundUpdateComponent } from './refund-update.component';
import { RefundDeletePopupComponent } from './refund-delete-dialog.component';
import { IRefund } from 'app/shared/model/refund.model';

@Injectable({ providedIn: 'root' })
export class RefundResolve implements Resolve<IRefund> {
  constructor(private service: RefundService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<IRefund> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        filter((response: HttpResponse<Refund>) => response.ok),
        map((refund: HttpResponse<Refund>) => refund.body)
      );
    }
    return of(new Refund());
  }
}

export const refundRoute: Routes = [
  {
    path: '',
    component: RefundComponent,
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'tbsApp.refund.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/view',
    component: RefundDetailComponent,
    resolve: {
      refund: RefundResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'tbsApp.refund.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'new',
    component: RefundUpdateComponent,
    resolve: {
      refund: RefundResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'tbsApp.refund.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/edit',
    component: RefundUpdateComponent,
    resolve: {
      refund: RefundResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'tbsApp.refund.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const refundPopupRoute: Routes = [
  {
    path: ':id/delete',
    component: RefundDeletePopupComponent,
    resolve: {
      refund: RefundResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'tbsApp.refund.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
