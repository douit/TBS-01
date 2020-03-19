import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
// import { LbdTableComponent } from '../lbd/lbd-table/lbd-table.component';
import { TbsSharedModule } from '../shared/shared.module';

import { TbsLandingComponent } from './tbs-landing.component';
import { TbsLandingRoutes } from './tbs-landing.routing';
import { RecaptchaModule, RECAPTCHA_SETTINGS, RecaptchaSettings } from 'ng-recaptcha';
import { RecaptchaFormsModule } from 'ng-recaptcha/forms';

@NgModule({
    imports: [
        TbsSharedModule,
        CommonModule,
        RouterModule.forChild(TbsLandingRoutes),
        FormsModule,
        RecaptchaModule,
        RecaptchaFormsModule
    ],
    declarations: [TbsLandingComponent],
    providers: [{
      provide: RECAPTCHA_SETTINGS,
      useValue: {
        siteKey: '6LeDUuIUAAAAAENY5IkCvpdOQ0KQgubF8DHbBzOp',
      } as RecaptchaSettings,
    }]
})

export class TbsLandingModule {}
