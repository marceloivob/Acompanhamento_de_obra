export class ContratoLote {
  constructor(
    public id?: number,
    public tipo?: string,
    public numero?: string,
    public aptoIniciar?: boolean,
    public acompEventos?: boolean,
    public configuradoMedicao?: boolean,
    public numeroUltimaMedicao?: number,
    public qtdeDiasSemMedicao?: number,
    public atrasado?: boolean,
    public paralisado?: boolean,
    public submetas?: SubmetaContratoLote[]
  ) {}
}

export class SubmetaContratoLote {
  constructor(
    public id?: number,
    public numero?: string,
    public descricao?: string,
    public situacao?: string,
    public regimeExecucao?: string,

    public valorSubmeta?: number,

    public valorRealizadoEmpresa?: number,
    public percentualRealizadoEmpresa?: number,

    public valorRealizadoConvenente?: number,
    public percentualRealizadoConvenente?: number,

    public valorRealizadoConcedente?: number,
    public percentualRealizadoConcedente?: number,

    public valorRealizadoAcumuladoEmpresa?: number,
    public percentualRealizadoAcumuladoEmpresa?: number,

    public valorRealizadoAcumuladoConvenente?: number,
    public percentualRealizadoAcumuladoConvenente?: number,

    public valorRealizadoAcumuladoConcedente?: number,
    public percentualRealizadoAcumuladoConcedente?: number
  ) {}
}
