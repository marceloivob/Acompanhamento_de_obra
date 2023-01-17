import { Injectable } from '@angular/core';
import { EmpresaService } from '../services/empresa.service';
import { UsuarioLogadoService } from '../services/usuario-logado.service';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Empresa } from '../model/empresa.model';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { AuthorityContext } from '../model/security/authority-context.model';

@Injectable({
  providedIn: 'root'
})
export class EmpresaResolverService implements Resolve<Empresa> {

  constructor(
    private empresaService: EmpresaService,
    private usuarioLogadoService: UsuarioLogadoService
  ) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Empresa> {

    const idEmpresa = +route.paramMap.get('idFornecedor');

    return this.empresaService.consultarEmpresa(idEmpresa).pipe(
      tap(empresa => {
        this.empresaService.empresaAtual = empresa;
        this.usuarioLogadoService.context = new AuthorityContext(null, empresa.id);
      })
    );
  }
}
