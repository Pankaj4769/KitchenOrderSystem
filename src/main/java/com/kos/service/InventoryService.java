package com.kos.service;

import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
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
			Item itm = inventoryRepository.saveAndFlush(item);
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
	    
	    if(existingItem.getItemQuantity()==0) {
		 existingItem.setItemQuantity(
				    existingItem.getItemQuantity() + item.getItemQuantity()
				);
	    }else {
	    	 existingItem.setItemQuantity(
					    item.getItemQuantity());
	    }
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
	
	public String deleteItemById(String id) {
		Item existing = getItemById(Integer.parseInt(id));
		if(existing != null) {
			Optional<ItemCategory> category = itemCategoryRepository.findById(Integer.parseInt(id));
			if(category.isPresent()) {
				itemCategoryRepository.delete(category.get());
				inventoryRepository.delete(existing);
				return "Success";
			}else {
				return "Failure";
			}
		}else {
			return "Failure";
		}
	}
	public Item updateItem(Item item) {
		Item existing = getItemById(item.getItemId());
      
		if (existing !=null) {
		    existing.setItemName(item.getItemName());
		    existing.setItemPrice(item.getItemPrice());
		    existing.setItem_status(item.getItem_status());
		    existing.setItemQuantity(item.getItemQuantity());
		  itemCategoryRepository.deleteCategoryByItemId(existing.getItemId());
		    
		    for (String category:item.getCategories()) {
		    	ItemCategory itemCategory = new ItemCategory();
		    	itemCategory.setItemId(item.getItemId());
		    	itemCategory.setCategoryType(category);
		    	itemCategoryRepository.save(itemCategory);
		    }
		    
		    return inventoryRepository.save(existing);
		}
    return null;
}

}
