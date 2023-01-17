import { FrenteObra } from '../model/frente-obra.model';
import { Submeta } from './../model/submeta.model';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AppService } from 'src/app/shared/core/App.server';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Macrosservico } from '../model/macrosservico.model';
import { Servico, ValorServicoBM } from '../model/servico.model';

@Injectable({
  providedIn: 'root'
})
export class SubmetaService {

  constructor(private http: HttpClient) { }

  listarSubmetas(idMedicao: string): Observable<any[]> {
    let url = `${AppService.endpoint}/medicoes/${idMedicao}/submetas`;
    return this.http.get<any>(url);
  }

  consultarSubmeta(idMedicao: number, idSubmeta: number): Observable<Submeta> {
    return this.http.get<any>(`${AppService.endpoint}/medicoes/${idMedicao}/submetas/${idSubmeta}`)
      .pipe(
        map(retorno => {

          let sub: Submeta = Object.assign(new Submeta(), retorno.data);

          let frentesObras = sub.frentesObra.map(frenteObra => {

            let fo: FrenteObra = Object.assign(new FrenteObra(), frenteObra);

            if (fo.macroServicosView) {
              let macroServicos = fo.macroServicosView.map(macroServico => {

                let ms: Macrosservico = Object.assign(new Macrosservico(), macroServico);

                let servicos = ms.servicos.map(servico => {

                  let serv: Servico = Object.assign(new Servico(), servico);

                  serv.valoresPorIdMedicao = this.montarMapaMarcacoesServico(serv);

                  return serv;

                });
                ms.servicos = servicos

                return ms;
              });
              fo.macroServicosView = macroServicos;
            }

            return fo;
          });
          sub.frentesObra = frentesObras;

          return sub;
        })
      );
  }

  salvar(idMedicao: number, idSubmetaVrpl: number, versaoSubmeta: number, frentesObra: FrenteObra[]): Observable<any> {
    let ret: Observable<any>;

    const arg = { 'frentesObra': frentesObra, 'versao': versaoSubmeta };

    ret = this.http.put(`${AppService.endpoint}/medicoes/${idMedicao}/submetas/${idSubmetaVrpl}`, arg);

    return ret;

  }

  assinavel(idContrato: number, idMedicao: number, idSubmeta: number): Observable<boolean> {
    return this.http.get<any>(`${AppService.endpoint}/contratos/${idContrato}/medicoes/${idMedicao}/submetas/${idSubmeta}/assinavel`)
      .pipe(
        map(retorno => retorno.data)
      );
  }

  assinar(idContrato: number, idMedicao: number, idSubmetaVrpl: number, versaoSubmeta: number, frentesObra: FrenteObra[]): Observable<any> {
    let ret: Observable<any>;

    const arg = { 'frentesObra': frentesObra, 'versao': versaoSubmeta };

    ret = this.http.put(`${AppService.endpoint}/contratos/${idContrato}/medicoes/${idMedicao}/submetas/${idSubmetaVrpl}/assinatura`, arg);

    return ret;
  }

  excluir(idMedicao: number, idSubmetaVRPL: number): Observable<any> {
    let ret: Observable<any>;

    ret = this.http.delete(`${AppService.endpoint}/medicoes/${idMedicao}/submetas/${idSubmetaVRPL}`);

    return ret;
  }

  listarSubmetasPorContrato(idContrato: number): Observable<Submeta[]> {
    return this.http.get<any>(`${AppService.endpoint}/contratos/${idContrato}/submetas`)
      .pipe(map(lista => lista.data.map((e: Response) => {
        return Object.assign(new Submeta(), e);
      })));
  }

  private montarMapaMarcacoesServico (servico: Servico): Map < number,ValorServicoBM> {
    let mapa = new Map < number,ValorServicoBM>();
    let arr:any[][] = Object.entries(servico.valoresPorIdMedicao);
    for (let i=0;i<arr.length;i++){
      let key = arr[i][0];
      let value = Object.assign(new ValorServicoBM(arr[i][1]['qtdEmpresa'],
        arr[i][1]['qtdConvenente'],
        arr[i][1]['qtdConcedente']
      ));

      mapa.set (+key,value);
    }
      return mapa;
  }
}
