import {NgModule} from '@angular/core';
import {TbsSharedLibsModule} from './shared-libs.module';
import {FindLanguageFromKeyPipe} from './language/find-language-from-key.pipe';
import {JhiAlertComponent} from './alert/alert.component';
import {JhiAlertErrorComponent} from './alert/alert-error.component';
import {HasAnyAuthorityDirective} from './auth/has-any-authority.directive';
import {NgxDatatableModule} from '@swimlane/ngx-datatable';
import {DatatableTotalEntriesComponent} from 'app/shared/datatable/datatable-total-entries/datatable-total-entries.component';
import {DatatableDirective} from 'app/shared/directives/datatable.directive';
import {TbsDateTimePipe} from 'app/shared/pipes/tbs-date-time/tbs-date-time.pipe';

@NgModule({
  imports: [
    TbsSharedLibsModule,
    NgxDatatableModule
  ],
  declarations: [
    FindLanguageFromKeyPipe,
    JhiAlertComponent,
    JhiAlertErrorComponent,
    /*JhiLoginModalComponent,*/
    HasAnyAuthorityDirective,
    DatatableTotalEntriesComponent,
    DatatableDirective,
    TbsDateTimePipe
  ],
  // entryComponents: [JhiLoginModalComponent],
  exports: [
    TbsSharedLibsModule,
    FindLanguageFromKeyPipe,
    JhiAlertComponent,
    JhiAlertErrorComponent,
    // JhiLoginModalComponent,
    HasAnyAuthorityDirective,
    NgxDatatableModule,
    DatatableTotalEntriesComponent,
    DatatableDirective,
    TbsDateTimePipe
  ]
})
export class TbsSharedModule {}
