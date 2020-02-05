import {NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';

import {TbsSharedModule} from 'app/shared/shared.module';
import {PasswordStrengthBarComponent} from './password/password-strength-bar.component';
import {PasswordComponent} from './password/password.component';
import {PasswordResetFinishComponent} from './password-reset/finish/password-reset-finish.component';
import {accountState} from './account.route';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';

@NgModule({
  imports: [TbsSharedModule, CommonModule, RouterModule.forChild(accountState), FormsModule],
  declarations: [
    // ActivateComponent,
    // RegisterComponent,
    PasswordComponent,
    PasswordStrengthBarComponent,
    // PasswordResetInitComponent,
    PasswordResetFinishComponent,
    // SessionsComponent,
    // SettingsComponent
  ]
})
export class TbsAccountModule {}
