import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ParalisacaoRoutingModule } from './paralisacao-routing.module';
import { CadastroParalisacaoComponent } from './cadastro-paralisacao/cadastro-paralisacao.component';
import { ListagemParalisacaoComponent } from './listagem-paralisacao/listagem-paralisacao.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { SiconvModule } from 'src/app/siconv.module';
import { ReactiveFormsModule } from '@angular/forms';
import { SiconvTextAreaModule } from '@serpro/ngx-siconv';

@NgModule({
  imports: [
    CommonModule,
    SharedModule,
    SiconvModule,
    ReactiveFormsModule,
    ParalisacaoRoutingModule,
    SiconvTextAreaModule
  ],
  declarations: [
    CadastroParalisacaoComponent,
    ListagemParalisacaoComponent
  ],
})
export class ParalisacaoModule {}
