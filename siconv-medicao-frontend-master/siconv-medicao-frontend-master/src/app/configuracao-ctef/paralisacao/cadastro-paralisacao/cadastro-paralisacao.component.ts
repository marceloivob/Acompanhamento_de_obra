import { Component, Injector } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import moment from 'moment';
import { Contrato } from 'src/app/shared/model/contrato.model';
import { Paralisacao, ResponsavelParalisacao, IndicativoParalisacao, MotivoParalisacao, AnexoParalisacao } from 'src/app/shared/model/paralisacao.model';
import { RequiredAuthorizer } from 'src/app/shared/model/security/required-authorizer.model';
import { ContratoService } from 'src/app/shared/services/contrato.service';
import { ParalisacaoService } from 'src/app/shared/services/paralisacao.service';
import { BaseComponent } from 'src/app/shared/util/base.component';
import { FileUtil } from 'src/app/shared/util/file-util';
import { DateValidators } from 'src/app/shared/validators/date-validators';

@Component({
  selector: 'app-cadastro-paralisacao',
  templateUrl: './cadastro-paralisacao.component.html',
})
export class CadastroParalisacaoComponent extends BaseComponent {

  acao: string;
  hoje = moment().toDate(); // data maxima para selecao no date-picker
  form: FormGroup;
  paralisacao: Paralisacao;
  anexoAtual: AnexoParalisacao;

  constructor(
    private _route: ActivatedRoute,
    private _router: Router,
    private _contratoService: ContratoService,
    private _paralisacaoService: ParalisacaoService,
    private _fb: FormBuilder,
    private injector: Injector
  ) {
    super(injector);
  }

  protected initializeComponent(): void {
    const url = this._route.snapshot.url;
    this.acao = url[url.length - 1].path;
    this.paralisacao = this.isInclude() ? new Paralisacao() : this._route.snapshot.data.paralisacao;
    this.initForm();
  }

  protected loadPermissions(): Map<string, RequiredAuthorizer> {
    return new Map([]);
  }

  private initForm() {
    this.form = this._fb.group({
      dataInicio:  [this.paralisacao.dataInicio],
      dataFim:     [this.paralisacao.dataFim, DateValidators.after('dataInicio')],
      responsavel: [this.paralisacao.responsavel.codigo],
      indicativo:  [this.paralisacao.indicativo.codigo],
      motivo:      [this.paralisacao.motivo.codigo],
      observacao:  [this.paralisacao.observacao],
      arquivo:     ['']
    });

    if (this.isDetail()) {
      this.form.disable();
    }
  }

  public onChangeDataInicio() {
    const dataFimCtrl = this.form.get('dataFim');
    dataFimCtrl.updateValueAndValidity();
    dataFimCtrl.markAsTouched();
  }

  public salvar() {
    if (!this.isFormularioValido()) return;

    this.atualizarModelComValoresFormulario();

    const observable = this.isInclude() ? this._paralisacaoService.incluir(this.paralisacao, this.contrato.id)
                                        : this._paralisacaoService.alterar(this.paralisacao);

    observable.subscribe(() => this.exibirMensagemSucesso());
  }

  private isFormularioValido(): boolean {
    for(const ctrl in this.form.controls) {
      this.form.get(ctrl).markAsTouched({ onlySelf: true });
    }
    return this.form.valid;
  }

  private atualizarModelComValoresFormulario() {
    this.paralisacao.dataInicio  = this.form.value['dataInicio'];
    this.paralisacao.dataFim     = this.form.value['dataFim'];
    this.paralisacao.responsavel = new ResponsavelParalisacao(this.form.value['responsavel']);
    this.paralisacao.indicativo  = new IndicativoParalisacao(this.form.value['indicativo']);
    this.paralisacao.motivo      = new MotivoParalisacao(this.form.value['motivo']);
    this.paralisacao.observacao  = this.form.value['observacao'];
  }

  private exibirMensagemSucesso() {
    super.adicionarMensagem ('Paralisação de Obra salva com sucesso.', true);
    this.voltar();
  }

  public selecionarArquivo(event: any) {

    if (event.target.files.length > 0) {
      const arquivo = event.target.files[0];
      const arquivoCtrl = this.form.get('arquivo');

      if (FileUtil.validarArquivo(arquivo, 10, arquivoCtrl)) {
        this.anexoAtual = new AnexoParalisacao();
        this.anexoAtual.arquivo = arquivo;
        this.anexoAtual.nmArquivo = arquivo.name;

      } else {
        this.anexoAtual = null;
      }

    } else {
      this.anexoAtual = null;
    }
  }

  public adicionarAnexoListagem() {
    if (this.anexoAtual) {
      this.paralisacao.anexos = [...this.paralisacao.anexos, this.anexoAtual];
      this.form.get('arquivo').setValue(null);
      this.anexoAtual = null;
    }
  }

  public excluirAnexoListagem(anexo: AnexoParalisacao) {

    this.paralisacao.anexos.splice(this.paralisacao.anexos.indexOf(anexo), 1);

    // Força uma atualização da ref. da variável para o Angular detectar a mudança
    this.paralisacao.anexos = this.paralisacao.anexos.slice();
}


  public voltar() {
    this._router.navigate(['./listar'], { relativeTo: this._route.parent });
  }

  get listaResponsavel(): ResponsavelParalisacao[] {
    return ResponsavelParalisacao.LISTA_DOMINIO;
  }

  get listaIndicativo(): IndicativoParalisacao[] {
    return IndicativoParalisacao.LISTA_DOMINIO;
  }

  get listaMotivo(): MotivoParalisacao[] {
    return MotivoParalisacao.LISTA_DOMINIO;
  }

  get contrato(): Contrato {
    return this._contratoService.contratoAtual;
  }

  public isInclude(): boolean {
    return this.acao === 'incluir';
  }

  public isDetail(): boolean {
    return this.acao === 'detalhar';
  }

  public isEdit(): boolean {
    return this.acao === 'editar';
  }
}
