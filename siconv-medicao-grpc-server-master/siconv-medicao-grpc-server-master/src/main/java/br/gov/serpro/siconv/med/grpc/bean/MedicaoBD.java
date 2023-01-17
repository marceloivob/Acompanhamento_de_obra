package br.gov.serpro.siconv.med.grpc.bean;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import lombok.Data;

@Data
public class MedicaoBD {
		@ColumnName("id")
		public Long id;
	
}
