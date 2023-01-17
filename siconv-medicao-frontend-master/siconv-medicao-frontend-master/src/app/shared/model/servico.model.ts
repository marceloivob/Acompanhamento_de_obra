export class Servico {

    constructor(
        public id?: number,
        public qtdInformada?: number,
        public descricao?: string,
        public preco?: number,
        public qtd?: number,
        public sgUnidade?: string,
        public vlTotalServico?: number,
        public qtdRealizadoEmpresa?: number,
        public valorRealizadoEmpresa?: number,
        public qtdAcumuladoEmpresa?: number,
        public valorAcumuladoEmpresa?: number,
        public qtdRealizadoConvenente?: number,
        public valorRealizadoConvenente?: number,
        public qtdAcumuladoConvenente?: number,
        public valorAcumuladoConvenente?: number,
        public qtdRealizadoConcedente?: number,
        public valorRealizadoConcedente?: number,
        public qtdAcumuladoConcedente?: number,
        public valorAcumuladoConcedente?: number,
        public permiteMedicao?: boolean,
        public qtdMaxPermitido?: number,
        public qdtRealizadoOriginal?: number,
        public qtdAcumuladoOriginal?: number,
        public possuiGlosasAnterioresConvenente?: boolean,
        public numero?: number,
        public valoresPorIdMedicao?: Map< number, ValorServicoBM>,
        public possuiGlosasAnterioresConcedente?: boolean,
    ) {}

    get possuiGlosaConvenente(): boolean {
      return this.qtdAcumuladoConvenente < this.qtdAcumuladoEmpresa;
    }

    get possuiGlosaConcedente(): boolean {
      return this.qtdAcumuladoConcedente < this.qtdAcumuladoConvenente;
    }
  }
    export class ValorServicoBM {
        constructor(
            public qtdEmpresa?:number,
            public qtdConvenente?:number,
            public qtdConcedente?:number
        ){}
    }
