import { RequiredAuthorizer } from '../../../shared/model/security/required-authorizer.model';
import { Profile } from '../../../shared/model/security/profile.enum';
import { Role } from '../../../shared/model/security/role.enum';
import { BaseComponent } from 'src/app/shared/util/base.component';
import { Component, Injector} from '@angular/core';
import { Contrato } from 'src/app/shared/model/contrato.model';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, FormControl, Validators } from '@angular/forms';
import { ContratoService } from 'src/app/shared/services/contrato.service';
import { TipoDocumento, TipoManifestoAmbiental, DocumentosComplementares } from '../../../shared/model/documentos-complementares.model';
import { TipoDocumentoEnum } from '../../../shared/model/tipo-documento.model';
import { TipoManifestoAmbientalEnum } from '../../../shared/model/tipo-manifesto-ambiental.model';
import { AnexoDocCompl } from '../../../shared/model/anexo-doc-complementar.model';
import { Submeta } from 'src/app/shared/model/submeta.model';
import { DocumentosComplementaresService } from 'src/app/shared/services/documentos-complementares.service';
import { dataEmissaoDocComplValidator, verificaValorCampoValidator, dataValidadeDocComplValidator,
         validaCampoDataDocComplValidator } from 'src/app/shared/validators/configuracao-validator';
import { tamanhoArquivoValidator, tipoArquivoValidator, tamanhoNomeArquivoValidator} from '../../../shared/validators/util-validator';
import { FileUtil } from 'src/app/shared/util/file-util';


/**
 * ------------------------------------- IMPORTANTE --------------------------------------------------
 *
 * Após a criação dos Componentes Siconv-DatePicker pelo FormBuilder, não alterar
 * validadores destes Componentes, pois isso causa inconsistências nas validações.
 *
 * ------------------------------------- FIM IMPORTANTE ----------------------------------------------
 *
 * ------------------------------------- PROBLEMA ----------------------------------------------------
 *
 * 1. Quando marcamos(markAsTouched, markAsPristine) em um componente siconv-input, os mesmos não
 *  refletem na tela o comportamento. Ex.: Marcar um componente inválido com markAsTouched para
 *  o mesmo exibir a borda vermelha na tela. O componente não exibe a borda vermelha pois existe
 *  uma limitação no Angular que impede que o FormControl alterado no Componente(.ts) notifique o
 *  Componente de Tela(.html). Segue abaixo alguns links que documentam isto, porém existem workarounds
 *  que podem ser feitos no nosso componente Siconv que podem contornar o problema. Para confirmar
 *  isto basta utilizar um input comum type="text" na tela e ligá-lo ao FormControl através do
 *  FormControlName que o mesmo será facilmente pintado de vermelho quando o FormControl for
 *  marcado com markAsTouched no componente(.ts).
 *
 *  https://github.com/vmware/clarity/issues/3191
 *  https://github.com/angular/angular/issues/10887
 *  https://stackoverflow.com/questions/44730711/how-do-i-know-when-custom-form-control-is-marked-as-pristine-in-angular
 *
 *  Este último link pode ser aplicado no componente siconv-input para contornar o problema.
 *
 * ------------------------------------- FIM PROBLEMA -------------------------------------------------
 *
 */
@Component({
  selector: 'app-cadastro-doc-complementar',
  templateUrl: './cadastro-doc-complementar.component.html'
})
export class CadastroDocComplementarComponent extends BaseComponent {

    idOperacao: string;
    emEdicaoDocCompl: boolean;
    emInclusaoDocCompl: boolean;

    isDetails = false;
    isDtEmissaoObrigatoria = true;
    isNrDocumentoObrigatorio  = true;
    isOrgaoEmissorObrigatorio = true;
    isValidadeObrigatoria = true;

    docCompl: DocumentosComplementares;

    // Variáveis para o Enum de Tipo de Documento e TipoManifestoAmbiental
    enumTipoDocumento = TipoDocumentoEnum;
    enumTipoManifestoAmbiental = TipoManifestoAmbientalEnum;
    nmArquivoAnexo = '';

