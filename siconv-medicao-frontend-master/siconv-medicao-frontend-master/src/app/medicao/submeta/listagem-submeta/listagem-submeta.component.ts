import { Profile } from '../../../shared/model/security/profile.enum';
import { Permission } from '../../../shared/model/security/permission.enum';
import { RequiredAuthorizer } from '../../../shared/model/security/required-authorizer.model';
import { BaseComponent } from 'src/app/shared/util/base.component';
import { TotalSubmetas } from '../../../shared/model/total-submetas.model';
import { SubmetaService } from '../../../shared/services/submeta.service';
import { Medicao } from '../../../shared/model/medicao.model';
import { Submeta } from '../../../shared/model/submeta.model';
import { Component, Input, Injector, LOCALE_ID, Inject, Output, EventEmitter } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { formatCurrency, formatNumber } from '@angular/common';
import { DataExport } from 'src/app/shared/model/data-export';
import { Role } from 'src/app/shared/model/security/role.enum';
import { ContratoService } from '../../../shared/services/contrato.service';
import { Contrato } from '../../../shared/model/contrato.model';

@Component({
  selector: 'app-listagem-submeta',
  templateUrl: './listagem-submeta.component.html',
  styleUrls: ['./listagem-submeta.component.scss']
})
export class ListagemSubmetaComponent extends BaseComponent {

  @Input() medicao: Medicao;

  @Output() loadComplete = new EventEmitter<{
    existeSubmetaAssinadaEmpresa: boolean;
    existeSubmetaAssinadaConvenente: boolean;
    existeSubmetaAssinadaConcedente: boolean;
  }>();

  // Dados de entrada do componente por rota
  acao: string;

  data: Submeta[];

  total: TotalSubmetas = new TotalSubmetas();

  lista: any[];
  fileExportName = 'Submetas';
  export: DataExport;
  dataExport: any[] = [];
  exibeEdicaoSubmeta: any;

  submetaEdicao: Submeta;
  submetaConcluida: boolean;


  constructor(private _submetaService: SubmetaService,
    public _router: Router,
    private _route: ActivatedRoute,
    @Inject(LOCALE_ID) private locale: string,
    private injector: Injector,
    private _contratoService: ContratoService) {
    super(injector);
  }

  loadPermissions(): Map<string, RequiredAuthorizer> {
    const profileEmpresa = [Profile.EMPRESA];
    const profileConvenente = [Profile.PROPONENTE];
    const profileConcedenteMandataria = [Profile.CONCEDENTE, Profile.MANDATARIA];
    return new Map([
      [
        'editar_submeta_empresa',
        new RequiredAuthorizer(profileEmpresa, [], [Permission.EDITAR_SUBMETA])
      ],
      [
        'editar_submeta_convenente',
        new RequiredAuthorizer(profileConvenente, [Role.FISCAL_CONVENENTE,
        Role.GESTOR_CONVENIO_CONVENENTE,
        Role.GESTOR_FINANCEIRO_CONVENENTE,
        Role.OPERADOR_FINANCEIRO_CONVENENTE], [])
      ],
      [
        'editar_submeta_concedente',
        new RequiredAuthorizer(profileConcedenteMandataria, [Role.FISCAL_CONCEDENTE,
        Role.GESTOR_CONVENIO_CONCEDENTE,
        Role.GESTOR_FINANCEIRO_CONCEDENTE,
        Role.OPERACIONAL_CONCEDENTE,
        Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA,
        Role.FISCAL_ACOMPANHAMENTO,
        Role.TECNICO_TERCEIRO], [])
      ]
    ]);
  }


  initializeComponent() {
    this.listarSubmetas();

    const url = this._route.snapshot.url;
    this.acao = url[url.length - 1].path;
  }

