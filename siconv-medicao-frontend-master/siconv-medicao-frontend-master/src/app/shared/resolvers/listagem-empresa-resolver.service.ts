import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, Router, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Empresa } from '../model/empresa.model';
import { EmpresaService } from '../services/empresa.service';

@Injectable({
  providedIn: 'root'
})
export class ListagemEmpresaResolverService implements Resolve<Empresa[]> {

  constructor(private empresaService: EmpresaService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Empresa[]> {
    return this.empresaService.listarEmpresas().pipe(
      tap(empresas => {
        if (empresas.length === 1 && !this.empresaService.empresaAtual) {
          this.router.navigate(['preenchimento/empresa/', empresas[0].id]);
        }
      })
    );
  }
}
