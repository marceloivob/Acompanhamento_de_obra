
export class AuthorityContext {
  idProposta: number;
  idEmpresa: number;

  constructor(idProposta: number, idEmpresa: number) {
    this.idProposta = idProposta;
    this.idEmpresa = idEmpresa;
  }
}
