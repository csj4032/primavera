package com.genius.primavera.domain;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvDate;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class KakaoTalkChat {

	@CsvDate(value = "yyyy-MM-dd HH:mm:ss")
	@CsvBindByPosition(position = 0, required = true)
	@CsvBindByName
	private LocalDateTime date;
	@CsvBindByPosition(position = 1, required = true)
	@CsvBindByName
	private String user;
	@CsvBindByPosition(position = 2, required = true)
	@CsvBindByName
	private String message;
}