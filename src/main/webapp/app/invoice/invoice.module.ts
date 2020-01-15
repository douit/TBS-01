import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { TbsSharedModule } from 'app/shared/shared.module';
import { InvoiceComponent } from './invoice.component';
import { InvoiceDetailComponent } from './invoice-detail.component';
import { InvoiceUpdateComponent } from './invoice-update.component';
import { InvoiceDeletePopupComponent, InvoiceDeleteDialogComponent } from './invoice-delete-dialog.component';
import { invoiceRoute, invoicePopupRoute } from './invoice.route';
import {MaterialModule} from 'app/app.module';
import {NgbDatepickerModule} from "@ng-bootstrap/ng-bootstrap";
import {Daterangepicker} from "ng2-daterangepicker";
import {FormsModule} from "@angular/forms";
import {MdModule} from "app/md/md.module";

const ENTITY_STATES = [...invoiceRoute, ...invoicePopupRoute];

@NgModule({
  imports: [TbsSharedModule,
    RouterModule.forChild(ENTITY_STATES),
    MaterialModule,
    MaterialModule,
    NgbDatepickerModule,
    Daterangepicker,
    FormsModule,
    MdModule,],
  declarations: [
    InvoiceComponent,
    InvoiceDetailComponent,
    InvoiceUpdateComponent,
    InvoiceDeleteDialogComponent,
    InvoiceDeletePopupComponent
  ],
  entryComponents: [InvoiceComponent, InvoiceUpdateComponent, InvoiceDeleteDialogComponent, InvoiceDeletePopupComponent]
})
export class TbsposInvoiceModule {}
