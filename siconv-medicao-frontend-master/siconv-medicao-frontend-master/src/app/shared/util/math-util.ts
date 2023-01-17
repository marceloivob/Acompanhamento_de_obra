/**
 * Classe utilitaria com funcoes aritimeticas 'null safe' e que utilizam arredondamento
 * de duas casas decimais.
 */
export class MathUtil {

  public static add(v1: number, v2: number): number {
    if (v1 == null) {
      return v2;
    }

    if (v2 == null) {
      return v1;
    }

    return this.fixPrecision(v1 + v2);
  }

  public static subtract(v1: number, v2: number): number {
    if (v2 == null) {
      return v1;
    }

    if (v1 == null) {
      return v2;
    }

    return this.fixPrecision(v1 - v2);
  }

  public static multiply(v1: number, v2: number): number {
    return v1 != null && v2 != null ? this.round(v1 * v2) : null;
  }

  public static percentage(value: number, total: number): number {
    return value != null && total != null ? this.round((value / total) * 100) : null;
  }

  private static round(value: number): number {
    var power = Math.pow(10, 2 || 0);
    return Math.round(value * power) / power;
  }

  private static fixPrecision(value: number): number {
    return value != null ? parseFloat(value.toFixed(2)) : null;
  }
}
