import { ResponsavelTecnicoService } from '../services/responsavel-tecnico.service';
import { Contrato } from 'src/app/shared/model/contrato.model';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { ContratoService } from 'src/app/shared/services/contrato.service';
import { ResponsavelTecnico } from '../model/responsavel-tecnico.model';

@Injectable({
  providedIn: 'root'
})
export class RtResolverService implements Resolve<ResponsavelTecnico> {

  constructor (private contratoService: ContratoService,
                private rtService: ResponsavelTecnicoService,
                private router: Router) {}


  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<ResponsavelTecnico> {

    const idRt = +route.paramMap.get('idRt');
    const contrato: Contrato = this.contratoService.contratoAtual;

    return this.rtService.consultarResponsavelTecnico (idRt).pipe(
      catchError((error) => {
        if (error.status === 404) {
          this.router.navigate(['/url-not-found']);
        }

        throw new Error(`O Responsavél Técnico ${idRt} não existe para o Contrato ${contrato.id}.`);
      }),
      tap ((rt: ResponsavelTecnico) =>  {

        if (contrato.id !== rt.idContratoSiconv) {
          this.router.navigate(['/url-not-found']);
        }

        return rt;

      }));
  }

}
