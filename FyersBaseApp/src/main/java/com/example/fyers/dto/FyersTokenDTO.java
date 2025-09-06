package com.example.fyers.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FyersTokenDTO {

	private String clientId;
	private String accessToken;
	private String refreshToken;
	private Long expiresIn;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String username; // from FyersDetails

}
