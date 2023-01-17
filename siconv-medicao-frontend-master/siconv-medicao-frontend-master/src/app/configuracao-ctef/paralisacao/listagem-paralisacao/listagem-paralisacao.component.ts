import { Component, Injector, TemplateRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BsModalRef, BsModalService } from 'ngx-bootstrap';
import { Contrato } from 'src/app/shared/model/contrato.model';
import { DataExport } from 'src/app/shared/model/data-export';
import { Empresa } from 'src/app/shared/model/empresa.model';
import { Paralisacao } from 'src/app/shared/model/paralisacao.model';
import { Profile } from 'src/app/shared/model/security/profile.enum';
import { RequiredAuthorizer } from 'src/app/shared/model/security/required-authorizer.model';
import { Role } from 'src/app/shared/model/security/role.enum';
import { ContratoService } from 'src/app/shared/services/contrato.service';
import { EmpresaService } from 'src/app/shared/services/empresa.service';
import { ParalisacaoService } from 'src/app/shared/services/paralisacao.service';
import { ListagemExpansivelComponent } from 'src/app/shared/util/listagem-expansivel.component';

@Component({
  selector: 'app-listagem-paralisacao',
  templateUrl: './listagem-paralisacao.component.html',
})
export class ListagemParalisacaoComponent extends ListagemExpansivelComponent {

  lista: Paralisacao[];
  listaPaginada: Paralisacao[];
  tableDataExport: DataExport;
  exibeBotaoIncluir: boolean;
  idParalisacaoExcluir: number;
  modalConfirmacaoExcluirRef: BsModalRef;

  constructor(
    private _route: ActivatedRoute,
    private _router: Router,
    private _contratoService: ContratoService,
    private _empresaService: EmpresaService,
    private _paralisacaoService: ParalisacaoService,
    private _modalService: BsModalService,
    private injector: Injector
  ) {
    super(injector);
  }

  protected initializeComponent(): void {
    this._paralisacaoService.listar(this.contrato.id).subscribe(response => {
      this.lista = response;
      this.initTableDataExport();
      this.definirIndicadorManutencao();
      this.atualizarExibicaoBotaoIncluir();
    });
  }

  protected loadPermissions(): Map<string, RequiredAuthorizer> {

    const roles = [
      Role.GESTOR_FINANCEIRO_CONVENENTE,
      Role.OPERADOR_FINANCEIRO_CONVENENTE,
      Role.GESTOR_CONVENIO_CONVENENTE,
      Role.FISCAL_CONVENENTE,
      Role.FISCAL_CONCEDENTE,
      Role.OPERACIONAL_CONCEDENTE,
      Role.GESTOR_CONVENIO_CONCEDENTE,
      Role.GESTOR_FINANCEIRO_CONCEDENTE,
      Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA,
      Role.FISCAL_ACOMPANHAMENTO,
      Role.TECNICO_TERCEIRO
    ];

    const profiles = [
        Profile.PROPONENTE,
        Profile.CONCEDENTE,
        Profile.MANDATARIA
    ];

    return new Map([
      ['manter', new RequiredAuthorizer (profiles, roles, [])]
    ]);
  }

  private initTableDataExport() {
    const data = [];
    const columns = ['Período', 'Responsável', 'Indicativo', 'Motivo', 'Anexos'];

    this.lista.forEach(paralisacao => {
      const linha = [];

      linha.push(paralisacao.periodo);
      linha.push(paralisacao.responsavel.descricao);
      linha.push(paralisacao.indicativo.descricao);
      linha.push(paralisacao.motivo.descricao);
      linha.push(paralisacao.anexos.reduce((nomes, anexo) => nomes + anexo.nmArquivo + '\n', ''));

      data.push(linha);
    });

    this.tableDataExport = new DataExport(columns, data);
  }

  private definirIndicadorManutencao() {
    // Sinaliza que apenas a paralisacao mais recente pode ser mantida
    if (this.lista.length > 0) {
      this.lista[0].permiteManutencao = true;
    }
  }

  private atualizarExibicaoBotaoIncluir() {
    this.exibeBotaoIncluir = this.canAccess('manter') && this.lista.every(paralisacao => paralisacao.dataFim != null);
  }

  public atualizarPagina(listaPaginada: Paralisacao[]) {
    this.listaPaginada = listaPaginada;
  }

  public expandirContrair(paralisacao: Paralisacao) {
    paralisacao.showDetail = !paralisacao.showDetail;
  }

  public prepararInclusao(): void {
    this._router.navigate(['../incluir'], { relativeTo: this._route });
  }

  public prepararEdicao(idParalisacao: number) {
    this._router.navigate(['../', idParalisacao, 'editar'], { relativeTo: this._route });
  }

  public prepararDetalhamento(idParalisacao: number) {
    this._router.navigate(['../', idParalisacao, 'detalhar'], { relativeTo: this._route });
  }

  public prepararExclusao(template: TemplateRef<any>, idParalisacao: number) {
    this.idParalisacaoExcluir = idParalisacao;
    this.modalConfirmacaoExcluirRef = this._modalService.show(template, { class: 'modal-sm' });
  }

  public cancelarExclusao() {
    this.modalConfirmacaoExcluirRef.hide();
    this.idParalisacaoExcluir = null;
  }

  public confirmarExclusao() {
    this.excluirParalisacao(this.idParalisacaoExcluir);
    this.modalConfirmacaoExcluirRef.hide();
    this.idParalisacaoExcluir = null;
  }

  private excluirParalisacao(idParalisacao: number) {
    this._paralisacaoService.excluir(idParalisacao).subscribe(() => {
      this.adicionarMensagem('Paralisação de Obra excluída com sucesso.');
      this.initializeComponent();
    });
  }

  get contrato(): Contrato {
    return this._contratoService.contratoAtual;
  }

  get empresa(): Empresa {
    return this._empresaService.empresaAtual;
  }

  public navegarAbaResponsavelTecnico() {
    if (this.contrato.inSocial) {
      this._router.navigate(['../../rtsocial'], { relativeTo: this._route });
    } else {
      this._router.navigate(['../../rt'], { relativeTo: this._route });
    }
  }

  public navegarAbaARTRRT() {
    this._router.navigate(['../../artrrt'], { relativeTo: this._route });
  }

  public navegarAbaDocumentacaoComplementar() {
    this._router.navigate(['../../doccomplementar'], { relativeTo: this._route });
  }

  public voltar() {
    const rootModule = this._route.snapshot.pathFromRoot[1].url[0].path;

    if (rootModule === 'preenchimento') {
      this._router.navigate(['/preenchimento/empresa', this.empresa.id]);
    } else {
      this._router.navigate(['/acompanhamento/proposta', this.contrato.propostaFk]);
    }
  }
}
