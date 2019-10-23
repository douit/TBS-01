import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
// import { LbdTableComponent } from '../lbd/lbd-table/lbd-table.component';
import { TbsSharedModule } from '../shared/shared.module';

import { TbsLandingComponent } from './tbs-landing.component';
import { TbsLandingRoutes } from './tbs-landing.routing';

@NgModule({
    imports: [
        TbsSharedModule,
        CommonModule,
        RouterModule.forChild(TbsLandingRoutes),
        FormsModule
    ],
    declarations: [TbsLandingComponent]
})

export class TbsLandingModule {}
