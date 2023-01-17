import { Component, Injector, TemplateRef, LOCALE_ID, Inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Contrato } from 'src/app/shared/model/contrato.model';
import { Empresa } from 'src/app/shared/model/empresa.model';
import { CnpjPipe } from 'src/app/shared/pipes/cnpj.pipe';
import { ContratoService } from 'src/app/shared/services/contrato.service';
import { EmpresaService } from 'src/app/shared/services/empresa.service';
import { SharedService } from 'src/app/shared/services/shared-service.service';
import { Medicao } from '../../shared/model/medicao.model';
import { Permission } from '../../shared/model/security/permission.enum';
import { Profile } from '../../shared/model/security/profile.enum';
import { RequiredAuthorizer } from '../../shared/model/security/required-authorizer.model';
import { MedicaoService } from '../../shared/services/medicao.service';
import { BsModalRef, BsModalService } from 'ngx-bootstrap';
import { DataExport } from 'src/app/shared/model/data-export';
import { formatDate, formatCurrency, formatNumber } from '@angular/common';
import { Role } from 'src/app/shared/model/security/role.enum';
import { MedicaoListagemDecorator } from '../../shared/model/medicao-listagem-decorator.model';
import { ListagemExpansivelComponent } from 'src/app/shared/util/listagem-expansivel.component';
import { ModalidadeEnum } from 'src/app/shared/enum/modalidade.enum';

@Component({
  selector: 'app-listagem-medicao',
  templateUrl: './listagem-medicao.component.html',
  styleUrls: ['./listagem-medicao.component.scss']
})
export class ListagemMedicaoComponent extends ListagemExpansivelComponent {

  data: MedicaoListagemDecorator[];
  lista: any[];
  fileExportName = 'Medições';
  export: DataExport;
  dataExport: any[] = [];
  exibeBotaoCriarMedicao = false;
  modalConfirmacaoExcluirRef: BsModalRef;
  modalConfirmacaoIniciarAtesteRef: BsModalRef;
  modalConfirmacaoIniciarComplementacaoRef: BsModalRef;
  modalConfirmacaoIniciarAnaliseRef: BsModalRef;

  idMedicaoExcluir: number;
  sitMedicaoExcluir: string;
  exclusaoAgrupada: boolean;

  idMedicaoCancelar: number;
  modalConfirmacaoCancelamentoRef: BsModalRef;
  nomeAcaoCancelamento = "";

  msgMedicaoIniciarAteste: string;
  idMedicaoIniciarAteste: number;

  msgMedicaoIniciarComplementacao: string;
  idMedicaoIniciarComplementacao: number;

  msgMedicaoIniciarAnalise: string;
  idMedicaoIniciarAnalise: number;

  constructor(
    private _medicaoService: MedicaoService,
    private _router: Router,
    private _modalService: BsModalService,
    private _route: ActivatedRoute,
    private _sharedService: SharedService,
    private _empresaService: EmpresaService,
    private _contratoService: ContratoService,
    private _cnpj: CnpjPipe,
    @Inject(LOCALE_ID) private locale: string,
    private injector: Injector
  ) {
    super(injector);
  }

  get contrato(): Contrato {
    return this._contratoService.contratoAtual;
  }

  get empresa(): Empresa {
    return this._empresaService.empresaAtual;
  }

