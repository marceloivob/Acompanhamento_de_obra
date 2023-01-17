import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { UsuarioLogadoService } from '../services/usuario-logado.service';
import { PlataformaService } from '../services/security/plataforma.service';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationPlataformaGuard implements CanActivate {

  constructor(
    private usuarioLogadoService: UsuarioLogadoService,
    private plataformaService: PlataformaService,
    private router: Router
  ) {}

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> | Promise<boolean> | boolean {

    if (!this.usuarioLogadoService.isLoggedIn()) {

      this.plataformaService.initAuthorizationCodeFlow(state.url);
      return false;

    } else if (!this.usuarioLogadoService.usuarioLogado.isLoginPlataforma) {

      this.router.navigate(['not-authorized'], { skipLocationChange: true });
      return false;
    }

    return true;
  }
}