    // Variáveis do Select de Tipo de Documentos
    listaTipoDocumento = [];
    // Variáveis do Select de Tipo de Documentos
    listaTipoManifestoAmbiental = [];

    // Variáveis do Pick-List de Submetas
    listaSubmetas: Submeta[];
    dadosSelecionados = [];
    dadosSource = [];
    dadosTarget = [];

    // Variáveis do file
    nomeArquivo = '';
    anexoAtual: AnexoDocCompl;

    todos = ['AUT', 'DEC', 'MAM', 'OSE', 'OTG', 'OUT'];
    excetoOutros = ['AUT', 'DEC', 'MAM', 'OSE', 'OTG'];
    outros = ['OUT'];
    outrosManifesto = ['OUT'];
    manifesto = ['MAM'];
    excetoOutrosOrdem = ['MAM', 'OTG', 'AUT', 'DEC'];
    tipoSelecionado: string;
    tipoManifestoSelecionado: string;


    // Variaveis do Forms e Controles do Forms
    manterFormGroup =  new FormGroup({});
    tipoDocumentoCtrl: FormControl;
    descricaoDocComplCtrl: FormControl;
    tipoManifestoAmbientalCtrl: FormControl;
    numeroDocComplCtrl: FormControl;
    orgaoEmissorDocComplCtrl: FormControl;
    dtEmissaoDocComplCtrl: FormControl;
    dtValidadeDocComplCtrl: FormControl;
    fileCtrl: FormControl;
    submetasCtrl: FormControl;
    nmArquivoCtrl: FormControl;
    txDescricaoOutrosCtrl: FormControl;
    eqLicencaInstalacaoCtrl: FormControl;

    tituloFieldset = 'Documentação Complementar';

  constructor(private route: ActivatedRoute,
              private _router: Router,
              private fb: FormBuilder,
              private _contratoService: ContratoService,
              private _servDocsCompl: DocumentosComplementaresService,
              private injector: Injector
              ) {
                super (injector);
              }

