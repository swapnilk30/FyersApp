package com.example.fyers.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fyers_tokens")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FyersToken {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String clientId;

	@Column(name = "access_token", columnDefinition = "TEXT")
	private String accessToken;

	@Column(name = "refresh_token", columnDefinition = "TEXT")
	private String refreshToken;

	private Long expiresIn;

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	// Link token to details via clientId (foreign key)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fyers_details_id", nullable = false)
	private FyersDetails fyersDetails;
	
	@PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
