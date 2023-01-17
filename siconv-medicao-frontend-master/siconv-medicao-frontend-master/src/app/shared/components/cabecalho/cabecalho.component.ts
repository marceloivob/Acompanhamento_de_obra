import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { User, TopComponent } from '@serpro/ngx-siconv';
import menuJson from './menu-showcase.json';
import { UsuarioLogadoService } from 'src/app/shared/services/usuario-logado.service';
import { Router, NavigationEnd } from '@angular/router';
import { Subscription } from 'rxjs';
import { SiconvMenuAntigoComponent } from '../siconv-menu-antigo/siconv-menu-antigo.component';


@Component({
  selector: 'app-cabecalho',
  templateUrl: './cabecalho.component.html',
  styleUrls: ['./cabecalho.component.scss']
})
export class CabecalhoComponent implements OnInit, OnDestroy {

  @ViewChild(SiconvMenuAntigoComponent)
  private siconvMenuAntigoComponent: SiconvMenuAntigoComponent;

  @ViewChild(TopComponent)
  private topComponent: TopComponent;

  usuario: User;
  menu = menuJson;
  isLoginIDP: boolean;
  isLoginPlataforma: boolean;

  subscription: Subscription;

  constructor(private _usuarioLogadoService: UsuarioLogadoService, private _router: Router) {
  }

  ngOnInit() {
    if (this._usuarioLogadoService.usuarioLogado) {
      this.usuario = new User(this._usuarioLogadoService.usuarioLogado.nome);
      this.isLoginIDP = this._usuarioLogadoService.usuarioLogado.isLoginIDP;
      this.isLoginPlataforma = this._usuarioLogadoService.usuarioLogado.isLoginPlataforma;
    }

    this.subscription = this._router.events
    .filter(event => event instanceof NavigationEnd)
    .subscribe(() => {

      if (this._usuarioLogadoService.usuarioLogado.isLoginIDP) {
        this.siconvMenuAntigoComponent.minutos = 29;
        this.siconvMenuAntigoComponent.segundos = 59;

      } else {
        this.topComponent.minutos = 29;
        this.topComponent.segundos = 59;
      }
    });
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  sessionTimeFeedback(sessionTime) {

    if (sessionTime == 'expired') {
      console.log('A sessão encerrou. Será preciso logar novamente');
      this.logoutFeedback();
    }
    if (sessionTime == 'warning') {
      console.log('Esta sessão irá se encerrar em menos 2 minutos');
    }
  }

  logoutFeedback() {

    this._usuarioLogadoService.logout();

  }

  getHomeUrl(): string {
    let redirectUrl: string;

    if (this._usuarioLogadoService.isLoggedIn()) {
      if (this._usuarioLogadoService.usuarioLogado.isLoginIDP) {
        redirectUrl = `/acompanhamento/proposta/${this._usuarioLogadoService.usuarioLogado.proposta}/dados-gerais`;
      } else {
        redirectUrl = `/preenchimento`;
      }
    }

    return redirectUrl;
  }

}
