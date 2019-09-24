import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { TbsSharedModule } from 'app/shared/shared.module';
import { RefundComponent } from './refund.component';
import { RefundDetailComponent } from './refund-detail.component';
import { RefundUpdateComponent } from './refund-update.component';
import { RefundDeletePopupComponent, RefundDeleteDialogComponent } from './refund-delete-dialog.component';
import { refundRoute, refundPopupRoute } from './refund.route';

const ENTITY_STATES = [...refundRoute, ...refundPopupRoute];

@NgModule({
  imports: [TbsSharedModule, RouterModule.forChild(ENTITY_STATES)],
  declarations: [RefundComponent, RefundDetailComponent, RefundUpdateComponent, RefundDeleteDialogComponent, RefundDeletePopupComponent],
  entryComponents: [RefundComponent, RefundUpdateComponent, RefundDeleteDialogComponent, RefundDeletePopupComponent]
})
export class TbsRefundModule {}
