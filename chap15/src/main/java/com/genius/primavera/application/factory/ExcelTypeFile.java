package com.genius.primavera.application.factory;

import com.genius.primavera.domain.ExcelImportRequest;
import com.genius.primavera.domain.ExcelImportResponse;
import com.genius.primavera.domain.Financial;
import com.genius.primavera.domain.MediaType;
import com.monitorjbl.xlsx.StreamingReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class ExcelTypeFile extends AbstractResponseFactory implements ResponseFactory {

	public ExcelTypeFile(ExcelImportRequest excelImportRequest) {
		super(excelImportRequest);
	}

	public ExcelImportResponse getExcelImportResponse() {
		List<Financial> financialList = new ArrayList<>();
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		try {
			Workbook workbook = StreamingReader.builder().open(excelImportRequest.getInputStream());
			Iterator<Sheet> sheets = workbook.sheetIterator();
			while (sheets.hasNext()) {
				Sheet sheet = sheets.next();
				Iterator<Row> rows = sheet.iterator();
				while (rows.hasNext()) {
					Row row = rows.next();
					if (row.getRowNum() == 0) continue;
					financialList.add(Financial.of(row));
				}
			}
			workbook.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		stopWatch.stop();
		log.debug("elapsed : " + stopWatch.getTotalTimeMillis());
		log.debug("financialList : {}" + financialList);
		return new ExcelImportResponse(excelImportRequest.getName(), excelImportRequest.getSize(), MediaType.EXCEL_TYPE, "row count : " + financialList.size());
	}
}