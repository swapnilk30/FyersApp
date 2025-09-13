package com.example.fyers.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.example.fyers.dto.OhlcData;
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

	public List<OhlcData> getStockHistory(String username) {

		FyersClass fyersClass = initializeFyers(username);

		StockHistoryModel model = new StockHistoryModel();
		model.Symbol = "NSE:SBIN-EQ";
		model.Resolution = "60";
		model.DateFormat = "1";
		model.RangeFrom = "2025-09-01";
		model.RangeTo = "2025-09-13";
		model.ContFlag = 1;

		Tuple<JSONObject, JSONObject> stockTuple = fyersClass.GetStockHistory(model);

		List<OhlcData> list = new ArrayList<>();

		if (stockTuple.Item2() == null) {
			JSONObject result = stockTuple.Item1();
			if (result.has("candles")) {
				for (Object candleObj : result.getJSONArray("candles")) {
					// JSONArray element → cast safely
					org.json.JSONArray candle = (org.json.JSONArray) candleObj;

					long epoch = candle.getLong(0);

					// --- Convert Epoch -> Readable Time ---
					String utcTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z").withZone(ZoneId.of("UTC"))
							.format(Instant.ofEpochSecond(epoch));

					String istTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
							.withZone(ZoneId.of("Asia/Kolkata")).format(Instant.ofEpochSecond(epoch));

					// --- Build OHLC Data (extended with formatted time) ---
					OhlcData ohlc = new OhlcData(
							epoch, candle.getDouble(1), // open
							candle.getDouble(2), // high
							candle.getDouble(3), // low
							candle.getDouble(4), // close
							candle.getLong(5), // volume
							utcTime, istTime);

					list.add(ohlc);
				}
			}
			return list;
		} else {
			throw new RuntimeException("Stock History Error: " + stockTuple.Item2());
		}
	}
	
	public List<OhlcData> getStockHistory(String username,String symbol) {

		FyersClass fyersClass = initializeFyers(username);
		
		String exchange = "NSE";
		String secType = (symbol.equalsIgnoreCase("NIFTY") || symbol.equalsIgnoreCase("BANKNIFTY")) ? "INDEX" : "EQ";
		String formatSymbol = formatSymbol(exchange, symbol, secType);
		
		StockHistoryModel model = new StockHistoryModel();
		model.Symbol = formatSymbol;
		model.Resolution = "60";
		model.DateFormat = "1";
		model.RangeFrom = "2025-09-01";
		model.RangeTo = "2025-09-13";
		model.ContFlag = 1;

		Tuple<JSONObject, JSONObject> stockTuple = fyersClass.GetStockHistory(model);

		List<OhlcData> list = new ArrayList<>();

		if (stockTuple.Item2() == null) {
			JSONObject result = stockTuple.Item1();
			if (result.has("candles")) {
				for (Object candleObj : result.getJSONArray("candles")) {
					// JSONArray element → cast safely
					org.json.JSONArray candle = (org.json.JSONArray) candleObj;

					long epoch = candle.getLong(0);

					// --- Convert Epoch -> Readable Time ---
					String utcTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z").withZone(ZoneId.of("UTC"))
							.format(Instant.ofEpochSecond(epoch));

					String istTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
							.withZone(ZoneId.of("Asia/Kolkata")).format(Instant.ofEpochSecond(epoch));

					// --- Build OHLC Data (extended with formatted time) ---
					OhlcData ohlc = new OhlcData(
							epoch, candle.getDouble(1), // open
							candle.getDouble(2), // high
							candle.getDouble(3), // low
							candle.getDouble(4), // close
							candle.getLong(5), // volume
							utcTime, istTime);

					list.add(ohlc);
				}
			}
			return list;
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

	public JSONObject GetStockQuotes(String username, String symbols) {
		FyersClass fyersClass = initializeFyers(username);

		Tuple<JSONObject, JSONObject> stockTuple = fyersClass.GetStockQuotes(symbols);

		if (stockTuple.Item2() == null) {
			System.out.println("Stock Quotes:" + stockTuple.Item1());
			return stockTuple.Item1();
		} else {
			System.out.println("Error: " + stockTuple.Item2());
			throw new RuntimeException("Error fetching GetStockQuotes: " + stockTuple.Item2());
		}

	}
	
	private String formatSymbol(String exchange, String symbol, String secType) {
	    return exchange + ":" + symbol + "-" + secType;
	}

}
