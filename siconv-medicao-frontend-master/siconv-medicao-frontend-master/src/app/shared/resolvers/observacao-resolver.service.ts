import { ObservacaoService } from './../services/observacao.service';
import { Observacao } from './../model/observacao.model';
import { Contrato } from 'src/app/shared/model/contrato.model';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { ContratoService } from 'src/app/shared/services/contrato.service';

@Injectable({
  providedIn: 'root'
})
export class ObservacaoResolverService implements Resolve<Observacao> {

  constructor (private contratoService: ContratoService,
                private observacaoService: ObservacaoService,
                private router: Router) {}


  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Observacao> {

    const idMedicao = +route.paramMap.get('idMedicao');
    const idObservacao = +route.paramMap.get('idObservacao');
    const contrato: Contrato = this.contratoService.contratoAtual;

    return this.observacaoService.consultarObservacao (idMedicao, idObservacao).pipe(
      catchError((error) => {
        if (error.status === 404) {
          this.router.navigate(['/url-not-found']);
        }

        throw new Error(`A Observação ${idObservacao} não existe para o Contrato ${contrato.id}.`);
      }),
      tap ((observacao: Observacao) =>  {

        if (contrato.id !== observacao.idContratoSiconv) {
          this.router.navigate(['/url-not-found']);
        }

        return observacao;

      }));
  }

}
