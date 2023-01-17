import { Permission } from './../model/security/permission.enum';
import { Role } from './../model/security/role.enum';
import { Profile } from './../model/security/profile.enum';
import { AppService } from './../core/App.server';
import { Injectable } from '@angular/core';
import { UsuarioLogado, TIPO_LOGIN_ENUM } from '../model/usuario-logado.model';
import { TokenService } from './security/token.service';
import { AuthorityContext } from '../model/security/authority-context.model';

@Injectable({
  providedIn: 'root'
})
export class UsuarioLogadoService {

  private _usuarioLogado: UsuarioLogado = null;

  private _context: AuthorityContext;

  constructor(private tokenService: TokenService) {
    this.loadUsuarioLogado();
    this.tokenService.tokenChanged$.subscribe(() => this.onTokenChange());
  }

  private loadUsuarioLogado(): void {

    if (this.tokenService.getToken()) {

      const token = this.tokenService.getToken();
      const tokenData = this.tokenService.getDataFromToken();

      const usuario = new UsuarioLogado();

      if (tokenData.iss === TIPO_LOGIN_ENUM.PLATAFORMA) {

        usuario.loadUsuarioPlataforma(token, tokenData);

      } else if (tokenData.iss === TIPO_LOGIN_ENUM.IDP) {

        usuario.loadUsuarioIDP(token, tokenData);
      }

      this._usuarioLogado = usuario;
    }
  }

  private onTokenChange(): void {

    if (!this.isLoggedIn()) {
      this.loadUsuarioLogado();

    } else {
      const currentContext = this.context;
      this.loadUsuarioLogado();
      if (!this.context) {
        this.context = currentContext;
      }
    }
  }

  public logout(): void {
    const urLogout = this.getUrlLogout();
    this.tokenService.removeToken();
    window.location.href = urLogout;
  }

  private getUrlLogout(): string {

    let urlLogout: string;

    if (this._usuarioLogado) {

      if (this._usuarioLogado.isLoginIDP) {
        urlLogout = AppService.urlToSICONVService + '/?LLO=true';

      } else {
        urlLogout =
          AppService.urlToGovBr +
          '/logout?post_logout_redirect_uri=' +
          AppService.domainFrontEnd +
          '/medicao';
      }
    }

    return urlLogout;
  }

  public isLoggedIn(): boolean {
    return this._usuarioLogado !== null && typeof this._usuarioLogado !== undefined;
  }

  public hasPermission(profiles: Profile[], roles: Role[], permission: Permission[]): boolean {

    return this._usuarioLogado.hasProfile(profiles) &&
            (this._usuarioLogado.hasRole(roles, this._context) || this._usuarioLogado.hasPermission(permission, this._context));
  }

  public get usuarioLogado(): UsuarioLogado {
    return this._usuarioLogado;
  }

  public get context(): AuthorityContext {
    return this._context;
  }

  public set context(context: AuthorityContext) {
    this._context = context;
  }
}
