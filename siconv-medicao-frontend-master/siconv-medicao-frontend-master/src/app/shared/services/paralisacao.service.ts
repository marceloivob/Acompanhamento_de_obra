import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AppService } from '../core/App.server';
import { map } from 'rxjs/operators';
import { DateUtil } from '../util/date-util';
import { Paralisacao } from '../model/paralisacao.model';

@Injectable({
  providedIn: 'root',
})
export class ParalisacaoService {

  constructor(private http: HttpClient) {}

  incluir(paralisacao: Paralisacao, idContrato: number): Observable<any> {
    const formData: FormData = new FormData();
    formData.append('_charset_', 'utf-8');
    const { anexos, ...dto } = paralisacao; // Remove anexos do DTO
    formData.append('paralisacaoDTO', JSON.stringify(dto));

    for (let index = 0; index < paralisacao.anexos.length; index++) {
      formData.append(index + '', paralisacao.anexos[index].arquivo);
    }

    const url = `${AppService.endpoint}/contratos/${idContrato}/paralisacoes`;

    return this.http.post(url, formData);
  }

  alterar(paralisacao: Paralisacao): Observable<any> {
    const formData: FormData = new FormData();
    formData.append('_charset_', 'utf-8');
    const { anexos, ...dto } = paralisacao; // Remove anexos do DTO
    formData.append('paralisacaoDTO', JSON.stringify(dto));

    for (let index = 0; index < paralisacao.anexos.length; index++) {
      if (paralisacao.anexos[index].id) {
        formData.append('id=' + paralisacao.anexos[index].id, paralisacao.anexos[index].nmArquivo);
      } else {
        formData.append(index + '', paralisacao.anexos[index].arquivo);
      }
    }

    const url = `${AppService.endpoint}/paralisacoes/${paralisacao.id}`;

    return this.http.put(url, formData);
  }

  listar(idContrato: number): Observable<any[]> {
    return this.http.get<any>(`${AppService.endpoint}/contratos/${idContrato}/paralisacoes`).pipe(
      map((resp) =>
        resp.data.map((e) => {
          const paralisacao = Object.assign(new Paralisacao(), e);

          paralisacao.dataInicio = DateUtil.generateDateByUSAPattern(paralisacao.dataInicio);
          paralisacao.dataFim = DateUtil.generateDateByUSAPattern(paralisacao.dataFim);

          return paralisacao;
        })
      )
    );
  }

  consultar(idParalisacao: number): Observable<Paralisacao> {
    return this.http.get<any>(`${AppService.endpoint}/paralisacoes/${idParalisacao}`).pipe(
      map((retorno) => {
        const paralisacao = Object.assign(new Paralisacao(), retorno.data);

        paralisacao.dataInicio = DateUtil.generateDateByUSAPattern(paralisacao.dataInicio);
        paralisacao.dataFim = DateUtil.generateDateByUSAPattern(paralisacao.dataFim);

        return paralisacao;
      })
    );
  }

  excluir(idParalisacao: number): Observable<any> {
    let retorno: Observable<any>;

    retorno = this.http.delete(`${AppService.endpoint}/paralisacoes/${idParalisacao}`);

    return retorno;
  }
}
