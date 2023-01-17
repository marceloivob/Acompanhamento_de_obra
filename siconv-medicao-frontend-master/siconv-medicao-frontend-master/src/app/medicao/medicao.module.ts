import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {
  SiconvDatePickerModule,
  SiconvLocalMessageModule,
  SiconvTextAreaModule
} from '@serpro/ngx-siconv';
import { SharedModule } from '../shared/shared.module';
import { SiconvModule } from '../siconv.module';
import { CadastroMedicaoComponent } from './cadastro-medicao/cadastro-medicao.component';
import { FormDadosMedicaoComponent } from './cadastro-medicao/form-dados-medicao/form-dados-medicao.component';
import { ListagemMedicaoComponent } from './listagem-medicao/listagem-medicao.component';
import { MedicaoRoutingModule } from './medicao-routing.module';
import { ListagemSubmetaComponent } from './submeta/listagem-submeta/listagem-submeta.component';
import { ListagemObservacaoComponent } from './observacao/listagem-observacao/listagem-observacao.component';
import { CadastroObservacaoComponent } from './observacao/cadastro-observacao/cadastro-observacao.component';
import { CadastroSubmetaComponent } from './submeta/cadastro-submeta/cadastro-submeta.component';
import { FormDadosSubmetaComponent } from './submeta/cadastro-submeta/form-dados-submeta/form-dados-submeta.component';
import { UiSwitchModule } from 'ngx-ui-switch';
import { DadosAssinaturaSubmetaComponent } from './submeta/cadastro-submeta/dados-assinatura-submeta/dados-assinatura-submeta.component';
import { DetalheDadosMedicaoComponent } from './cadastro-medicao/detalhe-dados-medicao/detalhe-dados-medicao.component';
import { ListagemHistoricoComponent } from './listagem-historico/listagem-historico.component';
import { SolicitanteVistoriaExtraPipe } from '../shared/pipes/solicitante.pipe';
import { FormDadosSubmetaServicoComponent } from './submeta/cadastro-submeta/form-dados-submeta-servico/form-dados-submeta-servico.component';
import { ListagemSubmetaAgrupadaComponent } from './submeta/listagem-submeta-agrupada/listagem-submeta-agrupada.component';

@NgModule({
  imports: [
    CommonModule,
    SharedModule,
    SiconvModule,
    FormsModule,
    ReactiveFormsModule,
    SiconvDatePickerModule,
    SiconvLocalMessageModule,
    SiconvTextAreaModule,
    UiSwitchModule,
    MedicaoRoutingModule
  ],
  declarations: [
    ListagemMedicaoComponent,
    CadastroMedicaoComponent,
    FormDadosMedicaoComponent,
    ListagemSubmetaComponent,
    ListagemObservacaoComponent,
    CadastroObservacaoComponent,
    CadastroSubmetaComponent,
    FormDadosSubmetaComponent,
    DadosAssinaturaSubmetaComponent,
    DetalheDadosMedicaoComponent,
    ListagemHistoricoComponent,
    SolicitanteVistoriaExtraPipe,
    FormDadosSubmetaServicoComponent,
    ListagemSubmetaAgrupadaComponent
  ]
})
export class MedicaoModule {}