  loadPermissions(): Map<string, RequiredAuthorizer> {
    const profileEmpresa = [Profile.EMPRESA];
    const profileConvenente = [Profile.PROPONENTE];
    const profileConcedenteMandataria = [Profile.CONCEDENTE, Profile.MANDATARIA];

    return new Map([
      [
        'incluir',
        new RequiredAuthorizer(profileEmpresa, [], [Permission.INCLUIR_MEDICAO])
      ],
      [
        'editarEmpresa',
        new RequiredAuthorizer(profileEmpresa, [], [Permission.EDITAR_MEDICAO])
      ],
      [
        'excluirEmpresa',
        new RequiredAuthorizer(profileEmpresa, [], [Permission.EXCLUIR_MEDICAO])
      ],
      [
        'excluirConvenente',
        new RequiredAuthorizer(profileConvenente, [Role.FISCAL_CONVENENTE,
          Role.GESTOR_CONVENIO_CONVENENTE,
          Role.GESTOR_FINANCEIRO_CONVENENTE,
          Role.OPERADOR_FINANCEIRO_CONVENENTE], [])
      ],
      [
        'excluirAdministrador',
        new RequiredAuthorizer(profileConcedenteMandataria, [Role.ADMINISTRADOR_SISTEMA,
          Role.ADMINISTRADOR_SISTEMA_ORGAO_EXTERNO], [])
      ],
      [
        'cancelarEnvioConvenente',
        new RequiredAuthorizer(profileEmpresa, [], [Permission.CANCELAR_ENVIO_MEDICAO_CONVENENTE])
      ],
      [
        'iniciarAteste',
        new RequiredAuthorizer(profileConvenente, [Role.FISCAL_CONVENENTE,
        Role.GESTOR_CONVENIO_CONVENENTE,
        Role.GESTOR_FINANCEIRO_CONVENENTE,
        Role.OPERADOR_FINANCEIRO_CONVENENTE], [])
      ],
      [
        'editarConvenente',
        new RequiredAuthorizer(profileConvenente, [Role.FISCAL_CONVENENTE,
        Role.GESTOR_CONVENIO_CONVENENTE,
        Role.GESTOR_FINANCEIRO_CONVENENTE,
        Role.OPERADOR_FINANCEIRO_CONVENENTE], [])
      ],
      [
        'cancelarEnvioConcedente',
        new RequiredAuthorizer(profileConcedenteMandataria, [Role.ADMINISTRADOR_SISTEMA,
        Role.ADMINISTRADOR_SISTEMA_ORGAO_EXTERNO], [])
      ],
      [
        'cancelarEnvioParaComplementacaoEmpresa',
        new RequiredAuthorizer(profileConvenente, [Role.FISCAL_CONVENENTE,
        Role.GESTOR_CONVENIO_CONVENENTE,
        Role.GESTOR_FINANCEIRO_CONVENENTE,
        Role.OPERADOR_FINANCEIRO_CONVENENTE], [])
      ],
      [
        'iniciarAnalise',
        new RequiredAuthorizer(profileConcedenteMandataria, [Role.FISCAL_CONCEDENTE,
        Role.GESTOR_CONVENIO_CONCEDENTE,
        Role.GESTOR_FINANCEIRO_CONCEDENTE,
        Role.OPERACIONAL_CONCEDENTE,
        Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA,
        Role.FISCAL_ACOMPANHAMENTO,
        Role.TECNICO_TERCEIRO], [])
      ],
      [
        'editarConcedenteMandataria',
        new RequiredAuthorizer(profileConcedenteMandataria, [Role.FISCAL_CONCEDENTE,
        Role.GESTOR_CONVENIO_CONCEDENTE,
        Role.GESTOR_FINANCEIRO_CONCEDENTE,
        Role.OPERACIONAL_CONCEDENTE,
        Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA,
        Role.FISCAL_ACOMPANHAMENTO,
        Role.TECNICO_TERCEIRO,
        Role.ADMINISTRADOR_SISTEMA,
        Role.ADMINISTRADOR_SISTEMA_ORGAO_EXTERNO], [])
      ],
      [
        'cancelarEnvioParaComplementacaoConvenente',
        new RequiredAuthorizer(profileConcedenteMandataria, [Role.ADMINISTRADOR_SISTEMA,
        Role.ADMINISTRADOR_SISTEMA_ORGAO_EXTERNO,
        Role.FISCAL_CONCEDENTE,
        Role.GESTOR_CONVENIO_CONCEDENTE,
        Role.GESTOR_FINANCEIRO_CONCEDENTE,
        Role.OPERACIONAL_CONCEDENTE,
        Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA,
        Role.FISCAL_ACOMPANHAMENTO,
        Role.TECNICO_TERCEIRO], [])
      ],
      [
        'cancelarAceite',
        new RequiredAuthorizer(profileConcedenteMandataria, [Role.FISCAL_CONCEDENTE,
        Role.GESTOR_CONVENIO_CONCEDENTE,
        Role.GESTOR_FINANCEIRO_CONCEDENTE,
        Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA,
        Role.FISCAL_ACOMPANHAMENTO,
        Role.TECNICO_TERCEIRO], [])
      ]
    ]);
  }

