import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Empresa } from 'src/app/shared/model/empresa.model';
import { SharedService } from 'src/app/shared/services/shared-service.service';

@Component({
  selector: 'app-listagem-empresas',
  templateUrl: './listagem-empresa.component.html',
  styleUrls: ['./listagem-empresa.component.scss']
})
export class ListagemEmpresaComponent implements OnInit {

  public empresas: Empresa[];

  constructor(
    private _sharedService: SharedService,
    private _router: Router,
    private _route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.empresas = this._route.snapshot.data['empresas'];

    const tituloTela = {
      titulo: 'Selecionar Empresa',
      subtitulo: 'Escolha uma empresa para iniciar o processo de medição'
    };

    this._sharedService.emitChange(tituloTela);
  }

  selecionarEmpresa(idEmpresa) {
    this._router.navigate(['../', idEmpresa], { relativeTo: this._route });
  }
}
