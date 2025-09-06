package com.example.fyers.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.fyers.model.FyersToken;

public interface FyersTokenRepository extends JpaRepository<FyersToken, Long>{

	
	 List<FyersToken> findByFyersDetailsId(Long detailsId);
}
