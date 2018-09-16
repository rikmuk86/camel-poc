package com.camel.poc.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UIController {
	
	@RequestMapping("/home")
	public String home() {
		return "forward:index.html";
	}
}
