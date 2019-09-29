import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule} from '@angular/router';
import {NewNavbarComponent} from './navbar.component';
import {TranslateService} from '@ngx-translate/core';
import {TbsSharedModule} from "../shared.module";

@NgModule({
    imports: [ RouterModule, CommonModule ,TbsSharedModule],
    declarations: [ NewNavbarComponent ],
    exports: [ NewNavbarComponent ],
    providers: [ TranslateService ]
})

export class NewNavbarModule {}
