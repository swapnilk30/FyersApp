package com.example.fyers.controller;

import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.fyers.service.FyersService;

@Controller
@RequestMapping("/fyers")
public class FyersViewController {
	
	//import from org.slf4j
	private static final Logger log=LoggerFactory.getLogger(FyersViewController.class);
	
	private final FyersService fyersService;

    public FyersViewController(FyersService fyersService) {
        this.fyersService = fyersService;
    }
	
    
    // Example: GET /api/fyers/swapnil/quotes?symbols=NSE:TCS-EQ,NSE:SBIN-EQ
	@GetMapping("/{username}/quotes")
    public String viewStockQuotes(@PathVariable String username, @RequestParam(defaultValue = "NSE:TCS-EQ") String symbols,Model model) {
        
		log.info("start: FyersViewController ---> viewStockQuotes id is {}",username);
		JSONObject quotes = fyersService.GetStockQuotes(username, symbols);
		Map<String, Object> quotesMap = quotes.toMap();
		model.addAttribute("quotes",quotesMap.get("d") );
        model.addAttribute("username", username); 
        return "quotes";
    }


}
