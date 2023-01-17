import { Component, OnInit, Input } from '@angular/core';
import { Contrato } from '../../model/contrato.model';
import { ContratoService } from '../../services/contrato.service';

@Component({
  selector: 'app-dados-ctef',
  templateUrl: './dados-ctef.component.html'
})
export class DadosCtefComponent implements OnInit {
  @Input()
  contrato: Contrato;

  @Input()
  collapsible = true;

  @Input()
  collapsed = false;

  constructor(private _contratoService: ContratoService) {}

  ngOnInit() {
    if (!this.contrato) {
      this.contrato = this._contratoService.contratoAtual;
    }
  }
}
