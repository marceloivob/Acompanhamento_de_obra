import { AppService } from './shared/core/App.server';
import { SiconvDatePickerModule, SiconvInputModule, SiconvSelectModule, SiconvPickListModule } from '@serpro/ngx-siconv';
import { registerLocaleData } from '@angular/common';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import localeptBR from '@angular/common/locales/br';
import localeptBRExtra from '@angular/common/locales/extra/br';
import localeptPt from '@angular/common/locales/pt';
import { LOCALE_ID, NgModule, APP_INITIALIZER } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { NgHttpLoaderModule } from 'ng-http-loader';
import { SimpleTimer } from 'ng2-simple-timer';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { CnpjPipe } from './shared/pipes/cnpj.pipe';
import { FiltroAnexosPipe } from './shared/pipes/filtro-anexos.pipe';
import { SiconvModule } from './siconv.module';
import { ptBrLocale, defineLocale } from 'ngx-bootstrap';
import { PlataformaCallbackComponent } from './shared/components/plataforma-callback/plataforma-callback.component';
import { IdpCallbackComponent } from './shared/components/idp-callback/idp-callback.component';
import { NotFoundComponent } from './shared/not-found/not-found.component';
import { NotAuthorizedComponent } from './shared/not-authorized/not-authorized.component';
import { SharedModule } from './shared/shared.module';
import { HttpErrorInterceptor, HttpAuthorizationInterceptor } from './shared/interceptors';

registerLocaleData(localeptPt, localeptBR, localeptBRExtra);

defineLocale('pt-br', ptBrLocale);

export function loadSettings() {
  return () => AppService.loadSettings();
}

@NgModule({
  declarations: [
    AppComponent,
    IdpCallbackComponent,
    PlataformaCallbackComponent,
    NotFoundComponent,
    NotAuthorizedComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    AppRoutingModule,
    NgHttpLoaderModule.forRoot(),
    SiconvModule,
    ReactiveFormsModule,
    HttpClientModule,
    SiconvDatePickerModule,
    SiconvInputModule,
    SiconvSelectModule,
    SiconvPickListModule,
    SharedModule
  ],
  providers: [
    SimpleTimer,
    CnpjPipe,
    FiltroAnexosPipe,
    { provide: APP_INITIALIZER, useFactory: loadSettings, deps: [], multi: true },
    { provide: LOCALE_ID, useValue: 'pt-BR' },
    { provide: HTTP_INTERCEPTORS, useClass: HttpErrorInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: HttpAuthorizationInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
