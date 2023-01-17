import { RequiredAuthorizer } from '../../../shared/model/security/required-authorizer.model';
import { Profile } from '../../../shared/model/security/profile.enum';
import { Role } from '../../../shared/model/security/role.enum';
import { formatDate } from '@angular/common';
import { Component, Inject, Injector, LOCALE_ID, OnInit, TemplateRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap';
import { Contrato } from 'src/app/shared/model/contrato.model';
import { DataExport } from 'src/app/shared/model/data-export';
import { DocumentosComplementaresService } from 'src/app/shared/services/documentos-complementares.service';
import { ActivatedRoute, Router } from '@angular/router';
import { ContratoService } from 'src/app/shared/services/contrato.service';
import { Empresa } from 'src/app/shared/model/empresa.model';
import { EmpresaService } from 'src/app/shared/services/empresa.service';
import { DocumentosComplementares } from 'src/app/shared/model/documentos-complementares.model';
import { ListagemExpansivelComponent } from 'src/app/shared/util/listagem-expansivel.component';

@Component({
  selector: 'app-listagem-doc-complementar',
  templateUrl: './listagem-doc-complementar.component.html'
})
export class ListagemDocComplementarComponent extends ListagemExpansivelComponent  implements OnInit {

  modalConfirmacaoExcluirRef: BsModalRef;
  modalConfirmacaoBloquearRef: BsModalRef;
  modalConfirmacaoDesbloquearRef: BsModalRef;
  idDocsComplExcluir: number;
  idDocBloquearDesbloquear: number;

  ///// Variaveis do objeto Siconv-Table
  listaDocsCompl: string;
  data = [];
  lista: any[];
  currentPage: number;
  dataExport: any[] = [];
  export: DataExport;
  fileExportName = 'listaDocsCompl';
  //////////////////////////////////////

  constructor(private _route: ActivatedRoute,
              private _router: Router,
              private _contratoService: ContratoService,
              private _empresaService: EmpresaService,
              private _servDocsCompl: DocumentosComplementaresService,
              @Inject(LOCALE_ID) private locale: string,
              private _modalService: BsModalService,
              private injector: Injector) {
    super(injector);
   }

  initializeComponent() {
    this.listarDocsCompl();
  }

  listarDocsCompl() {
      this.dataExport = [];
      this._servDocsCompl.consultarDocumentosComplementares(this.contrato.id).subscribe( element => {
        this.data = element;

        const columns = [
          'Tipo de Documento', 'Tipo Manifesto Ambiental', 'Órgão Emissor',
          'Data de Emissão', 'Válido Até', 'Submetas', 'Arquivo'
        ];

        this.data.forEach( value => {
          const linha = [];

          linha.push(value.tipoDocumento.descricao);
          if (value.tipoManifestoAmbiental) {
            linha.push(value.tipoManifestoAmbiental.descricao);
          } else {
            linha.push('');
          }

          linha.push(value.nmOrgaoEmissor);

          if (value.dtEmissao) {
            linha.push(formatDate(value.dtEmissao, 'dd/MM/yyyy', this.locale));
          } else {
            linha.push('');
          }

          if (value.dtValidade) {
            linha.push(formatDate(value.dtValidade, 'dd/MM/yyyy', this.locale));
          } else {
            linha.push('');
          }

          linha.push(value._submetas);

          linha.push(value.nmArquivo);

          this.dataExport.push(linha);
        });
        this.export = new DataExport(columns, this.dataExport);
      });
  }

  loadPermissions() {
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
          ['bloquear/desbloquear', new RequiredAuthorizer([Profile.CONCEDENTE], [Role.ADMINISTRADOR_SISTEMA, Role.ADMINISTRADOR_SISTEMA_ORGAO_EXTERNO], [])],
        ]);

  }

  incluir() {
    this._router.navigate(['../incluir'], { relativeTo: this._route });
  }

  expandedItems(indiceElementoSelecionado: number) {
    const docsCompl = this.lista[indiceElementoSelecionado];
    docsCompl.showDetail = !docsCompl.showDetail;
  }

  prepararDetalhamento(idDocsCompl: number) {
    this._router.navigate(['../', idDocsCompl, 'detalhar'], { relativeTo: this._route });
  }

  prepararEdicao(idDocsCompl: number) {
    this._router.navigate(['../', idDocsCompl, 'editar'], { relativeTo: this._route });
  }

  prepararExclusao(template: TemplateRef<any>, idExcluir: number) {
    this.idDocsComplExcluir = idExcluir;
    this.modalConfirmacaoExcluirRef = this._modalService.show(template, { class: 'modal-sm' });
  }

  prepararBloqueio(template: TemplateRef<any>, idBloquear: number) {
    this.idDocBloquearDesbloquear = idBloquear;
    this.modalConfirmacaoBloquearRef = this._modalService.show(template, { class: 'modal-sm' });
  }
  prepararDesbloqueio(template: TemplateRef<any>, idDesbloquear: number) {
    this.idDocBloquearDesbloquear = idDesbloquear;
    this.modalConfirmacaoDesbloquearRef = this._modalService.show(template, { class: 'modal-sm' });
  }


  cancelarExclusao() {
    this.modalConfirmacaoExcluirRef.hide();
    this.idDocsComplExcluir = null;
  }

  cancelarBloqueioDesbloqueio(bloquear: boolean) {
    if (bloquear) {
      this.modalConfirmacaoBloquearRef.hide();
    } else {
      this.modalConfirmacaoDesbloquearRef.hide();
    }
    this.idDocBloquearDesbloquear = null;
  }

  confirmarExclusao() {
    this.modalConfirmacaoExcluirRef.hide();
    this.excluirDocsCompl();
  }

  confirmarBloqueioDesbloqueio(bloquear: boolean){
    if (bloquear) {
      this.modalConfirmacaoBloquearRef.hide();
    } else {
      this.modalConfirmacaoDesbloquearRef.hide();
    }
    this.bloquearDesbloquearDocumentoComplementar(bloquear)
  }

  getListaPaginada(listap: any[]) {
    this.lista = listap;
  }

  excluirDocsCompl() {
    this._servDocsCompl.excluir(this.idDocsComplExcluir).subscribe(value => {
      this.adicionarMensagem('Documento Complementar excluído com sucesso.');
      this.listarDocsCompl();
    });
  }

  bloquearDesbloquearDocumentoComplementar(bloquear: boolean) {
    this._servDocsCompl.bloquearDesbloquear(this.idDocBloquearDesbloquear, bloquear).subscribe(value => {
      if (bloquear) {
        this.adicionarMensagem('Documento Complementar bloqueado com sucesso.');
      } else {
        this.adicionarMensagem('Documento Complementar desbloqueado com sucesso.');
      }
      this.listarDocsCompl();
    });
  }

  permiteManutencao(documento: DocumentosComplementares): boolean {
    return !documento.bloqueado &&
              (!documento.possuiMedicao || documento.tipoDocumento.codigo !== 'OSE');
  }

  getTooltipEdicaoDesabilitada(documento: DocumentosComplementares): string {
    return documento.possuiMedicao && documento.tipoDocumento.codigo === 'OSE'
      ? 'Ordem de serviço não pode ser editada pois já existe medição.'
      : 'Documento não pode ser editado pois está bloqueado.';
  }

  getTooltipExclusaoDesabilitada(documento: DocumentosComplementares): string {
    return documento.possuiMedicao && documento.tipoDocumento.codigo === 'OSE'
      ? 'Ordem de serviço não pode ser excluída pois já existe medição.'
      : 'Documento não pode ser excluído pois está bloqueado.';
  }

  get contrato(): Contrato {
    return this._contratoService.contratoAtual;
  }

  get empresa(): Empresa {
    return this._empresaService.empresaAtual;
  }

  navegarAbaResponsavelTecnico() {
    if (this.contrato.inSocial) {
      this._router.navigate(['../../rtsocial'], { relativeTo: this._route });
    } else {
      this._router.navigate(['../../rt'], { relativeTo: this._route });
    }
  }

  navegarAbaARTRRT() {
    this._router.navigate(['../../artrrt'], { relativeTo: this._route });
  }

  navegarAbaParalisacao() {
    this._router.navigate(['../../paralisacao'], { relativeTo: this._route });
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
