import { Injectable } from '@angular/core';
import { JwtHelperService } from '@auth0/angular-jwt';
import { Subject, interval } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TokenService {
  // Constantes
  private readonly TOKEN_STORAGE_KEY = 'token';

  // Atributos
  private token: string = null;
  private tokenChangedSource = new Subject<String>();
  private tokenExpiringSource = new Subject<any>();
  private jwtHelper = new JwtHelperService();
  private reTokenInterval: any = null;

  // Observables publicos
  public tokenChanged$ = this.tokenChangedSource.asObservable();
  public tokenExpiring$ = this.tokenExpiringSource.asObservable();

  constructor() {
    this.initializeToken();
  }

  private initializeToken(): void {
    const token = this.getStorage().getItem(this.TOKEN_STORAGE_KEY);
    if (this.isValidToken(token)) {
      this.token = token;
      this.unsetReTokenInterval();
      this.setReTokenInterval();
    }
  }

  public setToken(token: string): void {
    if (this.isValidToken(token)) {
      this.token = token;
      this.getStorage().setItem(this.TOKEN_STORAGE_KEY, token);
      this.tokenChangedSource.next(token);
      this.unsetReTokenInterval();
      this.setReTokenInterval();
    }
  }

  public getToken(): string {
    if (this.isValidToken(this.token)) {
      return this.token;
    }
    return null;
  }

  public removeToken(): void {
    this.token = null;
    this.getStorage().removeItem(this.TOKEN_STORAGE_KEY);
  }

  public getDataFromToken(): any {
    if (this.isValidToken(this.token)) {
      return this.jwtHelper.decodeToken(this.token);
    }
    return null;
  }

  private isValidToken(token: string): boolean {
    if (token !== null && typeof token !== undefined) {
      return !this.jwtHelper.isTokenExpired(token);
    }
    return false;
  }

  private setReTokenInterval() {
    const tokenData = this.getDataFromToken();
    if (tokenData) {
      const intervalInSeconds = tokenData.exp - new Date().getTime() / 1000 - 60; // um minuto antes de expirar
      const intervalInMiliseconds = intervalInSeconds * 1000;
      if (intervalInMiliseconds < 0) {
        this.tokenExpiringSource.next(tokenData);
      } else {
        this.reTokenInterval = interval(intervalInMiliseconds).subscribe(() => {
          this.tokenExpiringSource.next(tokenData);
          this.unsetReTokenInterval();
        });
      }
    }
  }

  private unsetReTokenInterval() {
    if (this.reTokenInterval) {
      this.reTokenInterval.unsubscribe();
      this.reTokenInterval = null;
    }
  }

  private getStorage() {
    return sessionStorage;
  }
}
