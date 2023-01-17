import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { SiconvFieldsetModule } from '@serpro/ngx-siconv';
import { SharedModule } from '../shared/shared.module';
import { CardContratoComponent } from './listagem-contrato-empresa/card-contrato/card-contrato.component';
import { ListagemContratoEmpresaComponent } from './listagem-contrato-empresa/listagem-contrato-empresa.component';
import { ListagemEmpresaComponent } from './listagem-empresa/listagem-empresa.component';
import { PreenchimentoRoutingModule } from './preenchimento-routing.module';

@NgModule({
  imports: [
    CommonModule,
    SharedModule,
    SiconvFieldsetModule,
    PreenchimentoRoutingModule
  ],
  declarations: [
    ListagemEmpresaComponent,
    ListagemContratoEmpresaComponent,
    CardContratoComponent
  ]
})
export class PreenchimentoModule {}
