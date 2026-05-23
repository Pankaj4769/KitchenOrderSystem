package com.kos.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.kos.dto.Item;
import com.kos.dto.ItemCategory;
import com.kos.dto.MessageResponse;
import com.kos.repository.InventoryRepository;
import com.kos.service.storage.ImageStorageService;
import com.kos.validation.InventoryValidator;
import com.kos.repository.ItemCategoryRepository;

@Service
public class InventoryService {

	private static final Logger logger = LogManager.getLogger(InventoryService.class);

	@Autowired
	InventoryRepository inventoryRepository;

	@Autowired
	ItemCategoryRepository itemCategoryRepository;

	@Autowired
	ImageStorageService imageStorage;

	private String saveImage(String base64Image, String restaurantId) {
	    logger.info("Entering saveImage()");
	    try {
	        if (base64Image == null || !base64Image.startsWith("data:image")) {
	            logger.info("Exiting saveImage()");
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
	        String result = "/assets/" + folderName + "/" + filename;
	        logger.info("Exiting saveImage()");
	        return result;
	    } catch (Exception e) {
	        logger.error("Error in saveImage(): {}", e.getMessage(), e);
	        return null;
	    }
	}

	public Item addItem(Item item) {
	    logger.info("Entering addItem()");
	    try {
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
				logger.info("Exiting addItem()");
				return itm;
			}catch(Exception e){
				logger.debug("Not able to  save item. Some Exception occurred", e);
				logger.info("Exiting addItem()");
				return new Item();
			}
	    } catch (RuntimeException e) {
	        logger.error("Error in addItem(): {}", e.getMessage(), e);
	        throw e;
	    }
	}
	public Item restockItem(Item item) {
		logger.info("Entering restockItem()");
		try {
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
			 Item result = inventoryRepository.save(existingItem);
			 logger.info("Exiting restockItem()");
			 return result;
		} catch (RuntimeException e) {
			logger.error("Error in restockItem(): {}", e.getMessage(), e);
			throw e;
		}
	}

	public List<String> getMenuCategories(String restId) {
		logger.info("Entering getMenuCategories()");
		try {
			List<String> result = itemCategoryRepository.findDistinctCategoriesByRestaurantId(restId);
			logger.info("Exiting getMenuCategories()");
			return result;
		} catch (RuntimeException e) {
			logger.error("Error in getMenuCategories(): {}", e.getMessage(), e);
			throw e;
		}
	}

	public List<Item> getAllItems(String restId){
		logger.info("Entering getAllItems()");
		try {
			Optional<List<Item>>  itemList= inventoryRepository.findItemListByRestaurantId(restId);
			if(itemList.isEmpty()) {
				logger.info("Exiting getAllItems()");
				return new ArrayList<Item>();
			}
			for(Item item: itemList.get()) {
				List<String> cList = itemCategoryRepository.findCategoryByItemId(item.getItemId());
				item.setCategories(cList);
			}
			logger.info("Exiting getAllItems()");
			return itemList.get();
		} catch (RuntimeException e) {
			logger.error("Error in getAllItems(): {}", e.getMessage(), e);
			throw e;
		}
	}

	public Item getItemById(Integer itemId){
		logger.info("Entering getItemById()");
		try {
			Optional<Item>  itm= inventoryRepository.findById(itemId);
			Item existing = itm.get();
			List<String> cList = itemCategoryRepository.findCategoryByItemId(existing.getItemId());
			existing.setCategories(cList);
			logger.info("Exiting getItemById()");
			return existing;
		} catch (RuntimeException e) {
			logger.error("Error in getItemById(): {}", e.getMessage(), e);
			throw e;
		}
	}

	public Item updateItemStatus(Integer itemId, boolean status) {
		logger.info("Entering updateItemStatus()");
		try {
			Item existing = getItemById(itemId);
			if(existing != null) {
				existing.setItem_status(status);
				inventoryRepository.save(existing);
				logger.info("Exiting updateItemStatus()");
				return existing;

			}else {
				logger.info("Exiting updateItemStatus()");
				return new Item();
			}
		} catch (RuntimeException e) {
			logger.error("Error in updateItemStatus(): {}", e.getMessage(), e);
			throw e;
		}
	}

