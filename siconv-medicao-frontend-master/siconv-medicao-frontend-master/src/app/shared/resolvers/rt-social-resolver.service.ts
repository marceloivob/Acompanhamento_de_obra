import { ResponsavelTecnicoSocialService } from './../services/responsavel-tecnico-social.service';
import { ResponsavelTecnicoSocial } from 'src/app/shared/model/responsavel-tecnico-social.model';
import { Contrato } from 'src/app/shared/model/contrato.model';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { ContratoService } from 'src/app/shared/services/contrato.service';

@Injectable({
  providedIn: 'root'
})
export class RtSocialResolverService implements Resolve<ResponsavelTecnicoSocial> {

  constructor (private contratoService: ContratoService,
                private rtSocialService: ResponsavelTecnicoSocialService,
                private router: Router) {}


  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<ResponsavelTecnicoSocial> {

    const idRespTecSocial = +route.paramMap.get('idRespTecSocial');
    const contrato: Contrato = this.contratoService.contratoAtual;

    return this.rtSocialService.consultarRTSocialPorId (idRespTecSocial).pipe(
      catchError((error) => {
        if (error.status === 404) {
          this.router.navigate(['/url-not-found']);
        }

        throw new Error(`O Responsavél Técnico Social ${idRespTecSocial} não existe para o Contrato ${contrato.id}.`);
      }),
      tap ((rtSocial: ResponsavelTecnicoSocial) =>  {

        if (contrato.id !== rtSocial.idContratoSiconv) {
          this.router.navigate(['/url-not-found']);
        }

        return rtSocial;

      }));
  }

}
