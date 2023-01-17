import { AfterViewInit, Directive, ElementRef, Optional } from '@angular/core';
import { TooltipDirective } from 'ngx-bootstrap';

@Directive({
  selector: '.coluna-texto-truncado',
})
export class ColunaTextoTruncadoDirective implements AfterViewInit {

  constructor(
    private elementRef: ElementRef,
    @Optional() private tooltip: TooltipDirective) {}

  ngAfterViewInit(): void {
    if (!this.tooltip) return;

    // Desabilita o tooltip quando não é necessário, ou seja,
    // o texto da coluna não foi truncado pelo CSS.
    setTimeout(() => {
      const element = this.elementRef.nativeElement;
      if (element.offsetHeight >= element.scrollHeight || element.offsetWidth < element.scrollWidth) {
        this.tooltip.isDisabled = true;
      }
    }, 500);
  }
}
