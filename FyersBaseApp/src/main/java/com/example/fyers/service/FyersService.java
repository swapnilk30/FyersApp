package com.example.fyers.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.example.fyers.model.FyersDetails;
import com.example.fyers.model.FyersToken;
import com.example.fyers.repository.FyersDetailsRepository;
import com.example.fyers.repository.FyersTokenRepository;
import com.tts.in.model.FyersClass;
import com.tts.in.model.StockHistoryModel;
import com.tts.in.utilities.Tuple;

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

	private FyersClass initializeFyers(String username) {

		FyersDetails details = detailsRepo.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("No Fyers details found for username: " + username));

		// Get the latest token
		FyersToken latestToken = details.getTokens().stream().max(Comparator.comparing(FyersToken::getCreatedAt))
				.orElseThrow(() -> new RuntimeException("No token found for username: " + username));

		FyersClass fyersClass = FyersClass.getInstance();
		fyersClass.clientId = details.getClientId();
		fyersClass.accessToken = latestToken.getAccessToken();

		return fyersClass;

	}

	public JSONObject getStockHistory(String username) {
		FyersClass fyersClass = initializeFyers(username);

		StockHistoryModel model = new StockHistoryModel();
		model.Symbol = "NSE:SBIN-EQ";
		model.Resolution = "30";
		model.DateFormat = "1";
		model.RangeFrom = "2021-01-01";
		model.RangeTo = "2022-02-03";
		model.ContFlag = 1;

		Tuple<JSONObject, JSONObject> stockTuple = fyersClass.GetStockHistory(model);

		if (stockTuple.Item2() == null) {
			return stockTuple.Item1();
		} else {
			throw new RuntimeException("Stock History Error: " + stockTuple.Item2());
		}
	}

	public JSONObject getHoldings(String username) {
		FyersClass fyersClass = initializeFyers(username);

		Tuple<JSONObject, JSONObject> holdingTuple = fyersClass.GetHoldings();
		if (holdingTuple.Item2() == null) {
			return holdingTuple.Item1();
		} else {
			throw new RuntimeException("Holdings Error: " + holdingTuple.Item2());
		}
	}

	public JSONObject getProfile(String username) {

		FyersClass fyersClass = initializeFyers(username);
		Tuple<JSONObject, JSONObject> ProfileResponseTuple = fyersClass.GetProfile();

		if (ProfileResponseTuple.Item2() == null) {
			System.out.println("Profile: " + ProfileResponseTuple.Item1());
			return ProfileResponseTuple.Item1();
		} else {
			System.out.println("Profile Error: " + ProfileResponseTuple.Item2());
			throw new RuntimeException("Error fetching profile: " + ProfileResponseTuple.Item2());
		}
	}

}
