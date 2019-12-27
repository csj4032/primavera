package com.genius.primavera.application.factory;

import com.genius.primavera.domain.ExcelImportRequest;
import com.genius.primavera.domain.ExcelImportResponse;
import com.genius.primavera.domain.Financial;
import com.genius.primavera.domain.MediaType;
import com.monitorjbl.xlsx.StreamingReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
					Financial financial = Financial.builder()
							.segment(row.getCell(0).getStringCellValue())
							.country(row.getCell(1).getStringCellValue())
							.product(row.getCell(2).getStringCellValue())
							.discountBand(row.getCell(3).getStringCellValue())
							.unitsSold(row.getCell(4).getNumericCellValue())
							.manufacturingPrice(BigDecimal.valueOf(row.getCell(5).getNumericCellValue()))
							.salePrice(BigDecimal.valueOf(row.getCell(6).getNumericCellValue()))
							.grossSales(BigDecimal.valueOf(row.getCell(7).getNumericCellValue()))
							.discounts(row.getCell(8).getStringCellValue())
							.sales(BigDecimal.valueOf(row.getCell(9).getNumericCellValue()))
							.COGS(BigDecimal.valueOf(row.getCell(10).getNumericCellValue()))
							.profit(BigDecimal.valueOf(row.getCell(11).getNumericCellValue()))
							.date(row.getCell(12).getDateCellValue().toInstant())
							.build();
					financialList.add(financial);
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