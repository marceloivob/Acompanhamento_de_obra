package br.gov.planejamento.siconv.med.infra.rest.mapper;

import static com.fasterxml.jackson.databind.AnnotationIntrospector.pair;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import br.gov.planejamento.siconv.med.infra.security.serializer.SensitiveDataAnnotationIntrospector;

@Provider
public class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper;

    public ObjectMapperContextResolver() {
        mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());

        AnnotationIntrospector defaultIntrospector = mapper.getSerializationConfig().getAnnotationIntrospector();
        AnnotationIntrospector customIntrospector = pair(defaultIntrospector,
                new SensitiveDataAnnotationIntrospector());
        mapper.setAnnotationIntrospector(customIntrospector);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}