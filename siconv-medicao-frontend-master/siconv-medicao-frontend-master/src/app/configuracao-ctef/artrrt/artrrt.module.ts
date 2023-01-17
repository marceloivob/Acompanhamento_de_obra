import { NomeRTPipe } from '../../shared/pipes/nome-rt.pipe';
import { CadastroArtRrtComponent } from './cadastro-art-rrt/cadastro-art-rrt.component';
import { SiconvModule } from './../../siconv.module';
import { ListagemArtRrtComponent } from './listargem-art-rrt/listagem-art-rrt.component';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ArtrrtRoutingModule } from './artrrt-routing.module';
import { SiconvTableModule, SiconvFieldsetModule, SiconvInputModule, SiconvDatePickerModule, SiconvLocalMessageModule } from '@serpro/ngx-siconv';
import { SharedModule } from 'src/app/shared/shared.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { BsDatepickerModule } from 'ngx-bootstrap';
import { NgxDatatableModule } from '@swimlane/ngx-datatable';

@NgModule({
  imports: [
    CommonModule,
    ArtrrtRoutingModule,

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
    ListagemArtRrtComponent,
    CadastroArtRrtComponent,
    NomeRTPipe
  ]
})
export class ArtrrtModule { }
