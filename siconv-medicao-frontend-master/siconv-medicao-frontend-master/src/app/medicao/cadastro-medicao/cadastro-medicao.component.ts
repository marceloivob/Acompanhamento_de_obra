import { SituacaoMedicaoEnum } from 'src/app/shared/enum/situacao-medicao.enum';
import { DatePipe } from '@angular/common';
import { Component, Injector, TemplateRef, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BsModalRef, BsModalService } from 'ngx-bootstrap';
import { Contrato } from 'src/app/shared/model/contrato.model';
import { Empresa } from 'src/app/shared/model/empresa.model';
import { CnpjPipe } from 'src/app/shared/pipes/cnpj.pipe';
import { EmpresaService } from 'src/app/shared/services/empresa.service';
import { MedicaoService } from 'src/app/shared/services/medicao.service';
import { BaseComponent } from 'src/app/shared/util/base.component';
import { Medicao } from '../../shared/model/medicao.model';
import { Permission } from '../../shared/model/security/permission.enum';
import { Role } from 'src/app/shared/model/security/role.enum';
import { Profile } from '../../shared/model/security/profile.enum';
import { RequiredAuthorizer } from '../../shared/model/security/required-authorizer.model';
import { ContratoService } from '../../shared/services/contrato.service';
import { SharedService } from '../../shared/services/shared-service.service';
import { FormDadosMedicaoComponent } from './form-dados-medicao/form-dados-medicao.component';
import { DateUtil } from 'src/app/shared/util/date-util';

@Component({
  selector: 'app-cadastro-medicao',
  templateUrl: './cadastro-medicao.component.html'
})
export class CadastroMedicaoComponent extends BaseComponent {
  @ViewChild(FormDadosMedicaoComponent)
  private formDadosMedicaoComponent: FormDadosMedicaoComponent;

  acao: string;

  medicao: Medicao;

  dadosVistoria: Medicao;

  existeSubmetaAssinadaEmpresa = false;
  existeSubmetaAssinadaConvenente = false;
  existeSubmetaAssinadaConcedente = false;
  permiteSolicitarComplemetacao = false;
  modalConfirmacaoEnviarRef: BsModalRef;
  modalConfirmacaoEnviarComplementacaoRef: BsModalRef;
  modalConfirmacaoAtestarRef: BsModalRef;
  modalConfirmacaoComplementacaoRef: BsModalRef;
  modalConfirmacaoAceitarRef: BsModalRef;

  constructor(
    private _route: ActivatedRoute,
    private _router: Router,
    private _sharedService: SharedService,
    private _contratoService: ContratoService,
    private _medicaoService: MedicaoService,
    private _modalService: BsModalService,
    private _cnpjPipe: CnpjPipe,
    private _empresaService: EmpresaService,
    private _datepipe: DatePipe,
    injector: Injector
  ) {
    super(injector);
  }

  initializeComponent() {
    const url = this._route.snapshot.url;
    this.acao = url[url.length - 1].path;
    this.inicializarMedicao();
  }

  async inicializarMedicao() {
    if (this.acao === 'editar' || this.acao === 'detalhar') {
      this.medicao = this._route.snapshot.data.medicao;

      this.emitirTitulo(this.medicao.sequencial);

      if ((this.medicao.situacao.codigo === 'AT' || this.medicao.situacao.codigo === 'CC' || this.medicao.situacao.codigo === 'AC') && this.acao === 'editar') {
        this._medicaoService.permiteSolicitarComplemetacao(this.medicao.id).subscribe(
          value => {
            this.permiteSolicitarComplemetacao = value;
          }
        );
      }

    } else {

      this._medicaoService.obterUltimaMedicao(this.contrato.id).subscribe(ultimaMedicao => {

        this.medicao = new Medicao();

        if (ultimaMedicao) {
          this.medicao.sequencial = ultimaMedicao.sequencial + 1;
          this.medicao.dataInicioObra = new Date(ultimaMedicao.dataInicioObra);
          this.medicao.dataInicio = new Date(DateUtil.adicionarDia(ultimaMedicao.dataFim, 1));

        } else {
          this.medicao.sequencial = 1;
        }

        this.emitirTitulo(this.medicao.sequencial);
      }
      );
    }
  }

