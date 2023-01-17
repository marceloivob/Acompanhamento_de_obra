import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PlataformaService } from '../../services/security/plataforma.service';
import { UsuarioLogadoService } from '../../services/usuario-logado.service';

@Component({
  template: ''
})
export class PlataformaCallbackComponent implements OnInit {

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private usuarioLogadoService: UsuarioLogadoService,
    private plataformaService: PlataformaService
  ) {}

  ngOnInit() {

    if (!this.usuarioLogadoService.isLoggedIn() || !this.usuarioLogadoService.usuarioLogado.isLoginPlataforma) {

      if (this.plataformaService.isAuthorizationCodeResponse(this.route.snapshot)) {

        this.plataformaService.requestAccessTokenAndLogin(this.route.snapshot).subscribe(resp => {
          this.router.navigate([resp.redirectRouteUrl]);
        });
      }
    }
  }
}
