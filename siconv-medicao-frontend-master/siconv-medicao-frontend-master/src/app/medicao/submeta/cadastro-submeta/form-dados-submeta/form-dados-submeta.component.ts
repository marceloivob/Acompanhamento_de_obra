import { Component, Injector, Input, QueryList, ViewChildren } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ToggleComponent } from '@serpro/ngx-siconv';
import { Evento } from 'src/app/shared/model/evento.model';
import { FrenteObra } from 'src/app/shared/model/frente-obra.model';
import { Medicao } from 'src/app/shared/model/medicao.model';
import { Submeta } from 'src/app/shared/model/submeta.model';
import { ListagemExpansivelComponent } from 'src/app/shared/util/listagem-expansivel.component';
import { Permission } from '../../../../shared/model/security/permission.enum';
import { Profile } from '../../../../shared/model/security/profile.enum';
import { RequiredAuthorizer } from '../../../../shared/model/security/required-authorizer.model';
import { Role } from '../../../../shared/model/security/role.enum';

@Component({
  selector: 'app-form-dados-submeta',
  templateUrl: './form-dados-submeta.component.html',
  styleUrls: ['./form-dados-submeta.component.scss']
})
export class FormDadosSubmetaComponent extends ListagemExpansivelComponent {

  acao: string;

  @Input() submeta: Submeta;
  @Input() medicao: Medicao;

  @ViewChildren('rowToggle')
  rowToggleQuery: QueryList<ToggleComponent>;

  public medicaoSubmetaForm: FormGroup;

  constructor(private _route: ActivatedRoute, private injector: Injector) {
    super (injector);
  }

  initializeComponent() {
    const url = this._route.snapshot.url;
    this.acao = url[url.length - 1].path;

    this.initToggles();
  }

  loadPermissions(): Map <string, RequiredAuthorizer> {
    const profileConvenente = [Profile.PROPONENTE];
    const profileEmpresa = [Profile.EMPRESA];
    const profileConcedenteMandataria = [Profile.CONCEDENTE, Profile.MANDATARIA];

    const rolesEdicaoConvenente = [
      Role.FISCAL_CONVENENTE,
      Role.GESTOR_CONVENIO_CONVENENTE,
      Role.GESTOR_FINANCEIRO_CONVENENTE,
      Role.OPERADOR_FINANCEIRO_CONVENENTE];

      const rolesEdicaoConcedenteMandataria = [
        Role.FISCAL_CONCEDENTE,
        Role.GESTOR_CONVENIO_CONCEDENTE,
        Role.GESTOR_FINANCEIRO_CONCEDENTE,
        Role.OPERACIONAL_CONCEDENTE,
        Role.AGENTE_ACOMPANHAMENTO_INSTITUICAO_MANDATARIA,
        Role.FISCAL_ACOMPANHAMENTO,
        Role.TECNICO_TERCEIRO];

    return new Map([
      ['preencherSubmetaEmpresa', new RequiredAuthorizer (profileEmpresa, [], [Permission.EDITAR_SUBMETA])],
      ['preencherSubmetaConvenente', new RequiredAuthorizer(profileConvenente, rolesEdicaoConvenente, [])],
      ['preencherSubmetaConcedenteMandataria', new RequiredAuthorizer(profileConcedenteMandataria, rolesEdicaoConcedenteMandataria, [])]
    ]);
  }

  initToggles() {
    const formControl = {};

    this.submeta.frentesObra.forEach((frenteObra: FrenteObra) =>
      frenteObra.eventos.forEach((evento: Evento) => {
        if (evento.permiteMarcacao) {
          formControl[frenteObra.id + '-' + evento.id] = new FormControl(evento.indRealizado);

          // A exibição default do toggle do Convenente é Sim.
          // Caso a submeta esteja salva, o toggle ficará com a marcação informada.
          if (this.isEditConvenente() && this.submeta.situacaoConvenente === null) {
            formControl[frenteObra.id + '-' + evento.id].setValue(true);
            this.atualizarDadosSubmeta('Convenente', frenteObra.id, evento.id, true);
          }

          if (this.isEditConcedente() && this.submeta.situacaoConcedente === null) {
            formControl[frenteObra.id + '-' + evento.id].setValue(true);
            this.atualizarDadosSubmeta('Concedente', frenteObra.id, evento.id, true);
          }
        }
      }
    ));

    this.medicaoSubmetaForm = new FormGroup(formControl);
  }

  exibirToggle(coluna: string, evento: Evento): boolean {
    return this.permiteMarcacaoColuna(coluna) && evento.permiteMarcacao;
  }

  // Utiliza os atributos nrSeqMedicaoEmpresa, nrSeqMedicaoConvenente de evento.model
  exibirNrSequencial(coluna: string, evento: Evento): boolean {
    const nrSeqMedicao = 'nrSeqMedicao' + coluna;
    return !this.exibirToggle(coluna, evento) && evento[nrSeqMedicao] != null;
  }

  // Verifica se é o caso em que o evento permite marcação pelo perfil e foi marcado originalmente
  // pelo perfil em medição acumulada. Ocorre em medições acumuladoras na situação 'Em Complementação'
  // Nestes casos é exibido o ícone com o nr da medição em que o evento foi concluído
  // e marcações feitas nesse evento não são computadas no Total da Medição
  // Utiliza os atributos nrSeqMedicaoEmpresa, nrSeqMedicaoConvenente de evento.model
  existeMarcacaoMedicaoAcumuladaEPermiteMarcacao(coluna: string, evento: Evento): boolean {
    const nrSeqMedicao = 'nrSeqMedicao' + coluna;
    return this.exibirToggle(coluna, evento) && evento[nrSeqMedicao] != null && evento[nrSeqMedicao] < this.medicao.sequencial;
  }

