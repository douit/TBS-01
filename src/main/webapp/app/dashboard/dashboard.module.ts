import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MdModule } from '../md/md.module';
import { MaterialModule } from '../app.module';

import { DashboardComponent } from './dashboard.component';
import { DashboardRoutes } from './dashboard.routing';
import {NgbDatepickerModule} from "@ng-bootstrap/ng-bootstrap";
import {Daterangepicker} from "ng2-daterangepicker";
import {TranslateModule} from "@ngx-translate/core";
import {TbsSharedModule} from "app/shared/shared.module";

@NgModule({
    imports: [
        CommonModule,
        RouterModule.forChild(DashboardRoutes),
        FormsModule,
        MdModule,
        MaterialModule,
        NgbDatepickerModule,
        Daterangepicker,
        TranslateModule,
      TbsSharedModule
    ],
    declarations: [DashboardComponent]
})

export class DashboardModule {}
