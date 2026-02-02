package com.kos.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.kos.dto.Item;
import com.kos.service.InventoryService;


@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class InventoryController {
	
	@Autowired
	InventoryService inventoryService;
	
	@GetMapping("/health")
	public ResponseEntity<String> getHealth() {
		return new ResponseEntity<String>("Ok",HttpStatus.OK);
	}
	
	@PostMapping("/addItem")
	public ResponseEntity<Item> addItem(@RequestBody Item item){
		
		return new ResponseEntity<Item>(inventoryService.addItem(item), HttpStatus.OK);
		
	}
	@PatchMapping("/restockItem")
	public ResponseEntity<Item> restockItem(@RequestBody Item item){
		
		return new ResponseEntity<Item>(inventoryService.restockItem(item), HttpStatus.OK);
		
	}
	
	

	
	@GetMapping("/getAllItems")
	public ResponseEntity<List<Item>> getAllItems(){
		return new ResponseEntity<List<Item>>(inventoryService.getAllItems(), HttpStatus.OK);
	}
	
	@DeleteMapping("/deleteItemById/{id}")
	public ResponseEntity<String> deleteItem(@PathVariable String itemId){
		return new ResponseEntity<String>(inventoryService.deleteItemById(itemId), HttpStatus.OK);
	}
	
	@PatchMapping("/updateItemStatus/{itemId}/{status}")
	public ResponseEntity<Item> updateItemStatus(@PathVariable String itemId, @PathVariable String status){
		
		return new ResponseEntity<Item>(inventoryService.updateItemStatus(Integer.parseInt(itemId), Boolean.parseBoolean(status)), HttpStatus.OK);
		
	}

}
