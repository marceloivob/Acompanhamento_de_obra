import { Servico } from './servico.model';

export class Macrosservico {

    constructor(
        public id?: number,
        public descricao?: string,
        public servicos?: Servico[],
        public numero?: number
    ) { }

}
