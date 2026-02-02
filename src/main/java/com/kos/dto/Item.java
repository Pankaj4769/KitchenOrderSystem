package com.kos.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Transient;

@Entity
public class Item {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonProperty("id")
	private Integer itemId;
	@JsonProperty("name")
	private String itemName;
	@JsonProperty("image")
	private String itemImgName;
	@JsonProperty("price")
	private Integer itemPrice;
	@JsonProperty("qty")
	private Integer itemQuantity;
	@JsonProperty("group")
	private String itemType;
	@JsonProperty("enabled")
	private boolean item_status;
	@JsonProperty("from")
	private String fromTime;
	@JsonProperty("to")
	private String toTime;
	
	
	@JsonProperty("category")
	@ElementCollection
	@CollectionTable(name = "item_categories", joinColumns = @JoinColumn(name = "item_id"))
	@Column(name = "category")
	@Transient
	private List<String> categories;
	
	
	public Integer getItemId() {
		return itemId;
	}
	public void setItemId(Integer itemId) {
		this.itemId = itemId;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public String getItemImgName() {
		return itemImgName;
	}
	public void setItemImgName(String itemImgName) {
		this.itemImgName = itemImgName;
	}
	public Integer getItemPrice() {
		return itemPrice;
	}
	public void setItemPrice(Integer itemPrice) {
		this.itemPrice = itemPrice;
	}
	public Integer getItemQuantity() {
		return itemQuantity;
	}
	public void setItemQuantity(Integer itemQuantity) {
		this.itemQuantity = itemQuantity;
	}
	public String getItemType() {
		return itemType;
	}
	public void setItemType(String itemType) {
		this.itemType = itemType;
	}
	public List<String> getCategories() {
		return categories;
	}
	public void setCategories(List<String> categories) {
		this.categories = categories;
	}
	public boolean getItem_status() {
		return item_status;
	}
	public void setItem_status(boolean item_status) {
		this.item_status = item_status;
	}
	public String getFromTime() {
		return fromTime;
	}
	public void setFromTime(String fromTime) {
		this.fromTime = fromTime;
	}
	public String getToTime() {
		return toTime;
	}
	public void setToTime(String toTime) {
		this.toTime = toTime;
	}
	
	
	

}
