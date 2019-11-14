import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { TbsSharedModule } from 'app/shared/shared.module';
import { PaymentComponent } from './payment.component';
import { PaymentDetailComponent } from './payment-detail.component';
import { PaymentUpdateComponent } from './payment-update.component';
import { PaymentDeletePopupComponent, PaymentDeleteDialogComponent } from './payment-delete-dialog.component';
import { paymentRoute, paymentPopupRoute } from './payment.route';
import {PaymentTestCcComponent} from './payment-test-cccomponent';

const ENTITY_STATES = [...paymentRoute, ...paymentPopupRoute];

@NgModule({
  imports: [TbsSharedModule, RouterModule.forChild(ENTITY_STATES)],
  declarations: [
    PaymentComponent,
    PaymentDetailComponent,
    PaymentUpdateComponent,
    PaymentDeleteDialogComponent,
    PaymentDeletePopupComponent,
    PaymentTestCcComponent
  ],
  entryComponents: [
    PaymentComponent,
    PaymentUpdateComponent,
    PaymentDeleteDialogComponent,
    PaymentDeletePopupComponent,
    PaymentTestCcComponent
  ]
})
export class TbsposPaymentModule {}
