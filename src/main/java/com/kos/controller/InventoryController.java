package com.kos.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kos.dto.Item;
import com.kos.dto.MessageResponse;
import com.kos.service.InventoryService;


@RestController
public class InventoryController {

    @Value("${item.image.upload.path}")
    private String uploadPath;

	@Autowired
	InventoryService inventoryService;
	
	@GetMapping("/health")
	public ResponseEntity<String> getHealth() {
		return new ResponseEntity<String>("Ok health",HttpStatus.OK);
	}
	
	@PostMapping("/addItem")
	public ResponseEntity<Item> addItem(@RequestBody Item item){
		
		return new ResponseEntity<Item>(inventoryService.addItem(item), HttpStatus.OK);
		
	}
	@PatchMapping("/restockItem")
	public ResponseEntity<Item> restockItem(@RequestBody Item item){
		
		return new ResponseEntity<Item>(inventoryService.restockItem(item), HttpStatus.OK);
		
	}
	
	// ✅ Update Item (name, price, category, status)
    @PatchMapping("/updateItem")
    public ResponseEntity<Item> updateItem(@RequestBody Item item) {
        return new ResponseEntity<Item>(inventoryService.updateItem(item), HttpStatus.OK);
    }
	
	

	
	@GetMapping("/getAllItems/{restId}")
	public ResponseEntity<List<Item>> getAllItems(@PathVariable String restId){
		return new ResponseEntity<List<Item>>(inventoryService.getAllItems(restId), HttpStatus.OK);
	}
	
	@DeleteMapping("/deleteItemById/{id}")
	public ResponseEntity<MessageResponse> deleteItemById(@PathVariable String id){
		return new ResponseEntity<MessageResponse>(inventoryService.deleteItemById(id), HttpStatus.OK);
	}
	
	@PatchMapping("/updateItemStatus/{itemId}/{status}")
	public ResponseEntity<Item> updateItemStatus(@PathVariable String itemId, @PathVariable String status){

		return new ResponseEntity<Item>(inventoryService.updateItemStatus(Integer.parseInt(itemId), Boolean.parseBoolean(status)), HttpStatus.OK);

	}

    /**
     * POST /uploadItemImage
     * Accepts a multipart image, renames it to sanitized(itemName).ext,
     * saves it to assets/{restaurantId}/, and returns the filename.
     * Frontend stores only the filename in the DB.
     */
    @PostMapping("/uploadItemImage")
    public ResponseEntity<Map<String, String>> uploadItemImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam("itemName") String itemName,
            @RequestParam("restaurantId") String restaurantId) {
        try {
            // Derive filename: strip everything except letters/digits, append extension
            String baseName = itemName.trim().replaceAll("[^a-zA-Z0-9]", "");
            if (baseName.isEmpty()) baseName = "item";

            String original = image.getOriginalFilename();
            String ext = "jpg";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase();
            }
            String fileName = baseName + "." + ext;

            // Ensure target directory exists
            Path dir = Paths.get(uploadPath, restaurantId);
            Files.createDirectories(dir);

            // Write file (overwrites any existing file with same name)
            Path dest = dir.resolve(fileName);
            image.transferTo(dest.toFile());

            return ResponseEntity.ok(Map.of("fileName", fileName));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

}
