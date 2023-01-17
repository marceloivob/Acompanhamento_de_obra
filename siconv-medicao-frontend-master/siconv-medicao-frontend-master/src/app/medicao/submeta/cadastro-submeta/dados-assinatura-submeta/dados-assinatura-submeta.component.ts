import { Assinatura } from 'src/app/shared/model/assinatura.model';
import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-dados-assinatura-submeta',
  templateUrl: './dados-assinatura-submeta.component.html',
  styleUrls: ['./dados-assinatura-submeta.component.scss']
})
export class DadosAssinaturaSubmetaComponent implements OnInit {

  @Input() assinaturas: Assinatura[];

  exibirColunaCREA: boolean = false;

  ngOnInit() {
    if (this.assinaturas) {
      this.assinaturas.forEach(assinatura => {
        if (assinatura.responsavel.nrCrea) {
          this.exibirColunaCREA = true;
        }
      });
    }
  }

}
