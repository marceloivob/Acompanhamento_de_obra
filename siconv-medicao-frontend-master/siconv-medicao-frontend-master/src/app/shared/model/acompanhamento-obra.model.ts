import { ContratoLote } from './contrato-lote.model';
import { TipoInstrumento } from './tipo-instrumento.model';

export class AcompanhamentoObra {
  constructor(
    public tipoInstrumento?: TipoInstrumento,
    public contratosLotes?: ContratoLote[],
    public valorTotalSubmetas?: number,
    public valorTotalEmpresa?: number,
    public valorTotalConvenente?: number,
    public valorTotalConcedente?: number,
    public percentualTotalEmpresa?: number,
    public percentualTotalConvenente?: number,
    public percentualTotalConcedente?: number
  ) {}
}
