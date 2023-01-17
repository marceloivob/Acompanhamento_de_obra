import { MessageService } from './../services/message.service';
import { RequiredAuthorizer } from '../model/security/required-authorizer.model';
import { UsuarioLogadoService } from 'src/app/shared/services/usuario-logado.service';
import { OnInit, OnDestroy, Injector } from '@angular/core';
import { UsuarioLogado } from '../model/usuario-logado.model';

export abstract class BaseComponent implements OnInit, OnDestroy {

  private _permissionMap: Map<string, RequiredAuthorizer>;
  private _accesMap: Map<string, boolean> = new Map();

  // Providers utilizados
  protected _usuarioLogadoService: UsuarioLogadoService;
  protected _messageService: MessageService;


  constructor(injector: Injector) {
    this._usuarioLogadoService = injector.get(UsuarioLogadoService);
    this._messageService = injector.get (MessageService);
  }

  ngOnInit() {
    if (this._messageService.isCache()) {
      this._messageService.clearCache();
    }

    this._permissionMap = this.loadPermissions();
    this.generateAccesMap();
    this.initializeComponent();

  }

  ngOnDestroy(): void {
    this.limparMensagemSairComponente();
  }

  protected abstract initializeComponent(): void;

  protected abstract loadPermissions(): Map<string, RequiredAuthorizer>;

  protected limparMensagemSairComponente(): void {
    if (!this._messageService.isCache()) {
      this._messageService.dismissMessage();
    }
  }

  protected adicionarMensagem (mensagem: string, cache?: boolean) {
    if (cache) {
      this._messageService.success (mensagem, cache);
    } else {
      this._messageService.success (mensagem, false);
    }
  }

  private generateAccesMap() {
    this._permissionMap.forEach ((authorizer, key: string) => {

      this._accesMap.set (key, this._usuarioLogadoService.hasPermission(authorizer.profiles, authorizer.roles, authorizer.permissions ) );
    });
  }

  canAccess(permissionKey: string): boolean {
    return this._accesMap.get(permissionKey);
  }

  get usuarioLogado(): UsuarioLogado {
    return this._usuarioLogadoService.usuarioLogado;
  }
}
