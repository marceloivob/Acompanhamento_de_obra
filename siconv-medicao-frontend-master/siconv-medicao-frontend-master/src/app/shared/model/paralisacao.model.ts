import moment from 'moment';

export class Paralisacao {
  constructor(
    public id?: number,
    public dataInicio?: Date,
    public dataFim?: Date,
    public responsavel = new ResponsavelParalisacao(),
    public indicativo = new IndicativoParalisacao(),
    public motivo = new MotivoParalisacao(),
    public observacao?: string,
    public anexos: AnexoParalisacao[] = [],
    public idContratoSiconv?: number,
    public versao?: number,
    public showDetail?: boolean,
    public permiteManutencao?: boolean
  ) {
  }

  get periodo(): string {
    return moment(this.dataInicio).format('DD/MM/YYYY') + 
      (this.dataFim != null ? ' a ' + moment(this.dataFim).format('DD/MM/YYYY') : '');
  }

  get descricaoResponsavel() {
    return this.responsavel.descricao;
  }

  get descricaoIndicativo() {
    return this.indicativo.descricao;
  }

  get descricaoMotivo() {
    return this.motivo.descricao;
  }

  get anexosExpandidos(): AnexoParalisacao[] {
    if (!this.anexos || this.anexos.length === 0) return null;
    return this.showDetail ? this.anexos : this.anexos.slice(0, 1);
  }
}

export class ResponsavelParalisacao {

  static readonly LISTA_DOMINIO = [
    new ResponsavelParalisacao('EMP', 'Empresa'),
    new ResponsavelParalisacao('CVE', 'Convenente'),
    new ResponsavelParalisacao('CCE', 'Concedente'),
    new ResponsavelParalisacao('MAN', 'Mandatária'),
    new ResponsavelParalisacao('ORG', 'Órgão de controle'),
    new ResponsavelParalisacao('JUD', 'Judiciário'),
    new ResponsavelParalisacao('OUT', 'Outros')
  ];

  constructor (
    public codigo?: string,
    public descricao?: string) {}
}

export class IndicativoParalisacao {

  static readonly LISTA_DOMINIO = [
    new IndicativoParalisacao(1, 'Decisão judicial ou de órgão de controle interno ou externo'),
    new IndicativoParalisacao(2, 'Declaração de empresa executora'),
    new IndicativoParalisacao(3, 'Declaração de órgão ou entidade da administração pública federal'),
    new IndicativoParalisacao(4, 'Sem apresentação de boletim de medição por período igual ou superior a 90 dias'),
    new IndicativoParalisacao(5, 'Outros')
  ];

  constructor (
    public codigo?: number,
    public descricao?: string) {}
}

export class MotivoParalisacao {

  static readonly LISTA_DOMINIO = [
    new MotivoParalisacao(1, 'Ação judicial'),
    new MotivoParalisacao(2, 'Alto reajuste dos valores de material'),
    new MotivoParalisacao(3, 'Ausência de recursos orçamentário/financeiro'),
    new MotivoParalisacao(4, 'Baixa governança sobre o objeto/localidade de recurso de emenda parlamentar'),
    new MotivoParalisacao(5, 'Carência no mercado local de materiais'),
    new MotivoParalisacao(6, 'Constantes necessidade de realinhamentos de preços'),
    new MotivoParalisacao(7, 'Demora na liberação de recursos pela união, acarretando aumento no valor do bem e desistência do fornecedor'),
    new MotivoParalisacao(8, 'Desistência ou abandono pela empresa com justificativa'),
    new MotivoParalisacao(9, 'Desistência ou abandono pela empresa sem justificativa'),
    new MotivoParalisacao(10, 'Desvio de finalidade (do objeto)'),
    new MotivoParalisacao(11, 'Dificuldades técnicas da organização executora'),
    new MotivoParalisacao(12, 'Excesso de burocracia (mandatária)'),
    new MotivoParalisacao(13, 'Execução em desacordo com o projeto'),
    new MotivoParalisacao(14, 'Falta de recursos de contrapartida'),
    new MotivoParalisacao(15, 'Falta de titularidade e/ou desapropriação'),
    new MotivoParalisacao(16, 'Falta equipe técnica nos municípios para operacionalizar os instrumentos'),
    new MotivoParalisacao(17, 'Inadimplência da empresa executora'),
    new MotivoParalisacao(18, 'Localização geográfica de difícil acesso para entrega de materiais e serviços'),
    new MotivoParalisacao(19, 'Não obtenção de licenças, autorizações ou outros instrumentos equivalentes'),
    new MotivoParalisacao(20, 'Índices pluviométricos elevados em decorrência de chuvas'),
    new MotivoParalisacao(21, 'Perda de prazo / prorrogação de vigência'),
    new MotivoParalisacao(22, 'Problemas na garantia contratual'),
    new MotivoParalisacao(23, 'Problemas técnicos de execução'),
    new MotivoParalisacao(24, 'Projeto mal elaborado com impacto no licitatório'),
    new MotivoParalisacao(25, 'Rescisão contratual'),
    new MotivoParalisacao(26, 'Revisão de projeto básico'),
    new MotivoParalisacao(27, 'Revisão de projeto executivo')
  ];

  constructor (
    public codigo?: number,
    public descricao?: string) {}
}

export class AnexoParalisacao {

  constructor(
    public id?: number,
    public nmArquivo?: string,
    public url?: string,
    public arquivo?: any,
    public paralisacaoFk?: number
  ) {}
}
