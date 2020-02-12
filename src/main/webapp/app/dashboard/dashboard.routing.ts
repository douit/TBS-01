import { Routes } from '@angular/router';

import { DashboardComponent } from './dashboard.component';
import {UserRouteAccessService} from 'app/core/auth/user-route-access-service';

export const DashboardRoutes: Routes = [
    {

      path: '',
      children: [ {
        path: 'dashboard',
        component: DashboardComponent,
        data: {
          authorities: ['ROLE_USER'],
          pageTitle: 'dashboard.title'
        },
        canActivate: [UserRouteAccessService]
    }]
}
];
