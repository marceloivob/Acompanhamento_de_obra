import { SituacaoMedicaoEnum } from './../../../../shared/enum/situacao-medicao.enum';
import { Component, Input, Injector, TemplateRef } from '@angular/core';
import { ListagemExpansivelComponent } from 'src/app/shared/util/listagem-expansivel.component';
import { Submeta } from 'src/app/shared/model/submeta.model';
import { Medicao } from 'src/app/shared/model/medicao.model';
import { ActivatedRoute } from '@angular/router';
import { RequiredAuthorizer } from 'src/app/shared/model/security/required-authorizer.model';
import { Profile } from 'src/app/shared/model/security/profile.enum';
import { FormControl, FormGroup } from '@angular/forms';
import { Servico } from 'src/app/shared/model/servico.model';
import { FrenteObra } from 'src/app/shared/model/frente-obra.model';
import { Macrosservico } from 'src/app/shared/model/macrosservico.model';
import { DecimalPipe } from '@angular/common';
import { MathUtil } from 'src/app/shared/util/math-util';
import { MedicaoService } from 'src/app/shared/services/medicao.service';
import { MedicaoAgrupada } from '../../../../shared/model/medicao-agrupada.model';
import { MemoriaCalculo } from '../../../../shared/model/memoria-calculo.model';
import { BsModalRef, BsModalService } from 'ngx-bootstrap';

@Component({
  selector: 'app-form-dados-submeta-servico',
  templateUrl: './form-dados-submeta-servico.component.html',
  styleUrls: ['./form-dados-submeta-servico.component.scss']
})
export class FormDadosSubmetaServicoComponent extends ListagemExpansivelComponent {

  MSG_GLOSA_EFETUADA: string = "Possui glosa efetuada.";
  MSG_GLOSAS_ANTERIORES: string = "Este quantitativo contempla glosas anteriores.";

  initialized: boolean;
  acao: string;
  modoComplementacaoAcumulada: boolean;
  msgSomatorioAgrupadas: String;

  // Funcao predicado para filtrar Frente Obra, Macrosservico e Servico na tabela
  filtroPreenchimento: (item?: any) => boolean;

  todasMedicoesAgrupamento: any[];
  memoriaCalculo: MemoriaCalculo[];
  modalMemoriaCalculoRef: BsModalRef;

  @Input() submeta: Submeta;
  @Input() medicao: Medicao;

  formControl: {[key: string]: FormControl} = {};
  medicaoSubmetaForm: FormGroup;

  constructor(
    private _decimalPipe: DecimalPipe,
    private _route: ActivatedRoute,
    private injector: Injector,
    private _medicaoService: MedicaoService,
    private _modalService: BsModalService
  ) {
    super(injector);
  }

  loadPermissions(): Map<string, RequiredAuthorizer> {
    return new Map([]);
  }

  initializeComponent() {
    const url = this._route.snapshot.url;
    this.acao = url[url.length - 1].path;
    this.modoComplementacaoAcumulada = url[0].path === 'acumulada';

    if (this.modoComplementacaoAcumulada) {
      // Recupera as medicoes do agrupamento (inclusive agrupadora)
      let medicaoAgrupadora = this._route.parent.snapshot.data.medicao;

      this._medicaoService.listarMedicoesAgrupadas(medicaoAgrupadora.id, false).subscribe((medicoesAgrupadas) => {
        medicoesAgrupadas.sort(MedicaoAgrupada.sort);
        this.todasMedicoesAgrupamento = [...medicoesAgrupadas, medicaoAgrupadora];
      });

      // No modo de complementacao de submeta acumulada exibe apenas itens
      // preenchidos originalmente pelo proprio ator
      if (this.isUsuarioEmpresa()) {
        this.filtroPreenchimento = (item: any) => item.preenchidoEmpresa;

      } else if (this.isUsuarioConvenente()) {
        this.filtroPreenchimento = (item: any) => item.preenchidoConvenente;
      }
    }

    this.initCamposPreenchimento();

    if (this.exibeMsgSomatorioAgrupadas()) {
      this.preencherMsgSomatorioAgrupadas();
    }

    this.initialized = true;
  }

