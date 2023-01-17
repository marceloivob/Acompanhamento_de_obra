import { Contrato } from 'src/app/shared/model/contrato.model';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { Component, Input, Injector } from '@angular/core';
import { Medicao } from 'src/app/shared/model/medicao.model';
import { dataFinalMedicaoPosteriorDataInicioMedicaoValidator,
          dataInicioObraDataAtualValidator,
          dataFinalMedicaoRequiredValidator,
          dataVistoriaAnteriorDataInicioObraValidator,
          dataVistoriaMedicaoRequiredValidator } from '../../../shared/validators/medicao-validator';
import { ActivatedRoute, Router } from '@angular/router';
import { SolicitanteVistoriaExtraEnum } from '../../../shared/model/solicitante-vistoria-extra.model';
import { BaseComponent } from 'src/app/shared/util/base.component';
import { RequiredAuthorizer } from 'src/app/shared/model/security/required-authorizer.model';
import { Profile } from 'src/app/shared/model/security/profile.enum';
import { Role } from 'src/app/shared/model/security/role.enum';
import { Permission } from 'src/app/shared/model/security/permission.enum';


@Component({
  selector: 'app-form-dados-medicao',
  templateUrl: './form-dados-medicao.component.html'
})
export class FormDadosMedicaoComponent extends BaseComponent {

   // Variável do Select de Solicitante Vistoria Extra
   listaSolicitante = [];

  @Input()
  contrato: Contrato;

  @Input()
  medicao: Medicao;

  // Dado obtido da rota
  acao: string;

  medicaoForm = new FormGroup({});
  dtNascimento: Date;
  dtMaxima = new Date();
  dtInicioObraCtrl: FormControl;
  dtFimCtrl: FormControl;
  dtVistoriaCtrl: FormControl;
  inVistoriaExtraCtrl: FormControl;
  solicitanteVistoriaExtraCtrl: FormControl;

  enumSolicitante = SolicitanteVistoriaExtraEnum;


  constructor(
    private readonly fb: FormBuilder,
    private _route: ActivatedRoute,
    private _router: Router,
    injector: Injector
  ) {
    super(injector);
  }

  initializeComponent() {
    const url = this._route.snapshot.url;
    this.acao = url[url.length - 1].path;
    this.initForm();
  }

  loadPermissions(): Map<string, RequiredAuthorizer> {
    const profiles = [Profile.EMPRESA];
    const roles = [Profile.PROPONENTE];
    const profileConcedenteMandataria = [Profile.CONCEDENTE, Profile.MANDATARIA];

    return new Map([
      ['editar', new RequiredAuthorizer(profiles, [Role.FISCAL_CONCEDENTE, Role.GESTOR_CONVENIO_CONCEDENTE, Role.GESTOR_FINANCEIRO_CONCEDENTE, Role.OPERACIONAL_CONCEDENTE, Role.FISCAL_ACOMPANHAMENTO, Role.TECNICO_TERCEIRO], [Permission.EDITAR_MEDICAO])],
      ['incluir', new RequiredAuthorizer(profiles, [], [Permission.INCLUIR_MEDICAO])],
      ['enviar_medicao', new RequiredAuthorizer(profiles, [], [Permission.ENVIAR_MEDICAO_CONVENENTE])],
      ['atestar_medicao', new RequiredAuthorizer(roles, [Role.FISCAL_CONVENENTE, Role.GESTOR_CONVENIO_CONVENENTE,
                                                         Role.GESTOR_FINANCEIRO_CONVENENTE, Role.OPERADOR_FINANCEIRO_CONVENENTE], [])],
      ['solicitar_complementacao_empresa', new RequiredAuthorizer(roles, [Role.FISCAL_CONVENENTE, Role.GESTOR_CONVENIO_CONVENENTE,
        Role.GESTOR_FINANCEIRO_CONVENENTE, Role.OPERADOR_FINANCEIRO_CONVENENTE], [])],
      ['aceitar_medicao', new RequiredAuthorizer(profileConcedenteMandataria, [Role.FISCAL_CONCEDENTE, Role.GESTOR_CONVENIO_CONCEDENTE, Role.GESTOR_FINANCEIRO_CONCEDENTE, Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA, Role.FISCAL_ACOMPANHAMENTO, Role.TECNICO_TERCEIRO], [])],
      ['salvar_dados_vistoria', new RequiredAuthorizer(profileConcedenteMandataria, [Role.FISCAL_CONCEDENTE, Role.GESTOR_CONVENIO_CONCEDENTE, Role.GESTOR_FINANCEIRO_CONCEDENTE, Role.OPERACIONAL_CONCEDENTE, Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA, Role.FISCAL_ACOMPANHAMENTO, Role.TECNICO_TERCEIRO], [])],

    ]);
  }

  get dtInicioObraVisivel() {
    return !this.isDetail() && this.medicao.sequencial === 1
              && this.medicao.permiteComplementacaoValor != false
              && (!this.medicao.situacao || this.medicao.situacao.codigo === 'EM' || this.medicao.situacao.codigo === 'CE');
 }

