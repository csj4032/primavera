package com.genius.primavera.infrastructure;

import com.genius.primavera.application.validator.*;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApplicationComponent {

	@Bean
	public List<Validator> sizeAndTypeValidation() {
		return List.of(new NullValidator(), new FileSizeValidator(), new MediaTypeValidation());
	}

	@Bean
	public List<Validator> sizeAndTypeAndVersionValidation() {
		return List.of(new NullValidator(), new FileSizeValidator(), new MediaTypeValidation(), new VersionValidation());
	}
}
