import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';


import {FieldErrorDisplayComponent} from './field-error-display.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {NouisliderModule} from 'ng2-nouislider';
import {TagInputModule} from 'ngx-chips';
import {MaterialModule} from '../../app.module';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        NouisliderModule,
        TagInputModule,
        MaterialModule
    ],
    declarations: [ FieldErrorDisplayComponent ],
    exports: [ FieldErrorDisplayComponent ]
})
export class FieldErrorDisplayModule {}
