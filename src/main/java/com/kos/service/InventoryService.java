package com.kos.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.kos.dto.Item;
import com.kos.dto.ItemCategory;
import com.kos.repository.InventoryRepository;
import com.kos.validation.InventoryValidator;
import com.kos.repository.ItemCategoryRepository;

@Service
public class InventoryService {
	
	Logger logger = LoggerFactory.getLogger(InventoryService.class);
	
	@Autowired
	InventoryRepository inventoryRepository;
	
	@Autowired
	ItemCategoryRepository itemCategoryRepository;
	
	public Item addItem(Item item) {
	  
	    InventoryValidator.validateQuantity(item);
		try {
			item.setItem_status(true);
			Item itm = inventoryRepository.save(item);
			if(itm != null && itm.getItemId()!= null) {
				for(String category: item.getCategories()) {
					ItemCategory itemCategory = new ItemCategory();
					itemCategory.setCategoryType(category);
					itemCategory.setItemId(itm.getItemId());
					itemCategoryRepository.save(itemCategory);
				}
			}
			return itm;
		}catch(Exception e){
			logger.debug("Not able to  save item. Some Exception occurred");
			return new Item();
		}
		 
	}
	public Item restockItem(Item item) {
		
	    InventoryValidator.validateItemId(item);
	    InventoryValidator.validateQuantity(item);
	    Item existingItem = inventoryRepository.findById(item.getItemId())
	    		.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
	    				"Item with ID " + item.getItemId() + " not found"));
	    
		 existingItem.setItemQuantity(
				    existingItem.getItemQuantity() + item.getItemQuantity()
				);
		 return inventoryRepository.save(existingItem);
	}
	
	public List<Item> getAllItems(){
		List<Item>  itemList= inventoryRepository.findAll();
		for(Item item: itemList) {
			List<String> cList = itemCategoryRepository.findCategoryByItemId(item.getItemId());
			item.setCategories(cList);
		}
		return itemList;
	}
	
	public Item getItemById(Integer itemId){
		Optional<Item>  itm= inventoryRepository.findById(itemId);
		Item existing = itm.get();
		List<String> cList = itemCategoryRepository.findCategoryByItemId(existing.getItemId());
		existing.setCategories(cList);
		return existing;
	}
	
	public Item updateItemStatus(Integer itemId, boolean status) {
		Item existing = getItemById(itemId);
		if(existing != null) {
			existing.setItem_status(status);
			inventoryRepository.save(existing);
			return existing;
			
		}else {
			return new Item();
		}
	}

}
