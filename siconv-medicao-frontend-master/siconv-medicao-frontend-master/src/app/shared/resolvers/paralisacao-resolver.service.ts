import { Contrato } from 'src/app/shared/model/contrato.model';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { ContratoService } from 'src/app/shared/services/contrato.service';
import { Paralisacao } from '../model/paralisacao.model';
import { ParalisacaoService } from '../services/paralisacao.service';

@Injectable({
  providedIn: 'root',
})
export class ParalisacaoResolverService implements Resolve<Paralisacao> {

  constructor(
    private _contratoService: ContratoService,
    private _paralisacaoService: ParalisacaoService,
    private _router: Router
  ) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Paralisacao> {

    const idParalisacao = +route.paramMap.get('idParalisacao');
    const contrato: Contrato = this._contratoService.contratoAtual;

    return this._paralisacaoService.consultar(idParalisacao).pipe(
      catchError((error) => {
        if (error.status === 404) {
          this._router.navigate(['/url-not-found']);
        }
        throw new Error('Não foi possível consultar a Paralisação ou ela não existe.');
      }),
      tap((paralisacao: Paralisacao) => {
        if (contrato.id !== paralisacao.idContratoSiconv) {
          this._router.navigate(['/url-not-found']);
          throw new Error(`A Paralisação ${idParalisacao} não existe para o Contrato ${contrato.id}.`);
        }
        return paralisacao;
      })
    );
  }
}
