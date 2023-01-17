import { EmpresaService } from 'src/app/shared/services/empresa.service';
import { ContratoService } from 'src/app/shared/services/contrato.service';
import { Profile } from '../../../shared/model/security/profile.enum';
import { Role } from '../../../shared/model/security/role.enum';
import { RequiredAuthorizer } from '../../../shared/model/security/required-authorizer.model';
import { BaseComponent } from 'src/app/shared/util/base.component';
import { formatDate } from '@angular/common';
import { Component, Inject, LOCALE_ID, TemplateRef, Injector } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BsModalRef, BsModalService } from 'ngx-bootstrap';
import { Contrato } from 'src/app/shared/model/contrato.model';
import { DataExport } from 'src/app/shared/model/data-export';
import { Empresa } from 'src/app/shared/model/empresa.model';
import { ResponsavelTecnicoService } from 'src/app/shared/services/responsavel-tecnico.service';
import { CpfPipe } from 'src/app/shared/pipes/cpf.pipe';

@Component({
  selector: 'app-listagem-rt',
  templateUrl: './listagem-rt.component.html',
  styleUrls: ['./listagem-rt.component.scss'],
  providers: [ CpfPipe ]
})
export class ListagemRtComponent extends BaseComponent {

  // @Input()
  contrato: Contrato;

  // @Input()
  empresa: Empresa;

  idProposta: number;

  ///// Variaveis do objeto Siconv-Table
  listaRespTecnico: string;
  data = [];
  lista: any[];
  dataExport: any[] = [];
  export: DataExport;
  fileExportName: string = 'listaResponsavelTecnico';
  //////////////////////////////////////

  // Controle de Aba
  aba: string = 'responsavelTecnico';

  constructor(private contratoService: ContratoService,
              private empresaService: EmpresaService,
              private route: ActivatedRoute,
              private _router: Router,
              private _modalService: BsModalService,
              private _responsaveltecnicoService: ResponsavelTecnicoService,
              @Inject(LOCALE_ID) private locale: string,
              private _cpfPipe: CpfPipe,
              private injector: Injector) {
                super(injector);
               }

  incluirRespTecnico() {
    this._router.navigate(['../incluir'], { relativeTo: this.route });
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
      ['excluir', new RequiredAuthorizer (profiles, roles, [])]
    ]);
  }

  initializeComponent() {
    this.dataExport = [];
    this.contrato = this.contratoService.contratoAtual;

    this.empresa = this.empresaService.empresaAtual;

    this._responsaveltecnicoService.listarResponsavelTecnico(this.contrato.id).subscribe(value => {
      this.data = value;
      const columns = [
        'CPF', 'Nome', 'Atividade',
        'Tipo', 'CREA/CAU', 'Data Inclusão'
      ];
      this.data.forEach(element => {
        element.contratoFk = this.contrato.id;
        const linha = [];
        linha.push(this._cpfPipe.transform(element.cpf));
        linha.push(element.nome);
        linha.push(element.atividade);
        linha.push(element.tipoContrato);
        linha.push(element.creacau);
        linha.push(formatDate(element.dataInclusao, 'dd/MM/yyyy', this.locale));
        this.dataExport.push(linha);
      });
      this.export = new DataExport(columns, this.dataExport);
    });
    this.route.params.subscribe(
      params => {
        this.idProposta = params['idProposta'];
    });
  }

  getListaPaginada(listap) {
    this.lista = listap;
  }

  prepararDetalhamento(idRt: number) {
    this._router.navigate(['../', idRt, 'detalhar'], { relativeTo: this.route });
  }

  prepararEdicao(idRt: number) {
    this._router.navigate(['../', idRt, 'editar'], { relativeTo: this.route });
  }

  /**
   * MÉTODOS PARA EXCLUSÃO
   **/

  idRtExcluir: number;
  versao: number;
  modalConfirmacaoExcluirRef: BsModalRef;

  prepararExclusao(template: TemplateRef<any>, idExcluir: number, versao: number) {
    this.idRtExcluir = idExcluir;
    this.versao = versao;
    this.modalConfirmacaoExcluirRef = this._modalService.show(template, {class: 'modal-sm'});
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
    this._responsaveltecnicoService.excluir(this.idRtExcluir).subscribe(value => {
      this.adicionarMensagem ('Responsável Técnico excluído com sucesso.');
      this.ngOnInit();
    });
  }

  getMotivoSemPermissaoEdicao() {
    return 'Responsável Técnico possui submeta assinada e não pode ser editado.';
  }

  getMotivoSemPermissaoExclusao(possuiART: boolean, possuiSubmetaAssinada: boolean) {
    if (possuiSubmetaAssinada) {
      return 'Responsável Técnico possui Submeta assinada e não pode ser excluído.';
    } else if (possuiART) {
      return 'Responsável Técnico está vinculado a pelo menos uma ART/RRT e não pode ser excluído.';
    }
  }

  abaSelecionada (evento: boolean, abaSelecionada: string) {
    if (evento) {
      this.aba = abaSelecionada;
    }
  }

  voltar() {
    const rootModule = this.route.snapshot.pathFromRoot[1].url[0].path;

    if (rootModule === 'preenchimento') {
      this._router.navigate(['/preenchimento/empresa', this.empresa.id]);

    } else {
      this._router.navigate(['/acompanhamento/proposta', this.contrato.propostaFk]);
    }
  }

  navegarAbaART() {
    this._router.navigate(['../../artrrt/listar'], { relativeTo: this.route });
  }

  navegarAbaDocComplementar(): void {
    this._router.navigate(['../../doccomplementar/listar'], { relativeTo: this.route });
  }

  navegarAbaParalisacao() {
    this._router.navigate(['../../paralisacao'], { relativeTo: this.route });
  }
}
