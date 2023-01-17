import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { UsuarioLogadoService } from '../../services/usuario-logado.service';
import { IdpService } from '../../services/security/idp.service';

@Component({
  template: ''
})
export class IdpCallbackComponent implements OnInit {

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private usuarioLogadoService: UsuarioLogadoService,
    private idpService: IdpService
  ) {}

  ngOnInit() {

    if (!this.usuarioLogadoService.isLoggedIn() || !this.usuarioLogadoService.usuarioLogado.isLoginIDP) {

      if (this.idpService.isAuthorizationCodeResponse(this.route.snapshot)) {

        this.idpService.requestAccessTokenAndLogin(this.route.snapshot).subscribe(resp => {
          this.router.navigate([resp.redirectRouteUrl]);
        });
      }
    }
  }
}
