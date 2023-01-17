import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { MessageService } from './../services/message.service';

const DEFAULT_ERROR_TITLE = 'Erro inesperado ao processar a requisição. Tente novamente.';
const DEFAULT_ERROR_DESCRIPTION = 'Caso o erro persista, contate o administrador do sistema.';

@Injectable()
export class HttpErrorInterceptor implements HttpInterceptor {

  constructor(
    private _messageService: MessageService,
    private _router: Router
  ) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    return next.handle(req).pipe(
      catchError((response) => {
        const error = response.error;

        this._messageService.dismissAll();

        if (response.status === 401 || response.status === 403) {
          this._router.navigate(['not-authorized']);

        } else if (error.status === 'FAIL') {
          const data = error.data;

          this._messageService.error(
            'Mensagem de Erro!',
            data.errors.map((item) => item.detail),
            null
          );

        } else {
          const data = error.data;

          if (data && data.ticket) {
            this._messageService.error(
              DEFAULT_ERROR_TITLE,
              DEFAULT_ERROR_DESCRIPTION + ' [Ticket: ' + data.ticket + ']'
            );

            if (data.stackTrace) {
              console.log('Error stack trace [Ticket ' + data.ticket + ']:\n' + data.stackTrace);
            }

          } else {
            this._messageService.error(DEFAULT_ERROR_TITLE, DEFAULT_ERROR_DESCRIPTION);
          }
        }

        return throwError(response);
      })
    );
  }
}
