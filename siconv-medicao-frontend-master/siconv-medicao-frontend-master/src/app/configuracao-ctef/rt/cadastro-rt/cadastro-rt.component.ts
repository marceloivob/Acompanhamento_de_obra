import { Profile } from '../../../shared/model/security/profile.enum';
import { Role } from '../../../shared/model/security/role.enum';
import { RequiredAuthorizer } from '../../../shared/model/security/required-authorizer.model';
import { BaseComponent } from 'src/app/shared/util/base.component';
import { CpfPipe } from '../../../shared/pipes/cpf.pipe';
import { Component, TemplateRef, Injector } from '@angular/core';
import { ContratoService } from '../../../shared/services/contrato.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Contrato } from 'src/app/shared/model/contrato.model';
import { FormGroup, FormBuilder, FormControl, Validators } from '@angular/forms';
import { Atividade } from 'src/app/shared/model/atividade.model';
import { UF } from 'src/app/shared/model/uf.model';
import { ResponsavelTecnicoService } from '../../../shared/services/responsavel-tecnico.service';
import { EmpresaService } from 'src/app/shared/services/empresa.service';
import { Empresa } from 'src/app/shared/model/empresa.model';
import { RegistroProfissional } from 'src/app/shared/model/registro-profissional.model';
import { ContratoResponsavelTecnico } from '../../../shared/model/contrato-responsavel-tecnico.model';
import { BsModalRef, BsModalService } from 'ngx-bootstrap';
import { ResponsavelTecnico } from 'src/app/shared/model/responsavel-tecnico.model';
import { validateTelefone } from 'src/app/shared/validators/util-validator';
import { StringUtil } from 'src/app/shared/util/string-util';
import { DataExport } from 'src/app/shared/model/data-export';

@Component({
  selector: 'app-manter-resp-tecnico',
  templateUrl: './cadastro-rt.component.html',
  providers: [CpfPipe]
})
export class CadastroRtComponent extends BaseComponent {

  contrato: Contrato;
  empresa: Empresa;
  idProposta: number;
  acao: string;

  responsavelTecnicoPesquisado: ResponsavelTecnico;

  cadastrarRespTecnicoForm = new FormGroup({});
  telefoneCtrl: FormControl;
  cpfCtrl: FormControl;
  identificacaoCtrl: FormControl;
  emailCtrl: FormControl;
  atividadeCtrl: FormControl;
  ufCtrl: FormControl;
  creacauCtrl: FormControl;

  emEdicaoResp = false;
  emEdicaoRespPorBuscaCPF = false;
  emEdicaoReg = false;
  emDetalhamento = false;
  atividadeSelecionada = '';

  tipoConsulta = 'EXE';

  modalConfirmacaoEdicaoRT: BsModalRef;
  contratoVinculado: ContratoResponsavelTecnico;
  registroProfissional: RegistroProfissional;
  rpAnterior: RegistroProfissional;

  ///// Variaveis do objeto Siconv-Table
  fileExportName = 'listaRegistroProfissional';
  data: RegistroProfissional[];
  dataExport: any[] = [];
  export: DataExport;
  lista: any[];
  //////////////////////////////////////

  contratoRespTecnico: ContratoResponsavelTecnico;

  atividades: Atividade[];
  ufs: UF[];


  constructor(private _contratoService: ContratoService,
    private _empresaService: EmpresaService,
    private _resptecnicoservice: ResponsavelTecnicoService,
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    public _router: Router,
    private _modalService: BsModalService,
    private cpfPipe: CpfPipe,
    private injector: Injector
  ) {
    super(injector);
  }


