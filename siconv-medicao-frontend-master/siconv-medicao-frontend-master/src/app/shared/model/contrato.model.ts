
import { Modalidade } from './tipo-instrumento.model';

export class Contrato {

    constructor(public id?: number,
                public numeroContrato?: string,
                public valorContrato?: number,
                public qtdeMedicoes?: number,
                public anoConvenioRepasse?: number,
                public numeroConvenioRepasse?: number,
                public localidade?: string,
                public nomeObjetoContratoFornecimento?: string,
                public nomeObjetoContratoRepasse?: string,
                public dtAssinatura?: Date,
                public fornecedorId?: number,
                public modalidade?: Modalidade,
                public dtFimVigencia?: Date,
                public urlSiconvMedicao?: string,
                public inSocial?: boolean,
                public propostaFk?: number,
                public isConfiguradoParaMedicao?: boolean,
                public valorTipoInstrumento?: number,
                public qtdeDiasSemMedicao?: number,
                public inContratoAtrasado?: boolean,
                public nomeConvenente?: string,
                public inAcompEvento?: boolean,
                public inContratoParalisado?: boolean) { }

}
