package br.gov.planejamento.siconv.med.infra.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Locale;

public class TemporalUtil {

	private TemporalUtil() {
		super();
	}

	/**
	 * Formata a data no Padrão Brasileiro pt-BR: DD/MM/AAAA
	 * 
	 * @param data
	 * @return
	 */
	public static String formataDataPtBR(LocalDate data) {

		return data.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(new Locale("pt", "BR")));

	}
	
	/**
	 * Calcula a diferrença entre duas datas(Temporal) 
	 * Obs.: Caso deseje calcular em dia/mes/anoa a data pode ser LocalDate
	 * 		 Caso deseje calcular em horas/minutos/segundos a data precisa ser do tipo LocalDateTime 
	 * 
	 * @param data1
	 * @param data2
	 * @param unit
	 * @return
	 */
	public static long diferenca (Temporal data1, Temporal data2, ChronoUnit unit) {
		
		switch (unit) {
		case DAYS:
			return ChronoUnit.DAYS.between(data1, data2);
		case MONTHS:
			return ChronoUnit.MONTHS.between(data1, data2);
		case YEARS:
			return ChronoUnit.YEARS.between(data1, data2);
		case HOURS:
			return ChronoUnit.HOURS.between(data1, data2);
		case MINUTES:
			return ChronoUnit.MINUTES.between(data1, data2);
		case SECONDS:
			return ChronoUnit.SECONDS.between(data1, data2);
		 default:
			 return -1;
		}
		
	}
	
	/**
	 * Verifica se @param dataRef está no intervalo fechado de datas
	 * 
	 * @param dataRef
	 * @param dataInicial
	 * @param dataFinal
	 * @return
	 */
	public static boolean validarNoIntervalo(LocalDate dataRef, LocalDate dataInicial, LocalDate dataFinal, Intervalo intervalo) {
		
		if (dataInicial == null || dataRef == null) {
			throw new IllegalArgumentException();
		}
		
		switch (intervalo) {
		case ABERTO:
			//Verifica a dataRef é maior ou igual a data Inicial e
			//se a dataRef é menor ou igual a dataFinal ou se a dataFinal é nula.
			return (dataInicial.isBefore(dataRef) || dataInicial.isEqual(dataRef)) && 
					((dataFinal == null) ||
					(dataFinal.isAfter(dataRef) || dataFinal.isEqual(dataRef)));
			
		case FECHADO:
			if (dataFinal == null) {
				throw new IllegalArgumentException();
			}

			//Verifica a dataRef é maior ou igual a data Inicial e
			//se a dataRef é menor ou igual a dataFinal.
			return (dataInicial.isBefore(dataRef) || dataInicial.isEqual(dataRef)) &&
					(dataFinal.isAfter(dataRef) || dataFinal.isEqual(dataRef));
		default:
			return Boolean.FALSE;
		}
		
	}

	public enum Intervalo {
		ABERTO,
		FECHADO;
	}
}
