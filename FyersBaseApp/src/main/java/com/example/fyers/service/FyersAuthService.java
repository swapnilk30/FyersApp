package com.example.fyers.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.fyers.dto.FyersTokenDTO;
import com.example.fyers.model.FyersDetails;
import com.example.fyers.model.FyersToken;
import com.example.fyers.repository.FyersDetailsRepository;
import com.example.fyers.repository.FyersTokenRepository;
import com.warrenstrange.googleauth.GoogleAuthenticator;

import jakarta.transaction.Transactional;

@Service
public class FyersAuthService {

	private static final String BASE_URL = "https://api-t2.fyers.in/vagator/v2";
	private static final String API_BASE = "https://api-t1.fyers.in/api/v3";
	private static final String GENERATE_TOKEN_ENDPOINT = "/validate-authcode";

	private final RestTemplate restTemplate;
	private final FyersDetailsRepository detailsRepo;
	private final FyersTokenRepository tokenRepo;

	public FyersAuthService(RestTemplate restTemplate, FyersDetailsRepository detailsRepo,
			FyersTokenRepository tokenRepo) {
		this.restTemplate = restTemplate;
		this.detailsRepo = detailsRepo;
		this.tokenRepo = tokenRepo;
	}
	
	@Transactional
	public FyersTokenDTO callAuthenticateApi(String username) {
		try {
			// 1. Get Fyers user details from DB
			FyersDetails details = detailsRepo.findByUsername(username)
					.orElseThrow(() -> new RuntimeException("FyersDetails not found for username: " + username));
			String clientId = details.getClientId();
			String secretKey = details.getSecretKey();
			String redirectUri = details.getRedirectUri();
			String pin = details.getPin();
			String totpToken = details.getTotpKey();

			// 2. Send OTP
			Map<String, Object> otpRes = sendLoginOtp(username);
			String requestKey = (String) otpRes.get("request_key");

			// 3. Verify OTP with TOTP
			Map<String, Object> otpVerify = verifyOtp(requestKey, totpToken);
			String otpVerifiedRequestKey = (String) otpVerify.get("request_key");
			
			//String.valueOf(pin);
			
			// 4. Verify PIN
			Map<String, Object> pinVerify = verifyPin(otpVerifiedRequestKey, pin);
			Map<String, Object> data = (Map<String, Object>) pinVerify.get("data");

			// 5. Extract temporary token
			String tempAccessToken = (String) data.get("access_token");

			// 6. Exchange for final access + refresh tokens
			Map<String, Object> tokenResp = exchangeForAccessToken(username, clientId, redirectUri, tempAccessToken, secretKey);
			
			String finalAccessToken = (String) tokenResp.get("access_token");
			String refreshToken = (String) tokenResp.get("refresh_token");
			
			Long expiresIn = tokenResp.get("expires_in") != null ? Long.valueOf(tokenResp.get("expires_in").toString())
					: null;
			
			// 7. Save token in DB
	        FyersToken tokenEntity = new FyersToken();
	        tokenEntity.setClientId(clientId);
	        tokenEntity.setAccessToken(finalAccessToken);
	        tokenEntity.setRefreshToken(refreshToken);
	        tokenEntity.setExpiresIn(expiresIn);
	        tokenEntity.setCreatedAt(LocalDateTime.now());
	        tokenEntity.setUpdatedAt(LocalDateTime.now());
	        FyersToken savedToken = tokenRepo.save(tokenEntity);
	        
	        // Convert to DTO
	        return new FyersTokenDTO(
	                savedToken.getClientId(),
	                savedToken.getAccessToken(),
	                savedToken.getRefreshToken(),
	                savedToken.getExpiresIn(),
	                savedToken.getCreatedAt(),
	                savedToken.getUpdatedAt(),
	                details.getUsername()
	        );
	        
		} catch (Exception e) {
			throw new RuntimeException("Authentication failed for username: " + username, e);
		}
	}

