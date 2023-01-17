import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Contrato } from '../../../shared/model/contrato.model';

@Component({
  selector: 'app-card-contrato',
  templateUrl: './card-contrato.component.html',
  styleUrls: ['./card-contrato.component.scss'],
})
export class CardContratoComponent implements OnInit {

  @Input()
  contratos: Contrato[];

  @Input()
  idFornecedor: number;

  constructor(
    private _router: Router,
    private _route: ActivatedRoute,
) {}

  ngOnInit() {
    //vazio
  }

  abrirMedicao(contrato: Contrato) {
    this._router.navigate(['../', contrato.id, 'medicao'], { relativeTo: this._route });
  }

  exibirConfiguraCtef(contrato: Contrato) {
    if (!contrato.inSocial) {
      this._router.navigate([`/preenchimento/empresa/${this.idFornecedor}/contrato/${contrato.id}/config/rt`]);
    } else {
      this._router.navigate([`/preenchimento/empresa/${this.idFornecedor}/contrato/${contrato.id}/config/rtsocial`]);
    }
  }
}
