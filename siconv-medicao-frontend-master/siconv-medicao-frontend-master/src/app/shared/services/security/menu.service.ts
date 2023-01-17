import { Observable } from 'rxjs';
import { AppService } from 'src/app/shared/core/App.server';
import { HttpHeaders, HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class MenuService {

  menuLoaded = false;
  menuSiconv: any;

  constructor(private http: HttpClient) { }

  getMenu(): Observable<any> {
    const headers = new HttpHeaders()
      .set('Accept',  'application/json');

    const httpOptions = {
      headers: headers,
      withCredentials: true
    };
    return this.http.get<any>(`${AppService.urlToSICONVService}/api/menu`, httpOptions);
  }


  carregarMenuIdp() {
    this.getMenu().subscribe(
      (values: any) => {
        this.menuLoaded = true;
        this.menuSiconv = values;
      }
    );
  }

  public obterURLLogoutSiconv (): string {
    return this.menuSiconv['UrlLinkLogout'];
  }

}
