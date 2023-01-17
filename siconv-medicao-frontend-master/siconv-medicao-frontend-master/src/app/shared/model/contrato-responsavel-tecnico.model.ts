export class ContratoResponsavelTecnico {

  constructor(public id?: number,
    public contratoFk?: number,
    public tipo?: any,
    public descricaoTipo?: string,
    public dataInclusao?: Date,
    public versao?: number,
    public possuiART?: boolean,
    public possuiARTAtiva?: boolean,
    public possuiSubmetaAssinada?: boolean) { }
}
