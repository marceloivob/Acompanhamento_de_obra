import { Profile } from '../../../shared/model/security/profile.enum';
import { Permission } from '../../../shared/model/security/permission.enum';
import { RequiredAuthorizer } from '../../../shared/model/security/required-authorizer.model';
import { BaseComponent } from 'src/app/shared/util/base.component';
import { Evento } from '../../../shared/model/evento.model';
import { Component, TemplateRef, ViewChild, Injector } from '@angular/core';
import { Contrato } from '../../../shared/model/contrato.model';
import { ContratoService } from '../../../shared/services/contrato.service';
import { SharedService } from 'src/app/shared/services/shared-service.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Medicao } from 'src/app/shared/model/medicao.model';
import { Submeta } from 'src/app/shared/model/submeta.model';
import { SubmetaService } from 'src/app/shared/services/submeta.service';
import { BsModalRef, BsModalService } from 'ngx-bootstrap';
import { FormDadosSubmetaComponent } from './form-dados-submeta/form-dados-submeta.component';
import { FrenteObra } from 'src/app/shared/model/frente-obra.model';
import { EmpresaService } from 'src/app/shared/services/empresa.service';
import { CnpjPipe } from 'src/app/shared/pipes/cnpj.pipe';
import { Empresa } from 'src/app/shared/model/empresa.model';
import { Role } from 'src/app/shared/model/security/role.enum';
import { SituacaoMedicaoEnum } from 'src/app/shared/enum/situacao-medicao.enum';
import { SituacaoSubmetaEnum } from 'src/app/shared/enum/situacao-submeta.enum';
import { FormDadosSubmetaServicoComponent } from './form-dados-submeta-servico/form-dados-submeta-servico.component';
import { Servico } from 'src/app/shared/model/servico.model';

@Component({
  selector: 'app-cadastro-submeta',
  templateUrl: './cadastro-submeta.component.html'
})
export class CadastroSubmetaComponent extends BaseComponent {

  acao: string;
  medicao: Medicao;
  submeta: Submeta;
  assinavel = false;
  modalConfirmacaoVoltarRef: BsModalRef;
  modalConfirmacaoSalvarRef: BsModalRef;
  modalConfirmacaoExcluirRef: BsModalRef;
  modalConfirmacaoAssinarRef: BsModalRef;
  DELAY = 5000;
  perfilAcesso: Profile;
  msgConfirmacaoSalvar: string;
  msgConfirmacaoAssinar: string;

  @ViewChild(FormDadosSubmetaComponent)
  private medicaoSubmetaComponent: FormDadosSubmetaComponent;

  @ViewChild(FormDadosSubmetaServicoComponent)
  private medicaoSubmetaServicoComponent: FormDadosSubmetaServicoComponent;

  tituloTela = {
    titulo: 'Preencher Medi????o',
    subtitulo: '',
    info: this._cnpj.transform(this.empresa.cnpj) + ' - ' + this.empresa.razaoSocial
  };

  constructor(private _route: ActivatedRoute,
    private _contratoService: ContratoService,
    private _submetaService: SubmetaService,
    private _sharedService: SharedService,
    private _modalService: BsModalService,
    private _router: Router,
    private _cnpj: CnpjPipe,
    private _empresaService: EmpresaService,
    private injector: Injector) {
    super(injector);
  }

