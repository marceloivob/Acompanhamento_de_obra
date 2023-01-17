import { SeverityEnum } from './../../shared/enum/severity.enum';
import { formatCurrency, formatNumber } from '@angular/common';
import { Component, Inject, LOCALE_ID, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AcompanhamentoObra } from 'src/app/shared/model/acompanhamento-obra.model';
import { Contrato } from 'src/app/shared/model/contrato.model';
import { ContratoLote, SubmetaContratoLote } from 'src/app/shared/model/contrato-lote.model';
import { DataExport } from 'src/app/shared/model/data-export';
import { ContratoService } from 'src/app/shared/services/contrato.service';
import { SharedService } from 'src/app/shared/services/shared-service.service';
import { AcompanhamentoConvenenteService } from '../../shared/services/acompanhamento-convenente.service';
import { Profile } from './../../shared/model/security/profile.enum';
import { UsuarioLogadoService } from './../../shared/services/usuario-logado.service';

@Component({
  selector: 'app-dados-gerais-acompanhamento',
  templateUrl: './dados-gerais-acompanhamento.component.html',
  styleUrls: ['./dados-gerais-acompanhamento.component.scss'],
})
export class DadosGeraisAcompanhamentoComponent implements OnInit {
  idProposta: number;
  acompObra: AcompanhamentoObra;
  lista: ContratoLote[] = [];
  export: DataExport;
  fileExportName = 'acompanhamentoObras';

  constructor(
    private _route: ActivatedRoute,
    private _router: Router,
    private _sharedService: SharedService,
    private _contratoService: ContratoService,
    private _acompanhamentoService: AcompanhamentoConvenenteService,
    private _usuarioLogadoService: UsuarioLogadoService,
    @Inject(LOCALE_ID) private locale: string
  ) { }

  ngOnInit() {
    let erro = false;

    this.idProposta = this._route.snapshot.params['idProposta'];

    this._acompanhamentoService.consultarContratosLotes(this.idProposta).subscribe((value) => {
      this.acompObra = value;
      this.prepararDadosExportacao();
      this.emitirTitulo();
    }, error => {
      error.error.data.errors.map((item) => {
        if (!item.severity || item.severity != SeverityEnum.CRITICAL) {
          this.emitirTitulo();
        }
      });
    });
  }

  emitirTitulo() {
    const tituloTela = {
      titulo: 'Dados Gerais',
      subtitulo: 'Acompanhamento de Obras',
      info: '',
    };

    this._sharedService.emitChange(tituloTela);
  }

  prepararDadosExportacao() {
    const columns = [
      'Tipo',
      'Medicao',
      'QuantidadeDiasSemMedicao',
      'Submeta',
      'SituacaoSubmeta',
      'RegimeExecucao',
      'ValorTotalSubmeta',
      'ValorRealizadoPeriodoEmpresa',
      'PercentualRealizadoPeriodoEmpresa',
      'ValorRealizadoAcumuladoEmpresa',
      'PercentualRealizadoAcumuladoEmpresa',
      'ValorRealizadoPeriodoConvenente',
      'PercentualRealizadoPeriodoConvenente',
      'ValorRealizadoAcumuladoConvenente',
      'PercentualRealizadoAcumuladoConvenente',
      'ValorRealizadoPeriodoConcedente',
      'PercentualRealizadoPeriodoConcedente',
      'ValorRealizadoAcumuladoConcedente',
      'PercentualRealizadoAcumuladoConcedente',
    ];

    const data = [];

    this.acompObra.contratosLotes.forEach((contratoLote: ContratoLote) => {
      contratoLote.submetas.forEach((submeta: SubmetaContratoLote) => {
        const linha = [];

        linha.push((contratoLote.tipo === 'C' ? 'CTEF ' : 'Lote ') + contratoLote.numero);
        linha.push(contratoLote.numeroUltimaMedicao ? 'Medição ' + contratoLote.numeroUltimaMedicao : '');
        linha.push(contratoLote.qtdeDiasSemMedicao || '');

        linha.push(submeta.numero + ' - ' + submeta.descricao);
        linha.push(submeta.situacao);
        linha.push(submeta.regimeExecucao);
        linha.push(this.formatarValor(submeta.valorSubmeta));

        // Empresa
        linha.push(this.formatarValor(submeta.valorRealizadoEmpresa));
        linha.push(this.formatarPercentual(submeta.percentualRealizadoEmpresa));

        linha.push(this.formatarValor(submeta.valorRealizadoAcumuladoEmpresa));
        linha.push(this.formatarPercentual(submeta.percentualRealizadoAcumuladoEmpresa));

        // Convenente
        linha.push(this.formatarValor(submeta.valorRealizadoConvenente));
        linha.push(this.formatarPercentual(submeta.percentualRealizadoConvenente));

        linha.push(this.formatarValor(submeta.valorRealizadoAcumuladoConvenente));
        linha.push(this.formatarPercentual(submeta.percentualRealizadoAcumuladoConvenente));

        // Concedente
        linha.push(this.formatarValor(submeta.valorRealizadoConcedente));
        linha.push(this.formatarPercentual(submeta.percentualRealizadoConcedente));

        linha.push(this.formatarValor(submeta.valorRealizadoAcumuladoConcedente));
        linha.push(this.formatarPercentual(submeta.percentualRealizadoAcumuladoConcedente));

        data.push(linha);
      });
    });

    this.export = new DataExport(columns, data);
  }

