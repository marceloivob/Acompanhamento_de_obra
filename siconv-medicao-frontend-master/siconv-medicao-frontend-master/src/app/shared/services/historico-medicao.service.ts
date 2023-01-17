import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AppService } from '../core/App.server';
import { DateUtil } from '../util/date-util';
import { HistoricoMedicao } from '../model/historico-medicao.model';

@Injectable({
  providedIn: 'root'
})
export class HistoricoMedicaoService {

  constructor(private http: HttpClient) { }

  listarHistoricoMedicoes(idContrato: number): Observable<any[]> {

    return this.http.get<any>(`${AppService.endpoint}/contratos/${idContrato}/historico`)
     .pipe(
       map(historicoMedicoes => historicoMedicoes.data.map(e => {

          const historicoMedicao = Object.assign(new HistoricoMedicao(), e);

          historicoMedicao.dataInicioObra = DateUtil.generateDateByUSAPattern (historicoMedicao.dataHora);

          return historicoMedicao;
       }))
    );

  }
}
