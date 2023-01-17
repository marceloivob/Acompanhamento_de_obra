import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TemplatePrincipalComponent } from '../shared/components/template-principal/template-principal.component';
import { ListagemContratoEmpresaComponent } from './listagem-contrato-empresa/listagem-contrato-empresa.component';
import { ListagemEmpresaComponent } from './listagem-empresa/listagem-empresa.component';
import { EmpresaResolverService } from '../shared/resolvers/empresa-resolver.service';
import { ContratoEmpresaResolverService } from '../shared/resolvers/contrato-empresa-resolver.service';
import { TemplateDadosContratoComponent } from '../shared/components/template-dados-contrato/template-dados-contrato.component';
import { ListagemEmpresaResolverService } from '../shared/resolvers/listagem-empresa-resolver.service';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'empresa',
    pathMatch: 'full'
  },
  {
    path: 'empresa',
    component: TemplatePrincipalComponent,
    data: { breadcrumb: 'Empresas' },
    children: [
      { path: '', redirectTo: 'listar', pathMatch: 'full' },
      {
        path: 'listar',
        resolve: {
          empresas : ListagemEmpresaResolverService
        },
        component: ListagemEmpresaComponent
      },
      {
        path: ':idFornecedor',
        resolve: {
          empresa: EmpresaResolverService
        },
        children: [
          { path: '', redirectTo: 'contrato', pathMatch: 'full' },
          {
            path: 'contrato',
            data: { breadcrumb: 'Contratos' },
            children: [
              { path: '', redirectTo: 'listar', pathMatch: 'full' },
              {
                path: 'listar',
                component: ListagemContratoEmpresaComponent,
                data: { breadcrumb: null }
              },
              {
                path: ':idContrato',
                component: TemplateDadosContratoComponent,
                resolve: {
                  context: ContratoEmpresaResolverService
                },
                data: { breadcrumb: null },
                children: [
                  {
                    path: 'medicao',
                    loadChildren: '../medicao/medicao.module#MedicaoModule',
                    data: { breadcrumb: 'Medição' },
                  },
                  {
                    path: 'config',
                    loadChildren: '../configuracao-ctef/configuracao-ctef.module#ConfiguracaoCtefModule'
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
export class PreenchimentoRoutingModule {}
