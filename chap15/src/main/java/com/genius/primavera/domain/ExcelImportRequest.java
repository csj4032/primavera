package com.genius.primavera.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExcelImportRequest {
	private String name;
	private MultipartFile file;

	public long getSize() {
		if (Objects.isNull(file)) return 0;
		return file.getSize();
	}
}