  ngOnChanges() {
    if (!this.initialized) return;
    this.initCamposPreenchimento();
  }

  private initCamposPreenchimento() {
    this.submeta.frentesObra.forEach((frenteObra: FrenteObra) => {
      frenteObra.macroServicosView.forEach((macrosservico: Macrosservico) => {
        macrosservico.servicos.forEach((servico: Servico) => {

          if (this.isEdit() && servico.permiteMedicao) {
            const qtdRealizado = 'qtdRealizado' + this.getNomePerfil();
            const qtdAcumulado = 'qtdAcumulado' + this.getNomePerfil();
            const qdtRealizadoFormatado = this._decimalPipe.transform(servico[qtdRealizado], '1.2-2');

            let control = new FormControl(qdtRealizadoFormatado);
            this.formControl[frenteObra.id + '-' + servico.id] = control;

            servico.qdtRealizadoOriginal = servico[qtdRealizado];
            servico.qtdAcumuladoOriginal = servico[qtdAcumulado];
            servico.qtdInformada = servico[qtdRealizado];

            // Sugere valor de preenchimento para o convenente
            if (this.isEditConvenente() && servico.qtdRealizadoConvenente == null) {
                control.setValue(this._decimalPipe.transform(servico.qtdMaxPermitido, '1.2-2'));
                this.atualizarDadosServicoSubmeta('Convenente', frenteObra.id, macrosservico.id, servico.id, servico.qtdMaxPermitido);
            }

            // Sugere valor de preenchimento para o concedente
            if (this.isEditConcedente() && servico.qtdRealizadoConcedente == null) {
              control.setValue(this._decimalPipe.transform(servico.qtdMaxPermitido, '1.2-2'));
              this.atualizarDadosServicoSubmeta('Concedente', frenteObra.id, macrosservico.id, servico.id, servico.qtdMaxPermitido);
            }

            // Atualiza a exibicao dos hints de glosa para os inputs
            if (this.isEditConvenente() || this.isEditConcedente()) {
              this.atualizarHintGlosaInputControl(control, servico);
            }
          }

          this.configurarIndicadorPreenchimento(frenteObra, macrosservico, servico);

        });
      });
    });

    this.medicaoSubmetaForm = new FormGroup(this.formControl);
  }

  private configurarIndicadorPreenchimento(frenteObra: FrenteObra, macrosservico: Macrosservico, servico: Servico) {

    // Sinaliza que a Frente Obra, Macrosservico e Servico possuem algum valor
    // preenchido pelo ator. O indicador sera usado no filtro de preenchimento.

    if (servico.qtdRealizadoEmpresa != null) {
      frenteObra['preenchidoEmpresa'] = true;
      macrosservico['preenchidoEmpresa'] = true;
      servico['preenchidoEmpresa'] = true;
    }

    if (servico.qtdRealizadoConvenente != null) {
      frenteObra['preenchidoConvenente'] = true;
      macrosservico['preenchidoConvenente'] = true;
      servico['preenchidoConvenente'] = true;
    }
  }

  exibirCampoPreenchimento(coluna: string, servico: Servico): boolean {
    return this.isEdit() && this.getNomePerfil() === coluna && servico.permiteMedicao;
  }

  exibirCampoQtdRealizado(coluna: string, servico: Servico): boolean {
    const qtdRealizado = 'qtdRealizado' + coluna;
    return !this.exibirCampoPreenchimento(coluna, servico) && servico[qtdRealizado] != null;
  }

  onChangeInputQtdInformadaEmpresa(
    idFrenteObra: number,
    idMacrosservico: number,
    idServico: number
  ) {
    let qtdInformada = this.parseInputValue(this.formControl[idFrenteObra + '-' + idServico].value);

    // O onChange foi definido para tratar apenas valores nulos (em branco), pois
    // o evento onBlur não é disparado pelo componente quando o valor é nulo.
    if (qtdInformada == null) {
      this.formControl[idFrenteObra + '-' + idServico].setValue('');
      this.atualizarDadosServicoSubmeta('Empresa', idFrenteObra, idMacrosservico, idServico, qtdInformada);
    }
  }

