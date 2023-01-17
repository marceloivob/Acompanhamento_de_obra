import { Component, OnInit, OnDestroy, Input, Output, EventEmitter } from '@angular/core';
import { MenuDomain, MenuItem, MenuSubItemArea } from './menu.domain';
import { SimpleTimer } from 'ng2-simple-timer';
import { BsModalRef } from 'ngx-bootstrap';
import { MenuService } from '../../services/security/menu.service';
import { UsuarioLogadoService } from '../../services/usuario-logado.service';
import { AppService } from '../../core/App.server';

@Component({
  selector: 'app-siconv-menu-antigo',
  templateUrl: './siconv-menu-antigo.component.html',
  styleUrls: ['./siconv-menu-antigo.component.scss']
})
export class SiconvMenuAntigoComponent implements OnInit, OnDestroy {

  // Variáveis de controle e montagem do menu
  menuLoaded: Promise<boolean>;
  menuSelecionado = false;
  menuitemSelecionado: MenuItem = new MenuItem();
  itensMenuSelecionado: MenuSubItemArea[];
  menu: MenuDomain;

  @Input() totalTime: number;
  @Input() warningTime: number;
  @Output() sessionTime = new EventEmitter();
  @Output() logout = new EventEmitter();

  dthora: string;
  minutos: number;
  segundos: number;
  timerId: string;
  modalRef: BsModalRef;

  constructor(private service: MenuService,
    private st: SimpleTimer,
    private _usuarioLogadoService: UsuarioLogadoService) { }

  ngOnInit() {
    this.getMenu();
    this.getDateHour();

    this.st.newTimer('1sec', 1);
    this.timerId = this.st.subscribe('1sec', () => this.callbackSeg());
    this.minutos = this.totalTime - 1;
    this.segundos = 60;
  }

  ngOnDestroy(): void {
    this.delAllTimer();
  }

  getDateHour() {
    this.dthora = new Date().toString();
  }

  callbackSeg() {
    this.segundos--;
    if (this.segundos === -1) {
      this.minutos--;
      this.segundos = 59;
    }

    if (this.minutos < 0) {
      this.delAllTimer();
      //A sessão encerrou. Será preciso logar novamente.
      this.sessionTime.emit('expired');
    }

    if (this.minutos === this.warningTime && this.segundos === 0) {
      //Esta sessão irá se encerrar em menos x minutos.
      this.sessionTime.emit('warning');
    }

  }

  delAllTimer() {
    this.minutos = 0;
    this.segundos = 0;
    this.st.delTimer('1sec');
  }

  async recuperarMenu() {
    await this.service.getMenu().subscribe((values: any) => {
      this.menu = new MenuDomain();
      const menu: MenuDomain = new MenuDomain();

      menu.urlImagemLogo = values.UrlImagemLogo;
      menu.urlLinkLogo = values.UrlLinkLogo;

      menu.urlLinkLogout = AppService.urlToIDPService;

      menu.infoUasg = values.InfoUasg;
      menu.infoConvenio = values.InfoConvenio;
      menu.textoLogout = values.TextoLogout;

      menu.identificacaoUsuario = this._usuarioLogadoService.usuarioLogado.cpf;
      menu.nomeUsuario = this._usuarioLogadoService.usuarioLogado.nome;
      menu.isUsuarioGuest = values.IsUsuarioGuest;

      menu.itensMenu = values.ListaMenu.map(lm => {
        const menuItem: MenuItem = new MenuItem();
        menuItem.id = lm.Id;
        menuItem.itensPorColuna = lm.ItensPorColuna;
        menuItem.label = lm.Label;
        menuItem.labelKey = lm.LabelKey;
        menuItem.funcionalidades = lm.Funcionalidades;
        menuItem.action = lm.Action;
        menuItem.ambiente = lm.Ambiente;
        menuItem.certificated = lm.Certificated;
        menuItem.itens = lm.Items.map(msi => {
          const si: MenuSubItemArea = new MenuSubItemArea();
          si.label = msi.Label;
          si.labelKey = msi.LabelKey;
          si.funcionalidades = msi.Funcionalidades;
          si.action = msi.Action;
          si.ambiente = msi.Ambiente;
          si.certificated = msi.Certificated;
          return si;
        });
        return menuItem;
      });
      this.menuLoaded = Promise.resolve(true);
      this.menu = menu;
    }
    );
  }

  getMenu() {
    if (this._usuarioLogadoService.usuarioLogado) {
      this.recuperarMenu();
    }
  }

  clickMenu(menuid: string) {
    this.menuSelecionado = true;
    const itensAgrupados = [];
    for (let i = 0; i < this.menu.itensMenu.length; i++) {
      if (this.menu.itensMenu[i].id === menuid) {
        this.menuitemSelecionado = this.menu.itensMenu[i];
        break;
      }
    }
    for (let i = 0; i < this.menuitemSelecionado.itens.length; i += this.menuitemSelecionado.itensPorColuna) {
      itensAgrupados.push(this.menuitemSelecionado.itens.slice(i, i + this.menuitemSelecionado.itensPorColuna));
    }
    this.itensMenuSelecionado = itensAgrupados;
  }

  logoutUser() {
    this.logout.emit(true);
  }
}
