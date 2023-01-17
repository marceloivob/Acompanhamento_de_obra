package br.gov.planejamento.siconv.med.infra.rest.response;

import static br.gov.planejamento.siconv.med.infra.util.ApplicationProperties.APPLICATION_JSON_UTF8;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public final class ResponseHelper {

	private ResponseHelper() {
	}

	public static <T> DefaultResponse<T> success(T data) {
		return new DefaultResponse<>(ResponseStatus.SUCCESS, data);
	}

	public static <T> Response success(int statusCode, T data) {
		return build(statusCode, new DefaultResponse<>(ResponseStatus.SUCCESS, data));
	}

	public static <T> Response success(int statusCode, T data, String etagValue) {
		return build(statusCode, new DefaultResponse<>(ResponseStatus.SUCCESS, data), EntityTag.valueOf(etagValue));
	}

	public static <T> Response ok(T data) {
		return success(Status.OK.getStatusCode(), data);
	}

	public static <T> Response ok(T data, String etagValue) {
		return success(Status.OK.getStatusCode(), data, etagValue);
	}

	public static <T> Response fail(int statusCode, T data) {
		return build(statusCode, new DefaultResponse<>(ResponseStatus.FAIL, data));
	}

	public static <T> Response error(int statusCode, String message, T data) {
		return build(statusCode, new DefaultResponse<>(ResponseStatus.ERROR, message, data));
	}

	private static <T> Response build(int statusCode, DefaultResponse<T> entity) {
		return builder(statusCode, entity).build();
	}

	private static <T> Response build(int statusCode, DefaultResponse<T> entity, EntityTag etag) {
		return builder(statusCode, entity).tag(etag).build();
	}

	private static <T> ResponseBuilder builder(int statusCode, DefaultResponse<T> entity) {
		return Response.status(statusCode).type(APPLICATION_JSON_UTF8).entity(entity);
	}

	@JsonInclude(value = Include.NON_NULL)
	@AllArgsConstructor
	@RequiredArgsConstructor
	@Getter
	public static class DefaultResponse<T> {

		private final ResponseStatus status;
		private String message;
		private final T data;
	}

	public enum ResponseStatus {
		SUCCESS, FAIL, ERROR;
	}
}
