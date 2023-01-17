import { Profile } from '../../../shared/model/security/profile.enum';
import { Role } from '../../../shared/model/security/role.enum';
import { RequiredAuthorizer } from '../../../shared/model/security/required-authorizer.model';
import { RegistroProfissional } from '../../../shared/model/registro-profissional.model';
import { Component, Inject, LOCALE_ID, Injector } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Contrato } from 'src/app/shared/model/contrato.model';
import { ResponsavelTecnico } from 'src/app/shared/model/responsavel-tecnico.model';
import { Submeta } from 'src/app/shared/model/submeta.model';
import { ContratoService } from 'src/app/shared/services/contrato.service';
import { ResponsavelTecnicoService } from 'src/app/shared/services/responsavel-tecnico.service';
import { AnexoArt } from '../../../shared/model/anexo-art';
import { ArtRrt } from '../../../shared/model/art-rrt.model';
import { ArtRrtService } from '../../../shared/services/art-rrt.service';
import { dataEmissaoArtRrtValidator } from '../../../shared/validators/configuracao-validator';
import { DatePipe } from '@angular/common';
import { BaseComponent } from '../../../shared/util/base.component';
import { tipoArquivoValidator, tamanhoArquivoValidator, tamanhoNomeArquivoValidator } from 'src/app/shared/validators/util-validator';
import { FileUtil } from 'src/app/shared/util/file-util';

@Component({
  selector: 'app-manter-art-rrt',
  templateUrl: './cadastro-art-rrt.component.html'
})
export class CadastroArtRrtComponent extends BaseComponent {

  manterFormGroup =  new FormGroup({});
  dtEmissaoCtrl: FormControl;
  dtInativacaoCtrl: FormControl;
  numeroArtRrtCtrl: FormControl;
  fileCtrl: FormControl;
  responsavelCtrl: FormControl;
  submetasCtrl: FormControl;
  nmArquivoAnexo: FormControl;
  nmArquivoAtual: string;
  tipoArtCtrl: FormControl;

  tituloFieldset = 'Anotação de Responsabilidade Técnica / Registro de Responsabilidade Técnica';

  contrato: Contrato;

  operacao: string;
  isDetail = false;
  idMedContratoRespTec: number;
  emEdicaoArt: boolean;
  isInativado: boolean = false;
  art: ArtRrt;

  desabilitarBtnSalvar = false;

  // Variáveis do Select de Responsável Técnico
  listaResponsavel: ResponsavelTecnico[] = new Array();

  // Variáveis do Pick-List de Submetas
  listaSubmetas: Submeta[];

  dadosSelecionados = [];
  dadosSource = [];
  dadosTarget = [];

 // Variáveis do file
 nomeArquivo = '';
 anexoAtual: AnexoArt;


  constructor(private route: ActivatedRoute,
              private _router: Router,
              private fb: FormBuilder,
              private _contratoService: ContratoService,
              private _artRrtService: ArtRrtService,
              private _responsavelTecnicoService: ResponsavelTecnicoService,
              @Inject(LOCALE_ID) private locale: string,
              public datepipe: DatePipe,
              private injector: Injector) {
                super(injector);
              }