  loadPermissions(): Map<string, RequiredAuthorizer> {
    const profileEmp = [Profile.EMPRESA];
    const roles = [Profile.PROPONENTE];
    const profileConcedenteMandataria = [Profile.CONCEDENTE, Profile.MANDATARIA];
    const profileConcedente = [Profile.CONCEDENTE];


    return new Map([
      ['editar', new RequiredAuthorizer(profileEmp, [], [Permission.EDITAR_MEDICAO])],
      ['incluir', new RequiredAuthorizer(profileEmp, [], [Permission.INCLUIR_MEDICAO])],
      ['enviar_medicao', new RequiredAuthorizer(profileEmp, [], [Permission.ENVIAR_MEDICAO_CONVENENTE])],
      ['atestar_medicao', new RequiredAuthorizer(roles, [Role.FISCAL_CONVENENTE, Role.GESTOR_CONVENIO_CONVENENTE,
      Role.GESTOR_FINANCEIRO_CONVENENTE, Role.OPERADOR_FINANCEIRO_CONVENENTE], [])],
      ['solicitar_complementacao_empresa', new RequiredAuthorizer(roles, [Role.FISCAL_CONVENENTE, Role.GESTOR_CONVENIO_CONVENENTE,
      Role.GESTOR_FINANCEIRO_CONVENENTE, Role.OPERADOR_FINANCEIRO_CONVENENTE], [])],
      ['solicitar_complementacao_convenente', new RequiredAuthorizer(profileConcedenteMandataria, [Role.FISCAL_CONCEDENTE, Role.GESTOR_CONVENIO_CONCEDENTE, Role.GESTOR_FINANCEIRO_CONCEDENTE, Role.OPERACIONAL_CONCEDENTE, Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA, Role.FISCAL_ACOMPANHAMENTO, Role.TECNICO_TERCEIRO], [])],
      ['solicitar_complementacao_convenente_admin', new RequiredAuthorizer(profileConcedente, [Role.ADMINISTRADOR_SISTEMA, Role.ADMINISTRADOR_SISTEMA_ORGAO_EXTERNO], [])],
      ['aceitar_medicao', new RequiredAuthorizer(profileConcedenteMandataria, [Role.FISCAL_CONCEDENTE, Role.GESTOR_CONVENIO_CONCEDENTE, Role.GESTOR_FINANCEIRO_CONCEDENTE, Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA, Role.FISCAL_ACOMPANHAMENTO, Role.TECNICO_TERCEIRO], [])],
      ['salvar_dados_vistoria', new RequiredAuthorizer(profileConcedenteMandataria, [Role.FISCAL_CONCEDENTE, Role.GESTOR_CONVENIO_CONCEDENTE, Role.GESTOR_FINANCEIRO_CONCEDENTE, Role.OPERACIONAL_CONCEDENTE, Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA, Role.FISCAL_ACOMPANHAMENTO, Role.TECNICO_TERCEIRO], [])],

    ]);
  }

