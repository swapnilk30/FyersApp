package com.example.fyers.controller;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fyers.dto.FyersTokenDTO;
import com.example.fyers.model.FyersDetails;
import com.example.fyers.model.FyersToken;
import com.example.fyers.service.FyersAuthService;
import com.example.fyers.service.FyersService;

@RestController
@RequestMapping("/api/fyers")
public class FyersController {

	private final FyersService fyersService;
	private final FyersAuthService fyersAuthService;

	public FyersController(FyersService fyersService,FyersAuthService fyersAuthService) {
		this.fyersService = fyersService;
		this.fyersAuthService = fyersAuthService;
	}

	// ------- CRUD for FyersDetails -------

	@PostMapping("/details")
	public FyersDetails createDetails(@RequestBody FyersDetails details) {
		return fyersService.createDetails(details);
	}

	@GetMapping("/details")
	public List<FyersDetails> getAllDetails() {
		return fyersService.getAllDetails();
	}

	@GetMapping("/details/{id}")
	public FyersDetails getDetails(@PathVariable Long id) {
		return fyersService.getDetailsById(id).orElseThrow();
	}

	@DeleteMapping("/details/{id}")
	public void deleteDetails(@PathVariable Long id) {
		fyersService.deleteDetails(id);
	}

	// ------- CRUD for FyersTokens -------

	@PostMapping("/details/{detailsId}/tokens")
	public FyersToken createToken(@PathVariable Long detailsId, @RequestBody String accessToken) {
		return fyersService.createToken(detailsId, accessToken);
	}

	@GetMapping("/details/{detailsId}/tokens")
	public List<FyersToken> getTokensByDetailsId(@PathVariable Long detailsId) {
		return fyersService.getTokensByDetailsId(detailsId);
	}

	@DeleteMapping("/tokens/{tokenId}")
	public void deleteToken(@PathVariable Long tokenId) {
		fyersService.deleteToken(tokenId);
	}

	//http://localhost:8080/api/fyers/authenticate/{username}
    @PostMapping("/authenticate/{username}")
    public FyersTokenDTO authenticate(@PathVariable String username){
        return fyersAuthService.callAuthenticateApi(username);
    }
    
    
    
    @GetMapping("/profile/{username}")
    public ResponseEntity<Map<String, Object>> getProfile(@PathVariable String username) {
    	
    	JSONObject profile = fyersService.getProfile(username);
    	
    	Map<String, Object> profileMap = profile.toMap();// Jackson will serialize properly
    	
        return ResponseEntity.ok(profileMap);
    }

    @GetMapping("/stock-history/{username}")
    public ResponseEntity<JSONObject> getStockHistory(@PathVariable String username) {
        return ResponseEntity.ok(fyersService.getStockHistory(username));
    }

    @GetMapping("/holdings/{username}")
    public ResponseEntity<JSONObject> getHoldings(@PathVariable String username) {
        return ResponseEntity.ok(fyersService.getHoldings(username));
    }

}
