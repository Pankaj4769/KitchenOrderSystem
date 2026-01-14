package com.kos.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.kos.dto.Item;
import com.kos.repository.InventoryRepository;
import com.kos.validation.InventoryValidator;

@Service
public class InventoryService {
	
	Logger logger = LoggerFactory.getLogger(InventoryService.class);
	
	@Autowired
	InventoryRepository inventoryRepository;
	
	public Item addItem(Item item) {
	    InventoryValidator.validateItemId(item);
	    InventoryValidator.validateQuantity(item);
		try {
			return inventoryRepository.save(item);
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

}
