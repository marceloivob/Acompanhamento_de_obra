import { RouterModule } from '@angular/router';
import { SharedModule } from './../shared/shared.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConfiguraCtefComponent } from './configura-ctef.component';

import { ConfiguracaoCtefRoutingModule } from './configuracao-ctef-routing.module';
import { SiconvFieldsetModule } from '@serpro/ngx-siconv';

@NgModule({
  imports: [
    CommonModule,
    ConfiguracaoCtefRoutingModule,
    SiconvFieldsetModule,
    RouterModule,
    SharedModule
  ],
  declarations: [ConfiguraCtefComponent]
})
export class ConfiguracaoCtefModule { }
