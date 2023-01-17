import { Injectable } from '@angular/core';
import { Observacao } from '../model/observacao.model';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AppService } from '../core/App.server';
import { map } from 'rxjs/operators';
import { DateUtil } from '../util/date-util';

@Injectable({
  providedIn: 'root'
})
export class ObservacaoService {

  constructor(private http: HttpClient) { }

  incluirObservacao(observacao: Observacao, idMedicao: string): Observable<any> {

    const formData: FormData = new FormData();
    formData.append('_charset_', 'utf-8');
    formData.append('observacaoDTO', JSON.stringify(observacao));

    for (let index = 0; index < observacao.anexos.length; index++) {
      formData.append(index + '', observacao.anexos[index].arquivo);
    }

    const url = `${AppService.endpoint}/medicoes/${idMedicao}/observacoes`;
    return this.http.post(url, formData);
  }

  alterarObservacao(observacao: Observacao, idMedicao: string): Observable<any> {

      const formData: FormData = new FormData();
      formData.append('_charset_', 'utf-8');
      formData.append('observacaoDTO', JSON.stringify(observacao));

      for (let index = 0; index < observacao.anexos.length; index++) {

        if (observacao.anexos[index].id) {
          formData.append('id=' + observacao.anexos[index].id , observacao.anexos[index].nmArquivo);
        } else {
          formData.append(index + '', observacao.anexos[index].arquivo);
        }
      }

      const url = `${AppService.endpoint}/medicoes/${idMedicao}/observacoes/${observacao.id}`;
      return this.http.put(url, formData);
  }

  listarObservacoes(idMedicao: number, medicoesAgrupadas: boolean): Observable<any[]> {
    let param = '';
    if (medicoesAgrupadas) {
      param = '?medicoesAgrupadas=true';
    }

    return this.http.get<any>(`${AppService.endpoint}/medicoes/${idMedicao}/observacoes${param}`)
      .pipe(
        map(observacoes => observacoes.data.map(e => {

          const observacao = Object.assign(new Observacao(), e);

          observacao.dtRegistro = DateUtil.generateDateByUSAPattern(observacao.dtRegistro);

          return observacao;
        }))
      );

  }

  consultarObservacao(idMedicao: number, idObservacao: number): Observable<Observacao> {
    return this.http.get<any>(`${AppService.endpoint}/medicoes/${idMedicao}/observacoes/${idObservacao}`)
      .pipe(
        map(retorno => {
          const observacao = Object.assign(new Observacao(), retorno.data);
          observacao.dtRegistro = DateUtil.generateDateByUSAPattern(observacao.dtRegistro);
          return observacao;
        })
      );
  }

  excluirObservacao(idMedicao: number, idObservacao: number): Observable<any> {

    let retorno: Observable<any>;

    retorno = this.http.delete(`${AppService.endpoint}/medicoes/${idMedicao}/observacoes/${idObservacao}`);

    return retorno;

  }

  ativarAnexoObservacao(idObservacao: number, idAnexo: number, idMedicao: number): Observable<any> {

    return this.http.put(`${AppService.endpoint}/medicoes/${idMedicao}/observacoes/${idObservacao}/anexos/${idAnexo}/ativa`, null);

  }

  inativarAnexoObservacao(idObservacao: number, idAnexo: number, idMedicao: number): Observable<any> {

    return this.http.put(`${AppService.endpoint}/medicoes/${idMedicao}/observacoes/${idObservacao}/anexos/${idAnexo}/inativa`, null);

  }


}
