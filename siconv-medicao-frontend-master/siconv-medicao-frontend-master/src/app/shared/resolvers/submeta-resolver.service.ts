import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, Router, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { Contrato } from 'src/app/shared/model/contrato.model';
import { ContratoService } from 'src/app/shared/services/contrato.service';
import { SubmetaService } from 'src/app/shared/services/submeta.service';
import { Submeta } from './../model/submeta.model';

@Injectable({
  providedIn: 'root'
})
export class SubmetaResolverService implements Resolve<Submeta> {

  constructor (private contratoService: ContratoService,
                private submetaService: SubmetaService,
                private router: Router) {}


  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Submeta> {

    // Obtém o id da Medicao Acumulada, caso não ecnontre que será a maioria dos casos,
    // obtém o id da Medicao
    let idMedicao = +route.paramMap.get('idMedicaoAcumulada');

    if (!idMedicao){
      idMedicao = +route.paramMap.get('idMedicao');
    }

    const idSubmeta = +route.paramMap.get('idSubmeta');
    const contrato: Contrato = this.contratoService.contratoAtual;

    return this.submetaService.consultarSubmeta (idMedicao, idSubmeta).pipe(
      catchError((error) => {
        if (error.status === 404) {
          this.router.navigate(['/url-not-found']);
        }

        throw new Error(`A Submeta ${idSubmeta} não existe para o Contrato ${contrato.id}.`);
      }),
      tap ((submeta: Submeta) =>  {

        if (contrato.id !== submeta.idContratoSiconv) {
          this.router.navigate(['/url-not-found']);
        }

        return submeta;

      }));
  }

}
