import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { TbsSharedModule } from 'app/shared/shared.module';
import {MaterialModule} from 'app/app.module';
import {NgMultiSelectDropDownModule} from 'ng-multiselect-dropdown';

import { adminState } from './admin.route';
import { AuditsComponent } from './audits/audits.component';
import { UserMgmtComponent } from './user-management/user-management.component';
import { UserMgmtDetailComponent } from './user-management/user-management-detail.component';
import { UserMgmtUpdateComponent } from './user-management/user-management-update.component';
import { UserMgmtDeleteDialogComponent } from './user-management/user-management-delete-dialog.component';
import { LogsComponent } from './logs/logs.component';
import { JhiMetricsMonitoringComponent } from './metrics/metrics.component';
import { JhiHealthModalComponent } from './health/health-modal.component';
import { JhiHealthCheckComponent } from './health/health.component';
import { JhiConfigurationComponent } from './configuration/configuration.component';
import { JhiDocsComponent } from './docs/docs.component';

@NgModule({
  imports: [
    TbsSharedModule,
    RouterModule.forChild(adminState),
    MaterialModule,
    NgMultiSelectDropDownModule
  ],
  declarations: [
    AuditsComponent,
    UserMgmtComponent,
    UserMgmtDetailComponent,
    UserMgmtUpdateComponent,
    UserMgmtDeleteDialogComponent,
    LogsComponent,
    JhiConfigurationComponent,
    JhiHealthCheckComponent,
    JhiHealthModalComponent,
    JhiDocsComponent,
    JhiMetricsMonitoringComponent
  ],
  entryComponents: [UserMgmtDeleteDialogComponent, JhiHealthModalComponent]
})
export class TbsAdminModule {}