  onPageChange(listaPaginada) {
    this.lista = listaPaginada;
  }

  exibirConfiguracaoCtef(idContrato: number) {
    this._contratoService.consultarContrato(idContrato).subscribe((contrato: Contrato) => {
      if (!contrato.inSocial) {
        this._router.navigate([`/acompanhamento/proposta/${this.idProposta}/contrato/${contrato.id}/config/rt`]);
      } else {
        this._router.navigate([`/acompanhamento/proposta/${this.idProposta}/contrato/${contrato.id}/config/rtsocial`]);
      }
    });
  }

  exibirListagemMedicoes(idContrato: string) {
    this._router.navigate([`/acompanhamento/proposta/${this.idProposta}/contrato/${idContrato}/medicao/`]);
  }

  permiteConfiguracao(contratoLote: ContratoLote): boolean {
    return contratoLote.tipo === 'C' && contratoLote.aptoIniciar;
  }

  getHintConfiguracao(contratoLote: ContratoLote): string {
    let retorno = '';

    if (contratoLote.tipo === 'C') {
      if (contratoLote.aptoIniciar) {
        retorno = this.capitalizeFirstLetter(this.getAcaoConfiguracaoMsgPorPerfil()) + ' Dados do Contrato';
      } else {
        retorno = 'Não é possível ' + this.getAcaoConfiguracaoMsgPorPerfil() +
          ' dados do contrato, pois ainda não foi emitida a autorização de início do objeto.';
      }
    } else {
      retorno = 'Não é possível ' + this.getAcaoConfiguracaoMsgPorPerfil() +
        ' dados, pois este Lote ainda não foi formalizado como contrato.';
    }

    return retorno;
  }

  private getAcaoConfiguracaoMsgPorPerfil(): string {
    if (this._usuarioLogadoService.usuarioLogado.profile === Profile.PROPONENTE) {
      return 'atualizar';
    } else {
      return 'detalhar';
    }
  }

  private getAcaoAcompanhamentoMedicaoMsgPorPerfil(): string {
    if (this._usuarioLogadoService.usuarioLogado.profile === Profile.PROPONENTE ||
      this._usuarioLogadoService.usuarioLogado.profile === Profile.CONCEDENTE ||
      this._usuarioLogadoService.usuarioLogado.profile === Profile.MANDATARIA) {
      return 'realizar';
    } else {
      return 'detalhar';
    }
  }

  private capitalizeFirstLetter(palavra: string): string {
    return palavra.charAt(0).toUpperCase() + palavra.slice(1);
  }

  permiteAcompanhamentoMedicao(contratoLote: ContratoLote): boolean {
    return contratoLote.configuradoMedicao;
  }

  getHintAcompanhamentoMedicao(contratoLote: ContratoLote): string {
    let retorno = '';

    if (contratoLote.tipo === 'C') {
      if (contratoLote.aptoIniciar) {
        if (contratoLote.configuradoMedicao) {
          retorno = this.capitalizeFirstLetter(this.getAcaoAcompanhamentoMedicaoMsgPorPerfil()) + ' Acompanhamento';
        } else {
          retorno = 'Não é possível ' + this.getAcaoAcompanhamentoMedicaoMsgPorPerfil() +
            ' acompanhamento pois os dados do contrato não foram atualizados pelo convenente.';
        }
      } else {
        retorno =
          'Não é possível ' + this.getAcaoAcompanhamentoMedicaoMsgPorPerfil() +
          ' o acompanhamento, pois ainda não foi emitida a autorização de início do objeto.';
      }
    } else {
      retorno = 'Não é possível ' + this.getAcaoAcompanhamentoMedicaoMsgPorPerfil() +
        ' o acompanhamento, pois este Lote ainda não foi formalizado como contrato.';
    }

    return retorno;
  }

  private formatarValor(valor: number): string {
    return valor ? formatCurrency(valor, this.locale, '') : '';
  }

  private formatarPercentual(percentual: number): string {
    return percentual ? formatNumber(percentual, this.locale, '1.2-2') : '';
  }
}
