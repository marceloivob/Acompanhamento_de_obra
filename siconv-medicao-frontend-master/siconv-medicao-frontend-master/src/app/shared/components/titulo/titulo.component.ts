import { Component, OnInit } from '@angular/core';
import { SharedService } from '../../services/shared-service.service';

@Component({
  selector: 'app-titulo',
  templateUrl: './titulo.component.html'
})
export class TituloComponent implements OnInit {

  module;
  titulo;
  subtitulo;
  info;

  constructor(private _sharedService: SharedService) {
    _sharedService.changeEmitted$.subscribe(
      tituloTela => {
        if (tituloTela.titulo) {
          this.module = "Medição";
        }
        this.titulo = tituloTela.titulo;
        this.subtitulo = tituloTela.subtitulo;
        this.info = tituloTela.info;
      });
  }

  ngOnInit() {
    // vazio
  }

}
