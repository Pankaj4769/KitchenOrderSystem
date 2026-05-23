package com.kos.controller;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kos.dto.Item;
import com.kos.dto.MessageResponse;
import com.kos.service.InventoryService;
import com.kos.service.storage.ImageStorageService;


@RestController
public class InventoryController {

    private static final Logger logger = LogManager.getLogger(InventoryController.class);

	@Autowired
	InventoryService inventoryService;

	@Autowired
	ImageStorageService imageStorage;

	@GetMapping("/health")
	public ResponseEntity<String> getHealth() {
		logger.info("Entering getHealth()");
		try {
			ResponseEntity<String> result = new ResponseEntity<String>("Ok health",HttpStatus.OK);
			logger.info("Exiting getHealth()");
			return result;
		} catch (RuntimeException e) {
			logger.error("Error in getHealth(): {}", e.getMessage(), e);
			throw e;
		}
	}

	@PostMapping("/addItem")
	public ResponseEntity<Item> addItem(@RequestBody Item item){
		logger.info("Entering addItem()");
		try {
			ResponseEntity<Item> result = new ResponseEntity<Item>(inventoryService.addItem(item), HttpStatus.OK);
			logger.info("Exiting addItem()");
			return result;
		} catch (RuntimeException e) {
			logger.error("Error in addItem(): {}", e.getMessage(), e);
			throw e;
		}
	}
	@PatchMapping("/restockItem")
	public ResponseEntity<Item> restockItem(@RequestBody Item item){
		logger.info("Entering restockItem()");
		try {
			ResponseEntity<Item> result = new ResponseEntity<Item>(inventoryService.restockItem(item), HttpStatus.OK);
			logger.info("Exiting restockItem()");
			return result;
		} catch (RuntimeException e) {
			logger.error("Error in restockItem(): {}", e.getMessage(), e);
			throw e;
		}
	}

	// ✅ Update Item (name, price, category, status)
    @PatchMapping("/updateItem")
    public ResponseEntity<Item> updateItem(@RequestBody Item item) {
        logger.info("Entering updateItem()");
        try {
            ResponseEntity<Item> result = new ResponseEntity<Item>(inventoryService.updateItem(item), HttpStatus.OK);
            logger.info("Exiting updateItem()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in updateItem(): {}", e.getMessage(), e);
            throw e;
        }
    }




	@GetMapping("/getAllItems/{restId}")
	public ResponseEntity<List<Item>> getAllItems(@PathVariable String restId){
		logger.info("Entering getAllItems() with restId={}", restId);
		try {
			ResponseEntity<List<Item>> result = new ResponseEntity<List<Item>>(inventoryService.getAllItems(restId), HttpStatus.OK);
			logger.info("Exiting getAllItems()");
			return result;
		} catch (RuntimeException e) {
			logger.error("Error in getAllItems(): {}", e.getMessage(), e);
			throw e;
		}
	}

	@DeleteMapping("/deleteItemById/{id}")
	public ResponseEntity<MessageResponse> deleteItemById(@PathVariable String id){
		logger.info("Entering deleteItemById() with id={}", id);
		try {
			ResponseEntity<MessageResponse> result = new ResponseEntity<MessageResponse>(inventoryService.deleteItemById(id), HttpStatus.OK);
			logger.info("Exiting deleteItemById()");
			return result;
		} catch (RuntimeException e) {
			logger.error("Error in deleteItemById(): {}", e.getMessage(), e);
			throw e;
		}
	}

	@PatchMapping("/updateItemStatus/{itemId}/{status}")
	public ResponseEntity<Item> updateItemStatus(@PathVariable String itemId, @PathVariable String status){
		logger.info("Entering updateItemStatus() with itemId={}", itemId);
		try {
			ResponseEntity<Item> result = new ResponseEntity<Item>(inventoryService.updateItemStatus(Integer.parseInt(itemId), Boolean.parseBoolean(status)), HttpStatus.OK);
			logger.info("Exiting updateItemStatus()");
			return result;
		} catch (RuntimeException e) {
			logger.error("Error in updateItemStatus(): {}", e.getMessage(), e);
			throw e;
		}
	}

    /**
     * Legacy: standalone image upload (kept for backward compatibility).
     * New callers should prefer /addItemWithImage or /updateItemWithImage,
     * which are atomic with the item row save.
     */
    @PostMapping("/uploadItemImage")
    public ResponseEntity<Map<String, String>> uploadItemImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam("itemName") String itemName,
            @RequestParam("restaurantId") String restaurantId) {
        logger.info("Entering uploadItemImage() with restaurantId={}", restaurantId);
        String key = imageStorage.store(image, restaurantId, itemName);
        logger.info("Exiting uploadItemImage()");
        return ResponseEntity.ok(Map.of("fileName", key));
    }

    /**
     * Combined atomic add: persists the item row and writes its image inside
     * one transaction. The `item` part is the JSON body that /addItem accepts;
     * the `image` part is optional. Use multipart/form-data.
     */
    @PostMapping(path = "/addItemWithImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Item> addItemWithImage(
            @RequestPart("item") Item item,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        logger.info("Entering addItemWithImage()");
        try {
            Item saved = inventoryService.addItemWithImage(item, image);
            logger.info("Exiting addItemWithImage()");
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            logger.error("Error in addItemWithImage(): {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Combined atomic update: applies field changes and optionally replaces
     * the image inside one transaction. Old image (if any and replaced) is
     * deleted after the row save succeeds.
     */
    @PatchMapping(path = "/updateItemWithImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Item> updateItemWithImage(
            @RequestPart("item") Item item,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        logger.info("Entering updateItemWithImage()");
        try {
            Item saved = inventoryService.updateItemWithImage(item, image);
            logger.info("Exiting updateItemWithImage()");
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            logger.error("Error in updateItemWithImage(): {}", e.getMessage(), e);
            throw e;
        }
    }

}
