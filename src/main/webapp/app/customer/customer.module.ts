import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { TbsSharedModule } from 'app/shared/shared.module';
import { CustomerComponent } from './customer.component';
import { CustomerDetailComponent } from './customer-detail.component';
import { CustomerUpdateComponent } from './customer-update.component';
import { CustomerDeletePopupComponent, CustomerDeleteDialogComponent } from './customer-delete-dialog.component';
import { customerRoute, customerPopupRoute } from './customer.route';
import {CustomerTestCcComponent} from './customer-test-cc.component';

const ENTITY_STATES = [...customerRoute, ...customerPopupRoute];

@NgModule({
  imports: [TbsSharedModule, RouterModule.forChild(ENTITY_STATES)],
  declarations: [
    CustomerComponent,
    CustomerDetailComponent,
    CustomerUpdateComponent,
    CustomerDeleteDialogComponent,
    CustomerDeletePopupComponent,
    CustomerTestCcComponent
  ],
  entryComponents: [
    CustomerComponent,
    CustomerUpdateComponent,
    CustomerDeleteDialogComponent,
    CustomerDeletePopupComponent,
    CustomerTestCcComponent]
})
export class TbsposCustomerModule {}
