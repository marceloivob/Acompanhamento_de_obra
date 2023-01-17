import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TemplatePrincipalComponent } from '../shared/components/template-principal/template-principal.component';
import { DadosGeraisAcompanhamentoComponent } from './dados-gerais-acompanhamento/dados-gerais-acompanhamento.component';
import { AuthenticationIdpGuard } from '../shared/guards/authentication-idp.guard';
import { ContratoEmpresaResolverService } from '../shared/resolvers/contrato-empresa-resolver.service';
import { TemplateDadosContratoComponent } from '../shared/components/template-dados-contrato/template-dados-contrato.component';

const routes: Routes = [
  {
    path: '',
    component: TemplatePrincipalComponent,
    children: [
      {
        path: 'proposta/:idProposta',
        data: { breadcrumb: 'Acompanhamento de Obras' },
        children: [
          {
            path: '',
            pathMatch: 'full',
            redirectTo: 'dados-gerais'
          },
          {
            path: 'dados-gerais',
            canActivate: [AuthenticationIdpGuard],
            component: DadosGeraisAcompanhamentoComponent,
            data: { breadcrumb: 'Listar CTEF / Lote' }
          },
          {
            path: '',
            data: { breadcrumb: false, label: 'Contrato de Execução e/ou Fornecimento' },
            children: [
              {
                path: 'contrato/:idContrato',
                canActivate: [AuthenticationIdpGuard],
                component: TemplateDadosContratoComponent,
                resolve: {
                  context: ContratoEmpresaResolverService
                },
                data: { breadcrumb: null },
                children: [
                  {
                    path: 'config',
                    loadChildren: '../configuracao-ctef/configuracao-ctef.module#ConfiguracaoCtefModule'
                  },
                  {
                    path: 'medicao',
                    loadChildren: '../medicao/medicao.module#MedicaoModule',
                    data: { breadcrumb: 'Medição' }
                  }
                ]
              }
            ]
          }
        ]
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AcompanhamentoRoutingModule {}
