package com.genius.primavera.interfaces;

import com.genius.primavera.domain.model.PrimaveraLog;
import com.genius.primavera.domain.repository.PrimaveraLogRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.result.view.Rendering;
import org.thymeleaf.spring5.context.webflux.IReactiveDataDriverContextVariable;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

import java.time.Duration;

@Slf4j
@Controller
public class ChartController {

	@Autowired
	private PrimaveraLogRepository primaveraLogRepository;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor(staticName = "of")
	static class Message {
		String payload;
	}

	@GetMapping(value = "/chart")
	public String chart(Model model) {
//		return Rendering
//				.view("chart")
//				.modelAttribute("message", "messages")
//				.modelAttribute("messages", new ReactiveDataDriverContextVariable(
//						Flux.zip(
//								Flux.interval(Duration.ofSeconds(1)),
//								Flux.just(
//										Message.of("and one"),
//										Message.of("and two"),
//										Message.of("and three"),
//										Message.of("and four!"))
//						).map(Tuple2::getT2),
//						1
//				))
//				.build();
		final IReactiveDataDriverContextVariable logs = new ReactiveDataDriverContextVariable(primaveraLogRepository.findAll(), 1);
		model.addAttribute("message", "Hello");
		model.addAttribute("logs", logs);
		return "chart";
	}

	@GetMapping(value = "/chartFlux")
	public @ResponseBody
	Flux<PrimaveraLog> chartFlux() {
		return primaveraLogRepository.findAll();
	}
}
