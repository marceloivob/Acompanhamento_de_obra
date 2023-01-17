export class StringUtil {


  /**
   * Remove todos os caracteres exceto número.
   *
   * @param arg
   */
  public static removerCaracteres (arg: string) {
    return arg.replace(/[^\d]+/g, '');
  }

  /**
   * Formata o Numero Telefônico para padrão de Celular e Fixo.
   *
   * @param arg
   *
   */
  public static formatarTelefone (arg: string): string {

    arg = arg.replace(/\D/g, '');
    arg = arg.replace(/^(\d{2})(\d)/g, '($1) $2');
    arg = arg.replace(/(\d)(\d{4})$/, '$1-$2');

    return arg;
  }

  public static replaceAll(str, find, replace) {
    var escapedFind=find.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, "\\$1");

    return str.replace(new RegExp(escapedFind, 'g'), replace);
  }

}
