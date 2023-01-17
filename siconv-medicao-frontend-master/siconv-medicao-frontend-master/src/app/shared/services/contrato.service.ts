import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AppService } from 'src/app/shared/core/App.server';
import { Contrato } from '../model/contrato.model';
import { DateUtil } from '../util/date-util';

@Injectable({
  providedIn: 'root'
})
export class ContratoService {

  public contratoAtual: Contrato;

  constructor(private http: HttpClient) {}

  public listarContratos(idEmpresa: number): Observable<Contrato[]> {
    return this.http
      .get<any>(`${AppService.endpoint}/empresas/${idEmpresa}/contratos`)
      .pipe(map(contratos => contratos.data.map(e => Object.assign(new Contrato(), e))));
  }

  public consultarContrato(idContrato: number): Observable<Contrato> {
    return this.http.get<any>(`${AppService.endpoint}/contratos/${idContrato}`).pipe(
      map(retorno => {
        const contrato = Object.assign(new Contrato(), retorno.data);
        contrato.dtAssinatura = DateUtil.generateDateByUSAPattern(contrato.dtAssinatura);
        contrato.dtFimVigencia = DateUtil.generateDateByUSAPattern(contrato.dtFimVigencia);

        return contrato;
      })
    );
  }

  temSubmetasAExecutar(idContrato: number): Observable<boolean> {
    return this.http.get<any>(`${AppService.endpoint}/contratos/${idContrato}/temsubmetasaexecutar`).pipe(
      map (retorno => retorno.data)
    );
  }

  isContratoParalisado(idContrato: number): Observable<boolean> {
    return this.http.get<any>(`${AppService.endpoint}/contratos/${idContrato}/paralisado`).pipe(
      map (retorno => retorno.data)
    );
  }
}
