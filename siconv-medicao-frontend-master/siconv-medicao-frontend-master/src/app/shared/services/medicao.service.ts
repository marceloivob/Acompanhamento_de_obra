import { DateUtil } from '../util/date-util';
import { Medicao } from '../model/medicao.model';
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { AppService } from 'src/app/shared/core/App.server';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { MedicaoAgrupada } from '../model/medicao-agrupada.model';

@Injectable({
  providedIn: 'root'
})
export class MedicaoService {

  constructor(private http: HttpClient) { }

  listarMedicoes(idContrato: number): Observable<any[]> {

    return this.http.get<any>(`${AppService.endpoint}/contratos/${idContrato}/medicoes`)
      .pipe(
        map(medicoes => medicoes.data.map(e => {

          const medicao = Object.assign(new Medicao(), e);

          medicao.dataInicioObra = DateUtil.generateDateByUSAPattern(medicao.dataInicioObra);
          medicao.dataInicio = DateUtil.generateDateByUSAPattern(medicao.dataInicio);
          medicao.dataFim = DateUtil.generateDateByUSAPattern(medicao.dataFim);
          medicao.dataVistoriaExtra = DateUtil.generateDateByUSAPattern(medicao.dataVistoriaExtra);

          return medicao;
        }))
      );

  }

  consultarMedicao(idMedicao: number): Observable<Medicao> {
    return this.http.get<any>(`${AppService.endpoint}/medicoes/${idMedicao}`)
      .pipe(
        map(retorno => {
          const medicao = Object.assign(new Medicao(), retorno.data);
          medicao.dataInicioObra = DateUtil.generateDateByUSAPattern(medicao.dataInicioObra);
          medicao.dataInicio = DateUtil.generateDateByUSAPattern(medicao.dataInicio);
          medicao.dataFim = DateUtil.generateDateByUSAPattern(medicao.dataFim);
          medicao.dataVistoriaExtra = DateUtil.generateDateByUSAPattern(medicao.dataVistoriaExtra);

          return medicao;
        })
      );
  }

  /**
   * Salvar uma Medição
   *
   * @param medicao
   *
   */
  salvarMedicao(medicao: Medicao, idContrato: number): Observable<any> {

    let ret: Observable<any>;

    if (medicao.id) {
      ret = this.http.put(`${AppService.endpoint}/medicoes/${medicao.id}`, medicao);
    } else {
      ret = this.http.post(`${AppService.endpoint}/contratos/${idContrato}/medicoes`, medicao);
    }

    return ret;
  }

  /**
   * Retorna a última de Medição realizada no contrato se houver alguma medição.
   *
   * @param idContrato
   *
   */
  obterUltimaMedicao(idContrato: number): Observable<Medicao> {
    return this.listarMedicoes(idContrato).pipe(
      map(medicoes => {
        if (medicoes.length > 0) {
          return medicoes[medicoes.length - 1];
        }
      })
    )
  }

  enviarMedicao(medicao: Medicao, idMedicao: number): Observable<any> {
    return this.http.put(`${AppService.endpoint}/medicoes/${idMedicao}/envio`, medicao);
  }

  atestarMedicao(medicao: Medicao, idMedicao: number): Observable<any> {
    return this.http.put(`${AppService.endpoint}/medicoes/${idMedicao}/ateste`, medicao);
  }

  solicitarComplementoEmpresa(medicao: Medicao, idMedicao: number): Observable<any> {
    return this.http.put(`${AppService.endpoint}/medicoes/${idMedicao}/complementacaoempresa`, medicao);
  }

  solicitarComplementoConvenente(medicao: Medicao, idMedicao: number): Observable<any> {
    return this.http.put(`${AppService.endpoint}/medicoes/${idMedicao}/complementacaoconvenente`, medicao);
  }

  permiteSolicitarComplemetacao(idMedicao: number): Observable<boolean> {
    return this.http.get<any>(`${AppService.endpoint}/medicoes/${idMedicao}/permitecomplementacao`)
      .pipe(
        map(retorno => retorno.data)
      );
  }

  excluir(idMedicao: number): Observable<any> {
    return this.http.delete(`${AppService.endpoint}/medicoes/${idMedicao}`);
  }

  cancelarEnvioConvenente(idMedicao: number): Observable<any> {
    return this.http.put(`${AppService.endpoint}/medicoes/${idMedicao}/cancelamentoenvioconvenente`, null);
  }

  cancelarAteste(idMedicao: number): Observable<any> {
    return this.http.put(`${AppService.endpoint}/medicoes/${idMedicao}/cancelamentoenvioconcedente`, null);
  }

  cancelarEnvioParaComplementacao(idMedicao: number): Observable<any> {
    return this.http.put(`${AppService.endpoint}/medicoes/${idMedicao}/cancelamentoenviocomplementacao`, null);
  }

  iniciarAteste(idMedicao: number): Observable<any> {
    return this.http.put(`${AppService.endpoint}/medicoes/${idMedicao}/inicioateste`, null);
  }

  iniciarComplementacao(idMedicao: number): Observable<any> {
    return this.http.put(`${AppService.endpoint}/medicoes/${idMedicao}/iniciocomplementacao`, null);
  }

  iniciarAnalise(idMedicao: number): Observable<any> {
    return this.http.put(`${AppService.endpoint}/medicoes/${idMedicao}/inicioanalise`, null);
  }

  salvarMedicaoConcedenteMandataria(medicao: Medicao, idMedicao: number): Observable<Medicao> {

    return this.http.put<any>(`${AppService.endpoint}/medicoes/${idMedicao}/concedentemandataria`, medicao)
      .pipe(
        map(retorno => {
          const medicao = Object.assign(new Medicao(), retorno.data);
          medicao.dataInicioObra = DateUtil.generateDateByUSAPattern(medicao.dataInicioObra);
          medicao.dataInicio = DateUtil.generateDateByUSAPattern(medicao.dataInicio);
          medicao.dataFim = DateUtil.generateDateByUSAPattern(medicao.dataFim);
          medicao.dataVistoriaExtra = DateUtil.generateDateByUSAPattern(medicao.dataVistoriaExtra);

          return medicao;
        })
      );
  }

  aceitarMedicao(medicao: Medicao, idMedicao: number): Observable<any> {
    return this.http.put(`${AppService.endpoint}/medicoes/${idMedicao}/aceite`, medicao);
  }

  cancelarAceite(idMedicao: number): Observable<any> {
    return this.http.put(`${AppService.endpoint}/medicoes/${idMedicao}/cancelaaceite`, null);
  }

  listarMedicoesAgrupadas (idMedicaoAgrupadora: number, submetasPreenchidas?: boolean): Observable<MedicaoAgrupada[]> {

    let params = new HttpParams();
    params=params.append('submetasPreenchidas', <string><any>submetasPreenchidas);

    return this.http.get<any>(`${AppService.endpoint}/medicoes/${idMedicaoAgrupadora}/listaagrupadas`,{params})
    .pipe(map(retorno =>
      retorno.data.map(e => Object.assign(new MedicaoAgrupada(), e))));
  }
}
