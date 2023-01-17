import { RegistroProfissional } from './registro-profissional.model';

export class ResponsavelTecnico {

    constructor(public id?: number,
        public cpf?: string,
        public nome?: string,
        public email?: string,
        public registrosProfissional?: RegistroProfissional[],
        public telefone?: string,
        public contratoFk?: number,
        public versao?: number,
        public idContratoSiconv?: number) { }


    get atividade() {
        const registro = this.obterRegistroVinculado(this.contratoFk);
        return registro.atividade;
    }

    get creacau() {
        const registro = this.obterRegistroVinculado(this.contratoFk);
        if (registro.uf) {
            return registro.nrCreaCau + '/' + registro.uf;
        }
        return registro.nrCreaCau;
    }

    get tipoContrato() {
        const registro = this.obterRegistroVinculado(this.contratoFk);
        let registroProfissional = Object.assign(new RegistroProfissional(), registro);
        const contrato = registroProfissional.obterContratoVinculado(this.contratoFk);
        return contrato.descricaoTipo;
    }

    get tipo() {
        const registro = this.obterRegistroVinculado(this.contratoFk);
        let registroProfissional = Object.assign(new RegistroProfissional(), registro);
        const contrato = registroProfissional.obterContratoVinculado(this.contratoFk);
        return contrato.tipo;
    }

    get dataInclusao() {
        const registro = this.obterRegistroVinculado(this.contratoFk);
        let registroProfissional = Object.assign(new RegistroProfissional(), registro);
        const contrato = registroProfissional.obterContratoVinculado(this.contratoFk);
        return contrato.dataInclusao;
    }

    get possuiART() {
      const registro = this.obterRegistroVinculado(this.contratoFk);
      const registroProfissional = Object.assign(new RegistroProfissional(), registro);
      const contrato = registroProfissional.obterContratoVinculado(this.contratoFk);
      return contrato.possuiART;
    }

    get possuiARTAtiva() {
        const registro = this.obterRegistroVinculado(this.contratoFk);
        const registroProfissional = Object.assign(new RegistroProfissional(), registro);
        const contrato = registroProfissional.obterContratoVinculado(this.contratoFk);
        return contrato.possuiARTAtiva;
    }

    get possuiSubmetaAssinada() {
      const registro = this.obterRegistroVinculado(this.contratoFk);
      const registroProfissional = Object.assign(new RegistroProfissional(), registro);
      const contrato = registroProfissional.obterContratoVinculado(this.contratoFk);
      return contrato.possuiSubmetaAssinada;
    }

    get idMedContratoRT() {
      const registro = this.obterRegistroVinculado(this.contratoFk);
      const registroProfissional = Object.assign(new RegistroProfissional(), registro);
      const contrato = registroProfissional.obterContratoVinculado(this.contratoFk);
      return contrato.id;
    }

    obterRegistroVinculado(contratoFk: number): RegistroProfissional {
        const registroSelecionado = this.registrosProfissional.filter((registro) => {
            let retorno = false;
            const registroProfissional = Object.assign(new RegistroProfissional(), registro);

            if (registroProfissional.contratos) {
                const contrato = registroProfissional.obterContratoVinculado(contratoFk);
                if (contrato) {
                    retorno = contrato.contratoFk === contratoFk;
                }
            }
            return retorno;
        });
        return registroSelecionado[0];
    }
}
