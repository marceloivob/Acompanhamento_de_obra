
interface ServerConfig {
  IDP: string;
  SICONV: string;
  urlGovBr : string;
  idAppGovBr : string;
}

export class AppService {

    private static _urlToIDPService: string;
    private static _urlToSICONVService: string;
    private static _idpAppName: string;
    private static _loaded = false;

    private static _urlGovBr : string;
    private static _idAppGovBr : string;


    public static isAmbienteLocal(): boolean {

        return (window.location.hostname.search('localhost') >= 0);
    }
    /**
     * acesso.gov.br
     */
    public static get domainFrontEnd(): string {
        if (AppService.isAmbienteLocal()) {
            return 'http://localhost:4200';
        } else {
            return window.location.origin;
        }
    }

    /**
     * SERVER
     */
    public static get endpoint(): string {
      if (AppService.isAmbienteLocal()) {
          return 'http://localhost:8080';
      } else {
          return window.location.origin + '/medicao-backend';
      }
  }


  public static loadSettings(): Promise<any> {

    return fetch(`${AppService.endpoint}/app/integrations`)
      .then( (response) => response.json() )
      .then( (parsedResponse) => {
        const config: ServerConfig = parsedResponse.data;
        AppService._urlToSICONVService = config.SICONV;
        AppService._urlToIDPService = config.IDP;

        if (AppService.isAmbienteLocal()) {
          AppService._idpAppName = 'MEDD';
        } else {
          AppService._idpAppName = 'MED';
        }

        AppService._urlGovBr = config.urlGovBr;
        AppService._idAppGovBr = config.idAppGovBr;

        AppService._loaded = true;
      }).catch( (e) => console.error('Não foi possível obter configuração do Servidor!', e) );
  }

  public static get idAppGovBr() {
    return AppService._idAppGovBr;
  }

  public static get urlToGovBr() {
    return AppService._urlGovBr;
  }

  public static get urlToIDPService() {
    return AppService._urlToIDPService;
  }

  public static get urlToSICONVService() {
    return AppService._urlToSICONVService;
  }

  public static get idpAppName() {
    return AppService._idpAppName;
  }
}
