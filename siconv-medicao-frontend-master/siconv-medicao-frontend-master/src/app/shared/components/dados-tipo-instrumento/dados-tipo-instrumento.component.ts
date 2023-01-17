import { Component, Input, OnInit } from '@angular/core';
import { Contrato } from '../../model/contrato.model';
import { ContratoService } from '../../services/contrato.service';
import { TipoInstrumento } from '../../model/tipo-instrumento.model';

@Component({
  selector: 'app-dados-tipo-instrumento',
  templateUrl: './dados-tipo-instrumento.component.html'
})
export class DadosTipoInstrumentoComponent implements OnInit {
  @Input()
  contrato: Contrato | TipoInstrumento;

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
