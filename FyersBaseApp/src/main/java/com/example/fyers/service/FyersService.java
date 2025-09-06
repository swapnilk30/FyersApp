package com.example.fyers.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.fyers.model.FyersDetails;
import com.example.fyers.model.FyersToken;
import com.example.fyers.repository.FyersDetailsRepository;
import com.example.fyers.repository.FyersTokenRepository;

@Service
public class FyersService {
	
    private final FyersDetailsRepository detailsRepo;
    private final FyersTokenRepository tokenRepo;

    public FyersService(FyersDetailsRepository detailsRepo, FyersTokenRepository tokenRepo) {
        this.detailsRepo = detailsRepo;
        this.tokenRepo = tokenRepo;
    }
	
    // ------- CRUD for FyersDetails -------
    
	public FyersDetails createDetails(FyersDetails details) {
        return detailsRepo.save(details);
    }

	public List<FyersDetails> getAllDetails() {
        return detailsRepo.findAll();
    }

	public Optional<FyersDetails> getDetailsById(Long id) {
        return detailsRepo.findById(id);
    }
	
	
	public void deleteDetails(Long id) {
        detailsRepo.deleteById(id);
    }
	
	 // ------- CRUD for FyersTokens -------
	  public FyersToken createToken(Long detailsId, String accessToken) {
	        FyersDetails details = detailsRepo.findById(detailsId)
	                .orElseThrow(() -> new RuntimeException("FyersDetails not found"));

	        FyersToken token = new FyersToken();
	        token.setAccessToken(accessToken);
	        token.setCreatedAt(LocalDateTime.now());
	        token.setUpdatedAt(LocalDateTime.now());
	        token.setFyersDetails(details);

	        return tokenRepo.save(token);
	    }

	    public List<FyersToken> getTokensByDetailsId(Long detailsId) {
	        return tokenRepo.findByFyersDetailsId(detailsId);
	    }

	    public void deleteToken(Long tokenId) {
	        tokenRepo.deleteById(tokenId);
	    }
	    
	    public void callAuthenticateApiFyers(){
	    	
	    }

}
