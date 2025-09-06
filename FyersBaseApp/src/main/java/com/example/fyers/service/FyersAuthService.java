package com.example.fyers.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.fyers.model.FyersDetails;
import com.example.fyers.model.FyersToken;
import com.example.fyers.repository.FyersDetailsRepository;
import com.example.fyers.repository.FyersTokenRepository;

@Service
public class FyersAuthService {

	private final RestTemplate restTemplate;
	private final FyersDetailsRepository detailsRepo;
	private final FyersTokenRepository tokenRepo;

	public FyersAuthService(RestTemplate restTemplate, FyersDetailsRepository detailsRepo,
			FyersTokenRepository tokenRepo) {
		this.restTemplate = restTemplate;
		this.detailsRepo = detailsRepo;
		this.tokenRepo = tokenRepo;
	}

	public FyersToken callAuthenticateApi(String username) {
		try {
			FyersDetails details = detailsRepo.findByUsername(username)
					.orElseThrow(() -> new RuntimeException("FyersDetails not found for username: " + username));
		    String clientId = details.getClientId();
	        String secretKey = details.getSecretKey();
	        String redirectUri = details.getRedirectUri();
	        int pin = details.getPin();
	        String totpToken = details.getTotpKey();
	        
	        
		} catch (Exception e) {

		}
		return null;
	}

}
