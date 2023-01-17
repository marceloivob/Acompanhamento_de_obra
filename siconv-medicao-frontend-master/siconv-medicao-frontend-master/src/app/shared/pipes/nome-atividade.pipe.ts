import { Pipe, PipeTransform } from '@angular/core';
import { Atividade } from '../model/atividade.model';

@Pipe({
  name: 'nomeAtividade'
})
export class NomeAtividadePipe implements PipeTransform {
  transform(atividades: Atividade[]): string[] {
    return atividades.map(atividade => atividade.nome);
  }
}
