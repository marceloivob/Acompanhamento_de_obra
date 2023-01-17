import { ArtRrtResolverService } from './../../shared/resolvers/artrrt-resolver.service';
import { CadastroArtRrtComponent } from './cadastro-art-rrt/cadastro-art-rrt.component';
import { ListagemArtRrtComponent } from './listargem-art-rrt/listagem-art-rrt.component';
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Role } from 'src/app/shared/model/security/role.enum';
import { AuthorizationGuard } from 'src/app/shared/guards/authorization.guard';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'listar',
    pathMatch: 'full'
  },
  {
    path: 'listar',
    component: ListagemArtRrtComponent,
    data: {
      breadcrumb:
        'Listar Anotação de Responsabilidade Técnica'
    }
  },
  {
    path: 'incluir',
    canActivate: [ AuthorizationGuard ] ,
    component: CadastroArtRrtComponent,
    data: {
      breadcrumb:
        'Incluir Anotação de Responsabilidade Técnica',
        roles: [  Role.FISCAL_CONVENENTE,
                  Role.GESTOR_CONVENIO_CONVENENTE,
                  Role.GESTOR_FINANCEIRO_CONVENENTE,
                  Role.OPERADOR_FINANCEIRO_CONVENENTE ],
        permissions: [ ]
    }
  },
  {
    path: ':idArtRrt/editar',
    canActivate: [ AuthorizationGuard ] ,
    component: CadastroArtRrtComponent,
    resolve: {
      artRrt: ArtRrtResolverService
    },
    data: {
      breadcrumb:
        'Editar Anotação de Responsabilidade Técnica',
        roles: [  Role.FISCAL_CONVENENTE,
                  Role.GESTOR_CONVENIO_CONVENENTE,
                  Role.GESTOR_FINANCEIRO_CONVENENTE,
                  Role.OPERADOR_FINANCEIRO_CONVENENTE ],
        permissions: [ ]
    }
  },
  {
    path: ':idArtRrt/detalhar',
    component: CadastroArtRrtComponent,
    resolve: {
      artRrt: ArtRrtResolverService
    },
    data: {
      breadcrumb:
        'Detalhar Anotação de Responsabilidade Técnica'
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ArtrrtRoutingModule {}
