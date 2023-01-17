import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot } from '@angular/router';
import { MaisBrasilAutorizaService } from '@serpro/maisbrasil-autoriza';
import { Observable } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { AppService } from './../../core/App.server';
import { TokenService } from './token.service';

@Injectable({
  providedIn: 'root'
})
export class PlataformaService {

  constructor(
    private maisBrasilAutorizaService: MaisBrasilAutorizaService,
    private tokenService: TokenService,
    private http: HttpClient
  ) {

    this.tokenService.tokenExpiring$.subscribe(tokenData => {
      if (tokenData.iss === 'maisbrasil') {
        this.getNewAccessToken().subscribe(newToken => {
          this.tokenService.setToken(newToken);
          console.log('Token da plataforma revalidado.');
        });
      }
    });
  }

  public initAuthorizationCodeFlow(redirectRouteUrl: string) {
    this.redirectRouteUrl = redirectRouteUrl;

    window.location.href = this.maisBrasilAutorizaService.getUrlRedirectGovBr(
      AppService.urlToGovBr,
      AppService.idAppGovBr,
      this.callbackRedirectUri
    );
  }

  public isAuthorizationCodeResponse(callbackRoute: ActivatedRouteSnapshot) {
    return callbackRoute.queryParamMap.has('code');
  }

  public requestAccessTokenAndLogin(callbackRoute: ActivatedRouteSnapshot): Observable<any> {
    const code = callbackRoute.queryParamMap.get('code');

    return this.getAccessTokenFromAuthorizationCode(code).pipe(
      map(token => {
        this.tokenService.setToken(token);
        return { token: token, redirectRouteUrl: this.redirectRouteUrl };
      })
    );
  }

  private getAccessTokenFromAuthorizationCode(code: string): Observable<string> {
    return this.http
      .post(`${AppService.endpoint}/api/authenticate`, {
        Code: code,
        RedirectURI: this.callbackRedirectUri
      })
      .pipe(
        map(data => data['body']['token']),
        catchError(err => {
          console.error(err);
          throw new Error('Erro na obtencao do token JWT a partir do code.');
        })
      );
  }

  private getNewAccessToken(): Observable<string> {
    return this.http.get(`${AppService.endpoint}/api/retoken`).pipe(
      map((data: any) => data.body.token)
    );
  }

  private set redirectRouteUrl(redirectRouteUrl: string) {
    if (redirectRouteUrl) {
      sessionStorage.setItem('redirect_route_url', redirectRouteUrl);
    } else {
      sessionStorage.removeItem('redirect_route_url');
    }
  }

  private get redirectRouteUrl(): string {
    return sessionStorage.getItem('redirect_route_url');
  }

  private get callbackRedirectUri() {
    return `${AppService.domainFrontEnd}/medicao/acesso`;
  }
}
