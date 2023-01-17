import { AlertMessageService, AlertMessageOptions, AlertMessage } from '@serpro/ngx-siconv';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class MessageService {

  private cache = false;

  constructor(private _alertMessageService: AlertMessageService) { }

  public success(message: string, redirect: boolean) {
    this._alertMessageService.dismissAll();
    if (redirect) {
      this.cache = true;
    }
    this._alertMessageService.success(message);
  }

  public error(title: string, description?: string, details?: string[], options?: AlertMessageOptions): AlertMessage {
    return this._alertMessageService.error(title, description, details, options);
  }

  public warn(title: string, description?: string, details?: string[], options?: AlertMessageOptions): AlertMessage {
    return this._alertMessageService.warn(title, description, details, options);
  }

  public dismissAll(): void {
    this._alertMessageService.dismissAll();
  }

  public isCache(): boolean {
    return this.cache;
  }

  public clearCache(): void {
    this.cache = false;
  }

  public dismissMessage() {
    this.clearCache();
    this._alertMessageService.dismissAll();
  }
}
