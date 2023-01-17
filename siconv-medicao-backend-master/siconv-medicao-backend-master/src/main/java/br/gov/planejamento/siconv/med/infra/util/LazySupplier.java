package br.gov.planejamento.siconv.med.infra.util;

import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LazySupplier<T> implements Supplier<T> {

	private final Supplier<T> supplier;
	private boolean initialized;
	private T value;

	@Override
	public T get() {
		if (!initialized) {
			initialized = true;
			value = supplier.get();
		}

		return value;
	}
}