package com.example.fyers.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeViewController {
	
	@GetMapping
	public String getHome() {
		return "index";
	}

}
