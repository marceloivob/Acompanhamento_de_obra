import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AppService } from '../core/App.server';
import { ResponsavelTecnicoSocial } from '../model/responsavel-tecnico-social.model';

@Injectable({
  providedIn: 'root'
})
export class ResponsavelTecnicoSocialService {

  constructor(private http: HttpClient) { }

  listarResponsavelTecnicoSocial(idContratoSiconv: number): Observable<ResponsavelTecnicoSocial[]> {
    return this.http.get<any>(`${AppService.endpoint}/contratos/${idContratoSiconv}/responsaveissocial`)
    .pipe(map(rtsocial => rtsocial.data
      .map((e: any) => {
        return Object.assign(new ResponsavelTecnicoSocial(), e);
      })));
  }

  consultarRTSocialPorId(id: number): Observable<ResponsavelTecnicoSocial> {
    return this.http.get<any>(`${AppService.endpoint}/responsaveissocial/${id}`)
      .pipe(map(rtsocial => Object.assign(new ResponsavelTecnicoSocial(), rtsocial.data)));
  }

  // Recupera o RTS caso o CPF/Tipo esteja registrado como RTS para o CTEF em questão
  async buscarRTScomCPFRegistradoCTEF(idContratoSiconv: number, cpf: string, tipo: string): Promise<ResponsavelTecnicoSocial> {
    const listaRTS = await this.listarResponsavelTecnicoSocial(idContratoSiconv).toPromise();
    const respTecSocial = listaRTS.find((rts: ResponsavelTecnicoSocial) => rts.responsavelTecnico.cpf === cpf && rts.tipo.codigo === tipo);
    return respTecSocial ? respTecSocial : undefined;
  }

  consultarResponsavelTecnicoElegivel(idContratoSiconv: number, cpf: string, tipo: string): Observable<ResponsavelTecnicoSocial> {
    return this.http.get<any>(`${AppService.endpoint}/contratos/${idContratoSiconv}/responsaveissocial/elegivel?cpf=${cpf}&tipo=${tipo}`)
      .pipe(map(rtsocial => {
        const rts = new ResponsavelTecnicoSocial();
        rts.responsavelTecnico = Object.assign({}, rtsocial.data);
        rts.id = rtsocial.data['idContratoResponsavelTecnicoSocial'];
        return rts;
      }));
  }

  salvar(rts: ResponsavelTecnicoSocial, idContratoSiconv: number): Observable<any> {
    if (!rts.id) {
      return this.http.post(`${AppService.endpoint}/contratos/${idContratoSiconv}/responsaveissocial`, this.toFormData(rts));
    } else {
      return this.http.put(`${AppService.endpoint}/responsaveissocial/${rts.id}`, this.toFormData(rts));
    }
  }

  private toFormData(rts: ResponsavelTecnicoSocial): FormData {
    const formData: FormData = new FormData();
    formData.append('_charset_', 'utf-8');
    formData.append('anexo', rts.arquivo);
    // Retira o anexo do objeto ResponsavelTecnicoSocial pois o backend não consegue deserializar o arquivo
    rts.arquivo = [];
    formData.append('contratoResponsavelTecnicoSocialDTO', JSON.stringify(rts));
    return formData;
  }

  excluir(idResponsavelContrato: number): Observable<any> {
    return this.http.delete(`${AppService.endpoint}/responsaveissocial/${idResponsavelContrato}`);
  }

  inativar(idResponsavelContrato: number): Observable<any> {
    return this.http.put(`${AppService.endpoint}/responsaveissocial/${idResponsavelContrato}/inativacao`, null);
  }
}
