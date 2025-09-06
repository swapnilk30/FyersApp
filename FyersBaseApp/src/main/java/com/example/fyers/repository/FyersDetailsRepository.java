package com.example.fyers.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.fyers.model.FyersDetails;

public interface FyersDetailsRepository extends JpaRepository<FyersDetails, Long>{

	
	Optional<FyersDetails> findByUsername(String username);
    Optional<FyersDetails> findByClientId(String clientId);
}
