import { ContratoResponsavelTecnico } from './contrato-responsavel-tecnico.model';

export class RegistroProfissional {

    constructor(public id?: number,
                public atividade?: string,
                public nrCreaCau?: string,
                public uf?: string,
                public contratos?: ContratoResponsavelTecnico[],
                public versao?: number,
                public isRPAssociadoOutroCTEF ?: boolean) {}


    obterContratoVinculado(contratoFk: number): ContratoResponsavelTecnico {
        const contratoSelecionado = this.contratos.filter((contrato) => {
            let retorno = false;
            let contratoRt = Object.assign(new ContratoResponsavelTecnico(), contrato);

            retorno = contratoRt.contratoFk == contratoFk;

            return retorno;
        });

        return contratoSelecionado[0];
    }
}
