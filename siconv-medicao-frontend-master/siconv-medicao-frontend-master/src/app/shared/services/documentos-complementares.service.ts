import { DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AppService } from '../core/App.server';
import { DocumentosComplementares } from '../model/documentos-complementares.model';
import { Submeta } from '../model/submeta.model';

@Injectable({
  providedIn: 'root'
})
export class DocumentosComplementaresService {

  constructor(private http: HttpClient, private datePipe: DatePipe) { }

  consultarDocumentosComplementares(idContrato: number): Observable<any[]> {
    return this.http.get<any>(`${AppService.endpoint}/contratos/${idContrato}/documentoscomplementares`)
      .pipe(map(lista => lista.data
        .map(e => {
          return Object.assign(new DocumentosComplementares(), e);
        })));
  }

  excluir(idDocComplementar: number): Observable<any> {

    let retorno: Observable<any>;

    retorno = this.http.delete(`${AppService.endpoint}/documentoscomplementares/${idDocComplementar}`);

    return retorno;

  }

  bloquearDesbloquear(idDocComplementar: number, bloquear: boolean): Observable<any> {

    let url = `${AppService.endpoint}/documentoscomplementares/${idDocComplementar}/bloqueado`;

    return this.http.put(url, bloquear, { headers: { 'Content-Type': 'application/json' } });

  }

  listarSubmetasContrato(idContrato: number): Observable<any[]> {
    return this.http.get<any>(`${AppService.endpoint}/contratos/${idContrato}/submetas`)
    .pipe(map(lista => lista.data.map(e => Object.assign(new Submeta(), e))));
  }

  incluirDocCompl(docCompl: DocumentosComplementares, contratoFk: number): Observable<any> {
          const formData: FormData = new FormData();
          formData.append('_charset_', 'utf-8');

          let url = `${AppService.endpoint}/contratos/${contratoFk}/documentoscomplementares`;
          formData.append('anexo', docCompl.arquivo);
          formData.append('documentoComplementarDTO', JSON.stringify(docCompl));

          return this.http.post(url, formData);
  }

  alterarDocCompl(docCompl: DocumentosComplementares, idDocCompl: number): Observable<any> {
    const formData: FormData = new FormData();
    formData.append('_charset_', 'utf-8');

    let url = `${AppService.endpoint}/documentoscomplementares/${idDocCompl}`;
    formData.append('anexo', docCompl.arquivo);
    formData.append('documentoComplementarDTO', JSON.stringify(docCompl));

    return this.http.put(url, formData);
}

  consultarDocumentoComplementar(idDocCompl: number): Observable<any> {
    return this.http.get<any>(`${AppService.endpoint}/documentoscomplementares/${idDocCompl}`)
      .pipe(
        map( retorno => {
                         return Object.assign(new DocumentosComplementares(), retorno.data);
                        }
        )
      );
  }


}