  loadPermissions(): Map<string, RequiredAuthorizer> {
    const profiles = [
      Profile.EMPRESA,
      Profile.PROPONENTE
    ];
    const profileConvenente = [Profile.PROPONENTE];

    const profileConcedenteMandataria = [Profile.CONCEDENTE, Profile.MANDATARIA];

    const rolesEdicaoConvenente = [
      Role.FISCAL_CONVENENTE,
      Role.GESTOR_CONVENIO_CONVENENTE,
      Role.GESTOR_FINANCEIRO_CONVENENTE,
      Role.OPERADOR_FINANCEIRO_CONVENENTE];

    return new Map([
      ['assinar', new RequiredAuthorizer(profiles, [], [Permission.ASSINAR_SUBMETA])],
      ['editar', new RequiredAuthorizer(profiles, [], [Permission.EDITAR_SUBMETA])],
      ['excluir', new RequiredAuthorizer(profiles, [], [Permission.EXCLUIR_SUBMETA])],
      ['editarSubmetaConvenente', new RequiredAuthorizer(profileConvenente, rolesEdicaoConvenente, [])],
      ['excluirSubmetaConvenente', new RequiredAuthorizer(profileConvenente, rolesEdicaoConvenente, [])],
      ['assinarSubmetaConvenente', new RequiredAuthorizer(profileConvenente, [Role.FISCAL_CONVENENTE], [])],
      [
        'salvarSubmetaConcedenteMandataria',
        new RequiredAuthorizer(profileConcedenteMandataria, [Role.FISCAL_CONCEDENTE,
        Role.GESTOR_CONVENIO_CONCEDENTE,
        Role.GESTOR_FINANCEIRO_CONCEDENTE,
        Role.OPERACIONAL_CONCEDENTE,
        Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA,
        Role.FISCAL_ACOMPANHAMENTO,
        Role.TECNICO_TERCEIRO], [])
      ],
      ['assinarSubmetaConcedenteMandataria', new RequiredAuthorizer(profileConcedenteMandataria, [Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA,
      Role.FISCAL_ACOMPANHAMENTO,
      Role.TECNICO_TERCEIRO], [])],
      ['excluirSubmetaConcedenteMandataria', new RequiredAuthorizer(profileConcedenteMandataria, [Role.FISCAL_CONCEDENTE,
      Role.GESTOR_CONVENIO_CONCEDENTE,
      Role.GESTOR_FINANCEIRO_CONCEDENTE,
      Role.OPERACIONAL_CONCEDENTE,
      Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA,
      Role.FISCAL_ACOMPANHAMENTO,
      Role.TECNICO_TERCEIRO], [])],


    ]);
  }

  initializeComponent() {
    const url = this._route.snapshot.url;
    this.acao = url[url.length - 1].path;

    this._sharedService.emitChange(this.tituloTela);

    if (this._route.snapshot.url[0].path === 'acumulada'){
      this.medicao = this._route.snapshot.data.medicaoAcumulada;
    } else {
      this.medicao = this._route.snapshot.data.medicao;
    }

    this.atualizarTituloTela();

    this.submeta = this._route.snapshot.data.submeta;

    this.isUsuarioPossuiPermissaoAssinar();
  }

  atualizarTituloTela() {
    if (this.isDetail()) {
      this.tituloTela.titulo = "Detalhar Medi????o";
    }
    this.tituloTela.titulo = this.tituloTela.titulo + ' ' + this.medicao.sequencial;

    if (this.isContratoAcompanhadoPorEvento()) {
      if (this.isDetail()) {
        this.tituloTela.subtitulo = "Eventos Conclu??dos at?? esta Medi????o";
      } else {
        this.tituloTela.subtitulo = 'Informe abaixo quais Eventos foram Conclu??dos na Medi????o';
      }
    } else {
      if (this.isDetail()) {
        this.tituloTela.subtitulo = "Servi??os Medidos at?? esta Medi????o";
      } else {
        this.tituloTela.subtitulo = 'Informe abaixo quais Servi??os foram medidos na Medi????o';
      }
    }

    this._sharedService.emitChange(this.tituloTela);
  }

  recuperarSubmetaAtualizada() {
    this._submetaService.consultarSubmeta(this.medicao.id, this.submeta.id).subscribe(value => {
      this.submeta = value;
    });
  }

  isUsuarioPossuiPermissaoAssinar() {
    if (!this.isDetail()) {
      this._submetaService.assinavel(this.contrato.id, this.medicao.id, this.submeta.id).subscribe(value => {
        this.assinavel = value;
      });
    }
  }

  voltarComValidacoes(template: TemplateRef<any>) {
    if ((this.isContratoAcompanhadoPorEvento() && this.medicaoSubmetaComponent.isFormDirty())
      || (!this.isContratoAcompanhadoPorEvento() && this.medicaoSubmetaServicoComponent.isFormDirty())) {
      this.modalConfirmacaoVoltarRef = this.abrirModalConfirmacao(template);
    } else {
      this.voltar();
    }
  }

  exibirBotaoSalvar(): boolean {
    return !this.isDetail() && this.permiteEditar();
  }

  exibirBotaoAssinar(): boolean {

    return !this.isDetail() && this.permiteAssinar() && this.assinavel;
  }

