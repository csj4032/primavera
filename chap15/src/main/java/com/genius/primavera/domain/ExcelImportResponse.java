package com.genius.primavera.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

@Getter
@Setter
public class ExcelImportResponse extends RepresentationModel<ExcelImportResponse> {
	private String name;
	private long size;

	public ExcelImportResponse(String name, long size) {
		this.name = name;
		this.size = size;
	}
}
