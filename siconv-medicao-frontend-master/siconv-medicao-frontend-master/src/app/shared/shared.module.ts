import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import {
  SiconvAlertMessagesModule,
  SiconvHeaderModule,
  SiconvTitleModule,
  SiconvFieldsetModule,
  SiconvPipesModule
} from '@serpro/ngx-siconv';
import { TemplatePrincipalComponent } from './components/template-principal/template-principal.component';
import { CabecalhoComponent } from './components/cabecalho/cabecalho.component';
import { DadosTipoInstrumentoComponent } from './components/dados-tipo-instrumento/dados-tipo-instrumento.component';
import { TituloComponent } from './components/titulo/titulo.component';
import { CnpjPipe } from './pipes/cnpj.pipe';
import { CpfPipe } from './pipes/cpf.pipe';
import { NomeAtividadePipe } from './pipes/nome-atividade.pipe';
import { ObjetoPipe } from './pipes/objeto.pipe';
import { SilgaUFPipe } from './pipes/sigla-uf.pipe';
import { DadosCtefComponent } from './components/dados-ctef/dados-ctef.component';
import { SiconvMenuAntigoComponent } from './components/siconv-menu-antigo/siconv-menu-antigo.component';
import { TemplateDadosContratoComponent } from './components/template-dados-contrato/template-dados-contrato.component';
import { FiltroAnexosPipe } from './pipes/filtro-anexos.pipe';
import { ColunaTextoTruncadoDirective } from './directives/coluna-texto-truncado.directive';
import { FiltroArrayPipe } from './pipes/filtro-array.pipe';
import { MapAttributePipe } from './pipes/map-attribute.pipe';

@NgModule({
  imports: [
    CommonModule,
    RouterModule,
    SiconvHeaderModule,
    SiconvAlertMessagesModule,
    SiconvTitleModule,
    SiconvFieldsetModule,
    SiconvPipesModule
  ],
  declarations: [
    TemplatePrincipalComponent,
    CabecalhoComponent,
    TituloComponent,
    CnpjPipe,
    CpfPipe,
    ObjetoPipe,
    NomeAtividadePipe,
    SilgaUFPipe,
    DadosTipoInstrumentoComponent,
    DadosCtefComponent,
    SiconvMenuAntigoComponent,
    TemplateDadosContratoComponent,
    FiltroAnexosPipe,
    ColunaTextoTruncadoDirective,
    FiltroArrayPipe,
    MapAttributePipe
  ],
  exports: [
    TemplatePrincipalComponent,
    CabecalhoComponent,
    TituloComponent,
    CnpjPipe,
    CpfPipe,
    ObjetoPipe,
    NomeAtividadePipe,
    SilgaUFPipe,
    DadosTipoInstrumentoComponent,
    DadosCtefComponent,
    TemplateDadosContratoComponent,
    FiltroAnexosPipe,
    ColunaTextoTruncadoDirective,
    FiltroArrayPipe,
    MapAttributePipe
  ]
})
export class SharedModule {}
