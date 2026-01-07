package com.kos.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kos.dto.Item;
import com.kos.repository.InventoryRepository;

@Service
public class InventoryService {
	
	Logger logger = LoggerFactory.getLogger(InventoryService.class);
	
	@Autowired
	InventoryRepository inventoryRepository;
	
	public Item addItem(Item item) {
		try {
			return inventoryRepository.save(item);
		}catch(Exception e){
			logger.debug("Not able to  save item. Some Exception occurred");
			return new Item();
		}
		 
	}

}
