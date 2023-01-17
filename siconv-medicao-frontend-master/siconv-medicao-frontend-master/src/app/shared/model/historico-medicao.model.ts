import { PerfilResponsavel } from './perfil.model';
export class HistoricoMedicao {

    constructor(
        public nrCpfResponsavel?: string,
        public nrSequencial?: number,
        public dataHora?: Date,
        public nomeResponsavel?: string,
        public inPerfilResponsavel?: PerfilResponsavel,
        public inSituacao?: SituacaoMedicao) { }

}

class SituacaoMedicao {

  constructor (
    public codigo?: string,
    public descricao?: string) {}
}
