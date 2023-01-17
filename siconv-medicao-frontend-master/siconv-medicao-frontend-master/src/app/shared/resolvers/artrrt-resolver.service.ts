import { ArtRrtService } from './../services/art-rrt.service';
import { Contrato } from 'src/app/shared/model/contrato.model';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { ContratoService } from 'src/app/shared/services/contrato.service';
import { ArtRrt } from '../model/art-rrt.model';

@Injectable({
  providedIn: 'root'
})
export class ArtRrtResolverService implements Resolve<ArtRrt> {

  constructor (private contratoService: ContratoService,
                private artRrtService: ArtRrtService,
                private router: Router) {}


  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<ArtRrt> {

    const idArtRrt = +route.paramMap.get('idArtRrt');
    const contrato: Contrato = this.contratoService.contratoAtual;

    return this.artRrtService.consultarArtRrt (idArtRrt).pipe(
      catchError((error) => {
        if (error.status === 404) {
          this.router.navigate(['/url-not-found']);
        }

        throw new Error(`O ART ${idArtRrt} não existe para o Contrato ${contrato.id}.`);
      }),
      tap ((artRrt: ArtRrt) =>  {

        if (contrato.id !== artRrt.idContratoSiconv) {
          this.router.navigate(['/url-not-found']);
        }

        return artRrt;

      }));
  }

}