  exibirBotaoExcluir() {
    return !this.isDetail() && !this.isMedicaoEmComplementacao() && this.permiteExcluir() && this.isSituacaoSubmeta(this.getNomePerfilEdicao(), SituacaoSubmetaEnum.RASCUNHO);
  }

  salvarRascunhoComValidacoes(template: TemplateRef<any>): void {
    this.msgConfirmacaoSalvar = null;

    // Verifica se a situa????o da submeta do perfil que est?? editando est?? assinada
    const submetaAssinada = this.isSituacaoSubmeta(this.getNomePerfilEdicao(), SituacaoSubmetaEnum.ASSINADA);

    if (!this.isMedicaoEmComplementacao() && submetaAssinada) {

      this.msgConfirmacaoSalvar = 'Esta a????o cancelar?? a assinatura dessa Submeta. Deseja prosseguir?';

    } else if (this.isMedicaoEmComplementacao()) {

      if (this.isContratoAcompanhadoPorEvento()) { // PLE

        // Verifica se existe altera????o em evento marcado em medi????o acumuladora
        const existeAlteracao = this.medicaoSubmetaComponent.existeAlteracaoEventoMedicaoAcumulada();

        if (submetaAssinada && existeAlteracao) {
          this.msgConfirmacaoSalvar =
            'Esta a????o cancelar?? a assinatura dessa submeta e as complementa????es realizadas para esta submeta ser??o refletidas nesta medi????o e tamb??m nas medi????es acumuladas a partir desta medi????o. Essa altera????o n??o poder?? ser desfeita. Deseja prosseguir?';

        } else if (submetaAssinada && !existeAlteracao) {
          this.msgConfirmacaoSalvar =
            'Esta a????o cancelar?? a assinatura dessa submeta. Essa altera????o n??o poder?? ser desfeita. Deseja prosseguir?';

        } else if (!submetaAssinada && existeAlteracao) {
          this.msgConfirmacaoSalvar =
            'As complementa????es realizadas para esta submeta ser??o refletidas nesta medi????o e tamb??m nas medi????es acumuladas a partir desta medi????o. Deseja prosseguir?';
        }

      } else if (!this.isContratoAcompanhadoPorEvento() && this.medicaoSubmetaServicoComponent.isFormularioValido()) { // BM

        if (submetaAssinada && this.isEditEmpresa()) {
          this.msgConfirmacaoSalvar =
            'Esta a????o cancelar?? a assinatura dessa submeta e as complementa????es realizadas para esta submeta ser??o refletidas nesta medi????o e nas posteriores, incluindo medi????o "Em Elabora????o", se existir. Deseja prosseguir?';

        } else if (submetaAssinada && this.isEditConvenente()) {
          this.msgConfirmacaoSalvar =
            'Esta a????o cancelar?? a assinatura dessa submeta e as complementa????es realizadas para esta submeta ser??o refletidas nesta medi????o e nas posteriores, incluindo medi????o "Em Ateste", se existir. Deseja prosseguir?';

        } else if (!submetaAssinada && this.isEditEmpresa()) {
          this.msgConfirmacaoSalvar =
            'As complementa????es realizadas para esta submeta ser??o refletidas nesta medi????o e nas posteriores, incluindo medi????o "Em Elabora????o", se existir. Deseja prosseguir?';

        } else if (!submetaAssinada && this.isEditConvenente()) {
          this.msgConfirmacaoSalvar =
            'As complementa????es realizadas para esta submeta ser??o refletidas nesta medi????o e nas posteriores, incluindo medi????o "Em Ateste", se existir. Deseja prosseguir?';
        }
      }
    }

    if (this.msgConfirmacaoSalvar) {
      this.modalConfirmacaoSalvarRef = this.abrirModalConfirmacao(template);

    } else {
      this.salvarRascunho();
    }
  }

  salvarRascunho(): void {
    let frentesObraManutencao = new Array();

    if (this.isContratoAcompanhadoPorEvento()) {
      frentesObraManutencao = this.preencherObjetoManutencaoPorEvento(this.submeta.frentesObra);
    } else {
      if (this.medicaoSubmetaServicoComponent.isFormularioValido()) {
        frentesObraManutencao = this.preencherObjetoManutencaoPorServico(this.submeta.frentesObra);
      }
    }

    if (frentesObraManutencao.length > 0) {
      this._submetaService.salvar(this.medicao.id,
        this.submeta.id,
        this.submeta.versao,
        frentesObraManutencao).subscribe(value => {
          if (this.isContratoAcompanhadoPorEvento()) {
            this.medicaoSubmetaComponent.markFormAsPristine();
          } else {
            this.medicaoSubmetaServicoComponent.markFormAsPristine();
          }

          this.recuperarSubmetaAtualizada();

          this.adicionarMensagem('Submeta salva com sucesso.');
        });
    }
  }

  assinarComValidacoes(template: TemplateRef<any>): void {
    this.msgConfirmacaoAssinar = null;

    if (this.isMedicaoEmComplementacao()) {

      if (this.isContratoAcompanhadoPorEvento() && this.medicaoSubmetaComponent.existeAlteracaoEventoMedicaoAcumulada()) {
        this.msgConfirmacaoAssinar =
          'As complementa????es realizadas para esta submeta ser??o refletidas nesta medi????o e tamb??m nas medi????es acumuladas a partir desta medi????o. Deseja prosseguir?';

      } else if (!this.isContratoAcompanhadoPorEvento() && this.medicaoSubmetaServicoComponent.isFormularioValido()) {

        if (this.isEditEmpresa()) {
          this.msgConfirmacaoAssinar =
            'As complementa????es realizadas para esta submeta ser??o refletidas nesta medi????o e nas posteriores, incluindo medi????o "Em Elabora????o", se existir. Deseja prosseguir?';

        } else if (this.isEditConvenente()) {
          this.msgConfirmacaoAssinar =
            'As complementa????es realizadas para esta submeta ser??o refletidas nesta medi????o e nas posteriores, incluindo medi????o "Em Ateste", se existir. Deseja prosseguir?';
        }
      }
    }

    if (this.msgConfirmacaoAssinar) {
      this.modalConfirmacaoAssinarRef = this.abrirModalConfirmacao(template);

    } else {
      this.assinar();
    }
  }

  assinar(): void {
    let frentesObraManutencao = new Array();

    if (this.isContratoAcompanhadoPorEvento()) {
      frentesObraManutencao = this.preencherObjetoManutencaoPorEvento(this.submeta.frentesObra);
    } else {
      if (this.medicaoSubmetaServicoComponent.isFormularioValido()) {
        frentesObraManutencao = this.preencherObjetoManutencaoPorServico(this.submeta.frentesObra);
      }
    }

    if (frentesObraManutencao.length > 0) {
      this._submetaService.assinar(this.contrato.id,
        this.medicao.id,
        this.submeta.id,
        this.submeta.versao,
        frentesObraManutencao).subscribe(value => {
          if (this.isContratoAcompanhadoPorEvento()) {
            this.medicaoSubmetaComponent.markFormAsPristine();
          } else {
            this.medicaoSubmetaServicoComponent.markFormAsPristine();
          }
          this.recuperarSubmetaAtualizada();

          this.adicionarMensagem('Submeta assinada com sucesso.');
        });
    }
  }

  abrirModalConfirmacao(template: TemplateRef<any>): BsModalRef {
    return this._modalService.show(template, { class: 'modal-sm' });
  }

  excluirRascunhoComValidacoes(template: TemplateRef<any>): void {
    if ((this.canAccess('assinar') && this.isSituacaoSubmeta('Empresa', SituacaoSubmetaEnum.RASCUNHO)) ||
      (this.canAccess('assinarSubmetaConvenente') && this.isSituacaoSubmeta('Convenente', SituacaoSubmetaEnum.RASCUNHO)) ||
      ((this.canAccess('excluirSubmetaConcedenteMandataria')) && this.isSituacaoSubmeta('Concedente', SituacaoSubmetaEnum.RASCUNHO))) {

      this.modalConfirmacaoExcluirRef = this.abrirModalConfirmacao(template);
    }
  }

  excluirRascunho(): void {
    this._submetaService.excluir(this.medicao.id, this.submeta.id).subscribe(value => {
      super.adicionarMensagem('Rascunho exclu??do com sucesso.', true);
      this.voltar();
    });
  }

  preencherObjetoManutencaoPorEvento(frentesObra: FrenteObra[]): FrenteObra[] {
    const frentesObraManutencao = new Array();
    let frenteObraManutencao: Object;

    let eventoManutencao: Object;

    for (const frenteObra of this.submeta.frentesObra) {
      const eventosManutencao = new Array();

      for (const evento of frenteObra.eventos) {
        eventoManutencao = new Evento(evento.id, evento.indRealizado);
        eventosManutencao.push(eventoManutencao);
      }
      frenteObraManutencao = new FrenteObra(frenteObra.id, eventosManutencao);
      frentesObraManutencao.push(frenteObraManutencao);
    }

    return frentesObraManutencao;
  }

  preencherObjetoManutencaoPorServico(frentesObra: FrenteObra[]): FrenteObra[] {
    const frentesObraManutencao = new Array();
    let frenteObraManutencao: Object;
    let servicoManutencao: Object;

    for (const frenteObra of this.submeta.frentesObra) {
      for (const macrosservico of frenteObra.macroServicosView) {
        const servicosManutencao = new Array();

        for (const servico of macrosservico.servicos) {
          if (servico.permiteMedicao) {
              servicoManutencao = new Servico(servico.id, servico.qtdInformada);
              servicosManutencao.push(servicoManutencao);
          }
        }

        frenteObraManutencao = new FrenteObra(frenteObra.id, null, servicosManutencao);
        frentesObraManutencao.push(frenteObraManutencao);
      }
    }

    return frentesObraManutencao;
  }

  voltar() {

    if (this._route.snapshot.url[0].path === 'acumulada'){
      this._router.navigate(['../../../../../'], { relativeTo: this._route });
    } else {
      this._router.navigate(['../../../'], { relativeTo: this._route });
    }

  }

  get contrato(): Contrato {
    return this._contratoService.contratoAtual;
  }

  get empresa(): Empresa {
    return this._empresaService.empresaAtual;
  }

  permiteEditar(): boolean {
    return this.canAccess('editar') || this.canAccess('editarSubmetaConvenente') || this.canAccess('salvarSubmetaConcedenteMandataria');
  }

  permiteAssinar(): boolean {
    return this.canAccess('assinar') || this.canAccess('assinarSubmetaConvenente') || this.canAccess('assinarSubmetaConcedenteMandataria');
  }

  permiteExcluir(): boolean {
    return this.canAccess('excluir') || this.canAccess('excluirSubmetaConvenente') || this.canAccess('excluirSubmetaConcedenteMandataria');
  }

  isDetail(): boolean {
    return this.acao === 'detalhar';
  }

  getNomePerfilEdicao(): string {
    if (this.isEditEmpresa()) {
      return 'Empresa';
    } else if (this.isEditConvenente()) {
      return 'Convenente';
    } else if (this.isEditConcedente()) {
      return 'Concedente';
    }
  }

  isEditEmpresa(): boolean {
    return this.acao === 'editar' && this.canAccess('editar');
  }

  isEditConvenente(): boolean {
    return this.acao === 'editar' && this.canAccess('editarSubmetaConvenente');
  }

  isEditConcedente(): boolean {
    return this.acao === 'editar' && this.canAccess('salvarSubmetaConcedenteMandataria');
  }

  isMedicaoEmComplementacao(): boolean {
    return this.isSituacaoMedicao(SituacaoMedicaoEnum.EM_COMPLEMENTACAO_PELA_EMPRESA) ||
      this.isSituacaoMedicao(SituacaoMedicaoEnum.EM_COMPLEMENTACAO_CONVENENTE);
  }

  // Verifica se a situa????o da submeta no perfil informado ?? igual ?? situa????o passada como par??metro
  // Utiliza os atributos situacaoEmpresa, situacaoConvenente de submeta.model
  isSituacaoSubmeta(perfil: string, situacao: SituacaoSubmetaEnum): boolean {
    return this.submeta['situacao' + perfil] != null && this.submeta['situacao' + perfil].codigo === situacao;
  }

  // Verifica se a situa????o da medi????o ?? igual ?? situa????o passada como par??metro
  isSituacaoMedicao(situacao: SituacaoMedicaoEnum): boolean {
    return this.medicao.situacao != null && this.medicao.situacao.codigo === situacao;
  }

  isContratoAcompanhadoPorEvento(): boolean {
    return this.contrato.inAcompEvento;
  }
}