  initializeComponent() {
    this.emitirTitulo();

    const columns = ['Número', 'Período', 'Realizado Empresa Período R$', 'Realizado Empresa Período %',
      'Realizado Empresa Acumulado R$', 'Realizado Empresa Acumulado %', 'Realizado Convenente Período R$',
      'Realizado Convenente Período %', 'Realizado Convenente Acumulado R$', 'Realizado Convenente Acumulado %',
      'Realizado Concedente Período R$', 'Realizado Concedente Período %', 'Realizado Concedente Acumulado R$',
      'Realizado Concedente Acumulado %', 'Situação'];

    this._medicaoService.listarMedicoes(this.contrato.id).subscribe(
      value => {
        this.data = this.montarMedicoesAgrupadas(value);
        this.carregarDadosExportacao(value);
        this.export = new DataExport(columns, this.dataExport);
        this.verificarExibicaoBotaoCriarMedicao();
      }
    );
  }

  private verificarExibicaoBotaoCriarMedicao(): void {

    this.exibeBotaoCriarMedicao = false;

    if (this.canAccess('incluir') && !this.existeMedicaoComSituacaoEmpresa()) {

      this._contratoService.isContratoParalisado(this.contrato.id).subscribe(paralisado => {

        if (!paralisado && !this.existeMedicao()) {

          this.exibeBotaoCriarMedicao = true;

        } else if (!paralisado && this.existeMedicao()) {

          this._contratoService.temSubmetasAExecutar(this.contrato.id).subscribe(temSubmetasAExecutar => {
            this.exibeBotaoCriarMedicao = temSubmetasAExecutar;
          });
        }
      });
    }
  }

  private existeMedicaoComSituacaoEmpresa(): boolean {
    return this.data.some(
      (decorator) =>
        decorator.medicao.situacao.codigo === 'EM'  ||
        decorator.medicao.situacao.codigo === 'ECE' ||
        decorator.medicao.situacao.codigo === 'CE'
    );
  }

  private existeMedicao(): boolean {
    return this.data.length > 0;
  }

  carregarDadosExportacao(listaMedicao: Medicao[]) {
    listaMedicao.reverse().forEach(element => {
      const linha = [];
      linha.push(element.sequencial);

      if (element.dataFim) {
        linha.push(formatDate(element.dataInicio, 'dd/MM/yyyy', this.locale) +
          ' a ' + formatDate(element.dataFim, 'dd/MM/yyyy', this.locale));
      } else {
        if (element.dataInicio != null) {
          linha.push(formatDate(element.dataInicio, 'dd/MM/yyyy', this.locale));
        } else {
          linha.push('');
        }
      }
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

      linha.push(element.situacao.descricao);
      this.dataExport.push(linha);
    });
  }

  emitirTitulo() {
    const tituloTela = {
      titulo: 'Medições',
      subtitulo: 'Lista de Medições Efetuadas',
      info:
        this._cnpj.transform(this.empresa.cnpj) +
        ' - ' +
        this.empresa.razaoSocial
    };

    this._sharedService.emitChange(tituloTela);
  }

  public getListaPaginada(listap) {
    this.lista = listap;
  }

  async criarMedicao() {
    this._router.navigate(['../incluir'], { relativeTo: this._route });
  }

  voltar() {
    const rootModule = this._route.snapshot.pathFromRoot[1].url[0].path;

    if (rootModule === 'preenchimento') {
      this._router.navigate(['/preenchimento/empresa', this.empresa.id]);

    } else {
      this._router.navigate(['/acompanhamento/proposta', this.contrato.propostaFk]);
    }
  }

  prepararDetalhamento(idMedicao: number) {
    this._router.navigate(['../', idMedicao, 'detalhar'], { relativeTo: this._route });
  }

  prepararEdicao(idMedicao: number, isBloqueada: boolean) {
    if (isBloqueada) {
      this._messageService.warn('Não é permitido realizar operações de manutenção em Medição bloqueada');
    } else {
      this._router.navigate(['../', idMedicao, 'editar'], { relativeTo: this._route });
    }
  }

  prepararExclusao(template: TemplateRef<any>, medParam: MedicaoListagemDecorator) {
    this.idMedicaoExcluir = medParam.medicao.id;
    this.sitMedicaoExcluir = medParam.medicao.situacao.descricao
    this.exclusaoAgrupada = medParam.filhas.length > 0;
    this.modalConfirmacaoExcluirRef = this._modalService.show(template, { class: 'modal-sm' });
  }