  salvarMedicao() {
    const controlDtFimMedicao = this.formDadosMedicaoComponent.medicaoForm.get('dtFimMedicao');
    controlDtFimMedicao.updateValueAndValidity({ onlySelf: true });

    this.medicao.dataInicioObra = this.formDadosMedicaoComponent.medicaoForm.value['dtInicioObra'];
    this.medicao.dataInicio = this.formDadosMedicaoComponent.medicaoForm.value['dtInicioMedicao'];
    this.medicao.dataFim = this.formDadosMedicaoComponent.medicaoForm.value['dtFimMedicao'];

    const controlDtInicioObra = this.formDadosMedicaoComponent.medicaoForm.get('dtInicioObra');

    const modoInclusao = this.medicao.id ? false : true;

    if (!controlDtFimMedicao.valid) {
      controlDtFimMedicao.markAsTouched({ onlySelf: true });
    } else {
      if (!controlDtInicioObra.valid) {
        controlDtInicioObra.markAsTouched({ onlySelf: true });
      } else {
        controlDtFimMedicao.markAsTouched({ onlySelf: true });
        this._medicaoService.salvarMedicao(this.medicao, this.contrato.id)
          .subscribe(
            value => {
              this.medicao = value.data;
              if (modoInclusao) {
                // Redireciona para a rota de alteracao
                if (this.medicao.situacao.codigo === 'EM') {
                  super.adicionarMensagem('Medição salva com sucesso.', true);
                } else {
                  super.adicionarMensagem('Complementação da medição salva com sucesso.', true);
                }
                this._router.navigate(['../', this.medicao.id, 'editar'], { relativeTo: this._route });
              } else {
                if (this.medicao.situacao.codigo === 'EM') {
                  super.adicionarMensagem('Medição salva com sucesso.');
                } else {
                  super.adicionarMensagem('Complementação da medição salva com sucesso.');
                }
              }
            }
          );
      }
    }
  }

  enviar(template: TemplateRef<any>) {
    const controlDtFimMedicao = this.formDadosMedicaoComponent.medicaoForm.get('dtFimMedicao');
    this.formDadosMedicaoComponent.dtFimObrigatoria = true;
    this.formDadosMedicaoComponent.medicaoForm.controls['dtFimMedicao'].updateValueAndValidity({ onlySelf: true });

    if (!controlDtFimMedicao.valid) {
      controlDtFimMedicao.markAsTouched({ onlySelf: true });
    } else {
      if (this.medicao.situacao.codigo === 'CE') {
        this.modalConfirmacaoEnviarComplementacaoRef = this._modalService.show(template, { class: 'modal-sm' });
      } else {
        this.modalConfirmacaoEnviarRef = this._modalService.show(template, { class: 'modal-sm' });
      }
    }

    this.formDadosMedicaoComponent.dtFimObrigatoria = false;
  }

  atestar(template: TemplateRef<any>) {
    this.modalConfirmacaoAtestarRef = this._modalService.show(template, { class: 'modal-sm' });
  }

  aceitar(template: TemplateRef<any>) {
    this.formDadosMedicaoComponent.dtVistoriaObrigatoria = true;
    this.formDadosMedicaoComponent.medicaoForm.controls['dtVistoria'].updateValueAndValidity({ onlySelf: true });
    this.carregarDadosVistoria();
    if (this.validarDadosVistoria(true)) {
      this.modalConfirmacaoAceitarRef = this._modalService.show(template, { class: 'modal-sm' });
    }
    this.formDadosMedicaoComponent.dtVistoriaObrigatoria = false;
  }

  solicitarComplementacao(template: TemplateRef<any>) {
    this.modalConfirmacaoComplementacaoRef = this._modalService.show(template, { class: 'modal-sm' });
  }


  confirmarEnviar() {
    this.modalConfirmacaoEnviarRef.hide();
    this.enviarMedicao();
  }

  confirmarEnviarComplementacao() {
    this.modalConfirmacaoEnviarComplementacaoRef.hide();
    this.enviarMedicao();
  }

  confirmarAtestar() {
    this.modalConfirmacaoAtestarRef.hide();
    this.atestarMedicao();
  }

  confirmarAceitar() {
    this.modalConfirmacaoAceitarRef.hide();
    this.aceitarMedicao();
  }

  confirmarSolicitacaoComplementacaoEmpresa() {
    this.modalConfirmacaoComplementacaoRef.hide();
    this.solicitarComplementoEmpresa();
  }

  confirmarSolicitacaoComplementacaoConvenente() {
    this.modalConfirmacaoComplementacaoRef.hide();
    this.solicitarComplementoConvenente();
  }

  atestarMedicao() {
    this._medicaoService.atestarMedicao(this.medicao, this.medicao.id).subscribe(
      () => {
        if (this.medicao.situacao.codigo === 'AT') {
          super.adicionarMensagem('Medição atestada com sucesso.', true);
        } else {
          super.adicionarMensagem('Complementação enviada com sucesso.', true);
        }
        this._router.navigate(['./', this.medicao.id, 'detalhar'], { relativeTo: this._route.parent });
      }
    );
  }

