import { Contrato } from 'src/app/shared/model/contrato.model';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { ContratoService } from 'src/app/shared/services/contrato.service';
import { Medicao } from '../model/medicao.model';
import { MedicaoService } from '../services/medicao.service';

@Injectable({
  providedIn: 'root'
})
export class MedicaoResolverService implements Resolve<Medicao> {

  constructor (private contratoService: ContratoService,
                private medicaoService: MedicaoService,
                private router: Router) {}


  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Medicao> {

    // Obtém o id da Medicao Acumulada, caso não ecnontre que será a maioria dos casos,
    // obtém o id da Medicao
    let idMedicao = +route.paramMap.get('idMedicaoAcumulada');

    if (!idMedicao){
      idMedicao = +route.paramMap.get('idMedicao');
    }

    const contrato: Contrato = this.contratoService.contratoAtual;

    return this.medicaoService.consultarMedicao (idMedicao).pipe(
      catchError((error) => {
        if (error.status === 404) {
          this.router.navigate(['/url-not-found']);
        }

        throw new Error(`A Medição ${idMedicao} não existe para o Contrato ${contrato.id}.`);
      }),
      tap ((medicao: Medicao) =>  {

        if (contrato.id !== medicao.idContratoSiconv) {
          this.router.navigate(['/url-not-found']);
        }

        return medicao;

      }));
  }

}
