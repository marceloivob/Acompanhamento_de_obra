package br.gov.planejamento.siconv.med.infra.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.RequiredArgsConstructor;

public class MathUtil {
	
	private MathUtil() {
	}
	
    public static BigDecimal calcularPercentual(BigDecimal valor, BigDecimal total) {

        if (valor == null || total == null) {
            return null;
        }

        return (valor.setScale(4, RoundingMode.HALF_UP)
                .divide(total.setScale(4, RoundingMode.HALF_UP), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))).setScale(2, RoundingMode.HALF_UP);
    }

	public static BigDecimal nullSafeMultiply(BigDecimal v1, BigDecimal v2) {

		if (v1 == null || v2 == null) {
			return null;
		}

		return v1.multiply(v2).setScale(2, RoundingMode.HALF_UP);
	}

    public static BigDecimal nullSafeAdd(BigDecimal v1, BigDecimal v2) {

		if (v1 == null) {
			return v2;
		}

		if (v2 == null) {
			return v1;
		}

		return v1.add(v2);
	}

	public static boolean isEqualToZero(BigDecimal valor) {
		return is(valor).equalTo(BigDecimal.ZERO);
	}

	public static BigDecimal zeroIfNull(BigDecimal valor) {
		return valor != null ? valor : BigDecimal.ZERO;
	}

	public static BigDecimalComparableBuilder is(BigDecimal valor) {
		return new BigDecimalComparableBuilder(valor);
	}

	@RequiredArgsConstructor
	public static class BigDecimalComparableBuilder {

		private final BigDecimal v1;

		public boolean equalTo(final BigDecimal v2) {
			return (v1 != null && v2 != null) ? v1.compareTo(v2) == 0 : (v1 == null && v2 == null);
		}

		public boolean greaterThan(final BigDecimal v2) {
			return v1.compareTo(v2) > 0;
		}
		
		public boolean lessOrEqualThan(final BigDecimal v2) {
			return v1.compareTo(v2) <= 0;
		}
	}
}
