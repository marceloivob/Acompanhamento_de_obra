package br.gov.planejamento.siconv.med.infra.message;

import static java.util.Collections.frequency;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class MessageKeyTest {

	@Test
	void testCodigosMensagensRepetidos() {

		List<String> keys = Stream.of(MessageKey.values()).map(MessageKey::value).collect(toList());

		Set<String> repetidos = keys.stream().filter(key -> frequency(keys, key) > 1).collect(toSet());

		assertThat("Não deve existir código de mensagem repetido", repetidos, empty());
	}
}