  initializeComponent() {

      const url = this.route.snapshot.url;
      this.idOperacao = url[url.length - 1].path;

      if (this.idOperacao === 'editar') {
        this.emEdicaoDocCompl = true;
      }

      if (this.idOperacao === 'incluir') {
        this.emInclusaoDocCompl = true;
      }

      if (this.idOperacao === 'detalhar') {
        this.isDetails = true;
      }

      this.carregarForm();
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
          ['editar', new RequiredAuthorizer (profiles, roles, [])],
        ]);

  }
  carregarForm() {

    const dtAtual = new Date();

    this.tipoDocumentoCtrl = new FormControl('', [Validators.required]);
    this.descricaoDocComplCtrl = new FormControl('', [Validators.required]);
    this.tipoManifestoAmbientalCtrl = new FormControl('', [Validators.required]);
    this.numeroDocComplCtrl = new FormControl('', [Validators.required, verificaValorCampoValidator()]);
    this.orgaoEmissorDocComplCtrl = new FormControl('', [Validators.required, verificaValorCampoValidator()]);
    this.dtEmissaoDocComplCtrl = new FormControl(null, [dataEmissaoDocComplValidator(),
                                                        validaCampoDataDocComplValidator(this.isDtEmissaoObrigatoria)]);
    this.dtValidadeDocComplCtrl = new FormControl(null, [dataValidadeDocComplValidator(this.dtEmissaoDocComplCtrl),
                                                        validaCampoDataDocComplValidator(this.isValidadeObrigatoria)]);
    this.nmArquivoCtrl = new FormControl(null);

    if (this.emEdicaoDocCompl) {
      this.fileCtrl = new FormControl(null, [tipoArquivoValidator,
        tamanhoArquivoValidator, tamanhoNomeArquivoValidator]);

    } else {
      this.fileCtrl = new FormControl(null, [Validators.required, tipoArquivoValidator,
        tamanhoArquivoValidator, tamanhoNomeArquivoValidator]);
    }

    this.submetasCtrl = new FormControl(this.submetasCtrl, [Validators.required]);
    this.txDescricaoOutrosCtrl = new FormControl('');
    this.eqLicencaInstalacaoCtrl = new FormControl (null);

    this.manterFormGroup = this.fb.group({
                                          tipoDocumento: this.tipoDocumentoCtrl,
                                          descricaoDocCompl: this.descricaoDocComplCtrl,
                                          tipoManifestoAmbiental: this.tipoManifestoAmbientalCtrl,
                                          numeroDocCompl: this.numeroDocComplCtrl,
                                          orgaoEmissorDocCompl: this.orgaoEmissorDocComplCtrl,
                                          dtEmissaoDocCompl: this.dtEmissaoDocComplCtrl,
                                          dtValidadeDocCompl: this.dtValidadeDocComplCtrl,
                                          submetas: this.submetasCtrl,
                                          nmArquivo: this.nmArquivoCtrl,
                                          file: this.fileCtrl,
                                          dtAtual: dtAtual,
                                          txDescricaoOutros: this.txDescricaoOutrosCtrl,
                                          eqLicencaInstalacao: this.eqLicencaInstalacaoCtrl
                                        });

    this.initForm();
  }


  initForm() {

    this.docCompl = new DocumentosComplementares();
    this.docCompl.tipoDocumento = new TipoDocumento();
    this.docCompl.tipoManifestoAmbiental = new TipoManifestoAmbiental();
    this.docCompl.submetas = [];

    if (this.emEdicaoDocCompl) {

      this.docCompl = this.route.snapshot.data.docComplementar;

      this.tipoSelecionado = this.docCompl.tipoDocumento.codigo;

      if (this.tipoSelecionado === 'OUT') {
          this.descricaoDocComplCtrl.setValidators(verificaValorCampoValidator());
      }

      this.tipoDocumentoCtrl.setValue(this.docCompl.tipoDocumento);
      this.tipoManifestoAmbientalCtrl.setValue(this.docCompl.tipoManifestoAmbiental);

      if (this.docCompl.txDescricao) {
          this.descricaoDocComplCtrl.setValue(this.docCompl.txDescricao);
      } else {
        this.descricaoDocComplCtrl.setValue('');
      }

      this.dtEmissaoDocComplCtrl.setValue(this.docCompl.dtEmissao);
      this.dtValidadeDocComplCtrl.setValue(this.docCompl.dtValidade);
      this.numeroDocComplCtrl.setValue(this.docCompl.nrDocumento);
      this.orgaoEmissorDocComplCtrl.setValue(this.docCompl.nmOrgaoEmissor);
      this.nmArquivoCtrl.setValue(this.docCompl.nmArquivo);
      this.fileCtrl.setValue(this.docCompl.arquivo);
      this.nomeArquivo = this.docCompl.nmArquivo;

      const anexo = new AnexoDocCompl();
      anexo.coCeph = this.docCompl.coCeph;
      anexo.nmArquivo = this.docCompl.nmArquivo;
      anexo.url = this.docCompl.url;
      this.anexoAtual = anexo;

      this.dadosSelecionados = [];
      this.docCompl.submetas.forEach(submeta => {
        this.dadosSelecionados.push({ name: submeta.nrSubmetaAnalise + ' - ' + submeta.descricao, data: submeta });
      });

      this.carregarSubmetas(this.contrato.id);
      this.carregarTipoDocumento();

      if (this.tipoSelecionado === 'MAM') {
          this.carregarTipoManifestoAmbiental();
      }

    } else {

            if (this.isDetails) {

              this.docCompl = this.route.snapshot.data.docComplementar;

              this.tipoSelecionado = this.docCompl.tipoDocumento.codigo;
              this.tipoDocumentoCtrl.setValue(this.docCompl.tipoDocumento);
              this.tipoManifestoAmbientalCtrl.setValue(this.docCompl.tipoManifestoAmbiental);
              this.descricaoDocComplCtrl.setValue(this.docCompl.txDescricao);
              this.dtEmissaoDocComplCtrl.setValue(this.docCompl.dtEmissao);
              this.dtValidadeDocComplCtrl.setValue(this.docCompl.dtValidade);
              this.numeroDocComplCtrl.setValue(this.docCompl.nrDocumento);
              this.orgaoEmissorDocComplCtrl.setValue(this.docCompl.nmOrgaoEmissor);
              this.nmArquivoCtrl.setValue(this.docCompl.nmArquivo);
              this.fileCtrl.setValue(this.docCompl.arquivo);
              this.nomeArquivo = this.docCompl.nmArquivo;

              this.dadosSelecionados = [];
              this.docCompl.submetas.forEach(submeta => {
                this.dadosSelecionados.push({ name: submeta.nrSubmetaAnalise + ' - ' + submeta.descricao, data: submeta });
              });

              this.carregarSubmetas(this.contrato.id);
              this.carregarTipoDocumento();
              if (this.tipoSelecionado === 'MAM') {
                  this.carregarTipoManifestoAmbiental();
              }

              this.desabilitaCampos();
            } else {
                this.carregarSubmetas(this.contrato.id);
                this.carregarTipoDocumento();
            }

    }
  }

  carregarTipoDocumento() {

    this.listaTipoDocumento = this.converteEnum(this.enumTipoDocumento);

    if (this.emEdicaoDocCompl || this.isDetails) {
      this.manterFormGroup.controls['tipoDocumento']
      .setValue(this.recuperarTipoDocumentoSelecionado(this.docCompl.tipoDocumento.codigo));
    }
    this.atualizarLabelOpcional(this.docCompl.tipoDocumento.codigo);
  }

  atualizarLabelOpcional(tipoDoc: string) {

    if (tipoDoc === 'OUT') {
          this.isDtEmissaoObrigatoria = false;
          this.isNrDocumentoObrigatorio = false;
          this.isOrgaoEmissorObrigatorio = false;
          this.numeroDocComplCtrl.setValidators(null);
          this.orgaoEmissorDocComplCtrl.setValidators(null);
    } else {
          this.isDtEmissaoObrigatoria = true;
          this.isNrDocumentoObrigatorio = true;
          this.isOrgaoEmissorObrigatorio = true;
          this.numeroDocComplCtrl.setValidators(verificaValorCampoValidator());
          this.orgaoEmissorDocComplCtrl.setValidators(verificaValorCampoValidator());
    }

    if (tipoDoc === 'OTG' || tipoDoc === 'AUT' || tipoDoc === 'DEC') {
          this.isValidadeObrigatoria = false;
    } else {
          this.isValidadeObrigatoria = true;
    }

  }

  carregarTipoManifestoAmbiental() {

    this.listaTipoManifestoAmbiental = this.converteEnum(this.enumTipoManifestoAmbiental);

    if ((this.emEdicaoDocCompl || this.isDetails) && this.docCompl.tipoManifestoAmbiental != null) {
      this.manterFormGroup.controls['tipoManifestoAmbiental']
      .setValue(this.recuperarTipoManifestoSelecionado(this.docCompl.tipoManifestoAmbiental.codigo));

      if (this.docCompl.tipoManifestoAmbiental.codigo === 'OUT' ) {
        this.txDescricaoOutrosCtrl.setValue (this.docCompl.txDescricaoOutros);

        this.eqLicencaInstalacaoCtrl.setValue (this.docCompl.eqLicencaInstalacao);
        this.preencherDadosTipoManifestoOutros (this.docCompl.tipoManifestoAmbiental.codigo);
      }

      this.atualizarObrigatoriedadeValidade(this.docCompl.tipoManifestoAmbiental.codigo);
    }

  }

  recuperarTipoDocumentoSelecionado(idTipoDoc: string) {
    const tipoDocEncontrado = this.listaTipoDocumento.filter ( doc => {
                                                                      return doc.codigo === idTipoDoc;
                                                              });
    return tipoDocEncontrado[0];
  }

  recuperarTipoManifestoSelecionado(idTipomanifesto: string) {
    const tipoManifestoEncontrado = this.listaTipoManifestoAmbiental.filter ( doc => {
                                                                      return doc.codigo === idTipomanifesto;
                                                              });
    return tipoManifestoEncontrado[0];
  }

  converteEnum(tipoEnum: any) {
    return Object.keys(tipoEnum).map(o => {
                                                    return {codigo: o, descricao: tipoEnum[o]};
                                              });
  }

  /**
   *  MÉTODOS DO CAMPO DE ANEXO
   **/
  fileUpload(event: any): void {
    if (event.target.files.length > 0) {
      if (FileUtil.validarArquivo(event.target.files[0], 10, this.fileCtrl)) {
        const anexo = new AnexoDocCompl();
        anexo.arquivo = event.target.files[0];
        anexo.nmArquivo = anexo.arquivo['name'];
        this.anexoAtual = anexo;
      } else {
        this.anexoAtual = null;
      }
    } else {
      this.anexoAtual = null;
    }

    if ((this.emEdicaoDocCompl) && (event.target.files.length === 0)) {
            this.fileCtrl.setErrors(null);
    }
  }

  /**
   * Métodos e variáveis do picklist de Submetas AssociadasdescricaoDocCompl
  **/
  carregarSubmetas(idContrato: number) {
    this._servDocsCompl.listarSubmetasContrato(idContrato).subscribe(value => {
      this.listaSubmetas = value;

      this.carregarPickList();
    });
  }

  onDadosChanged(event) {
    this.dadosSelecionados = event;

    if (this.dadosSelecionados.length === 0) {
      this.submetasCtrl.setErrors ({ 'required': true });
      this.submetasCtrl.markAsTouched({ onlySelf: true });
    } else {
      this.submetasCtrl.reset();
    }
  }

  carregarPickList() {

    this.listaSubmetas.forEach(submeta => {

          if (this.listaSubmetas.length > 0) {

              let docComplTemSubmeta = false;

              if (this.docCompl.submetas.length !== 0) {
                this.docCompl.submetas.forEach(sub => {
                  if (sub.id === submeta.id) {
                    docComplTemSubmeta = true;
                  }
                });
              }

              if (docComplTemSubmeta) {
                this.dadosTarget.push({ name: submeta.nrSubmetaAnalise + ' - ' + submeta.descricao, data: submeta });
                this.dadosSource.splice(submeta.id, 1);
              } else {
                this.dadosSource.push({ name: submeta.nrSubmetaAnalise + ' - ' + submeta.descricao, data: submeta });
              }

          } else {

              this.dadosSource.push({ name: submeta.nrSubmetaAnalise + ' - ' + submeta.descricao, data: submeta });

          }

    });

  }

  verificaSelecao(event) {

    if (event) {

      const id = event.codigo;
      this.tipoSelecionado = id;

      this.atualizarLabelOpcional(id);

      this.atualizaSelecaoDeSubmetas();

      if (id === 'DEC' ||
      id === 'AUT' ||
      id === 'OTG') {
        this.isValidadeObrigatoria = false;
      }

      if (id === 'MAM') {
        this.carregarTipoManifestoAmbiental();
      } else {
        this.listaTipoManifestoAmbiental = [];
      }

      if (id === 'OUT') {
        this.descricaoDocComplCtrl.setValidators(verificaValorCampoValidator());
      } else {
        this.descricaoDocComplCtrl.clearValidators();
      }
    } else {
      this.limparCampos();

      this.tipoSelecionado = '';
      this.emEdicaoDocCompl = false;
    }

    this.preencherDadosTipoManifestoOutros (null);

    this.manterFormGroup.markAsPristine();
    this.manterFormGroup.markAsUntouched();
    this.manterFormGroup.updateValueAndValidity();
  }

  private atualizaSelecaoDeSubmetas() {
    if (this.excetoOutrosOrdem.includes(this.tipoSelecionado)) {
      this.dadosTarget = [];
      this.dadosSource = [];

      this.listaSubmetas.forEach(submeta => {

        if (this.listaSubmetas.length > 0) {

          let isSelecionados = false;

          isSelecionados = this.isSubmetasSelecionadas(submeta);

          if (isSelecionados) {
            this.dadosTarget.push({ name: submeta.nrSubmetaAnalise + ' - ' + submeta.descricao, data: submeta });
            this.dadosSource.splice(submeta.id, 1);
          } else {
            this.dadosSource.push({ name: submeta.nrSubmetaAnalise + ' - ' + submeta.descricao, data: submeta });
          }
        } else {
          this.dadosSource.push({ name: submeta.nrSubmetaAnalise + ' - ' + submeta.descricao, data: submeta });
        }

      });
    }
  }

  private isSubmetasSelecionadas(submeta: Submeta) {
    let selecionadas = false;
    if (this.dadosSelecionados.length !== 0) {
      this.dadosSelecionados.forEach(sub => {
        if (sub.data.id === submeta.id) {
          selecionadas = true;
        }
      });
    }
    return selecionadas;
  }

  private limparCampos() {
    this.manterFormGroup.controls['tipoDocumento'].setValue(this.tipoDocumentoCtrl);
    this.manterFormGroup.controls['descricaoDocCompl'].setValue('');
    this.manterFormGroup.controls['tipoManifestoAmbiental'].setValue(this.tipoManifestoAmbientalCtrl);
    this.manterFormGroup.controls['numeroDocCompl'].setValue('');
    this.manterFormGroup.controls['orgaoEmissorDocCompl'].setValue('');
    this.manterFormGroup.controls['dtEmissaoDocCompl'].setValue(null);
    this.manterFormGroup.controls['dtValidadeDocCompl'].setValue(null);
    this.manterFormGroup.controls['nmArquivo'].setValue(null);
    this.manterFormGroup.controls['file'].setValue(null);
    this.manterFormGroup.controls['submetas'].setValue(null);
  }

  validaManifesto(event: any) {

    let codigoTipoManifesto = null;
    if (event && event.codigo) {
      codigoTipoManifesto = event.codigo;
    }

    this.atualizarObrigatoriedadeValidade(codigoTipoManifesto);
    this.preencherDadosTipoManifestoOutros (codigoTipoManifesto);
  }

  validaEquivLicInst() {

    this.atualizarObrigatoriedadeValidade (this.tipoManifestoSelecionado);
  }

  preencherDadosTipoManifestoOutros(codigoTipoManifesto: string) {

    if (codigoTipoManifesto && codigoTipoManifesto === 'OUT') {
      this.tipoManifestoSelecionado = codigoTipoManifesto;
      this.txDescricaoOutrosCtrl.setValidators (verificaValorCampoValidator());
    } else {
      if (!codigoTipoManifesto) {
        this.tipoManifestoAmbientalCtrl.reset();
      }
      this.tipoManifestoSelecionado = '';
      this.txDescricaoOutrosCtrl.clearValidators();
      this.txDescricaoOutrosCtrl.reset();
      this.eqLicencaInstalacaoCtrl.reset();
    }
  }

  atualizarObrigatoriedadeValidade(codigoTipoManifesto: string) {

    if (codigoTipoManifesto &&
        (codigoTipoManifesto === 'DIS' || codigoTipoManifesto === 'PRO' ||
        (codigoTipoManifesto === 'OUT' &&
          (this.eqLicencaInstalacaoCtrl.value == null ||
            this.eqLicencaInstalacaoCtrl.value === false ) ))) {
      this.isValidadeObrigatoria = false;
    } else {
      this.isValidadeObrigatoria = true;
    }
  }

  desabilitaCampos() {
    Object.keys(this.manterFormGroup.controls).forEach(key => {
          this.manterFormGroup.get(key).disable();
    });
  }

  verificaExibicao(telaExibicao: Array<any>, tipoexibicao: string): boolean {
    return telaExibicao.includes(tipoexibicao);
  }

  verificaDataValidade() {
    if (this.dtValidadeDocComplCtrl.value) {
      this.dtValidadeDocComplCtrl.updateValueAndValidity();
    }
  }

  verificaDataEmissao(arg: string) {
    if (this.dtEmissaoDocComplCtrl.value) {
      this.dtEmissaoDocComplCtrl.updateValueAndValidity();
    }
  }

  cancelar() {
    this._router.navigate(['./listar'], { relativeTo: this.route.parent });
  }

  salvar() {

    if (this.validaCamposObrigatorios()) {

      this.preparaDocComplementarParaInclusaoAlteracao();

      if (this.manterFormGroup.valid) {

          if (!this.docCompl.id) {
            this.incluirDocComplementar();
          } else {
            this.alterarDocComplementar();
          }
        }
      }
  }

  private preparaDocComplementarParaInclusaoAlteracao() {
    if (this.anexoAtual) {
      this.docCompl.arquivo = this.anexoAtual.arquivo;
      this.docCompl.nmArquivo = this.anexoAtual.nmArquivo;
    }

    this.docCompl.tipoDocumento = this.tipoDocumentoCtrl.value;

    if (this.tipoManifestoAmbientalCtrl.value) {
      this.docCompl.tipoManifestoAmbiental = this.tipoManifestoAmbientalCtrl.value;
    } else {
      this.docCompl.tipoManifestoAmbiental = null;
    }

    this.docCompl.txDescricao = this.descricaoDocComplCtrl.value;
    this.docCompl.nrDocumento = this.numeroDocComplCtrl.value;
    this.docCompl.dtEmissao = this.dtEmissaoDocComplCtrl.value;
    this.docCompl.dtValidade = this.dtValidadeDocComplCtrl.value;
    this.docCompl.nmOrgaoEmissor = this.orgaoEmissorDocComplCtrl.value;
    this.docCompl.txDescricaoOutros = this.txDescricaoOutrosCtrl.value;
    this.docCompl.eqLicencaInstalacao = this.eqLicencaInstalacaoCtrl.value;

    if (this.dadosSelecionados.length !== 0) {
      this.docCompl.submetas = [];
      this.dadosSelecionados.forEach(element => {
        this.docCompl.submetas.push(element['data']);
      });
    }

    if (this.anexoAtual !== undefined) {
      this.docCompl.nmArquivo = '';
      this.docCompl.coCeph = '';
      this.docCompl.url = '';
    }
  }

  private alterarDocComplementar() {
    this._servDocsCompl.alterarDocCompl(this.docCompl, this.docCompl.id)
      .subscribe(value => {
        this.docCompl = value.data;
        super.adicionarMensagem('Documento Complementar Salvo com sucesso.', true);
        this._router.navigate(['./listar'], { relativeTo: this.route.parent });
      });
  }

  private incluirDocComplementar() {
    this._servDocsCompl.incluirDocCompl(this.docCompl, this.contrato.id)
      .subscribe(value => {
        this.docCompl = value.data;
        super.adicionarMensagem('Documento Complementar salvo com sucesso.', true);
        this._router.navigate(['./listar'], { relativeTo: this.route.parent });
      });
  }

  validaCamposObrigatorios() {

    let validado = true;

    if (this.tipoDocumentoCtrl.value) {
      switch (this.tipoDocumentoCtrl.value.codigo) {
        case 'MAM': {
              validado = this.validaTipoDocumentoMAM(validado);
              break;
        }
        case 'AUT': case 'DEC': case 'OTG': {
              validado = this.validaTipoDocumentoAUT_DEC_OTG(validado);
              break;
        }
        case 'OUT': {
              validado = this.validaTipoDocumentoOUT(validado);
              break;
        }
        case 'OSE': {
              this.validaTipoDocumentoOSE();
              break;
        }
      }
    } else {
        this.tipoDocumentoCtrl.setErrors({ 'required': true });
        this.tipoDocumentoCtrl.markAsTouched({onlySelf: true});
        validado = false;
    }

    if (!this.dtEmissaoDocComplCtrl.valid) {
        this.dtEmissaoDocComplCtrl.markAsTouched({ onlySelf: true });
        validado = false;
    }

    if (!this.dtValidadeDocComplCtrl.valid) {
      this.dtValidadeDocComplCtrl.markAsTouched({ onlySelf: true });
      validado = false;
    }

    if (!this.numeroDocComplCtrl.valid) {
        this.numeroDocComplCtrl.markAsTouched({ onlySelf: true });
        validado = false;
    }

    if (!this.orgaoEmissorDocComplCtrl.valid) {
      this.orgaoEmissorDocComplCtrl.markAsTouched({ onlySelf: true });
      validado = false;
    }

    if (!this.fileCtrl.valid) {
      this.fileCtrl.markAsTouched({ onlySelf: true });
      validado = false;
    }

    return validado;
  }

  private validaTipoDocumentoOSE() {
    this.descricaoDocComplCtrl.setValue('');
    this.descricaoDocComplCtrl.setErrors(null);
    this.tipoManifestoAmbientalCtrl.setValue('');
    this.tipoManifestoAmbientalCtrl.setErrors(null);
    this.dtValidadeDocComplCtrl.setValue('');
    this.dtValidadeDocComplCtrl.setErrors(null);
    this.submetasCtrl.setValue('');
    this.submetasCtrl.setErrors(null);
  }

  private validaTipoDocumentoOUT(validado: boolean) {
    if (!this.descricaoDocComplCtrl.valid) {
      this.descricaoDocComplCtrl.markAsTouched({ onlySelf: true });
      validado = false;
    }

    if (!this.numeroDocComplCtrl.valid) {
      this.numeroDocComplCtrl.setErrors(null);
      validado = false;
    }

    if (!this.orgaoEmissorDocComplCtrl.valid) {
      this.orgaoEmissorDocComplCtrl.setErrors(null);
      validado = false;
    }

    this.tipoManifestoAmbientalCtrl.setValue('');
    this.tipoManifestoAmbientalCtrl.setErrors(null);
    this.dtValidadeDocComplCtrl.setErrors(null);
    this.submetasCtrl.setValue('');
    this.submetasCtrl.setErrors(null);
    return validado;
  }

  private validaTipoDocumentoAUT_DEC_OTG(validado: boolean) {
    if (this.dadosSelecionados.length === 0) {
      this.submetasCtrl.setErrors({ 'required': true });
      this.submetasCtrl.markAsTouched({ onlySelf: true });
      validado = false;
    } else {
      this.submetasCtrl.setErrors(null);
    }

    this.descricaoDocComplCtrl.setValue('');
    this.descricaoDocComplCtrl.setErrors(null);
    this.tipoManifestoAmbientalCtrl.setValue('');
    this.tipoManifestoAmbientalCtrl.setErrors(null);
    return validado;
  }

  private validaTipoDocumentoMAM(validado: boolean) {
    if (this.tipoManifestoAmbientalCtrl.valid) {

      if (this.tipoManifestoAmbientalCtrl.value.codigo === 'LPR' ||
        this.tipoManifestoAmbientalCtrl.value.codigo === 'LIN' ||
        this.tipoManifestoAmbientalCtrl.value.codigo === 'LOP' ||
        (this.tipoManifestoAmbientalCtrl.value.codigo === 'OUT' && this.eqLicencaInstalacaoCtrl.value === true)) {
        if (!this.dtValidadeDocComplCtrl.valid) {
          this.dtValidadeDocComplCtrl.markAsTouched({ onlySelf: true });
          validado = false;
        }
      } else {
        this.verificaDataValidade();
      }
    } else {
      this.tipoManifestoAmbientalCtrl.setErrors({ 'required': true });
      this.tipoManifestoAmbientalCtrl.markAsTouched({ onlySelf: true });
      validado = false;
    }

    if (this.dadosSelecionados.length === 0) {
      this.submetasCtrl.setErrors({ 'required': true });
      this.submetasCtrl.markAsTouched({ onlySelf: true });
      validado = false;
    } else {
      this.submetasCtrl.setErrors(null);
    }

    if (!this.txDescricaoOutrosCtrl.valid) {
      this.txDescricaoOutrosCtrl.markAsTouched({ onlySelf: true });
      validado = false;
    }

    this.descricaoDocComplCtrl.setValue('');
    this.descricaoDocComplCtrl.setErrors(null);
    return validado;
  }

  get contrato(): Contrato {
    return this._contratoService.contratoAtual;
  }
}
