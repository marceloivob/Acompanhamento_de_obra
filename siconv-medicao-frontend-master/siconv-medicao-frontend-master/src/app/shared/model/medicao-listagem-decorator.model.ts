import { Medicao } from './medicao.model';

export class MedicaoListagemDecorator {

  constructor(
      public medicao?: Medicao,
      public showDetail?: boolean,
      public filhas?: MedicaoListagemDecorator[]) {
        this.filhas = [];
        this.showDetail = true;
      }

}
