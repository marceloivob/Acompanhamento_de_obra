import { MedicaoService } from 'src/app/shared/services/medicao.service';
import { formatCurrency, formatNumber, DatePipe } from '@angular/common';
import { Component, Inject, Injector, Input, LOCALE_ID, QueryList, ViewChildren } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Contrato } from 'src/app/shared/model/contrato.model';
import { DataExport } from 'src/app/shared/model/data-export';
import { Medicao } from 'src/app/shared/model/medicao.model';
import { Permission } from 'src/app/shared/model/security/permission.enum';
import { Profile } from 'src/app/shared/model/security/profile.enum';
import { RequiredAuthorizer } from 'src/app/shared/model/security/required-authorizer.model';
import { ListagemExpansivelComponent } from 'src/app/shared/util/listagem-expansivel.component';
import { ContratoService } from 'src/app/shared/services/contrato.service';
import { SubmetaService } from 'src/app/shared/services/submeta.service';
import { MedicaoAgrupada } from '../../../shared/model/medicao-agrupada.model';
import { ToggleComponent } from '@serpro/ngx-siconv';
import { Role } from 'src/app/shared/model/security/role.enum';


@Component({
  selector: 'app-listagem-submeta-agrupada',
  templateUrl: './listagem-submeta-agrupada.component.html'
})
export class ListagemSubmetaAgrupadaComponent  extends ListagemExpansivelComponent  {

  acao: string;

  @Input() medicao: Medicao;
  medicoesAgrupadas: MedicaoAgrupada[];

  @ViewChildren('rowToggle')
  rowToggleQuery: QueryList<ToggleComponent>;

  data: MedicaoAgrupada[];

  fileExportName = 'Medicoes_Agrupadas_Submetas';
  export: DataExport;
  dataExport: any[] = [];



  constructor(private _submetaService: SubmetaService,
    private _medicaoService: MedicaoService,
    public _router: Router,
    private _route: ActivatedRoute,
    private _datepipe: DatePipe,
    @Inject(LOCALE_ID) private locale: string,
    private injector: Injector,
    private _contratoService: ContratoService) {
    super(injector);
  }

  loadPermissions(): Map<string, RequiredAuthorizer> {
    const profileEmpresa = [Profile.EMPRESA];
    const profileConvenente = [Profile.PROPONENTE];
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
    ]);
  }


  initializeComponent() {
    this.listarSubmetasMedicoeAgrupadas();

    const url = this._route.snapshot.url;
    this.acao = url[url.length - 1].path;
  }

  private listarSubmetasMedicoeAgrupadas() {

      this._medicaoService.listarMedicoesAgrupadas(this.medicao.id, true).subscribe(
         retorno => {
          this.medicoesAgrupadas = retorno;
          this.data = retorno;

          this.data.sort (MedicaoAgrupada.sortRevert);

          this.exportData ();
         }
      );
  }


  private exportData (): void {

    const columns = ['Descrição', 'Valor', 'Realizado Empresa Período R$', 'Realizado Empresa Período %',
    'Realizado Empresa Acumulado R$', 'Realizado Empresa Acumulado %', 'Realizado Convenente Período R$',
    'Realizado Convenente Período %', 'Realizado Convenente Acumulado R$', 'Realizado Convenente Acumulado %',
    'Realizado Concedente Período R$', 'Realizado Concedente Período %', 'Realizado Concedente Acumulado R$',
    'Realizado Concedente Acumulado %'];


    this.medicoesAgrupadas.forEach(medAgrupada => {

      const master = [];

      master.push('Medição '
        + medAgrupada.sequencial
        + ': '
        + this._datepipe.transform(medAgrupada.dataInicio, 'dd/MM/yyyy')
        + ' - '
        + this._datepipe.transform(medAgrupada.dataFim, 'dd/MM/yyyy'));


      this.dataExport.push(master);

      medAgrupada.listaSubmetasPreenchidas.forEach(sub => {
             const detail = [];

            detail.push(sub.descricao);
            detail.push(formatCurrency(sub.valor, this.locale, ''));
            // Empresa
            detail.push(sub.valorRealizadoEmpresa || sub.valorRealizadoEmpresa === 0 ?
              formatCurrency(sub.valorRealizadoEmpresa, this.locale, '').trim() : '');
            detail.push(sub.percentualRealizadoEmpresa || sub.percentualRealizadoEmpresa === 0 ?
               formatNumber(sub.percentualRealizadoEmpresa, this.locale, '1.2-2') : '');
            detail.push(sub.valorRealizadoAcumuladoEmpresa || sub.valorRealizadoAcumuladoEmpresa === 0 ?
               formatCurrency(sub.valorRealizadoAcumuladoEmpresa, this.locale, '').trim() : '');
            detail.push(sub.percentualRealizadoAcumuladoEmpresa || sub.percentualRealizadoAcumuladoEmpresa === 0 ?
              formatNumber(sub.percentualRealizadoAcumuladoEmpresa, this.locale, '1.2-2') : '');

              // Convenente
            detail.push(sub.valorRealizadoConvenente || sub.valorRealizadoConvenente === 0 ?
                formatCurrency(sub.valorRealizadoConvenente, this.locale, '').trim() : '');
            detail.push(sub.percentualRealizadoConvenente || sub.percentualRealizadoConvenente === 0 ?
                formatNumber(sub.percentualRealizadoConvenente, this.locale, '1.2-2') : '');
            detail.push(sub.valorRealizadoAcumuladoConvenente || sub.valorRealizadoAcumuladoConvenente === 0 ?
                formatCurrency(sub.valorRealizadoAcumuladoConvenente, this.locale, '').trim() : '');
            detail.push(sub.percentualRealizadoAcumuladoConvenente || sub.percentualRealizadoAcumuladoConvenente === 0 ?
                formatNumber(sub.percentualRealizadoAcumuladoConvenente, this.locale, '1.2-2') : '');

            // Concedente
            detail.push(sub.valorRealizadoConcedente || sub.valorRealizadoConcedente === 0 ?
                formatCurrency(sub.valorRealizadoConcedente, this.locale, '').trim() : '');
            detail.push(sub.percentualRealizadoConcedente || sub.percentualRealizadoConcedente === 0 ?
                formatNumber(sub.percentualRealizadoConcedente, this.locale, '1.2-2') : '');
            detail.push(sub.valorRealizadoAcumuladoConcedente || sub.valorRealizadoAcumuladoConcedente === 0 ?
                formatCurrency(sub.valorRealizadoAcumuladoConcedente, this.locale, '').trim() : '');
            detail.push(sub.percentualRealizadoAcumuladoConcedente || sub.percentualRealizadoAcumuladoConcedente === 0 ?
                formatNumber(sub.percentualRealizadoAcumuladoConcedente, this.locale, '1.2-2') : '');

            this.dataExport.push(detail);
        });

        this.export = new DataExport(columns, this.dataExport);
    });

  }

  get contrato(): Contrato {
    return this._contratoService.contratoAtual;
  }

  public selecionarSubmeta(idMedicaoAcumulada: number, idSubmeta: number) {
    this._router.navigate(['./acumulada', idMedicaoAcumulada, 'submeta', idSubmeta, 'editar'], { relativeTo: this._route });
  }

  prepararDetalhamento(idMedicaoAcumulada: number, idSubmeta: number) {
    this._router.navigate(['./acumulada', idMedicaoAcumulada, 'submeta', idSubmeta, 'detalhar'], { relativeTo: this._route });
  }
  isEdit(): boolean {
    return this.acao === 'editar';
  }
}
