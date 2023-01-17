import { ContratoService } from './../../../shared/services/contrato.service';
import { Profile } from '../../../shared/model/security/profile.enum';
import { Role } from '../../../shared/model/security/role.enum';
import { RequiredAuthorizer } from '../../../shared/model/security/required-authorizer.model';

import { ArtRrtService } from '../../../shared/services/art-rrt.service';
import { Contrato } from 'src/app/shared/model/contrato.model';
import { Component, TemplateRef, Inject, LOCALE_ID, Output, EventEmitter, Injector } from '@angular/core';
import { formatDate } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { BsModalRef, BsModalService } from 'ngx-bootstrap';
import { DataExport } from 'src/app/shared/model/data-export';
import { ArtRrt } from 'src/app/shared/model/art-rrt.model';
import { Empresa } from 'src/app/shared/model/empresa.model';
import { EmpresaService } from 'src/app/shared/services/empresa.service';
import { ListagemExpansivelComponent } from 'src/app/shared/util/listagem-expansivel.component';

@Component({
  selector: 'app-lista-art-rrt',
  templateUrl: './listagem-art-rrt.component.html'
})
export class ListagemArtRrtComponent extends ListagemExpansivelComponent {

  contrato: Contrato;

  @Output()
  inativarOuExcluir = new EventEmitter();

  // idProposta: number;

  ///// Variaveis do objeto Siconv-Table
  listaArtRrt: string;
  data = [];
  lista: any[];
  currentPage: number;
  dataExport: any[] = [];
  export: DataExport;
  fileExportName = 'listaArtRrt';
  //////////////////////////////////////

  // Controle de Aba
  aba = 'artrrt';

  idRtExcluir: number;
  modalConfirmacaoExcluirRef: BsModalRef;

  idRtInativar: number;
  modalConfirmacaoInativarRef: BsModalRef;

  constructor(
    private route: ActivatedRoute,
    public _router: Router,
    private _modalService: BsModalService,
    private _aartrrtService: ArtRrtService,
    private _contratoService: ContratoService,
    private _empresaService: EmpresaService,
    @Inject(LOCALE_ID) private locale: string,
    private injector: Injector) {
    super(injector);
  }

  initializeComponent() {
    this.contrato = this._contratoService.contratoAtual;
    this.listarArtRrt(this.contrato.id);
  }

  loadPermissions(): Map<string, RequiredAuthorizer> {
    const roles = [
      Role.FISCAL_CONVENENTE,
      Role.GESTOR_CONVENIO_CONVENENTE,
      Role.GESTOR_FINANCEIRO_CONVENENTE,
      Role.OPERADOR_FINANCEIRO_CONVENENTE];

    const profiles = [
      Profile.PROPONENTE
    ];

    return new Map([
      ['incluir', new RequiredAuthorizer(profiles, roles, [])],
      ['editar', new RequiredAuthorizer(profiles, roles, [])],
      ['excluir', new RequiredAuthorizer(profiles, roles, [])],
      ['inativar', new RequiredAuthorizer(profiles, roles, [])]
    ]);
  }

  listarArtRrt(idContrato: number) {
    this.dataExport = [];
    this._aartrrtService.listarArtRrt(idContrato).subscribe(value => {
      this.data = value;

      const columns = [
        'Tipo', 'ART/RRT', 'Data de Emissão',
        'Responsável Técnico', 'Submetas',
        'Data de Inativação', 'Arquivo'
      ];
      this.data.forEach(element => {
        const linha = [];
        linha.push(element.descricaoTipo);
        linha.push(element.numero);
        linha.push(formatDate(element.dataEmissao, 'dd/MM/yyyy', this.locale));
        linha.push(element.nomeResponsavelTecnico);
        linha.push(element.listasubmetas);
        if (element.dataInativacao != null) {
          linha.push(formatDate(element.dataInativacao, 'dd/MM/yyyy', this.locale));
        } else {
          linha.push('');
        }
        linha.push(element.nmArquivo);
        this.dataExport.push(linha);
      });
      this.export = new DataExport(columns, this.dataExport);

    });

  }

