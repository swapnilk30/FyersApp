package com.example.fyers.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OhlcData {

	private long epoch;
	private double open;
	private double high;
	private double low;
	private double close;
	private long volume;
	
	private String utcTime;
	private String istTime;


}
