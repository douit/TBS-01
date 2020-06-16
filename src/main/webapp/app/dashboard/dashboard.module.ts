import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MdModule } from '../md/md.module';
import { MaterialModule } from '../app.module';

import { DashboardComponent } from './dashboard.component';
import { DashboardRoutes } from './dashboard.routing';
// import {NgbDatepickerModule} from "@ng-bootstrap/ng-bootstrap";
import {Daterangepicker} from "ng2-daterangepicker";

@NgModule({
    imports: [
        CommonModule,
        RouterModule.forChild(DashboardRoutes),
        FormsModule,
        MdModule,
        MaterialModule,
        Daterangepicker
    ],
    declarations: [DashboardComponent]
})

export class DashboardModule {}
