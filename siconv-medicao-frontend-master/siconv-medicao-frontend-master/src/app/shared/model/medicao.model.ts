export class Medicao {

    constructor(
        public id?: number,
        public versao?: number,
        public sequencial?: number,
        public dataInicioObra?: Date,
        public dataInicio?: Date,
        public dataFim?: Date,
        public bloqueada?: boolean,

        // Empresa
        public valorRealizadoEmpresa?: number,
        public percentualRealizadoEmpresa?: number,
        public valorRealizadoAcumuladoEmpresa?: number,
        public percentualRealizadoAcumuladoEmpresa?: number,
        // Convenente
        public valorRealizadoConvenente?: number,
        public percentualRealizadoConvenente?: number,
        public valorRealizadoAcumuladoConvenente?: number,
        public percentualRealizadoAcumuladoConvenente?: number,
        // Concedente
        public valorRealizadoConcedente?: number,
        public percentualRealizadoConcedente?: number,
        public valorRealizadoAcumuladoConcedente?: number,
        public percentualRealizadoAcumuladoConcedente?: number,

        public possuiSubmetaAssinada?: boolean,
        public permiteCancelarEnvio?: boolean,
        public permiteCancelarEnvioParaComplementacao?: boolean,
        public permiteIniciarAteste?: boolean,
        public permiteCancelarAceite?: boolean,
        public permiteComplementacaoValor?: boolean,
        public permiteExcluir?: boolean,
        public situacao?: SituacaoMedicao,
        public idContratoSiconv?: number,
        public idMedicaoAgrupadora?: number,
        public sequencialMedicaoAgrupadora?: number,
        public isMedicaoAgrupadora?: boolean,

        // Vistoria Extra
        public vistoriaExtra?: boolean,
        public dataVistoriaExtra?: Date,
        public solicitanteVistoriaExtra?: SolicitanteVistoriaExtra
        ) {
        }

}

class SituacaoMedicao {

  constructor (
    public codigo?: string,
    public descricao?: string) {}
}

class SolicitanteVistoriaExtra {

  constructor (
    public codigo?: string,
    public descricao?: string) {}
}
