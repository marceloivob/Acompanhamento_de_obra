import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Empresa } from 'src/app/shared/model/empresa.model';
import { CnpjPipe } from 'src/app/shared/pipes/cnpj.pipe';
import { EmpresaService } from 'src/app/shared/services/empresa.service';
import { SharedService } from 'src/app/shared/services/shared-service.service';

@Component({
  selector: 'app-configura-ctef',
  templateUrl: './configura-ctef.component.html'
})
export class ConfiguraCtefComponent implements OnInit {

  empresa: Empresa;

  constructor(private _sharedService: SharedService,
              private _empresaService: EmpresaService,
              private _cnpj: CnpjPipe,
              public _router: Router) { }

  ngOnInit() {

    this.empresa = this._empresaService.empresaAtual;

    this.emitirTitulo(this.empresa);

  }

  emitirTitulo(empresa: Empresa) {

    const tituloTela = {
      titulo: 'Dados Gerais',
      subtitulo: 'Configuração de Contratos de Execução e/ou Fornecimento',
      info: this._cnpj.transform(this.empresa.cnpj) + ' - ' + this.empresa.razaoSocial
    };

    this._sharedService.emitChange(tituloTela);
  }
}