  getListaPaginada(listap: any[]) {
    this.lista = listap;
  }

  expandedItems(indiceElementoSelecionado: number) {
    const art = this.lista[indiceElementoSelecionado];
    art.showDetail = !art.showDetail;
  }

  incluir() {
    this._router.navigate(['../incluir'], { relativeTo: this.route });
  }

  prepararDetalhamento(idArt: number) {
    this._router.navigate([`../${idArt}/detalhar`], { relativeTo: this.route });
  }

  prepararEdicao(idArt: number) {
    this._router.navigate([`../${idArt}/editar`], { relativeTo: this.route });
  }

  /**
   * MÉTODOS PARA EXCLUSÃO
   **/



  prepararExclusao(template: TemplateRef<any>, idExcluir: number) {
    this.idRtExcluir = idExcluir;
    this.modalConfirmacaoExcluirRef = this._modalService.show(template, { class: 'modal-sm' });
  }

  cancelarExclusao() {
    this.modalConfirmacaoExcluirRef.hide();
    this.idRtExcluir = null;
  }

  confirmarExclusao() {
    this.modalConfirmacaoExcluirRef.hide();
    this.excluirRT();
  }

  excluirRT() {
    this._aartrrtService.excluir(this.idRtExcluir).subscribe(value => {
      this.adicionarMensagem('ART / RRT excluído com sucesso.');
      this.initializeComponent();
      this.inativarOuExcluir.emit();
    });
  }

  /**
   * MÉTODOS PARA INATIVAÇÃO
   **/

  prepararInativacao(template: TemplateRef<any>, idInativar: number) {
    this.idRtInativar = idInativar;
    this.modalConfirmacaoInativarRef = this._modalService.show(template, { class: 'modal-sm' });
  }

  cancelarInativacao() {
    this.modalConfirmacaoInativarRef.hide();
    this.idRtInativar = null;
  }

  confirmarInativacao() {
    this.modalConfirmacaoInativarRef.hide();
    this.inativarRT();
  }

  inativarRT() {
    this._aartrrtService.inativar(this.idRtInativar).subscribe(value => {
      this.adicionarMensagem('Anotação/Registro de Responsabilidade Técnica inativado com sucesso.');
      this.initializeComponent();
      this.inativarOuExcluir.emit();
    });
  }

  getMotivoSemPermissaoEdicao(dataInativacao: boolean, possuiSubmetaAssinada: boolean) {
    if (dataInativacao) {
      return 'ART / RRT não pode ser editado, pois está inativo.'
    } else if (possuiSubmetaAssinada) {
      return 'ART / RRT não pode ser editado, pois existe alguma submeta assinada pelo Responsável Técnico associado.';
    }
  }

  getMotivoSemPermissaoInativacao(art: ArtRrt): string {

    let toolTip;

    if (art.dataInativacao) {
      toolTip = 'ART / RRT está inativo.';
    } else {
      toolTip = 'ART / RRT possui Submeta assinada e não pode ser inativado';
    }

    return toolTip;
  }

  getMotivoSemPermissaoExclusao() {
    return 'ART / RRT não pode ser excluído, pois existe alguma submeta assinada pelo Responsável Técnico associado.';
  }

  navegarAbaRT(): void {
    this._router.navigate(['../../rt/listar'], { relativeTo: this.route });
  }

  navegarAbaDocComplementar(): void {
    this._router.navigate(['../../doccomplementar/listar'], { relativeTo: this.route });
  }

  navegarAbaParalisacao() {
    this._router.navigate(['../../paralisacao'], { relativeTo: this.route });
  }

  voltar() {
    const rootModule = this.route.snapshot.pathFromRoot[1].url[0].path;

    if (rootModule === 'preenchimento') {
      this._router.navigate(['/preenchimento/empresa', this.empresa.id]);

    } else {
      this._router.navigate(['/acompanhamento/proposta', this.contrato.propostaFk]);
    }
  }

  get empresa(): Empresa {
    return this._empresaService.empresaAtual;
  }
}
