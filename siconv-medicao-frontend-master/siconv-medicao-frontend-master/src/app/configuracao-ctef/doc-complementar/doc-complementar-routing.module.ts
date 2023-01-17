import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CadastroDocComplementarComponent } from './cadastro-doc-complementar/cadastro-doc-complementar.component';
import { ListagemDocComplementarComponent } from './listagem-doc-complementar/listagem-doc-complementar.component';
import { DocComplementarResolverService } from '../../shared/resolvers/doc-complementar-resolver.service';
import { AuthorizationGuard } from 'src/app/shared/guards/authorization.guard';
import { Role } from 'src/app/shared/model/security/role.enum';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'listar',
    pathMatch: 'full'
  },
  {
    path: 'listar',
    component: ListagemDocComplementarComponent,
    data: {
      breadcrumb: 'Listar Documentação Complementar'
    }
  },
  {
    path: 'incluir',
    canActivate: [ AuthorizationGuard ],
    component: CadastroDocComplementarComponent,
    data: {
      breadcrumb: 'Incluir Documentação Complementar',
      roles: [  Role.FISCAL_CONVENENTE,
                Role.GESTOR_CONVENIO_CONVENENTE,
                Role.GESTOR_FINANCEIRO_CONVENENTE,
                Role.OPERADOR_FINANCEIRO_CONVENENTE ],
      permissions: [ ]
    }
  },
  {
    path: ':idDocCompl/editar',
    canActivate: [ AuthorizationGuard ],
    component: CadastroDocComplementarComponent,
    resolve: {
      docComplementar: DocComplementarResolverService
    },
    data: {
      breadcrumb: 'Editar Documentação Complementar',
      roles: [  Role.FISCAL_CONVENENTE,
                Role.GESTOR_CONVENIO_CONVENENTE,
                Role.GESTOR_FINANCEIRO_CONVENENTE,
                Role.OPERADOR_FINANCEIRO_CONVENENTE ],
      permissions: [ ]
    }
  },
  {
    path: ':idDocCompl/detalhar',
    component: CadastroDocComplementarComponent,
    resolve: {
      docComplementar: DocComplementarResolverService
    },
    data: {
      breadcrumb: 'Detalhar Documentação Complementar'
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DocComplementarRoutingModule {}
