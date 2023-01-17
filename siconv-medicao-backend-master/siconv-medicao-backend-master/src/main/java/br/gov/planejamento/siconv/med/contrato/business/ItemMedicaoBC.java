package br.gov.planejamento.siconv.med.contrato.business;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.gov.planejamento.siconv.med.integration.contratos.ContratosGrpcConsumer;
import br.gov.planejamento.siconv.med.integration.vrpl.VrplGRPCConsumer;
import br.gov.planejamento.siconv.med.medicao.entity.dto.EventoFrenteObraTotalizadoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.ServicoFrenteObraTotalizadoDTO;
import br.gov.planejamento.siconv.med.medicao.entity.dto.SubmetaVrplDTO;

@ApplicationScoped
public class ItemMedicaoBC {

	@Inject
	private ContratosGrpcConsumer contratosConsumer;
	
	@Inject
	private VrplGRPCConsumer vrplConsumer;
	
	
	public List<EventoFrenteObraTotalizadoDTO> getEventoFO(Long contratoFk) {

		List<EventoFrenteObraTotalizadoDTO> retorno = new ArrayList<>();

		List<SubmetaVrplDTO> listaSubmetas = contratosConsumer.listarSubmetasPorContratoId(contratoFk);

		List<Long> idsSubmetas = listaSubmetas.stream().map(SubmetaVrplDTO::getId).collect(toList());

		listaSubmetas = vrplConsumer.getListaSubmetasPorId(idsSubmetas);

		listaSubmetas.forEach(sub -> sub.getFrentesObras().forEach(fo -> fo.getEventos().forEach(ev -> {
			EventoFrenteObraTotalizadoDTO item = new EventoFrenteObraTotalizadoDTO();
			item.setIdSubmetaVrpl(sub.getId());
			item.setIdFrenteObra(fo.getId());
			item.setIdEvento(ev.getId());
			item.setTotalEvento(ev.getValor());
			retorno.add(item);
		})));

		return retorno;
	}
	
	public List<ServicoFrenteObraTotalizadoDTO> getMacroServicoFO(Long contratoFk) {

		List<ServicoFrenteObraTotalizadoDTO> retorno = new ArrayList<>();

		List<SubmetaVrplDTO> listaSubmetas = contratosConsumer.listarSubmetasPorContratoId(contratoFk);

		List<Long> idsSubmetas = listaSubmetas.stream().map(SubmetaVrplDTO::getId).collect(toList());

		listaSubmetas = vrplConsumer.getListaSubmetasPorId(idsSubmetas);

		listaSubmetas.forEach(sub -> 
			sub.getFrentesObras().forEach(fo -> fo.getMacroServicosView().forEach(macroservico -> 
				macroservico.getServicos().forEach(servico -> {
					ServicoFrenteObraTotalizadoDTO item = new ServicoFrenteObraTotalizadoDTO();
					item.setIdSubmetaVrpl(sub.getId());
					item.setIdFrenteObra(fo.getId());
					item.setIdServico(servico.getId());
					item.setQtdeServico(servico.getQtd());
					item.setVlPrecoUnitarioLicitado(servico.getPreco());
					retorno.add(item);
				})
			))
		);

		return retorno;
	}
	
}
