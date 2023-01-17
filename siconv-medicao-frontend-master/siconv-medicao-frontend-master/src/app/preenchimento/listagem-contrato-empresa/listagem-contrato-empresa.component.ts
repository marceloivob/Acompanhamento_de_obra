import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CnpjPipe } from 'src/app/shared/pipes/cnpj.pipe';
import { Contrato } from '../../shared/model/contrato.model';
import { Empresa } from '../../shared/model/empresa.model';
import { ContratoService } from '../../shared/services/contrato.service';
import { EmpresaService } from '../../shared/services/empresa.service';
import { SharedService } from '../../shared/services/shared-service.service';

@Component({
  selector: 'app-listagem-contrato-empresa',
  templateUrl: './listagem-contrato-empresa.component.html',
  styleUrls: ['./listagem-contrato-empresa.component.scss'],
})
export class ListagemContratoEmpresaComponent implements OnInit {
  contratos: Contrato[];

  constructor(
    private _sharedService: SharedService,
    private _empresaService: EmpresaService,
    private _contratoService: ContratoService,
    private _cnpj: CnpjPipe,
    public _router: Router,
    private _route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.emitirTitulo(this.empresa);
    this.listaContratos();
  }

  get empresa(): Empresa {
    return this._empresaService.empresaAtual;
  }

  emitirTitulo(empresa: Empresa) {
    let tituloTela = {
      titulo: 'Selecionar Contrato de Execução e/ou Fornecimento',
      subtitulo: 'Escolha um dos contratos abaixo para realizar medições.',
      info: this._cnpj.transform(this.empresa.cnpj) + ' - ' + this.empresa.razaoSocial,
    };

    this._sharedService.emitChange(tituloTela);
  }

  listaContratos() {
    this._contratoService.listarContratos(this.empresa.id).subscribe((value) => (this.contratos = value));
  }

  voltar() {
    this._router.navigate(['../../../'], { relativeTo: this._route });
  }
}