  solicitarComplementoEmpresa() {
    this._medicaoService.solicitarComplementoEmpresa(this.medicao, this.medicao.id).subscribe(
      () => {
        super.adicionarMensagem('Solicitação de complementação efetuada com sucesso.', true);
        this._router.navigate(['./listar'], { relativeTo: this._route.parent });
      }
    );
  }

  solicitarComplementoConvenente() {
    this.carregarDadosVistoria();
    if (this.validarDadosVistoria(false)) {
      this._medicaoService.solicitarComplementoConvenente(this.dadosVistoria, this.medicao.id).subscribe(
        () => {
          super.adicionarMensagem('Solicitação de complementação efetuada com sucesso.', true);
          this._router.navigate(['./listar'], { relativeTo: this._route.parent });
        }
      );
    }
  }

  aceitarMedicao() {
    this.carregarDadosVistoria();
    if (this.validarDadosVistoria(true)) {
      this._medicaoService.aceitarMedicao(this.dadosVistoria, this.medicao.id).subscribe(
        () => {
          super.adicionarMensagem('Medição aceita com sucesso.', true);
          this._router.navigate(['./', this.medicao.id, 'detalhar'], { relativeTo: this._route.parent });
        }
      );
    }
  }

  enviarMedicao() {
    this.medicao.dataInicioObra = this.formDadosMedicaoComponent.medicaoForm.value['dtInicioObra'];
    this.medicao.dataInicio = this.formDadosMedicaoComponent.medicaoForm.value['dtInicioMedicao'];
    this.medicao.dataFim = this.formDadosMedicaoComponent.medicaoForm.value['dtFimMedicao'];

    const controlDtInicioObra = this.formDadosMedicaoComponent.medicaoForm.get('dtInicioObra');
    const controlDtFimMedicao = this.formDadosMedicaoComponent.medicaoForm.get('dtFimMedicao');

    if (!controlDtInicioObra.valid) {
      controlDtInicioObra.markAsTouched({ onlySelf: true });
    }
    if (!controlDtFimMedicao.valid) {
      controlDtFimMedicao.markAsTouched({ onlySelf: true });
    } else if (this.validaDataFim() && this.validaDataInicioObra()) {
      this._medicaoService.enviarMedicao(this.medicao, this.medicao.id).subscribe(
        () => {
          if (this.medicao.situacao.codigo === 'EM') {
            super.adicionarMensagem('Medição enviada com sucesso.', true);
          } else {
            super.adicionarMensagem('Complementação enviada com sucesso.', true);
          }
          this._router.navigate(['./', this.medicao.id, 'detalhar'], { relativeTo: this._route.parent });
        }
      );
    }
  }

  emitirTitulo(idMedicao: number) {
    var tituloTela = {
      titulo: 'Medição ' + idMedicao,
      subtitulo: 'Escolha uma submeta abaixo para realizar medições.',
      info: this._cnpjPipe.transform(this.empresa.cnpj) + ' - ' + this.empresa.razaoSocial
    };
    if (this.isInclude()) {
      tituloTela = {
        titulo: 'Medição ' + idMedicao,
        subtitulo: 'Informe os dados para iniciar uma medição',
        info: this._cnpjPipe.transform(this.empresa.cnpj) + ' - ' + this.empresa.razaoSocial
      };
    } else if (this.isDetail()) {
      tituloTela = {
        titulo: 'Medição ' + idMedicao,
        subtitulo: 'Escolha uma submeta abaixo para detalhar a medição',
        info: this._cnpjPipe.transform(this.empresa.cnpj) + ' - ' + this.empresa.razaoSocial
      };
    } else if (this.canAccess('atestar_medicao')) {
      tituloTela = {
        titulo: 'Medição ' + idMedicao,
        subtitulo: 'Escolha uma submeta abaixo para fiscalizar uma medição',
        info: this._cnpjPipe.transform(this.empresa.cnpj) + ' - ' + this.empresa.razaoSocial
      };
    } else if (this.canAccess('aceitar_medicao')) {
      tituloTela = {
        titulo: 'Medição ' + idMedicao,
        subtitulo: 'Escolha uma submeta abaixo para analisar uma medição',
        info: this._cnpjPipe.transform(this.empresa.cnpj) + ' - ' + this.empresa.razaoSocial
      };
    }

    this._sharedService.emitChange(tituloTela);
  }

