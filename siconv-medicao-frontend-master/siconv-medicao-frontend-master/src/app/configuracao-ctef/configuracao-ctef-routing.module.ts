import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ConfiguraCtefComponent } from './configura-ctef.component';

const routes: Routes = [
  {
    path: '',
    component: ConfiguraCtefComponent,
    data: { breadcrumb: null },
    children: [
      {
        path: 'rtsocial',
        loadChildren: './rt-social/rt-social.module#RtSocialModule'
      },
      {
        path: 'rt',
        loadChildren: './rt/rt.module#RtModule'
      },
      {
        path: 'doccomplementar',
        loadChildren: './doc-complementar/doc-complementar.module#DocComplementarModule'
      },
      {
        path: 'artrrt',
        loadChildren: './artrrt/artrrt.module#ArtrrtModule'
      },
      {
        path: 'paralisacao',
        loadChildren: './paralisacao/paralisacao.module#ParalisacaoModule'
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ConfiguracaoCtefRoutingModule {}
