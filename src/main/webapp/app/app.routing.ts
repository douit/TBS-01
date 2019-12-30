import { Routes } from '@angular/router';

import { AdminLayoutComponent } from './layouts/admin/admin-layout.component';
import { AuthLayoutComponent } from './layouts/auth/auth-layout.component';
import {TbsLandingComponent} from './tbs-landing/tbs-landing.component';
import {UserRouteAccessService} from './core/auth/user-route-access-service';

export const AppRoutes: Routes = [
    {
      path: '',
      redirectTo: 'dashboard',
      pathMatch: 'full',
    }, {
    path: '',
    component: AuthLayoutComponent,
    children: [{
      path: '',
      loadChildren: () => import('./tbs-landing/tbs-landing.module').then(m => m.TbsLandingModule)
      // loadChildren: './tbs-landing/tbs-landing.module#TbsLandingModule'
    }]
    }, {
      path: 'item',
      component: AdminLayoutComponent,
      canActivate: [UserRouteAccessService],
      loadChildren: () => import('./item/item.module').then(m => m.TbsposItemModule)
    },
    {
      path: 'invoice',
      component: AdminLayoutComponent,
      canActivate: [UserRouteAccessService],
      loadChildren: () => import('./invoice/invoice.module').then(m => m.TbsposInvoiceModule)
    },
    {
      path: 'payment',
      component: AdminLayoutComponent,
      canActivate: [UserRouteAccessService],
      loadChildren: () => import('./payment/payment.module').then(m => m.TbsposPaymentModule)
    },
    {
      path: 'customer',
      component: AdminLayoutComponent,
      canActivate: [UserRouteAccessService],
      loadChildren: () => import('./customer/customer.module').then(m => m.TbsposCustomerModule)
    }, {
      path: '',
      component: AdminLayoutComponent,
      canActivate: [UserRouteAccessService],
      children: [
          {
        path: '',
        // loadChildren: './dashboard/dashboard.module#DashboardModule'
        loadChildren: () => import('./dashboard/dashboard.module').then(m => m.DashboardModule)
    }, {
        path: 'components',
        // loadChildren: './components/components.module#ComponentsModule'
          loadChildren: () => import('./components/components.module').then(m => m.ComponentsModule)
    }, {
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
    }
  ]}, {
      path: '',
      component: AuthLayoutComponent,
      children: [{
        path: 'pages',
        // loadChildren: './pages/pages.module#PagesModule'
        loadChildren: () => import('./pages/pages.module').then(m => m.PagesModule)
      }]
    },
    {
      path: 'admin',
      component: AdminLayoutComponent,
      // loadChildren: () => import('./admin/admin.module').then(m => m.TbsAdminModule),
      loadChildren: () => import('./admin/admin.module').then(m => m.TbsAdminModule),
      canActivate: [UserRouteAccessService]
    },
    {
      path: 'account',
      component: AuthLayoutComponent,
      // loadChildren: () => import('./account/account.module').then(m => m.TbsAccountModule)
      loadChildren: () => import('./account/account.module').then(m => m.TbsAccountModule)
    }
];
