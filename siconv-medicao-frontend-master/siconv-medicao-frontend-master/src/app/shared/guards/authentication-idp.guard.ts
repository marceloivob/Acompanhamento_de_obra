import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { UsuarioLogadoService } from '../services/usuario-logado.service';
import { IdpService } from '../services/security/idp.service';
import { TokenService } from '../services/security/token.service';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationIdpGuard implements CanActivate {

  constructor(
    private usuarioLogadoService: UsuarioLogadoService,
    private idpService: IdpService,
    private tokenService: TokenService,
    private router: Router
  ) {}

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> | Promise<boolean> | boolean {

    const idProposta = next.params['idProposta'];

    if (!this.usuarioLogadoService.isLoggedIn()) {

      this.idpService.initAuthorizationCodeFlow(idProposta, state.url);
      return false;

    } else if (this.usuarioLogadoService.usuarioLogado.isLoginIDP &&
               this.usuarioLogadoService.usuarioLogado.proposta !== idProposta) {

      this.tokenService.removeToken();
      this.idpService.initAuthorizationCodeFlow(idProposta, state.url);
      return false;

    } else if (!this.usuarioLogadoService.usuarioLogado.isLoginIDP) {

      this.router.navigate(['not-authorized'], { skipLocationChange: true });
      return false;
    }

    return true;
  }
}
