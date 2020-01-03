package com.genius.primavera.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class ExcelImportResponse extends RepresentationModel<ExcelImportResponse> {
	private String name;
	private long size;
	private MediaType mediaType;
	private String message;

	public ExcelImportResponse(String name, long size, MediaType mediaType, String message) {
		this.name = name;
		this.size = size;
		this.mediaType = mediaType;
		this.message = message;
	}
}
