import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { map } from 'rxjs/operators';
import { AcompanhamentoObra } from '../model/acompanhamento-obra.model';
import { AppService } from '../core/App.server';
import { TipoInstrumento } from '../model/tipo-instrumento.model';

@Injectable({
  providedIn: 'root'
})
export class AcompanhamentoConvenenteService {

  constructor(private http: HttpClient) { }

  consultarContratosLotes(idProposta: number): Observable<AcompanhamentoObra> {
    return this.http.get<any>(`${AppService.endpoint}/propostas/${idProposta}/contratoslotes`)
      .pipe( map( retorno => {
        const acompObra = Object.assign(new AcompanhamentoObra(), retorno.data);
        acompObra.tipoInstrumento = Object.assign(new TipoInstrumento(), acompObra.tipoInstrumento);
        return acompObra;
      }));
  }

}
