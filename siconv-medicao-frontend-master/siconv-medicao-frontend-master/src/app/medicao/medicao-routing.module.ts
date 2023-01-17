import { Permission } from './../shared/model/security/permission.enum';
import { MedicaoResolverService } from './../shared/resolvers/medicao-resolver.service';
import { ListagemHistoricoComponent } from './listagem-historico/listagem-historico.component';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CadastroMedicaoComponent } from './cadastro-medicao/cadastro-medicao.component';
import { ListagemMedicaoComponent } from './listagem-medicao/listagem-medicao.component';
import { CadastroObservacaoComponent } from './observacao/cadastro-observacao/cadastro-observacao.component';
import { ListagemObservacaoComponent } from './observacao/listagem-observacao/listagem-observacao.component';
import { CadastroSubmetaComponent } from './submeta/cadastro-submeta/cadastro-submeta.component';
import { ObservacaoResolverService } from '../shared/resolvers/observacao-resolver.service';
import { SubmetaResolverService } from '../shared/resolvers/submeta-resolver.service';
import { Role } from '../shared/model/security/role.enum';
import { AuthorizationGuard } from '../shared/guards/authorization.guard';

const routes: Routes = [
  { path: '', redirectTo: 'listar', pathMatch: 'full' },
  {
    path: 'listar',
    component: ListagemMedicaoComponent,
    data: { breadcrumb: 'Listar Medição'}
  },
  {
    path: 'historico',
    data: { breadcrumb: 'Listar Histórico'},
    children: [
      { path: '', redirectTo: 'listar', pathMatch: 'full' },
      {
        path: 'listar',
        component: ListagemHistoricoComponent,
        data: { breadcrumb: null }
      }
    ]
  },
  {
    path: 'incluir',
    canActivate: [ AuthorizationGuard ],
    component: CadastroMedicaoComponent,
    data: { breadcrumb: 'Criar Medição',
            roles: [ ],
            permissions: [ Permission.INCLUIR_MEDICAO ]
          }
  },
  {
    path: ':idMedicao/editar',
    canActivate: [ AuthorizationGuard ],
    component: CadastroMedicaoComponent,
    resolve: {
      medicao: MedicaoResolverService
    },
    data: { breadcrumb: 'Alterar Medição',
    roles: [  Role.GESTOR_FINANCEIRO_CONVENENTE,
              Role.OPERADOR_FINANCEIRO_CONVENENTE,
              Role.GESTOR_CONVENIO_CONVENENTE,
              Role.FISCAL_CONVENENTE,
              Role.FISCAL_CONCEDENTE,
              Role.OPERACIONAL_CONCEDENTE,
              Role.GESTOR_FINANCEIRO_CONCEDENTE,
              Role.GESTOR_CONVENIO_CONCEDENTE,
              Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA,
              Role.FISCAL_ACOMPANHAMENTO,
              Role.TECNICO_TERCEIRO,
              Role.ADMINISTRADOR_SISTEMA,
              Role.ADMINISTRADOR_SISTEMA_ORGAO_EXTERNO ],
    permissions: [  Permission.EDITAR_MEDICAO,
                    Permission.INCLUIR_MEDICAO,
                    Permission.ENVIAR_MEDICAO_CONVENENTE,
                    Permission.ASSINAR_SUBMETA,
                    Permission.EDITAR_SUBMETA,
                    Permission.EXCLUIR_SUBMETA,
                    Permission.INCLUIR_OBSERVACAO_MEDICAO,
                    Permission.EDITAR_OBSERVACAO_MEDICAO,
                    Permission.EXCLUIR_OBSERVACAO_MEDICAO]
          }
  },
  {
    path: ':idMedicao/editar',
    data: { breadcrumb: 'Alterar Medição' },
    resolve: {
      medicao: MedicaoResolverService
    },
    children: [
      {
        path: 'submeta/:idSubmeta/editar',
        canActivate: [ AuthorizationGuard ],
        component: CadastroSubmetaComponent,
        resolve: {
          submeta: SubmetaResolverService
        },
        data: {
          breadcrumb: 'Preencher Medição Submeta',
          roles: [  Role.GESTOR_FINANCEIRO_CONVENENTE,
                    Role.OPERADOR_FINANCEIRO_CONVENENTE,
                    Role.GESTOR_CONVENIO_CONVENENTE,
                    Role.FISCAL_CONVENENTE,
                    Role.FISCAL_CONCEDENTE,
                    Role.OPERACIONAL_CONCEDENTE,
                    Role.GESTOR_CONVENIO_CONCEDENTE,
                    Role.GESTOR_FINANCEIRO_CONCEDENTE,
                    Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA,
                    Role.FISCAL_ACOMPANHAMENTO,
                    Role.TECNICO_TERCEIRO ],
          permissions: [ Permission.ASSINAR_SUBMETA,
                         Permission.EDITAR_SUBMETA,
                         Permission.EXCLUIR_SUBMETA ]
              }
      },
      {
        path: 'submeta/:idSubmeta/detalhar',
        component: CadastroSubmetaComponent,
        resolve: {
          submeta: SubmetaResolverService
        },
        data: {
          breadcrumb: 'Detalhar Medição Submeta'
        }
      },
      {
        path: 'observacao/listar',
        component: ListagemObservacaoComponent,
        data: {
          breadcrumb: 'Listar Observação'
        }
      },
      {
        path: 'observacao/incluir',
        canActivate: [ AuthorizationGuard ],
        component: CadastroObservacaoComponent,
        data: {
          breadcrumb: 'Incluir Observação',
          roles: [Role.GESTOR_FINANCEIRO_CONVENENTE,
                  Role.OPERADOR_FINANCEIRO_CONVENENTE,
                  Role.GESTOR_CONVENIO_CONVENENTE,
                  Role.FISCAL_CONVENENTE,
                  Role.FISCAL_CONCEDENTE,
                  Role.GESTOR_CONVENIO_CONCEDENTE,
                  Role.GESTOR_FINANCEIRO_CONCEDENTE,
                  Role.OPERACIONAL_CONCEDENTE,
                  Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA,
                  Role.FISCAL_ACOMPANHAMENTO,
                  Role.TECNICO_TERCEIRO ],
          permissions: [  Permission.INCLUIR_OBSERVACAO_MEDICAO]
        }
      },
      {
        path: 'observacao/:idObservacao/editar',
        canActivate: [ AuthorizationGuard ],
        component: CadastroObservacaoComponent,
        data: {
          breadcrumb: 'Alterar Observação',
          roles: [Role.GESTOR_FINANCEIRO_CONVENENTE,
                  Role.OPERADOR_FINANCEIRO_CONVENENTE,
                  Role.GESTOR_CONVENIO_CONVENENTE,
                  Role.FISCAL_CONVENENTE,
                  Role.FISCAL_CONCEDENTE,
                  Role.GESTOR_CONVENIO_CONCEDENTE,
                  Role.GESTOR_FINANCEIRO_CONCEDENTE,
                  Role.OPERACIONAL_CONCEDENTE,
                  Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA,
                  Role.FISCAL_ACOMPANHAMENTO,
                  Role.TECNICO_TERCEIRO ],
          permissions: [ Permission.EDITAR_OBSERVACAO_MEDICAO ]
              },
        resolve: {
          obs: ObservacaoResolverService
        },
      },
      {
        path: 'acumulada/:idMedicaoAcumulada/submeta/:idSubmeta/editar',
        canActivate: [ AuthorizationGuard ],
        component: CadastroSubmetaComponent,
        resolve: {
          medicaoAcumulada: MedicaoResolverService,
          submeta: SubmetaResolverService
        },
        data: {
          breadcrumb: 'Preencher Submeta Medição Acumulada',
          roles: [  Role.GESTOR_FINANCEIRO_CONVENENTE,
                    Role.OPERADOR_FINANCEIRO_CONVENENTE,
                    Role.GESTOR_CONVENIO_CONVENENTE,
                    Role.FISCAL_CONVENENTE,
                    Role.FISCAL_CONCEDENTE,
                    Role.OPERACIONAL_CONCEDENTE,
                    Role.GESTOR_CONVENIO_CONCEDENTE,
                    Role.GESTOR_FINANCEIRO_CONCEDENTE,
                    Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA,
                    Role.FISCAL_ACOMPANHAMENTO,
                    Role.TECNICO_TERCEIRO ],
          permissions: [ Permission.ASSINAR_SUBMETA,
                         Permission.EDITAR_SUBMETA,
                         Permission.EXCLUIR_SUBMETA ]
              }
      },
      {
        path: 'acumulada/:idMedicaoAcumulada/submeta/:idSubmeta/detalhar',
        component: CadastroSubmetaComponent,
        resolve: {
          medicaoAcumulada: MedicaoResolverService,
          submeta: SubmetaResolverService
        },
        data: {
          breadcrumb: 'Detalhar Submeta Medição Acumulada'
        }
      },
    ]
  },
  {
    path: ':idMedicao/detalhar',
    component: CadastroMedicaoComponent,
    resolve: {
      medicao: MedicaoResolverService
    },
    data: { breadcrumb: 'Detalhar Medição' }
  },
  {
    path: ':idMedicao/detalhar',
    data: { breadcrumb: 'Detalhar Medição' },
    resolve: {
      medicao: MedicaoResolverService
    },
    children: [
      {
        path: 'submeta/:idSubmeta/detalhar',
        component: CadastroSubmetaComponent,
        resolve: {
          submeta: SubmetaResolverService
        },
        data: {
          breadcrumb: 'Detalhar Medição Submeta'
        }
      },
      {
        path: 'observacao/listar',
        component: ListagemObservacaoComponent,
        data: {
          breadcrumb: 'Listar Observação'
        }
      },
      {
        path: 'acumulada/:idMedicaoAcumulada/submeta/:idSubmeta/detalhar',
        component: CadastroSubmetaComponent,
        resolve: {
          submeta: SubmetaResolverService
        },
        data: {
          breadcrumb: 'Detalhar Medição Acumulada Submeta'
        }
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MedicaoRoutingModule {}