  initializeComponent() {
    this.contrato = this._contratoService.contratoAtual;
    this.empresa = this._empresaService.empresaAtual;
    this.idProposta = this.contrato.propostaFk;

    const url = this.route.snapshot.url;
    this.acao = url[url.length - 1].path;

    this.emEdicaoReg = false;
    this.carregaForm();

    if (this.acao !== 'incluir') {

      this.responsavelTecnicoPesquisado = this.route.snapshot.data.rt;

      this.responsavelTecnicoPesquisado.contratoFk = this.contrato.id;
      if (this.responsavelTecnicoPesquisado.registrosProfissional) {
        this.obterContrato();
        this.tipoConsulta = this.contratoVinculado.tipo.codigo;
      }
      if (this.acao === 'detalhar') {
        this.emDetalhamento = true;
      } else if (this.acao === 'editar') {
        this.emEdicaoResp = true;
      }
      this.carregaFormManutencao();

    }
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
      ['salvar', new RequiredAuthorizer(profiles, roles, [])],
      ['pesquisar', new RequiredAuthorizer(profiles, roles, [])],
      ['incluirRP', new RequiredAuthorizer(profiles, roles, [])],
      ['excluirRP', new RequiredAuthorizer(profiles, roles, [])],
      ['editarRP', new RequiredAuthorizer(profiles, roles, [])],
      ['adicionarRP', new RequiredAuthorizer(profiles, roles, [])],
      ['alterarRP', new RequiredAuthorizer(profiles, roles, [])]
    ]);
  }

  formatarTelefone() {
    const tel = this.cadastrarRespTecnicoForm.value['telefone'];

    this.cadastrarRespTecnicoForm.controls['telefone'].setValue(StringUtil.formatarTelefone(tel));
  }

  carregaFormManutencao() {

    this.cadastrarRespTecnicoForm.controls['cpf'].setValue(this.responsavelTecnicoPesquisado.cpf);
    const controlCPF = this.cadastrarRespTecnicoForm.get('cpf');
    controlCPF.disable({ onlySelf: true });

    this.cadastrarRespTecnicoForm.
      controls['identificacao'].setValue(this.cpfPipe.
        transform(this.responsavelTecnicoPesquisado.cpf) + ' - ' +
        this.responsavelTecnicoPesquisado.nome);
    this.cadastrarRespTecnicoForm.controls['email'].
      setValue(this.responsavelTecnicoPesquisado.email);

    this.cadastrarRespTecnicoForm.controls['telefone'].
      setValue(this.responsavelTecnicoPesquisado.telefone);
    const controlTelefone = this.cadastrarRespTecnicoForm.get('telefone');
    if (this.emDetalhamento) {
      controlTelefone.disable({ onlySelf: true });
    }

    if (this.responsavelTecnicoPesquisado.registrosProfissional) {
      const registroProfissional = this.responsavelTecnicoPesquisado.registrosProfissional.map((registro) => Object.assign(new RegistroProfissional(), registro));

      this.data = registroProfissional;
    } else {
      this.data = [];
      this.responsavelTecnicoPesquisado.registrosProfissional = this.data;
    }
    this.loadExportColumns();

    this.responsavelTecnicoPesquisado.contratoFk = this.contratoVinculado.contratoFk;
  }



  loadExportColumns() {
    const columns = [
      'ATIVIDADE', 'CAU/CREA', 'UF'
    ];

    this.data.forEach((element: RegistroProfissional) => {
      const linha = [];
      linha.push(element.atividade);
      linha.push(element.nrCreaCau);
      linha.push(element.uf);
      this.dataExport.push(linha);
    });
    this.export = new DataExport(columns, this.dataExport);
  }

  carregaForm() {
    this.telefoneCtrl = new FormControl(null, [Validators.required, validateTelefone]);
    this.cpfCtrl = new FormControl(null, Validators.required);

    this.identificacaoCtrl = new FormControl(null);
    this.identificacaoCtrl.disable({ onlySelf: true });

    this.emailCtrl = new FormControl(null);
    this.emailCtrl.disable({ onlySelf: true });

    this.atividadeCtrl = new FormControl(null, Validators.required);

    this.ufCtrl = new FormControl(null, Validators.required);

    this.creacauCtrl = new FormControl(null, Validators.required);

    this.cadastrarRespTecnicoForm = this.formBuilder.group({
      cpf: this.cpfCtrl,
      identificacao: this.identificacaoCtrl,
      email: this.emailCtrl,
      telefone: this.telefoneCtrl,
      atividade: this.atividadeCtrl,
      uf: this.ufCtrl,
      crea_cau: this.creacauCtrl
    });

    this.atividades = [{ 'nome': 'Arquitetura', 'id': 1 }, { 'nome': 'Engenharia', 'id': 2 }];

    this.ufs = [{ 'sigla': 'AC' }, { 'sigla': 'AL' }, { 'sigla': 'AP' }, { 'sigla': 'AM' },
    { 'sigla': 'BA' }, { 'sigla': 'CE' }, { 'sigla': 'DF' }, { 'sigla': 'ES' },
    { 'sigla': 'GO' }, { 'sigla': 'MA' }, { 'sigla': 'MT' }, { 'sigla': 'MS' },
    { 'sigla': 'MG' }, { 'sigla': 'PA' }, { 'sigla': 'PB' }, { 'sigla': 'PR' },
    { 'sigla': 'PE' }, { 'sigla': 'PI' }, { 'sigla': 'RJ' }, { 'sigla': 'RN' },
    { 'sigla': 'RS' }, { 'sigla': 'RO' }, { 'sigla': 'RR' }, { 'sigla': 'SC' },
    { 'sigla': 'SP' }, { 'sigla': 'SE' }, { 'sigla': 'TO' }];
  }

  pesquisar(template: TemplateRef<any>) {
    this._messageService.dismissAll();

    const cpfControl = this.cadastrarRespTecnicoForm.controls['cpf'];
    if (!cpfControl.valid) {
      cpfControl.markAsTouched({ onlySelf: true });
    } else {

      if ((cpfControl.valid && cpfControl.value !== null && cpfControl.value !== '') && (this.tipoConsulta)) {
        this._resptecnicoservice.consultarResponsavelTecnicoPorTipo(this.cadastrarRespTecnicoForm.value['cpf'].replace(/\D/g, ''),
          this.tipoConsulta,
          this.contrato.id).subscribe(value => {

            this.responsavelTecnicoPesquisado = value;
            this.responsavelTecnicoPesquisado.contratoFk = this.contrato.id;
            if (this.responsavelTecnicoPesquisado.registrosProfissional) {
              this.validaRegistroProfissionalAssociadoCTEF(template);
            } else {
              this.confirmaEdicao();
            }
          },
            error => {
              this.cancelaEdicao();
            }
          );
      }
    }
  }

  private validaRegistroProfissionalAssociadoCTEF(template: TemplateRef<any>) {
    this.obterContrato();
    if (this.registroProfissional) {
      // Verifica se existe algum Registro Profissional associado ao CTEF em questão
      if (this.existeRPAssociadoCTEFQuestao(this.responsavelTecnicoPesquisado.registrosProfissional)) {
        if (!this.existeSubmetaAssinadaParaRT(this.responsavelTecnicoPesquisado.registrosProfissional)) {
          this.modalConfirmarEdicao(template);
        } else {
          this._messageService.error('Responsável Técnico possui submeta assinada e não pode ser editado.');
        }
      } else {
        this.contratoVinculado = null;
        this.confirmaEdicao();
      }
    }
  }

  /**
   * Carrega o Formulário após a pesquisa do CPF.
   *
   */
  private carregarForm() {

    this.cadastrarRespTecnicoForm.controls['cpf'].setValue(this.cpfPipe.transform(this.responsavelTecnicoPesquisado.cpf));
    this.cadastrarRespTecnicoForm.
      controls['identificacao'].setValue(this.cpfPipe.
        transform(this.responsavelTecnicoPesquisado.cpf) + ' - ' +
        this.responsavelTecnicoPesquisado.nome);
    this.cadastrarRespTecnicoForm.controls['email'].
      setValue(this.responsavelTecnicoPesquisado.email);
    this.cadastrarRespTecnicoForm.controls['telefone'].
      setValue(this.responsavelTecnicoPesquisado.telefone);

    if (this.responsavelTecnicoPesquisado.registrosProfissional) {
      let registroProfissional = this.responsavelTecnicoPesquisado.registrosProfissional.map((registro) => Object.assign(new RegistroProfissional(), registro));
      this.data = registroProfissional;
      if (this.data.length == 1) {
        this.associarRPContrato(registroProfissional[0]);
      }
    } else {
      this.data = [];
      this.responsavelTecnicoPesquisado.registrosProfissional = this.data;
    }

    this.responsavelTecnicoPesquisado.contratoFk = this.contrato.id;

  }

  async modalConfirmarEdicao(template: TemplateRef<any>) {
    this.modalConfirmacaoEdicaoRT = this._modalService.show(template, { class: 'modal-sm' });
  }


  public getListaPaginada(listap) {
    this.lista = listap;
  }

  verificaTipoConsulta(event) {
    const target = event.target;

    if (target.checked) {
      if (this.tipoConsulta !== target.value) {
        this.emEdicaoReg = false;
        this.emEdicaoResp = false;
        this.cadastrarRespTecnicoForm.controls['identificacao'].setValue('');
        this.cadastrarRespTecnicoForm.controls['email'].setValue('');
        this.data = [];
      }
      this.tipoConsulta = target.value;
    } else {
      this.tipoConsulta = '';
    }
  }

  selectChange(event) {
    if (event && event.id === 1) {
      this.atividadeSelecionada = 'arq';
    } else if (event && event.id === 2) {
      this.atividadeSelecionada = 'eng';
    } else {
      this.atividadeSelecionada = '';
    }
  }

  voltar() {
    this._router.navigate(['./listar'], { relativeTo: this.route.parent });
  }

  cancelar() {
    this._router.navigate(['./listar'], { relativeTo: this.route.parent });
  }

  adicionarRegistro() {
    let ufRp;
    const regProf = new RegistroProfissional;
    const controlAtividade = this.cadastrarRespTecnicoForm.get('atividade');
    const controlCreaCau = this.cadastrarRespTecnicoForm.get('crea_cau');
    const controlUf = this.cadastrarRespTecnicoForm.get('uf');
    let falhaValidacao = false;

    if (this.atividadeSelecionada === 'eng') {
      ufRp = this.cadastrarRespTecnicoForm.controls['uf'].value['sigla'];
    } else {
      ufRp = '';
    }

    regProf.atividade = this.cadastrarRespTecnicoForm.controls['atividade'].value['nome'];
    regProf.nrCreaCau = this.cadastrarRespTecnicoForm.controls['crea_cau'].value;
    regProf.uf = ufRp;

    if (!controlAtividade.valid) {
      controlAtividade.markAsTouched({ onlySelf: true });
      falhaValidacao = true;
    }

    if (!controlCreaCau.valid) {
      controlCreaCau.markAsTouched({ onlySelf: true });
      falhaValidacao = true;
    }

    if ((this.atividadeSelecionada === 'eng') && (!controlUf.valid)) {
      controlUf.markAsTouched({ onlySelf: true });
      falhaValidacao = true;
    }

    if (!this.validaRegistroDuplicado(regProf, true)) {

      if (!falhaValidacao) {
        this.data = [...this.data, regProf];

        this.cadastrarRespTecnicoForm.controls['atividade'].setValue('');
        this.cadastrarRespTecnicoForm.controls['crea_cau'].setValue('');
        this.cadastrarRespTecnicoForm.controls['uf'].setValue('');

        this.emEdicaoReg = false;

        if (this.data.length === 1) {
          this.associarRPContrato(regProf);
        }
      }
    }

  }

  alterarRegistro() {
    let ufRp;
    let falhaValidacao = false;
    let atividade;
    let crea_cau;
    const regProf = new RegistroProfissional;
    const controlAtividade = this.cadastrarRespTecnicoForm.get('atividade');
    const controlCreaCau = this.cadastrarRespTecnicoForm.get('crea_cau');
    const controlUf = this.cadastrarRespTecnicoForm.get('uf');


    atividade = this.getAtividade(atividade);

    if (atividade === '' || atividade === 'selecione') {
      this.validarOpcaoAtividade();
      falhaValidacao = true;
    }

    if (this.atividadeSelecionada === 'eng') {
      ufRp = this.getUf(ufRp);
      if (ufRp === '' || ufRp === 'selecione') {
        this.validarOpcaoUF();
        falhaValidacao = true;
      }
    } else {
      ufRp = '';
    }

    crea_cau = this.cadastrarRespTecnicoForm.controls['crea_cau'].value;

    //carrega os dados alterados na tela para validar
    regProf.id = this.rpAnterior.id;
    regProf.atividade = atividade;
    regProf.nrCreaCau = crea_cau;
    regProf.uf = ufRp;

    if (!controlAtividade.valid) {
      controlAtividade.markAsTouched({ onlySelf: true });
      falhaValidacao = true;
    }

    if (!controlCreaCau.valid) {
      controlCreaCau.markAsTouched({ onlySelf: true });
      falhaValidacao = true;
    }

    if ((this.atividadeSelecionada === 'eng') && (!controlUf.valid)) {
      controlUf.markAsTouched({ onlySelf: true });
      falhaValidacao = true;
    }

    if (!this.validaRegistroDuplicado(regProf, false)) {
      if (!falhaValidacao) {
        this.rpAnterior.atividade = atividade;
        this.rpAnterior.nrCreaCau = crea_cau;
        this.rpAnterior.uf = ufRp;

        this.cadastrarRespTecnicoForm.controls['atividade'].setValue('');
        this.cadastrarRespTecnicoForm.controls['crea_cau'].setValue('');
        this.cadastrarRespTecnicoForm.controls['uf'].setValue('');

        this.emEdicaoReg = false;
        this.rpAnterior = null;
      }
    }
  }

  private getAtividade(atividade: any) {
    if (this.cadastrarRespTecnicoForm.controls['atividade'].value['nome']) {
      atividade = this.cadastrarRespTecnicoForm.controls['atividade'].value['nome'];
    } else {
      atividade = this.cadastrarRespTecnicoForm.controls['atividade'].value;
    }
    return atividade;
  }

  private getUf(ufRp: any) {
    if (this.cadastrarRespTecnicoForm.controls['uf'].value['sigla']) {
      ufRp = this.cadastrarRespTecnicoForm.controls['uf'].value['sigla'];
    } else {
      ufRp = this.cadastrarRespTecnicoForm.controls['uf'].value;
    }
    return ufRp;
  }

  private validarOpcaoAtividade() {
    const ctrlAtividade = this.cadastrarRespTecnicoForm.controls['atividade'];
    ctrlAtividade.markAsTouched({ onlySelf: true });
    this._messageService.error('Selecione uma opção válida para Atividade');
  }

  private validarOpcaoUF() {
    const ctrlUf = this.cadastrarRespTecnicoForm.controls['uf'];
    ctrlUf.markAsTouched({ onlySelf: true });
    this._messageService.error('Selecione uma opção válida para UF');
  }

  validaRegistroDuplicado(rp: RegistroProfissional, validaInclusao: Boolean): RegistroProfissional {
    const regDuplicado = this.data.filter((registro) => {

      if (registro.id === rp.id && rp.id === undefined && rp.atividade === registro.atividade ) {
          if (validaInclusao) {
            if (rp.atividade === 'Engenharia'
                && rp.uf === registro.uf) {
                this._messageService.error('Responsável Técnico já possui CREA para a UF informada.');
                return true;
            } else if (rp.atividade === 'Arquitetura') {
                this._messageService.error('Responsável Técnico já possui CAU cadastrado.');
                return true;
            }
          } else {
            if (rp.atividade === 'Engenharia' && rp.uf !== this.rpAnterior.uf && rp.uf === registro.uf) {
              this._messageService.error('Responsável Técnico já possui CREA para a UF informada.');
              return true;
            }
          }
          return false;
      } else {
        if ((rp.id !== registro.id || rp.id === undefined)
        && rp.atividade === registro.atividade) {
        if (rp.atividade === 'Engenharia'
          && rp.uf === registro.uf) {
          this._messageService.error('Responsável Técnico já possui CREA para a UF informada.');
          return true;
        } else if (rp.atividade === 'Arquitetura') {
          this._messageService.error('Responsável Técnico já possui CAU cadastrado.');
          return true;
        }
        return false;
        }
      }
    });
    return regDuplicado[0];
  }

  incluirReg() {

    //Limpa o RT anterior a fim de tratar a inclusão. Sem isso botão Alterar
    // estava sendo exibido no lugar do Adicionar.
    this.rpAnterior = null;

    const controlAtividade = this.cadastrarRespTecnicoForm.get('atividade');
    const controlCreaCau = this.cadastrarRespTecnicoForm.get('crea_cau');
    const controlUf = this.cadastrarRespTecnicoForm.get('uf');

    controlAtividade.reset();
    controlCreaCau.reset();
    controlUf.reset();

    this.cadastrarRespTecnicoForm.controls['atividade'].setValue('');
    this.cadastrarRespTecnicoForm.controls['crea_cau'].setValue('');
    this.cadastrarRespTecnicoForm.controls['uf'].setValue('');

    this.emEdicaoReg = true;
    this.atividadeSelecionada = '';

  }

  excluirRP(item: RegistroProfissional) {
    this.data.splice(this.data.indexOf(item), 1);

    // Força uma atualização da ref. da variável para o Angular detectar a mudança
    this.data = this.data.slice();

    // verifica se o registro profissional que está sendo excluído está vinculado ao contrato
    if (item.contratos && item.obterContratoVinculado(this.contratoVinculado.contratoFk)) {
      this.contratoVinculado = null;
    }
  }

  existeRPAssociadoCTEFQuestao(listaRegistrosProfissionais: Array<RegistroProfissional>) {
    let isRPAssociado = false;

    for (let rp of listaRegistrosProfissionais) {
      if ((rp.contratos) && (rp.contratos.length !== 0)) {
        for (const contratoRT of rp.contratos) {
          if (contratoRT.contratoFk == this.contrato.id) {
            isRPAssociado = true;
            break;
          }
        }
      }
    }

    return isRPAssociado;
  }

  existeSubmetaAssinadaParaRT(listaRegistrosProfissionais: Array<RegistroProfissional>) {
    let existeSubmetaAssinada = false;

    for (const rp of listaRegistrosProfissionais) {
      if ((rp.contratos) && (rp.contratos.length !== 0)) {
        for (const contratoRT of rp.contratos) {
          if (contratoRT.contratoFk == this.contrato.id && contratoRT.possuiSubmetaAssinada) {
            existeSubmetaAssinada = true;
            break;
          }
        }
      }
    }

    return existeSubmetaAssinada;
  }

  isRPAssociadoOutroCTEF(rp: RegistroProfissional) {
    let isRPAssociado = false;

    if (!rp.isRPAssociadoOutroCTEF) {
      if ((rp.contratos) && (rp.contratos.length !== 0)) {
        for (const contratoRT of rp.contratos) {
          if (contratoRT.contratoFk != this.contrato.id) {
            isRPAssociado = true;
            break;
          }
        }
      }
      rp.isRPAssociadoOutroCTEF = isRPAssociado;
    } else {
      isRPAssociado = rp.isRPAssociadoOutroCTEF;
    }
    return isRPAssociado;
  }

  confirmaEdicao() {
    if (this.modalConfirmacaoEdicaoRT) {
      this.modalConfirmacaoEdicaoRT.hide();
      this._router.navigate(['../', this.responsavelTecnicoPesquisado.idMedContratoRT, 'editar'], { relativeTo: this.route });
    } else {
      this.emEdicaoResp = true;
      this.emEdicaoRespPorBuscaCPF = true;
      this.carregarForm();
    }
  }

  cancelaEdicao() {
    if (this.modalConfirmacaoEdicaoRT) {
      this.modalConfirmacaoEdicaoRT.hide();
    }

    this.emEdicaoReg = false;
    this.emEdicaoResp = false;
    this.cadastrarRespTecnicoForm.controls['identificacao'].setValue('');
    this.cadastrarRespTecnicoForm.controls['email'].setValue('');
    this.cadastrarRespTecnicoForm.controls['telefone'].setValue('');
    this.data = [];
  }


  obterContrato() {
    this.registroProfissional = Object.assign(new RegistroProfissional(), this.responsavelTecnicoPesquisado.obterRegistroVinculado(this.contrato.id));
    if (this.registroProfissional.contratos) {
      this.contratoVinculado = this.registroProfissional.obterContratoVinculado(this.contrato.id);
    } else {
      this.contratoVinculado = new ContratoResponsavelTecnico();
      this.contratoVinculado.contratoFk = this.contrato.id;
      this.contratoVinculado.tipo = {codigo: this.tipoConsulta};
    }
  }

  associarRPContrato(registroAssociado: RegistroProfissional) {
    if (!this.contratoVinculado) {
      this.obterContrato();
    }

    this.data.forEach((registro) => {
      if (registro.contratos) {
        const indice = registro.contratos.indexOf(this.contratoVinculado);
        if (indice > -1) {
          registro.contratos.splice(indice, 1);
        }
      }

      if (registro === registroAssociado) {
        registro.contratos = [];
        registro.contratos.push(this.contratoVinculado);
      }
    });
  }


  prepararAlteracaoRegistroProfissional(rp: RegistroProfissional) {
    this.emEdicaoReg = true;
    if (rp.atividade === 'Arquitetura') {
      this.atividadeSelecionada = 'arq';
    } else {
      this.atividadeSelecionada = 'eng';
    }
    this.rpAnterior = rp;
    this.cadastrarRespTecnicoForm.controls['atividade'].setValue(rp.atividade);
    this.cadastrarRespTecnicoForm.controls['crea_cau'].setValue(rp.nrCreaCau);
    this.cadastrarRespTecnicoForm.controls['uf'].setValue(rp.uf);
  }

  salvar() {

    const control = this.cadastrarRespTecnicoForm.get('telefone');

    if (!control.valid) {
      control.markAsTouched({ onlySelf: true });
    } else if (this.data.length === 0 || !this.contratoVinculado) {
      this._messageService.error('O Responsável Técnico deve ter pelo menos um Registro Profissional informado.');
    } else {
      this.responsavelTecnicoPesquisado.telefone = this.cadastrarRespTecnicoForm.value['telefone'];
      this.responsavelTecnicoPesquisado.registrosProfissional = [];
      this.responsavelTecnicoPesquisado.registrosProfissional = this.data;

      this._resptecnicoservice.salvar(this.responsavelTecnicoPesquisado, this.contrato.id).subscribe(value => {

        super.adicionarMensagem('Responsável Técnico salvo com sucesso!', true);
        this._router.navigate(['./listar'], { relativeTo: this.route.parent });
      });
    }
  }

}
