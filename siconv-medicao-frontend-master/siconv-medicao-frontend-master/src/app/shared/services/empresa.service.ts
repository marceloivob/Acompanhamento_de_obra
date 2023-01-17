import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AppService } from 'src/app/shared/core/App.server';
import { Empresa } from 'src/app/shared/model/empresa.model';

@Injectable({
  providedIn: 'root'
})
export class EmpresaService {

  public empresaAtual: Empresa;

  constructor(private http: HttpClient) {}

  public listarEmpresas(): Observable<Empresa[]> {
    return this.http
      .get<any>(`${AppService.endpoint}/empresas/`)
      .pipe(map(empresas => empresas.data.map(e => Object.assign(new Empresa(), e))));
  }

  public consultarEmpresa(idFornecedor: number): Observable<Empresa> {
    return this.http
      .get<any>(`${AppService.endpoint}/empresas/${idFornecedor}`)
      .pipe(map(retorno => Object.assign(new Empresa(), retorno.data)));
  }
}
