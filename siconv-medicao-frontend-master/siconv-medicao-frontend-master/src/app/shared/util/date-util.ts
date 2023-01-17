export class DateUtil {

  /**
    * Adiciona uma qtde de dias a Data

    * @ param dataArg
    * @ param qtde
  */
  public static adicionarDia (dataArg: Date, qtde: number): Date {

    const novaData = new Date (dataArg);

    if (dataArg) {
      novaData.setDate (novaData.getDate() + qtde);
    }

    return novaData;
  }

  /**
   * Gera um Objeto Date com base em uma string no padrão yyyy-MM-dd
   *
   * @ param data
   *
   */
  public static generateDateByUSAPattern(data: string): any {
    let retorno: Date = null;
    let dia: number;
    let mes: number;
    let ano: number;

    if (data) {
      dia = parseInt (data.substring(8,10));
      mes = parseInt (data.substring(5,7))-1;
      ano = parseInt (data.substring(0,4));

      retorno = new Date (ano, mes, dia);
    }

    return retorno;
  }

}
