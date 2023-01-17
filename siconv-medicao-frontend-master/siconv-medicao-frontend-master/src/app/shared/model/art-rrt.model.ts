import { Submeta } from 'src/app/shared/model/submeta.model';
import { ResponsavelTecnico } from './responsavel-tecnico.model';

export class ArtRrt {

    constructor(public id?: number,
                public tipo?: string,
                public numero?: string,
                public dataEmissao?: Date,
                public responsavelTecnico?: ResponsavelTecnico,
                public submetas?: Submeta[],
                public dataInativacao?: Date,
                public nmArquivo?: string,
                public coCeph?: string,
                public arquivo?: any,
                public url?: string,
                public idMedContratoRespTec?: number,
                public showDetail: boolean  = true,
                public possuiSubmetaAssinada?: boolean,
                public idContratoSiconv?: number,
                public versao?: number) {}

    get listasubmetas(): string {
      return this.submetas.length > 0 ? this.submetas
        .map(sub => sub.nrSubmetaAnalise + ' - ' + sub.descricao)
        .reduce((prev, cur) => prev + '\n' + cur) : '';
    }

    get nomeResponsavelTecnico() {
        return this.responsavelTecnico.nome;
    }
}
