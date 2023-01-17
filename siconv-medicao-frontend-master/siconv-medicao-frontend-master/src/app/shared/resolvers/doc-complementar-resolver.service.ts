import { Contrato } from 'src/app/shared/model/contrato.model';
import { DocumentosComplementaresService } from 'src/app/shared/services/documentos-complementares.service';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { DocumentosComplementares } from 'src/app/shared/model/documentos-complementares.model';
import { Observable } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { ContratoService } from 'src/app/shared/services/contrato.service';

@Injectable({
  providedIn: 'root'
})
export class DocComplementarResolverService implements Resolve<DocumentosComplementares> {

  constructor(private contratoService: ContratoService,
    private documentoscomplementaresService: DocumentosComplementaresService,
    private router: Router) { }


  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<DocumentosComplementares> {

    const idDocCompl = +route.paramMap.get('idDocCompl');
    const contrato: Contrato = this.contratoService.contratoAtual;

    return this.documentoscomplementaresService.consultarDocumentoComplementar(idDocCompl).pipe(
      catchError((error) => {
        if (error.status === 404) {
          this.router.navigate(['/url-not-found']);
        }

        throw new Error(`O Documento ${idDocCompl} não existe para o Contrato ${contrato.id}.`);
      }),
      tap((docComplementar: DocumentosComplementares) => {

        if (contrato.id !== docComplementar.idContratoSiconv) {
          this.router.navigate(['/url-not-found']);
        }

        return docComplementar;

      }));
  }

}
