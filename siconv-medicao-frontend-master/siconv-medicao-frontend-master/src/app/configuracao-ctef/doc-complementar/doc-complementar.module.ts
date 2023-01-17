import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DocComplementarRoutingModule } from './doc-complementar-routing.module';
import { ListagemDocComplementarComponent } from './listagem-doc-complementar/listagem-doc-complementar.component';
import { TipoDocumentoPipe } from 'src/app/shared/pipes/tipo-documento.pipe';
import { TipoManifestoAmbientalPipe } from 'src/app/shared/pipes/tipo-manifesto-ambiental.pipe';
import { SharedModule } from 'src/app/shared/shared.module';
import { SiconvModule } from 'src/app/siconv.module';
import { CadastroDocComplementarComponent } from './cadastro-doc-complementar/cadastro-doc-complementar.component';
import { ReactiveFormsModule } from '@angular/forms';
import { DocComplementarResolverService } from '../../shared/resolvers/doc-complementar-resolver.service';

@NgModule({
  imports: [CommonModule, SharedModule, SiconvModule, ReactiveFormsModule, DocComplementarRoutingModule],
  declarations: [
    ListagemDocComplementarComponent,
    CadastroDocComplementarComponent,
    TipoDocumentoPipe,
    TipoManifestoAmbientalPipe
  ],
  providers: [ DocComplementarResolverService ]
})
export class DocComplementarModule {}
