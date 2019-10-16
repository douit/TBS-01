import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NewNavbarComponent } from './navbar.component';
import { MatButtonModule } from '@angular/material';
@NgModule({
    imports: [ RouterModule, CommonModule, MatButtonModule ],
    declarations: [ NewNavbarComponent ],
    exports: [ NewNavbarComponent ]
})

export class NewNavbarModule {}
