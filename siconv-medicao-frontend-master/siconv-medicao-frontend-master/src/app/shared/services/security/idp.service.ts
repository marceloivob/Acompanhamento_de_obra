import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot } from '@angular/router';
import { Observable, interval } from 'rxjs';
import { map } from 'rxjs/operators';
import { AppService } from 'src/app/shared/core/App.server';
import { TokenService } from './token.service';

@Injectable({
  providedIn: 'root'
})
export class IdpService {

  // Constantes
  private readonly SESSION_REFRESH_INTERVAL_IN_MINUTES = 5;

  // Atributos
  private sessionRefreshInterval: any = null;

  constructor(private tokenService: TokenService, private http: HttpClient) {

    this.tokenService.tokenExpiring$.subscribe(() => {
      if (this.isCurrentTokenIdp()) {
        this.getNewAccessToken().subscribe(newToken => {
          this.tokenService.setToken(newToken);
          console.log('Token do IDP revalidado.');
        });
      }
    });

    if (this.isCurrentTokenIdp()) {
      this.setSessionRefreshInterval();
    }
  }

  private isCurrentTokenIdp(): boolean {
    const tokenData = this.tokenService.getDataFromToken();
    return tokenData && tokenData.iss === 'siconvidp';
  }

  public initAuthorizationCodeFlow(idProposta: number, redirectRouteUrl: string): void {
    this.redirectRouteUrl = redirectRouteUrl;
    window.location.href = `${AppService.urlToIDPService}/token/key?app=${AppService.idpAppName}&idProposta=${idProposta}`;
  }

  public isAuthorizationCodeResponse(callbackRoute: ActivatedRouteSnapshot): boolean {
    return callbackRoute.queryParamMap.has('token') && callbackRoute.queryParamMap.has('idProposta');
  }

  public requestAccessTokenAndLogin(callbackRoute: ActivatedRouteSnapshot): Observable<any> {
    const code = callbackRoute.queryParamMap.get('token');
    const idProposta = +callbackRoute.queryParamMap.get('idProposta');

    return this.getAccessTokenFromAuthorizationCode(code, idProposta).pipe(
      map(token => {
        this.tokenService.setToken(token);
        this.unsetSessionRefreshInterval();
        this.setSessionRefreshInterval();
        return { token: token, redirectRouteUrl: this.redirectRouteUrl };
      })
    );
  }

  private getAccessTokenFromAuthorizationCode(code: string, idProposta: number): Observable<string> {
    return this.http
      .get(`${AppService.urlToIDPService}/token/jwt?token=${code}&prop=${idProposta}`, { withCredentials: true })
      .pipe(map(data => data['token']));
  }

  private getNewAccessToken(): Observable<string> {
    return this.http
      .get(`${AppService.urlToIDPService}/jwt/refresh`, {
        withCredentials: true,
        headers: { Authorization: `Bearer ${this.tokenService.getToken()}` }
      })
      .pipe(map((data: any) => data.token));
  }

  private pingSiconv(): void {
    const done = (data: any) => console.log('Ping Siconv ', data);
    fetch(`${AppService.urlToSICONVService}/refresh.jsp`, { credentials: 'include' })
      .then(done)
      .catch(done);
  }

  private pingIdp(): void {
    const done = (data: any) => console.log('Ping IDP ', data);
    fetch(`${AppService.urlToIDPService}/public/refresh.jsp`, { credentials: 'include' })
      .then(done)
      .catch(done);
  }

  private setSessionRefreshInterval(): void {
    const intervalInMiliseconds = 1000 * 60 * this.SESSION_REFRESH_INTERVAL_IN_MINUTES;
    this.sessionRefreshInterval = interval(intervalInMiliseconds).subscribe(() => {
      if (this.isCurrentTokenIdp()) {
        this.pingSiconv();
        this.pingIdp();
      }
    });
  }

  private unsetSessionRefreshInterval(): void {
    if (this.sessionRefreshInterval) {
      this.sessionRefreshInterval.unsubscribe();
      this.sessionRefreshInterval = null;
    }
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
}
