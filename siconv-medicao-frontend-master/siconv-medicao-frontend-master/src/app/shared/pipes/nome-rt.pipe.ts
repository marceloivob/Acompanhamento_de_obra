import { Pipe, PipeTransform } from '@angular/core';
import { ResponsavelTecnico } from '../model/responsavel-tecnico.model';

@Pipe({
    name: 'nomeRT'
})
export class NomeRTPipe implements PipeTransform {
    transform(responsaveis: ResponsavelTecnico[]): string[] {
        return responsaveis.map(rt => rt.nome);
    }
}
