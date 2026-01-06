package com.kos.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kos.dto.Item;
import com.kos.repository.ItemRepository;

@Service
public class ItemService {
	
	Logger logger = LoggerFactory.getLogger(ItemService.class);
	
	@Autowired
	ItemRepository itemRepository;
	
	public Item addItem(Item item) {
		try {
			return itemRepository.save(item);
		}catch(Exception e){
			logger.debug("Not able to  save item. Some Exception occurred");
			return new Item();
		}
		 
	}

}
