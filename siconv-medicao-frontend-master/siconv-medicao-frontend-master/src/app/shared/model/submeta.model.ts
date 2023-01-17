import { FrenteObra } from './frente-obra.model';
import { Assinatura } from './assinatura.model';

export class Submeta {

  constructor(
      public id?: number,
      public descricao?: string,
      public valor?: number,
      public indice?: number,

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

      public situacaoEmpresa?: Situacao,
      public situacaoConvenente?: Situacao,
      public situacaoConcedente?: Situacao,
      public frentesObra?: FrenteObra[],
      public assinaturas?: Assinatura[],
      public nrSubmetaAnalise?: string,
      public versao?: number,
      public permiteMarcacaoEmpresa?: boolean,
      public permiteMarcacaoConvenente?: boolean,
      public permiteMarcacaoConcedente?: boolean,

      public idContratoSiconv?: number) {}
}
class Situacao{
  constructor(
    public codigo?: string,
    public descricao?: string
  ) {}
}