	public MessageResponse deleteItemById(String id) {
		logger.info("Entering deleteItemById() with id={}", id);
		try {
			Item existing = getItemById(Integer.parseInt(id));
			if(existing != null) {
				itemCategoryRepository.deleteCategoryByItemId(existing.getItemId());
				inventoryRepository.delete(existing);
				logger.info("Exiting deleteItemById()");
				return new MessageResponse("Success", true);
				}else {
					logger.info("Exiting deleteItemById()");
					return new MessageResponse("Failure", false);
				}
		} catch (RuntimeException e) {
			logger.error("Error in deleteItemById(): {}", e.getMessage(), e);
			throw e;
		}
	}
	public Item updateItem(Item item) {
		logger.info("Entering updateItem()");
		try {
			if (item.getItemId() == null) {
				logger.warn("updateItem called with null itemId — skipping update");
				logger.info("Exiting updateItem()");
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

			    Item result = inventoryRepository.save(existing);
			    logger.info("Exiting updateItem()");
			    return result;
			}
			logger.info("Exiting updateItem()");
			return null;
		} catch (RuntimeException e) {
			logger.error("Error in updateItem(): {}", e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * Atomic add: saves the Item row and (if provided) writes the image file
	 * inside one transaction. If either step fails, the Item row is rolled
	 * back so no orphan DB record exists. (The small window where commit fails
	 * after disk write would still leave a file behind — accepted; cleanable.)
	 */
	@Transactional
	public Item addItemWithImage(Item item, @Nullable MultipartFile image) {
		logger.info("Entering addItemWithImage()");
		InventoryValidator.validateQuantity(item);
		item.setItem_status(true);

		Item saved = inventoryRepository.saveAndFlush(item);
		if (saved.getItemId() == null) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to persist item");
		}

		if (image != null && !image.isEmpty()) {
			String key = imageStorage.store(image, item.getRestaurantId(), item.getItemName());
			saved.setItemImgName(key);
			saved = inventoryRepository.saveAndFlush(saved);
		}

		if (item.getCategories() != null) {
			for (String category : item.getCategories()) {
				ItemCategory itemCategory = new ItemCategory();
				itemCategory.setCategoryType(category);
				itemCategory.setItemId(saved.getItemId());
				itemCategoryRepository.save(itemCategory);
			}
		}

		logger.info("Exiting addItemWithImage()");
		return saved;
	}

	/**
	 * Atomic update: applies field changes and (if a new image is provided)
	 * stores it and deletes the old one. Old-image deletion runs only after
	 * the row update succeeds; if the DB save throws, the new file is left
	 * behind but no DB inconsistency results.
	 */
	@Transactional
	public Item updateItemWithImage(Item item, @Nullable MultipartFile image) {
		logger.info("Entering updateItemWithImage()");
		if (item.getItemId() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "itemId is required");
		}

		Item existing = inventoryRepository.findById(item.getItemId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"Item with ID " + item.getItemId() + " not found"));

		existing.setItemName(item.getItemName());
		existing.setItemPrice(item.getItemPrice());
		existing.setItem_status(item.getItem_status());
		existing.setItemQuantity(item.getItemQuantity());
		existing.setItemType(item.getItemType());
		existing.setFromTime(item.getFromTime());
		existing.setToTime(item.getToTime());

		String oldKey = existing.getItemImgName();
		if (image != null && !image.isEmpty()) {
			String newKey = imageStorage.store(image, existing.getRestaurantId(), item.getItemName());
			existing.setItemImgName(newKey);
		}

		itemCategoryRepository.deleteCategoryByItemId(existing.getItemId());
		if (item.getCategories() != null) {
			for (String category : item.getCategories()) {
				ItemCategory itemCategory = new ItemCategory();
				itemCategory.setItemId(existing.getItemId());
				itemCategory.setCategoryType(category);
				itemCategoryRepository.save(itemCategory);
			}
		}

		Item result = inventoryRepository.save(existing);

		// Best-effort cleanup of the old image after a successful field update.
		if (image != null && !image.isEmpty() && oldKey != null && !oldKey.equals(result.getItemImgName())) {
			imageStorage.delete(oldKey);
		}

		logger.info("Exiting updateItemWithImage()");
		return result;
	}

}
