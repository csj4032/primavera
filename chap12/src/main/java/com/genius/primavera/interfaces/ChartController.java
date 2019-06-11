package com.genius.primavera.interfaces;

import com.genius.primavera.domain.repository.PrimaveraLogRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.spring5.context.webflux.IReactiveSSEDataDriverContextVariable;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class ChartController {

    @Autowired
    private PrimaveraLogRepository primaveraLogRepository;

    @GetMapping(value = "/chart")
    public String logs(final Model model) {
        final IReactiveSSEDataDriverContextVariable primaveraLogFlux = new ReactiveDataDriverContextVariable(primaveraLogRepository.findAll(), 1, 1);
        model.addAttribute("logs", primaveraLogFlux);
        return "chart";
    }
}