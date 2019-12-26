package com.genius.primavera.domain;

import com.genius.primavera.application.ExcelFileValid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.http.impl.io.EmptyInputStream;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@Getter
@Setter
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
	public InputStream getInputStream() throws IOException {
		if (file == null) return EmptyInputStream.nullInputStream();
		return this.getFile().getInputStream();
	}
}
