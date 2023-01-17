package br.gov.planejamento.siconv.med.infra.util;

import static java.lang.Integer.parseInt;

import java.util.Comparator;

/**
 * Comparador para números de submetas no seguinte formato:
 * <code>[número meta].[número submeta]</code>
 */
public class NumeroSubmetaFormatadoComparator implements Comparator<String> {

    private static final String REGEX_PONTO = "\\.";

    public static final NumeroSubmetaFormatadoComparator INSTANCE = new NumeroSubmetaFormatadoComparator();

    private NumeroSubmetaFormatadoComparator() {
        super();
    }

    @Override
    public int compare(String o1, String o2) {

        String[] split1 = o1.split(REGEX_PONTO);
        Integer nrMeta1 = parseInt(split1[0]);
        Integer nrSubmeta1 = parseInt(split1[1]);

        String[] split2 = o2.split(REGEX_PONTO);
        Integer nrMeta2 = parseInt(split2[0]);
        Integer nrSubmeta2 = parseInt(split2[1]);

        return nrMeta1.equals(nrMeta2) ? nrSubmeta1.compareTo(nrSubmeta2) : nrMeta1.compareTo(nrMeta2);
    }
}