	private Map<String, Object> exchangeForAccessToken(String username, String clientId, String redirectUri, String tempAccessToken,
			String secretKey) {
		
		try {
			// Step 1: Prepare Token API request
	        Map<String, Object> tokenReq = Map.of(
	                "fyers_id", username,
	                "app_id", clientId.substring(0, clientId.length() - 4),
	                "redirect_uri", redirectUri,
	                "appType", "100",
	                "code_challenge", "",
	                "state", "None",
	                "scope", "",
	                "nonce", "",
	                "response_type", "code",
	                "create_cookie", true
	        );
	        
	        HttpHeaders headers = new HttpHeaders();
	        //headers.setBearerAuth(tempAccessToken);
	        headers.set("authorization", "Bearer " + tempAccessToken);
	        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(tokenReq, headers);
	        
	        // Step 2: Call Token API to get Auth URL
	        ResponseEntity<Map> tokenRespEntity = restTemplate.postForEntity(
	                "https://api-t1.fyers.in/api/v3/token",
	                requestEntity,
	                Map.class
	        );
	        
	        Map<String, Object> tokenResp = tokenRespEntity.getBody();
	        if (tokenResp == null || !tokenResp.containsKey("Url")) {
	            throw new RuntimeException("Invalid response from Fyers token API");
	        }
	        String authUrl = (String) tokenResp.get("Url");
	        String authCode = extractAuthCode(authUrl);
	        
	        // Step 3: Exchange Auth Code for Access Token
	        return generateToken(authCode, clientId, secretKey);
	        
		} catch (Exception e) {
			throw new RuntimeException("Failed to exchange for access token: " + e.getMessage(), e);
		}
	}

	private Map<String, Object> generateToken(String authCode, String clientId, String secretKey) {
		try {
			// Step 1: Create SHA-256 hash of clientId:secretKey
			String raw = clientId + ":" + secretKey;
			
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
			StringBuilder hexString = new StringBuilder();
			for (byte b : hash) {
				hexString.append(String.format("%02x", b));
			}
			String appIdHash = hexString.toString();
			
	        // Step 2: Prepare request body
			Map<String, String> body = new HashMap<>();
			body.put("grant_type", "authorization_code");
			body.put("appIdHash", appIdHash);
			body.put("code", authCode);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);
	        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
	        // Step 3: Call Generate Token API
			ResponseEntity<Map> response = restTemplate.postForEntity(API_BASE + GENERATE_TOKEN_ENDPOINT, entity,
					Map.class);

	        Map<String, Object> tokenResponse = response.getBody();
	        if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
	            throw new RuntimeException("Failed to generate token, response: " + response);
	        }

	        return tokenResponse;

		} catch (Exception e) {
			throw new RuntimeException("Error while generating token: " + e.getMessage(), e);
		}
	}

	private String extractAuthCode(String authUrl) {
		String authCode = null;
		try {

			URI uri = new URI(authUrl);
			String[] params = uri.getQuery().split("&");
			for (String p : params) {
				if (p.startsWith("auth_code=")) {
					authCode = p.split("=")[1];
					return authCode;
				}
			}
		} catch (Exception e) {

		}
		return authCode;
	}

	private Map<String, Object> verifyPin(String requestKey, String pin) {
		String url = BASE_URL + "/verify_pin_v2";
		Map<String, Object> payload = Map.of("request_key", requestKey, "identity_type", "pin", "identifier",
				getEncodedString(pin));
		return restTemplate.postForObject(url, payload, Map.class);
	}

	private Map<String, Object> sendLoginOtp(String username) {
		String url = BASE_URL + "/send_login_otp_v2";
		Map<String, Object> payload = Map.of("fy_id", getEncodedString(username), "app_id", "2");
		return restTemplate.postForObject(url, payload, Map.class);
	}

	private Map<String, Object> verifyOtp(String requestKey, String totpKey) {
		GoogleAuthenticator gAuth = new GoogleAuthenticator();
		int otp = gAuth.getTotpPassword(totpKey);

		String url = BASE_URL + "/verify_otp";
		Map<String, Object> payload = Map.of("request_key", requestKey, "otp", otp);

		return restTemplate.postForObject(url, payload, Map.class);
	}

	private String getEncodedString(String input) {
		return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
	}

}
