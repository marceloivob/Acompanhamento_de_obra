import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'solicitanteVistoriaExtra'
})
export class SolicitanteVistoriaExtraPipe implements PipeTransform {
  transform(solicitante: any[]): string[] {
    return solicitante.map(tipo => tipo.descricao);
}}
