
export enum Profile {
    CONCEDENTE = 'CONCEDENTE',
    EMPRESA = 'EMPRESA',
    MANDATARIA = 'MANDATARIA',
    PROPONENTE = 'PROPONENTE',
    GUEST = 'GUEST'
}

export namespace Profile {
  export function valueOf(profileStr: string): Profile {
    return (<any>Profile)[profileStr];
  }
}
