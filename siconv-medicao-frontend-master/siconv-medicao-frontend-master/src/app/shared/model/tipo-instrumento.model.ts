export class TipoInstrumento {
  constructor(
    public numeroConvenioRepasse?: number,
    public anoConvenioRepasse?: number,
    public localidade?: string,
    public nomeObjetoContratoRepasse?: string,
    public urlSiconvMedicao?: string,
    public modalidade?: Modalidade,
    public nomeConvenente?: string
  ) {}

  get descricaoModalidade() {
    return this.modalidade ? this.modalidade.descricao : '';
  }
}

export class Modalidade {
  constructor(public codigo?: number, public descricao?: string, public possuiInstituicaoMandataria?: boolean) {}
}
