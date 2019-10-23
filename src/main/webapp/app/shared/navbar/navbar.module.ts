import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NavbarComponent } from './navbar.component';
import { MatButtonModule } from '@angular/material';
import {TbsSharedModule} from '../shared.module';
@NgModule({
    imports: [ RouterModule, CommonModule, MatButtonModule, TbsSharedModule ],
    declarations: [ NavbarComponent ],
    exports: [ NavbarComponent ]
})

export class NavbarModule {}