  onBlurInputQtdInformadaEmpresa(
    idFrenteObra: number,
    idMacrosservico: number,
    idServico: number
  ) {
    let qtdInformada = this.parseInputValue(this.formControl[idFrenteObra + '-' + idServico].value);

    if (qtdInformada === 0 && !this.modoComplementacaoAcumulada) {
      qtdInformada = null;
      this.formControl[idFrenteObra + '-' + idServico].setValue('');
    }

    this.atualizarDadosServicoSubmeta('Empresa', idFrenteObra, idMacrosservico, idServico, qtdInformada);
  }

  onBlurInputQtdInformadaConvenente(
    idFrenteObra: number,
    idMacrosservico: number,
    idServico: number,
    servico: Servico
  ) {
    let control = this.formControl[idFrenteObra + '-' + idServico];
    let qtdInformada = this.parseInputValue(control.value);
    this.atualizarDadosServicoSubmeta('Convenente', idFrenteObra, idMacrosservico, idServico, qtdInformada);
    this.atualizarHintGlosaInputControl(control, servico);
  }

  onBlurInputQtdInformadaConcedente(
    idFrenteObra: number,
    idMacrosservico: number,
    idServico: number,
    servico: Servico
  ) {
    let control = this.formControl[idFrenteObra + '-' + idServico];
    let qtdInformada = this.parseInputValue(control.value);
    this.atualizarDadosServicoSubmeta('Concedente', idFrenteObra, idMacrosservico, idServico, qtdInformada);
    this.atualizarHintGlosaInputControl(control, servico);
  }

  private atualizarHintGlosaInputControl(control: FormControl, servico: Servico) {
    control['exibeHintGlosa'] =
      control.valid &&
      (servico.possuiGlosaConvenente ||
        servico.possuiGlosasAnterioresConvenente ||
        servico.possuiGlosaConcedente ||
        servico.possuiGlosasAnterioresConcedente);
  }

  private parseInputValue(value: any): number {
    return (!value || /^\s*$/.test(value)) ? null : parseFloat(value.toString().replaceAll('.', '').replace(',', '.'));
  }

  private atualizarDadosServicoSubmeta(coluna: string, idFrenteObra: number, idMacrosservico: number, idServico: number, qtdInformada: number) {
    const campoQtdRealizado = 'qtdRealizado' + coluna;
    const campoValorRealizado = 'valorRealizado' + coluna;
    const campoQtdAcumulado = 'qtdAcumulado' + coluna;
    const campoValorAcumulado = 'valorAcumulado' + coluna;

    const servico = this.submeta.frentesObra.find(
      (frenteObra) => frenteObra.id === idFrenteObra).macroServicosView.find(
        (macrosservico) => macrosservico.id === idMacrosservico).servicos.find(
           (serv) => { return serv.id === idServico; });

    servico.qtdInformada = qtdInformada;
    servico[campoQtdRealizado] = qtdInformada;
    servico[campoValorRealizado] = MathUtil.multiply(servico.preco, qtdInformada);

    if (qtdInformada == null && servico.qdtRealizadoOriginal === servico.qtdAcumuladoOriginal) {
      servico[campoQtdAcumulado] =  null;
    } else {
      servico[campoQtdAcumulado] =  MathUtil.add(MathUtil.subtract(servico.qtdAcumuladoOriginal, servico.qdtRealizadoOriginal), qtdInformada);
    }

    servico[campoValorAcumulado] = MathUtil.multiply(servico.preco, servico[campoQtdAcumulado]);

    this.validarCampoQuantidadeInformada(idFrenteObra, idMacrosservico, idServico, servico);

    // Atualiza os campos valorRealizado* e valorRealizadoAcumulado* da submeta
    this.atualizarTotalizadores(coluna);
  }

