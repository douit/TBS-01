import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { errorRoute } from './layouts/error/error.route';
// import { navbarRoute } from './layouts/navbar/navbar.route';
import { DEBUG_INFO_ENABLED } from 'app/app.constants';
import {UserRouteAccessService} from "./core/auth/user-route-access-service";
import {TbsLandingComponent} from "./tbs-landing/tbs-landing.component";
import {NotPermittedComponent} from "./shared/not-permitted/not-permitted.component";
import {AdminLayoutComponent} from './layouts/admin/admin-layout.component';

const LAYOUT_ROUTES = [ ...errorRoute];

@NgModule({
  imports: [
    RouterModule.forRoot(
      [
        {
          path: '',
          redirectTo: 'dashboard',
          pathMatch: 'full',
        },
        {
        path: "home",
        component: TbsLandingComponent
        },
        {
          path: '',
          component: AdminLayoutComponent,
          canActivate: [UserRouteAccessService],
          children: [
            {
              path: '',
              //loadChildren: './dashboard/dashboard.module#DashboardModule'
              loadChildren: () => import('./dashboard/dashboard.module').then(m => m.DashboardModule)
            }
          ]
        },
        {
          path: 'admin',
          loadChildren: () => import('./admin/admin.module').then(m => m.TbsAdminModule),
          canActivate: [UserRouteAccessService],
        },
        {
          path: 'account',
          loadChildren: () => import('./account/account.module').then(m => m.TbsAccountModule)
        },
        ...LAYOUT_ROUTES
      ],
      { enableTracing: DEBUG_INFO_ENABLED }
    )
  ],
  exports: [RouterModule]
})
export class TbsAppRoutingModule {}
