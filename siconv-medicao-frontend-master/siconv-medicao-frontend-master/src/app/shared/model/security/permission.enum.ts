export enum Permission {
  INCLUIR_MEDICAO = 1000,
  EDITAR_MEDICAO = 1001,
  EXCLUIR_MEDICAO = 1002,
  ENVIAR_MEDICAO_CONVENENTE = 1003,
  CANCELAR_ENVIO_MEDICAO_CONVENENTE = 1004,
  EDITAR_SUBMETA = 1005,
  EXCLUIR_SUBMETA = 1006,
  ASSINAR_SUBMETA = 1007,
  INCLUIR_OBSERVACAO_MEDICAO = 1008,
  EDITAR_OBSERVACAO_MEDICAO = 1009,
  EXCLUIR_OBSERVACAO_MEDICAO = 1010
}

export namespace Permission {
  export function valueOf(permissionCode: number): Permission {
    return (<any>Permission)[permissionCode];
  }
}
