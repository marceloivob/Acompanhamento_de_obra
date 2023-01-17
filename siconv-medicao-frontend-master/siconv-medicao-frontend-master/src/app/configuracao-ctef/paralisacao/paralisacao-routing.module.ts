import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthorizationGuard } from 'src/app/shared/guards/authorization.guard';
import { Role } from 'src/app/shared/model/security/role.enum';
import { ParalisacaoResolverService } from 'src/app/shared/resolvers/paralisacao-resolver.service';
import { CadastroParalisacaoComponent } from './cadastro-paralisacao/cadastro-paralisacao.component';
import { ListagemParalisacaoComponent } from './listagem-paralisacao/listagem-paralisacao.component';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'listar',
    pathMatch: 'full',
  },
  {
    path: 'listar',
    component: ListagemParalisacaoComponent,
    data: { breadcrumb: 'Listar Paralisação' },
  },
  {
    path: 'incluir',
    canActivate: [AuthorizationGuard],
    component: CadastroParalisacaoComponent,
    data: {
      breadcrumb: 'Incluir Paralisação',
      roles: [
        Role.GESTOR_FINANCEIRO_CONVENENTE,
        Role.OPERADOR_FINANCEIRO_CONVENENTE,
        Role.GESTOR_CONVENIO_CONVENENTE,
        Role.FISCAL_CONVENENTE,
        Role.FISCAL_CONCEDENTE,
        Role.OPERACIONAL_CONCEDENTE,
        Role.GESTOR_CONVENIO_CONCEDENTE,
        Role.GESTOR_FINANCEIRO_CONCEDENTE,
        Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA,
        Role.FISCAL_ACOMPANHAMENTO,
        Role.TECNICO_TERCEIRO
      ],
      permissions: [],
    },
  },
  {
    path: ':idParalisacao/editar',
    canActivate: [AuthorizationGuard],
    resolve: {
      paralisacao: ParalisacaoResolverService,
    },
    component: CadastroParalisacaoComponent,
    data: {
      breadcrumb: 'Editar Paralisação',
      roles: [
        Role.GESTOR_FINANCEIRO_CONVENENTE,
        Role.OPERADOR_FINANCEIRO_CONVENENTE,
        Role.GESTOR_CONVENIO_CONVENENTE,
        Role.FISCAL_CONVENENTE,
        Role.FISCAL_CONCEDENTE,
        Role.OPERACIONAL_CONCEDENTE,
        Role.GESTOR_CONVENIO_CONCEDENTE,
        Role.GESTOR_FINANCEIRO_CONCEDENTE,
        Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA,
        Role.FISCAL_ACOMPANHAMENTO,
        Role.TECNICO_TERCEIRO
      ],
      permissions: [],
    },
  },
  {
    path: ':idParalisacao/detalhar',
    resolve: {
      paralisacao: ParalisacaoResolverService,
    },
    component: CadastroParalisacaoComponent,
    data: {
      breadcrumb: 'Detalhar Paralisação',
    },
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ParalisacaoRoutingModule {}
