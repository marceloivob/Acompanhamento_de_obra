import { AnexoArt } from '../../../shared/model/anexo-art';
import { Component, Inject, Injector, LOCALE_ID, TemplateRef } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PickListModel } from '@serpro/ngx-siconv/lib/components/picklist/model/picklist.model';
import { BsModalRef, BsModalService } from 'ngx-bootstrap';
import { AtividadeEnum } from 'src/app/shared/enum/atividade.enum';
import { TipoResponsavelTecnicoEnum } from 'src/app/shared/enum/tipo-responsavel-tecnico.enum';
import { Contrato } from 'src/app/shared/model/contrato.model';
import { ResponsavelTecnicoSocial } from 'src/app/shared/model/responsavel-tecnico-social.model';
import { Submeta } from 'src/app/shared/model/submeta.model';
import { TipoResponsavelTecnico } from 'src/app/shared/model/tipo-responsavel-tecnico.model';
import { CpfPipe } from 'src/app/shared/pipes/cpf.pipe';
import { ContratoService } from 'src/app/shared/services/contrato.service';
import { ResponsavelTecnicoSocialService } from 'src/app/shared/services/responsavel-tecnico-social.service';
import { SubmetaService } from 'src/app/shared/services/submeta.service';
import { BaseComponent } from 'src/app/shared/util/base.component';
import { FileUtil } from 'src/app/shared/util/file-util';
import { StringUtil } from 'src/app/shared/util/string-util';
import { tamanhoArquivoValidator, tipoArquivoValidator, tamanhoNomeArquivoValidator } from '../../../shared/validators/util-validator';
import { Profile } from '../../../shared/model/security/profile.enum';
import { RequiredAuthorizer } from '../../../shared/model/security/required-authorizer.model';
import { Role } from '../../../shared/model/security/role.enum';

@Component({
  selector: 'app-cadastro-rt-social',
  templateUrl: './cadastro-rt-social.component.html',
  providers: [ CpfPipe ]
})
export class CadastroRtSocialComponent extends BaseComponent {

  // Dados de entrada do componente por rota
  acao: string;

  // Dados do picklist de submetas
  submetasSource: PickListModel[];
  submetasTarget: PickListModel[];
  // No componente siconv-picklist não existe a property formControlName para utilizar o Reactive Form
  submetasFormCtrl: FormControl;

  nomeArquivoAtual: string;
  anexoAtual: AnexoArt;
  rts: ResponsavelTecnicoSocial;
  formulario: FormGroup;
  modalConfirmacaoEdicaoRTS: BsModalRef;

  exibirBtnSalvar = true;

  constructor(private _route: ActivatedRoute,
    private _router: Router,
    private _contratoService: ContratoService,
    private _responsavelTecnicoSocialService: ResponsavelTecnicoSocialService,
    private _submetaService: SubmetaService,
    private _modalService: BsModalService,
    private _cpfPipe: CpfPipe,
    @Inject(LOCALE_ID) private locale: string,
    private injector: Injector) {
      super (injector);
    }

  initializeComponent() {

    const url = this._route.snapshot.url;
    this.acao = url[url.length - 1].path;

    this.initForm();

    if (this.isInclude()) {
      this.rts = new ResponsavelTecnicoSocial();
    } else {
      this.rts = this._route.snapshot.data.rtSocial;

      this.prepararManutencao();
    }
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
      ['editar', new RequiredAuthorizer (profiles, roles, [])],
    ]);
  }

  pesquisar(template: TemplateRef<any>) {
    const cpf = this.formulario.get('cpf').value.replace(/\D/g, '');
    const tipoRT = this.formulario.get('tipoRT').value;

    let isValid = this.validarCampo('cpf');
    isValid = this.validarCampo('tipoRT') && isValid;

    if (isValid) {
      // Recupera o RTS caso ele esteja vinculado ao CTEF em questão
      this._responsavelTecnicoSocialService.buscarRTScomCPFRegistradoCTEF(this.contrato.id, cpf, tipoRT)
        .then((rtsExistente: ResponsavelTecnicoSocial) => {
          if (rtsExistente) {
            this.rts.id = rtsExistente.id;
            // Permite a edição do registro caso não esteja inativo e o RT não possua submeta assinada
            if (rtsExistente.possuiSubmetaAssinada) {
              this._messageService.error('O Responsável Técnico não pode ser editado, pois existe alguma submeta assinada.');
            } else if (rtsExistente.dtInativacao) {
              this._messageService.error('O Responsável Técnico não pode ser editado, pois está inativo.');
            } else {
              this.modalConfirmacaoEdicaoRTS = this._modalService.show(template, { class: 'modal-sm' });
            }
          } else {
            this._responsavelTecnicoSocialService.consultarResponsavelTecnicoElegivel(this.contrato.id, cpf, tipoRT)
              .subscribe(
                (respTecSocial: ResponsavelTecnicoSocial) => {
                  this.rts = respTecSocial;
                  this.loadFormFromModel();
                  this.exibirBtnSalvar = false;
                },
                (error: any) => {
                  this.rts.createResponsavelTecnico();
                }
              );
          }
        });
    }
  }

  initForm(alteracaoTipoRT?: boolean): void {
    // Campos de pesquisa de RT
    let cpf = '';
    let tipoRT = '';

    this.exibirBtnSalvar = true;

    // Na alteração de tipo de RT não limpa os campos CPF/Tipo RT,
    // seta obrigatoriedade dos campos órgão e não faz o loadSubmetas
    if (alteracaoTipoRT) {
      cpf = this.formulario.get('cpf').value;
      tipoRT = this.formulario.get('tipoRT').value;
      this.setObrigatoriedadeCamposOrgao();
    }

    this.formulario =  new FormGroup({
      cpf: new FormControl(cpf, Validators.required),
      tipoRT: new FormControl(tipoRT, Validators.required),
      identificacao: new FormControl({value: '', disabled: true}, Validators.required),
      dtInclusao: new FormControl({value: '', disabled: true}),
      dtInativacao: new FormControl({value: '', disabled: true}),
      email: new FormControl({value: '', disabled: true}, Validators.required),
      telefone: new FormControl('', [Validators.required, Validators.minLength(14), Validators.maxLength(15)]),
      atividade: new FormControl({value: AtividadeEnum.SOC.valueOf(), disabled: true}, Validators.required),
      curriculo: new FormControl('', [Validators.required, tipoArquivoValidator, tamanhoArquivoValidator, tamanhoNomeArquivoValidator]),
      formacao: new FormControl('', [Validators.required, Validators.maxLength(100)]),
      registroProfissional: new FormControl('', Validators.maxLength(100)),
      nomeOrgao: new FormControl(''),
      telefoneOrgao: new FormControl(''),
      emailOrgao: new FormControl(''),
      submetasFormCtrl: new FormControl(null)
    });
  }

  // Configura algumas variáveis dependendo da operação: Edição / Detalhamento
  configurarManutencaoPorTipoOperacao(): void {
    if (this.isDetail()) {
      this.desabilitarCampos(this.camposFormulario);
    } else if (this.isEdit()) {
      this.desabilitarCampos(['cpf', 'tipoRT']);
      this.formulario.get('curriculo').setValidators([]);
      this.nomeArquivoAtual = this.rts.nomeArquivo;
      this.exibirBtnSalvar = false;
    }
  }

  loadFormFromModel(): void {
    this.formulario.get('cpf').setValue(this._cpfPipe.transform(this.rts.responsavelTecnico.cpf));
    if (this.rts.tipo) {
      this.formulario.get('tipoRT').setValue(this.rts.tipo.codigo);
    }
    this.formulario.get('identificacao').setValue(
      this._cpfPipe.transform(this.rts.responsavelTecnico.cpf) + ' - ' + this.rts.responsavelTecnico.nome);
    this.formulario.get('dtInclusao').setValue(this.rts.dtInclusao);
    this.formulario.get('dtInativacao').setValue(this.rts.dtInativacao);
    this.formulario.get('email').setValue(this.rts.responsavelTecnico.email);
    this.formulario.get('telefone').setValue(this.rts.responsavelTecnico.telefone);
    this.formulario.get('formacao').setValue(this.rts.formacao);
    this.formulario.get('registroProfissional').setValue(this.rts.registroProfissional);
    if (this.rts.orgao) {
      this.formulario.get('nomeOrgao').setValue(this.rts.orgao.nome);
      this.formulario.get('telefoneOrgao').setValue(this.rts.orgao.telefone);
      this.formulario.get('emailOrgao').setValue(this.rts.orgao.email);
    }

    this.setObrigatoriedadeCamposOrgao();
    this.loadSubmetas(this.rts.submetas);
  }

  loadModelFromForm(): void {
    this.rts.tipo = new TipoResponsavelTecnico(this.formulario.get('tipoRT').value);
    this.rts.createResponsavelTecnico(
      this.rts.responsavelTecnico.id,
      this.formulario.get('cpf').value.replace(/\D/g, ''),
      this.formulario.get('telefone').value,
      this.rts.responsavelTecnico.versao
    );
    this.rts.formacao = this.formulario.get('formacao').value;
    this.rts.registroProfissional = this.formulario.get('registroProfissional').value;
    if (TipoResponsavelTecnicoEnum[this.rts.tipo.codigo] === TipoResponsavelTecnicoEnum.FIS) {
      this.rts.createOrgao(
        this.formulario.get('nomeOrgao').value,
        this.formulario.get('telefoneOrgao').value,
        this.formulario.get('emailOrgao').value
      );
    }
    this.rts.submetas = this.submetasTarget
      .map((submetaPL: PickListModel) => Object({id: submetaPL.data['id']}));
  }

  loadSubmetas(submetasAssociadas?: Submeta[]): void {
    this.submetasSource = [];
    this.submetasTarget = [];
    this._submetaService.listarSubmetasPorContrato(this.contrato.id)
      .subscribe((submetas: Submeta[]) => {
        const subSource = submetasAssociadas
          ? submetas.filter((sub: Submeta) =>
            submetasAssociadas.map((subAssociadas: Submeta) => subAssociadas.id !== sub.id)
              .reduce((curr, prev) => curr && prev))
          : submetas;

        subSource.forEach((sub: Submeta) => {
          this.submetasSource.push({name: sub.nrSubmetaAnalise + ' - ' + sub.descricao, data: sub});
        });

        const subTarget = submetasAssociadas ? submetasAssociadas : [];
        subTarget.forEach((sub: Submeta) => {
          this.submetasTarget.push({name: sub.nrSubmetaAnalise + ' - ' + sub.descricao, data: sub});
        });

      });
  }

  prepararManutencao(): void {
    this.loadFormFromModel();
    this.configurarManutencaoPorTipoOperacao();
  }

  validarFormulario(): boolean {
    this.camposFormulario.forEach((campo: string) => this.validarCampo(campo));
    // Desabilita o controle do campo de Submetas para validar o formulário como um todo,
    // o campo submetas é validado separadamente
    this.formulario.get('submetasFormCtrl').reset();
    const isValid = this.formulario.valid;
    return this.validarCampoSubmetas() && isValid;
  }

  validarCampo(nomeCampo: string): boolean {
    let isValid = true;

    // Valida o campo caso ele esteja habilitado
    if (this.formulario.get(nomeCampo).status !== 'DISABLED' && !this.formulario.get(nomeCampo).valid) {
      isValid = false;
      this.formulario.get(nomeCampo).markAsTouched({ onlySelf: true });
    }
    return isValid;
  }

  desabilitarCampos(listaNomeCampo: string[]): void {
    listaNomeCampo.forEach((nomeCampo: string) => this.formulario.get(nomeCampo).disable({ onlySelf: true }));
  }

  validarCampoSubmetas(): boolean {
    let isValid = true;
    if (this.submetasTarget.length === 0) {
      isValid = false;
      this.formulario.get('submetasFormCtrl').setErrors ({ 'required': true });
      this.formulario.get('submetasFormCtrl').markAsTouched({ onlySelf: true });
    } else {
      this.formulario.get('submetasFormCtrl').reset();
    }
    return isValid;
  }

  salvar(): void {

    if (this.anexoAtual) {
      this.rts.arquivo = this.anexoAtual.arquivo;
      this.rts.nomeArquivo = this.anexoAtual.nmArquivo;
    }

    if (this.validarFormulario()) {
      this.loadModelFromForm();

      this._responsavelTecnicoSocialService.salvar(this.rts, this.contrato.id)
        .subscribe(() => {
          super.adicionarMensagem ('Responsável Técnico salvo com sucesso.', true);
          this._router.navigate(['./listar'], { relativeTo: this._route.parent });
        });
    }
  }

  voltar() {
    this._router.navigate(['./listar'], { relativeTo: this._route.parent });
  }

  fileUpload(event: any): void {
    const files = event.target.files;
    if (files.length > 0 && FileUtil.validarArquivo(files[0], 10, this.formulario.get('curriculo'))) {
      const anexo = new AnexoArt();
      anexo.arquivo = event.target.files[0];
      anexo.nmArquivo = anexo.arquivo['name'];
      this.anexoAtual = anexo;
     } else {
       this.anexoAtual = null;
     }
  }

  cancelarEdicao(): void {
    this.rts.createResponsavelTecnico();
    this.hideModalConfirmarEdicao();
  }

  confirmarEdicao(): void {
    this.hideModalConfirmarEdicao();
    this._router.navigate(['./', this.rts.id, 'editar'], { relativeTo: this._route.parent });
  }

  hideModalConfirmarEdicao(): void {
    if (this.modalConfirmacaoEdicaoRTS) {
      this.modalConfirmacaoEdicaoRTS.hide();
    }
  }

  onChangedTipoRT(): void {
    this.initForm(true);
    this.rts.createResponsavelTecnico();
  }

  setObrigatoriedadeCamposOrgao(): void {
    if (TipoResponsavelTecnicoEnum[this.formulario.get('tipoRT').value] === TipoResponsavelTecnicoEnum.FIS) {
      this.formulario.get('nomeOrgao').setValidators([Validators.required, Validators.maxLength(100)]);
      this.formulario.get('telefoneOrgao').setValidators([Validators.required, Validators.minLength(14), Validators.maxLength(15)]);
      this.formulario.get('emailOrgao').setValidators([Validators.required, Validators.maxLength(100), Validators.email]);
    } else {
      ['nomeOrgao', 'telefoneOrgao', 'emailOrgao'].forEach((campo: string) => {
        this.formulario.get(campo).setValidators([]);
        this.formulario.get(campo).setErrors(null);
      });
    }
  }

  onChangedSubmetas(event: any) {
    this.submetasTarget = event;
    this.validarCampoSubmetas();
  }

  formatarTelefone(campoTelefone: string) {
    const tel = this.formulario.get(campoTelefone).value;
    this.formulario.get(campoTelefone).setValue(StringUtil.formatarTelefone(tel));
  }

  get requiredMessage(): string {
    return 'Campo de preenchimento obrigatório.';
  }

  get camposFormulario(): string[] {
    return [
      'cpf',
      'tipoRT',
      'telefone',
      'curriculo',
      'formacao',
      'registroProfissional',
      'submetasFormCtrl',
      'nomeOrgao',
      'telefoneOrgao',
      'emailOrgao'];
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

  get contrato(): Contrato {
    return this._contratoService.contratoAtual;
  }
}
