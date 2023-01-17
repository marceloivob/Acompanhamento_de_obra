import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AuthenticationPlataformaGuard } from './shared/guards/authentication-plataforma.guard';
import { PlataformaCallbackComponent } from './shared/components/plataforma-callback/plataforma-callback.component';
import { IdpCallbackComponent } from './shared/components/idp-callback/idp-callback.component';
import { NotFoundComponent } from './shared/not-found/not-found.component';
import { NotAuthorizedComponent } from './shared/not-authorized/not-authorized.component';

const routes: Routes = [
  { path: '', redirectTo: 'preenchimento', pathMatch: 'full' },
  {
    path: 'preenchimento',
    canActivate: [AuthenticationPlataformaGuard],
    loadChildren: './preenchimento/preenchimento.module#PreenchimentoModule'
  },
  {
    path: 'acompanhamento',
    loadChildren: './acompanhamento/acompanhamento.module#AcompanhamentoModule'
  },
  { path: 'idp', component: IdpCallbackComponent, pathMatch: 'full' },
  {
    path: 'acesso',
    component: PlataformaCallbackComponent,
    pathMatch: 'full'
  },
  { path: 'not-authorized', component: NotAuthorizedComponent, pathMatch: 'full' },
  { path: '**', component: NotFoundComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
