package com.genius.primavera.application.template;

import java.util.List;
import java.util.function.Function;

@FunctionalInterface
public interface ExcelImportTemplate<T, R> {

	List<R> read(final Function<T, R> function);

}