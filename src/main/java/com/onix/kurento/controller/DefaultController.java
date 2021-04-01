package com.onix.kurento.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public final class DefaultController {

    @GetMapping
    public String index(
            final Model model,
            @Value("${callstats.appId}") final int appId,
            @Value("${callstats.appSecret}") final String appSecret
    ) {
        model.addAttribute("appId", appId);
        model.addAttribute("appSecret", appSecret);
        return "index";
    }

}