  private listarSubmetas() {

    const columns = ['               Descrição               ', 'Valor', 'Realizado Empresa Período R$', 'Realizado Empresa Período %',
      'Realizado Empresa Acumulado R$', 'Realizado Empresa Acumulado %', 'Realizado Convenente Período R$',
      'Realizado Convenente Período %', 'Realizado Convenente Acumulado R$', 'Realizado Convenente Acumulado %',
      'Realizado Concedente Período R$', 'Realizado Concedente Período %', 'Realizado Concedente Acumulado R$',
      'Realizado Concedente Acumulado %'];

    this._submetaService.listarSubmetas(this.medicao.id.toString()).subscribe(
      value => {

        this.data = value['data']['submetas'];
        this.total = value['data']['total'];

        this.data.forEach((element, index) => {
          const linha = [];
          element.indice = index;

          linha.push(element.descricao);
          linha.push(formatCurrency(element.valor, this.locale, ''));

          // Empresa
          linha.push(element.valorRealizadoEmpresa || element.valorRealizadoEmpresa === 0 ?
            formatCurrency(element.valorRealizadoEmpresa, this.locale, '').trim() : '');
          linha.push(element.percentualRealizadoEmpresa || element.percentualRealizadoEmpresa === 0 ?
            formatNumber(element.percentualRealizadoEmpresa, this.locale, '1.2-2') : '');
          linha.push(element.valorRealizadoAcumuladoEmpresa || element.valorRealizadoAcumuladoEmpresa === 0 ?
            formatCurrency(element.valorRealizadoAcumuladoEmpresa, this.locale, '').trim() : '');
          linha.push(element.percentualRealizadoAcumuladoEmpresa || element.percentualRealizadoAcumuladoEmpresa === 0 ?
            formatNumber(element.percentualRealizadoAcumuladoEmpresa, this.locale, '1.2-2') : '');
          // Convenente
          linha.push(element.valorRealizadoConvenente || element.valorRealizadoConvenente === 0 ?
            formatCurrency(element.valorRealizadoConvenente, this.locale, '').trim() : '');
          linha.push(element.percentualRealizadoConvenente || element.percentualRealizadoConvenente === 0 ?
            formatNumber(element.percentualRealizadoConvenente, this.locale, '1.2-2') : '');
          linha.push(element.valorRealizadoAcumuladoConvenente || element.valorRealizadoAcumuladoConvenente === 0 ?
            formatCurrency(element.valorRealizadoAcumuladoConvenente, this.locale, '').trim() : '');
          linha.push(element.percentualRealizadoAcumuladoConvenente || element.percentualRealizadoAcumuladoConvenente === 0 ?
            formatNumber(element.percentualRealizadoAcumuladoConvenente, this.locale, '1.2-2') : '');
          // Concedente
          linha.push(element.valorRealizadoConcedente || element.valorRealizadoConcedente === 0 ?
            formatCurrency(element.valorRealizadoConcedente, this.locale, '').trim() : '');
          linha.push(element.percentualRealizadoConcedente || element.percentualRealizadoConcedente === 0 ?
            formatNumber(element.percentualRealizadoConcedente, this.locale, '1.2-2') : '');
          linha.push(element.valorRealizadoAcumuladoConcedente || element.valorRealizadoAcumuladoConcedente === 0 ?
            formatCurrency(element.valorRealizadoAcumuladoConcedente, this.locale, '').trim() : '');
          linha.push(element.percentualRealizadoAcumuladoConcedente || element.percentualRealizadoAcumuladoConcedente === 0 ?
            formatNumber(element.percentualRealizadoAcumuladoConcedente, this.locale, '1.2-2') : '');

          this.dataExport.push(linha);
        });
        this.export = new DataExport(columns, this.dataExport);

        const existeSubmetaAssinadaEmpresa = this.data.find((sub: Submeta) => {
          return sub.situacaoEmpresa ? sub.situacaoEmpresa.codigo === 'ASS' : false;
        }) ? true : false;

        const existeSubmetaAssinadaConvenente = this.data.find((sub: Submeta) => {
          return sub.situacaoConvenente ? sub.situacaoConvenente.codigo === 'ASS' : false;
        }) ? true : false;

        const existeSubmetaAssinadaConcedente = this.data.find((sub: Submeta) => {
          return sub.situacaoConcedente ? sub.situacaoConcedente.codigo === 'ASS' : false;
        }) ? true : false;

        this.loadComplete.emit({ existeSubmetaAssinadaEmpresa, existeSubmetaAssinadaConvenente, existeSubmetaAssinadaConcedente });
      },
      error => { }
    );

  }

  public getListaPaginada(listap) {
    this.lista = listap;
  }

  get contrato(): Contrato {
    return this._contratoService.contratoAtual;
  }

  get msgBotaoEditarInativo(): string {

    let msgBotao = '';

    if (this.contrato.inAcompEvento) {

      if (this.canAccess('editar_submeta_empresa')) {
        msgBotao = 'Todos os eventos já foram concluídos.';

      } else if (this.canAccess('editar_submeta_convenente')) {
        msgBotao = 'Não há eventos para atestar.';

      } else if (this.canAccess('editar_submeta_concedente')) {
        msgBotao = 'Não há eventos para analisar.';
      }

    } else {

      if (this.canAccess('editar_submeta_empresa')) {
        msgBotao = 'Todos os serviços já foram medidos.';

      } else if (this.canAccess('editar_submeta_convenente')) {
        msgBotao = 'Não há serviços para atestar.';

      } else if (this.canAccess('editar_submeta_concedente')) {
        msgBotao = 'Não há serviços para analisar.';
      }
    }

    return msgBotao;
  }

  public exibeBotaoEditar(): boolean {
    return this.isEdit()
        && this.medicao.permiteComplementacaoValor != false
        && (this.canAccess('editar_submeta_empresa')
            || this.canAccess('editar_submeta_convenente')
            || this.canAccess('editar_submeta_concedente'));
  }

  public permiteMarcacao(submeta: Submeta): boolean {
    return (this.canAccess('editar_submeta_empresa') && submeta.permiteMarcacaoEmpresa)
           || (this.canAccess('editar_submeta_convenente') && submeta.permiteMarcacaoConvenente)
           || (this.canAccess('editar_submeta_concedente') && submeta.permiteMarcacaoConcedente);
  }

  public selecionarSubmeta(idSubmeta) {
    this._router.navigate(['./submeta', idSubmeta, 'editar'], { relativeTo: this._route });
  }

  prepararDetalhamento(idSubmeta: number) {
    this._router.navigate(['./submeta', idSubmeta, 'detalhar'], { relativeTo: this._route });
  }

  isEdit(): boolean {
    return this.acao === 'editar';
  }

}
