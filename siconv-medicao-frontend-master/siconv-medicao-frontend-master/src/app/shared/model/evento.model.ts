import { Servico } from './servico.model';

export class Evento {

    constructor(
        public id?: number,
        public indRealizado?: boolean,
        public descricao?: string,
        public valor?: number,
        public nrSeqMedicaoEmpresa?: number,
        public nrSeqMedicaoConvenente?: number,
        public nrSeqMedicaoConcedente?: number,
        public servicos?: Servico[],
        public showServicos?: boolean,
        public permiteMarcacao?: boolean) {
            showServicos = false;
    }
}
