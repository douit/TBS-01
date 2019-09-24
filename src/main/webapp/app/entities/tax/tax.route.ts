import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { Observable, of } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { Tax } from 'app/shared/model/tax.model';
import { TaxService } from './tax.service';
import { TaxComponent } from './tax.component';
import { TaxDetailComponent } from './tax-detail.component';
import { TaxUpdateComponent } from './tax-update.component';
import { TaxDeletePopupComponent } from './tax-delete-dialog.component';
import { ITax } from 'app/shared/model/tax.model';

@Injectable({ providedIn: 'root' })
export class TaxResolve implements Resolve<ITax> {
  constructor(private service: TaxService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<ITax> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        filter((response: HttpResponse<Tax>) => response.ok),
        map((tax: HttpResponse<Tax>) => tax.body)
      );
    }
    return of(new Tax());
  }
}

export const taxRoute: Routes = [
  {
    path: '',
    component: TaxComponent,
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'tbsApp.tax.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/view',
    component: TaxDetailComponent,
    resolve: {
      tax: TaxResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'tbsApp.tax.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'new',
    component: TaxUpdateComponent,
    resolve: {
      tax: TaxResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'tbsApp.tax.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/edit',
    component: TaxUpdateComponent,
    resolve: {
      tax: TaxResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'tbsApp.tax.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const taxPopupRoute: Routes = [
  {
    path: ':id/delete',
    component: TaxDeletePopupComponent,
    resolve: {
      tax: TaxResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'tbsApp.tax.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