  private validarCampoQuantidadeInformada(idFrenteObra: number, idMacrosservico: number, idServico: number, servico: Servico) {

    if (servico.qtdInformada > servico.qtdMaxPermitido) {

      let msg = '';
      let detalhe: any = null;

      if (servico.qtdAcumuladoEmpresa > servico.qtd) {
        msg = 'O valor informado não é válido porque o acumulado da empresa não pode ser maior que o planejado.';

      } else if (servico.qtdAcumuladoConvenente > servico.qtdAcumuladoEmpresa) {
        msg = 'O valor informado não é válido porque o acumulado do convenente não pode ser maior que o acumulado empresa.';

      } else if (servico.qtdAcumuladoConcedente > servico.qtdAcumuladoConvenente) {
        msg = 'O valor informado não é válido porque o acumulado do concedente não pode ser maior que o acumulado convenente.';

      } else if (this.modoComplementacaoAcumulada) {

        let memoriaCalculo = this.getMemoriaCalculoErroQtdMaximaInvalida(servico);
        let ultimoRegistro = memoriaCalculo[memoriaCalculo.length - 1];

        if (ultimoRegistro.acumuladoEmpresa > servico.qtd) {
          msg = 'O valor informado não é válido porque, na medição ' + ultimoRegistro.sequencial +
                ', o acumulado da empresa não pode ser maior que o planejado.';

        } else if (ultimoRegistro.acumuladoConvenente > ultimoRegistro.acumuladoEmpresa) {
          msg = 'O valor informado não é válido porque, na medição ' + ultimoRegistro.sequencial +
                ', o acumulado do convenente não pode ser maior que o acumulado da empresa.';
        }

        detalhe = { memoriaCalculo };
      }

      this.formControl[idFrenteObra + '-' + idServico].setErrors({
        qtdMaximaInvalida: { msg, detalhe }
      });
    }
  }

  private getMemoriaCalculoErroQtdMaximaInvalida(servico: Servico): MemoriaCalculo[] {

    let memoriaCalculo: MemoriaCalculo[] = [];
    let acumuladoEmpresa = 0.0;
    let acumuladoConvenente = 0.0;

    this.todasMedicoesAgrupamento.forEach((medicaoAgrupamento) => {

      if (medicaoAgrupamento.id >= this.medicao.id
          && acumuladoEmpresa <= servico.qtd
          && acumuladoConvenente <= acumuladoEmpresa) {

        let memoria = new MemoriaCalculo();
        memoria.sequencial = medicaoAgrupamento.sequencial;
        memoria.dataInicio = medicaoAgrupamento.dataInicio;
        memoria.dataFim = medicaoAgrupamento.dataFim;

        let valorServico = servico.valoresPorIdMedicao.get(medicaoAgrupamento.id);

        if (valorServico) {

          // propria medicao (dto servico ja possui as qtd recalculadas)
          if (this.medicao.id === medicaoAgrupamento.id) {
            memoria.periodoEmpresa = servico.qtdRealizadoEmpresa;
            memoria.periodoConvenente = servico.qtdRealizadoConvenente;
            acumuladoEmpresa = servico.qtdAcumuladoEmpresa;
            acumuladoConvenente = servico.qtdAcumuladoConvenente;

          // medicao posterior (precisa atualizar as qtd acumuladas)
          } else {
            memoria.periodoEmpresa = valorServico.qtdEmpresa;
            memoria.periodoConvenente = valorServico.qtdConvenente;
            acumuladoEmpresa = MathUtil.add(acumuladoEmpresa, valorServico.qtdEmpresa);
            acumuladoConvenente = MathUtil.add(acumuladoConvenente, valorServico.qtdConvenente);
          }

          memoria.acumuladoEmpresa = acumuladoEmpresa;
          memoria.acumuladoConvenente = acumuladoConvenente;

          memoriaCalculo.push(memoria);
        }
      }
    });

    return memoriaCalculo;
  }

