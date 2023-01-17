import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ListagemRtSocialComponent } from './listagem-rt-social/listagem-rt-social.component';
import { CadastroRtSocialComponent } from './cadastro-rt-social/cadastro-rt-social.component';
import { RtSocialResolverService } from 'src/app/shared/resolvers/rt-social-resolver.service';
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
    component: ListagemRtSocialComponent,
    data: { breadcrumb: 'Listar Responsável Técnico' }
  },
  {
    path: 'incluir',
    canActivate: [ AuthorizationGuard ],
    component: CadastroRtSocialComponent,
    data: { breadcrumb: 'Incluir Responsável Técnico',
            roles: [  Role.FISCAL_CONVENENTE,
                      Role.GESTOR_CONVENIO_CONVENENTE,
                      Role.GESTOR_FINANCEIRO_CONVENENTE,
                      Role.OPERADOR_FINANCEIRO_CONVENENTE ],
            permissions: [ ]
          }
  },
  {
    path: ':idRespTecSocial/editar',
    canActivate: [ AuthorizationGuard ],
    resolve: {
      rtSocial: RtSocialResolverService
    },
    component: CadastroRtSocialComponent,
    data: { breadcrumb: 'Editar Responsável Técnico',
            roles: [  Role.FISCAL_CONVENENTE,
                      Role.GESTOR_CONVENIO_CONVENENTE,
                      Role.GESTOR_FINANCEIRO_CONVENENTE,
                      Role.OPERADOR_FINANCEIRO_CONVENENTE ],
            permissions: [ ]
          }
  },
  {
    path: ':idRespTecSocial/detalhar',
    resolve: {
      rtSocial: RtSocialResolverService
    },
    component: CadastroRtSocialComponent,
    data: {
      breadcrumb: 'Detalhar Responsável Técnico'
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RtSocialRoutingModule {}
