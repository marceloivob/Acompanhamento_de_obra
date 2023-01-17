import { Component, Injector, Input } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Medicao } from 'src/app/shared/model/medicao.model';
import { Profile } from 'src/app/shared/model/security/profile.enum';
import { RequiredAuthorizer } from 'src/app/shared/model/security/required-authorizer.model';
import { BaseComponent } from 'src/app/shared/util/base.component';


@Component({
  selector: 'app-detalhe-dados-medicao',
  templateUrl: './detalhe-dados-medicao.component.html'
})
export class DetalheDadosMedicaoComponent extends BaseComponent {
  @Input()
  medicao: Medicao;

  constructor(
    private _route: ActivatedRoute,
    private _router: Router,
    injector: Injector
  ) {
    super(injector);
  }

  initializeComponent() {
    
  }
  
  loadPermissions(): Map<string, RequiredAuthorizer> {
    return new Map([]);
  }

  get exibeDadosVistoria() {  
    return (this.medicao.situacao.codigo === 'ACT' || this.isConcedenteMandataria()) && this.medicao.idMedicaoAgrupadora === null;
  }

  get exibeDadosVistoriaExtra() {
    return this.exibeDadosVistoria && this.medicao.vistoriaExtra == true;
  }

  isConcedenteMandataria() : boolean {
    return this.usuarioLogado.hasProfile([Profile.CONCEDENTE]) || this.usuarioLogado.hasProfile([Profile.MANDATARIA]);
  }

}
