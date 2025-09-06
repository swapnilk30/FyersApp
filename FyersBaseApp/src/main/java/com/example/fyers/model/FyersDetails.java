package com.example.fyers.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "fyers_details")
public class FyersDetails {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Username is required")
	@Column(nullable = false, unique = true)
	private String username;

	@NotBlank(message = "Secret Key is required")
	private String secretKey;

	@NotBlank(message = "Client ID is required")
	private String clientId;

	@NotBlank(message = "Redirect URI is required")
    private String redirectUri;

    private String totpKey;

    private String pin;
    
    // One client can have many tokens
    @OneToMany(mappedBy = "fyersDetails", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FyersToken> tokens = new ArrayList<>();

}
