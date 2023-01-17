import { Role } from './security/role.enum';
import { Permission } from './security/permission.enum';
import { Profile } from './security/profile.enum';
import { MBAuthEmpresa } from '@serpro/maisbrasil-autoriza';
import { AuthorityContext } from './security/authority-context.model';

export enum TIPO_LOGIN_ENUM {
  IDP = 'siconvidp',
  PLATAFORMA = 'maisbrasil'
}

export class UsuarioLogado {

  private _cpf: string;
  private _nome: string;

  private _tipoLogin: TIPO_LOGIN_ENUM;
  private _profile: Profile;
  private _propostaFk: string;

  private _authPermission = new Map<string, Permission[]>();
  private _authRole = new Map<string, Role[]>();
  private _token: string;

  public loadUsuarioIDP(token: string, tokenData: any) {

    this._nome = tokenData.nome;
    this._cpf = tokenData.cpf;
    this._tipoLogin = TIPO_LOGIN_ENUM.IDP;
    this._profile = Profile.valueOf(tokenData.tipoEnte);
    this._token = token;
    this._propostaFk = tokenData.idProposta.toString();

    let roles = [];
    if (tokenData.roles) {
      roles = tokenData.roles.map(role => Role.valueOf(role));
    }

    // Usuario com vinculo de fiscalizacao/acompanhamento no Siconv sera tratado como
    // CONCEDENTE que possui ROLE de fiscal de acompanhamento ou tecnico de terceiros.
    if (tokenData.vinculoFiscalizacao) {
      roles.push(Role.valueOf(tokenData.vinculoFiscalizacao));
      this._profile = Profile.CONCEDENTE;
    }

    this._authRole.set(this._propostaFk, roles);
  }

  public loadUsuarioPlataforma(token: string, tokenData: any) {

    const usuario = tokenData.usuario;
    const empresas = tokenData.empresas;

    this._cpf = tokenData['sub'];
    this._nome = usuario['nome'];
    this._tipoLogin = TIPO_LOGIN_ENUM.PLATAFORMA;
    this._profile = Profile.EMPRESA;
    this._token = token;

    if (empresas && empresas.length > 0) {
      empresas.forEach((empresa: MBAuthEmpresa) => {
        const permissoes: Permission[] = empresa.operacoes.
          map(operacao => Permission.valueOf(operacao)).
          filter(elem => elem ? true : false);

        this._authPermission.set(empresa.id.toString(), permissoes);
      });
    }
  }

  public get cpf(): string {
    return this._cpf;
  }

  public get nome(): string {
    return this._nome;
  }

  public get token(): string {
    return this._token;
  }

  public get proposta(): string {
    return this._propostaFk;
  }


  public hasProfile(profiles: Profile[]): boolean {

    return profiles.filter(argProfile => argProfile === this._profile)
      .length > 0;

  }

  public hasPermission(permissions: Permission[], _context: AuthorityContext): boolean {
    if (_context && _context.idEmpresa && permissions) {

      const perm: Permission[] = this._authPermission.get(_context.idEmpresa.toString());
      if (perm) {
        return perm.map(myPermission => permissions.find(permission => Permission.valueOf(permission) === myPermission))
          .filter(permissionFound => permissionFound != null)
          .length > 0;
      } else {
        return false;
      }
    } else {
      throw new Error('Não há contexto informado.');
    }
  }

  public hasRole(roles: Role[], _context: AuthorityContext): boolean {

    if (roles) {
      const rol: Role[] = this._authRole.get(_context.idProposta.toString());

      if (rol) {
        return rol
          .map(myRole => roles.find(role => role === myRole))
          .filter(roleFound => roleFound != null)
          .length > 0;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }


  public get isLoginIDP(): boolean {
    // tslint:disable-next-line: no-use-before-declare
    return TIPO_LOGIN_ENUM.IDP === this._tipoLogin;
  }

  public get isLoginPlataforma(): boolean {
    // tslint:disable-next-line: no-use-before-declare
    return TIPO_LOGIN_ENUM.PLATAFORMA === this._tipoLogin;
  }

  public get profile(): Profile {
    return this._profile;
  }

}

export interface IDPJwt {
  id: number;
  cpf: string;
  nome: string;
  idProposta: number;
  ente: string;
  tipoEnte: string;
  roles?: string[];
  exp: number;
  iss: string;
  aud: string;
  iat: number;
}
