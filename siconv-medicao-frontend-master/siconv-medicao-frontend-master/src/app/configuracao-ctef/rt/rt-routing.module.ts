import { RtResolverService } from './../../shared/resolvers/rt-resolver.service';
import { CadastroRtComponent } from './cadastro-rt/cadastro-rt.component';
import { ListagemRtComponent } from './listagem-rt/listagem-rt.component';
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
    component: ListagemRtComponent,
    data: { breadcrumb: 'Listar Responsável Técnico' }
  },
  {
    path: 'incluir',
    component: CadastroRtComponent,
    canActivate: [ AuthorizationGuard ] ,
    data: { breadcrumb: 'Incluir Responsável Técnico',
            roles: [  Role.FISCAL_CONVENENTE,
                      Role.GESTOR_CONVENIO_CONVENENTE,
                      Role.GESTOR_FINANCEIRO_CONVENENTE,
                      Role.OPERADOR_FINANCEIRO_CONVENENTE ],
            permissions: [ ]
          }
  },
  {
    path: ':idRt/detalhar',
    component: CadastroRtComponent,
    resolve: {
      rt: RtResolverService
    },
    data: {
      breadcrumb: 'Detalhar Responsável Técnico'
    }
  },
  {
    path: ':idRt/editar',
    canActivate: [ AuthorizationGuard ] ,
    component: CadastroRtComponent,
    resolve: {
      rt: RtResolverService
    },
    data: {
      breadcrumb: 'Editar Responsável Técnico',
      roles: [  Role.FISCAL_CONVENENTE,
                Role.GESTOR_CONVENIO_CONVENENTE,
                Role.GESTOR_FINANCEIRO_CONVENENTE,
                Role.OPERADOR_FINANCEIRO_CONVENENTE ],
      permissions: [ ]
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RtRoutingModule {}
