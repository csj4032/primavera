package com.genius.primavera.application.template;

import org.apache.poi.ss.usermodel.Row;

import java.util.List;
import java.util.function.Function;

public interface ExcelImportTemplate<T> {

	List<T> read(final Function<Row, T> function);

}
