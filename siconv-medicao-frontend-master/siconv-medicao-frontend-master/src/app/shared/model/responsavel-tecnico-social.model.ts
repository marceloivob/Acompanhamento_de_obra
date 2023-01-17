import { Submeta } from './submeta.model';
import { TipoResponsavelTecnico } from './tipo-responsavel-tecnico.model';

export class ResponsavelTecnicoSocial {

  constructor(
    public id?: number,
    public tipo?: TipoResponsavelTecnico,
    public responsavelTecnico?: ResponsavelTecnico,
    public atividade?: string,
    public formacao?: string,
    public registroProfissional?: string,
    public orgao?: Orgao,
    public dtInclusao?: Date,
    public dtInativacao?: Date,
    public submetas?: Submeta[],
    public showDetail: boolean = true,
    public possuiSubmetaAssinada?: boolean,
    public nomeArquivo?: string,
    public urlArquivo?: string,
    public arquivo?: any,
    public idContratoSiconv?: number,
    public versao?: number
  ) {}

  createResponsavelTecnico(id?: number, cpf?: string, telefone?: string, versao?: number): void {
    this.responsavelTecnico = {
      id: id,
      cpf: cpf,
      telefone: telefone,
      versao: versao
    };
  }

  createOrgao(nome?: string, telefone?: string, email?: string) {
    this.orgao = {
      nome: nome,
      telefone: telefone,
      email: email
    };
  }

  // Métodos acessores para serem utilizados na siconv-table para ordenação
  get _tipo(): string {
    return this.tipo.descricao;
  }

  get _cpf(): string {
    return this.responsavelTecnico.cpf;
  }

  get _nome(): string {
    return this.responsavelTecnico.nome;
  }

  get _submetas(): string {
    return this.submetas.length > 0 ? this.submetas
      .map(sub => sub.nrSubmetaAnalise + ' - ' + sub.descricao)
      .reduce((prev, cur) => prev + ' \n' + cur) : '';
  }
}

class Orgao {
  constructor(
    public nome?: string,
    public telefone?: string,
    public email?: string
  ) {}
}

class ResponsavelTecnico {
  constructor(
    public id?: number,
    public cpf?: string,
    public nome?: string,
    public email?: string,
    public telefone?: string,
    public versao?: number
  ) {}
}
