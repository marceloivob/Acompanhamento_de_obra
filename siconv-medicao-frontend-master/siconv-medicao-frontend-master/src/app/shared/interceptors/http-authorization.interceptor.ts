import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AppService } from '../core/App.server';
import { UsuarioLogadoService } from '../services/usuario-logado.service';

@Injectable()
export class HttpAuthorizationInterceptor implements HttpInterceptor {

  constructor(private _usuarioLogadoService: UsuarioLogadoService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (req.url.includes(`${AppService.endpoint}`) && this._usuarioLogadoService.isLoggedIn()) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${this._usuarioLogadoService.usuarioLogado.token}`
        },
      });
    }
    return next.handle(req);
  }
}
