export class MenuSubItemArea {
  label: string;
  labelKey: string;
  funcionalidades: string;
  action: string;
  ambiente: string;
  certificated: boolean;
}

export class MenuItem {
  itens: MenuSubItemArea[];
  id: string;
  itensPorColuna: number;
  label: string;
  labelKey: string;
  funcionalidades: string;
  action: string;
  ambiente: string;
  certificated: boolean;
}

export class MenuDomain {
  itensMenu: MenuItem[];
  urlImagemLogo: string;
  urlLinkLogo: string;
  urlLinkLogout: string;
  infoUasg: string;
  infoConvenio: string;
  textoLogout: string;
  identificacaoUsuario: string;
  nomeUsuario: string;
  isUsuarioGuest: boolean;
}
