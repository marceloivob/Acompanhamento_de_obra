package br.gov.planejamento.siconv.med.medicao.business.builder;

import static br.gov.planejamento.siconv.med.infra.util.MathUtil.nullSafeMultiply;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.gov.planejamento.siconv.med.infra.security.SecurityContext;
import br.gov.planejamento.siconv.med.medicao.business.builder.SubmetaMedicaoBuilder.Context;
import br.gov.planejamento.siconv.med.medicao.entity.dto.MedicaoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoVrplDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoVrplDTO.ValorServicoBM;
import io.quarkus.arc.Unremovable;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaMedicaoDTO;

@Unremovable
@ApplicationScoped
public class CalculoValoresServicoStep extends AbstractSubmetaMedicaoStep {

	@Inject
	public CalculoValoresServicoStep(SecurityContext securityContext) {
		super(securityContext);
	}

	@Override
	public void process(SubmetaMedicaoDTO submetaMedicao, Context builderContext) {

		MedicaoDTO medicao = builderContext.getMedicao();

		getServicos(submetaMedicao).forEach(servico -> this.calcularValoresServico(servico, medicao.getId()));
	}

	private void calcularValoresServico(ServicoVrplDTO servico, Long idMedicao) {
		
		servico.setVlTotalServico(nullSafeMultiply(servico.getQtd(), servico.getPreco()));

		servico.setQtdRealizadoEmpresa(calcularQtdRealizado(idMedicao, servico, ValorServicoBM::getQtdEmpresa));
		servico.setQtdRealizadoConvenente(calcularQtdRealizado(idMedicao, servico, ValorServicoBM::getQtdConvenente));
		servico.setQtdRealizadoConcedente(calcularQtdRealizado(idMedicao, servico, ValorServicoBM::getQtdConcedente));

		servico.setQtdAcumuladoEmpresa(calcularQtdAcumulado(idMedicao, servico, ValorServicoBM::getQtdEmpresa));
		servico.setQtdAcumuladoConvenente(calcularQtdAcumulado(idMedicao, servico, ValorServicoBM::getQtdConvenente));
		servico.setQtdAcumuladoConcedente(calcularQtdAcumulado(idMedicao, servico, ValorServicoBM::getQtdConcedente));

		servico.setValorRealizadoEmpresa(nullSafeMultiply(servico.getPreco(), servico.getQtdRealizadoEmpresa()));
		servico.setValorRealizadoConvenente(nullSafeMultiply(servico.getPreco(), servico.getQtdRealizadoConvenente()));
		servico.setValorRealizadoConcedente(nullSafeMultiply(servico.getPreco(), servico.getQtdRealizadoConcedente()));

		servico.setValorAcumuladoEmpresa(nullSafeMultiply(servico.getPreco(), servico.getQtdAcumuladoEmpresa()));
		servico.setValorAcumuladoConvenente(nullSafeMultiply(servico.getPreco(), servico.getQtdAcumuladoConvenente()));
		servico.setValorAcumuladoConcedente(nullSafeMultiply(servico.getPreco(), servico.getQtdAcumuladoConcedente()));
	}

	private BigDecimal calcularQtdRealizado(Long idMedicao, ServicoVrplDTO servico,
			Function<ServicoVrplDTO.ValorServicoBM, BigDecimal> funcaoRetornoQuantidade) {

		if (servico.getValoresPorIdMedicao().containsKey(idMedicao)) {
			return funcaoRetornoQuantidade.apply(servico.getValoresPorIdMedicao().get(idMedicao));
		}

		return null;
	}

	private BigDecimal calcularQtdAcumulado(Long idMedicao, ServicoVrplDTO servico,
			Function<ServicoVrplDTO.ValorServicoBM, BigDecimal> funcaoRetornoQuantidade) {

		return servico.getValoresPorIdMedicao().entrySet().stream()
				.filter(entry -> entry.getKey() <= idMedicao)
				.map(Map.Entry::getValue)
				.map(funcaoRetornoQuantidade)
				.filter(Objects::nonNull)
				.reduce(BigDecimal::add)
				.orElse(null);
	}
}