  validaDataInicioObra(): boolean {
    if (Object.prototype.toString.call(this.medicao.dataInicioObra) === '[object Date]') {
      if (!isNaN(this.medicao.dataInicioObra.getTime()) && this.medicao.dataInicioObra) {
        if (this.medicao.dataInicioObra.getTime() <= Date.now()) {
          return true;
        } else {
          this._messageService.error(' Campo não deve ser maior que a data atual (' +
            this._datepipe.transform(Date.now(), 'dd/MM/yyyy') + ').'
          );
          return false;
        }
      } else {
        this._messageService.error('Data de Início do Objeto inválida! Campo de preenchimento obrigatório.');
        return false;
      }
    } else {
      this._messageService.error('Data de Início do Objeto inválida! Campo de preenchimento obrigatório.');
      return false;
    }
  }

  validaDataFim(): boolean {
    if (Object.prototype.toString.call(this.medicao.dataFim) === '[object Date]') {
      if (!isNaN(this.medicao.dataFim.getTime()) && this.medicao.dataFim) {
        if (
          this.medicao.dataFim.getTime() >= this.medicao.dataInicio.getTime() &&
          this.medicao.dataFim.getTime() <= Date.now()
        ) {
          return true;
        } else {
          this._messageService.error('Campo Data Fim da Medição deve ser maior ou igual a Data de Início da Medição (' +
            this.medicao.dataInicio.toLocaleDateString() + ') e menor ou igual a Data do Envio da Medição (' +
            this._datepipe.transform(Date.now(), 'dd/MM/yyyy') + ').'
          );
          return false;
        }
      } else {
        this._messageService.error('Data Fim de Medição inválida ! Campo de preenchimento obrigatório.');

        return false;
      }
    } else {
      this._messageService.error('Data Fim de Medição inválida ! Campo de preenchimento obrigatório.');
      return false;
    }
  }

  get exibeSalvar(): boolean {
    return this.isInclude() || (this.isEdit()
                                && this.medicao.permiteComplementacaoValor != false
                                && (this.medicao.situacao.codigo === 'EM' || this.medicao.situacao.codigo === 'CE'));
  }

  get exibeEnviar(): boolean {
    return this.isEdit() && (this.medicao.situacao.codigo === 'EM')
      && this.existeSubmetaAssinadaEmpresa;
  }

  get exibeEnviarComplementacao(): boolean {
    return this.isEdit() && this.medicao.situacao.codigo === 'CE' && this.existeSubmetaAssinadaEmpresa;
  }

  get exibeAtestar(): boolean {
    return this.isEdit() && (this.medicao.situacao.codigo === 'AT' || this.medicao.situacao.codigo === 'CC') && this.existeSubmetaAssinadaConvenente;
  }


  get exibeSolicitarComplemento(): boolean {
    return this.isEdit() && (this.medicao.situacao.codigo === 'AT' || this.medicao.situacao.codigo === 'CC' || this.medicao.situacao.codigo === 'AC') && this.permiteSolicitarComplemetacao;
  }

  get exibeAceitar(): boolean {
    return this.isEdit() && this.medicao.situacao.codigo === 'AC' && this.existeSubmetaAssinadaConcedente;
  }

  get exibeSalvarDadosVistoria(): boolean {
    return this.isEdit() && this.medicao.situacao.codigo === 'AC';
  }
  get contrato(): Contrato {
    return this._contratoService.contratoAtual;
  }

  get empresa(): Empresa {
    return this._empresaService.empresaAtual;
  }

  isInclude(): boolean {
    return this.acao === 'incluir';
  }

