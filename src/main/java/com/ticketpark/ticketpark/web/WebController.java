package com.ticketpark.ticketpark.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String webMain(){
        return "main";
    }

    @GetMapping("/join")
    public String joinPage(){
        return "index";
    }

}
