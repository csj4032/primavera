package com.genius.primavera.application.template;

import com.monitorjbl.xlsx.StreamingReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

@Slf4j
public class ExcelImportTemplate<T> {

	private final InputStream inputStream;

	public ExcelImportTemplate(final InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public List<T> read(final Function<Row, T> function) {
		List<T> list = Collections.emptyList();
		Workbook workbook = StreamingReader.builder().open(inputStream);
		Iterator<Sheet> sheets = workbook.sheetIterator();
		while (sheets.hasNext()) {
			Sheet sheet = sheets.next();
			Iterator<Row> rows = sheet.iterator();
			while (rows.hasNext()) {
				Row row = rows.next();
				if (row.getRowNum() == 0) continue;
				list.add(function.apply(row));
			}
		}
		try {
			inputStream.close();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return list;
	}
}
