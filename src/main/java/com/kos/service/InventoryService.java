package com.kos.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.kos.dto.Item;
import com.kos.dto.ItemCategory;
import com.kos.dto.MessageResponse;
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

	private String saveImage(String base64Image, String restaurantId) {
	    try {
	        if (base64Image == null || !base64Image.startsWith("data:image")) {
	            return base64Image;
	        }
	        
	        String[] parts = base64Image.split(",");
	        String imageString = parts.length > 1 ? parts[1] : parts[0];
	        
	        byte[] imageBytes = Base64.getDecoder().decode(imageString);
	        
	        // The absolute path requested by the user
	        String basePath = "D:/kos_new/kosUI/kosUI/src/assets";
	        
	        // Create the restaurant-specific folder if it doesn't exist
	        String folderName = (restaurantId != null && !restaurantId.isBlank()) ? restaurantId : "default";
	        Path path = Paths.get(basePath, folderName);
	        if (!Files.exists(path)) {
	            Files.createDirectories(path);
	        }
	        
	        String filename = UUID.randomUUID().toString() + ".jpg";
	        Path filePath = path.resolve(filename);
	        Files.write(filePath, imageBytes);
	        
	        // Return the relative path for the frontend (e.g. "/assets/77/uuid.jpg")
	        return "/assets/" + folderName + "/" + filename;
	    } catch (Exception e) {
	        logger.error("Failed to save image", e);
	        return null;
	    }
	}

	public Item addItem(Item item) {
	  
	    InventoryValidator.validateQuantity(item);
		try {
			item.setItem_status(true);
			
			// Process Base64 image and save as file
			if (item.getItemImgName() != null && item.getItemImgName().startsWith("data:image")) {
			    item.setItemImgName(saveImage(item.getItemImgName(), item.getRestaurantId()));
			}
			
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
			logger.debug("Not able to  save item. Some Exception occurred", e);
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
	
	public List<String> getMenuCategories(String restId) {
		return itemCategoryRepository.findDistinctCategoriesByRestaurantId(restId);
	}

	public List<Item> getAllItems(String restId){
		Optional<List<Item>>  itemList= inventoryRepository.findItemListByRestaurantId(restId);
		if(itemList.isEmpty()) {
			return new ArrayList<Item>();
		}
		for(Item item: itemList.get()) {
			List<String> cList = itemCategoryRepository.findCategoryByItemId(item.getItemId());
			item.setCategories(cList);
		}
		return itemList.get();
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
	
	public MessageResponse deleteItemById(String id) {
		Item existing = getItemById(Integer.parseInt(id));
		if(existing != null) {
			itemCategoryRepository.deleteCategoryByItemId(existing.getItemId());
			inventoryRepository.delete(existing);
			return new MessageResponse("Success", true);
			}else {
				return new MessageResponse("Failure", false);
			}
	}
	public Item updateItem(Item item) {
		if (item.getItemId() == null) {
			logger.warn("updateItem called with null itemId — skipping update");
			return new Item();
		}
		Item existing = getItemById(item.getItemId());

		if (existing != null) {
		    existing.setItemName(item.getItemName());
		    existing.setItemPrice(item.getItemPrice());
		    existing.setItem_status(item.getItem_status());
		    existing.setItemQuantity(item.getItemQuantity());
		    existing.setItemType(item.getItemType());
		    existing.setFromTime(item.getFromTime());
		    existing.setToTime(item.getToTime());
		    
		    // Persist image — process Base64 if a new one was provided, otherwise keep existing URL
		    if (item.getItemImgName() != null && !item.getItemImgName().isBlank()) {
		        if (item.getItemImgName().startsWith("data:image")) {
		            existing.setItemImgName(saveImage(item.getItemImgName(), existing.getRestaurantId()));
		        } else {
		            existing.setItemImgName(item.getItemImgName());
		        }
		    }
		    
		    itemCategoryRepository.deleteCategoryByItemId(existing.getItemId());

		    for (String category : item.getCategories()) {
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
