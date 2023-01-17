import { Profile } from '../../../shared/model/security/profile.enum';
import { Role } from '../../../shared/model/security/role.enum';
import { RequiredAuthorizer } from '../../../shared/model/security/required-authorizer.model';
import { Component, OnInit, Inject, LOCALE_ID, TemplateRef, Injector } from '@angular/core';
import { Contrato } from 'src/app/shared/model/contrato.model';
import { ActivatedRoute, Router } from '@angular/router';
import { BsModalService, BsModalRef } from 'ngx-bootstrap';
import { ResponsavelTecnicoSocialService } from 'src/app/shared/services/responsavel-tecnico-social.service';
import { DataExport } from '../../../shared/model/data-export';
import { formatDate } from '@angular/common';
import { ResponsavelTecnicoSocial } from 'src/app/shared/model/responsavel-tecnico-social.model';
import { CpfPipe } from 'src/app/shared/pipes/cpf.pipe';
import { TipoResponsavelTecnicoEnum } from 'src/app/shared/enum/tipo-responsavel-tecnico.enum';
import { ContratoService } from 'src/app/shared/services/contrato.service';
import { Empresa } from 'src/app/shared/model/empresa.model';
import { EmpresaService } from 'src/app/shared/services/empresa.service';
import { ListagemExpansivelComponent } from 'src/app/shared/util/listagem-expansivel.component';

@Component({
  selector: 'app-listagem-rt-social',
  templateUrl: './listagem-rt-social.component.html',
  providers: [ CpfPipe ]
})
export class ListagemRtSocialComponent extends ListagemExpansivelComponent implements OnInit {

  // Variáveis para a siconv-table
  data: ResponsavelTecnicoSocial[] = [];
  lista: ResponsavelTecnicoSocial[];
  export: DataExport;
  fileExportName = 'listaResponsavelTecnicoSocial';

  // Enum
  TipoResponsavelTecnicoEnum = TipoResponsavelTecnicoEnum;

  /**
   * MÉTODOS PARA EXCLUSÃO
   **/
  idRtExcluir: number;
  versao: number;
  modalConfirmacaoExcluirRef: BsModalRef;

  /**
   * MÉTODOS PARA INATIVAÇÃO
   **/
  idRtInativar: number;
  modalConfirmacaoInativarRef: BsModalRef;

  constructor(
    private _route: ActivatedRoute,
    private _router: Router,
    private _contratoService: ContratoService,
    private _empresaService: EmpresaService,
    private _modalService: BsModalService,
    private _responsavelTecnicoSocialService: ResponsavelTecnicoSocialService,
    private _cpfPipe: CpfPipe,
    @Inject(LOCALE_ID) private _locale: string,
    private injector: Injector) {
      super (injector);
    }

  initializeComponent() {
    this._responsavelTecnicoSocialService.listarResponsavelTecnicoSocial(this.contrato.id)
      .subscribe(value => {
        this.data = value;
        this.getExport(value);
      });
  }

  loadPermissions(): Map <string, RequiredAuthorizer> {
    const roles = [
      Role.FISCAL_CONVENENTE,
      Role.GESTOR_CONVENIO_CONVENENTE,
      Role.GESTOR_FINANCEIRO_CONVENENTE,
      Role.OPERADOR_FINANCEIRO_CONVENENTE];

    const profiles = [
        Profile.PROPONENTE
    ];

    return new Map([
      ['incluir', new RequiredAuthorizer (profiles, roles, [])],
      ['editar', new RequiredAuthorizer (profiles, roles, [])],
      ['excluir', new RequiredAuthorizer (profiles, roles, [])],
      ['inativar', new RequiredAuthorizer (profiles, roles, [])]
    ]);
  }

  getListaPaginada(listap) {
    this.lista = listap;
  }

  expandedItems(indiceElementoSelecionado: number) {
    const rts = this.lista[indiceElementoSelecionado];
    rts.showDetail = !rts.showDetail;
  }

  getExport(listaRTSocial: ResponsavelTecnicoSocial[]): ResponsavelTecnicoSocial[] {
    const data = [];
    const columns = [
      'CPF', 'Nome', 'Tipo',
      'Submetas', 'Data Inclusão', 'Data Inativação'
    ];

    listaRTSocial.forEach(element => {
      const linha = [];
      linha.push(this._cpfPipe.transform(element.responsavelTecnico.cpf));
      linha.push(element.responsavelTecnico.nome);
      linha.push(TipoResponsavelTecnicoEnum[element.tipo.codigo]);
      linha.push(element._submetas);
      linha.push(formatDate(element.dtInclusao, 'dd/MM/yyyy', this._locale));
      linha.push(element.dtInativacao ? formatDate(element.dtInativacao, 'dd/MM/yyyy', this._locale) : '');

      data.push(linha);
    });

    this.export = new DataExport(columns, data);

    return listaRTSocial;
  }

  incluir(): void {
    this._router.navigate(['../incluir'], { relativeTo: this._route });
  }

  prepararDetalhamento(idResponsavelTecnicoSocial: number) {
    this._router.navigate(['../', idResponsavelTecnicoSocial, 'detalhar'], { relativeTo: this._route });
  }

  prepararEdicao(idResponsavelTecnicoSocial: number) {
    this._router.navigate(['../', idResponsavelTecnicoSocial, 'editar'], { relativeTo: this._route });
  }

  /**
   * Inativação
   */
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
    this._responsavelTecnicoSocialService.inativar(this.idRtInativar).subscribe(value => {
      this.adicionarMensagem('Responsável Técnico inativado com sucesso.');
      this.initializeComponent();
    });
  }

  /**
   * Exclusão
   */
  prepararExclusao(template: TemplateRef<any>, idExcluir: number, versao: number) {
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
    this._responsavelTecnicoSocialService.excluir(this.idRtExcluir).subscribe(value => {
      this.adicionarMensagem('Responsável Técnico excluído com sucesso.');
      this.initializeComponent();
    });
  }

  getMotivoSemPermissaoEdicao(rts: ResponsavelTecnicoSocial): string {
    if (rts.dtInativacao) {
      return 'Responsável Técnico está inativo e não pode ser editado.';
    } else if (rts.possuiSubmetaAssinada) {
      return 'Responsável Técnico possui submeta assinada e não pode ser editado.';
    }
  }

  getMotivoSemPermissaoInativacao(rts: ResponsavelTecnicoSocial): string {
    if (rts.dtInativacao) {
      return 'Responsável Técnico está inativo.';
    }
  }

  getMotivoSemPermissaoExclusao(rts: ResponsavelTecnicoSocial): string {
    if (rts.possuiSubmetaAssinada) {
      return 'Responsável Técnico possui Submeta assinada e não pode ser excluído.';
    }
  }

  get contrato(): Contrato {
    return this._contratoService.contratoAtual;
  }

  get empresa(): Empresa {
    return this._empresaService.empresaAtual;
  }

  voltar() {
    const rootModule = this._route.snapshot.pathFromRoot[1].url[0].path;

    if (rootModule === 'preenchimento') {
      this._router.navigate(['/preenchimento/empresa', this.empresa.id]);

    } else {
      this._router.navigate(['/acompanhamento/proposta', this.contrato.propostaFk]);
    }
  }

  navegarAbaDocumentacaoComplementar() {
    this._router.navigate(['../../doccomplementar'], { relativeTo: this._route });
  }

  navegarAbaParalisacao() {
    this._router.navigate(['../../paralisacao'], { relativeTo: this._route });
  }
}