  public theBoundCallback: Function;

  prepararCancelamento(template: TemplateRef<any>, idCancelar: number, callbackFunction: Function, acao: string) {
    this.idMedicaoCancelar = idCancelar;
    this.theBoundCallback = callbackFunction;
    this.nomeAcaoCancelamento = acao;
    this.modalConfirmacaoCancelamentoRef = this._modalService.show(template, { class: 'modal-sm' });
  }

  cancelarAcaoCancelamento() {
    this.modalConfirmacaoCancelamentoRef.hide();
    this.idMedicaoCancelar = null;
  }

  confirmarAcaoCancelamento() {
    this.modalConfirmacaoCancelamentoRef.hide();
    this.theBoundCallback();
  }

  cancelarAceite() {
    this._medicaoService.cancelarAceite(this.idMedicaoCancelar).subscribe(value => {
      this.adicionarMensagem('Aceite da medição cancelado com sucesso.');
      this.initializeComponent();
    });
  }

  cancelarAteste() {
    this._medicaoService.cancelarAteste(this.idMedicaoCancelar).subscribe(value => {
      this.adicionarMensagem('Ateste da medição cancelado com sucesso.');
      this.initializeComponent();
    });
  }

  cancelarEnvioConvenente() {
    this._medicaoService.cancelarEnvioConvenente(this.idMedicaoCancelar).subscribe(value => {
      this.adicionarMensagem('Envio da medição cancelado com sucesso.');
      this.initializeComponent();
    });
  }

  cancelarEnvioParaComplementacao() {
    this._medicaoService.cancelarEnvioParaComplementacao(this.idMedicaoCancelar).subscribe(value => {
      this.adicionarMensagem('Envio para complementação cancelado com sucesso.');
      this.initializeComponent();
    });
  }

  cancelarExclusao() {
    this.modalConfirmacaoExcluirRef.hide();
    this.idMedicaoExcluir = null;
    this.sitMedicaoExcluir = null;
    this.exclusaoAgrupada = null;
  }

  confirmarExclusao() {
    this.modalConfirmacaoExcluirRef.hide();
    this.excluirMedicao();
  }

  excluirMedicao() {
    this._medicaoService.excluir(this.idMedicaoExcluir).subscribe(value => {
      this.adicionarMensagem('Medição excluída com sucesso.');
      this.initializeComponent();
    });
  }

  exibirOpcaoExcluir(medicao: Medicao): boolean {
    return this.canAccess('excluirEmpresa') && (medicao.situacao.codigo === 'EM' || medicao.situacao.codigo === 'CE')
           || this.canAccess('excluirConvenente') && (medicao.situacao.codigo === 'AT' || medicao.situacao.codigo === 'CC')
           || this.canAccess('excluirAdministrador');
  }

  exibirOpcaoCancelarEnvioConvenente(medicao: Medicao): boolean {
    return this.canAccess('cancelarEnvioConvenente') && medicao.situacao.codigo === 'EC';
  }

  exibirOpcaoCancelarAteste(medicao: Medicao): boolean {
    return this.canAccess('cancelarEnvioConcedente') && medicao.situacao.codigo === 'ATD';
  }

  exibirOpcaoCancelarEnvioParaComplementacaoEmpresa(medicao: Medicao): boolean {
    return this.canAccess('cancelarEnvioParaComplementacaoEmpresa') && medicao.situacao.codigo === 'ECE';
  }

  exibirOpcaoCancelarEnvioParaComplementacaoConvenente(medicao: Medicao): boolean {
    return this.canAccess('cancelarEnvioParaComplementacaoConvenente') && medicao.situacao.codigo === 'ECC';
  }

  navegarAbaHistorico() {
    this._router.navigate(['../historico/listar'], { relativeTo: this._route });
  }

  prepararModalConfirmacaoIniciarComplementacao(template: TemplateRef<any>, idMedicao: number) {
    this.msgMedicaoIniciarComplementacao = 'Complementação da medição será iniciada. Deseja prosseguir?';
    this.modalConfirmacaoIniciarComplementacaoRef = this._modalService.show(template, { class: 'modal-sm' });
    this.idMedicaoIniciarComplementacao = idMedicao;
  }

  cancelarIniciarComplementacao() {
    this.modalConfirmacaoIniciarComplementacaoRef.hide();
  }

