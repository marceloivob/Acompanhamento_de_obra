import { RegistroProfissional } from '../model/registro-profissional.model';
import { Injectable } from '@angular/core';
import { AppService } from 'src/app/shared/core/App.server';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ResponsavelTecnico } from '../model/responsavel-tecnico.model';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ResponsavelTecnicoService {

  constructor(private http: HttpClient) { }

  listarResponsavelTecnico(idContrato: number): Observable<any[]> {
    return this.http.get<any>(`${AppService.endpoint}/responsavel/listar/${idContrato}`)
      .pipe(
        map(
          resptecnicos => resptecnicos.data.map(e => {
            const ret = Object.assign(new ResponsavelTecnico(), e);

            ret.registrosProfissional = ret.registrosProfissional.map(rp => Object.assign(new RegistroProfissional(), rp));

            return ret;
          }))
      );
  }

  listarResponsavelTecnicoPorTipo(idContrato: number, tipo: string): Observable<any[]> {
    return this.http.get<any>(`${AppService.endpoint}/responsavel/listar/${idContrato}/tipo/${tipo}`)
      .pipe(
        map(resptecnicos => resptecnicos.data.map(e => Object.assign(new ResponsavelTecnico(), e)))
      );
  }

  salvar(responsavelTecnico: ResponsavelTecnico, idContrato: number): Observable<any> {

    let ret: Observable<any>;

    if (responsavelTecnico.id) {
      ret = this.http.put(`${AppService.endpoint}/contratos/${idContrato}/responsavel/${responsavelTecnico.id}`, responsavelTecnico);
    } else {
      ret = this.http.post(`${AppService.endpoint}/contratos/${idContrato}/responsavel`, responsavelTecnico);
    }

    return ret;

  }

  excluir(idMedContratoRespTec: number): Observable<any> {

    let retorno: Observable<any>;

    retorno = this.http.delete(`${AppService.endpoint}/responsavel/${idMedContratoRespTec}`);

    return retorno;

  }

  consultarResponsavelTecnicoPorTipo(cpf: string, tipo: string, idContrato: number, validate?: boolean): Observable<ResponsavelTecnico> {
    let queryParam = '';
    if (typeof validate !== 'undefined') {
      queryParam = `?validate=${validate.valueOf()}`;
    }
    return this.http.get<any>(`${AppService.endpoint}/responsavel/${cpf}/tipo/${tipo}/contrato/${idContrato}${queryParam}`)
      .pipe(
        map(responsavelTecnicoAPI => {
          return Object.assign(new ResponsavelTecnico(), responsavelTecnicoAPI.data);
        })
      );
  }

  consultarResponsavelTecnico(idContratoRt: number): Observable<ResponsavelTecnico> {

    return this.http.get<any>(`${AppService.endpoint}/responsavelTecnico/${idContratoRt}`)
      .pipe(
        map(responsavelTecnicoAPI => {
          return Object.assign(new ResponsavelTecnico(), responsavelTecnicoAPI.data);
        })
      );
  }
}
