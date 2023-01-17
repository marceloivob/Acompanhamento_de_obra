import { FiltroAnexosPipe } from './../../../shared/pipes/filtro-anexos.pipe';
import { Profile } from '../../../shared/model/security/profile.enum';
import { Permission } from '../../../shared/model/security/permission.enum';
import { RequiredAuthorizer } from '../../../shared/model/security/required-authorizer.model';
import { Component, TemplateRef, Inject, LOCALE_ID, Injector } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Medicao } from 'src/app/shared/model/medicao.model';
import { ObservacaoService } from 'src/app/shared/services/observacao.service';
import { Observacao } from 'src/app/shared/model/observacao.model';
import { CnpjPipe } from 'src/app/shared/pipes/cnpj.pipe';
import { EmpresaService } from 'src/app/shared/services/empresa.service';
import { SharedService } from 'src/app/shared/services/shared-service.service';
import { BsModalRef, BsModalService } from 'ngx-bootstrap';
import { DataExport } from 'src/app/shared/model/data-export';
import { formatDate } from '@angular/common';
import { Empresa } from 'src/app/shared/model/empresa.model';
import { Role } from 'src/app/shared/model/security/role.enum';
import { SituacaoMedicaoEnum } from 'src/app/shared/enum/situacao-medicao.enum';
import { PerfilEnum } from 'src/app/shared/enum/perfil.enum';
import { Anexo } from 'src/app/shared/model/anexo.modelo';
import { ListagemExpansivelComponent } from 'src/app/shared/util/listagem-expansivel.component';

@Component({
  selector: 'app-listagem-observacao',
  templateUrl: './listagem-observacao.component.html',
  styleUrls: ['./listagem-observacao.component.scss']
})
export class ListagemObservacaoComponent extends ListagemExpansivelComponent {
  acaoParent: string;
  medicao: Medicao;
  idObsExcluir: number;
  modalConfirmacaoExcluirRef: BsModalRef;
  data: Observacao[];
  dataObservacoesMedicoesAgrupadas: Observacao[];
  lista: any[];
  listaObservacoesMedicoesAgrupadas: Observacao[];
  dataExport: any[] = [];
  export: DataExport;
  exportObsMedicoesAgrupadas: DataExport;
  tituloListaObsMedicaoAtual = 'Observações - Medição Atual';
  tituloListaObsMedicoesAgrupadas = 'Observações - Medições Acumuladas';
  exibirFiltroAnexoAgrupadora = false;
  exibirFiltroAnexoAgrupada = false;
  modalConfirmacaoInativacaoAnexoRef: BsModalRef;
  modalConfirmacaoAtivacaoAnexoRef: BsModalRef;
  mdIdObservacao: number;
  mdIdAnexo: number;
  mdIdMedicaoAgrupada: number;

  constructor(
    private _router: Router,
    private _cnpj: CnpjPipe,
    private _filtroAnexos: FiltroAnexosPipe,
    private _empresaService: EmpresaService,
    private _sharedService: SharedService,
    private _observacaoService: ObservacaoService,
    private _modalService: BsModalService,
    private _route: ActivatedRoute,
    @Inject(LOCALE_ID) private locale: string,
    private injector: Injector
  ) {
    super(injector);
  }

  protected initializeComponent() {
    this.medicao = this._route.snapshot.data.medicao;
    const urlParent = this._route.parent.snapshot.url;
    this.acaoParent = urlParent[urlParent.length - 1].path;
    this.carregarListaObservacao();
    if (this.medicao.isMedicaoAgrupadora) {
      this.carregarListaObservacoesAgrupadas();
    }
    this.emitirTitulo();
  }

  private carregarListaObservacao() {
    const columns = ['Data', 'Perfil', 'Responsável', 'Descrição', 'Anexos'];

    this._observacaoService.listarObservacoes(this.medicao.id, false).subscribe(
      value => {
        this.data = value;
        this.dataExport = [];
        this.data.forEach(element => {
          const linha = [];

          linha.push(formatDate(element.dtRegistro, 'dd/MM/yyyy', this.locale));
          linha.push(element.inPerfilResponsavel.descricao);
          linha.push(element.nomeResponsavel);
          linha.push(this.breakTextInLines(element.txObservacao));
          linha.push(this.listaAnexosExportacao(element.anexos, this.exibirFiltroAnexoAgrupadora));
          this.dataExport.push(linha);
        });
        this.export = new DataExport(columns, this.dataExport);
      });
  }

