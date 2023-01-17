import { BsDatepickerModule } from 'ngx-bootstrap';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { SharedModule } from './../../shared/shared.module';
import { CadastroRtComponent } from './cadastro-rt/cadastro-rt.component';
import { SiconvModule } from './../../siconv.module';
import { SiconvTableModule, SiconvFieldsetModule, SiconvInputModule, SiconvDatePickerModule, SiconvLocalMessageModule } from '@serpro/ngx-siconv';
import { ListagemRtComponent } from './listagem-rt/listagem-rt.component';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { RtRoutingModule } from './rt-routing.module';
import { NgxDatatableModule } from '@swimlane/ngx-datatable';

@NgModule({
  imports: [
    CommonModule,
    RtRoutingModule,
    SiconvModule,
    SiconvTableModule,
    SiconvFieldsetModule,
    SharedModule,
    SiconvInputModule,

    SiconvDatePickerModule,
    ReactiveFormsModule,
    BsDatepickerModule.forRoot(),
    FormsModule,
    NgxDatatableModule,
    SiconvLocalMessageModule
  ],
  declarations: [
    ListagemRtComponent,
    CadastroRtComponent
  ]
})
export class RtModule { }
