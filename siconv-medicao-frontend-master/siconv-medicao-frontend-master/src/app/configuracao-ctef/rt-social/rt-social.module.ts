import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from 'src/app/shared/shared.module';
import { SiconvModule } from 'src/app/siconv.module';
import { ListagemRtSocialComponent } from './listagem-rt-social/listagem-rt-social.component';
import { RtSocialRoutingModule } from './rt-social-routing.module';
import { CadastroRtSocialComponent } from './cadastro-rt-social/cadastro-rt-social.component';

@NgModule({
  imports: [
    CommonModule,
    SharedModule,
    SiconvModule,
    ReactiveFormsModule,
    RtSocialRoutingModule
  ],
  declarations: [
    ListagemRtSocialComponent,
    CadastroRtSocialComponent
  ]
})
export class RtSocialModule {}