  // Atualiza os totalizadores com os valores informados nos serviços.
  private atualizarTotalizadores(coluna: string): void {
    this.submeta['valorRealizado' + coluna] = 0;
    this.submeta['valorRealizadoAcumulado' + coluna] = 0;

    this.submeta.frentesObra.forEach((frenteObra: FrenteObra) =>
      frenteObra.macroServicosView.forEach((macrosservico: Macrosservico) =>
        macrosservico.servicos.forEach((servico: Servico) => {
          this.submeta['valorRealizado' + coluna] = MathUtil.add(this.submeta['valorRealizado' + coluna], servico['valorRealizado' + coluna]);
          this.submeta['valorRealizadoAcumulado' + coluna] = MathUtil.add(this.submeta['valorRealizadoAcumulado' + coluna], servico['valorAcumulado' + coluna]);
        })));

    this.submeta['percentualRealizado' + coluna] = MathUtil.percentage(this.submeta['valorRealizado' + coluna], this.submeta.valor);
    this.submeta['percentualRealizadoAcumulado' + coluna] = MathUtil.percentage(this.submeta['valorRealizadoAcumulado' + coluna], this.submeta.valor);
  }

  isFormularioValido(): boolean {
    return this.medicaoSubmetaForm.valid;
  }

  isFormDirty() {
    return this.medicaoSubmetaForm.dirty;
  }

  markFormAsPristine() {
    this.medicaoSubmetaForm.markAsPristine();
  }

  expandAllMacrosservico(show: boolean, frenteObra: FrenteObra) {
    frenteObra.macroServicosView.forEach((macrosservico) => {
      const toggle = this.toggles.find((t) => t.source === macrosservico);
      if (toggle) {
        toggle.show = !show;
      }
    });
  }

  isEdit(): boolean {
    return this.acao === 'editar';
  }

  isEditEmpresa(): boolean {
    return this.isEdit() && this.isUsuarioEmpresa();
  }

  isEditConvenente(): boolean {
    return this.isEdit() && this.isUsuarioConvenente();
  }

  isEditConcedente(): boolean {
    return this.isEdit() && this.isUsuarioConcedente();
  }

  private isUsuarioEmpresa(): boolean {
    return this.usuarioLogado.profile === Profile.EMPRESA;
  }

  private isUsuarioConvenente(): boolean {
    return this.usuarioLogado.profile === Profile.PROPONENTE;
  }

  private isUsuarioConcedente(): boolean {
    return this.usuarioLogado.profile === Profile.CONCEDENTE
           || this.usuarioLogado.profile === Profile.MANDATARIA;
  }

  private getNomePerfil(): string {
    if (this.isUsuarioEmpresa()) {
      return 'Empresa';

    } else if (this.isUsuarioConvenente()) {
      return 'Convenente';

    } else if (this.isUsuarioConcedente()) {
      return 'Concedente';
    }
  }

  private preencherMsgSomatorioAgrupadas() {
    this._medicaoService.listarMedicoesAgrupadas(this.medicao.id, false).subscribe(
      medicoesAgrupadas => {
        if(medicoesAgrupadas && medicoesAgrupadas.length > 0) {
          medicoesAgrupadas.sort(MedicaoAgrupada.sort);
          this.msgSomatorioAgrupadas = "Somatório das medições " + medicoesAgrupadas[0].sequencial + " a " + this.medicao.sequencial;
        }
      }
    );
  }

  private exibeMsgSomatorioAgrupadas(): boolean {
    return (this.isEditConvenente() || this.isEditConcedente()) && this.medicao.isMedicaoAgrupadora;
  }

  isCampoPreenchimentoEmpresaObrigatorio(): Boolean{
    return this.medicao.idMedicaoAgrupadora && this.medicao.situacao.codigo === SituacaoMedicaoEnum.EM_COMPLEMENTACAO_PELA_EMPRESA;
  }

  exibirMemoriaCalculo(coluna: string, template: TemplateRef<any>, memoriaCalculo: MemoriaCalculo[]) {
    let modalSizeClass = (coluna === 'Convenente') ? 'modal-lg' : 'modal-md';
    this.memoriaCalculo = memoriaCalculo;
    this.modalMemoriaCalculoRef = this._modalService.show(template, { class: 'modal-memoria-calculo ' + modalSizeClass });
  }
}
