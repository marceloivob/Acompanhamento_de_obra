export class Assinatura {

    constructor(
        public responsavel?: Responsavel,
        public data?: Date) { }
  }

export class Responsavel {
    constructor(
        public nome?: string,
        public nrCpf?: string,
        public perfil?: string,
        public nrCrea?: string) {}
}