  iniciarComplementacao() {
    this.modalConfirmacaoIniciarComplementacaoRef.hide();

    this._medicaoService.iniciarComplementacao(this.idMedicaoIniciarComplementacao).subscribe(value => {
      super.adicionarMensagem('Complementação da medição iniciada com sucesso.', true);
      this._router.navigate(['../', this.idMedicaoIniciarComplementacao, 'editar'], { relativeTo: this._route });
    });
  }

  prepararModalConfirmacaoIniciarAteste(template: TemplateRef<any>, medicaoListagem: MedicaoListagemDecorator) {
    this.msgMedicaoIniciarAteste = this.obterMsgIniciarAtesteMedicao(medicaoListagem);
    this.modalConfirmacaoIniciarAtesteRef = this._modalService.show(template, { class: 'modal-sm' });
  }

  obterMsgIniciarAtesteMedicao(medParam: MedicaoListagemDecorator): string {

    const situacaoLista: any = this.possuiMedicaoAnterior(medParam);
    this.idMedicaoIniciarAteste = medParam.medicao.id;

    if (!situacaoLista) {
      return 'Deseja iniciar o ateste da medição?';
    } else if (situacaoLista.haMedicaoAnteriorEmAteste) {
      return 'Deseja iniciar o ateste acumulado da medição? Ressalta-se que os dados informados pelo Convenente na medição acumulada "Em Ateste" serão perdidos, exceto as Observações não bloqueadas que serão migradas para medição acumuladora.';
    } else if (situacaoLista.haMedicaoAnteriorEnviadaConvenente) {
      return 'Deseja iniciar o ateste acumulado da medição?';
    }
  }

  possuiMedicaoAnterior(medParam: MedicaoListagemDecorator): any {

    let retorno: any;

    this.lista.forEach((medIteracao: MedicaoListagemDecorator) => {
      if (medIteracao.medicao.sequencial < medParam.medicao.sequencial) {
        if (medIteracao.medicao.situacao.codigo === 'AT') {
          retorno = { haMedicaoAnteriorEmAteste: true };
        } else if (medIteracao.medicao.situacao.codigo === 'EC') {
          retorno = { haMedicaoAnteriorEnviadaConvenente: true };
        }
      }
    });

    return retorno;
  }

  cancelarEnviarAteste() {
    this.modalConfirmacaoIniciarAtesteRef.hide();
  }

  iniciarAteste() {
    this.modalConfirmacaoIniciarAtesteRef.hide();

    this._medicaoService.iniciarAteste(this.idMedicaoIniciarAteste).subscribe(value => {
      super.adicionarMensagem('Ateste da medição iniciado com sucesso.', true);
      this._router.navigate(['../', this.idMedicaoIniciarAteste, 'editar'], { relativeTo: this._route });
    });
  }

  /* INICIO INICIAR ANÁLISE CONCEDENTE/MANDATÁRIA */

  acompanhaPropostaDaModalidade() {
    if (this.isConcedente() || this.isMandataria()) {
      return true;
    } else {
      return false;
    }
  }

  isConcedente() {
    return this.usuarioLogado.hasProfile([Profile.CONCEDENTE]);
  }

  isMandataria() {
    return this.usuarioLogado.hasProfile([Profile.MANDATARIA]) &&
      (this.contrato.modalidade.codigo === ModalidadeEnum.CONTRATO_DE_REPASSE ||
        (this.contrato.modalidade.codigo === ModalidadeEnum.TERMO_COMPROMISSO && this.contrato.modalidade.possuiInstituicaoMandataria));
  }

  prepararModalConfirmacaoIniciarAnalise(template: TemplateRef<any>, medicaoListagem: MedicaoListagemDecorator) {
    this.msgMedicaoIniciarAnalise = this.obterMsgIniciarAnaliseMedicao(medicaoListagem);
    this.modalConfirmacaoIniciarAnaliseRef = this._modalService.show(template, { class: 'modal-sm' });
  }

