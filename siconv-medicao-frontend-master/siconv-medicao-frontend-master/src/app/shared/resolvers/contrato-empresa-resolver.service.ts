import { Injectable } from '@angular/core';
import { Contrato } from '../model/contrato.model';
import { Empresa } from '../model/empresa.model';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { EmpresaService } from '../services/empresa.service';
import { ContratoService } from '../services/contrato.service';
import { flatMap, map, tap, catchError } from 'rxjs/operators';
import { UsuarioLogadoService } from '../services/usuario-logado.service';
import { AuthorityContext } from '../model/security/authority-context.model';

@Injectable({
  providedIn: 'root'
})
export class ContratoEmpresaResolverService implements Resolve<{ contrato: Contrato; empresa: Empresa }> {
  constructor(
    private contratoService: ContratoService,
    private empresaService: EmpresaService,
    private usuarioLogadoService: UsuarioLogadoService,
    private router: Router
  ) {}

  resolve(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<{ contrato: Contrato; empresa: Empresa }> {
    const idContrato = route.paramMap.get('idContrato');
    const idFornecedor = route.paramMap.get('idFornecedor');
    const idProposta = route.paramMap.get('idProposta');

    return this.contratoService.consultarContrato(+idContrato).pipe(
      catchError(() => {
        this.router.navigate(['/url-not-found']);
        throw new Error('Não foi possível consultar o contrato ou ele não existe.');
      }),
      tap(contrato => {
        if (idFornecedor && +idFornecedor !== contrato.fornecedorId) {
          this.router.navigate(['/url-not-found']);
          throw new Error('Contrato não associado ao fornecedor informado.');
        }

        if (idProposta && +idProposta !== contrato.propostaFk) {
          this.router.navigate(['/url-not-found']);
          throw new Error('Contrato não associado à proposta informada.');
        }
      }),
      flatMap(contrato =>
        this.consultarEmpresa(contrato.fornecedorId).pipe(
          map(empresa => {
            const resolvedData = { contrato, empresa };

            this.contratoService.contratoAtual = resolvedData.contrato;
            this.empresaService.empresaAtual = resolvedData.empresa;
            this.usuarioLogadoService.context = new AuthorityContext(
              resolvedData.contrato.propostaFk,
              resolvedData.empresa.id
            );

            return resolvedData;
          })
        )
      )
    );
  }

  private consultarEmpresa(idFornecedor: number): Observable<Empresa> {
    if (this.empresaService.empresaAtual != null &&
          this.empresaService.empresaAtual.id === idFornecedor) {
      return of(this.empresaService.empresaAtual);
    } else {
      return this.empresaService.consultarEmpresa(idFornecedor);
    }
  }
}