  async initForm() {

    let dtInicioObra;
    let dtInicioMedicao;
    let dtFimMedicao;
    let dtVistoria;
    let inVistoria;
    let solicitante;

    const dtAtual = new Date();

    dtInicioObra = this.medicao.dataInicioObra;
    dtInicioMedicao = this.medicao.dataInicio;
    dtFimMedicao = this.medicao.dataFim;

    dtVistoria = this.medicao.dataVistoriaExtra;
    inVistoria = this.medicao.vistoriaExtra;
    solicitante = this.medicao.solicitanteVistoriaExtra;

    this.dtFimCtrl = new FormControl(dtFimMedicao);

    this.dtInicioObraCtrl = new FormControl(dtInicioObra, null);

    this.dtVistoriaCtrl = new FormControl(dtVistoria);
    this.inVistoriaExtraCtrl = new FormControl(inVistoria);
    this.solicitanteVistoriaExtraCtrl = new FormControl(solicitante);

    this.listaSolicitante = this.converteEnum(this.enumSolicitante);
    if(inVistoria) {
      this.solicitanteVistoriaExtraCtrl.setValue(this.recuperarSolicitanteSelecionado(solicitante.codigo));
    }

    this.medicaoForm = this.fb.group({
      dtInicioObra: this.dtInicioObraCtrl,
      dtInicioMedicao: dtInicioMedicao,
      dtFimMedicao: this.dtFimCtrl,
      dtAtual: dtAtual,
      dtFimObrigatoria: false,
      dtVistoria: this.dtVistoriaCtrl,
      dtVistoriaObrigatoria: false,
      inVistoria: this.inVistoriaExtraCtrl,
      solicitante: this.solicitanteVistoriaExtraCtrl
    });

    this.dtInicioObraCtrl.setValidators ([Validators.required,
                                          dataInicioObraDataAtualValidator(this.medicaoForm.controls['dtInicioObra'])]);


    this.dtFimCtrl.setValidators ([dataFinalMedicaoPosteriorDataInicioMedicaoValidator,
                                   dataFinalMedicaoRequiredValidator]);


    this.dtVistoriaCtrl.setValidators([dataVistoriaAnteriorDataInicioObraValidator(this.medicao.dataInicioObra),
                                      dataVistoriaMedicaoRequiredValidator]);



 }

 recuperarSolicitanteSelecionado(idSolicitante: string) {
  const solicitanteEncontrado = this.listaSolicitante.filter ( sol => {
                                                                    return sol.codigo === idSolicitante;
                                                            });
  return solicitanteEncontrado[0];
}

  atualizaDataInicioMedicao(arg: any) {

    this.medicaoForm.patchValue({
      dtInicioMedicao: this.medicaoForm.value['dtInicioObra']
    });
  }

  get exibeEdicaoDataFimMedicao() {
    return this.isInclude() || (this.isEdit()
                                && this.medicao.permiteComplementacaoValor != false
                                && (this.medicao.situacao.codigo === 'EM' || this.medicao.situacao.codigo === 'CE' ));
  }

  onChangedVistoriaExtra(): void {
    if(!this.inVistoriaExtraCtrl.value) {
      this.solicitanteVistoriaExtraCtrl.setValue("");
    }
  }

  get exibeEdicaoDadosVistoriaExtra() {
    if(!this.isInclude()) {
      const exibirEdicao = (this.isEdit() && this.medicao.situacao.codigo === 'AC' && this.canAccess("salvar_dados_vistoria"));
      if(!exibirEdicao) {
        this.solicitanteVistoriaExtraCtrl.disable ({onlySelf: true});
        this.inVistoriaExtraCtrl.disable ({onlySelf: true});
      }
      return exibirEdicao;
    } else {
      return false;
    }
  }

  exibirSolicitante(): boolean {
      return !this.isInclude() && this.inVistoriaExtraCtrl.value &&  (this.isConcedenteMandataria() || this.medicao.situacao.codigo === 'ACT');
  }

  isConcedenteMandataria() : boolean {
    return this.usuarioLogado.hasProfile([Profile.CONCEDENTE]) || this.usuarioLogado.hasProfile([Profile.MANDATARIA]);
  }

  isInclude(): boolean {
    return this.acao === 'incluir';
  }

  isEdit(): boolean {
    return this.acao === 'editar';
  }

  isDetail(): boolean {
    return this.acao === 'detalhar';
  }

  isMedicaoAcumulada() {
    return(this.medicao.idMedicaoAgrupadora ? true : false);
  }

  exibirMedicaoAcumuladora(idMedicaoAgrupadora: number) {

    let shouldReuseRoute = this._router.routeReuseStrategy.shouldReuseRoute;
    this._router.routeReuseStrategy.shouldReuseRoute = () => false;
    this._router.navigate(['../../', idMedicaoAgrupadora, 'detalhar'], { relativeTo: this._route }).then(() => {
      this._router.routeReuseStrategy.shouldReuseRoute = shouldReuseRoute;
    });

  }

  get dtFimObrigatoria(): boolean {
    return this.medicaoForm.value['dtFimObrigatoria'];
  }

  set dtFimObrigatoria(value: boolean) {
    this.medicaoForm.patchValue({ dtFimObrigatoria: value });
  }

  get dtVistoriaObrigatoria(): boolean {
    return this.medicaoForm.value['dtVistoriaObrigatoria'];
  }

  set dtVistoriaObrigatoria(value: boolean) {
    this.medicaoForm.patchValue({ dtVistoriaObrigatoria: value });
  }

  converteEnum(tipoEnum: any) {
    return Object.keys(tipoEnum).map(o => {
                                                    return {codigo: o, descricao: tipoEnum[o]};
                                              });
  }
}
