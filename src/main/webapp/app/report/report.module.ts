import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule} from '@angular/router';
import {NouisliderModule} from 'ng2-nouislider';
import {MaterialModule} from '../app.module';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {TagInputModule} from 'ngx-chips';
import {AgmCoreModule} from '@agm/core';
import {NgMultiSelectDropDownModule} from 'ng-multiselect-dropdown';
import {Daterangepicker} from 'ng2-daterangepicker';
import {reportRoutes} from './report.route';
import {PaymentReportComponent} from './payment-report/payment-report.component';
import {TbsSharedModule} from 'app/shared/shared.module';

@NgModule({
    imports: [
        TbsSharedModule,
        CommonModule,
        RouterModule.forChild(reportRoutes),
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        NouisliderModule,
        TagInputModule,
        MaterialModule,
        NgMultiSelectDropDownModule,
        Daterangepicker,
        AgmCoreModule.forRoot()
    ],
    declarations: [PaymentReportComponent]
})
export class ReportModule {
}

