import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { UsuarioLogadoService } from '../services/usuario-logado.service';
import { AuthorityContext } from '../model/security/authority-context.model';

@Injectable({
  providedIn: 'root'
})
export class AuthorizationGuard implements CanActivate {

  constructor(
    private usuarioLogadoService: UsuarioLogadoService,
    private router: Router

  ) {}

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> | Promise<boolean> | boolean {

    let retorno = false;
    const idEmpresa = +this.getParameter ('idFornecedor', next);

    let _context: AuthorityContext;
    if (idEmpresa) {
      _context = new AuthorityContext (null, idEmpresa);

      retorno = this.usuarioLogadoService.usuarioLogado.hasPermission (next.data.permissions, _context);

    } else {
      const idProposta = +this.getParameter ('idProposta', next);
      _context = new AuthorityContext (idProposta, null);

      retorno = this.usuarioLogadoService.usuarioLogado.hasRole (next.data.roles, _context);

    }

    if (retorno) {
      return retorno;
    } else {
      this.router.navigate(['/not-authorized']);
    }

    return retorno;
  }


  private getParameter (paramName: string, next: ActivatedRouteSnapshot): string {

    if (next || next !== null) {
      const urlParam = next.paramMap.get(paramName);

      if (urlParam === null || !urlParam) {
        return this.getParameter (paramName, next.parent);
      } else {
        return urlParam;
      }
    } else {
      return undefined;
    }

  }


}
