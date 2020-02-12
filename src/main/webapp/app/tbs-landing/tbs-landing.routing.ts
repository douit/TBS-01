import { Routes } from '@angular/router';

import { TbsLandingComponent } from './tbs-landing.component';

export const TbsLandingRoutes: Routes = [
    {

      path: 'login',
      children: [ {
        path: '',
        component: TbsLandingComponent,
        data: {
          authorities: [],
          pageTitle: 'global.menu.account.login'
        },
    }]
}
];