  isDetail(): boolean {
    return this.acao === 'detalhar';
  }

  isEdit(): boolean {
    return this.acao === 'editar';
  }

  voltar() {
    this._router.navigate(['./listar'], { relativeTo: this._route.parent });
  }

  navegarAbaObservacoes() {
    this._router.navigate(['./observacao/listar'], { relativeTo: this._route });
  }

  onListagemSubmetaLoad(event: any) {
    this.existeSubmetaAssinadaEmpresa = event.existeSubmetaAssinadaEmpresa;
    this.existeSubmetaAssinadaConvenente = event.existeSubmetaAssinadaConvenente;
    this.existeSubmetaAssinadaConcedente = event.existeSubmetaAssinadaConcedente;
  }

  isConcedente(): boolean {
    return this.usuarioLogado.hasProfile([Profile.CONCEDENTE]);
  }

  isMandataria(): boolean {
    return this.usuarioLogado.hasProfile([Profile.MANDATARIA]);
  }

  salvarDadosVistoria() {
    this.carregarDadosVistoria();

    if (this.validarDadosVistoria(false)) {
      this._medicaoService.salvarMedicaoConcedenteMandataria(this.dadosVistoria, this.medicao.id)
        .subscribe(
          value => {
            this.medicao = value;
            // Redireciona para a rota de alteracao
            if (this.medicao.situacao.codigo === 'AC') {
              super.adicionarMensagem('Medição salva com sucesso.', true);
            } else {
              super.adicionarMensagem('Complementação da medição salva com sucesso.', true);
            }
          }
        );
    }
  }

  carregarDadosVistoria() {

    this.dadosVistoria = new Medicao();
    this.dadosVistoria.vistoriaExtra = this.formDadosMedicaoComponent.medicaoForm.value['inVistoria'];
    this.dadosVistoria.dataVistoriaExtra = this.formDadosMedicaoComponent.medicaoForm.value['dtVistoria'];
    if (this.dadosVistoria.vistoriaExtra) {
      this.dadosVistoria.solicitanteVistoriaExtra = this.formDadosMedicaoComponent.medicaoForm.value['solicitante'];
    }
    this.dadosVistoria.versao = this.medicao.versao;

  }

  validarDadosVistoria(isDataVistoriaObrigatoria: boolean) {
    const controlDtVistoria = this.formDadosMedicaoComponent.medicaoForm.get('dtVistoria');
    controlDtVistoria.updateValueAndValidity({ onlySelf: true });

    const controlSolicitante = this.formDadosMedicaoComponent.medicaoForm.get('solicitante');
    controlSolicitante.updateValueAndValidity({ onlySelf: true });

    if (!controlDtVistoria.valid) {
      controlDtVistoria.markAsTouched({ onlySelf: true });
    }
    if (this.dadosVistoria.vistoriaExtra) {
      if (!controlSolicitante.valid) {
        controlSolicitante.markAsTouched({ onlySelf: true });
      }
    }

    if (!isDataVistoriaObrigatoria) {
      if (this.dadosVistoria.dataVistoriaExtra && !controlDtVistoria.valid) {
        return false;
      }
    } else {
      if (!controlDtVistoria.valid) {
        return false;
      }
    }

    if (this.dadosVistoria.vistoriaExtra && !controlSolicitante.valid) {
      return false;
    }
    return true;
  }

  public isEdicaoComplementacaoValorAgrupadoraBM(): boolean {
      return this.isEdit()
          && !this.contrato.inAcompEvento
          && this.medicao
          && this.medicao.permiteComplementacaoValor != false
          && this.medicao.isMedicaoAgrupadora
          && this.medicao.situacao
          && ((this.medicao.situacao.codigo === SituacaoMedicaoEnum.EM_COMPLEMENTACAO_PELA_EMPRESA
              && this.usuarioLogado.profile === Profile.EMPRESA)
              ||
              (this.medicao.situacao.codigo === SituacaoMedicaoEnum.EM_COMPLEMENTACAO_CONVENENTE
              && this.usuarioLogado.profile === Profile.PROPONENTE));
  }
}
