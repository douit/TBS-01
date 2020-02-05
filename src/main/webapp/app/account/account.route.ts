import {Routes} from '@angular/router';
import {passwordRoute} from './password/password.route';
import {passwordResetFinishRoute} from './password-reset/finish/password-reset-finish.route';
import {PasswordResetFinishComponent} from 'app/account/password-reset/finish/password-reset-finish.component';

const ACCOUNT_ROUTES = [
  // activateRoute,
  passwordRoute,
  passwordResetFinishRoute,
  // passwordResetInitRoute,
  // registerRoute,
  // sessionsRoute,
  // settingsRoute
];

export const accountState: Routes = [
  /*{
    path: '',
    children: ACCOUNT_ROUTES
  }*/
  {

    path: '',
    children: [ {
      path: 'reset/finish',
      component: PasswordResetFinishComponent
    }]
  }
];
