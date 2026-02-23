package com.kos.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Restaurent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer restaurentId; 
	private String restaurentName;
	
	
	public Integer getRestaurentId() {
		return restaurentId;
	}
	public void setRestaurentId(Integer restaurentId) {
		this.restaurentId = restaurentId;
	}
	public String getRestaurentName() {
		return restaurentName;
	}
	public void setRestaurentName(String restaurentName) {
		this.restaurentName = restaurentName;
	}
	
	
	
}
