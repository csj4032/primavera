package com.genius.primavera.domain;

import com.genius.primavera.application.ExcelFileValid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.io.EmptyInputStream;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@Getter
@Setter
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class ExcelImportRequest implements ExcelFileValid {

	private String name;
	private MultipartFile file;

	public long getSize() {
		if (Objects.isNull(file)) return 0;
		return file.getSize();
	}

	@Override
	public InputStream getInputStream() {
		InputStream inputStream = EmptyInputStream.nullInputStream();
		try {
			inputStream = this.getFile().getInputStream();
		} catch (IOException | NullPointerException e) {
			log.error(e.getMessage());
		}
		return inputStream;
	}
}
