package com.genius.primavera.application;

import java.io.IOException;
import java.io.InputStream;

@FunctionalInterface
public interface ExcelFileValid {

	InputStream getInputStream() throws IOException;
}
