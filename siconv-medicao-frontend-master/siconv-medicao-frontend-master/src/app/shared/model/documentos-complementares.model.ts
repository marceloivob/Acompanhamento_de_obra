import { Submeta } from './submeta.model';

export class DocumentosComplementares {
  constructor(
    public id?: number,
    public tipoDocumento?: TipoDocumento,
    public txDescricao?: string,
    public nrDocumento?: string,
    public tipoManifestoAmbiental?: TipoManifestoAmbiental,
    public nmOrgaoEmissor?: string,
    public dtEmissao?: Date,
    public dtValidade?: Date,
    public possuiMedicao?: boolean,
    public showDetail: boolean = true,
    public submetas?: Submeta[],
    public nmArquivo?: string,
    public coCeph?: string,
    public bloqueado?: boolean,
    public arquivo?: any,
    public url?: string,
    public idContratoSiconv?: number,
    public versao?: number,
    public txDescricaoOutros?: string,
    public eqLicencaInstalacao?: boolean) {
    }

    get _descricaoTipoDocumento(): string {
      return this.tipoDocumento.descricao;
    }

    get _descricaoManifestoAmbiental(): string {
      if (this.tipoManifestoAmbiental) {
        return this.tipoManifestoAmbiental.descricao;
      } else {
        return '';
      }

    }

    get _submetas(): string {
      return this.submetas.length > 0 ? this.submetas
        .map(sub => sub.nrSubmetaAnalise + ' - ' + sub.descricao)
        .reduce((prev, cur) => prev + '\n' + cur) : '';
    }

}

export class TipoDocumento {
  constructor(
    public codigo?: string,
    public descricao?: string
) {}
}

export class TipoManifestoAmbiental {
  constructor(
    public codigo?: string,
    public descricao?: string
) {}

}

