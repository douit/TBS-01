import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { TbsSharedModule } from 'app/shared/shared.module';
import { DiscountComponent } from './discount.component';
import { DiscountDetailComponent } from './discount-detail.component';
import { DiscountUpdateComponent } from './discount-update.component';
import { DiscountDeletePopupComponent, DiscountDeleteDialogComponent } from './discount-delete-dialog.component';
import { discountRoute, discountPopupRoute } from './discount.route';

const ENTITY_STATES = [...discountRoute, ...discountPopupRoute];

@NgModule({
  imports: [TbsSharedModule, RouterModule.forChild(ENTITY_STATES)],
  declarations: [
    DiscountComponent,
    DiscountDetailComponent,
    DiscountUpdateComponent,
    DiscountDeleteDialogComponent,
    DiscountDeletePopupComponent
  ],
  entryComponents: [DiscountComponent, DiscountUpdateComponent, DiscountDeleteDialogComponent, DiscountDeletePopupComponent]
})
export class TbsposDiscountModule {}
