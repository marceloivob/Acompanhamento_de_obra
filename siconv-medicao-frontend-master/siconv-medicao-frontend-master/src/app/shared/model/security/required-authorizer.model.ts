import { Role } from './role.enum';
import { Permission } from './permission.enum';
import { Profile } from './profile.enum';
export class RequiredAuthorizer {

  constructor(private _profiles: Profile[],
              private _roles: Role[],
              private _permissions: Permission[]) {

  }


  public get profiles (): Profile[] {
    return this._profiles;
  }

  public get permissions (): Permission[] {
    return this._permissions;
  }

  public get roles (): Role[] {
    return this._roles;
  }
}
