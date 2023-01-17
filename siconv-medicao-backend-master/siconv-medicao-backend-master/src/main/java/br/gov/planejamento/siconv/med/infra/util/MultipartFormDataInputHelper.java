package br.gov.planejamento.siconv.med.infra.util;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class MultipartFormDataInputHelper {

    private static final String DEFAULT_CHARSET = "UTF-8";

    private MultipartFormDataInput multipart;

    public MultipartFormDataInputHelper(MultipartFormDataInput multipart) {
        this.multipart = multipart;
        configureCharset();
    }

    private void configureCharset() {
        multipart.getFormDataMap().values().stream().flatMap(List::stream)
                .forEach(inputPart -> inputPart.setMediaType(inputPart.getMediaType().withCharset(DEFAULT_CHARSET)));
    }

    private <T> T getFormDataPart(String key, Class<T> rawType) throws IOException {
        return multipart.getFormDataPart(key, rawType, null);
    }

    public String getString(String key) throws IOException {
        return getFormDataPart(key, String.class);
    }

    public Long getLong(String key) throws IOException {
        String strLong = getString(key);
        return isEmpty(strLong) ? null : Long.valueOf(strLong);
    }

    public LocalDate getLocalDate(String key) throws IOException {
        String srtDate = getString(key);
        return isEmpty(srtDate) ? null : LocalDate.parse(srtDate);
    }
    
    public LocalDateTime getLocalDateTime(String key) throws IOException {
        String srtDate = getString(key);
        
        if(isEmpty(srtDate)) {
        	return null;
        } else {
        	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
            return LocalDateTime.parse(srtDate, formatter);
        }

    }

    public <R, T extends JavaType> R getJsonType(String key, Function<TypeFactory, T> typeConstructor)
            throws IOException {
        String strJson = getString(key);

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());

        return !isEmpty(strJson) ? mapper.readValue(strJson, typeConstructor.apply(mapper.getTypeFactory())) : null;
    }
}