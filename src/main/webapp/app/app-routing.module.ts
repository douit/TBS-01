import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { errorRoute } from './layouts/error/error.route';
// import { navbarRoute } from './layouts/navbar/navbar.route';
import { DEBUG_INFO_ENABLED } from 'app/app.constants';
import {UserRouteAccessService} from "./core/auth/user-route-access-service";
import {TbsLandingComponent} from "./tbs-landing/tbs-landing.component";
import {NotPermittedComponent} from "./shared/not-permitted/not-permitted.component";
import {AdminLayoutComponent} from './layouts/admin/admin-layout.component';
import { Routes } from '@angular/router';


export const AppRoutes: Routes = [
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
            }, {
              path: 'components',
              //loadChildren: './components/components.module#ComponentsModule'
              loadChildren: () => import('./components/components.module').then(m => m.ComponentsModule)
            }/*, {
              path: 'forms',
              // loadChildren: './forms/forms.module#Forms'
              loadChildren: () => import('./forms/forms.module').then(m => m.Forms)
            }, {
              path: 'tables',
              // loadChildren: './tables/tables.module#TablesModule'
              loadChildren: () => import('./tables/tables.module').then(m => m.TablesModule)
            }, {
              path: 'maps',
              // loadChildren: './maps/maps.module#MapsModule'
              loadChildren: () => import('./maps/maps.module').then(m => m.MapsModule)
            }, {
              path: 'widgets',
              // loadChildren: './widgets/widgets.module#WidgetsModule'
              loadChildren: () => import('./widgets/widgets.module').then(m => m.WidgetsModule)
            }, {
              path: 'charts',
              // loadChildren: './charts/charts.module#ChartsModule'
              loadChildren: () => import('./charts/charts.module').then(m => m.ChartsModule)
            }, {
              path: 'calendar',
              // loadChildren: './calendar/calendar.module#CalendarModule'
              loadChildren: () => import('./calendar/calendar.module').then(m => m.CalendarModule)
            }, {
              path: '',
              // loadChildren: './userpage/user.module#UserModule'
              loadChildren: () => import('./userpage/user.module').then(m => m.UserModule)
            }, {
              path: '',
              // loadChildren: './timeline/timeline.module#TimelineModule'
              loadChildren: () => import('./timeline/timeline.module').then(m => m.TimelineModule)
            }*/
          ]
        }/*,
        {
          path: 'admin',
          loadChildren: () => import('./admin/admin.module').then(m => m.TbsAdminModule),
          canActivate: [UserRouteAccessService],
        },
        {
          path: 'account',
          loadChildren: () => import('./account/account.module').then(m => m.TbsAccountModule)
        }*/
      ];