  // Verifica se existe algum evento que foi marcado originalmente em medição
  // acumulada e foi desmarcado na medição acumuladora atual,
  // pelo próprio ator que informou
  // Pode ocorrer em medições acumuladoras na situação 'Em Complementação'
  existeAlteracaoEventoMedicaoAcumulada() : boolean {
    const coluna = this.getNomePerfilEdicao();
    let existeAlteracao = false;

    this.submeta.frentesObra.forEach((frenteObra: FrenteObra) => {
      frenteObra.eventos.forEach((evento: Evento) => {
        existeAlteracao = existeAlteracao ||
            (this.existeMarcacaoMedicaoAcumuladaEPermiteMarcacao(coluna, evento)
            && this.medicaoSubmetaForm.value[frenteObra.id + '-' + evento.id] === false);
      })
    });

    return existeAlteracao;
  }

  permiteMarcacaoColuna(coluna: string): boolean {
    return this.getNomePerfilEdicao() === coluna;
  }

  // Utiliza os atributos valorRealizadoEmpresa, valorRealizadoConvenente
  // valorRealizadoAcumuladoEmpresa, valorRealizadoAcumuladoConvenente de submeta.model
  atualizarDadosSubmeta(coluna: string, idFrenteObra: number, idEvento: number, status: boolean) {
    const campoValorRealizado = 'valorRealizado' + coluna;
    const campoValorRealizadoAcumulado = 'valorRealizadoAcumulado' + coluna;
    const evento = this.submeta.frentesObra.find(
      (frenteObra) => frenteObra.id === idFrenteObra).eventos.find(
        (ev) => { return ev.id === idEvento; });

    evento.indRealizado = status;

    // Atualiza os campos valorRealizado* e valorRealizadoAcumulado*
    // Não atualiza o valorRealizado* para os eventos que foram marcados em medições acumuladas,
    // pois o total da medição considera apenas os eventos marcados na medição atual
    if (this.existeMarcacaoMedicaoAcumuladaEPermiteMarcacao(coluna, evento)) {
      this.atualizarValor([campoValorRealizadoAcumulado], evento, status);
    } else {
      this.atualizarValor([campoValorRealizado, campoValorRealizadoAcumulado], evento, status);
    }
  }

  // Atualiza os campos informados com o valor do evento.
  // Soma se o status for true (toggle marcado), subtrai caso contrário (toggle desmarcado)
  atualizarValor(campos: string[], evento: Evento, status: boolean): void {
    campos.forEach((campo: string) => {
      if (status) {
        this.submeta[campo] += evento.valor;
      } else {
        this.submeta[campo] -= evento.valor;
      }
    });
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

  isFormDirty() {
    return this.medicaoSubmetaForm.dirty;
  }

  markFormAsPristine() {
    this.medicaoSubmetaForm.markAsPristine();
  }

  expandedChildren(show: boolean, indiceElementoSelecionado: number) {
    const posicao = this.obterPosicaoFilhos (indiceElementoSelecionado);

    // O limite é a posição mais o offset
    const limite = posicao.start + posicao.offset;

    for (let posicaoElemento = posicao.start; posicaoElemento < limite ; posicaoElemento++) {
      // Busca o elemento rowToggle que deve alterado
      const toggleSelecionado = this.rowToggleQuery.find ((toggle , indice) => {
        return posicaoElemento === indice;
      } );

      // Aplica o comportamento (expandir ou retrair) ao elemento encontrado se houver.
      if (toggleSelecionado) {
        toggleSelecionado.show = show;
      }
    }
  }

  /**
   * Obtêm a posição da Frente Obra clicada e também o offset referente aos Eventos filhos da frente de Obra
   *
   * @param indiceElementoSelecionado
   *
   */
  obterPosicaoFilhos(indiceElementoSelecionado: number) {

    // Identifica a Frente de Obra foi selecionada
    const frenteObraSelecionada = this.submeta.frentesObra.find ((fo , indice) => {
      return indice === indiceElementoSelecionado;
    } );

    let contador = 0;

    /**
     *  Identifica quantos eventos estão antes da Frente de Obra selecionada.
     *  Ex.: {FO:0,eventos:[ev0, ev1, ev2] , FO:1,eventos:[ev3, ev4]}
     *  Caso a Frente de Obra(FO)=1 seja selecionada, então o retorno será pos: 3 e offset: 2 =>
     *  Significa que a soma dos eventos de todas as Frente de Obras anteriores a
     *  Frente de Obra que foi selecionada é 3, e o offset é o tamanho do array de Eventos da Frente de Obra clicada
     */
    for (const fo of this.submeta.frentesObra) {
      if (fo.id !== frenteObraSelecionada.id) {
        contador += fo.eventos.length;
      } else {
        break;
      }
    }

    return {'start': contador , 'offset' : frenteObraSelecionada.eventos.length};
  }

  isEditEmpresa(): boolean {
    return this.acao === 'editar' && this.canAccess('preencherSubmetaEmpresa');
  }

  isEditConvenente(): boolean {
    return this.acao === 'editar' && this.canAccess('preencherSubmetaConvenente');
  }

  isEditConcedente(): boolean {
    return this.acao === 'editar' && this.canAccess('preencherSubmetaConcedenteMandataria');
  }
}
