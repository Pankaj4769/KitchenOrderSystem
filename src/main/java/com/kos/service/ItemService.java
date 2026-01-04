package com.kos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kos.dto.Item;
import com.kos.repository.ItemRepository;

@Service
public class ItemService {
	
	@Autowired
	ItemRepository itemRepository;
	
	public Item addItem(Item item) {
		return itemRepository.save(item);
	}

}