  private carregarListaObservacoesAgrupadas() {
    const columns = ['Data', 'Perfil', 'Medição', 'Responsável', 'Descrição', 'Anexos'];

    this._observacaoService.listarObservacoes(this.medicao.id, true).subscribe(
      value => {
        this.dataObservacoesMedicoesAgrupadas = value;
        this.dataExport = [];
        this.dataObservacoesMedicoesAgrupadas.forEach(element => {
          const linha = [];

          linha.push(formatDate(element.dtRegistro, 'dd/MM/yyyy', this.locale));
          linha.push(element.inPerfilResponsavel.descricao);
          linha.push(element.sequencialMedicaoAgrupada);
          linha.push(element.nomeResponsavel);
          linha.push(this.breakTextInLines(element.txObservacao));
          linha.push(this.listaAnexosExportacao(element.anexos, this.exibirFiltroAnexoAgrupada));
          this.dataExport.push(linha);
        });
        this.exportObsMedicoesAgrupadas = new DataExport(columns, this.dataExport);
      });
  }

  listaAnexosExportacao(anexos: Anexo[], exibirInativos: boolean): string {
    const filterAnexos = new FiltroAnexosPipe();
    const arquivos = filterAnexos.transform(anexos, exibirInativos);

    return arquivos.reduce((prevVal, elem) =>
      prevVal + elem.nmArquivo + '\n', '');
  }

  private emitirTitulo() {
    const tituloTela = {
      titulo: 'Medição ' + this.medicao.sequencial,
      subtitulo: 'Adicione uma Observação à Medição.',
      info: this._cnpj.transform(this.empresa.cnpj) + ' - ' + this.empresa.razaoSocial
    };

    if (this.isParentDetail()) {
      tituloTela.subtitulo = 'Lista de Observações';
    }

    this._sharedService.emitChange(tituloTela);
  }

  protected loadPermissions(): Map<string, RequiredAuthorizer> {
    const profiles = [Profile.EMPRESA, Profile.PROPONENTE, Profile.CONCEDENTE, Profile.MANDATARIA];
    const roles = [Role.GESTOR_FINANCEIRO_CONVENENTE, Role.OPERADOR_FINANCEIRO_CONVENENTE,
    Role.GESTOR_CONVENIO_CONVENENTE, Role.FISCAL_CONVENENTE,
    Role.FISCAL_CONCEDENTE, Role.GESTOR_CONVENIO_CONCEDENTE, Role.GESTOR_FINANCEIRO_CONCEDENTE, Role.OPERACIONAL_CONCEDENTE,
    Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA, Role.FISCAL_ACOMPANHAMENTO, Role.TECNICO_TERCEIRO];

    return new Map([
      ['incluir', new RequiredAuthorizer(profiles, roles, [Permission.INCLUIR_OBSERVACAO_MEDICAO])],
      ['editarEmpresa', new RequiredAuthorizer([Profile.EMPRESA], [], [Permission.EDITAR_OBSERVACAO_MEDICAO])],
      ['excluirEmpresa', new RequiredAuthorizer([Profile.EMPRESA], [], [Permission.EXCLUIR_OBSERVACAO_MEDICAO])],
      ['ativarAnexo', new RequiredAuthorizer([Profile.PROPONENTE, Profile.CONCEDENTE, Profile.MANDATARIA], roles, [])],
      ['manterConvenente', new RequiredAuthorizer([Profile.PROPONENTE], [Role.GESTOR_FINANCEIRO_CONVENENTE, Role.OPERADOR_FINANCEIRO_CONVENENTE, Role.GESTOR_CONVENIO_CONVENENTE, Role.FISCAL_CONVENENTE], [])],
      ['manterFiscalAcompanhamento', new RequiredAuthorizer([Profile.CONCEDENTE], [Role.FISCAL_ACOMPANHAMENTO], [])],
      ['manterTecnicoTerceiro', new RequiredAuthorizer([Profile.CONCEDENTE], [Role.TECNICO_TERCEIRO], [])],
      ['manterConcedente', new RequiredAuthorizer([Profile.CONCEDENTE], [Role.FISCAL_CONCEDENTE, Role.GESTOR_CONVENIO_CONCEDENTE, Role.GESTOR_FINANCEIRO_CONCEDENTE, Role.OPERACIONAL_CONCEDENTE], [])],
      ['manterMandataria', new RequiredAuthorizer([Profile.MANDATARIA], [Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA], [])]
    ]);
  }

  public novaObservacao() {
    this._router.navigate(['../incluir'], { relativeTo: this._route });
  }

  exibirEdicao(idObservacao: number) {
    this._router.navigate(['../', idObservacao, 'editar'], { relativeTo: this._route });
  }

