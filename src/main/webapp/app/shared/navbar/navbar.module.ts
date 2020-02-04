import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NavbarComponent } from './navbar.component';
import { MatButtonModule } from '@angular/material';
import {TbsSharedModule} from '../shared.module';
import {TranslateService} from "@ngx-translate/core";
@NgModule({
    imports: [ RouterModule, CommonModule, MatButtonModule, TbsSharedModule ],
    declarations: [ NavbarComponent ],
    exports: [ NavbarComponent ],
  providers: [ TranslateService ]
})

export class NavbarModule {}
