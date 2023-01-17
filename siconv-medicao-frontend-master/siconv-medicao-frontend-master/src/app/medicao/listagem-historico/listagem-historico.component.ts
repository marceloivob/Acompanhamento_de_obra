import { Empresa } from 'src/app/shared/model/empresa.model';
import { HistoricoMedicaoService } from './../../shared/services/historico-medicao.service';
import { HistoricoMedicao } from '../../shared/model/historico-medicao.model';
import { BaseComponent } from 'src/app/shared/util/base.component';
import { Component, Injector, Inject, LOCALE_ID } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { EmpresaService } from 'src/app/shared/services/empresa.service';
import { ContratoService } from 'src/app/shared/services/contrato.service';
import { RequiredAuthorizer } from 'src/app/shared/model/security/required-authorizer.model';
import { Contrato } from 'src/app/shared/model/contrato.model';
import { Profile } from '../../shared/model/security/profile.enum';
import { Permission } from 'src/app/shared/model/security/permission.enum';
import { formatDate } from '@angular/common';
import { DataExport } from 'src/app/shared/model/data-export';

@Component({
  selector: 'app-listagem-historico',
  templateUrl: './listagem-historico.component.html'
})
export class ListagemHistoricoComponent extends BaseComponent {

  data: HistoricoMedicao[];
  lista: any[];
  dataExport: any[] = [];
  export: DataExport;
  fileExportName = 'Histórico_das_Medições';

  constructor(
    private _router: Router,
    private _route: ActivatedRoute,
    private _contratoService: ContratoService,
    private _empresaService: EmpresaService,
    private _historicoMedicaoService: HistoricoMedicaoService,
    private injector: Injector,
    @Inject(LOCALE_ID) private locale: string
  ) {
    super(injector);
  }

  get contrato(): Contrato {
    return this._contratoService.contratoAtual;
  }

  get empresa(): Empresa {
    return this._empresaService.empresaAtual;
  }

  protected initializeComponent(): void {

    const columns = ['Data/Hora', 'Perfil', 'Responsável', 'Medição', 'Situação da Medição'];

    this._historicoMedicaoService.listarHistoricoMedicoes(this.contrato.id).subscribe(
      value => {
        this.data = value;
        this.data.forEach(element => {
          const linha = [];
          linha.push(formatDate(element.dataHora, 'dd/MM/yyyy HH:mm:ss', this.locale));
          linha.push((element.inPerfilResponsavel) ? element.inPerfilResponsavel.descricao : '');
          linha.push(element.nomeResponsavel);
          linha.push(element.nrSequencial);
          linha.push(element.inSituacao.descricao);
          this.dataExport.push(linha);
        });
        this.export = new DataExport(columns, this.dataExport);
      },
      error => {}
    );

  }

  loadPermissions(): Map<string, RequiredAuthorizer> {
    const profiles = [Profile.EMPRESA];

    return new Map([
      [
        'incluir',
        new RequiredAuthorizer(profiles, [], [Permission.INCLUIR_MEDICAO])
      ],
      [
        'editar',
        new RequiredAuthorizer(profiles, [], [Permission.EDITAR_MEDICAO])
      ],
      [
        'excluir',
        new RequiredAuthorizer(profiles, [], [Permission.EXCLUIR_MEDICAO])
      ],
      [
        'cancelarEnvioConvenente',
        new RequiredAuthorizer(profiles, [], [Permission.CANCELAR_ENVIO_MEDICAO_CONVENENTE])
      ]
    ]);
  }

  public getListaPaginada(listap) {
    this.lista = listap;
  }

  public navegarAbaMedicao() {
    this._router.navigate(['../../'], { relativeTo: this._route });
  }

  voltar() {
    const rootModule = this._route.snapshot.pathFromRoot[1].url[0].path;

    if (rootModule === 'preenchimento') {
      this._router.navigate(['/preenchimento/empresa', this.empresa.id]);

    } else {
      this._router.navigate(['/acompanhamento/proposta', this.contrato.propostaFk]);
    }
  }
}
