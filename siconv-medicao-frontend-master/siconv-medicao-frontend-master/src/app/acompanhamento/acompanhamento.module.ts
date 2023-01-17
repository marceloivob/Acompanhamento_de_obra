import { SharedModule } from './../shared/shared.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SiconvModule } from './../siconv.module';

import { AcompanhamentoRoutingModule } from './acompanhamento-routing.module';
import { DadosGeraisAcompanhamentoComponent } from './dados-gerais-acompanhamento/dados-gerais-acompanhamento.component';
import { SiconvTableModule } from '@serpro/ngx-siconv';

@NgModule({
  imports: [
    CommonModule,
    SharedModule,
    AcompanhamentoRoutingModule,
    SiconvModule,
    SiconvTableModule
  ], exports: [
      SiconvTableModule
  ],
  declarations: [
    DadosGeraisAcompanhamentoComponent,
  ]
})
export class AcompanhamentoModule { }
