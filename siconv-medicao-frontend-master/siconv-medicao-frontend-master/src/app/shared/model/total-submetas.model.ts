export class TotalSubmetas {

  constructor(
      public valorSubmeta?: number,
       // Empresa
       public valorRealizadoEmpresa?: number,
       public percentualRealizadoEmpresa?: number,
       public valorRealizadoAcumuladoEmpresa?: number,
       public percentualRealizadoAcumuladoEmpresa?: number,
       //Convenente
       public valorRealizadoConvenente?: number,
       public percentualRealizadoConvenente?: number,
       public valorRealizadoAcumuladoConvenente?: number,
       public percentualRealizadoAcumuladoConvenente?: number,
       //Concedente
       public valorRealizadoConcedente?: number,
       public percentualRealizadoConcedente?: number,
       public valorRealizadoAcumuladoConcedente?: number,
       public percentualRealizadoAcumuladoConcedente?: number,
      ) { }

}