  public getListaPaginada(listap, medicoesAgrupadas: boolean) {
    if (!medicoesAgrupadas) {
      this.lista = listap;
    } else {
      this.listaObservacoesMedicoesAgrupadas = listap;
    }
  }

  public expandedObs(indiceElementoSelecionado: number, medicoesAgrupadas: boolean) {

    let observacao;
    if (!medicoesAgrupadas) {
      observacao = this.lista[indiceElementoSelecionado];
    } else {
      observacao = this.listaObservacoesMedicoesAgrupadas[indiceElementoSelecionado];
    }
    observacao.showDetail = !observacao.showDetail;
  }

  public prepararExclusao(template: TemplateRef<any>, idObsExcluir: number, versaoObsExcluir: number) {
    this.idObsExcluir = idObsExcluir;
    this.modalConfirmacaoExcluirRef = this._modalService.show(template, { class: 'modal-sm' });
  }

  public cancelarExclusao() {
    this.modalConfirmacaoExcluirRef.hide();
    this.idObsExcluir = null;
  }

  public confirmarExclusao() {
    this.modalConfirmacaoExcluirRef.hide();
    this.excluirObservacao();
  }

  public excluirObservacao() {
    this._observacaoService.excluirObservacao(this.medicao.id, this.idObsExcluir).subscribe(value => {
      this.adicionarMensagem('Observação excluída com sucesso.');
      this.initializeComponent();
    });
  }

  public navegarAbaMedicao() {
    this._router.navigate(['../../'], { relativeTo: this._route });
  }

  get empresa(): Empresa {
    return this._empresaService.empresaAtual;
  }

  public breakTextInLines(text): string {
    const value = this.createStringArray(text);
    return value.reduce((prevVal, elem) =>
      prevVal + elem + '\n', '');
  }

  public createStringArray(text: string): any[] {
    let value: any[] = [];
    for (let i = 0; i < (text.length / 80); i++) {
      value.push(text.substring(i * 80, 80 + i * 80));
    }
    return value;
  }

  isMedicaoAgrupada(): boolean {
    return this.medicao.idMedicaoAgrupadora != null;
  }

  isMedicaoBloqueada(): boolean {
    return this.medicao.bloqueada;
  }

  isEdicaoEmpresa(): boolean {
    return this.usuarioLogado.hasProfile([Profile.EMPRESA])
      && (this.medicao.situacao.codigo === SituacaoMedicaoEnum.EM_ELABORACAO || this.medicao.situacao.codigo === SituacaoMedicaoEnum.EM_COMPLEMENTACAO_PELA_EMPRESA);
  }

  isEdicaoConvenente(): boolean {
    return this.usuarioLogado.hasProfile([Profile.PROPONENTE])
      && (this.medicao.situacao.codigo === SituacaoMedicaoEnum.EM_ATESTE || this.medicao.situacao.codigo === SituacaoMedicaoEnum.EM_COMPLEMENTACAO_CONVENENTE);
  }

  isEdicaoConcedente(): boolean {
    return this.usuarioLogado.hasProfile([Profile.CONCEDENTE])
      && (this.medicao.situacao.codigo === SituacaoMedicaoEnum.EM_ANALISE_PELO_CONCEDENTE);
  }

  isEdicaoMandataria(): boolean {
    return this.usuarioLogado.hasProfile([Profile.MANDATARIA])
      && (this.medicao.situacao.codigo === SituacaoMedicaoEnum.EM_ANALISE_PELO_CONCEDENTE);
  }

  exibirOpcaoManutencao(): boolean {
    return !this.isParentDetail() && !this.isMedicaoAgrupada() && !this.isMedicaoBloqueada() && (this.isEdicaoEmpresa() || this.isEdicaoConvenente() || this.isEdicaoConcedente() || this.isEdicaoMandataria());
  }

  onCheckExibirInativoAgrupadora(event: any): void {
    this.exibirFiltroAnexoAgrupadora = !this.exibirFiltroAnexoAgrupadora;
    this.ngOnInit();
  }

  onCheckExibirInativoAgrupada(event: any): void {
    this.exibirFiltroAnexoAgrupada = !this.exibirFiltroAnexoAgrupada;
    this.ngOnInit();
  }

  permiteAtivarInativarAnexo(obs: Observacao): boolean {
    return this.canAccess("ativarAnexo") &&
        ((this.isEdicaoConvenente() && obs.inPerfilResponsavel.codigo === PerfilEnum.EMPRESA)
          || (this.isEdicaoConcedente() && obs.inPerfilResponsavel.codigo === PerfilEnum.CONVENENTE)
          || (this.isEdicaoMandataria() && obs.inPerfilResponsavel.codigo === PerfilEnum.CONVENENTE));
  }