  obterMsgIniciarAnaliseMedicao(medicaoListagemParam: MedicaoListagemDecorator): string {
    const situacaoLista: any = this.possuiMedicaoAnteriorEmAnalise(medicaoListagemParam);
    this.idMedicaoIniciarAnalise = medicaoListagemParam.medicao.id;

    if (!situacaoLista && !medicaoListagemParam.filhas.length) {
      return 'Deseja iniciar a análise da medição?';
    } else if (!situacaoLista && medicaoListagemParam.filhas.length) {
      return 'Deseja iniciar a análise acumulada da medição?';
    } else if (situacaoLista.haMedicaoAnteriorAtestadaComplementacaoOriginadaAnalise) {
      return 'Deseja iniciar a análise acumulada da medição? Ressalta-se que com exceção das observações, os dados informados pelo Concedente/Mandatária na medição acumulada "Atestada", que foi objeto de uma complementação do Convenente solicitada anteriormente pelo Concedente/Mandatária, serão perdidos.';
    } else if (situacaoLista.haMedicaoAnteriorEmAnalise) {
      return 'Deseja iniciar a análise acumulada da medição? Ressalta-se que os dados informados pelo Concedente/Mandatária na medição acumulada "Em Análise pelo Concedente/Mandatária" serão perdidos, exceto as Observações não bloqueadas que serão migradas para medição acumuladora.';
    } else if (situacaoLista.haMedicaoAnteriorAtestada) {
      return 'Deseja iniciar a análise acumulada da medição?';
    }
  }

  possuiMedicaoAnteriorEmAnalise(medParam: MedicaoListagemDecorator): any {

    let retorno: any;

    this.lista.forEach((medIteracao: MedicaoListagemDecorator) => {
      if (medIteracao.medicao.sequencial < medParam.medicao.sequencial) {
        if (medIteracao.medicao.situacao.codigo === 'AC') {
          retorno = { haMedicaoAnteriorEmAnalise: true };
        } else if (medIteracao.medicao.situacao.codigo === 'ATD' && medIteracao.medicao.permiteComplementacaoValor == null) {
          retorno = { haMedicaoAnteriorAtestada: true };
        } else if (medIteracao.medicao.situacao.codigo === 'ATD' && medIteracao.medicao.permiteComplementacaoValor != null &&  !medIteracao.medicao.permiteComplementacaoValor) {
          retorno = { haMedicaoAnteriorAtestadaComplementacaoOriginadaAnalise: true};
        } else if (medIteracao.medicao.situacao.codigo === 'ATD' && medIteracao.medicao.permiteComplementacaoValor != null &&  medIteracao.medicao.permiteComplementacaoValor) {
          retorno = { haMedicaoAnteriorAtestada: true};
        }
      }
    });

    return retorno;
  }

  cancelarIniciarAnalise() {
    this.modalConfirmacaoIniciarAnaliseRef.hide();
  }

  iniciarAnalise() {
    this.modalConfirmacaoIniciarAnaliseRef.hide();

    this._medicaoService.iniciarAnalise(this.idMedicaoIniciarAnalise).subscribe(value => {
      super.adicionarMensagem('Análise da medição iniciada com sucesso.', true);
      this._router.navigate(['../', this.idMedicaoIniciarAnalise, 'editar'], { relativeTo: this._route });
    });
  }
  /* FIM INICIAR ANÁLISE CONCEDENTE/MANDATÁRIA */

  public expandeContraiMedicao(indiceElementoSelecionado: number) {
    const medicaoListagemDecorator: MedicaoListagemDecorator = this.data[indiceElementoSelecionado];
    medicaoListagemDecorator.showDetail = !medicaoListagemDecorator.showDetail;
  }

  /**
 * Agrupa as medicções filhas, se houver, na medição agrupadora.
 *
 * medicoes
 */
  public montarMedicoesAgrupadas(medicoes: Medicao[]): MedicaoListagemDecorator[] {

    const medicaoListagemDecorator: MedicaoListagemDecorator[] = medicoes.map(med => new MedicaoListagemDecorator(med));

    return medicaoListagemDecorator.reverse().filter((medListagem: MedicaoListagemDecorator) => {

      // Verifica se a medição possui uma agrupadora, portanto ela é filha.
      if (medListagem.medicao.idMedicaoAgrupadora) {
        // Encontra a medição Agrupadora
        const medAgrupadoraRetornada = medicaoListagemDecorator.find((medAgrupadora: MedicaoListagemDecorator) => {
          return medAgrupadora.medicao.id === medListagem.medicao.idMedicaoAgrupadora;
        });

        medAgrupadoraRetornada.filhas.push(medListagem);

        return false;
      } else {
        return true;
      }
    });
  }

}
