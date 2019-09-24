import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { TbsSharedModule } from 'app/shared/shared.module';
import { TaxComponent } from './tax.component';
import { TaxDetailComponent } from './tax-detail.component';
import { TaxUpdateComponent } from './tax-update.component';
import { TaxDeletePopupComponent, TaxDeleteDialogComponent } from './tax-delete-dialog.component';
import { taxRoute, taxPopupRoute } from './tax.route';

const ENTITY_STATES = [...taxRoute, ...taxPopupRoute];

@NgModule({
  imports: [TbsSharedModule, RouterModule.forChild(ENTITY_STATES)],
  declarations: [TaxComponent, TaxDetailComponent, TaxUpdateComponent, TaxDeleteDialogComponent, TaxDeletePopupComponent],
  entryComponents: [TaxComponent, TaxUpdateComponent, TaxDeleteDialogComponent, TaxDeletePopupComponent]
})
export class TbsTaxModule {}