  prepararAtivacaoAnexo(template: TemplateRef<any>, idObservacao: number, idAnexo: number, idMedicaoAgrupada: number) {
    this.modalConfirmacaoAtivacaoAnexoRef = this._modalService.show(template, { class: 'modal-sm' });

    this.mdIdAnexo = idAnexo;
    this.mdIdObservacao = idObservacao;
    this.mdIdMedicaoAgrupada = idMedicaoAgrupada;

  }

  ativarAnexo() {
    let idMedicaoAtivarAnexo: number;

    if (this.mdIdMedicaoAgrupada) {
      idMedicaoAtivarAnexo = this.mdIdMedicaoAgrupada;
    } else {
      idMedicaoAtivarAnexo = this.medicao.id;
    }

    this._observacaoService.ativarAnexoObservacao(this.mdIdObservacao, this.mdIdAnexo, idMedicaoAtivarAnexo).subscribe(value => {
      this.adicionarMensagem('Anexo ativado com sucesso.');
      this.initializeComponent();
    });
  }

  prepararInativacaoAnexo(template: TemplateRef<any>, idObservacao: number, idAnexo: number, idMedicaoAgrupada) {
    this.modalConfirmacaoInativacaoAnexoRef = this._modalService.show(template, { class: 'modal-sm' });

    this.mdIdAnexo = idAnexo;
    this.mdIdObservacao = idObservacao;
    this.mdIdMedicaoAgrupada = idMedicaoAgrupada;
  }

  inativarAnexo() {

    let idMedicaoInativarAnexo: number;

    if (this.mdIdMedicaoAgrupada) {
      idMedicaoInativarAnexo = this.mdIdMedicaoAgrupada;
    } else {
      idMedicaoInativarAnexo = this.medicao.id;
    }

    this._observacaoService.inativarAnexoObservacao(this.mdIdObservacao, this.mdIdAnexo, idMedicaoInativarAnexo).subscribe(value => {
      this.adicionarMensagem('Anexo inativado com sucesso.');
      this.initializeComponent();
    });
  }

  cancelarInativacaoAnexo() {
    this.modalConfirmacaoInativacaoAnexoRef.hide();
  }

  cancelarAtivacaoAnexo() {
    this.modalConfirmacaoAtivacaoAnexoRef.hide();
  }

  confirmarInativacaoAnexo() {
    this.modalConfirmacaoInativacaoAnexoRef.hide();
    this.inativarAnexo();
  }

  confirmarAtivacaoAnexo() {
    this.modalConfirmacaoAtivacaoAnexoRef.hide();
    this.ativarAnexo();
  }

  isUsuarioComPerfilEdicao(obs: Observacao): boolean {
    return obs.inPerfilResponsavel.codigo === PerfilEnum.EMPRESA && this.canAccess('editarEmpresa')
        || this.isUsuarioComPerfilManutencao(obs);
  }

  isUsuarioComPerfilExclusao(obs: Observacao): boolean {
    return obs.inPerfilResponsavel.codigo === PerfilEnum.EMPRESA && this.canAccess('excluirEmpresa')
        || this.isUsuarioComPerfilManutencao(obs);
  }

  private isUsuarioComPerfilManutencao(obs: Observacao): boolean {
    return obs.inPerfilResponsavel.codigo === PerfilEnum.CONVENENTE            && this.canAccess('manterConvenente')
        || obs.inPerfilResponsavel.codigo === PerfilEnum.CONCEDENTE            && this.canAccess('manterConcedente')
        || obs.inPerfilResponsavel.codigo === PerfilEnum.MANDATARIA            && this.canAccess('manterMandataria')
        || obs.inPerfilResponsavel.codigo === PerfilEnum.FISCAL_ACOMPANHAMENTO && this.canAccess('manterFiscalAcompanhamento') && !this.canAccess('manterConcedente')
        || obs.inPerfilResponsavel.codigo === PerfilEnum.TECNICO_TERCEIRO      && this.canAccess('manterTecnicoTerceiro')      && !this.canAccess('manterConcedente');
  }

  isParentDetail(): boolean {
    return this.acaoParent === 'detalhar';
  }

  voltar() {
    this._router.navigate(['./listar'], { relativeTo: this._route.parent.parent });
  }

  exibirExpansivelParaAnexosObs(obs: Observacao, exibirInativos: boolean): boolean {
    const filterAnexos = new FiltroAnexosPipe();
    const anexos = filterAnexos.transform(obs.anexos, exibirInativos);

    if (anexos.length > 1) {
      return true;
    }
    return false;
  }

}
