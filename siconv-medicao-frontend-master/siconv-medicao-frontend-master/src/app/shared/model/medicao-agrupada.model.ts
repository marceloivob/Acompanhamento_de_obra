import { Submeta } from "./submeta.model";

export class MedicaoAgrupada {

  constructor(
      public sequencial?: number,
      public id?: number,
      public dataInicio?: Date,
      public dataFim?: Date,
      public listaSubmetasPreenchidas?: Submeta[]
  ) {
  }


  static sortRevert(medA: MedicaoAgrupada, medB: MedicaoAgrupada): number {

    if (medA.sequencial > medB.sequencial) return -1;

    if (medA.sequencial < medB.sequencial) return 1;

    return 0;

  }

  static sort(medA: MedicaoAgrupada, medB: MedicaoAgrupada): number {

    if (medA.sequencial > medB.sequencial) return 1;

    if (medA.sequencial < medB.sequencial) return -1;

    return 0;

  }

}