  initializeComponent() {
    this.contrato = this._contratoService.contratoAtual;

    const url = this.route.snapshot.url;
    this.operacao = url[url.length - 1].path;

    if (this.operacao === 'incluir') {
      this.carregarSubmetas(this.contrato.id);
    }

    this.initForm();

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
      ['salvar', new RequiredAuthorizer (profiles, roles, [])]
    ]);
  }

  async initForm() {

    const dtAtual = new Date();

    if (this.operacao === 'incluir') {

      this.dtEmissaoCtrl = new FormControl(this.dtEmissaoCtrl, [Validators.required]);
      this.numeroArtRrtCtrl = new FormControl(this.numeroArtRrtCtrl, [Validators.required]);
      this.responsavelCtrl = new FormControl(this.responsavelCtrl, [Validators.required]);
      this.fileCtrl = new FormControl(null, [Validators.required, tipoArquivoValidator, tamanhoArquivoValidator, tamanhoNomeArquivoValidator]);
      this.tipoArtCtrl = new FormControl (null);

      this.submetasCtrl = new FormControl(null, [Validators.required]);

      this.manterFormGroup = this.fb.group({
        dtEmissao: this.dtEmissaoCtrl,
        numeroArtRrtCtrl: this.numeroArtRrtCtrl,
        file: this.fileCtrl,
        responsavelCtrl: this.responsavelCtrl,
        dtAtual: dtAtual,
        tipoArt: this.tipoArtCtrl
      });

      this.dtEmissaoCtrl.setValidators(dataEmissaoArtRrtValidator(this.manterFormGroup.value['dtAtual']));
    } else {
      this.carregarForm();
      this.art = this.route.snapshot.data.artRrt;

      if (this.operacao === 'editar') {
        this.emEdicaoArt = true;
      }

      if (this.operacao === 'detalhar') {
        this.isDetail = true;
      }

      this.carregarFormManutencao();
    }
  }

  carregarForm() {

    const dtAtual = new Date();

    this.dtEmissaoCtrl = new FormControl(this.dtEmissaoCtrl, [Validators.required]);
    this.dtInativacaoCtrl = new FormControl(this.dtInativacaoCtrl, null);
    this.numeroArtRrtCtrl = new FormControl(this.numeroArtRrtCtrl, [Validators.required]);
    this.responsavelCtrl = new FormControl(this.responsavelCtrl, [Validators.required]);
    this.submetasCtrl = new FormControl(null, [Validators.required]);
    this.nmArquivoAnexo = new FormControl(null, [Validators.required]);
    this.fileCtrl = new FormControl(null, [tipoArquivoValidator, tamanhoArquivoValidator, tamanhoNomeArquivoValidator]);
    this.tipoArtCtrl = new FormControl (null);


    this.manterFormGroup = this.fb.group({
                                          dtEmissao       : this.dtEmissaoCtrl,
                                          dtInativacao    : this.dtInativacaoCtrl,
                                          numeroArtRrtCtrl: this.numeroArtRrtCtrl,
                                          responsavelCtrl : this.responsavelCtrl,
                                          file: this.fileCtrl,
                                          nmArquivoAnexo: this.nmArquivoAnexo,
                                          dtAtual: dtAtual,
                                          tipoArt: this.tipoArtCtrl
                                        });

    this.dtEmissaoCtrl.setValidators(dataEmissaoArtRrtValidator(this.manterFormGroup.value['dtAtual']));

  }

  carregarFormManutencao() {

    this.manterFormGroup.controls['dtEmissao'].setValue(this.art.dataEmissao);

    if (this.art.dataInativacao){
      this.manterFormGroup.controls['dtInativacao'].setValue(this.art.dataInativacao);
      this.isInativado = true;
    }
    else {
      this.isInativado = false;
    }
    this.manterFormGroup.controls['numeroArtRrtCtrl'].setValue(this.art.numero);
    const numeroArtRrtCtrl = this.manterFormGroup.get('numeroArtRrtCtrl');
    const responsavelCtrl = this.manterFormGroup.get('responsavelCtrl');
    this.manterFormGroup.controls['nmArquivoAnexo'].setValue(this.art.nmArquivo);
    this.nmArquivoAtual = this.art.nmArquivo;
    this.carregarSubmetas(this.contrato.id);

    this.manterFormGroup.controls['tipoArt'].setValue(this.art.tipo['codigo']);

    const anexo = new AnexoArt();
    anexo.coCeph = this.art.coCeph;
    anexo.nmArquivo = this.art.nmArquivo;
    anexo.url = this.art.url;
    this.anexoAtual = anexo;

    this.dadosSelecionados = [];
    this.art.submetas.forEach(submeta => {
      this.dadosSelecionados.push({ name: submeta.nrSubmetaAnalise + ' - ' + submeta.descricao, data: submeta });
    });

    this.carregarRT(this.contrato.id, this.manterFormGroup.value.tipoArt);

    if (this.isDetail) {
      numeroArtRrtCtrl.disable({ onlySelf: true });
      responsavelCtrl.disable({ onlySelf: true });
      this.tipoArtCtrl.disable ({onlySelf: true});
    }
  }

  /**
   * MÉTODOS E VARIÁVEIS DO SELECT DE RESPONSÁVEL TÉCNICO
  **/
  carregarRT(idContrato: number, tipo: string) {
    this._responsavelTecnicoService.listarResponsavelTecnicoPorTipo(idContrato, tipo).subscribe(value => {
      this.listaResponsavel = value;
      if (this.art && this.art.responsavelTecnico && this.art.responsavelTecnico.id) {
        this.manterFormGroup.controls['responsavelCtrl'].setValue(this.obterResponsavelTecnicoSelecionado(this.art.responsavelTecnico.id));
      } else {
        this.manterFormGroup.controls['responsavelCtrl'].setValue([]);
        this.manterFormGroup.controls['responsavelCtrl'].reset();
      }
    });
  }

  public obterResponsavelTecnicoSelecionado (idRtSelecionado: number): ResponsavelTecnico {

    const rtEncontrado = this.listaResponsavel.filter ( rt => {
      return rt.id === idRtSelecionado;
    });

    return rtEncontrado[0];
  }


  onCheckTipo(event: any): void {
    this.carregarRT(this.contrato.id, this.manterFormGroup.value.tipoArt);
  }

  /**
   * Métodos e variáveis do picklist de Submetas Associadas
  **/
  carregarSubmetas(idContrato: number) {

    this._artRrtService.listarSubmetasContrato(idContrato).subscribe(value => {
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

    if (!this.art) {
      this.art = new ArtRrt();
      this.art.submetas = new Array();
    }

    this.listaSubmetas.forEach(submeta => {

      if (this.art && this.listaSubmetas.length > 0) {
        let artTemSubmeta = false;
        if (this.art.submetas) {
          this.art.submetas.forEach(sub => {
            if (sub.id === submeta.id) {
              artTemSubmeta = true;
            }
          });
        }

        if (artTemSubmeta) {
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

  /**
   *  MÉTODOS DO CAMPO DE ANEXO
   **/
  fileUpload(event) {
    if (event.target.files.length > 0 &&
        FileUtil.validarArquivo(event.target.files[0], 10, this.manterFormGroup.get('file'))) {
      const anexo = new AnexoArt();
      anexo.arquivo = event.target.files[0];
      anexo.nmArquivo = anexo.arquivo['name'];
      this.anexoAtual = anexo;
    } else {
      this.anexoAtual = null;
    }
  }

  validaCamposObrigatorios() {
    if (!this.numeroArtRrtCtrl.valid || this.numeroArtRrtCtrl.value.trim() === '') {
      this.numeroArtRrtCtrl.setErrors ({ 'required': true });
      this.numeroArtRrtCtrl.markAsTouched({ onlySelf: true });
    }

    if (!this.dtEmissaoCtrl.valid) {
      this.dtEmissaoCtrl.markAsTouched({ onlySelf: true });
    }

    if (this.dadosSelecionados.length === 0) {
      this.submetasCtrl.setErrors ({ 'required': true });
      this.submetasCtrl.markAsTouched({ onlySelf: true });
    }

    if ((!this.responsavelCtrl.valid) ||
        (this.manterFormGroup.value['responsavelCtrl'] === 'selecione')) {
       this.responsavelCtrl.markAsTouched({ onlySelf: true });
    }

    if (!this.fileCtrl.valid && (this.fileCtrl.value === null || this.fileCtrl.value === '')) {
      this.fileCtrl.setErrors ({ 'required': true });
      this.fileCtrl.markAsTouched({ onlySelf: true });
    }
  }


  isCampoSubmetasValido(): boolean {
    return this.dadosSelecionados.length === 0 ? false : true;
  }

  /**
   *  CHAMA SERVIÇO PARA SALVAR ART / RRT *
   *
  **/
  salvar() {

    if (this.anexoAtual) {
      this.art.arquivo = this.anexoAtual.arquivo;
      this.art.nmArquivo = this.anexoAtual.nmArquivo;
    }

    this.validaCamposObrigatorios();
    this.art.numero = this.manterFormGroup.value['numeroArtRrtCtrl'];

    this.art.dataEmissao = this.manterFormGroup.value['dtEmissao'];

    if (this.dadosSelecionados.length !== 0) {
      this.art.submetas = [];
      this.dadosSelecionados.forEach(element => {
        this.art.submetas.push (element['data']);
      });
    }

    this.art.tipo = this.manterFormGroup.value.tipoArt;

    this.obterResponsavelSelecionado();

     if (this.manterFormGroup.valid && this.isCampoSubmetasValido()) {

      if (!this.art.id) {
            this._artRrtService.incluirArt(this.art, this.contrato.id).subscribe(value => {
              this.art = value.data;
              super.adicionarMensagem ('ART / RRT salva com sucesso.', true);
              this._router.navigate(['./listar'], {relativeTo : this.route.parent});
            });
      } else {
            this._artRrtService.alterarArt(this.art, this.art.id).subscribe(value => {
                this.art = value.data;
                super.adicionarMensagem ('ART / RRT salva com sucesso.', true);
                this._router.navigate(['./listar'], {relativeTo : this.route.parent});
            });
      }
    }
  }

  obterResponsavelSelecionado() {
        const responsavelTecnico = Object.assign(new ResponsavelTecnico(), this.manterFormGroup.value['responsavelCtrl']);
        const registro = Object.assign(new RegistroProfissional(), responsavelTecnico.obterRegistroVinculado(this.contrato.id));
        this.art.idMedContratoRespTec = registro.obterContratoVinculado(this.contrato.id).id;
  }

  cancelar() {
    this._router.navigate(['./listar'], {relativeTo : this.route.parent});
  }

  verificaListaVazia(): boolean {
       return this.listaResponsavel.length === 0 ? true : false;
  }
}
