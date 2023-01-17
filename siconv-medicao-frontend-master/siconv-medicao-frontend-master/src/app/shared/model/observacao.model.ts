import { Anexo } from './anexo.modelo';
import { PerfilResponsavel } from './perfil.model';

export class Observacao {
  constructor(public id?: number,
    public versao?: number,
    public dtRegistro?: Date,
    public inPerfilResponsavel?: PerfilResponsavel,
    public nrCpfResponsavel?: string,
    public txObservacao?: string,
    public medicaoFk?: number,
    public anexos?: Anexo[],
    public showDetail?: boolean,
    public nomeResponsavel?: string,
    public idContratoSiconv?: number,
    public sequencialMedicaoAgrupada?: number,
    public inBloqueio?: boolean) { }

  get listaAnexos() {
    return this.anexos.reduce((prevVal, elem) =>
      prevVal + elem.nmArquivo + '\n', '');
  }

  get descricao(): string {
    return this.inPerfilResponsavel.descricao;
  }

}

