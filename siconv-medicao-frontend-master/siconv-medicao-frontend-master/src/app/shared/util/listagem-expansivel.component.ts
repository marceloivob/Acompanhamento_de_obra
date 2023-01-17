import { ViewChildren, QueryList } from '@angular/core';
import { ToggleComponent } from '@serpro/ngx-siconv';
import { BaseComponent } from 'src/app/shared/util/base.component';

export abstract class ListagemExpansivelComponent extends BaseComponent {


  @ViewChildren(ToggleComponent)
  toggles: QueryList<ToggleComponent>;


  public expandCollapseClickOutsideIcon (objetoSelecionado: any) {


    const toggle: ToggleComponent = this.toggles.find ( tog => tog.source === objetoSelecionado);

    if (toggle) {
      toggle.toggle();
    }

  }

}
