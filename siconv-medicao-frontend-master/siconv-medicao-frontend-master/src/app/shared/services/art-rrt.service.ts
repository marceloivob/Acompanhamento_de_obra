import { DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AppService } from 'src/app/shared/core/App.server';
import { Submeta } from '../model/submeta.model';
import { ArtRrt } from '../model/art-rrt.model';

@Injectable({
  providedIn: 'root'
})
export class ArtRrtService {

  constructor(private http: HttpClient, private datePipe: DatePipe) { }

  listarArtRrt(idContrato: number): Observable<any[]> {
    return this.http.get<any>(`${AppService.endpoint}/contratos/${idContrato}/arts/`)
      .pipe(map(lista => lista.data.map(e => {
        return  Object.assign(new ArtRrt(), e);
      })));
  }

  consultarArtRrt(idArt: number): Observable<ArtRrt> {
    return this.http.get<any>(`${AppService.endpoint}/arts/${idArt}/`)
      .pipe(map(retorno => {

        return Object.assign(new ArtRrt(), retorno.data);
      }));
  }

  excluir(idArtRrt: number): Observable<any> {

    let retorno: Observable<any>;

    retorno = this.http.delete(`${AppService.endpoint}/arts/${idArtRrt}`);

    return retorno;

  }

  inativar(idArtRrt: number): Observable<any> {

    let retorno: Observable<any>;

    retorno = this.http.put(`${AppService.endpoint}/arts/${idArtRrt}/inativacao`, null);

    return retorno;

  }

  listarSubmetasContrato(idContrato: number): Observable<any[]> {
    return this.http.get<any>(`${AppService.endpoint}/contratos/${idContrato}/submetas`)
      .pipe(map(lista => lista.data.map(e => Object.assign(new Submeta(), e)))
      );
  }


  incluirArt(art: ArtRrt, contratoFk: number): Observable<any> {
    const formData: FormData = new FormData();
    formData.append('_charset_', 'utf-8');

    let url = `${AppService.endpoint}/contratos/${contratoFk}/arts`;

    formData.append('numero', art.numero.toString());
    formData.append('dataEmissao', this.datePipe.transform(art.dataEmissao, 'yyyy-MM-dd'));
    formData.append('tipo', art.tipo.toString());
    formData.append('idMedContratoRespTec', art.idMedContratoRespTec.toString());

    formData.append('anexo', art.arquivo);

    formData.append('submetas', JSON.stringify(art.submetas));



    return this.http.post(url, formData);

  }


  alterarArt(art: ArtRrt, idArt: number): Observable<any> {

    const formData: FormData = new FormData();
    formData.append('_charset_', 'utf-8');

    const url = `${AppService.endpoint}/arts/${idArt}`;

    formData.append('numero', art.numero.toString());
    formData.append('dataEmissao', this.datePipe.transform(art.dataEmissao, 'yyyy-MM-dd'));
    formData.append('tipo', art.tipo.toString());
    formData.append('idMedContratoRespTec', art.idMedContratoRespTec.toString());
    formData.append('anexo', art.arquivo);
    formData.append('versao', art.versao.toString());
    formData.append('submetas', JSON.stringify(art.submetas));

    return this.http.put(url, formData);

  }

}
