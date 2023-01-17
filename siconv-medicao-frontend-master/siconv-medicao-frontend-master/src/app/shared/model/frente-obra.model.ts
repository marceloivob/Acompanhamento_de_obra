import { Evento } from './evento.model';
import { Macrosservico } from './macrosservico.model';
import { Servico } from './servico.model';

export class FrenteObra {

    constructor(
        public id?: number,
        public eventos?: Evento[],
        public servicos?: Servico[],
        public macroServicosView?: Macrosservico[],
        public descricao?: string) { }
}
