package br.gov.planejamento.siconv.med.medicao.entity.dto.submetaservicosalvar;

import java.util.List;
import java.util.function.BiConsumer;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import lombok.Data;

/**
 * <p>
 * Submeta DTO que representa o trio: frente obra + evento +
 * indExecutadoEmpresa.
 * <p>
 * Segue a mesma estrutura utilizada pelo SubmetaMedicaoDTO (lista Frente de
 * Obras -> lista de eventos -> indExecutadoEmpresa por evento.
 * <p>
 * SubmetaSalvarDTO é utilizado apenas para o serviço de Salvar Submeta,
 * evitando o tráfego de mais dados que não são necessários no momento de Salvar
 * Submeta, caso fosse utilizado SubmetaMedicaoDTO.
 */
@Data
public class SubmetaSalvarDTO {

	private Long versao;

	@Valid
	@NotEmpty
	private List<FrenteObraSubmetaSalvarDTO> frentesObra;

	public Long getVersao() {
		return versao != null ? versao : 0;
	}

	public void forEachFrenteObraServico(BiConsumer<FrenteObraSubmetaSalvarDTO, ServicoSubmetaSalvarDTO> consumer) {
		getFrentesObra().forEach(
				frenteObra -> frenteObra.getServicos().forEach(servico -> consumer.accept(frenteObra, servico)));

	}
}
