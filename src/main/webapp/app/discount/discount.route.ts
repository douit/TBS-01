import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { Observable, of } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { Discount } from 'app/shared/model/discount.model';
import { DiscountService } from './discount.service';
import { DiscountComponent } from './discount.component';
import { DiscountDetailComponent } from './discount-detail.component';
import { DiscountUpdateComponent } from './discount-update.component';
import { DiscountDeletePopupComponent } from './discount-delete-dialog.component';
import { IDiscount } from 'app/shared/model/discount.model';

@Injectable({ providedIn: 'root' })
export class DiscountResolve implements Resolve<IDiscount> {
  constructor(private service: DiscountService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<IDiscount> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        filter((response: HttpResponse<Discount>) => response.ok),
        map((discount: HttpResponse<Discount>) => discount.body)
      );
    }
    return of(new Discount());
  }
}

export const discountRoute: Routes = [
  {
    path: '',
    component: DiscountComponent,
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'tbsApp.discount.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/view',
    component: DiscountDetailComponent,
    resolve: {
      discount: DiscountResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'tbsApp.discount.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'new',
    component: DiscountUpdateComponent,
    resolve: {
      discount: DiscountResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'tbsApp.discount.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/edit',
    component: DiscountUpdateComponent,
    resolve: {
      discount: DiscountResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'tbsApp.discount.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const discountPopupRoute: Routes = [
  {
    path: ':id/delete',
    component: DiscountDeletePopupComponent,
    resolve: {
      discount: DiscountResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'tbsApp.discount.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
